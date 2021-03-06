package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.casthrift.ADCreativeType.BANNER;
import static com.inmobi.casthrift.ADCreativeType.NATIVE;
import static com.inmobi.casthrift.DemandSourceType.DCP;

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
import com.inmobi.adserve.channels.api.SASParamsUtils;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.SegmentFactory;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl.AdvertiserFailureThrottler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;

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
            final SASRequestParameters sasParams, final CasInternalRequestParameters casInternalGlobal,
            final List<ChannelSegment> rtbSegments) throws Exception {
        final List<ChannelSegment> segments = new ArrayList<>();
        LOG.debug("Total channels available for sending requests {}", rows.size());
        /*
         NOTE: For a request that qualifies the in-banner video criteria, at this point we don't know whether an
         interstitial video response will be sent or Banner. Same applies for Static Native vs Native Video.
         At this point, the creative type is set to Banner/Native for video supported requests. If the request gets fulfilled
         with a video ad, creative type will be chosen accordingly.
         Setting the creativeType to BANNER for DCP native requests
         */
        final boolean isNative = SASParamsUtils.isNativeRequest(sasParams);
        final ADCreativeType creativeType = sasParams.getDst() == DCP.getValue() ? BANNER : isNative ? NATIVE : BANNER;
        LOG.debug("Creative type is : {}", creativeType);
        int index = 0;
        for (final ChannelSegment row : rows) {
            final ChannelSegmentEntity csEntity = row.getChannelSegmentEntity();
            final AdNetworkInterface network = segmentFactory.getChannel(csEntity.getAdvertiserId(), adapterConfig,
                    null, null, base, channel, advertiserSet, sasParams);
            if (null == network) {
                LOG.debug("No adapter found for adGroup: {}", csEntity.getAdgroupId());
                continue;
            }
            LOG.debug("adapter found for adGroup: {} advertiserid is {} is {}", csEntity.getAdgroupId(),
                    row.getChannelSegmentEntity().getAdvertiserId(), network.getName());
            if (null == repositoryHelper.queryChannelRepository(csEntity.getChannelId())) {
                LOG.debug("No channel entity found for channel id: {}", csEntity.getChannelId());
                continue;
            }

            final long incId = csEntity.getIncId(creativeType);
            if (incId == -1) {
                LOG.debug("Could not find incId for adGroup {} and creativeType {}", csEntity.getAdgroupId());
                continue;
            }

            // Replacing int key in auction id to generate impression id
            sasParams.setImpressionId(
                    ImpressionIdGenerator.getInstance().resetWilburyIntKey(casInternalGlobal.getAuctionId(), incId));

            final CasInternalRequestParameters casInternal = getLocalCasInternal(sasParams, casInternalGlobal);
            if (DemandSourceType.IX.getValue() == sasParams.getDst()) {
                casInternal.setAuctionId(ImpressionIdGenerator.getInstance()
                        .resetWilburyIntKey(casInternalGlobal.getAuctionId(), sasParams.getSiteIncId() + index));
                LOG.debug("IX Multi format: {} {} auction id is {}", csEntity.getSecondaryAdFormatConstraints(),
                        sasParams.getRequestedAdType(), casInternal.getAuctionId());
                ++index;
            }
            controlEnrichment(casInternal, csEntity);
            sasParams.setAdIncId(incId);
            LOG.debug("impression id is {}", sasParams.getImpressionId());
            LOG.debug("Sending request to Channel of advertiserId {}", csEntity.getAdvertiserId());
            LOG.debug("external site key is {}", csEntity.getExternalSiteKey());
            network.disableIPResolution(config.getBoolean("isIPRepositoryDisabled", true));
            if (network.configureParameters(sasParams, casInternal, csEntity, row.getRequestedSlotId(),
                    repositoryHelper)) {
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

    private CasInternalRequestParameters getLocalCasInternal(final SASRequestParameters sasParams,
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
        casInternal.setTrackingAllowed(casGlobal.isTrackingAllowed());
        casInternal.setSiteFloor(sasParams.getSiteFloor());
        casInternal.setZipCode(sasParams.getPostalCode());
        casInternal.setLatLong(sasParams.getLatLong());
        casInternal.setAppUrl(sasParams.getAppUrl());
        casInternal.setTraceEnabled(casGlobal.isTraceEnabled());
        casInternal.setSiteAccountType(casGlobal.getSiteAccountType());
        casInternal.setImeiMD5(casGlobal.getImeiMD5());
        casInternal.setImeiSHA1(casGlobal.getImeiSHA1());
        return casInternal;
    }

    private void controlEnrichment(final CasInternalRequestParameters casInternal,
            final ChannelSegmentEntity csEntity) {
        LOG.debug("In controlEnrichment AdGroup Id = {}, Advertiser Id = {}", csEntity.getAdgroupId(),
                csEntity.getAdvertiserId());
        if (csEntity.isStripUdId()) {
            LOG.debug("Stripping all UIDs");
            casInternal.setUid(null);
            casInternal.setUidO1(null);
            casInternal.setUidMd5(null);
            casInternal.setUidIFA(null);
            casInternal.setGpid(null);
            casInternal.setUidIFV(null);
            casInternal.setUidIDUS1(null);
            casInternal.setUidSO1(null);
            casInternal.setTrackingAllowed(false);
            casInternal.setImeiMD5(null);
            casInternal.setImeiSHA1(null);
        }
        if (csEntity.isStripZipCode()) {
            LOG.debug("Stripping ZIP Codes");
            casInternal.setZipCode(null);
        }
        if (csEntity.isStripLatlong()) {
            LOG.debug("Stripping LatLong");
            casInternal.setLatLong(null);
        }
        if (!csEntity.isAppUrlEnabled()) {
            LOG.debug("Stripping App URL");
            casInternal.setAppUrl(null);
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
                LOG.debug("Successfully sent request to channel of  advertiser id {} and channel id {}",
                        channelSegment.getChannelSegmentEntity().getId(),
                        channelSegment.getChannelSegmentEntity().getChannelId());
                AdvertiserFailureThrottler.incrementTotalCounter(channelSegment.getAdNetworkInterface().getId(),
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
                        channelSegment.getChannelSegmentEntity().getId(),
                        channelSegment.getChannelSegmentEntity().getChannelId());
                AdvertiserFailureThrottler.incrementTotalCounter(channelSegment.getAdNetworkInterface().getId(),
                        System.currentTimeMillis());
            } else {
                rtbItr.remove();
            }
        }
        return rankList;
    }

}
