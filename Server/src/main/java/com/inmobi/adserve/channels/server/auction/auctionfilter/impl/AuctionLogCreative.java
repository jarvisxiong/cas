package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CreativeCache;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;

public class AuctionLogCreative extends AbstractAuctionFilter {
    private final RepositoryHelper repositoryHelper;
    private final CreativeCache creativeCache;

    @Inject
    protected AuctionLogCreative(final Provider<Marker> traceMarkerProvider, final RepositoryHelper repositoryHelper,
            final ServerConfig serverConfiguration, final CreativeCache creativeCache) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_CREATIVE_VALIDATOR_FILTER, serverConfiguration);
        this.repositoryHelper = repositoryHelper;
        this.creativeCache = creativeCache;
        isApplicableRTBD = true;
        isApplicableIX = true;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment rtbSegment,
            final CasInternalRequestParameters casInternalRequestParameters) {
        final String creativeId = rtbSegment.getAdNetworkInterface().getCreativeId();
        String advertiserAccountId = rtbSegment.getChannelEntity().getAccountId();

        // In case of IX, we are only interested in logging VIDEO creative. This logging is required since video
        // creative is not yet supported in the RP creative audit API.

        if (rtbSegment.getAdNetworkInterface().getDst() == DemandSourceType.IX) {
            if (rtbSegment.getAdNetworkInterface().getCreativeType() != ADCreativeType.INTERSTITIAL_VIDEO) {
                return false;
            }
            // Replace the RP account Id with the DSP account Id.
            advertiserAccountId =
                    ((IXAdNetwork) rtbSegment.getAdNetworkInterface()).getDspChannelSegmentEntity().getAdvertiserId();
        }

        // There is no point in logging if either of the below is not present.
        if (StringUtils.isEmpty(creativeId) || StringUtils.isEmpty(advertiserAccountId)) {
            return false;
        }

        // Handling de-duping in Cache
        final CreativeEntity creativeEntity = repositoryHelper.queryCreativeRepository(advertiserAccountId, creativeId);
        final boolean presentInCache = creativeCache.isPresentInCache(advertiserAccountId, creativeId);
        if (null == creativeEntity && !presentInCache) {
            rtbSegment.getAdNetworkInterface().setLogCreative(true);
            creativeCache.addToCache(advertiserAccountId, creativeId);
        } else if (null != creativeEntity && presentInCache) {
            // Remove from local cache since we have this in DB already
            creativeCache.removeFromCache(advertiserAccountId, creativeId);
        }
        return false;
    }
}
