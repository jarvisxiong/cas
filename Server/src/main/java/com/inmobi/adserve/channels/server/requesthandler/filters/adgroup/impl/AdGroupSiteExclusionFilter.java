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
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupSiteExclusionFilter extends AbstractAdGroupLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdGroupSiteExclusionFilter.class);

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupSiteExclusionFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_SITE_EXCLUSION_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {

        boolean isFilterAtAdvertiserLevel = channelSegment.getChannelSegmentEntity().getSitesIE().isEmpty();

        // applying site inclusion-exclusion at advertiser level
        if (isFilterAtAdvertiserLevel) {
            return isSiteExcludedAtAdvertiserLevel(channelSegment, sasParams);
        }

        return isSiteExcludedAtAdGroupLevel(channelSegment, sasParams);

    }

    /**
     * @param channelSegment
     * @param sasParams
     * @return
     */
    private boolean isSiteExcludedAtAdGroupLevel(final ChannelSegment channelSegment,
            final SASRequestParameters sasParams) {
        boolean result;
        if (channelSegment.getChannelSegmentEntity().getSitesIE().contains(sasParams.getSiteId())) {
            result = !channelSegment.getChannelSegmentEntity().isSiteInclusion();
        } else {
            result = channelSegment.getChannelSegmentEntity().isSiteInclusion();
        }
        return result;
    }

    /**
     * @param channelSegment
     * @param sasParams
     * @return
     */
    private boolean isSiteExcludedAtAdvertiserLevel(final ChannelSegment channelSegment,
            final SASRequestParameters sasParams) {
        boolean result;
        if (channelSegment.getChannelEntity().getSitesIE().contains(sasParams.getSiteId())) {
            result = !channelSegment.getChannelEntity().isSiteInclusion();
        } else {
            result = channelSegment.getChannelEntity().isSiteInclusion();
        }
        return result;
    }

}
