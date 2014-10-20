package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import javax.inject.Inject;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdvertiserBurnLimitExceededFilter extends AbstractAdvertiserLevelFilter {

	private final ServerConfig serverConfiguration;

	@Inject
	public AdvertiserBurnLimitExceededFilter(final Provider<Marker> traceMarkerProvider,
			final ServerConfig serverConfiguration) {
		super(traceMarkerProvider, InspectorStrings.DROPPED_IN_BURN_FILTER);
		this.serverConfiguration = serverConfiguration;
	}

	@Override
	protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
		return channelSegment.getChannelFeedbackEntity().getBalance() < channelSegment.getChannelFeedbackEntity()
				.getRevenue() * serverConfiguration.getRevenueWindow();

	}

}
