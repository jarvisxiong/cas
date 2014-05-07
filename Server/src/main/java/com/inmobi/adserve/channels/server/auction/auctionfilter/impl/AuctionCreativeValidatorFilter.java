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
import com.inmobi.adserve.channels.types.CreativeStatus;
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

    @Inject

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        CreativeEntity creativeEntity = repositoryHelper.queryCreativeRepository(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
        CreativeStatus creativeStatus = CreativeStatus.APPROVED;
        boolean presentInCache = creativeCache.isPresentInCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
        if (null != creativeEntity) {
            creativeStatus = creativeEntity.getCreativeStatus();
            if (presentInCache) {
                creativeCache.removeFromCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
            }
        } else if (!presentInCache) {
            rtbSegment.getAdNetworkInterface().setLogCreative(true);
            creativeCache.addToCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
        }
        if (creativeStatus == CreativeStatus.APPROVED && casInternalRequestParameters.siteAccountType == AccountType.MANAGED) {
           return false;
        }
        if (creativeStatus == CreativeStatus.APPROVED || creativeStatus == CreativeStatus.PENDING && casInternalRequestParameters.siteAccountType == AccountType.SELF_SERVE) {
           return false;
        }
        return true;
    }
}
