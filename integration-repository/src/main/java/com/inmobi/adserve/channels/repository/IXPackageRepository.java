package com.inmobi.adserve.channels.repository;


import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.googlecode.cqengine.CQEngine;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.MultiValueNullableAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.data.repository.DBReaderDelegate;
import com.inmobi.data.repository.ScheduledDbReader;
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
import com.inmobi.segmentparameter.SegmentParameter;

import lombok.Getter;


public class IXPackageRepository {
    private volatile Map<Integer, IXPackageEntity> packageSet = Collections.emptyMap();
    @Getter
    private IndexedCollection<IXPackageEntity> packageIndex;
    private ScheduledDbReader reader;
    private static final Integer[][] EMPTY_2D_INTEGER_ARRAY = new Integer[0][];
    private Logger logger;

    public static final String ALL_SITE_ID = "A";
    public static final Integer ALL_COUNTRY_ID = -1;
    public static final Integer ALL_OS_ID = -1;
    public static final Integer ALL_SLOT_ID = -1;

    public static final Attribute<IXPackageEntity, String> DEAL_IDS =
            new MultiValueNullableAttribute<IXPackageEntity, String>("deal_ids", false) {
                @Override
                public List<String> getNullableValues(final IXPackageEntity entity) {
                    final List<String> dealIds = entity.getDealIds();
                    return dealIds;
                }
            };

    public static final Attribute<IXPackageEntity, String> SITE_ID = new MultiValueAttribute<IXPackageEntity, String>(
            "site_id") {
        @Override
        @SuppressWarnings("unchecked")
        public List<String> getValues(final IXPackageEntity entity) {
            final Segment segment = entity.getSegment();

            Collection<String> siteIds = null;
            final SegmentParameter<?> siteIdParam = segment.getSegmentParameters().get(SiteId.class.getName());
            if (siteIdParam != null) {
                siteIds = (Collection<String>) siteIdParam.getValue();
            }
            if (siteIds == null || siteIds.isEmpty()) {
                siteIds = Collections.singleton(ALL_SITE_ID);
            }
            return new ArrayList<>(siteIds);
        }
    };

    public static final Attribute<IXPackageEntity, Integer> COUNTRY_ID =
            new MultiValueAttribute<IXPackageEntity, Integer>("country_id") {
                @Override
                @SuppressWarnings("unchecked")
                public List<Integer> getValues(final IXPackageEntity entity) {
                    final Segment segment = entity.getSegment();
                    Collection<Integer> countryIds = null;
                    final SegmentParameter<?> countryIdParam =
                            segment.getSegmentParameters().get(Country.class.getName());
                    if (countryIdParam != null) {
                        countryIds = (Collection<Integer>) countryIdParam.getValue();
                    }
                    if (countryIds == null || countryIds.isEmpty()) {
                        countryIds = Collections.singleton(ALL_COUNTRY_ID);
                    }
                    return new ArrayList<>(countryIds);
                }
            };

    public static final Attribute<IXPackageEntity, Integer> OS_ID = new MultiValueAttribute<IXPackageEntity, Integer>(
            "os_id") {
        @Override
        @SuppressWarnings("unchecked")
        public List<Integer> getValues(final IXPackageEntity entity) {
            final Segment segment = entity.getSegment();

            Collection<Integer> osIds = null;
            final SegmentParameter<?> osIdParam = segment.getSegmentParameters().get(DeviceOs.class.getName());
            if (osIdParam != null) {
                osIds = (Collection<Integer>) osIdParam.getValue();
            }
            if (osIds == null || osIds.isEmpty()) {
                osIds = Collections.singleton(ALL_OS_ID);
            }
            return new ArrayList<>(osIds);
        }
    };

    public static final Attribute<IXPackageEntity, Integer> SLOT_ID =
            new MultiValueAttribute<IXPackageEntity, Integer>("slot_id") {
                @Override
                @SuppressWarnings("unchecked")
                public List<Integer> getValues(final IXPackageEntity entity) {
                    final Segment segment = entity.getSegment();

                    Collection<Integer> slotIds = null;
                    final SegmentParameter<?> slotIdParam = segment.getSegmentParameters().get(SlotId.class.getName());
                    if (slotIdParam != null) {
                        slotIds = (Collection<Integer>) slotIdParam.getValue();
                    }
                    if (slotIds == null || slotIds.isEmpty()) {
                        slotIds = Collections.singleton(ALL_SLOT_ID);
                    }
                    return new ArrayList<>(slotIds);
                }
            };

    public void init(final Logger logger, final DataSource dataSource, final Configuration config,
            final String instanceName) {

        this.logger = logger;
        final String query = config.getString("query");
        final MetricRegistry metricsRegistry = new MetricRegistry();

        reader =
                new ScheduledDbReader(dataSource, query, null, new IXPackageReaderDelegate(), new MetricRegistry(),
                        getRepositorySchedule(config), Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                                .setNameFormat("repository-update-%d").build()), instanceName);

        packageIndex = CQEngine.newInstance();
        packageIndex.addIndex(HashIndex.onAttribute(SITE_ID));
        packageIndex.addIndex(HashIndex.onAttribute(COUNTRY_ID));
        packageIndex.addIndex(HashIndex.onAttribute(OS_ID));
        packageIndex.addIndex(HashIndex.onAttribute(SLOT_ID));
        packageIndex.addIndex(HashIndex.onAttribute(DEAL_IDS));

        metricsRegistry.register(MetricRegistry.name(this.getClass(), "size"), (Gauge<Integer>) () -> packageSet.size());
        start();
    }

    public class IXPackageReaderDelegate implements DBReaderDelegate {
        private Map<Integer, IXPackageEntity> newIXPackageSet;

        @Override
        public void beforeEachIteration() {
            newIXPackageSet = new HashMap<>();
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

                final String[] dealIds = (String[]) rs.getArray("deal_ids").getArray();
                final String[] accessTypes = (String[]) rs.getArray("access_types").getArray();
                final Double[] dealFloors = (Double[]) rs.getArray("deal_floors").getArray();

                String[] viewabilityTrackers = null;
                if (null != rs.getArray("viewability_trackers")) {
                    viewabilityTrackers = (String[]) rs.getArray("viewability_trackers").getArray();
                }

                Integer[] rpAgencyIds = null;
                Double[] agencyRebatePercentages = null;
                if (null != rs.getArray("rp_agency_ids")) {
                    rpAgencyIds = (Integer[]) rs.getArray("rp_agency_ids").getArray();
                }
                if (null != rs.getArray("agency_rebate_percentages")) {
                    agencyRebatePercentages = (Double[]) rs.getArray("agency_rebate_percentages").getArray();
                }

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

                final Object[] outputArray = (Object[]) rs.getArray("scheduled_tods").getArray();
                final Integer[][] scheduleTimeOfDays =
                        outputArray.length == 0 ? EMPTY_2D_INTEGER_ARRAY : (Integer[][]) outputArray;

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
                entityBuilder.scheduledTimeOfDays(scheduleTimeOfDays);
                entityBuilder.secondaryAdFormatConstraints(secondaryAdFormatConstraints);

                if (null != dealIds) {
                    entityBuilder.dealIds(Arrays.asList(dealIds));
                }
                if (null != dealFloors) {
                    entityBuilder.dealFloors(Arrays.asList(dealFloors));
                }
                if (null != rpAgencyIds) {
                    entityBuilder.rpAgencyIds(Arrays.asList(rpAgencyIds));
                }
                if (null != agencyRebatePercentages) {
                    entityBuilder.agencyRebatePercentages(Arrays.asList(agencyRebatePercentages));
                }
                if (null != accessTypes) {
                    entityBuilder.accessTypes(Arrays.asList(accessTypes));
                }
                if (null != geoFenceRegion) {
                    entityBuilder.geoFenceRegion(geoFenceRegion);
                }
                if (null != viewabilityTrackers) {
                    entityBuilder.viewabilityTrackers(Arrays.asList(viewabilityTrackers));
                }
                entityBuilder.dataVendorCost(dataVendorCost);

                final IXPackageEntity entity = entityBuilder.build();
                final boolean active = rs.getBoolean("is_active");
                if (active) {
                    newIXPackageSet.put(id, entity);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding entity with id: " + id + " to IXPackageRepository.");
                    }
                } else {
                    newIXPackageSet.remove(id);
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
            packageSet = ImmutableMap.copyOf(newIXPackageSet);
            packageIndex.clear();
            packageIndex.addAll(packageSet.values());
        }
    }

    private void start() {
        logger.info("Start IXPackageRepository updates.");
        reader.startAsync();
    }

    public void stop() {
        logger.info("Stop IXPackageRepository updates.");
        reader.stopAsync();
    }

    public boolean isInitialized() {
        return reader.isInitialized();
    }

    public Collection<IXPackageEntity> getIXPackageSet() {
        if (packageSet != null) {
            return packageSet.values();
        } else {
            return Collections.emptySet();
        }
    }

    private Scheduler getRepositorySchedule(final Configuration config) {
        final int initialDelay = Preconditions.checkNotNull(config.getInt("initialDelay"));
        final int refreshTime = Preconditions.checkNotNull(config.getInt("refreshTime"));

        return Scheduler.newFixedRateSchedule(initialDelay, refreshTime, TimeUnit.SECONDS);
    }

    private static Set<Set<Integer>> extractDmpFilterExpression(final String dmpFilterExpressionJson)
            throws JSONException {
        final Set<Set<Integer>> dmpFilterSegmentExpression = new HashSet<>();

        if (StringUtils.isNotEmpty(dmpFilterExpressionJson)) {
            final JSONArray dmpSegmentsJsonArray = new JSONArray(dmpFilterExpressionJson);
            for (int andSetIdx = 0; andSetIdx < dmpSegmentsJsonArray.length(); andSetIdx++) {
                final JSONArray andJsonArr = (JSONArray) dmpSegmentsJsonArray.get(andSetIdx);
                final Set<Integer> orSet = new HashSet<>();
                for (int orSetIdx = 0; orSetIdx < andJsonArr.length(); orSetIdx++) {
                    orSet.add((Integer) andJsonArr.get(orSetIdx));
                }
                dmpFilterSegmentExpression.add(orSet);
            }
        }

        return dmpFilterSegmentExpression;
    }

    /**
     * This function extracts the os version targeting meta data. Meta Data consists of a map that maps the os id to a
     * Closed Range
     *
     * note: osId in the adPoolRequest is a long, osId in CAS is an int and osId in the ix_packages table is a short
     * 
     * @param osVersionTargetingJson
     * @return
     * @throws JSONException
     */
    protected static Map<Integer, Range<Double>> extractOsVersionTargeting(final String osVersionTargetingJson)
            throws JSONException {
        final ImmutableMap.Builder<Integer, Range<Double>> osVersionTargeting = new ImmutableMap.Builder<>();

        if (StringUtils.isNotEmpty(osVersionTargetingJson)) {
            final JSONArray jsonArray = new JSONArray(osVersionTargetingJson);

            // Iterate over all os ids
            for (int index = 0; index < jsonArray.length(); ++index) {
                final JSONObject osEntry = (JSONObject) jsonArray.get(index);
                final JSONArray osVersionRangeJsonArray = osEntry.getJSONArray("range");
                Range<Double> osVersionRange;

                // Sanity for malformed ranges
                if (osVersionRangeJsonArray.length() != 2) {
                    osVersionRange = Range.all();
                } else {
                    double minVer = osVersionRangeJsonArray.getDouble(0);
                    double maxVer = osVersionRangeJsonArray.getDouble(1);
                    // Sanity for range: minVer must always be <= maxVer
                    if (minVer > maxVer) {
                        final double temp = minVer;
                        minVer = maxVer;
                        maxVer = temp;
                    }

                    osVersionRange = Range.closed(minVer, maxVer);
                }

                osVersionTargeting.put(osEntry.getInt("osId"), osVersionRange);
            }
        }

        return osVersionTargeting.build();
    }

    /**
     * This function extracts the device manufacturer and device model targeting meta data. Meta Data consists of a map
     * that maps the device manufacturer id (Long) to the inclusion boolean (Boolean) to the set of device model ids.
     *
     * @param manufModelTargetingJson
     * @return Map as described above
     * @throws JSONException
     */
    protected static Map<Long, Pair<Boolean, Set<Long>>> extractManufModelTargeting(final String manufModelTargetingJson)
            throws JSONException {
        final ImmutableMap.Builder<Long, Pair<Boolean, Set<Long>>> manufModelTargeting = new ImmutableMap.Builder<>();

        if (StringUtils.isNotEmpty(manufModelTargetingJson)) {
            final JSONArray jsonArray = new JSONArray(manufModelTargetingJson);

            // Iterate over all device manufacturer ids
            for (int manufIndex = 0; manufIndex < jsonArray.length(); ++manufIndex) {
                final JSONObject manufEntry = (JSONObject) jsonArray.get(manufIndex);

                final Builder<Long> modelIds = new Builder<>();
                final JSONArray modelIdsJsonArray = manufEntry.getJSONArray("modelIds");

                // Iterate over all the device model ids and add them to the modelIds Set
                for (int modelIndex = 0; modelIndex < modelIdsJsonArray.length(); ++modelIndex) {
                    modelIds.add(modelIdsJsonArray.getLong(modelIndex));
                }

                // Determine whether the modelIds Set is an inclusion or an exclusion Set
                final Boolean incl = manufEntry.getBoolean("incl");

                // Sanity: If modelIds Set is empty and incl is false, then skip manufacturer
                if (0 == modelIdsJsonArray.length() && !incl) {
                    continue;
                }

                manufModelTargeting.put(manufEntry.getLong("manufId"), ImmutablePair.of(incl, modelIds.build()));
            }
        }

        return manufModelTargeting.build();
    }

    static Pair<Boolean, Set<Integer>> extractSdkVersionTargeting(final String sdkVersionTargetingJson)
            throws JSONException {
        final ImmutableSet.Builder<Integer> sdkVersionSet = new ImmutableSet.Builder<>();
        boolean exclusion = true;

        if (StringUtils.isNotBlank(sdkVersionTargetingJson)) {
            try {
                final JSONObject sdkVersionTargetingJsonObject = new JSONObject(sdkVersionTargetingJson);
                JSONArray sdkVersionJsonArray = null;

                // Inclusion has higher priority in case of faulty jsons
                if (sdkVersionTargetingJsonObject.has("inclusion")) {
                    exclusion = false;
                    sdkVersionJsonArray = sdkVersionTargetingJsonObject.getJSONArray("inclusion");
                } else if (sdkVersionTargetingJsonObject.has("exclusion")) {
                    sdkVersionJsonArray = sdkVersionTargetingJsonObject.getJSONArray("exclusion");
                }

                if (null != sdkVersionJsonArray) {
                    for (int index = 0; index < sdkVersionJsonArray.length(); ++index) {
                        try {
                            sdkVersionSet.add(sdkVersionJsonArray.getInt(index));
                        } catch (final Exception e) {
                            // Ignore entry
                        }
                    }
                }
            } catch (final JSONException je) {
                // Ignore list
            }
        }

        return ImmutablePair.of(exclusion, sdkVersionSet.build());
    }
}
