package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.adpool.RequestedAdType.BANNER;
import static com.inmobi.adserve.adpool.RequestedAdType.INTERSTITIAL;
import static com.inmobi.adserve.channels.adnetworks.ix.TargetingSegmentFiltersV2.checkForAdTypeTargeting;
import static com.inmobi.adserve.channels.adnetworks.ix.TargetingSegmentFiltersV2.checkForSDKVersionTargeting;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_AD_TYPE_TARGETING_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_DMP_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_GEO_REGION_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_LANGUAGE_TARGETING_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_MANUF_MODEL_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_OS_VERSION_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_SDK_VERSION_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_SEGMENT_SUBSET_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.IX_PACKAGE_MATCH_LATENCY;
import static com.inmobi.adserve.channels.util.InspectorStrings.PACKAGE_FILTER_STATS;
import static com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints.REWARDED_VAST_VIDEO;
import static com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints.STATIC;
import static com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints.VAST_VIDEO;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.google.common.collect.ImmutableSortedSet;
import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.GeoRegionFenceMapEntity;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.segment.Segment;
import com.inmobi.segment.impl.CarrierId;
import com.inmobi.segment.impl.City;
import com.inmobi.segment.impl.ConnectionType;
import com.inmobi.segment.impl.ConnectionTypeEnum;
import com.inmobi.segment.impl.Country;
import com.inmobi.segment.impl.DeviceOs;
import com.inmobi.segment.impl.GeoSourceType;
import com.inmobi.segment.impl.GeoSourceTypeEnum;
import com.inmobi.segment.impl.InventoryType;
import com.inmobi.segment.impl.InventoryTypeEnum;
import com.inmobi.segment.impl.LatlongPresent;
import com.inmobi.segment.impl.SiteCategory;
import com.inmobi.segment.impl.SiteId;
import com.inmobi.segment.impl.SlotId;
import com.inmobi.segment.impl.UidPresent;
import com.inmobi.segment.impl.ZipCodePresent;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@NoArgsConstructor(access = PRIVATE)
class IXPackageMatcher {

    static final Set<Integer> BANNER_ALLOWED_SDKS_FOR_VIEWABILITY =
            ImmutableSortedSet.of(300, 364, 365, 370, 371, 381, 400, 402, 403, 404, 410, 411, 430, 440, 441, 442, 443,
                    451, 452, 453, 454, 455);
    static final Set<Integer> INTERSTITIAL_ALLOWED_SDKS_FOR_VIEWABILITY =
            ImmutableSortedSet.of(442, 443, 452, 454, 456);
    static final Set<Integer> VAST_ALLOWED_SDKS_FOR_VIEWABILITY =
            ImmutableSortedSet.of(402, 403, 404, 410, 411, 430, 440, 441, 442, 443, 450, 451, 453, 456);

    static Map<Integer, Boolean> findMatchingPackageIds(final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final Short selectedSlotId,
            final ChannelSegmentEntity adGroupEntity) {
        log.debug("Finding matching packages (V1)");
        final Map<Integer, Boolean> matchedPackages = new HashMap<>();

        // TODO: Do package matching on the intersection of ump selected slots and those present in the adgroup
        final Segment requestSegment = createRequestSegment(sasParams, selectedSlotId);

        final long startTime = System.currentTimeMillis();
        final ResultSet<IXPackageEntity> resultSet = repositoryHelper.queryIXPackageRepository(sasParams.getOsId(),
                sasParams.getSiteId(), sasParams.getCountryId().intValue(), selectedSlotId);

        final long latency = System.currentTimeMillis() - startTime;
        log.debug("Packages (V1) match latency: {}", latency);
        InspectorStats.updateYammerTimerStats(sasParams.getDemandSourceType().name(), IX_PACKAGE_MATCH_LATENCY, latency);

        if (log.isDebugEnabled() && null != resultSet) {
            log.debug("Number of packages (V1) selected after OS({}), Site({}), Country({}) and Slot({}) filtration: {}",
                    sasParams.getOsId(), sasParams.getSiteId(), sasParams.getCountryId().intValue(), selectedSlotId,
                    resultSet.size());
        }

        int droppedInPackageDMPFilter = 0;
        int droppedInPackageManufModelFilter = 0;
        int droppedInPackageOsVersionFilter = 0;
        int droppedInPackageGeoRegionFilter = 0;
        int droppedInPackageSegmentSubsetFilter = 0;
        int droppedInPackageAdTypeTargetingFilter = 0;
        int droppedInPackageLanguageTargetingFilter = 0;
        int droppedInSdkVersionFilter = 0;


        // TODO: Refactor into proper filters
        if (null != resultSet) {
            for (final IXPackageEntity packageEntity : resultSet) {
                if (requestSegment.isSubsetOf(packageEntity.getSegment())) {
                    final boolean failsAdTypeTargetingFilter =
                            !checkForAdTypeTargeting(adGroupEntity.getSecondaryAdFormatConstraints(), packageEntity.getSecondaryAdFormatConstraints());

                    if (failsAdTypeTargetingFilter) {
                        log.debug("Package {} dropped in Ad Type Targeting Filter", packageEntity.getId());
                        droppedInPackageAdTypeTargetingFilter += 1;
                        continue;
                    }

                    final boolean failedInLanguageTargetingFilter = !checkForLanguageTargeting(sasParams.getLanguage(), packageEntity);
                    if (failedInLanguageTargetingFilter) {
                        log.debug("Package {} dropped in Language Targeting Filter", packageEntity.getId());
                        droppedInPackageLanguageTargetingFilter += 1;
                        continue;
                    }

                    // Add to matchedPackageIds only if csId's match
                    if (CollectionUtils.isNotEmpty(packageEntity.getDmpFilterSegmentExpression()) && !checkForCsidMatch(sasParams.getCsiTags(), packageEntity
                            .getDmpFilterSegmentExpression())) {
                        log.debug("Package {} dropped in DMP Filter", packageEntity.getId());
                        droppedInPackageDMPFilter += 1;
                        continue;
                    }

                    // Manuf Model Targeting
                    if (!TargetingSegmentFiltersV2.checkForManufModelTargeting(packageEntity.getManufModelTargeting(), sasParams
                            .getManufacturerId(), sasParams.getModelId())) {
                        log.debug("Package {} dropped in Manufacturer Model Filter", packageEntity.getId());
                        droppedInPackageManufModelFilter += 1;
                        continue;
                    }

                    // OS Version Targeting
                    if (!TargetingSegmentFiltersV2.checkForOsVersionTargeting(packageEntity.getOsVersionTargeting(), sasParams
                            .getOsId(), sasParams.getOsMajorVersion())) {
                        log.debug("Package {} dropped in OS Version Filter", packageEntity.getId());
                        droppedInPackageOsVersionFilter += 1;
                        continue;
                    }

                    Set<Integer> viewabilityInclusionList = null;
                    final SecondaryAdFormatConstraints adgroupSecondaryAdFormat = adGroupEntity.getSecondaryAdFormatConstraints();
                    if (STATIC == adgroupSecondaryAdFormat) {
                        if (BANNER == sasParams.getRequestedAdType()) {
                            viewabilityInclusionList = BANNER_ALLOWED_SDKS_FOR_VIEWABILITY;
                        } else if (INTERSTITIAL == sasParams.getRequestedAdType()) {
                            viewabilityInclusionList = INTERSTITIAL_ALLOWED_SDKS_FOR_VIEWABILITY;
                        }
                    } else if (VAST_VIDEO == adgroupSecondaryAdFormat || REWARDED_VAST_VIDEO == adgroupSecondaryAdFormat) {
                        viewabilityInclusionList = VAST_ALLOWED_SDKS_FOR_VIEWABILITY;
                    }

                    if (!checkForSDKVersionTargeting(sasParams.getSdkVersion(), packageEntity.getSdkVersionTargeting(), packageEntity
                            .isViewable(), new ImmutablePair<>(false, viewabilityInclusionList))) {
                        log.debug("Package {} dropped in SDK Version Filter", packageEntity.getId());
                        droppedInSdkVersionFilter += 1;
                        continue;
                    }
                    // Add to matchedPackageIds only if at least one fence from the set of request fence ids
                    // is present in the set of fence ids defined in the package.
                    // But if the package is selected because of its geoCookieTargeting do not drop it.
                    boolean servedByGeoCookie = false;
                    if (StringUtils.isNotEmpty(packageEntity.getGeoFenceRegion())) {
                        final String geoRegionNameCountryCombo = packageEntity.getGeoFenceRegion() + '_' + sasParams.getCountryId();
                        final GeoRegionFenceMapEntity geoRegionFenceMapEntity =
                                repositoryHelper.queryGeoRegionFenceMapRepositoryByRegionNameCountryCombo(geoRegionNameCountryCombo);

                        if (null == geoRegionFenceMapEntity || null != geoRegionFenceMapEntity.getFenceIdsList() && (
                                null == sasParams.getGeoFenceIds() || !CollectionUtils
                                        .containsAny(sasParams.getGeoFenceIds(), geoRegionFenceMapEntity.getFenceIdsList()))) {
                            log.debug("Package {} does not match geo Fences, check if it geocookieTargeted", packageEntity
                                    .getId());
                            if (packageEntity.getGeocookieId() != null && sasParams.getCsiTags() != null && sasParams.getCsiTags().contains(packageEntity.getGeocookieId())) {
                                servedByGeoCookie = true;
                            } else {
                                log.debug("Package {} with geocookie {} dropped in Geo Fence Filter", packageEntity
                                        .getId(), packageEntity.getGeocookieId());
                                droppedInPackageGeoRegionFilter += 1;
                                continue;
                            }
                        }
                    }

                    matchedPackages.put(packageEntity.getId(), servedByGeoCookie);
                } else {
                    log.debug("Package {} dropped in Segment Subset Filter", packageEntity.getId());
                    droppedInPackageSegmentSubsetFilter += 1;
                }
            }
        }

        incrementPackageFilterStat(DROPPED_IN_PACKAGE_AD_TYPE_TARGETING_FILTER, droppedInPackageAdTypeTargetingFilter);
        incrementPackageFilterStat(DROPPED_IN_PACKAGE_DMP_FILTER, droppedInPackageDMPFilter);
        incrementPackageFilterStat(DROPPED_IN_PACKAGE_MANUF_MODEL_FILTER, droppedInPackageManufModelFilter);
        incrementPackageFilterStat(DROPPED_IN_PACKAGE_OS_VERSION_FILTER, droppedInPackageOsVersionFilter);
        incrementPackageFilterStat(DROPPED_IN_PACKAGE_GEO_REGION_FILTER, droppedInPackageGeoRegionFilter);
        incrementPackageFilterStat(DROPPED_IN_PACKAGE_SEGMENT_SUBSET_FILTER, droppedInPackageSegmentSubsetFilter);
        incrementPackageFilterStat(DROPPED_IN_PACKAGE_LANGUAGE_TARGETING_FILTER,
                droppedInPackageLanguageTargetingFilter);
        incrementPackageFilterStat(DROPPED_IN_PACKAGE_SDK_VERSION_FILTER, droppedInSdkVersionFilter);

        if (log.isDebugEnabled()) {
            log.debug("Packages selected: {}", Arrays.toString(matchedPackages.keySet().toArray()));
        }
        return matchedPackages;
    }

    private static void incrementPackageFilterStat(final String dropInFilterStat, final int increment) {
        InspectorStats.incrementStatCount(PACKAGE_FILTER_STATS, dropInFilterStat, increment);
    }

    private static boolean checkForLanguageTargeting(final String reqLanguage, final IXPackageEntity packageEntity) {
        final Set<String> languageTargetingSet = packageEntity.getLanguageTargetingSet();
        return null == languageTargetingSet
                ? true
                : languageTargetingSet.isEmpty() ? true : languageTargetingSet.contains(reqLanguage);
    }

    public static boolean checkForCsidMatch(final Set<Integer> csiReqTags, final Set<Set<Integer>> dmpFilterExpression) {
        if (CollectionUtils.isEmpty(csiReqTags)) {
            return false;
        } else {
            for (final Set<Integer> smallSet : dmpFilterExpression) {
                if (Collections.disjoint(smallSet, csiReqTags)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Segment createRequestSegment(final SASRequestParameters sasParams, final Short selectedSlotId) {
        final Country reqCountry = new Country();
        final DeviceOs reqDeviceOs = new DeviceOs();
        final SiteId reqSiteId = new SiteId();
        final SlotId reqSlotId = new SlotId();
        final CarrierId reqCarrierId = new CarrierId();
        final LatlongPresent reqLatlongPresent = new LatlongPresent();
        final ZipCodePresent reqZipCodePresent = new ZipCodePresent();
        final UidPresent reqUidPresent = new UidPresent();
        final InventoryType reqInventoryType = new InventoryType();

        // Below are optional params and requires NULL check.
        ConnectionType reqConnectionType = null;
        SiteCategory reqSiteCategory = null;
        City reqCity = null;
        GeoSourceType reqGeoSource = null;

        reqCountry.init(Collections.singleton(sasParams.getCountryId().intValue()));
        reqDeviceOs.init(Collections.singleton(sasParams.getOsId()));
        reqSiteId.init(Collections.singleton(sasParams.getSiteId()));
        reqSlotId.init(Collections.singleton(selectedSlotId.intValue()));
        reqLatlongPresent.init(StringUtils.isNotEmpty(sasParams.getLatLong()));
        reqZipCodePresent.init(StringUtils.isNotEmpty(sasParams.getPostalCode()));
        reqUidPresent.init(isUdIdPresent(sasParams));
        reqCarrierId.init(Collections.singleton((long) sasParams.getCarrierId())); // TODO: fix long->int cast in
        // ThriftRequestParser

        final InventoryTypeEnum reqInventoryEnum = GlobalConstant.APP.equalsIgnoreCase(sasParams.getSource())
                ? InventoryTypeEnum.APP
                : InventoryTypeEnum.BROWSER;
        reqInventoryType.init(Collections.singleton(reqInventoryEnum));

        if (null != sasParams.getConnectionType()) {
            reqConnectionType = new ConnectionType();
            final ConnectionTypeEnum connectionTypeEnum =
                    ConnectionTypeEnum.valueOf(sasParams.getConnectionType().name());
            reqConnectionType.init(Collections.singleton(connectionTypeEnum));
        }

        if (sasParams.getSiteContentType() != null) {
            reqSiteCategory = new SiteCategory();
            reqSiteCategory.init(sasParams.getSiteContentType().name());
        }

        if (sasParams.getCity() != null) {
            reqCity = new City();
            reqCity.init(Collections.singleton(sasParams.getCity()));
        }

        if (sasParams.getLocationSource() != null) {
            reqGeoSource = new GeoSourceType();
            final GeoSourceTypeEnum geoSourceTypeEnum = GeoSourceTypeEnum.valueOf(sasParams.getLocationSource().name());
            reqGeoSource.init(Collections.singleton(geoSourceTypeEnum));
        }

        final Segment.Builder requestSegmentBuilder = new Segment.Builder();
        requestSegmentBuilder.addSegmentParameter(reqCountry).addSegmentParameter(reqDeviceOs)
                .addSegmentParameter(reqSiteId).addSegmentParameter(reqSlotId).addSegmentParameter(reqLatlongPresent)
                .addSegmentParameter(reqZipCodePresent).addSegmentParameter(reqCarrierId)
                .addSegmentParameter(reqUidPresent).addSegmentParameter(reqInventoryType);

        if (reqConnectionType != null) {
            requestSegmentBuilder.addSegmentParameter(reqConnectionType);
        }
        if (reqSiteCategory != null) {
            requestSegmentBuilder.addSegmentParameter(reqSiteCategory);
        }
        if (reqCity != null) {
            requestSegmentBuilder.addSegmentParameter(reqCity);
        }
        if (reqGeoSource != null) {
            requestSegmentBuilder.addSegmentParameter(reqGeoSource);
        }

        final Segment requestSegment = requestSegmentBuilder.build();

        return requestSegment;
    }

    private static boolean isUdIdPresent(final SASRequestParameters sasParams) {
        return  !(null == sasParams.getTUidParams() || sasParams.getTUidParams().isEmpty());
    }
}
