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
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.utils.CasUtils;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class ChannelSegmentFilterApplier {
    private static final Logger              LOG = LoggerFactory.getLogger(ChannelSegmentFilterApplier.class);

    private final Provider<Marker>           traceMarkerProvider;

    private final Set<AdvertiserLevelFilter> advertiserLevelFilters;

    private final Set<AdGroupLevelFilter>    adGroupLevelFilters;

    private final CasUtils                   casUtils;

    @Inject
    public ChannelSegmentFilterApplier(final Provider<Marker> traceMarkerProvider,
            final Set<AdvertiserLevelFilter> advertiserLevelFilters, final Set<AdGroupLevelFilter> adGroupLevelFilters,
            final CasUtils casUtils) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.advertiserLevelFilters = advertiserLevelFilters;
        this.adGroupLevelFilters = adGroupLevelFilters;
        this.casUtils = casUtils;
    }

    public List<ChannelSegment> getChannelSegments(final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails,
            final SASRequestParameters sasParams) {

        Marker traceMarker = traceMarkerProvider.get();

        // apply all filters
        // advertiser level filter

        for (AdvertiserLevelFilter advertiserLevelFilter : advertiserLevelFilters) {
            advertiserLevelFilter.filter(matchedSegmentDetails, sasParams);
        }

        printSegments(matchedSegmentDetails);

        // adGroup level filter

        CasContext casContext = new CasContext();
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
