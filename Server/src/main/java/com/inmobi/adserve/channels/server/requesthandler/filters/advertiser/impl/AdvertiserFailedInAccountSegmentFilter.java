package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;


/**
 * @author abhishek.parwal
 *
 */
@Singleton
public class AdvertiserFailedInAccountSegmentFilter extends AbstractAdvertiserLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdvertiserFailedInAccountSegmentFilter.class);
    private final Map<String, String> advertiserIdNameMap;

    @Inject
    public AdvertiserFailedInAccountSegmentFilter(final Provider<Marker> traceMarkerProvider,
            @AdvertiserIdNameMap final Map<String, String> advertiserIdNameMap) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_ACCOUNT_SEGMENT_FILTER);
        this.advertiserIdNameMap = advertiserIdNameMap;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
        final int accountSegment = channelSegment.getChannelEntity().getAccountSegment();
        final String advertiserId = channelSegment.getChannelEntity().getAccountId();
        LOG.debug("AccountId from ChannelEntity: {} ", channelSegment.getChannelEntity().getAccountId());
        LOG.debug("AdvertiserId from ChannelFeedbackEntity: {}", channelSegment.getChannelFeedbackEntity()
                .getAdvertiserId());
        LOG.debug("AdvertiserId from ChannelSegmentEntity: {}", channelSegment.getChannelSegmentEntity()
                .getAdvertiserId());

        final String advertiserName = advertiserIdNameMap.get(advertiserId);
        return advertiserName == null || sasParams.getDst() == 6 && null != sasParams.getAccountSegment()
                && !sasParams.getAccountSegment().isEmpty() && !sasParams.getAccountSegment().contains(accountSegment);
    }
}
