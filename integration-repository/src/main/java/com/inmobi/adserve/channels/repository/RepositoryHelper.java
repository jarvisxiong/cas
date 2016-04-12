package com.inmobi.adserve.channels.repository;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.in;

import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.entity.CAUMetadataEntity;
import com.inmobi.adserve.channels.entity.CcidMapEntity;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.GeoRegionFenceMapEntity;
import com.inmobi.adserve.channels.entity.GeoZipEntity;
import com.inmobi.adserve.channels.entity.IMEIEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.IXBlocklistEntity;
import com.inmobi.adserve.channels.entity.IXBlocklistRepository;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SdkMraidMapEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SiteFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteFilterEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.query.IXBlocklistsQuery;
import com.inmobi.adserve.channels.query.NativeAdTemplateQuery;
import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.adserve.channels.query.SiteFilterQuery;
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.phoenix.exception.RepositoryException;

import lombok.Getter;
import lombok.Setter;


@Getter
public class RepositoryHelper {
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(RepositoryHelper.class);
    private final RepositoryStatsProvider repositoryStatsProvider;
    private final IXPackageRepository ixPackageRepository;
    private final SiteAerospikeFeedbackRepository siteAerospikeFeedbackRepository;
    private final IMEIAerospikeRepository imeiAerospikeRepository;

    private final ChannelRepository channelRepository;
    private final ChannelAdGroupRepository channelAdGroupRepository;
    private final ChannelFeedbackRepository channelFeedbackRepository;
    private final ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
    private final SiteMetaDataRepository siteMetaDataRepository;
    private final SiteTaxonomyRepository siteTaxonomyRepository;
    private final PricingEngineRepository pricingEngineRepository;
    private final SiteFilterRepository siteFilterRepository;
    private final SiteEcpmRepository siteEcpmRepository;
    private final CurrencyConversionRepository currencyConversionRepository;
    private final WapSiteUACRepository wapSiteUACRepository;
    private final IXAccountMapRepository ixAccountMapRepository;
    private final CreativeRepository creativeRepository;
    private final NativeAdTemplateRepository nativeAdTemplateRepository;
    private final GeoZipRepository geoZipRepository;
    private final SlotSizeMapRepository slotSizeMapRepository;
    private final SdkMraidMapRepository sdkMraidMapRepository;
    private final GeoRegionFenceMapRepository geoRegionFenceMapRepository;
    private final CcidMapRepository ccidMapRepository;
    private final IXBlocklistRepository ixBlocklistRepository;
    private final CAUMetaDataRepository cauMetaDataRepository;

    public RepositoryHelper(final Builder builder) {
        channelRepository = builder.channelRepository;
        channelAdGroupRepository = builder.channelAdGroupRepository;
        channelFeedbackRepository = builder.channelFeedbackRepository;
        channelSegmentFeedbackRepository = builder.channelSegmentFeedbackRepository;
        siteMetaDataRepository = builder.siteMetaDataRepository;
        siteTaxonomyRepository = builder.siteTaxonomyRepository;
        siteAerospikeFeedbackRepository = builder.siteAerospikeFeedbackRepository;
        imeiAerospikeRepository = builder.imeiAerospikeRepository;
        pricingEngineRepository = builder.pricingEngineRepository;
        siteFilterRepository = builder.siteFilterRepository;
        siteEcpmRepository = builder.siteEcpmRepository;
        currencyConversionRepository = builder.currencyConversionRepository;
        wapSiteUACRepository = builder.wapSiteUACRepository;
        ixAccountMapRepository = builder.ixAccountMapRepository;
        creativeRepository = builder.creativeRepository;
        nativeAdTemplateRepository = builder.nativeAdTemplateRepository;
        ixPackageRepository = builder.ixPackageRepository;
        geoZipRepository = builder.geoZipRepository;
        slotSizeMapRepository = builder.slotSizeMapRepository;
        sdkMraidMapRepository = builder.sdkMraidMapRepository;
        geoRegionFenceMapRepository = builder.geoRegionFenceMapRepository;
        ccidMapRepository = builder.ccidMapRepository;
        ixBlocklistRepository = builder.ixBlocklistRepository;
        cauMetaDataRepository = builder.cauMetaDataRepository;

        repositoryStatsProvider = new RepositoryStatsProvider();
        repositoryStatsProvider.addRepositoryToStats(nativeAdTemplateRepository)
                .addRepositoryToStats(channelRepository).addRepositoryToStats(channelAdGroupRepository)
                .addRepositoryToStats(channelFeedbackRepository).addRepositoryToStats(channelSegmentFeedbackRepository)
                .addRepositoryToStats(siteMetaDataRepository).addRepositoryToStats(siteTaxonomyRepository)
                .addRepositoryToStats(pricingEngineRepository).addRepositoryToStats(siteFilterRepository)
                .addRepositoryToStats(siteEcpmRepository).addRepositoryToStats(currencyConversionRepository)
                .addRepositoryToStats(wapSiteUACRepository).addRepositoryToStats(ixAccountMapRepository)
                .addRepositoryToStats(creativeRepository).addRepositoryToStats(geoZipRepository)
                .addRepositoryToStats(slotSizeMapRepository).addRepositoryToStats(geoRegionFenceMapRepository)
                .addRepositoryToStats(ccidMapRepository).addRepositoryToStats(ixBlocklistRepository)
                .addRepositoryToStats(sdkMraidMapRepository).addRepositoryToStats(cauMetaDataRepository);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private ChannelRepository channelRepository;
        private ChannelAdGroupRepository channelAdGroupRepository;
        private ChannelFeedbackRepository channelFeedbackRepository;
        private ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
        private SiteMetaDataRepository siteMetaDataRepository;
        private SiteTaxonomyRepository siteTaxonomyRepository;
        private SiteAerospikeFeedbackRepository siteAerospikeFeedbackRepository;
        private IMEIAerospikeRepository imeiAerospikeRepository;
        private PricingEngineRepository pricingEngineRepository;
        private SiteFilterRepository siteFilterRepository;
        private SiteEcpmRepository siteEcpmRepository;
        private CurrencyConversionRepository currencyConversionRepository;
        private WapSiteUACRepository wapSiteUACRepository;
        private IXAccountMapRepository ixAccountMapRepository;
        private CreativeRepository creativeRepository;
        private NativeAdTemplateRepository nativeAdTemplateRepository;
        private IXPackageRepository ixPackageRepository;
        private GeoZipRepository geoZipRepository;
        private SlotSizeMapRepository slotSizeMapRepository;
        private SdkMraidMapRepository sdkMraidMapRepository;
        private GeoRegionFenceMapRepository geoRegionFenceMapRepository;
        private CcidMapRepository ccidMapRepository;
        private IXBlocklistRepository ixBlocklistRepository;
        private CAUMetaDataRepository cauMetaDataRepository;

        public RepositoryHelper build() {
            Preconditions.checkNotNull(channelRepository);
            Preconditions.checkNotNull(channelAdGroupRepository);
            Preconditions.checkNotNull(channelFeedbackRepository);
            Preconditions.checkNotNull(channelSegmentFeedbackRepository);
            Preconditions.checkNotNull(siteMetaDataRepository);
            Preconditions.checkNotNull(siteTaxonomyRepository);
            Preconditions.checkNotNull(siteAerospikeFeedbackRepository);
            Preconditions.checkNotNull(imeiAerospikeRepository);
            Preconditions.checkNotNull(pricingEngineRepository);
            Preconditions.checkNotNull(siteFilterRepository);
            Preconditions.checkNotNull(siteEcpmRepository);
            Preconditions.checkNotNull(currencyConversionRepository);
            Preconditions.checkNotNull(wapSiteUACRepository);
            Preconditions.checkNotNull(ixAccountMapRepository);
            Preconditions.checkNotNull(creativeRepository);
            Preconditions.checkNotNull(nativeAdTemplateRepository);
            Preconditions.checkNotNull(ixPackageRepository);
            Preconditions.checkNotNull(geoZipRepository);
            Preconditions.checkNotNull(slotSizeMapRepository);
            Preconditions.checkNotNull(geoRegionFenceMapRepository);
            Preconditions.checkNotNull(ccidMapRepository);
            Preconditions.checkNotNull(ixBlocklistRepository);
            Preconditions.checkNotNull(sdkMraidMapRepository);
            Preconditions.checkNotNull(cauMetaDataRepository);
            return new RepositoryHelper(this);
        }
    }

    public CAUMetadataEntity queryCauMetaDataRepository(final Long id) {
        try {
            return cauMetaDataRepository.query(id);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying CAU MetaData Repository, {}", ignored);
        }
        return null;
    }

    public GeoZipEntity queryGeoZipRepository(final Integer zipId) {
        try {
            return geoZipRepository.query(zipId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying Geo Zip Repository, {}", ignored);
        }
        return null;
    }

    public SlotSizeMapEntity querySlotSizeMapRepository(final Short slotId) {
        try {
            return slotSizeMapRepository.query(slotId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying SlotSizeMap Repository, {}", ignored);
        }
        return null;
    }

    public ChannelEntity queryChannelRepository(final String channelId) {
        try {
            return channelRepository.query(channelId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying Channel Repository, {}", ignored);
        }
        return null;
    }

    public ChannelSegmentEntity queryChannelAdGroupRepository(final String adGroupId) {
        try {
            return channelAdGroupRepository.query(adGroupId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying ChannelAdGroup Repository, {}", ignored);
        }
        return null;
    }

    public ChannelSegmentFeedbackEntity queryChannelSegmentFeedbackRepository(final String adGroupId) {
        try {
            return channelSegmentFeedbackRepository.query(adGroupId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying ChannelSegmentFeedback Repository, {}", ignored);
        }
        return null;
    }

    public ChannelFeedbackEntity queryChannelFeedbackRepository(final String advertiserId) {
        try {
            return channelFeedbackRepository.query(advertiserId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying ChannelFeedback Repository, {}", ignored);
        }
        return null;
    }

    public SiteTaxonomyEntity querySiteTaxonomyRepository(final String id) {
        try {
            return siteTaxonomyRepository.query(id);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying SiteTaxonomy Repository, {}", ignored);
        }
        return null;
    }

    public SiteMetaDataEntity querySiteMetaDetaRepository(final String siteId) {
        try {
            return siteMetaDataRepository.query(siteId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying SiteMetaData Repository, {}", ignored);
        }
        return null;
    }

    public SegmentAdGroupFeedbackEntity querySiteAerospikeFeedbackRepository(final String siteId,
            final Integer segmentId) {
        return siteAerospikeFeedbackRepository.query(siteId, segmentId);
    }

    public IMEIEntity queryIMEIRepository(final String gpId) {
        return imeiAerospikeRepository.query(gpId);
    }

    public SiteFeedbackEntity querySiteAerospikeFeedbackRepository(final String siteId) {
        return siteAerospikeFeedbackRepository.query(siteId);
    }

    public PricingEngineEntity queryPricingEngineRepository(final Integer country, final Integer os) {
        try {
            return pricingEngineRepository.query(new PricingEngineQuery(country, os));
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying PricingEngine Repository, {}", ignored);
        }
        return null;
    }

    public CreativeEntity queryCreativeRepository(final String advertiserId, final String creativeId) {
        try {
            return creativeRepository.query(new CreativeQuery(advertiserId, creativeId));
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying Creative Repository, {}", ignored);
        }
        return null;
    }

    public SiteFilterEntity querySiteFilterRepository(final String siteId, final Integer ruleType) {
        try {
            return siteFilterRepository.query(new SiteFilterQuery(siteId, ruleType));
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying SiteFilter Repository, {}", ignored);
        }
        return null;
    }

    public SiteEcpmEntity querySiteEcpmRepository(final String siteId, final Integer countryId, final Integer osId) {
        try {
            return siteEcpmRepository.query(new SiteEcpmQuery(siteId, countryId, osId));
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying SiteEcpm Repository, {}", ignored);
        }
        return null;
    }

    public CurrencyConversionEntity queryCurrencyConversionRepository(final String countryId) {
        try {
            return currencyConversionRepository.query(countryId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying CurrencyConversion Repository, {}", ignored);
        }
        return null;
    }

    public WapSiteUACEntity queryWapSiteUACRepository(final String id) {
        try {
            return wapSiteUACRepository.query(id);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying WapSiteUAC Repository, {}", ignored);
        }
        return null;
    }

    public SdkMraidMapEntity querySdkMraidMapRepository(final String sdk_name) {
        try {
            return sdkMraidMapRepository.query(sdk_name);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying SdkMraidMap Repository, {}", ignored);
        }
        return null;
    }

    public IXAccountMapEntity queryIXAccountMapRepository(final Long rpNetworkId) {
        try {
            return ixAccountMapRepository.query(rpNetworkId);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying IXAccountMap Repository, {}", ignored);
        }
        return null;
    }

    public NativeAdTemplateEntity queryNativeAdTemplateRepository(final Long placementId, final TemplateClass templateClass) {
        try {
            return nativeAdTemplateRepository.query(new NativeAdTemplateQuery(placementId, templateClass));
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying NativeAdTemplate Repository, {}", ignored);
        }
        return null;
    }

    public GeoRegionFenceMapEntity queryGeoRegionFenceMapRepositoryByRegionNameCountryCombo(
            final String geoRegionNameCountryCombo) {
        try {
            return geoRegionFenceMapRepository.query(geoRegionNameCountryCombo);
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying GeoRegionFenceMap Repository, {}", ignored);
        }
        return null;
    }

    public CcidMapEntity queryCcidMapRepository(final Integer ccid) {
        if (null != ccid) {
            try {
                return ccidMapRepository.query(ccid);
            } catch (final RepositoryException ignored) {
                LOG.debug("Exception while querying Ccid Map Repository, {}", ignored);
            }
        }
        return null;
    }

    public IXBlocklistEntity queryIXBlocklistRepository(final String key, final IXBlocklistKeyType keyType,
            final IXBlocklistType blocklistType) {
        try {
            return ixBlocklistRepository.query(new IXBlocklistsQuery(key, keyType, blocklistType));
        } catch (final RepositoryException ignored) {
            LOG.debug("Exception while querying IX Blocklist Repository, {}", ignored);
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResultSet<IXPackageEntity> queryIXPackageRepository(final int osId, final String siteId,
            final int countryId, final int slotId) {
        // Prepare query for CQEngine repository
        final Query query =
                and(in(IXPackageRepository.OS_ID, osId, IXPackageRepository.ALL_OS_ID),
                        in(IXPackageRepository.SITE_ID, siteId, IXPackageRepository.ALL_SITE_ID),
                        in(IXPackageRepository.COUNTRY_ID, countryId, IXPackageRepository.ALL_COUNTRY_ID),
                        in(IXPackageRepository.SLOT_ID, slotId, IXPackageRepository.ALL_SLOT_ID));
        return ixPackageRepository.getPackageIndex().retrieve(query);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public IXPackageEntity queryIxPackageByDeal(final String dealId) {
        // Prepare query for CQEngine repository
        final Query query = equal(IXPackageRepository.DEAL_IDS, dealId);
        return (IXPackageEntity) ixPackageRepository.getPackageIndex().retrieve(query).uniqueResult();
    }

}
