package com.inmobi.adserve.channels.repository;

import static com.googlecode.cqengine.query.QueryFactory.and;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.has;
import static com.googlecode.cqengine.query.QueryFactory.in;
import static com.googlecode.cqengine.query.QueryFactory.not;
import static com.googlecode.cqengine.query.QueryFactory.or;
import static com.inmobi.adserve.channels.repository.pmp.DealAttributes.DEAL_ID;
import static com.inmobi.adserve.channels.repository.pmp.DealAttributes.DSP_ID;
import static com.inmobi.adserve.channels.repository.pmp.DealAttributes.DST_ID;
import static com.inmobi.adserve.channels.repository.pmp.DealAttributes.PACKAGE_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.ALL_COUNTRY_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.ALL_OS_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.ALL_SITE_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.ALL_SLOT_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.COUNTRY_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.OS_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.SITE_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.SLOT_ID;
import static com.inmobi.adserve.channels.repository.pmp.PackageAttributes.NONE;
import static com.inmobi.adserve.channels.repository.pmp.PackageAttributes.TARGETING_SEGMENT_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.ALL;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.COUNTRY_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.DSP_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.DST_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_AD_FORMATS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_CARRIERS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_CONNECTION_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_INTEGRATION_METHODS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_INVENTORY_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_LANGUAGES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_LOCATION_SOURCES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_PUBLISHERS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_SITES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_SITE_CONTENT_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_SLOTS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_AD_FORMATS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_CARRIERS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_CONNECTION_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_INTEGRATION_METHODS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_INVENTORY_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_LANGUAGES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_LOCATION_SOURCES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_PUBLISHERS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_SITES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_SITE_CONTENT_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_SLOTS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.MANUF_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.OS_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.TARGETING_SEGMENT_ID;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.USD;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.IntegrationMethod;
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
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.entity.pmp.PackageEntity;
import com.inmobi.adserve.channels.entity.pmp.TargetingSegmentEntity;
import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.query.IXBlocklistsQuery;
import com.inmobi.adserve.channels.query.NativeAdTemplateQuery;
import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.adserve.channels.query.SiteFilterQuery;
import com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes;
import com.inmobi.adserve.channels.repository.pmp.PackageAttributes;
import com.inmobi.adserve.channels.repository.pmp.PackageRepositoryV2;
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.phoenix.exception.RepositoryException;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;

import lombok.Getter;
import lombok.Setter;


@Getter
public class RepositoryHelper {
    public final static boolean QUERY_IN_OLD_REPO = true;
    public final static boolean DO_NOT_QUERY_IN_OLD_REPO = false;

    private final static Logger LOG = LoggerFactory.getLogger(RepositoryHelper.class);
    private final RepositoryStatsProvider repositoryStatsProvider;
    private final IXPackageRepository ixPackageRepository;
    private final PackageRepositoryV2 packageRepositoryV2;
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
        packageRepositoryV2 = builder.PackageRepositoryV2;
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
        private PackageRepositoryV2 PackageRepositoryV2;
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
            Preconditions.checkNotNull(PackageRepositoryV2);
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

    public double calculatePriceInUSD(final double price, final String currencyCode) {
        if (StringUtils.isEmpty(currencyCode) || USD.equalsIgnoreCase(currencyCode)) {
            return price;
        } else {
            final CurrencyConversionEntity currencyConversionEntity = queryCurrencyConversionRepository(currencyCode);
            if (null != currencyConversionEntity && null != currencyConversionEntity.getConversionRate()
                    && currencyConversionEntity.getConversionRate() > 0.0) {
                return price / currencyConversionEntity.getConversionRate();
            }
        }
        return price;
    }

    public double calculatePriceInLocal(final double price, final String currencyCode) {
        if (USD.equalsIgnoreCase(currencyCode)) {
            return price;
        }
        final CurrencyConversionEntity currencyConversionEntity = queryCurrencyConversionRepository(currencyCode);
        if (null != currencyConversionEntity && null != currencyConversionEntity.getConversionRate()) {
            return price * currencyConversionEntity.getConversionRate();
        }
        return price;
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

    public ResultSet<IXPackageEntity> queryIXPackageRepository(final int osId, final String siteId,
            final int countryId, final int slotId) {
        // Prepare query for CQEngine repository
        final Query<IXPackageEntity> query = and(
                    or(equal(OS_ID, osId), equal(OS_ID, ALL_OS_ID)),
                    or(equal(SITE_ID, siteId), equal(SITE_ID, ALL_SITE_ID)),
                    or(equal(COUNTRY_ID, countryId), equal(COUNTRY_ID, ALL_COUNTRY_ID)),
                    or(equal(SLOT_ID, slotId), equal(SLOT_ID, ALL_SLOT_ID))
        );
        return ixPackageRepository.getIndexedPackages().retrieve(query);
    }

    @SuppressWarnings("unchecked")
    public ResultSet<TargetingSegmentEntity> queryForMatchingTargetingSegments(
            final DemandSourceType dst,
            final String advertiserId,
            final InventoryType inventoryType,
            final ContentType contentRating,
            final ConnectionType connectionType,
            final LocationSource locationSource,
            final IntegrationMethod integrationMethod,
            final short slotId,
            final SecondaryAdFormatConstraints adType,
            final String publisherId,
            final String siteId,
            final String languageCode,
            final Integer carrierId,
            final int osId,
            final long countryId,
            final Long manufacturer
        ) {
        final Long carrier = null != carrierId ? new Long(carrierId) : null;
        final int country = (int)countryId;

        final Query<TargetingSegmentEntity> dstQuery = in (DST_IDS, dst);

        final Query<TargetingSegmentEntity> dspQuery = StringUtils.isNotBlank(advertiserId) ?
                or(not(has(DSP_IDS)), in (DSP_IDS, advertiserId)) : ALL;
        
        final Query<TargetingSegmentEntity> inclusionInventoryTypeQuery = null != inventoryType ?
                or(not(has(INC_INVENTORY_TYPES)), in (INC_INVENTORY_TYPES, inventoryType)) : ALL;
        final Query<TargetingSegmentEntity> exclusionInventoryTypeQuery = null != inventoryType ?
                not(in (EXC_INVENTORY_TYPES, inventoryType)) : ALL;
        
        final Query<TargetingSegmentEntity> inclusionContentRatingQuery = null != contentRating ?
                or(not(has(INC_SITE_CONTENT_TYPES)), in (INC_SITE_CONTENT_TYPES, contentRating)) : ALL;
        final Query<TargetingSegmentEntity> exclusionContentRatingQuery = null != contentRating ?
                not(in (EXC_SITE_CONTENT_TYPES, contentRating)) : ALL;
        
        final Query<TargetingSegmentEntity> inclusionConnectionTypeQuery = null != connectionType ?
                or(not(has(INC_CONNECTION_TYPES)), in (INC_CONNECTION_TYPES, connectionType)) : ALL;
        final Query<TargetingSegmentEntity> exclusionConnectionTypeQuery = null != connectionType ?
                not(in (EXC_CONNECTION_TYPES, connectionType)) : ALL;
        
        final Query<TargetingSegmentEntity> inclusionLocationSourceQuery = null != locationSource ?
                or(not(has(INC_LOCATION_SOURCES)), in (INC_LOCATION_SOURCES, locationSource)) : ALL;
        final Query<TargetingSegmentEntity> exclusionLocationSourceQuery = null != locationSource ?
                not(in (EXC_LOCATION_SOURCES, locationSource)) : ALL;
        
        final Query<TargetingSegmentEntity> inclusionIntegrationMethodQuery = null != integrationMethod ?
                or(not(has(INC_INTEGRATION_METHODS)), in (INC_INTEGRATION_METHODS, integrationMethod)) : ALL;
        final Query<TargetingSegmentEntity> exclusionIntegrationMethodQuery = null != integrationMethod ?
                not(in (EXC_INTEGRATION_METHODS, integrationMethod)) : ALL;

        final Query<TargetingSegmentEntity> inclusionCarrierQuery = null != carrier ?
                or(not(has(INC_CARRIERS)), in (INC_CARRIERS, carrier)) : ALL;
        final Query<TargetingSegmentEntity> exclusionCarrierQuery = null != carrier ?
                not(in (EXC_CARRIERS, carrier)) : ALL;

        final Query<TargetingSegmentEntity> inclusionLanguageQuery = StringUtils.isNotBlank(languageCode) ?
                or(not(has(INC_LANGUAGES)), in (INC_LANGUAGES, languageCode)) : ALL;
        final Query<TargetingSegmentEntity> exclusionLanguageQuery = StringUtils.isNotBlank(languageCode) ?
                not(in (EXC_LANGUAGES, languageCode)) : ALL;

        final Query<TargetingSegmentEntity> inclusionAdTypeQuery = null != adType ?
                or(not(has(INC_AD_FORMATS)), in (INC_AD_FORMATS, adType)) : ALL;
        final Query<TargetingSegmentEntity> exclusionAdTypeQuery = null != adType ?
                not(in (EXC_AD_FORMATS, adType)) : ALL;

        final Query<TargetingSegmentEntity> inclusionPublisherIdQuery = StringUtils.isNotBlank(publisherId) ?
                or(not(has(INC_PUBLISHERS)), in (INC_PUBLISHERS, publisherId)) : ALL;
        final Query<TargetingSegmentEntity> exclusionPublisherIdQuery = StringUtils.isNotBlank(publisherId) ?
                not(in (EXC_PUBLISHERS, publisherId)) : ALL;

        final Query<TargetingSegmentEntity> inclusionSiteIdQuery = StringUtils.isNotBlank(siteId) ?
                or(not(has(INC_SITES)), in (INC_SITES, siteId)) : ALL;
        final Query<TargetingSegmentEntity> exclusionSiteIdQuery = StringUtils.isNotBlank(siteId) ?
                not(in (EXC_SITES, siteId)) : ALL;

        final Query<TargetingSegmentEntity> manufacturerQuery = null != manufacturer ?
                or(not(has(MANUF_IDS)), in (MANUF_IDS, manufacturer)) : ALL;

        final Query<TargetingSegmentEntity> osQuery = or(not(has(OS_IDS)), in (OS_IDS, osId));
        final Query<TargetingSegmentEntity> countryQuery = or(not(has(COUNTRY_IDS)), in (COUNTRY_IDS, country));
        final Query<TargetingSegmentEntity> inclusionSlotsQuery = or(not(has(INC_SLOTS)), in (INC_SLOTS, slotId));
        final Query<TargetingSegmentEntity> exclusionSlotsQuery = not(in (EXC_SLOTS, slotId));

        final Query<TargetingSegmentEntity> query =
                and(
                    dstQuery,
                    dspQuery,
                    inclusionInventoryTypeQuery, exclusionInventoryTypeQuery,
                    inclusionContentRatingQuery, exclusionContentRatingQuery,
                    inclusionConnectionTypeQuery, exclusionConnectionTypeQuery,
                    inclusionLocationSourceQuery, exclusionLocationSourceQuery,
                    inclusionIntegrationMethodQuery, exclusionIntegrationMethodQuery,
                    inclusionCarrierQuery, exclusionCarrierQuery,
                    inclusionLanguageQuery, exclusionLanguageQuery,
                    inclusionAdTypeQuery, exclusionAdTypeQuery,
                    inclusionPublisherIdQuery, exclusionPublisherIdQuery,
                    inclusionSiteIdQuery, exclusionSiteIdQuery,
                    inclusionSlotsQuery, exclusionSlotsQuery,
                    manufacturerQuery,
                    osQuery,
                    countryQuery
                );
        return packageRepositoryV2.getIndexedTargetingSegments().retrieve(query);
    }

    public final Optional<IXPackageEntity> queryIXPackageEntityByPackageId(final Integer packageId) {
        Optional<IXPackageEntity> ixPackageEntity = Optional.empty();

        if (null != packageId) {
            final Query<IXPackageEntity> query = equal(DeprecatedIXPackageAttributes.PACKAGE_ID, packageId);
            final ResultSet<IXPackageEntity> rs = ixPackageRepository.getIndexedPackages().retrieve(query);
            if (rs.isNotEmpty()) {
                try {
                    ixPackageEntity = Optional.of(rs.uniqueResult());
                } catch (final Exception ignored) {
                    // Ignored
                }
            }
        }

        return ixPackageEntity;
    }

    /*public final ResultSet<TargetingSegmentEntity> getMatchingTargetingSegments(final Query query) {
        return packageRepositoryV2.getIndexedTargetingSegments().retrieve(query);
    }*/

    public final Optional<TargetingSegmentEntity> getTargetingSegmentEntityById(final Long id) {
        Optional<TargetingSegmentEntity> tse = Optional.empty();

        if (null != id) {
            final Query<TargetingSegmentEntity> query = equal(TARGETING_SEGMENT_ID, id);
            final ResultSet<TargetingSegmentEntity> rs = packageRepositoryV2.getIndexedTargetingSegments().retrieve(query);
            if (rs.isNotEmpty()) {
                try {
                    tse = Optional.of(rs.uniqueResult());
                } catch (final Exception ignored) {
                    // Ignored
                }
            }
        }

        return tse;
    }

    public Optional<DealEntity> queryDealById(final String dealId, final boolean checkInOldRepo) {
        Optional<DealEntity> dealEntity = Optional.empty();

        if (StringUtils.isNotBlank(dealId)) {
            final Query<DealEntity> query = equal(DEAL_ID, dealId);
            final ResultSet<DealEntity> rs = packageRepositoryV2.getIndexedDeals().retrieve(query);
            if (rs.isNotEmpty()) {
                try {
                    dealEntity = Optional.of(rs.uniqueResult());
                } catch (final Exception ignored) {
                    // Ignored
                }
            } else if (checkInOldRepo) {
                final ResultSet<DealEntity> rsFromOld = ixPackageRepository.getIndexedDeals().retrieve(query);
                if (rsFromOld.isNotEmpty()) {
                    try {
                        dealEntity = Optional.of(rsFromOld.uniqueResult());
                    } catch (final Exception ignored) {
                        // Ignored
                    }
                }
            }
        }

        return dealEntity;
    }

    public ResultSet<PackageEntity> queryPackagesByTargetingSegmentIds(final Set<Long> targetingSegments) {

        final Query<PackageEntity> query = CollectionUtils.isNotEmpty(targetingSegments) ?
                in(TARGETING_SEGMENT_IDS, targetingSegments) : NONE;

        return packageRepositoryV2.getIndexedPackages().retrieve(query);
    }

    public Set<Long> queryTargetingSegmentsUsedByPackage(final Integer packageId, final Set<Long> targetingSegments) {
        Set<Long> targetingSegmentsUsed = new HashSet<>();

        if (null != packageId && CollectionUtils.isNotEmpty(targetingSegments)) {
            final Query<PackageEntity> query = equal(PackageAttributes.PACKAGE_ID, packageId);
            final ResultSet<PackageEntity> rs = packageRepositoryV2.getIndexedPackages().retrieve(query);
            if (rs.isNotEmpty()) {
                try {
                    final PackageEntity pe = rs.uniqueResult();
                    targetingSegmentsUsed = Sets.intersection(pe.getTargetingSegmentIds(), targetingSegments);
                } catch (final Exception ignored) {
                    // Ignored
                }
            }
        }

        return targetingSegmentsUsed;
    }

    public final Set<DealEntity> queryDealsByPackageIds(final Set<Integer> packageIds, final DemandSourceType dst,
            final String dsp) {
        final Set<DealEntity> deals = new HashSet<>();

        if (CollectionUtils.isNotEmpty(packageIds)) {
            final Query<DealEntity> query = and(in(PACKAGE_ID, packageIds), equal(DST_ID, dst), equal(DSP_ID, dsp));
            final ResultSet<DealEntity> rs = packageRepositoryV2.getIndexedDeals().retrieve(query);

            for (final DealEntity de : rs) {
                deals.add(de);
            }
        }

        return deals;
    }

    public ResultSet<DealEntity> lazilyFindMatchingDeals(final Set<Integer> packageId, final DemandSourceType dst,
            final String dsp) {
        final Query<DealEntity> query = and(in(PACKAGE_ID, packageId), equal(DST_ID, dst), equal(DSP_ID, dsp));
        return packageRepositoryV2.getIndexedDeals().retrieve(query);
    }

}
