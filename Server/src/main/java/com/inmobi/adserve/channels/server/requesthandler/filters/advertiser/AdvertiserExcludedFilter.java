package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdvertiserExcludedFilter extends AbstractAdvertiserLevelFilter {

    private static final Logger    LOG = LoggerFactory.getLogger(AdvertiserExcludedFilter.class);

    private final RepositoryHelper repositoryHelper;

    @Inject
    public AdvertiserExcludedFilter(final Provider<Marker> traceMarkerProvider, final RepositoryHelper repositoryHelper) {
        super(traceMarkerProvider, InspectorStrings.droppedinAdvertiserInclusionFilter);
        this.repositoryHelper = repositoryHelper;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams) {
        boolean result = false;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        SiteMetaDataEntity siteMetaDataEntity = repositoryHelper.querySiteMetaDetaRepository(sasParams.getSiteId());
        if (siteMetaDataEntity != null) {
            Set<String> advertisersIncludedbySite = siteMetaDataEntity.getAdvertisersIncludedBySite();
            Set<String> advertisersIncludedbyPublisher = siteMetaDataEntity.getAdvertisersIncludedByPublisher();
            // checking if site has advertiser inclusion list
            if (!advertisersIncludedbySite.isEmpty()) {
                result = !advertisersIncludedbySite.contains(advertiserId);
            }
            // else checking in publisher advertiser inclusion list if any
            else {
                result = !advertisersIncludedbyPublisher.isEmpty()
                        && !advertisersIncludedbyPublisher.contains(advertiserId);
            }
        }
        return result;
    }

}