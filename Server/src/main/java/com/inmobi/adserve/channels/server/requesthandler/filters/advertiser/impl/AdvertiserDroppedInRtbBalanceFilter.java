package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdvertiserDroppedInRtbBalanceFilter extends AbstractAdvertiserLevelFilter {

	private final Map<String, AdapterConfig> advertiserIdConfigMap;

	private final ServerConfig serverConfiguration;

	@Inject
	public AdvertiserDroppedInRtbBalanceFilter(final Provider<Marker> traceMarkerProvider,
			final Map<String, AdapterConfig> advertiserIdConfigMap, final ServerConfig serverConfiguration) {
		super(traceMarkerProvider, InspectorStrings.DROPPED_IN_RTB_BALANCE_FILTER);
		this.advertiserIdConfigMap = advertiserIdConfigMap;
		this.serverConfiguration = serverConfiguration;
	}

	@Override
	protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
		final String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
		final boolean isRtbPartner = advertiserIdConfigMap.get(advertiserId).isRtb();
		boolean result = false;
		if (isRtbPartner) {
			result =
					channelSegment.getChannelFeedbackEntity().getBalance() < serverConfiguration
							.getRtbBalanceFilterAmount();
		}
		return result;
	}

}
