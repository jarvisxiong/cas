package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdvertiserExcludedFilter extends AbstractAdvertiserLevelFilter {
    private final RepositoryHelper repositoryHelper;

    @Inject
    public AdvertiserExcludedFilter(final Provider<Marker> traceMarkerProvider, final RepositoryHelper repositoryHelper) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_ADVERTISER_EXCLUSION_FILTER);
        this.repositoryHelper = repositoryHelper;
    }

    /**
     * Returns true if advertiser is not present in site's advertiser inclusion list OR if advertiser is not present in
     * publisher's advertiser inclusion list when site doesn't have advertiser inclusion list
     */
    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
        final String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        final SiteMetaDataEntity siteMetaDataEntity =
                repositoryHelper.querySiteMetaDetaRepository(sasParams.getSiteId());
        if (siteMetaDataEntity != null) {
            final Set<String> advertisersIncludedbySite = siteMetaDataEntity.getAdvertisersIncludedBySite();
            final Set<String> advertisersIncludedbyPublisher = siteMetaDataEntity.getAdvertisersIncludedByPublisher();
            // checking if site has advertiser inclusion list
            if (!advertisersIncludedbySite.isEmpty()) {
                return !advertisersIncludedbySite.contains(advertiserId);
            } else { // else checking in publisher advertiser inclusion list if any
                return !advertisersIncludedbyPublisher.isEmpty()
                        && !advertisersIncludedbyPublisher.contains(advertiserId);
            }
        }
        return false;
    }

}
