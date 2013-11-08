package com.inmobi.adserve.channels.repository;

import com.google.common.base.Preconditions;
import com.inmobi.adserve.channels.entity.*;
import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.adserve.channels.query.PublisherFilterQuery;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.phoenix.exception.RepositoryException;
import lombok.Getter;
import lombok.Setter;


@Getter
public class RepositoryHelper
{
    private final ChannelRepository                channelRepository;
    private final ChannelAdGroupRepository         channelAdGroupRepository;
    private final ChannelFeedbackRepository        channelFeedbackRepository;
    private final ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
    private final SiteMetaDataRepository           siteMetaDataRepository;
    private final SiteTaxonomyRepository           siteTaxonomyRepository;
    private final SiteCitrusLeafFeedbackRepository siteCitrusLeafFeedbackRepository;
    private final PricingEngineRepository          pricingEngineRepository;
    private final PublisherFilterRepository        publisherFilterRepository;
    private final SiteEcpmRepository               siteEcpmRepository;
    private final RepositoryStatsProvider          repositoryStatsProvider;

    public RepositoryHelper(Builder builder)
    {
        this.channelRepository = builder.channelRepository;
        this.channelAdGroupRepository = builder.channelAdGroupRepository;
        this.channelFeedbackRepository = builder.channelFeedbackRepository;
        this.channelSegmentFeedbackRepository = builder.channelSegmentFeedbackRepository;
        this.siteMetaDataRepository = builder.siteMetaDataRepository;
        this.siteTaxonomyRepository = builder.siteTaxonomyRepository;
        this.siteCitrusLeafFeedbackRepository = builder.siteCitrusLeafFeedbackRepository;
        this.pricingEngineRepository = builder.pricingEngineRepository;
        this.publisherFilterRepository = builder.publisherFilterRepository;
        this.siteEcpmRepository = builder.siteEcpmRepository;
        this.repositoryStatsProvider = new RepositoryStatsProvider();
        this.repositoryStatsProvider
                .addRepositoryToStats(this.channelRepository)
                    .addRepositoryToStats(this.channelAdGroupRepository)
                    .addRepositoryToStats(this.channelFeedbackRepository)
                    .addRepositoryToStats(this.channelSegmentFeedbackRepository)
                    .addRepositoryToStats(this.siteMetaDataRepository)
                    .addRepositoryToStats(this.siteTaxonomyRepository)
                    .addRepositoryToStats(this.pricingEngineRepository)
                    .addRepositoryToStats(this.publisherFilterRepository)
                    .addRepositoryToStats(this.siteEcpmRepository);
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    @Setter
    public static class Builder
    {
        private ChannelRepository                channelRepository;
        private ChannelAdGroupRepository         channelAdGroupRepository;
        private ChannelFeedbackRepository        channelFeedbackRepository;
        private ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
        private SiteMetaDataRepository           siteMetaDataRepository;
        private SiteTaxonomyRepository           siteTaxonomyRepository;
        private SiteCitrusLeafFeedbackRepository siteCitrusLeafFeedbackRepository;
        private PricingEngineRepository          pricingEngineRepository;
        private PublisherFilterRepository        publisherFilterRepository;
        private SiteEcpmRepository               siteEcpmRepository;

        public RepositoryHelper build()
        {
            Preconditions.checkNotNull(channelRepository);
            Preconditions.checkNotNull(channelAdGroupRepository);
            Preconditions.checkNotNull(channelFeedbackRepository);
            Preconditions.checkNotNull(channelSegmentFeedbackRepository);
            Preconditions.checkNotNull(siteMetaDataRepository);
            Preconditions.checkNotNull(siteTaxonomyRepository);
            Preconditions.checkNotNull(siteCitrusLeafFeedbackRepository);
            Preconditions.checkNotNull(pricingEngineRepository);
            Preconditions.checkNotNull(publisherFilterRepository);
            Preconditions.checkNotNull(siteEcpmRepository);
            return new RepositoryHelper(this);
        }
    }

    public ChannelEntity queryChannelRepository(String channelId)
    {
        try {
            return channelRepository.query(channelId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public ChannelSegmentEntity queryChannelAdGroupRepository(String adGroupId)
    {
        try {
            return channelAdGroupRepository.query(adGroupId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public ChannelSegmentFeedbackEntity queryChannelSegmentFeedbackRepository(String adGroupId)
    {
        try {
            return channelSegmentFeedbackRepository.query(adGroupId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public ChannelFeedbackEntity queryChannelFeedbackRepository(String advertiserId)
    {
        try {
            return channelFeedbackRepository.query(advertiserId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SiteTaxonomyEntity querySiteTaxonomyRepository(String id)
    {
        try {
            return siteTaxonomyRepository.query(id);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SiteMetaDataEntity querySiteMetaDetaRepository(String siteId)
    {
        try {
            return siteMetaDataRepository.query(siteId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SegmentAdGroupFeedbackEntity querySiteCitrusLeafFeedbackRepository(String siteId, Integer segmentId,
            DebugLogger logger)
    {
        return siteCitrusLeafFeedbackRepository.query(siteId, segmentId, logger);
    }

    public SiteFeedbackEntity querySiteCitrusLeafFeedbackRepository(String siteId)
    {
        return siteCitrusLeafFeedbackRepository.query(siteId);
    }

    public PricingEngineEntity queryPricingEngineRepository(int country, int os)
    {
        try {
            return pricingEngineRepository.query(new PricingEngineQuery(country, os));
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public PublisherFilterEntity queryPublisherFilterRepository(String siteId, Integer ruleType)
    {
        try {
            return publisherFilterRepository.query(new PublisherFilterQuery(siteId, ruleType));
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SiteEcpmEntity querySiteEcpmRepository(String siteId, Integer countryId, Integer osId)
    {
        try {
            return siteEcpmRepository.query(new SiteEcpmQuery(siteId, countryId, osId));
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

}
