package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
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

    @Inject
    protected AuctionCreativeValidatorFilter(Provider<Marker> traceMarkerProvider, final RepositoryHelper repositoryHelper) {
        super(traceMarkerProvider, InspectorStrings.droppedInCreativeValidatorFilter);
        this.repositoryHelper = repositoryHelper;
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        CreativeEntity creativeEntity = repositoryHelper.queryCreativeRepository(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface().getCreativeId());
        CreativeStatus creativeStatus = CreativeStatus.APPROVED;
        if (null != creativeEntity) {
            creativeStatus = creativeEntity.getCreativeStatus();
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
