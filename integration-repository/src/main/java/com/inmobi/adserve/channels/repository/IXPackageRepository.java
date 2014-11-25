package com.inmobi.adserve.channels.repository;


import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.googlecode.cqengine.CQEngine;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.data.repository.DBReaderDelegate;
import com.inmobi.data.repository.ScheduledDbReader;
import com.inmobi.segment.Segment;
import com.inmobi.segment.impl.CarrierId;
import com.inmobi.segment.impl.Country;
import com.inmobi.segment.impl.DeviceOs;
import com.inmobi.segment.impl.InventoryType;
import com.inmobi.segment.impl.InventoryTypeEnum;
import com.inmobi.segment.impl.LatlongPresent;
import com.inmobi.segment.impl.NetworkType;
import com.inmobi.segment.impl.SiteCategory;
import com.inmobi.segment.impl.SiteCategoryEnum;
import com.inmobi.segment.impl.SiteId;
import com.inmobi.segment.impl.SlotId;
import com.inmobi.segment.impl.UidPresent;
import com.inmobi.segment.impl.ZipCodePresent;
import com.inmobi.segmentparameter.SegmentParameter;
import lombok.Getter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class IXPackageRepository {
    private volatile Map<Long, IXPackageEntity> packageSet = Collections.emptyMap();
    @Getter
    private IndexedCollection<IXPackageEntity> packageIndex;
    private ScheduledDbReader reader;
    private static final Integer[][] EMPTY_2D_INTEGER_ARRAY = new Integer[0][];
    private Logger logger;

    public static final String ALL_SITE_ID = "A";
    public static final Integer ALL_COUNTRY_ID = -1;
    public static final Integer ALL_OS_ID = -1;
    public static final Integer ALL_SLOT_ID = -1;

    public static final Attribute<IXPackageEntity, String> SITE_ID = new MultiValueAttribute<IXPackageEntity, String>(
            "site_id") {
        public List<String> getValues(IXPackageEntity entity) {
            Segment segment = entity.getSegment();

            Collection<String> siteIds = null;
            SegmentParameter<?> siteIdParam = segment.getSegmentParameters().get(SiteId.class.getName());
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
                public List<Integer> getValues(IXPackageEntity entity) {
                    Segment segment = entity.getSegment();

                    Collection<Integer> countryIds = null;
                    SegmentParameter<?> countryIdParam = segment.getSegmentParameters().get(Country.class.getName());
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
        public List<Integer> getValues(IXPackageEntity entity) {
            Segment segment = entity.getSegment();

            Collection<Integer> osIds = null;
            SegmentParameter<?> osIdParam = segment.getSegmentParameters().get(DeviceOs.class.getName());
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
                public List<Integer> getValues(IXPackageEntity entity) {
                    Segment segment = entity.getSegment();

                    Collection<Integer> slotIds = null;
                    SegmentParameter<?> slotIdParam = segment.getSegmentParameters().get(SlotId.class.getName());
                    if (slotIdParam != null) {
                        slotIds = (Collection<Integer>) slotIdParam.getValue();
                    }
                    if (slotIds == null || slotIds.isEmpty()) {
                        slotIds = Collections.singleton(ALL_SLOT_ID);
                    }
                    return new ArrayList<>(slotIds);
                }
            };

    public void init(Logger logger, DataSource dataSource, Configuration config, String instanceName) {

        this.logger = logger;
        String query = config.getString("query");
        MetricRegistry metricsRegistry = new MetricRegistry();

        this.reader =
                new ScheduledDbReader(dataSource, query, null, new IXPackageReaderDelegate(), new MetricRegistry(),
                        getRepositorySchedule(config), Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                        .setNameFormat("repository-update-%d").build()), instanceName);

        packageIndex = CQEngine.newInstance();
        packageIndex.addIndex(HashIndex.onAttribute(SITE_ID));
        packageIndex.addIndex(HashIndex.onAttribute(COUNTRY_ID));
        packageIndex.addIndex(HashIndex.onAttribute(OS_ID));
        packageIndex.addIndex(HashIndex.onAttribute(SLOT_ID));

        metricsRegistry.register(MetricRegistry.name(this.getClass(), "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return packageSet.size();
            }
        });
        start();
    }

    public class IXPackageReaderDelegate implements DBReaderDelegate {
        private Map<Long, IXPackageEntity> newIXPackageSet;

        @Override
        public void beforeEachIteration() {
            newIXPackageSet = new HashMap<>();
        }

        @Override
        public Timestamp readRow(ResultSet rs) throws SQLException {

            Timestamp ts;
            try {
                long id = rs.getLong("id");
                Integer[] osIds = (Integer[]) rs.getArray("os_ids").getArray();
                String[] siteIds = (String[]) rs.getArray("site_ids").getArray();
                boolean latLongOnly = rs.getBoolean("lat_long_only");
                boolean zipCodeOnly = rs.getBoolean("zip_code_only");
                boolean ifaOnly = rs.getBoolean("ifa_only");
                Integer[] countryIds = (Integer[]) rs.getArray("country_ids").getArray();
                String[] inventoryTypes = (String[]) rs.getArray("inventory_types").getArray();
                Long[] carrierIds = (Long[]) rs.getArray("carrier_ids").getArray();
                String[] siteCategories = (String[]) rs.getArray("site_categories").getArray();
                String[] connectionTypes = (String[]) rs.getArray("connection_types").getArray();
                int dataVendorId = rs.getInt("data_vendor_id");
                int dmpId = rs.getInt("dmp_id");

                Set<Set<Integer>> dmpFilterSegmentExpression;
                try {
                    dmpFilterSegmentExpression = extractDmpFilterExpression(rs.getString("dmp_filter_expression"));
                } catch (JSONException e) {
                    logger.error("Invalid dmpFilterExpressionJson in IXPackageRepository for id " + id, e);
                    // Skip this record.
                    return rs.getTimestamp("last_modified");
                }

                Object[] outputArray = (Object[]) rs.getArray("scheduled_tods").getArray();
                Integer[][] scheduleTimeOfDays =
                        (outputArray.length == 0) ? EMPTY_2D_INTEGER_ARRAY : (Integer[][]) outputArray;

                Integer[] slotIds = (Integer[]) rs.getArray("placement_slot_ids").getArray();

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

                InventoryType inventoryType = null;
                if (ArrayUtils.isNotEmpty(inventoryTypes)) {
                    InventoryTypeEnum[] inventoryTypeEnums = new InventoryTypeEnum[inventoryTypes.length];
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
                    SiteCategoryEnum[] siteCategoryEnums = new SiteCategoryEnum[siteCategories.length];
                    for (int i = 0; i < siteCategories.length; i++) {
                        siteCategoryEnums[i] = SiteCategoryEnum.valueOf(siteCategories[i]);
                    }
                    siteCategory = new SiteCategory();
                    siteCategory.init(ImmutableSet.copyOf(siteCategoryEnums));
                }

                NetworkType networkType = null;
                if (ArrayUtils.isNotEmpty(connectionTypes)) {
                    Integer[] networkTypeIds = new Integer[connectionTypes.length];
                    for (int i = 0; i < connectionTypes.length; i++) {
                        networkTypeIds[i] = connectionTypes[i].equals("WIFI") ? 0 : 1;
                    }
                    networkType = new NetworkType();
                    networkType.init(ImmutableSet.copyOf(networkTypeIds));
                }

                SlotId slotId = null;
                if (ArrayUtils.isNotEmpty(slotIds)) {
                    slotId = new SlotId();
                    slotId.init(ImmutableSet.copyOf(slotIds));
                }

                // Segment builder.
                Segment.Builder repoSegmentBuilder = new Segment.Builder();

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

                if (inventoryType != null) {
                    repoSegmentBuilder.addSegmentParameter(inventoryType);
                }

                if (carrierId != null) {
                    repoSegmentBuilder.addSegmentParameter(carrierId);
                }

                if (siteCategory != null) {
                    repoSegmentBuilder.addSegmentParameter(siteCategory);
                }

                if (networkType != null) {
                    repoSegmentBuilder.addSegmentParameter(networkType);
                }

                if (slotId != null) {
                    repoSegmentBuilder.addSegmentParameter(slotId);
                }
                Segment segment = repoSegmentBuilder.build();

                // Entity builder
                IXPackageEntity.Builder entityBuilder = IXPackageEntity.newBuilder();
                entityBuilder.setId(id);
                entityBuilder.setSegment(segment);
                entityBuilder.setDmpId(dmpId);
                entityBuilder.setDmpVendorId(dataVendorId);
                entityBuilder.setDmpFilterSegmentExpression(dmpFilterSegmentExpression);
                entityBuilder.setScheduledTimeOfDays(scheduleTimeOfDays);

                IXPackageEntity entity = entityBuilder.build();

                boolean active = rs.getBoolean("is_active");
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

            } catch (Exception e) {
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
        if (this.packageSet != null) {
            return this.packageSet.values();
        } else {
            return Collections.emptySet();
        }
    }

    private Scheduler getRepositorySchedule(Configuration config) {
        int initialDelay = Preconditions.checkNotNull(config.getInt("initialDelay"));
        int refreshTime = Preconditions.checkNotNull(config.getInt("refreshTime"));

        return Scheduler.newFixedRateSchedule(initialDelay, refreshTime, TimeUnit.SECONDS);
    }

    private Set<Set<Integer>> extractDmpFilterExpression(String dmpFilterExpressionJson) throws JSONException {
        Set<Set<Integer>> dmpFilterSegmentExpression = new HashSet<>();
        if (!(StringUtils.isEmpty(dmpFilterExpressionJson))) {
            JSONArray dmpSegmentsJsonArray = new JSONArray(dmpFilterExpressionJson);
            for (int andSetIdx = 0; andSetIdx < dmpSegmentsJsonArray.length(); andSetIdx++) {
                JSONArray andJsonArr = (JSONArray) dmpSegmentsJsonArray.get(andSetIdx);
                Set<Integer> orSet = new HashSet<>();
                for (int orSetIdx = 0; orSetIdx < andJsonArr.length(); orSetIdx++) {
                    orSet.add((Integer) (andJsonArr.get(orSetIdx)));
                }
                dmpFilterSegmentExpression.add(orSet);
            }
        }
        return dmpFilterSegmentExpression;
    }

}
