package com.inmobi.adserve.channels.server.requesthandler.filters;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AdvertiserLevelFilter;
import com.inmobi.adserve.channels.server.utils.CasUtils;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class ChannelSegmentFilterApplier {
    private static final Logger               LOG = LoggerFactory.getLogger(ChannelSegmentFilterApplier.class);

    private final Provider<Marker>            traceMarkerProvider;

    private final List<AdvertiserLevelFilter> advertiserLevelFilters;

    private final List<AdGroupLevelFilter>    adGroupLevelFilters;

    private final CasUtils                    casUtils;

    @Inject
    public ChannelSegmentFilterApplier(final Provider<Marker> traceMarkerProvider,
            final List<AdvertiserLevelFilter> advertiserLevelFilters,
            final List<AdGroupLevelFilter> adGroupLevelFilters, final CasUtils casUtils) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.advertiserLevelFilters = advertiserLevelFilters;
        this.adGroupLevelFilters = adGroupLevelFilters;
        this.casUtils = casUtils;
    }

    public List<ChannelSegment> getChannelSegments(final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails,
            final SASRequestParameters sasParams, final CasContext casContext) {

        Marker traceMarker = traceMarkerProvider.get();

        // apply all filters

        // advertiser level filter
        for (AdvertiserLevelFilter advertiserLevelFilter : advertiserLevelFilters) {
            advertiserLevelFilter.filter(matchedSegmentDetails, sasParams);
        }

        printSegments(matchedSegmentDetails);

        // adGroup level filter
        casContext.setPricingEngineEntity(casUtils.fetchPricingEngineEntity(sasParams));

        // get the channel segments
        List<ChannelSegment> channelSegmentList = Lists.newArrayList();

        for (AdvertiserMatchedSegmentDetail matchedSegmentDetail : matchedSegmentDetails) {
            channelSegmentList.addAll(matchedSegmentDetail.getChannelSegmentList());
        }

        int sumOfSiteImpressions = 0;
        for (ChannelSegment channelSegment : channelSegmentList) {
            sumOfSiteImpressions += channelSegment.getChannelSegmentCitrusLeafFeedbackEntity().getBeacons();
        }
        casContext.setSumOfSiteImpressions(sumOfSiteImpressions);
        LOG.debug(traceMarker, "Sum of Site impressions:{}", sumOfSiteImpressions);

        for (AdGroupLevelFilter adGroupLevelFilter : adGroupLevelFilters) {
            adGroupLevelFilter.filter(channelSegmentList, sasParams, casContext);
        }

        Collections.sort(channelSegmentList, ChannelSegment.CHANNEL_SEGMENT_REVERSE_COMPARATOR);
        printSegments(traceMarker, channelSegmentList);

        return channelSegmentList;
    }

    /**
     * @param traceMarker
     * @param channelSegmentList
     */
    private void printSegments(final Marker traceMarker, final List<ChannelSegment> channelSegmentList) {
        for (ChannelSegment channelSegment : channelSegmentList) {
            LOG.debug(traceMarker, "Segment with advertiserId {} adGroupId {} Pecpm {}", channelSegment
                    .getChannelSegmentEntity().getAdvertiserId(), channelSegment.getChannelSegmentEntity()
                    .getAdgroupId(), channelSegment.getPrioritisedECPM());
        }
    }

    private void printSegments(final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails) {
        Marker traceMarker = traceMarkerProvider.get();

        if (LOG.isTraceEnabled()) {
            LOG.debug(traceMarker, "Remaining AdGroups are :");
            for (AdvertiserMatchedSegmentDetail advertiserMatchedSegmentDetail : matchedSegmentDetails) {
                for (ChannelSegment channelSegment : advertiserMatchedSegmentDetail.getChannelSegmentList()) {
                    LOG.debug(traceMarker, "Advertiser is {} and AdGp is {}", channelSegment.getChannelEntity()
                            .getAccountId(), channelSegment.getChannelSegmentEntity().getAdgroupId());
                }
            }
        }
    }
}
