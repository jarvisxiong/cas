package com.inmobi.adserve.channels.repository.pmp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.inmobi.adserve.channels.repository.pmp.DealAttributes.DEAL_ID;
import static com.inmobi.adserve.channels.repository.pmp.DealAttributes.DSP_ID;
import static com.inmobi.adserve.channels.repository.pmp.DealAttributes.DST_ID;
import static com.inmobi.adserve.channels.repository.pmp.PackageAttributes.PACKAGE_ID;
import static com.inmobi.adserve.channels.repository.pmp.PackageAttributes.TARGETING_SEGMENT_IDS;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractCountryCitiesTargeting;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractDmpFilterExpression;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractManufModelTargeting;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractOsVersionTargeting;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractSdkVersionTargeting;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.getSet;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.getThirdPartyTrackerMap;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.COUNTRY_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.DSP_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.DST_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_AD_FORMATS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_CONNECTION_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_INTEGRATION_METHODS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_INVENTORY_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_LOCATION_SOURCES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_SITE_CONTENT_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.EXC_SLOTS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_AD_FORMATS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_CONNECTION_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_INTEGRATION_METHODS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_INVENTORY_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_LOCATION_SOURCES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_SITE_CONTENT_TYPES;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.INC_SLOTS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.OS_IDS;
import static com.inmobi.adserve.channels.repository.pmp.TargetingSegmentAttributes.TARGETING_SEGMENT_ID;
import static com.inmobi.adserve.channels.util.InspectorStrings.BADLY_CONFIGURED_DEAL;
import static com.inmobi.adserve.channels.util.InspectorStrings.BADLY_CONFIGURED_TARGETING_SEGMENT;
import static com.inmobi.adserve.channels.util.InspectorStrings.OVERALL_PMP_ERROR_STATS;
import static com.inmobi.casthrift.DemandSourceType.IX;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.googlecode.cqengine.CQEngine;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.compound.CompoundIndex;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.entity.pmp.PackageEntity;
import com.inmobi.adserve.channels.entity.pmp.TargetingSegmentEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.demand.enums.AuctionType;
import com.inmobi.adserve.channels.util.demand.enums.DealType;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.data.repository.DBReaderDelegate;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;

import lombok.Getter;


public class PackageRepositoryV2 extends AbstractCQEngineRepository {

    @Getter
    private IndexedCollection<TargetingSegmentEntity> indexedTargetingSegments;
    @Getter
    private IndexedCollection<PackageEntity> indexedPackages;
    @Getter
    private IndexedCollection<DealEntity> indexedDeals;

    public void init(final Logger logger, final DataSource dataSource, final Configuration config,
            final String instanceName) {

        // TODO: Expose these + IXPackageRepo metrics to repoStats/graphite
        super.init(logger, dataSource, config, instanceName, new PackageReaderDelegateV2());

        // Creating CQ Engine Collections
        indexedTargetingSegments = CQEngine.newInstance();
        indexedPackages = CQEngine.newInstance();
        indexedDeals = CQEngine.newInstance();

        // Indexes
        indexedTargetingSegments.addIndex(CompoundIndex.onAttributes(DST_IDS, DSP_IDS, INC_INVENTORY_TYPES, EXC_INVENTORY_TYPES, INC_SITE_CONTENT_TYPES, EXC_SITE_CONTENT_TYPES));
        indexedTargetingSegments.addIndex(CompoundIndex.onAttributes(DST_IDS, DSP_IDS, INC_CONNECTION_TYPES, EXC_CONNECTION_TYPES, INC_LOCATION_SOURCES, EXC_LOCATION_SOURCES));
        indexedTargetingSegments.addIndex(CompoundIndex.onAttributes(DST_IDS, DSP_IDS, INC_INTEGRATION_METHODS, EXC_INTEGRATION_METHODS));
        indexedTargetingSegments.addIndex(CompoundIndex.onAttributes(DST_IDS, DSP_IDS, INC_AD_FORMATS, INC_SLOTS));
        indexedTargetingSegments.addIndex(CompoundIndex.onAttributes(DST_IDS, DSP_IDS, EXC_AD_FORMATS, EXC_SLOTS));
        indexedTargetingSegments.addIndex(CompoundIndex.onAttributes(DST_IDS, DSP_IDS, COUNTRY_IDS, OS_IDS));
        indexedTargetingSegments.addIndex(UniqueIndex.onAttribute(TARGETING_SEGMENT_ID));

        indexedPackages.addIndex(UniqueIndex.onAttribute(PACKAGE_ID));
        indexedPackages.addIndex(HashIndex.onAttribute(TARGETING_SEGMENT_IDS));

        indexedDeals.addIndex(UniqueIndex.onAttribute(DEAL_ID));
        indexedDeals.addIndex(CompoundIndex.onAttributes(DealAttributes.PACKAGE_ID, DST_ID, DSP_ID));



        /*metricsRegistry.register(MetricRegistry.name(this.getClass(),
                "Packages-size"), (Gauge<Integer>) () -> indexedTargetingSegments.size());
        metricsRegistry.register(MetricRegistry.name(this.getClass(),
                "Targeting-segments-size"), (Gauge<Integer>) () -> indexedTargetingSegments.size());
        metricsRegistry.register(MetricRegistry.name(this.getClass(),
                "Deals-size"), (Gauge<Integer>) () -> indexedDeals.size());*/

        start();
    }

    private class PackageReaderDelegateV2 implements DBReaderDelegate {
        private Map<Long, TargetingSegmentEntity> newTargetingSegmentMap;
        private Set<PackageEntity> newPackageSet;
        private Set<DealEntity> newDealSet;

        @Override
        public void beforeEachIteration() {
            newTargetingSegmentMap = new HashMap<>();
            newPackageSet = new HashSet<>();
            newDealSet = new HashSet<>();
        }

        @Override
        public Timestamp readRow(final ResultSet rs) throws SQLException {
            try {
                final int packageId = rs.getInt("package_id");

                // Deals
                final String[] dealIds = (String[]) rs.getArray("deal_ids").getArray();
                final Double[] dealFloors = (Double[]) rs.getArray("deal_floors").getArray();
                final String[] dealCurs = (String[]) rs.getArray("deal_floor_curs").getArray();
                final String[] dealTypes = (String[]) rs.getArray("deal_types").getArray();
                final String[] dealAuctionTypes = (String[]) rs.getArray("deal_auction_types").getArray();
                final Integer[] dealDsts = (Integer[]) rs.getArray("deal_dsts").getArray();
                final String[] dealDspAccountGuids = (String[]) rs.getArray("deal_dsp_account_ids").getArray();
                final String[] dealThirdPartyTrackerJsons = (String[]) rs.getArray("deal_third_party_tracker_jsons").getArray();
                final Boolean[] dealBillOnViewabilityFlags = (Boolean[]) rs.getArray("deal_bill_on_viewability_flags").getArray();
                final Double[] dealAgencyRebatePercentages = (Double[]) rs.getArray("deal_agency_rebate_percentages").getArray();
                final Integer[] dealExternalAgencyIds = (Integer[]) rs.getArray("deal_external_agency_ids").getArray();

                final Set<DealEntity> deals = new HashSet<>();
                for (int i = 0; i < dealIds.length; ++i) {
                    try {
                        final DealEntity deal = DealEntity.newBuilder()
                                .id(dealIds[i])
                                .floor(dealFloors[i])
                                .currency(dealCurs[i])
                                .dealType(DealType.getDealTypeByName(dealTypes[i]))
                                .auctionType(AuctionType.getAuctionTypeByName(dealAuctionTypes[i]))
                                .dst(null != dealDsts[i] ? DemandSourceType.findByValue(dealDsts[i]) : null)
                                .dsp(dealDspAccountGuids[i])
                                .toBeBilledOnViewability(dealBillOnViewabilityFlags[i])
                                .agencyRebatePercentage(dealAgencyRebatePercentages[i])
                                .externalAgencyId(dealExternalAgencyIds[i])
                                .thirdPartyTrackersMap(getThirdPartyTrackerMap(dealThirdPartyTrackerJsons[i]))
                                .packageId(packageId)
                                .build();

                        checkNotNull(deal.getDst(), "Demand Source Type is missing for deal: %s", deal.getId());
                        checkArgument(IX == deal.getDst() || isNotBlank(deal.getDsp()), "No DSP attached to deal: %s", deal.getId());
                        checkArgument(deal.getFloor() >= 0, "Deal Floor is negative for deal: %s", deal.getId());

                        if (null != deal.getAgencyRebatePercentage()) {
                            checkArgument(deal.getAgencyRebatePercentage() > 0, "Agency rebate cannot be negative. Deal: %s", deal.getId());
                            checkArgument(deal.getAgencyRebatePercentage() <= 100, "Agency rebate cannot be greater than 100. Deal %s", deal.getId());
                            checkArgument(null != deal.getExternalAgencyId(), "Agency missing for agency rebate deal: %s", deal.getId());
                        }

                        deals.add(deal);
                    } catch (final Exception e) {
                        logger.error("Ignoring erroneously configured deal: " + dealIds[i] + ".\n" , e);
                        InspectorStats.incrementStatCount(OVERALL_PMP_ERROR_STATS, BADLY_CONFIGURED_DEAL + dealIds[i]);
                    }
                }

                // Targeting Segments
                final Long[] targetSegments = (Long[]) rs.getArray("targeting_segment_ids").getArray();

                final Boolean[] tsInventoryTypesInclusionFlags = (Boolean[]) rs.getArray("ts_inventory_types_list_is_inclusion").getArray();
                final Boolean[] tsSiteContentTypesInclusionFlags = (Boolean[]) rs.getArray("ts_site_content_types_list_is_inclusion").getArray();
                final Boolean[] tsConnectionTypesInclusionFlags = (Boolean[]) rs.getArray("ts_connection_types_list_is_inclusion").getArray();
                final Boolean[] tsLocationSourcesInclusionFlags = (Boolean[]) rs.getArray("ts_location_sources_list_is_inclusion").getArray();
                final Boolean[] tsIntegrationMethodsInclusionFlags = (Boolean[]) rs.getArray("ts_integration_methods_list_is_inclusion").getArray();
                final Boolean[] tsPublisherListInclusionFlags = (Boolean[]) rs.getArray("ts_publisher_list_is_inclusion").getArray();
                final Boolean[] tsSiteListInclusionFlags = (Boolean[]) rs.getArray("ts_site_list_is_inclusion").getArray();
                final Boolean[] tsCarrierListInclusionFlags = (Boolean[]) rs.getArray("ts_carrier_list_is_inclusion").getArray();
                final Boolean[] tsLanguageListInclusionFlags = (Boolean[]) rs.getArray("ts_language_list_is_inclusion").getArray();
                final Boolean[] tsGeoRegionInclusionFlags = (Boolean[]) rs.getArray("ts_geo_region_is_inclusion").getArray();
                final Boolean[] tsSlotsListInclusionFlags = (Boolean[]) rs.getArray("ts_slots_list_is_inclusion").getArray();
                final Boolean[] tsAdTypesListInclusionFlag = (Boolean[]) rs.getArray("ts_ad_types_list_is_inclusion").getArray();

                final String[] tsInventoryTypes = (String[]) rs.getArray("ts_inventory_types").getArray();
                final String[] tsSiteContentTypes = (String[]) rs.getArray("ts_site_content_types").getArray();
                final String[] tsConnectionTypes = (String[]) rs.getArray("ts_connection_types").getArray();
                final String[] tsLocationSources = (String[]) rs.getArray("ts_location_sources").getArray();
                final String[] tsIntegrationMethods = (String[]) rs.getArray("ts_integration_methods").getArray();
                final String[] tsPublishers = (String[]) rs.getArray("ts_publishers").getArray();
                final String[] tsSites = (String[]) rs.getArray("ts_sites").getArray();
                final String[] tsCarriers = (String[]) rs.getArray("ts_carriers").getArray();
                final String[] tsLanguages = (String[]) rs.getArray("ts_languages").getArray();
                final String[] tsGeoFenceRegions = (String[]) rs.getArray("ts_geo_fence_region").getArray();
                final String[] tsSlots = (String[]) rs.getArray("ts_slots").getArray();
                final String[] tsAdTypes = (String[]) rs.getArray("ts_ad_types").getArray();
                final String[] tsCsidFilterExpressions = (String[]) rs.getArray("ts_csid_filter_expression").getArray();
                final Double[] tsDataVendorCosts = (Double[]) rs.getArray("ts_data_vendor_cost").getArray();

                final String[] tsSdkVersionJsons = (String[]) rs.getArray("ts_sdk_version_json").getArray();
                final String[] tsOsVersionJsons = (String[]) rs.getArray("ts_os_version_json").getArray();
                final String[] tsCountryCityJsons = (String[]) rs.getArray("ts_country_city_json").getArray();
                final String[] tsManufModelJsons = (String[]) rs.getArray("ts_manuf_model_json").getArray();

                final Map<Long, TargetingSegmentEntity> targetingSegmentMap = new HashMap<>();
                int targetingSegmentsReused = 0;
                for (int i = 0; i < targetSegments.length; ++i) {
                    try {
                        final Long targetingSegmentId = targetSegments[i];

                        if (newTargetingSegmentMap.containsKey(targetingSegmentId)) {
                            ++targetingSegmentsReused;
                            final TargetingSegmentEntity tse = newTargetingSegmentMap.get(targetingSegmentId);
                            tse.getDsts().addAll(deals.stream().map(DealEntity::getDst).collect(Collectors.toSet()));
                            tse.getDsps().addAll(deals.stream().map(DealEntity::getDsp).collect(Collectors.toSet()));
                            targetingSegmentMap.put(targetingSegmentId, tse);
                        } else {
                            final TargetingSegmentEntity.Builder tsBuilder = TargetingSegmentEntity.newBuilder();
                            tsBuilder.id(targetingSegmentId);

                            {
                                final Set<String> inventoryTypeStrings = getSet(tsInventoryTypes[i]);
                                if (CollectionUtils.isNotEmpty(inventoryTypeStrings)) {
                                    final Set<InventoryType> inventoryTypes = ImmutableSet
                                            .copyOf(inventoryTypeStrings.stream().map(InventoryType::valueOf).iterator());
                                    if (tsInventoryTypesInclusionFlags[i]) {
                                        tsBuilder.includedInventoryTypes(inventoryTypes);
                                    } else {
                                        tsBuilder.excludedInventoryTypes(inventoryTypes);
                                    }
                                }
                            }

                            {
                                final Set<String> siteContentTypeStrings = getSet(tsSiteContentTypes[i]);
                                if (CollectionUtils.isNotEmpty(siteContentTypeStrings)) {
                                    final Set<ContentType> siteContentTypes = ImmutableSet
                                            .copyOf(siteContentTypeStrings.stream().map(ContentType::valueOf).iterator());
                                    if (tsSiteContentTypesInclusionFlags[i]) {
                                        tsBuilder.includedSiteContentTypes(siteContentTypes);
                                    } else {
                                        tsBuilder.excludedSiteContentTypes(siteContentTypes);
                                    }
                                }
                            }

                            {
                                final Set<String> connectionTypeStrings = getSet(tsConnectionTypes[i]);
                                if (CollectionUtils.isNotEmpty(connectionTypeStrings)) {
                                    final Set<ConnectionType> connectionTypes = ImmutableSet
                                            .copyOf(connectionTypeStrings.stream().map(ConnectionType::valueOf).iterator());
                                    if (tsConnectionTypesInclusionFlags[i]) {
                                        tsBuilder.includedConnectionTypes(connectionTypes);
                                    } else {
                                        tsBuilder.excludedConnectionTypes(connectionTypes);
                                    }
                                }
                            }

                            {
                                final Set<String> locationSourceStrings = getSet(tsLocationSources[i]);
                                if (CollectionUtils.isNotEmpty(locationSourceStrings)) {
                                    final Set<LocationSource> locationSourceTypes = ImmutableSet
                                            .copyOf(locationSourceStrings.stream().map(LocationSource::valueOf).iterator());
                                    if (tsLocationSourcesInclusionFlags[i]) {
                                        tsBuilder.includedLocationSources(locationSourceTypes);
                                    } else {
                                        tsBuilder.excludedLocationSources(locationSourceTypes);
                                    }
                                }
                            }

                            {
                                final Set<String> integrationMethodStrings = getSet(tsIntegrationMethods[i]);
                                if (CollectionUtils.isNotEmpty(integrationMethodStrings)) {
                                    final Set<IntegrationMethod> integrationMethods = ImmutableSet
                                            .copyOf(integrationMethodStrings.stream().map(IntegrationMethod::valueOf).iterator());
                                    if (tsIntegrationMethodsInclusionFlags[i]) {
                                        tsBuilder.includedIntegrationMethod(integrationMethods);
                                    } else {
                                        tsBuilder.excludedIntegrationMethod(integrationMethods);
                                    }
                                }
                            }

                            {
                                Set<String> publishers = getSet(tsPublishers[i]);
                                if (CollectionUtils.isNotEmpty(publishers)) {
                                    publishers = ImmutableSet.copyOf(publishers);
                                    if (tsPublisherListInclusionFlags[i]) {
                                        tsBuilder.includedPublishers(publishers);
                                    } else {
                                        tsBuilder.excludedPublishers(publishers);
                                    }
                                }
                            }

                            {
                                Set<String> sites = getSet(tsSites[i]);
                                if (CollectionUtils.isNotEmpty(sites)) {
                                    sites = ImmutableSet.copyOf(sites);
                                    if (tsSiteListInclusionFlags[i]) {
                                        tsBuilder.includedSites(sites);
                                    } else {
                                        tsBuilder.excludedSites(sites);
                                    }
                                }
                            }

                            {
                                Set<Long> carriers = getSet(tsCarriers[i]);
                                if (CollectionUtils.isNotEmpty(carriers)) {
                                    carriers = ImmutableSet.copyOf(carriers);
                                    if (tsCarrierListInclusionFlags[i]) {
                                        tsBuilder.includedCarriers(carriers);
                                    } else {
                                        tsBuilder.excludedCarriers(carriers);
                                    }
                                }
                            }

                            {
                                Set<String> languages = getSet(tsLanguages[i]);
                                if (CollectionUtils.isNotEmpty(languages)) {
                                    languages = ImmutableSet.copyOf(languages);
                                    if (tsLanguageListInclusionFlags[i]) {
                                        tsBuilder.includedLanguages(languages);
                                    } else {
                                        tsBuilder.excludedLanguages(languages);
                                    }
                                }
                            }

                            {
                                final String geoFenceRegion = tsGeoFenceRegions[i];
                                if (StringUtils.isNotBlank(geoFenceRegion)) {
                                    if (tsGeoRegionInclusionFlags[i]) {
                                        tsBuilder.includedGeoCustomRegion(geoFenceRegion);
                                    } else {
                                        tsBuilder.excludedGeoCustomRegion(geoFenceRegion);
                                    }
                                }
                            }

                            {
                                Set<Short> slots = getSet(tsSlots[i]);
                                if (CollectionUtils.isNotEmpty(slots)) {
                                    slots = ImmutableSet.copyOf(slots);
                                    if (tsSlotsListInclusionFlags[i]) {
                                        tsBuilder.includedSlots(slots);
                                    } else {
                                        tsBuilder.excludedSlots(slots);
                                    }
                                }
                            }

                            {
                                Set<Integer> adTypeStrings = getSet(tsAdTypes[i]);
                                if (CollectionUtils.isNotEmpty(adTypeStrings)) {
                                    adTypeStrings = ImmutableSet.copyOf(adTypeStrings);
                                    final Set<SecondaryAdFormatConstraints> adTypes = ImmutableSet
                                            .copyOf(adTypeStrings.stream().map(SecondaryAdFormatConstraints::getDemandAdFormatConstraintsByValue)
                                                    .iterator());
                                    if (tsAdTypesListInclusionFlag[i]) {
                                        tsBuilder.includedAdTypes(adTypes);
                                    } else {
                                        tsBuilder.excludedAdTypes(adTypes);
                                    }
                                }
                            }

                            final Set<Set<Integer>> dmpFilterSegmentExpression = extractDmpFilterExpression(tsCsidFilterExpressions[i]);
                            tsBuilder.csidFilterExpression(dmpFilterSegmentExpression);
                            tsBuilder.dataVendorCost(tsDataVendorCosts[i]);

                            final Pair<Boolean, Set<Integer>> sdkVersionsInclExcl = extractSdkVersionTargeting(tsSdkVersionJsons[i]);
                            tsBuilder.sdkVersionsInclExcl(sdkVersionsInclExcl);

                            // Device OS related targeting
                            final Map<Integer, Range<Double>> osVersionTargeting = extractOsVersionTargeting(tsOsVersionJsons[i]);
                            final Set<Integer> osSet = osVersionTargeting.keySet();
                            tsBuilder.osSet(CollectionUtils.isEmpty(osSet) ? null : osSet);
                            tsBuilder.osVersionsRange(osVersionTargeting);

                            // Country + Cities + States + Geo region Id + Language
                            final Map<Integer, Pair<Boolean, Set<Integer>>> citiesInclExcl =
                                    extractCountryCitiesTargeting(tsCountryCityJsons[i]);
                            final Set<Integer> countriesSet = citiesInclExcl.keySet();
                            tsBuilder.countries(CollectionUtils.isEmpty(countriesSet) ? null : countriesSet);
                            tsBuilder.citiesInclExcl(citiesInclExcl);

                            // Device manufacturer and model related targeting
                            final Map<Long, Pair<Boolean, Set<Long>>> deviceModelsInclExcl = extractManufModelTargeting(tsManufModelJsons[i]);
                            final Set<Long> manufacturersSet = deviceModelsInclExcl.keySet();
                            tsBuilder.manufacturers(CollectionUtils.isEmpty(manufacturersSet) ? null : manufacturersSet);
                            tsBuilder.deviceModelsInclExcl(deviceModelsInclExcl);

                            // Assign the dsts and dsps of associated deals
                            tsBuilder.dsts(deals.stream().map(DealEntity::getDst).collect(Collectors.toSet()));
                            tsBuilder.dsps(deals.stream().map(DealEntity::getDsp).collect(Collectors.toSet()));

                            targetingSegmentMap.put(targetingSegmentId, tsBuilder.build());
                        }
                    } catch (final Exception e) {
                        logger.error("Ignoring erroneously configured targeting segment: " + targetSegments[i] + ".\n" , e);
                        InspectorStats.incrementStatCount(OVERALL_PMP_ERROR_STATS, BADLY_CONFIGURED_TARGETING_SEGMENT + targetSegments[i]);
                    }
                }

                // Add mappings, if there is at least 1 non-erroneous targeting segment and 1 non-erroneous deal
                if (deals.size() > 0 && (targetingSegmentMap.size() > 0 || targetingSegmentsReused > 0)) {
                    final PackageEntity packageEntity = PackageEntity.newBuilder()
                            .id(packageId)
                            .targetingSegmentIds(targetingSegmentMap.values().stream().map(TargetingSegmentEntity::getId).collect(Collectors.toSet()))
                            .enforceViewabilitySDKs(rs.getBoolean("package_enforce_viewability_sdks"))
                            .build();

                    newPackageSet.add(packageEntity);
                    newDealSet.addAll(deals);
                    newTargetingSegmentMap.putAll(targetingSegmentMap);
                }

            } catch (final Exception e) {
                logger.error("Error while reading row in PackageRepositoryV2. ", e);
            }

            return new Timestamp(0);
        }

        // TODO: Can this be done incrementally? Does this cause a small downtime in package requests?
        @Override
        public void afterEachIteration() {
            final Set<TargetingSegmentEntity> targetingSegmentSet = ImmutableSet.copyOf(newTargetingSegmentMap.values());
            final Set<PackageEntity> packageSet = ImmutableSet.copyOf(newPackageSet);
            final Set<DealEntity> dealSet = ImmutableSet.copyOf(newDealSet);

            indexedTargetingSegments.clear();
            indexedPackages.clear();
            indexedDeals.clear();

            indexedDeals.addAll(dealSet);
            indexedPackages.addAll(packageSet);
            indexedTargetingSegments.addAll(targetingSegmentSet);
        }
    }
}
