package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CreativeCache;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.adserve.channels.types.CreativeExposure;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuctionCreativeValidatorFilter extends AbstractAuctionFilter {

    private final RepositoryHelper repositoryHelper;
    private CreativeCache creativeCache;

    @Inject
    protected AuctionCreativeValidatorFilter(Provider<Marker> traceMarkerProvider, final RepositoryHelper repositoryHelper,
                                             final ServerConfig serverConfiguration, final CreativeCache creativeCache) {
        super(traceMarkerProvider, InspectorStrings.droppedInCreativeValidatorFilter, serverConfiguration);
        this.repositoryHelper = repositoryHelper;
        this.creativeCache = creativeCache;
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        CreativeEntity creativeEntity = repositoryHelper.queryCreativeRepository(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
        CreativeExposure creativeExposure = CreativeExposure.SELF_SERVE;

        //Setting appropriate exposure level
        if (null != creativeEntity) {
            creativeExposure = creativeEntity.getExposureLevel();
            if (creativeExposure == CreativeExposure.ALL && !creativeEntity.getImageUrl().equalsIgnoreCase(rtbSegment.getAdNetworkInterface().getIUrl())) {
                creativeExposure = CreativeExposure.SELF_SERVE;
            }
        }

        //Handling de-duping in Cache
        boolean presentInCache = creativeCache.isPresentInCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
        if (null == creativeEntity && !presentInCache) {
            rtbSegment.getAdNetworkInterface().setLogCreative(true);
            creativeCache.addToCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
        } else if (null != creativeEntity && presentInCache) {
            creativeCache.removeFromCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
        }

        //Filtering logic
        if (creativeExposure == CreativeExposure.ALL) {
           return false;
        } else if ((creativeExposure == CreativeExposure.SELF_SERVE) && casInternalRequestParameters.siteAccountType == AccountType.SELF_SERVE) {
           return false;
        }

        return true;
    }
}
