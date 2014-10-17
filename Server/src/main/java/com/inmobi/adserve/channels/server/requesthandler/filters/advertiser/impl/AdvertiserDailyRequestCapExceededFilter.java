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
public class AdvertiserDailyRequestCapExceededFilter extends AbstractAdvertiserLevelFilter {

  @Inject
  public AdvertiserDailyRequestCapExceededFilter(final Provider<Marker> traceMarkerProvider) {
    super(traceMarkerProvider, InspectorStrings.DROPPED_IN_REQUEST_CAP_FILTER);
  }

  @Override
  protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {

    return channelSegment.getChannelFeedbackEntity().getTodayRequests() > channelSegment.getChannelEntity()
        .getRequestCap();
  }

}
