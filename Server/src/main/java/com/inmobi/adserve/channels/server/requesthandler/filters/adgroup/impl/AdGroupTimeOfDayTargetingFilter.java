package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Calendar;

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
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupTimeOfDayTargetingFilter extends AbstractAdGroupLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdGroupTimeOfDayTargetingFilter.class);

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupTimeOfDayTargetingFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_TOD_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        Marker traceMarker = traceMarkerProvider.get();

        Long[] timeOfDayTargetingArray = channelSegment.getChannelSegmentEntity().getTod();
        if (timeOfDayTargetingArray == null) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        int hourOfDay = 1 << now.get(Calendar.HOUR_OF_DAY);
        LOG.debug(traceMarker, "ToD array is :  {}", (Object[]) timeOfDayTargetingArray);
        long dayOfWeek = timeOfDayTargetingArray[now.get(Calendar.DAY_OF_WEEK) - 1];
        long todt = dayOfWeek & hourOfDay;
        LOG.debug(traceMarker, "dayOfWeek is : {} hourOfDay is : {} todt calculated is : {}", dayOfWeek, hourOfDay,
                todt);

        if (todt == 0) {
            return true;
        }

        return false;

    }

}
