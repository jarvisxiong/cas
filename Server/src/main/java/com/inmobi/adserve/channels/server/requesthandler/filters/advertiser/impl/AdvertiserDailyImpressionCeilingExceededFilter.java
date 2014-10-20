package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import javax.inject.Inject;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdvertiserDailyImpressionCeilingExceededFilter extends AbstractAdvertiserLevelFilter {

	@Inject
	public AdvertiserDailyImpressionCeilingExceededFilter(final Provider<Marker> traceMarkerProvider) {
		super(traceMarkerProvider, InspectorStrings.DROPPED_IN_IMPRESSION_FILTER);
	}

	@Override
	protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
		return channelSegment.getChannelFeedbackEntity().getTodayImpressions() > channelSegment.getChannelEntity()
				.getImpressionCeil();
	}

}
