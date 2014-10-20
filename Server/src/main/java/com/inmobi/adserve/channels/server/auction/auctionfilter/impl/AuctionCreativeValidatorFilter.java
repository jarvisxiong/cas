package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.adserve.channels.types.CreativeExposure;
import com.inmobi.adserve.channels.util.InspectorStrings;

@Singleton
public class AuctionCreativeValidatorFilter extends AbstractAuctionFilter {

	private final RepositoryHelper repositoryHelper;

	@Inject
	protected AuctionCreativeValidatorFilter(final Provider<Marker> traceMarkerProvider,
			final RepositoryHelper repositoryHelper, final ServerConfig serverConfiguration) {
		super(traceMarkerProvider, InspectorStrings.DROPPED_IN_CREATIVE_VALIDATOR_FILTER, serverConfiguration);
		this.repositoryHelper = repositoryHelper;
		isApplicableRTBD = true;
		isApplicableIX = false;
	}

	@Override
	protected boolean failedInFilter(final ChannelSegment rtbSegment,
			final CasInternalRequestParameters casInternalRequestParameters) {
		final CreativeEntity creativeEntity =
				repositoryHelper.queryCreativeRepository(rtbSegment.getChannelEntity().getAccountId(), rtbSegment
						.getAdNetworkInterface().getCreativeId());
		CreativeExposure creativeExposure = CreativeExposure.SELF_SERVE;

		// Setting appropriate exposure level
		if (null != creativeEntity) {
			creativeExposure = creativeEntity.getExposureLevel();
			if (creativeExposure == CreativeExposure.ALL
					&& !creativeEntity.getImageUrl().equalsIgnoreCase(rtbSegment.getAdNetworkInterface().getIUrl())) {
				creativeExposure = CreativeExposure.SELF_SERVE;
			}
		}

		// Filtering logic
		if (creativeExposure == CreativeExposure.ALL) {
			return false;
		} else if (creativeExposure == CreativeExposure.SELF_SERVE
				&& casInternalRequestParameters.getSiteAccountType() == AccountType.SELF_SERVE) {
			return false;
		}

		return true;
	}
}
