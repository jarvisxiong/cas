package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;

/**
 * Created by ishanbhatnagar on 28/1/15.
 */
@Singleton
public class AdGroupAutomationFrameworkFilter extends AbstractAdGroupLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdGroupAutomationFrameworkFilter.class);

    @Inject
    protected AdGroupAutomationFrameworkFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_AUTOMATION_FRAMEWORK_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
                                     final CasContext casContext) {

        return null != sasParams.getAutomationTestId() &&
                !sasParams.getAutomationTestId().equalsIgnoreCase(channelSegment.getChannelSegmentEntity().getAutomationTestId());
    }
}