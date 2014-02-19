package com.inmobi.adserve.channels.repository;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.PublisherFilterEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SiteFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.adserve.channels.query.PublisherFilterQuery;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.phoenix.exception.RepositoryException;


@Getter
public class RepositoryHelper {
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
    private final CurrencyConversionRepository     currencyConversionRepository;
    private final RepositoryStatsProvider          repositoryStatsProvider;
    private static final Logger                    LOG = LoggerFactory.getLogger(RepositoryHelper.class);

    public RepositoryHelper(final Builder builder) {
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
        this.currencyConversionRepository = builder.currencyConversionRepository;
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
                    .addRepositoryToStats(this.siteEcpmRepository)
                    .addRepositoryToStats(this.currencyConversionRepository);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
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
        private CurrencyConversionRepository     currencyConversionRepository;

        public RepositoryHelper build() {
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
            Preconditions.checkNotNull(currencyConversionRepository);
            return new RepositoryHelper(this);
        }
    }

    public ChannelEntity queryChannelRepository(final String channelId) {
        try {
            return channelRepository.query(channelId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public ChannelSegmentEntity queryChannelAdGroupRepository(final String adGroupId) {
        try {
            return channelAdGroupRepository.query(adGroupId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public ChannelSegmentFeedbackEntity queryChannelSegmentFeedbackRepository(final String adGroupId) {
        try {
            return channelSegmentFeedbackRepository.query(adGroupId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public ChannelFeedbackEntity queryChannelFeedbackRepository(final String advertiserId) {
        try {
            return channelFeedbackRepository.query(advertiserId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SiteTaxonomyEntity querySiteTaxonomyRepository(final String id) {
        try {
            return siteTaxonomyRepository.query(id);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SiteMetaDataEntity querySiteMetaDetaRepository(final String siteId) {
        try {
            return siteMetaDataRepository.query(siteId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SegmentAdGroupFeedbackEntity querySiteCitrusLeafFeedbackRepository(final String siteId,
            final Integer segmentId) {
        return siteCitrusLeafFeedbackRepository.query(siteId, segmentId);
    }

    public SiteFeedbackEntity querySiteCitrusLeafFeedbackRepository(final String siteId) {
        return siteCitrusLeafFeedbackRepository.query(siteId);
    }

    public PricingEngineEntity queryPricingEngineRepository(final int country, final int os) {
        try {
            return pricingEngineRepository.query(new PricingEngineQuery(country, os));
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public PublisherFilterEntity queryPublisherFilterRepository(final String siteId, final Integer ruleType) {
        try {
            return publisherFilterRepository.query(new PublisherFilterQuery(siteId, ruleType));
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SiteEcpmEntity querySiteEcpmRepository(final String siteId, final Integer countryId, final Integer osId) {
        try {
            return siteEcpmRepository.query(new SiteEcpmQuery(siteId, countryId, osId));
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public CurrencyConversionEntity queryCurrencyConversionRepository(final String countryId) {
        try {
            return currencyConversionRepository.query(countryId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

}