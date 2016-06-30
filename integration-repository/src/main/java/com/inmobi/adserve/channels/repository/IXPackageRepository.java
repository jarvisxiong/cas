package com.inmobi.adserve.channels.repository;


import static com.inmobi.adserve.channels.repository.pmp.DealAttributes.DEAL_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.COUNTRY_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.OS_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.PACKAGE_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.SITE_ID;
import static com.inmobi.adserve.channels.repository.pmp.DeprecatedIXPackageAttributes.SLOT_ID;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractDmpFilterExpression;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractManufModelTargeting;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractOsVersionTargeting;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.extractSdkVersionTargeting;
import static com.inmobi.adserve.channels.repository.pmp.PackageHelperV2.getThirdPartyTrackerMap;
import static com.inmobi.adserve.channels.util.demand.enums.AuctionType.FIRST_PRICE;
import static com.inmobi.casthrift.DemandSourceType.IX;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.json.JSONException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.repository.pmp.AbstractCQEngineRepository;
import com.inmobi.adserve.channels.util.demand.enums.DealType;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.data.repository.DBReaderDelegate;
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
import com.inmobi.segment.impl.SiteCategoryEnum;
import com.inmobi.segment.impl.SiteId;
import com.inmobi.segment.impl.SlotId;
import com.inmobi.segment.impl.UidPresent;
import com.inmobi.segment.impl.ZipCodePresent;

import lombok.Getter;


public class IXPackageRepository extends AbstractCQEngineRepository {
    @Getter
    private IndexedCollection<IXPackageEntity> indexedPackages;
    @Getter
    private IndexedCollection<DealEntity> indexedDeals;

    public void init(final Logger logger, final DataSource dataSource, final Configuration config,
            final String instanceName) {

        super.init(logger, dataSource, config, instanceName, new IXPackageReaderDelegate());

        indexedPackages = new ConcurrentIndexedCollection<>();
        indexedDeals = new ConcurrentIndexedCollection<>();

        indexedPackages.addIndex(HashIndex.onAttribute(SITE_ID));
        indexedPackages.addIndex(HashIndex.onAttribute(COUNTRY_ID));
        indexedPackages.addIndex(HashIndex.onAttribute(OS_ID));
        indexedPackages.addIndex(HashIndex.onAttribute(SLOT_ID));
        indexedPackages.addIndex(UniqueIndex.onAttribute(PACKAGE_ID));

        indexedDeals.addIndex(UniqueIndex.onAttribute(DEAL_ID));

        start();
    }

    private class IXPackageReaderDelegate implements DBReaderDelegate {
        private Set<IXPackageEntity> newIXPackageSet;
        private Set<DealEntity> newIXDealsSet;

        @Override
        public void beforeEachIteration() {
            newIXPackageSet = new HashSet<>();
            newIXDealsSet = new HashSet<>();
        }

        @Override
        public Timestamp readRow(final ResultSet rs) throws SQLException {
            Timestamp ts;
            try {
                final int id = rs.getInt("id");
                final Integer[] osIds = (Integer[]) rs.getArray("os_ids").getArray();
                final String[] siteIds = (String[]) rs.getArray("site_ids").getArray();
                final boolean latLongOnly = rs.getBoolean("lat_long_only");
                final boolean zipCodeOnly = rs.getBoolean("zip_code_only");
                final boolean ifaOnly = rs.getBoolean("ifa_only");
                final Integer[] countryIds = (Integer[]) rs.getArray("country_ids").getArray();
                final Integer[] cityIds = (Integer[]) rs.getArray("city_ids").getArray();
                final String[] inventoryTypes = (String[]) rs.getArray("inventory_types").getArray();
                final Long[] carrierIds = (Long[]) rs.getArray("carrier_ids").getArray();
                final String[] siteCategories = (String[]) rs.getArray("site_categories").getArray();
                final String[] connectionTypes = (String[]) rs.getArray("connection_types").getArray();
                String[] geoSourceTypes = null;
                final String geoFenceRegion = rs.getString("geo_fence_region");
                final Array languageArray = rs.getArray("language_targeting_list");
                final Set<String> languageTargetingSet = new HashSet<>();
                final Integer geocookieId = rs.getInt("geocookie_id");
                if (null != languageArray) {
                    if (null != languageArray.getArray()) {
                        final String[] languageTargetingList = (String[]) languageArray.getArray();
                        Collections.addAll(languageTargetingSet, languageTargetingList);
                    }
                }
                final boolean viewable = rs.getBoolean("viewable");
                final Array adTypeTargetingArray = rs.getArray("ad_types");
                Set<SecondaryAdFormatConstraints> secondaryAdFormatConstraints = null;
                if (null != adTypeTargetingArray) {
                    final Stream<Integer> adTypesStream = Arrays.stream((Integer[])adTypeTargetingArray.getArray());
                    secondaryAdFormatConstraints = ImmutableSet.copyOf(adTypesStream
                        .distinct()
                        .map(SecondaryAdFormatConstraints::getDemandAdFormatConstraintsByValue)
                        .filter(demandConstraint -> SecondaryAdFormatConstraints.UNKNOWN != demandConstraint)
                        .collect(Collectors.toSet()));
                }
                if (CollectionUtils.isEmpty(secondaryAdFormatConstraints)) {
                    secondaryAdFormatConstraints = ImmutableSet.of(SecondaryAdFormatConstraints.ALL);
                }

                if (null != rs.getArray("geo_source_types")) {
                    geoSourceTypes = (String[]) rs.getArray("geo_source_types").getArray();
                }

                final int dataVendorId = rs.getInt("data_vendor_id");
                final Double dataVendorCost = rs.getDouble("data_vendor_cost");
                final int dmpId = rs.getInt("dmp_id");
                Set<Set<Integer>> dmpFilterSegmentExpression;
                try {
                    dmpFilterSegmentExpression = extractDmpFilterExpression(rs.getString("dmp_filter_expression"));
                } catch (final JSONException e) {
                    logger.error("Invalid dmpFilterExpressionJson in IXPackageRepository for id " + id, e);
                    // Skip this record.
                    return rs.getTimestamp("last_modified");
                }

                Map<Integer, Range<Double>> osVersionTargeting;
                try {
                    osVersionTargeting = extractOsVersionTargeting(rs.getString("os_version_targeting"));
                } catch (final JSONException e) {
                    logger.error("Invalid OsVersionTargeting Json in IXPackageRepository for id " + id, e);
                    // Skip this record.
                    return rs.getTimestamp("last_modified");
                }

                Map<Long, Pair<Boolean, Set<Long>>> manufModelTargeting;
                try {
                    manufModelTargeting = extractManufModelTargeting(rs.getString("manuf_model_targeting"));
                } catch (final JSONException e) {
                    logger.error("Invalid ManufModelTargeting Json in IXPackageRepository for id " + id, e);
                    // Skip this record.
                    return rs.getTimestamp("last_modified");
                }

                Pair<Boolean, Set<Integer>> sdkVersionTargeting;
                try {
                    sdkVersionTargeting = extractSdkVersionTargeting(rs.getString("sdk_version_targeting"));
                } catch (final JSONException e) {
                    logger.error("Invalid SdkVersionTargeting Json in IXPackageRepository for id " + id, e);
                    // Skip this record.
                    return rs.getTimestamp("last_modified");
                }

                final Integer[] slotIds = (Integer[]) rs.getArray("placement_slot_ids").getArray();

                SiteId site = null;
                if (ArrayUtils.isNotEmpty(siteIds)) {
                    site = new SiteId();
                    site.init(ImmutableSet.copyOf(siteIds));
                }

                DeviceOs os = null;
                if (ArrayUtils.isNotEmpty(osIds)) {
                    os = new DeviceOs();
                    os.init(ImmutableSet.copyOf(osIds));
                }

                LatlongPresent latLongPresent = null;
                if (latLongOnly) {
                    latLongPresent = new LatlongPresent();
                    latLongPresent.init(latLongOnly);
                }

                ZipCodePresent zipCodePresent = null;
                if (zipCodeOnly) {
                    zipCodePresent = new ZipCodePresent();
                    zipCodePresent.init(zipCodeOnly);
                }

                UidPresent uidPresent = null;
                if (ifaOnly) {
                    uidPresent = new UidPresent();
                    uidPresent.init(ifaOnly);
                }

                Country country = null;
                if (ArrayUtils.isNotEmpty(countryIds)) {
                    country = new Country();
                    country.init(ImmutableSet.copyOf(countryIds));
                }

                City city = null;
                if (ArrayUtils.isNotEmpty(cityIds)) {
                    city = new City();
                    city.init(ImmutableSet.copyOf(cityIds));
                }
                InventoryType inventoryType = null;
                if (ArrayUtils.isNotEmpty(inventoryTypes)) {
                    final InventoryTypeEnum[] inventoryTypeEnums = new InventoryTypeEnum[inventoryTypes.length];
                    for (int i = 0; i < inventoryTypes.length; i++) {
                        inventoryTypeEnums[i] = InventoryTypeEnum.valueOf(inventoryTypes[i]);
                    }
                    inventoryType = new InventoryType();
                    inventoryType.init(ImmutableSet.copyOf(inventoryTypeEnums));
                }

                CarrierId carrierId = null;
                if (ArrayUtils.isNotEmpty(carrierIds)) {
                    carrierId = new CarrierId();
                    carrierId.init(ImmutableSet.copyOf(carrierIds));
                }

                SiteCategory siteCategory = null;
                if (ArrayUtils.isNotEmpty(siteCategories)) {
                    final SiteCategoryEnum[] siteCategoryEnums = new SiteCategoryEnum[siteCategories.length];
                    for (int i = 0; i < siteCategories.length; i++) {
                        siteCategoryEnums[i] = SiteCategoryEnum.valueOf(siteCategories[i]);
                    }
                    siteCategory = new SiteCategory();
                    siteCategory.init(ImmutableSet.copyOf(siteCategoryEnums));
                }

                ConnectionType connectionType = null;
                if (ArrayUtils.isNotEmpty(connectionTypes)) {
                    final ConnectionTypeEnum[] connectionTypeEnums = new ConnectionTypeEnum[connectionTypes.length];
                    for (int i = 0; i < connectionTypes.length; i++) {
                        connectionTypeEnums[i] = ConnectionTypeEnum.valueOf(connectionTypes[i]);
                    }
                    connectionType = new ConnectionType();
                    connectionType.init(ImmutableSet.copyOf(connectionTypeEnums));
                }

                GeoSourceType geoSourceType = null;
                if (ArrayUtils.isNotEmpty(geoSourceTypes)) {
                    final GeoSourceTypeEnum[] geoSourceTypeEnums = new GeoSourceTypeEnum[geoSourceTypes.length];
                    for (int i = 0; i < geoSourceTypes.length; i++) {
                        geoSourceTypeEnums[i] = GeoSourceTypeEnum.valueOf(geoSourceTypes[i]);
                    }
                    geoSourceType = new GeoSourceType();
                    geoSourceType.init(ImmutableSet.copyOf(geoSourceTypeEnums));
                }

                SlotId slotId = null;
                if (ArrayUtils.isNotEmpty(slotIds)) {
                    slotId = new SlotId();
                    slotId.init(ImmutableSet.copyOf(slotIds));
                }

                // Segment builder.
                final Segment.Builder repoSegmentBuilder = new Segment.Builder();

                if (site != null) {
                    repoSegmentBuilder.addSegmentParameter(site);
                }
                if (os != null) {
                    repoSegmentBuilder.addSegmentParameter(os);
                }

                if (latLongPresent != null) {
                    repoSegmentBuilder.addSegmentParameter(latLongPresent);
                }

                if (zipCodePresent != null) {
                    repoSegmentBuilder.addSegmentParameter(zipCodePresent);
                }

                if (uidPresent != null) {
                    repoSegmentBuilder.addSegmentParameter(uidPresent);
                }

                if (country != null) {
                    repoSegmentBuilder.addSegmentParameter(country);
                }

                if (city != null) {
                    repoSegmentBuilder.addSegmentParameter(city);
                }

                if (inventoryType != null) {
                    repoSegmentBuilder.addSegmentParameter(inventoryType);
                }

                if (carrierId != null) {
                    repoSegmentBuilder.addSegmentParameter(carrierId);
                }

                if (siteCategory != null) {
                    repoSegmentBuilder.addSegmentParameter(siteCategory);
                }

                if (connectionType != null) {
                    repoSegmentBuilder.addSegmentParameter(connectionType);
                }

                if (slotId != null) {
                    repoSegmentBuilder.addSegmentParameter(slotId);
                }

                if (geoSourceType != null) {
                    repoSegmentBuilder.addSegmentParameter(geoSourceType);
                }

                final Segment segment = repoSegmentBuilder.build();

                // Entity builder
                final IXPackageEntity.Builder entityBuilder = IXPackageEntity.newBuilder();
                entityBuilder.id(id);
                entityBuilder.viewable(viewable);
                entityBuilder.segment(segment);
                entityBuilder.dmpId(dmpId);
                entityBuilder.dmpVendorId(dataVendorId);
                entityBuilder.dmpFilterSegmentExpression(dmpFilterSegmentExpression);
                entityBuilder.osVersionTargeting(osVersionTargeting);
                entityBuilder.manufModelTargeting(manufModelTargeting);
                entityBuilder.sdkVersionTargeting(sdkVersionTargeting);
                entityBuilder.languageTargetingSet(languageTargetingSet);
                entityBuilder.secondaryAdFormatConstraints(secondaryAdFormatConstraints);
                entityBuilder.dataVendorCost(dataVendorCost);

                if (null != geoFenceRegion) {
                    entityBuilder.geoFenceRegion(geoFenceRegion);
                }

                if (null != geocookieId) {
                    entityBuilder.geocookieId(geocookieId);
                }

                final IXPackageEntity entity = entityBuilder.build();

                // Creating Deals
                final String[] dealIds = (String[]) rs.getArray("deal_ids").getArray();
                final String[] accessTypes = (String[]) rs.getArray("access_types").getArray();
                final Double[] dealFloors = (Double[]) rs.getArray("deal_floors").getArray();

                Integer[] rpAgencyIds = null;
                Double[] agencyRebatePercentages = null;
                if (null != rs.getArray("rp_agency_ids")) {
                    rpAgencyIds = (Integer[]) rs.getArray("rp_agency_ids").getArray();
                }
                if (null != rs.getArray("agency_rebate_percentages")) {
                    agencyRebatePercentages = (Double[]) rs.getArray("agency_rebate_percentages").getArray();
                }

                String[] thirdPartyTrackerJsonList = null;
                if (null != rs.getArray("third_party_tracker_json_list")) {
                    thirdPartyTrackerJsonList = (String[]) rs.getArray("third_party_tracker_json_list").getArray();
                }

                for (int i = 0; i< dealIds.length; ++i) {
                    final DealEntity.Builder dealBuilder = DealEntity.newBuilder();
                    dealBuilder.id(dealIds[i]);
                    dealBuilder.floor(dealFloors[i]);
                    dealBuilder.auctionType(FIRST_PRICE);
                    dealBuilder.dst(IX);

                    if (null != rpAgencyIds) {
                        dealBuilder.externalAgencyId(rpAgencyIds[i]);
                    }

                    if (null != agencyRebatePercentages) {
                        dealBuilder.agencyRebatePercentage(agencyRebatePercentages[i]);
                    }

                    dealBuilder.toBeBilledOnViewability(viewable);
                    dealBuilder.packageId(id);

                    dealBuilder.dealType(DealType.getDealTypeByName(accessTypes[i]));
                    dealBuilder.thirdPartyTrackersMap(getThirdPartyTrackerMap(thirdPartyTrackerJsonList[i]));
                    newIXDealsSet.add(dealBuilder.build());
                }

                final boolean active = rs.getBoolean("is_active");
                if (active) {
                    newIXPackageSet.add(entity);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding entity with id: " + id + " to IXPackageRepository.");
                    }
                } else {
                    newIXPackageSet.remove(entity);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removing entity with id: " + id + " from IXPackageRepository.");
                    }
                }
                ts = rs.getTimestamp("last_modified");
            } catch (final Exception e) {
                logger.error("Error while reading row in IXPackageRepository.", e);
                ts = new Timestamp(0);
            }
            return ts;
        }

        @Override
        public void afterEachIteration() {
            final Set<IXPackageEntity> packagesSet = ImmutableSet.copyOf(newIXPackageSet);
            final Set<DealEntity> dealsSet = ImmutableSet.copyOf(newIXDealsSet);
            // TODO: Investigate whether this can cause any problems in the future
            indexedPackages.clear();
            indexedDeals.clear();

            indexedDeals.addAll(dealsSet);
            indexedPackages.addAll(packagesSet);
        }
    }


}
