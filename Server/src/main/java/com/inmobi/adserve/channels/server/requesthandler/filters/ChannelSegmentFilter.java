package com.inmobi.adserve.channels.server.requesthandler.filters;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.testng.collections.Lists;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class ChannelSegmentFilter {
    private static final Logger              LOG = LoggerFactory.getLogger(ChannelSegmentFilter.class);

    private final Provider<Marker>           traceMarkerProvider;

    private final Set<AdvertiserLevelFilter> advertiserLevelFilters;

    @Inject
    public ChannelSegmentFilter(final Provider<Marker> traceMarkerProvider,
            final Set<AdvertiserLevelFilter> advertiserLevelFilters) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.advertiserLevelFilters = advertiserLevelFilters;
    }

    public List<ChannelSegment> getChannelSegments(final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails,
            final SASRequestParameters sasParams) {

        // apply all filters
        // advertiser level filter

        for (AdvertiserLevelFilter advertiserLevelFilter : advertiserLevelFilters) {
            advertiserLevelFilter.filter(matchedSegmentDetails, sasParams);
        }

        printSegments(matchedSegmentDetails);

        // adGroup level filter

        // get the channel segments
        List<ChannelSegment> channelSegmentList = Lists.newArrayList();
        for (AdvertiserMatchedSegmentDetail matchedSegmentDetail : matchedSegmentDetails) {
            channelSegmentList.addAll(matchedSegmentDetail.getChannelSegmentList());
        }

        return channelSegmentList;
    }

    private void printSegments(final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails) {
        Marker traceMarker = traceMarkerProvider.get();

        if (LOG.isDebugEnabled()) {

            LOG.debug(traceMarker, "Remaining AdGroups are :");

            for (AdvertiserMatchedSegmentDetail advertiserMatchedSegmentDetail : matchedSegmentDetails) {

                for (ChannelSegment channelSegment : advertiserMatchedSegmentDetail.getChannelSegmentList()) {

                    LOG.debug(traceMarker, "Advertiser is {} and AdGp is {}", advertiserMatchedSegmentDetail
                            .getAdvertiserId(), channelSegment.getChannelSegmentEntity().getAdgroupId());
                }
            }
        }
    }
}
