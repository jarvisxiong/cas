package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupDstFilter extends AbstractAdGroupLevelFilter {
    private static final Logger       LOG = LoggerFactory.getLogger(AdGroupDstFilter.class);
    private final Map<String, String> advertiserIdNameMap;

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupDstFilter(final Provider<Marker> traceMarkerProvider, final Map<String, String> advertiserIdNameMap) {
        super(traceMarkerProvider, InspectorStrings.droppedInDstFilter);
        this.advertiserIdNameMap = advertiserIdNameMap;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {
        return (sasParams.getDst() == 6 && channelSegment.getChannelSegmentEntity().getDst() != sasParams.getDst())
                || (sasParams.getDst() == 2 && sasParams.isResponseOnlyFromDcp() && channelSegment
                        .getChannelSegmentEntity().getDst() != sasParams.getDst());

    }

    @Override
    protected void incrementStats(final ChannelSegment channelSegment) {
        String advertiserName = advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId());
        InspectorStats.incrementStatCount(advertiserName, InspectorStrings.droppedInDstFilter);
    }

}
