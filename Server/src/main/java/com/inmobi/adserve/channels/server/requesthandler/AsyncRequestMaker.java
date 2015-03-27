package com.inmobi.adserve.channels.server.requesthandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.SegmentFactory;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl.AdvertiserFailureThrottler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ClickUrlMakerV6;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.ADCreativeType;

import io.netty.channel.Channel;


@Singleton
public class AsyncRequestMaker {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncRequestMaker.class);

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
        final List<ChannelSegment> segments = new ArrayList<ChannelSegment>();
        LOG.debug("Total channels available for sending requests {}", rows.size());
        /*
         NOTE: For a request that qualifies the in-banner video criteria, at this point we don't know whether an
         interstitial video response will be sent or Banner.
         At this point, the creative type is set to Banner for video supported requests. If the request gets fullfiled
         with a video ad, creative type will be chosen accordingly.
        */
        final ADCreativeType creativeType = isNativeRequest(sasParams) ? ADCreativeType.NATIVE : ADCreativeType.BANNER;
        LOG.debug("Creative type is : {}", creativeType);

        for (final ChannelSegment row : rows) {
            final ChannelSegmentEntity channelSegmentEntity = row.getChannelSegmentEntity();
            final AdNetworkInterface network =
                    segmentFactory.getChannel(channelSegmentEntity.getAdvertiserId(), adapterConfig, null, null, base,
                            channel, advertiserSet);
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

            final long incId = channelSegmentEntity.getIncId(creativeType);
            if (incId == -1) {
                LOG.debug("Could not find incId for adGroup {} and creativeType {}",
                        channelSegmentEntity.getAdgroupId());
                continue;
            }

            String clickUrl = null;
            String beaconUrl = null;
            // Replacing int key in auction id to generate impression id
            sasParams.setImpressionId(ImpressionIdGenerator.getInstance().resetWilburyIntKey(
                    casInternalRequestParameterGlobal.getAuctionId(), incId));
            final CasInternalRequestParameters casInternalRequestParameters =
                    getCasInternalRequestParameters(sasParams, casInternalRequestParameterGlobal);

            controlEnrichment(casInternalRequestParameters, channelSegmentEntity);
            sasParams.setAdIncId(incId);
            LOG.debug("impression id is {}", sasParams.getImpressionId());

            if ((network.isClickUrlRequired() || network.isBeaconUrlRequired()) && null != sasParams.getImpressionId()) {
                boolean isCpc = false;
                if (null != channelSegmentEntity.getPricingModel()
                        && "cpc".equalsIgnoreCase(channelSegmentEntity.getPricingModel())) {
                    isCpc = true;
                }
                final ClickUrlMakerV6 clickUrlMakerV6 =
                        setClickParams(isCpc, config, sasParams, channelSegmentEntity.getDst() - 1);
                clickUrlMakerV6.createClickUrls();
                clickUrl = clickUrlMakerV6.getClickUrl();
                beaconUrl = clickUrlMakerV6.getBeaconUrl();
                LOG.debug("click url : {}", clickUrl);
                LOG.debug("beacon url : {}", beaconUrl);
            }

            LOG.debug("Sending request to Channel of advertiserId {}", channelSegmentEntity.getAdvertiserId());
            LOG.debug("external site key is {}", channelSegmentEntity.getExternalSiteKey());

            network.disableIPResolution(config.getBoolean("isIPRepositoryDisabled", true));
            if (network.configureParameters(sasParams, casInternalRequestParameters, channelSegmentEntity, clickUrl,
                    beaconUrl, row.getRequestedSlotId(), repositoryHelper)) {
                InspectorStats.incrementStatCount(network.getName(), InspectorStrings.SUCCESSFUL_CONFIGURE);
                row.setAdNetworkInterface(network);
                if (network.isRtbPartner() || network.isIxPartner()) {
                    rtbSegments.add(row);
                    LOG.debug("{} is a rtb/ix partner so adding this network to rtb ranklist", network.getName());
                } else {
                    segments.add(row);
                }
            }
        }
        return segments;
    }

    private CasInternalRequestParameters getCasInternalRequestParameters(final SASRequestParameters sasParams,
            final CasInternalRequestParameters casGlobal) {
        final CasInternalRequestParameters casInternal = new CasInternalRequestParameters();
        casInternal.setImpressionId(sasParams.getImpressionId());
        casInternal.setBlockedIabCategories(casGlobal.getBlockedIabCategories());
        casInternal.setBlockedAdvertisers(casGlobal.getBlockedAdvertisers());
        casInternal.setAuctionBidFloor(casGlobal.getAuctionBidFloor());
        casInternal.setAuctionId(casGlobal.getAuctionId());
        casInternal.setUid(casGlobal.getUid());
        casInternal.setUidO1(casGlobal.getUidO1());
        casInternal.setUidIFA(casGlobal.getUidIFA());
        casInternal.setGpid(casGlobal.getGpid());
        casInternal.setUidIFV(casGlobal.getUidIFV());
        casInternal.setUidSO1(casGlobal.getUidSO1());
        casInternal.setUidIDUS1(casGlobal.getUidIDUS1());
        casInternal.setUidMd5(casGlobal.getUidMd5());
        casInternal.setUidADT(casGlobal.getUidADT());
        casInternal.setSiteFloor(sasParams.getSiteFloor());
        if (null != sasParams.getPostalCode()) {
            casInternal.setZipCode(sasParams.getPostalCode());
        }
        casInternal.setLatLong(sasParams.getLatLong());
        casInternal.setAppUrl(sasParams.getAppUrl());
        casInternal.setTraceEnabled(casGlobal.isTraceEnabled());
        casInternal.setSiteAccountType(casGlobal.getSiteAccountType());

        return casInternal;
    }

    private void controlEnrichment(final CasInternalRequestParameters casInternalRequestParameters,
            final ChannelSegmentEntity channelSegmentEntity) {
        if (channelSegmentEntity.isStripUdId()) {
            casInternalRequestParameters.setUid(null);
            casInternalRequestParameters.setUidO1(null);
            casInternalRequestParameters.setUidMd5(null);
            casInternalRequestParameters.setUidIFA(null);
            casInternalRequestParameters.setGpid(null);
            casInternalRequestParameters.setUidIFV(null);
            casInternalRequestParameters.setUidIDUS1(null);
            casInternalRequestParameters.setUidSO1(null);
            casInternalRequestParameters.setUidADT(null);
        }
        if (channelSegmentEntity.isStripZipCode()) {
            casInternalRequestParameters.setZipCode(null);
        }
        if (channelSegmentEntity.isStripLatlong()) {
            casInternalRequestParameters.setLatLong(null);
        }
        if (!channelSegmentEntity.isAppUrlEnabled()) {
            casInternalRequestParameters.setAppUrl(null);
        }

    }

    public List<ChannelSegment> makeAsyncRequests(final List<ChannelSegment> rankList, final Channel channel,
            final List<ChannelSegment> rtbSegments) {
        final Iterator<ChannelSegment> itr = rankList.iterator();
        while (itr.hasNext()) {
            final ChannelSegment channelSegment = itr.next();
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                    InspectorStrings.TOTAL_INVOCATIONS);
            if (channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
                LOG.debug("Successfully sent request to channel of  advertiser id {} and channel id {}", channelSegment
                        .getChannelSegmentEntity().getId(), channelSegment.getChannelSegmentEntity().getChannelId());
                AdvertiserFailureThrottler.increamentRequestsCounter(channelSegment.getAdNetworkInterface().getId(),
                        System.currentTimeMillis());
            } else {
                itr.remove();
            }
        }
        final Iterator<ChannelSegment> rtbItr = rtbSegments.iterator();
        while (rtbItr.hasNext()) {
            final ChannelSegment channelSegment = rtbItr.next();
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                    InspectorStrings.TOTAL_INVOCATIONS);
            if (channelSegment.getAdNetworkInterface().makeAsyncRequest()) {
                LOG.debug("Successfully sent request to rtb channel of  advertiser id {} and channel id {}",
                        channelSegment.getChannelSegmentEntity().getId(), channelSegment.getChannelSegmentEntity()
                                .getChannelId());
                AdvertiserFailureThrottler.increamentRequestsCounter(channelSegment.getAdNetworkInterface().getId(),
                        System.currentTimeMillis());
            } else {
                rtbItr.remove();
            }
        }
        return rankList;
    }

    private static ClickUrlMakerV6 setClickParams(final boolean pricingModel, final Configuration config,
            final SASRequestParameters sasParams, final Integer dst) {
        final ClickUrlMakerV6.Builder builder = ClickUrlMakerV6.newBuilder();
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
        builder.setImageBeaconFlag(true); // true/false
        builder.setBeaconEnabledOnSite(true); // do not know
        builder.setTestMode(false);
        builder.setRmAd(sasParams.isRichMedia());
        builder.setRmBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
        builder.setClickURLPrefix(config.getString("clickmaker.clickURLPrefix"));
        builder.setImageBeaconURLPrefix(config.getString("clickmaker.beaconURLPrefix"));
        builder.setTestRequest(false);
        builder.setLatlonval(sasParams.getLatLong());
        builder.setRtbSite(sasParams.getSst() != 0); // TODO:- Change this according to thrift enums
        builder.setDst(dst.toString());
        builder.setBudgetBucketId("101"); // Default Value
        return new ClickUrlMakerV6(builder);
    }

    private boolean isNativeRequest(final SASRequestParameters sasParams) {
        return "native".equalsIgnoreCase(sasParams.getRFormat());
    }
}
