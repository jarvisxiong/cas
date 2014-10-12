package com.inmobi.adserve.channels.repository;

import lombok.Getter;
import lombok.Setter;

import com.google.common.base.Preconditions;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SiteFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteFilterEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.adserve.channels.query.SiteFilterQuery;
import com.inmobi.phoenix.exception.RepositoryException;


@Getter
public class RepositoryHelper {
    private final ChannelRepository                channelRepository;
    private final ChannelAdGroupRepository         channelAdGroupRepository;
    private final ChannelFeedbackRepository        channelFeedbackRepository;
    private final ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
    private final SiteMetaDataRepository           siteMetaDataRepository;
    private final SiteTaxonomyRepository           siteTaxonomyRepository;
    private final SiteAerospikeFeedbackRepository  siteAerospikeFeedbackRepository;
    private final PricingEngineRepository          pricingEngineRepository;
    private final SiteFilterRepository             siteFilterRepository;
    private final SiteEcpmRepository               siteEcpmRepository;
    private final CurrencyConversionRepository     currencyConversionRepository;
    private final WapSiteUACRepository             wapSiteUACRepository;
    private final IXAccountMapRepository           ixAccountMapRepository;
    private final CreativeRepository               creativeRepository;
    private final RepositoryStatsProvider          repositoryStatsProvider;
    private final NativeAdTemplateRepository	   nativeAdTemplateRepository;

    public RepositoryHelper(final Builder builder) {
        this.channelRepository = builder.channelRepository;
        this.channelAdGroupRepository = builder.channelAdGroupRepository;
        this.channelFeedbackRepository = builder.channelFeedbackRepository;
        this.channelSegmentFeedbackRepository = builder.channelSegmentFeedbackRepository;
        this.siteMetaDataRepository = builder.siteMetaDataRepository;
        this.siteTaxonomyRepository = builder.siteTaxonomyRepository;
        this.siteAerospikeFeedbackRepository = builder.siteAerospikeFeedbackRepository;
        this.pricingEngineRepository = builder.pricingEngineRepository;
        this.siteFilterRepository = builder.siteFilterRepository;
        this.siteEcpmRepository = builder.siteEcpmRepository;
        this.currencyConversionRepository = builder.currencyConversionRepository;
        this.wapSiteUACRepository = builder.wapSiteUACRepository;
        this.ixAccountMapRepository = builder.ixAccountMapRepository;
        this.creativeRepository = builder.creativeRepository;
        this.nativeAdTemplateRepository = builder.nativeAdTemplateRepository;
        this.repositoryStatsProvider = new RepositoryStatsProvider();
        this.repositoryStatsProvider
                .addRepositoryToStats(this.nativeAdTemplateRepository)
                .addRepositoryToStats(this.channelRepository)
                .addRepositoryToStats(this.channelAdGroupRepository)
                .addRepositoryToStats(this.channelFeedbackRepository)
                .addRepositoryToStats(this.channelSegmentFeedbackRepository)
                .addRepositoryToStats(this.siteMetaDataRepository)
                .addRepositoryToStats(this.siteTaxonomyRepository)
                .addRepositoryToStats(this.pricingEngineRepository)
                .addRepositoryToStats(this.siteFilterRepository)
                .addRepositoryToStats(this.siteEcpmRepository)
                .addRepositoryToStats(this.currencyConversionRepository)
                .addRepositoryToStats(this.wapSiteUACRepository)
                .addRepositoryToStats(this.ixAccountMapRepository)
                .addRepositoryToStats(this.creativeRepository)
                .addRepositoryToStats(this.nativeAdTemplateRepository);

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
        private SiteAerospikeFeedbackRepository  siteAerospikeFeedbackRepository;
        private PricingEngineRepository          pricingEngineRepository;
        private SiteFilterRepository             siteFilterRepository;
        private SiteEcpmRepository               siteEcpmRepository;
        private CurrencyConversionRepository     currencyConversionRepository;
        private WapSiteUACRepository             wapSiteUACRepository;
        private IXAccountMapRepository           ixAccountMapRepository;
        private CreativeRepository               creativeRepository;
        private NativeAdTemplateRepository       nativeAdTemplateRepository;

        public RepositoryHelper build() {
            Preconditions.checkNotNull(channelRepository);
            Preconditions.checkNotNull(channelAdGroupRepository);
            Preconditions.checkNotNull(channelFeedbackRepository);
            Preconditions.checkNotNull(channelSegmentFeedbackRepository);
            Preconditions.checkNotNull(siteMetaDataRepository);
            Preconditions.checkNotNull(siteTaxonomyRepository);
            Preconditions.checkNotNull(siteAerospikeFeedbackRepository);
            Preconditions.checkNotNull(pricingEngineRepository);
            Preconditions.checkNotNull(siteFilterRepository);
            Preconditions.checkNotNull(siteEcpmRepository);
            Preconditions.checkNotNull(currencyConversionRepository);
            Preconditions.checkNotNull(wapSiteUACRepository);
            Preconditions.checkNotNull(ixAccountMapRepository);
            Preconditions.checkNotNull(creativeRepository);
            Preconditions.checkNotNull(nativeAdTemplateRepository);
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

    public SegmentAdGroupFeedbackEntity querySiteAerospikeFeedbackRepository(final String siteId,
            final Integer segmentId) {
        return siteAerospikeFeedbackRepository.query(siteId, segmentId);
    }

    public SiteFeedbackEntity querySiteAerospikeFeedbackRepository(final String siteId) {
        return siteAerospikeFeedbackRepository.query(siteId);
    }

    public PricingEngineEntity queryPricingEngineRepository(final int country, final int os) {
        try {
            return pricingEngineRepository.query(new PricingEngineQuery(country, os));
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public CreativeEntity queryCreativeRepository(final String advertiserId, final String creativeId) {
        try {
            return creativeRepository.query(new CreativeQuery(advertiserId, creativeId));
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }

    public SiteFilterEntity querySiteFilterRepository(final String siteId, final Integer ruleType) {
        try {
            return siteFilterRepository.query(new SiteFilterQuery(siteId, ruleType));
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
    
    public WapSiteUACEntity queryWapSiteUACRepository(final String id) {
        try {
            return wapSiteUACRepository.query(id);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }
    
    public IXAccountMapEntity queryIXAccountMapRepository(final Long rpNetworkId) {
      try {
          return ixAccountMapRepository.query(rpNetworkId);
      }
      catch (RepositoryException ignored) {
      }
      return null;
  }

   public NativeAdTemplateEntity queryNativeAdTemplateRepository(final String siteId) {
        try {
            return nativeAdTemplateRepository.query(siteId);
        }
        catch (RepositoryException ignored) {
        }
        return null;
    }
}