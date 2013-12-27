package com.inmobi.adserve.channels.server.requesthandler.filters;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdvertiserFailedInAccountSegmentFilter extends AbstractAdvertiserLevelFilter {

    private static final Logger       LOG = LoggerFactory.getLogger(AdvertiserFailedInAccountSegmentFilter.class);

    private final Map<String, String> advertiserIdNameMap;

    @Inject
    public AdvertiserFailedInAccountSegmentFilter(final Provider<Marker> traceMarkerProvider,
            final Map<String, String> advertiserIdNameMap) {
        super(traceMarkerProvider, InspectorStrings.droppedInAccountSegmentFilter);
        this.advertiserIdNameMap = advertiserIdNameMap;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {

        int accountSegment = channelSegment.getChannelEntity().getAccountSegment();
        String advertiserId = channelSegment.getChannelEntity().getAccountId();
        String advertiserName = advertiserIdNameMap.get(advertiserId);

        return (advertiserName == null || (sasParams.getDst() == 6 && null != sasParams.getAccountSegment()
                && !sasParams.getAccountSegment().isEmpty() && !sasParams.getAccountSegment().contains(accountSegment)));
    }

    @Override
    protected void incrementStats(final ChannelSegment channelSegment) {
        String advertiserId = channelSegment.getChannelEntity().getAccountId();
        String advertiserName = advertiserIdNameMap.get(advertiserId);
        InspectorStats.incrementStatCount(advertiserName, InspectorStrings.droppedInAccountSegmentFilter);
    }
}