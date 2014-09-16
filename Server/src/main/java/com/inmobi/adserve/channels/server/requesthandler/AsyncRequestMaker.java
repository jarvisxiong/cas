package com.inmobi.adserve.channels.server.requesthandler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ChannelServer;
import com.inmobi.adserve.channels.server.SegmentFactory;
import com.inmobi.adserve.channels.types.AdFormatType;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.MetricsManager;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.phoenix.batteries.util.WilburyUUID;

import io.netty.channel.Channel;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@Singleton
public class AsyncRequestMaker {
    private static final Logger  LOG = LoggerFactory.getLogger(AsyncRequestMaker.class);
    private static final AtomicInteger counter = new AtomicInteger();

    private final SegmentFactory segmentFactory;

    @Inject
    public AsyncRequestMaker(final SegmentFactory segmentFactory) {
        this.segmentFactory = segmentFactory;
    }

    /**
     * For each channel we configure the parameters and make the async request if the async request is successful we add
     * it to segment list else we drop it
     */
    public List<ChannelSegment> prepareForAsyncRequest(final List<ChannelSegment> rows, final Configuration config,
            final Configuration rtbConfig, final Configuration adapterConfig, final HttpRequestHandlerBase base,
            final Set<String> advertiserSet, final Channel channel, final RepositoryHelper repositoryHelper,
            final SASRequestParameters sasParams, final CasInternalRequestParameters casInternalRequestParameterGlobal,
            final List<ChannelSegment> rtbSegments) throws Exception {

        List<ChannelSegment> segments = new ArrayList<ChannelSegment>();

        LOG.debug("Total channels available for sending requests {}", rows.size());
        boolean isRtbEnabled = rtbConfig.getBoolean("isRtbEnabled", false);
        int rtbMaxTimeOut = rtbConfig.getInt("RTBreadtimeoutMillis", 200);
        LOG.debug("isRtbEnabled is {}  and rtbMaxTimeout is {}", isRtbEnabled, rtbMaxTimeOut);

        /*
         NOTE: For a request that qualifies the in-banner video criteria, at this point we don't know whether an
         interstitial video response will be sent or Banner.
         At this point, the creative type is set to Banner for video supported requests. If the request gets fullfiled
         with a video ad, creative type will be chosen accordingly.
        */
        ADCreativeType creativeType = isNativeRequest(sasParams) ? ADCreativeType.NATIVE : ADCreativeType.BANNER;
        LOG.debug("Creative type is : {}", creativeType);

        for (ChannelSegment row : rows) {
            ChannelSegmentEntity channelSegmentEntity = row.getChannelSegmentEntity();
            AdNetworkInterface network = segmentFactory.getChannel(channelSegmentEntity.getAdvertiserId(), row
                    .getChannelSegmentEntity().getChannelId(), adapterConfig, null, null, base, channel, advertiserSet,
                    isRtbEnabled, rtbMaxTimeOut, sasParams.getDst(), repositoryHelper);
            if (null == network) {
                LOG.debug("No adapter found for adGroup: {}", channelSegmentEntity.getAdgroupId());
                continue;
            }
            LOG.debug("adapter found for adGroup: {} advertiserid is {} is {}", channelSegmentEntity.getAdgroupId(),
                    row.getChannelSegmentEntity().getAdvertiserId(), network.getName());
            if (null == repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId())) {
                LOG.debug("No channel entity found for channel id: {}", channelSegmentEntity.getChannelId());
                continue;
            }

            long incId = channelSegmentEntity.getIncId(creativeType);
            if (incId == -1) {
                LOG.debug("Could not find incId for adGroup {} and creativeType {}", channelSegmentEntity.getAdgroupId());
                continue;
            }

            String clickUrl = null;
            String beaconUrl = null;
            sasParams.setImpressionId(getImpressionId(incId));
            CasInternalRequestParameters casInternalRequestParameters = getCasInternalRequestParameters(sasParams,
                    casInternalRequestParameterGlobal, channelSegmentEntity);

            controlEnrichment(casInternalRequestParameters, channelSegmentEntity);
            sasParams.setAdIncId(incId);
            LOG.debug("impression id is {}", sasParams.getImpressionId());

            if ((network.isClickUrlRequired() || network.isBeaconUrlRequired()) && null != sasParams.getImpressionId()) {
                boolean isCpc = false;
                if (null != channelSegmentEntity.getPricingModel()
                        && channelSegmentEntity.getPricingModel().equalsIgnoreCase("cpc")) {
                    isCpc = true;
                }
                ClickUrlMakerV6 clickUrlMakerV6 = setClickParams(isCpc, config, sasParams,
                        channelSegmentEntity.getDst() - 1);
                clickUrlMakerV6.createClickUrls();
                clickUrl = clickUrlMakerV6.getClickUrl();
                beaconUrl = clickUrlMakerV6.getBeaconUrl();
                LOG.debug("click url : {}", clickUrl);
                LOG.debug("beacon url : {}", beaconUrl);
            }

            LOG.debug("Sending request to Channel of advsertiserId {}", channelSegmentEntity.getAdvertiserId());
            LOG.debug("external site key is {}", channelSegmentEntity.getExternalSiteKey());

            if (network.configureParameters(sasParams, casInternalRequestParameters, channelSegmentEntity, clickUrl,
                    beaconUrl)) {
                InspectorStats.incrementStatCount(network.getName(), InspectorStrings.successfulConfigure);
                row.setAdNetworkInterface(network);
                if (network.isRtbPartner()) {
                    rtbSegments.add(row);
                    LOG.debug("{} is a rtb partner so adding this network to rtb ranklist", network.getName());
                }
                else {
                    segments.add(row);
                }
            }
        }
        return segments;
    }

    private CasInternalRequestParameters getCasInternalRequestParameters(final SASRequestParameters sasParams,
            final CasInternalRequestParameters casInternalRequestParameterGlobal,
            final ChannelSegmentEntity channelSegmentEntity) {
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.impressionId = sasParams.getImpressionId();
        casInternalRequestParameters.blockedCategories = casInternalRequestParameterGlobal.blockedCategories;
        casInternalRequestParameters.blockedAdvertisers = casInternalRequestParameterGlobal.blockedAdvertisers;
        casInternalRequestParameters.highestEcpm = casInternalRequestParameterGlobal.highestEcpm;
        casInternalRequestParameters.rtbBidFloor = casInternalRequestParameterGlobal.rtbBidFloor;
        casInternalRequestParameters.auctionId = casInternalRequestParameterGlobal.auctionId;
        casInternalRequestParameters.uid = casInternalRequestParameterGlobal.uid;
        casInternalRequestParameters.uidO1 = casInternalRequestParameterGlobal.uidO1;
        casInternalRequestParameters.uidIFA = casInternalRequestParameterGlobal.uidIFA;
        casInternalRequestParameters.gpid = casInternalRequestParameterGlobal.gpid;
        casInternalRequestParameters.uidIFV = casInternalRequestParameterGlobal.uidIFV;
        casInternalRequestParameters.uidSO1 = casInternalRequestParameterGlobal.uidSO1;
        casInternalRequestParameters.uidIDUS1 = casInternalRequestParameterGlobal.uidIDUS1;
        casInternalRequestParameters.uidMd5 = casInternalRequestParameterGlobal.uidMd5;
        casInternalRequestParameters.uidADT = casInternalRequestParameterGlobal.uidADT;
        if (null != sasParams.getPostalCode()) {
            casInternalRequestParameters.zipCode = sasParams.getPostalCode().toString();
        }
        casInternalRequestParameters.latLong = sasParams.getLatLong();
        casInternalRequestParameters.appUrl = sasParams.getAppUrl();
        casInternalRequestParameters.traceEnabled = casInternalRequestParameterGlobal.traceEnabled;
        casInternalRequestParameters.siteAccountType = casInternalRequestParameterGlobal.siteAccountType;

        // Set impressionIdForVideo if banner video is supported on this request.
        casInternalRequestParameters.impressionIdForVideo = getImpressionIdForVideo(sasParams,
                channelSegmentEntity.getAdFormatIds(), channelSegmentEntity.getIncIds());

        return casInternalRequestParameters;
    }

    private String getImpressionIdForVideo(final SASRequestParameters sasParams,
                                           final Integer[] adFormatIds, final Long[] adIncIds) {

        if (!sasParams.isBannerVideoSupported() || adFormatIds == null || adIncIds == null) {
            LOG.debug("In-banner video ad is not supported.");
            return null;
        }

        for (int i = 0; i < adFormatIds.length; i++) {
            //  Get impression id for video ad format.
            if (adFormatIds[i] == AdFormatType.VIDEO.getValue()) {
                String impressionId = getImpressionId(adIncIds[i]);
                LOG.debug("impression id for in-banner video ad is {}.", impressionId);
                return impressionId;
            }
        }
        LOG.debug("Inconsistent data in the database. Could not find a video ad for this ad group.");
        return null;
    }

    private void controlEnrichment(final CasInternalRequestParameters casInternalRequestParameters,
            final ChannelSegmentEntity channelSegmentEntity) {
        if (channelSegmentEntity.isStripUdId()) {
            casInternalRequestParameters.uid = null;
            casInternalRequestParameters.uidO1 = null;
            casInternalRequestParameters.uidMd5 = null;
            casInternalRequestParameters.uidIFA = null;
            casInternalRequestParameters.gpid = null;
            casInternalRequestParameters.uidIFV = null;
            casInternalRequestParameters.uidIDUS1 = null;
            casInternalRequestParameters.uidSO1 = null;
            casInternalRequestParameters.uidADT = null;
        }
        if (channelSegmentEntity.isStripZipCode()) {
            casInternalRequestParameters.zipCode = null;
        }
        if (channelSegmentEntity.isStripLatlong()) {
            casInternalRequestParameters.latLong = null;
        }
        if (!channelSegmentEntity.isAppUrlEnabled()) {
            casInternalRequestParameters.appUrl = null;
        }

    }

    public List<ChannelSegment> makeAsyncRequests(final List<ChannelSegment> rankList, final Channel channel,
            final List<ChannelSegment> rtbSegments) {
        Iterator<ChannelSegment> itr = rankList.iterator();
        while (itr.hasNext()) {
            ChannelSegment channelSegment = itr.next();
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                    InspectorStrings.totalInvocations);
            if (channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
                LOG.debug("Successfully sent request to channel of  advertiser id {} and channel id {}", channelSegment
                        .getChannelSegmentEntity().getId(), channelSegment.getChannelSegmentEntity().getChannelId());
            }
            else {
                itr.remove();
            }
        }
        Iterator<ChannelSegment> rtbItr = rtbSegments.iterator();
        while (rtbItr.hasNext()) {
            ChannelSegment channelSegment = rtbItr.next();
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                    InspectorStrings.totalInvocations);
            if (channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
                LOG.debug("Successfully sent request to rtb channel of  advertiser id {} and channel id {}",
                        channelSegment.getChannelSegmentEntity().getId(), channelSegment.getChannelSegmentEntity()
                                .getChannelId());
            }
            else {
                rtbItr.remove();
            }
        }
        return rankList;
    }

    public String getImpressionId(final long adId) {
        String uuidIntKey = (WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId)).toString();
        String uuidMachineKey = (WilburyUUID.setMachineId(uuidIntKey, ChannelServer.hostIdCode)).toString();
        String uuidWithCyclicCounter = (WilburyUUID.setCyclicCounter(uuidMachineKey,
                (byte) (Math.abs(counter.getAndIncrement() % 128)))).toString();
        return (WilburyUUID.setDataCenterId(uuidWithCyclicCounter, ChannelServer.dataCenterIdCode)).toString();
    }

    private static ClickUrlMakerV6 setClickParams(final boolean pricingModel, final Configuration config,
            final SASRequestParameters sasParams, final Integer dst) {
        ClickUrlMakerV6.Builder builder = ClickUrlMakerV6.newBuilder();
        builder.setImpressionId(sasParams.getImpressionId());
        builder.setAge(null != sasParams.getAge() ? sasParams.getAge().intValue() : 0);
        builder.setCountryId(null != sasParams.getCountryId() ? sasParams.getCountryId().intValue() : 0);
        builder.setLocation(null != sasParams.getState() ? sasParams.getState() : 0);
        builder.setSegmentId(null != sasParams.getSiteSegmentId() ? sasParams.getSiteSegmentId() : 0);
        builder.setGender(null != sasParams.getGender() ? sasParams.getGender() : "");
        builder.setCPC(pricingModel);
        builder.setCarrierId(sasParams.getCarrierId());
        builder.setHandsetInternalId(sasParams.getHandsetInternalId());
        builder.setIpFileVersion(sasParams.getIpFileVersion().longValue());
        builder.setIsBillableDemog(false);
        builder.setSiteIncId(sasParams.getSiteIncId());
        builder.setUdIdVal(sasParams.getTUidParams());
        builder.setCryptoSecretKey(config.getString("clickmaker.key.1.value"));
        builder.setTestCryptoSecretKey(config.getString("clickmaker.key.2.value"));
        builder.setImageBeaconFlag(true);// true/false
        builder.setBeaconEnabledOnSite(true);// do not know
        builder.setTestMode(false);
        builder.setRmAd(sasParams.isRichMedia());
        builder.setRmBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
        builder.setClickURLPrefix(config.getString("clickmaker.clickURLPrefix"));
        builder.setImageBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
        builder.setTestRequest(false);
        builder.setLatlonval(sasParams.getLatLong());
        builder.setRtbSite(sasParams.getSst() != 0);// TODO:- Change this according to thrift enums
        builder.setDst(dst.toString());
        builder.setBudgetBucketId("101"); // Default Value
        return new ClickUrlMakerV6(builder);
    }

    private boolean isNativeRequest(final SASRequestParameters sasParams){
        return "native".equalsIgnoreCase(sasParams.getRFormat());
    }
}
