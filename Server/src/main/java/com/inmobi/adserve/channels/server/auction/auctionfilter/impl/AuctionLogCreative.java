package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CreativeCache;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;

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
		isApplicableIX = false;
	}

	@Override
	protected boolean failedInFilter(final ChannelSegment rtbSegment,
			final CasInternalRequestParameters casInternalRequestParameters) {

		if (StringUtils.isEmpty(rtbSegment.getAdNetworkInterface().getCreativeId())) {
			return false;
		}

		// Handling de-duping in Cache
		final CreativeEntity creativeEntity =
				repositoryHelper.queryCreativeRepository(rtbSegment.getChannelEntity().getAccountId(), rtbSegment
						.getAdNetworkInterface().getCreativeId());
		final boolean presentInCache =
				creativeCache.isPresentInCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment
						.getAdNetworkInterface().getCreativeId());
		if (null == creativeEntity && !presentInCache) {
			rtbSegment.getAdNetworkInterface().setLogCreative(true);
			creativeCache.addToCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment.getAdNetworkInterface()
					.getCreativeId());
		} else if (null != creativeEntity && presentInCache) {
			creativeCache.removeFromCache(rtbSegment.getChannelEntity().getAccountId(), rtbSegment
					.getAdNetworkInterface().getCreativeId());
		}

		return false;
	}
}
