/*
package com.inmobi.adserve.channels.repository;

import static com.inmobi.adserve.channels.repository.IXPackageRepository.extractManufModelTargeting;
import static com.inmobi.adserve.channels.repository.IXPackageRepository.extractOsVersionTargeting;
import static com.inmobi.adserve.channels.repository.IXPackageRepository.extractSdkVersionTargeting;
import static org.powermock.api.easymock.PowerMock.createMock;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.phoenix.batteries.data.test.NoOpDataSource;
import com.inmobi.phoenix.batteries.data.test.ResultSetExpectationSetter;
import com.inmobi.segment.Segment;
import com.inmobi.segment.impl.City;
import com.inmobi.segment.impl.ConnectionType;
import com.inmobi.segment.impl.ConnectionTypeEnum;
import com.inmobi.segment.impl.Country;
import com.inmobi.segment.impl.DeviceOs;
import com.inmobi.segment.impl.InventoryType;
import com.inmobi.segment.impl.InventoryTypeEnum;
import com.inmobi.segment.impl.LatlongPresent;
import com.inmobi.segment.impl.SiteCategory;
import com.inmobi.segment.impl.SiteCategoryEnum;
import com.inmobi.segment.impl.SlotId;

public class IXPackageRepositoryTest {

    @Test
    public void testRepo() throws SQLException {
        Configuration mockConfig = createMock(Configuration.class);
        Logger mockLogger = createMock(Logger.class);
        EasyMock.expect(mockConfig.getString("query")).andReturn("select * from ix_packages;").once();
        EasyMock.expect(mockConfig.getInt("initialDelay")).andReturn(1).once();
        EasyMock.expect(mockConfig.getInt("refreshTime")).andReturn(300).once();
        EasyMock.replay(mockConfig);

        IXPackageRepository repository = new IXPackageRepository();

        repository.init(mockLogger, NoOpDataSource.getNoOpDataSource(),
                mockConfig,
                "dummy");

        Assert.assertTrue(repository.getIXPackageSet().isEmpty());
        ResultSet rs = EasyMock.createNiceMock(ResultSet.class);


        Array osArray = EasyMock.createNiceMock(Array.class);
        Integer[] deviceOS = new Integer[] {3, 5};
        EasyMock.expect(rs.getArray("os_ids")).andReturn(osArray).anyTimes();
        EasyMock.expect(osArray.getArray()).andReturn(deviceOS).anyTimes();

        Array sitesArray = EasyMock.createNiceMock(Array.class);
        String[] siteIds = {};
        EasyMock.expect(rs.getArray("site_ids")).andReturn(sitesArray).anyTimes();
        EasyMock.expect(sitesArray.getArray()).andReturn(siteIds).anyTimes();

        Array dealsArray = EasyMock.createNiceMock(Array.class);
        String[] dealIds = {"1", "2"};
        EasyMock.expect(rs.getArray("deal_ids")).andReturn(dealsArray).anyTimes();
        EasyMock.expect(dealsArray.getArray()).andReturn(dealIds).anyTimes();

        Array countriesArray = EasyMock.createNiceMock(Array.class);
        Integer[] countries = new Integer[] {94, 46};
        EasyMock.expect(rs.getArray("country_ids")).andReturn(countriesArray).anyTimes();
        EasyMock.expect(countriesArray.getArray()).andReturn(countries).anyTimes();

        Array citiesArray = EasyMock.createNiceMock(Array.class);
        Integer[] cities = new Integer[] {12345, 23456};
        EasyMock.expect(rs.getArray("city_ids")).andReturn(citiesArray).anyTimes();
        EasyMock.expect(citiesArray.getArray()).andReturn(cities).anyTimes();

        Array slotsArray = EasyMock.createNiceMock(Array.class);
        Integer[] slots = new Integer[] {14, 32};
        EasyMock.expect(rs.getArray("placement_slot_ids")).andReturn(slotsArray).anyTimes();
        EasyMock.expect(slotsArray.getArray()).andReturn(slots).anyTimes();

        Array inventoryTypeArray = EasyMock.createNiceMock(Array.class);
        String[] inventories = new String[] {"BROWSER", "APP"};
        EasyMock.expect(rs.getArray("inventory_types")).andReturn(inventoryTypeArray).anyTimes();
        EasyMock.expect(inventoryTypeArray.getArray()).andReturn(inventories).anyTimes();

        Array carrierIdArray = EasyMock.createNiceMock(Array.class);
        Long[] carrierIds = {};
        EasyMock.expect(rs.getArray("carrier_ids")).andReturn(carrierIdArray).anyTimes();
        EasyMock.expect(carrierIdArray.getArray()).andReturn(carrierIds).anyTimes();

        Array siteCatArray = EasyMock.createNiceMock(Array.class);
        String[] siteCats = new String[] {"PERFORMANCE", "FAMILY_SAFE"};
        EasyMock.expect(rs.getArray("site_categories")).andReturn(siteCatArray).anyTimes();
        EasyMock.expect(siteCatArray.getArray()).andReturn(siteCats).anyTimes();

        Array connTypeArray = EasyMock.createNiceMock(Array.class);
        String[] connTypes = new String[] {"WIFI"};
        EasyMock.expect(rs.getArray("connection_types")).andReturn(connTypeArray).anyTimes();
        EasyMock.expect(connTypeArray.getArray()).andReturn(connTypes).anyTimes();

        Array appStoreCatArray = EasyMock.createNiceMock(Array.class);
        Integer[] appStoreCats = {};
        EasyMock.expect(rs.getArray("app_store_categories")).andReturn(appStoreCatArray).anyTimes();
        EasyMock.expect(appStoreCatArray.getArray()).andReturn(appStoreCats).anyTimes();

        Array sdkVersionArray = EasyMock.createNiceMock(Array.class);
        String[] sdkVersions = {};
        EasyMock.expect(rs.getArray("sdk_versions")).andReturn(sdkVersionArray).anyTimes();
        EasyMock.expect(sdkVersionArray.getArray()).andReturn(sdkVersions).anyTimes();

        Array zipCodesArray = EasyMock.createNiceMock(Array.class);
        String[] zipCodes = {};
        EasyMock.expect(rs.getArray("zip_codes")).andReturn(zipCodesArray).anyTimes();
        EasyMock.expect(zipCodesArray.getArray()).andReturn(zipCodes).anyTimes();

        Array csIdsArray = EasyMock.createNiceMock(Array.class);
        Integer[] csIds = {};
        EasyMock.expect(rs.getArray("cs_ids")).andReturn(csIdsArray).anyTimes();
        EasyMock.expect(csIdsArray.getArray()).andReturn(csIds).anyTimes();

        Array dealFloorsArray = EasyMock.createNiceMock(Array.class);
        Double[] dealFloors = {0.3, 0.5};
        EasyMock.expect(rs.getArray("deal_floors")).andReturn(dealFloorsArray).anyTimes();
        EasyMock.expect(dealFloorsArray.getArray()).andReturn(dealFloors).anyTimes();
        
        Array accessTypesArray = EasyMock.createNiceMock(Array.class);
        String[] accessTypes = {"RIGHT_TO_FIRST_REFUSAL_DEAL", "PREFERRED_DEAL"};
        EasyMock.expect(rs.getArray("access_types")).andReturn(accessTypesArray).anyTimes();
        EasyMock.expect(accessTypesArray.getArray()).andReturn(accessTypes).anyTimes();

        Array todArray = EasyMock.createNiceMock(Array.class);
        Integer[] tods = {};
        EasyMock.expect(rs.getArray("scheduled_tods")).andReturn(todArray).anyTimes();
        EasyMock.expect(todArray.getArray()).andReturn(tods).anyTimes();

        Array placementAdTypeArray = EasyMock.createNiceMock(Array.class);
        String[] placementAdTypes = {};
        EasyMock.expect(rs.getArray("placement_ad_types")).andReturn(placementAdTypeArray).anyTimes();
        EasyMock.expect(placementAdTypeArray.getArray()).andReturn(placementAdTypes).anyTimes();


        ResultSetExpectationSetter.setExpectation(rs, "id", 1, Types.INTEGER);
        ResultSetExpectationSetter.setExpectation(rs, "lat_long_only", true, Types.BOOLEAN);
        ResultSetExpectationSetter.setExpectation(rs, "zip_code_only", false, Types.BOOLEAN);
        ResultSetExpectationSetter.setExpectation(rs, "ifa_only", false, Types.BOOLEAN);

        ResultSetExpectationSetter.setExpectation(rs, "data_vendor_id", 0, Types.INTEGER);
        ResultSetExpectationSetter.setExpectation(rs, "dmp_id", 0, Types.INTEGER);
        ResultSetExpectationSetter.setExpectation(rs, "dmp_filter_expression", null, Types.VARCHAR);

        ResultSetExpectationSetter.setExpectation(rs, "is_active", true, Types.BOOLEAN);
        ResultSetExpectationSetter.setExpectation(rs, "last_modified", new Timestamp(500), Types.TIMESTAMP);

        EasyMock.replay(osArray);
        EasyMock.replay(sitesArray);
        EasyMock.replay(dealsArray);
        EasyMock.replay(countriesArray);
        EasyMock.replay(citiesArray);
        EasyMock.replay(slotsArray);
        EasyMock.replay(inventoryTypeArray);
        EasyMock.replay(carrierIdArray);
        EasyMock.replay(siteCatArray);
        EasyMock.replay(connTypeArray);
        EasyMock.replay(appStoreCatArray);
        EasyMock.replay(sdkVersionArray);
        EasyMock.replay(zipCodesArray);
        EasyMock.replay(csIdsArray);
        EasyMock.replay(dealFloorsArray);
        EasyMock.replay(todArray);
        EasyMock.replay(placementAdTypeArray);

        EasyMock.replay(rs);

        IXPackageRepository.IXPackageReaderDelegate delegate = repository.new IXPackageReaderDelegate();
        delegate.beforeEachIteration();
        delegate.readRow(rs);
        delegate.afterEachIteration();

        Collection<IXPackageEntity> packageSet = repository.getIXPackageSet();
        Assert.assertEquals(packageSet.size(), 1);
        IXPackageEntity packageEntity = (IXPackageEntity) packageSet.toArray()[0];

        Assert.assertEquals(packageEntity.getId(), 1);
        Assert.assertEquals(packageEntity.getDmpId(), 0);
        Assert.assertEquals(packageEntity.getDmpVendorId(), 0);
        Assert.assertEquals(packageEntity.getDmpFilterSegmentExpression(), new HashSet<>());

        // build expected params
        Country e_country = new Country();
        e_country.init(ImmutableSet.copyOf(new Integer[] {94, 46}));

        City e_city = new City();
        e_city.init(ImmutableSet.copyOf(new Integer[] {12345, 23456}));

        DeviceOs e_os = new DeviceOs();
        e_os.init(ImmutableSet.copyOf(new Integer[] {3, 5}));

        SlotId e_slot = new SlotId();
        e_slot.init(ImmutableSet.copyOf(new Integer[] {14, 32}));

        InventoryType e_inventoryType = new InventoryType();
        e_inventoryType.init(ImmutableSet.copyOf(new InventoryTypeEnum[] {InventoryTypeEnum.BROWSER, InventoryTypeEnum.APP}));

        SiteCategory e_siteCategory = new SiteCategory();
        e_siteCategory.init(ImmutableSet.copyOf(new SiteCategoryEnum[] {SiteCategoryEnum.PERFORMANCE, SiteCategoryEnum.FAMILY_SAFE}));

        ConnectionType e_connectionType = new ConnectionType();
        e_connectionType.init(Arrays.asList(ConnectionTypeEnum.WIFI));

        LatlongPresent e_latLongPresent = new LatlongPresent();
        e_latLongPresent.init(true);

        Segment.Builder repoSegmentBuilder = new Segment.Builder();
        Segment e_segment = repoSegmentBuilder
                .addSegmentParameter(e_country)
                .addSegmentParameter(e_city)
                .addSegmentParameter(e_os)
                .addSegmentParameter(e_slot)
                .addSegmentParameter(e_inventoryType)
                .addSegmentParameter(e_siteCategory)
                .addSegmentParameter(e_connectionType)
                .addSegmentParameter(e_latLongPresent)
                .build();

        Assert.assertTrue(e_segment.isEqualTo(packageEntity.getSegment()));
    }

    @DataProvider(name = "OS Version Targeting JSONs")
    public Object[][] paramDataProviderForOsVersionTargeting() {

        Map<Integer, Range<Double>> mapWithSingleEntryWithNoTargeting = new HashMap<>();
        Map<Integer, Range<Double>> mapWithSingleEntryWithTargeting = new HashMap<>();
        Map<Integer, Range<Double>> mapWithSingleEntryWithTargetingIntegerRange = new HashMap<>();
        Map<Integer, Range<Double>> mapWithSingleEntryWithTargetingReversedRange = new HashMap<>();
        Map<Integer, Range<Double>> mapWithMultipleEntriesWithSingleTargeting = new HashMap<>();
        Map<Integer, Range<Double>> mapWithMultipleEntriesWithBothTargeting = new HashMap<>();

        mapWithSingleEntryWithNoTargeting.put(3, Range.all());
        mapWithSingleEntryWithTargeting.put(3, Range.closed(4.0, 5.0));
        mapWithSingleEntryWithTargetingIntegerRange.put(3, Range.closed(4.0, 5.0));
        mapWithSingleEntryWithTargetingReversedRange.put(3, Range.closed(4.0, 5.0));

        mapWithMultipleEntriesWithSingleTargeting.put(3, Range.closed(4.0, 5.0));
        mapWithMultipleEntriesWithSingleTargeting.put(5, Range.all());

        mapWithMultipleEntriesWithBothTargeting.put(3, Range.closed(4.0, 5.0));
        mapWithMultipleEntriesWithBothTargeting.put(5, Range.closed(7.0, 8.1));

        return new Object[][] {
                {"testNull", null, new HashMap<Short, Range<Double>>()},
                {"testEmpty", "", new HashMap<Short, Range<Double>>()},
                {"testEmptyJsonArray", "[]", new HashMap<Short, Range<Double>>()},
                {"testJsonArrayWithSingleEntryWithNoTargeting", "[{\"osId\":3, \"range\":[]}]", mapWithSingleEntryWithNoTargeting},
                {"testJsonArrayWithSingleEntryWithTargeting", "[{\"osId\":3, \"range\":[4.0, 5.0]}]", mapWithSingleEntryWithTargeting},
                {"testJsonArrayWithSingleEntryWithTargetingIntegerRange", "[{\"osId\":3, \"range\":[4, 5]}]", mapWithSingleEntryWithTargetingIntegerRange},
                {"testJsonArrayWithSingleEntryWithTargetingReversedRange", "[{\"osId\":3, \"range\":[5.0, 4.0]}]", mapWithSingleEntryWithTargetingReversedRange},
                {"testJsonArrayWithTwoEntriesWithSingleTargeting", "[{\"osId\":3, \"range\":[4.0, 5.0]}, {\"osId\":5, \"range\":[]}]", mapWithMultipleEntriesWithSingleTargeting},
                {"testJsonArrayWithTwoEntriesWithBothTargeting", "[{\"osId\":3, \"range\":[4.0, 5.0]}, {\"osId\":5, \"range\":[7.0, 8.1]}]", mapWithMultipleEntriesWithBothTargeting}
        };
    }

    @Test(dataProvider = "OS Version Targeting JSONs")
    public void testExtractOsVersionTargeting(final String useCaseName, String osVersionTargetingJson,
                                              final Map<Short, Range<Double>> metaData) throws Exception {
        Assert.assertTrue(metaData.equals(extractOsVersionTargeting(osVersionTargetingJson)));
    }

    @DataProvider(name = "Manuf Model Targeting JSONs")
    public Object[][] paramDataProviderForManufModelTargeting() {

        Map<Long, Pair<Boolean, Set<Long>>> mapWithSingleEntryWithNoTargetingInclusionIsTrue = new HashMap<>();
        Map<Long, Pair<Boolean, Set<Long>>> mapWithSingleEntryWithTargetingInclusionIsTrue = new HashMap<>();
        Map<Long, Pair<Boolean, Set<Long>>> mapWithTwoEntryWithTargetingSetForTwoAndInclusionSetForOne = new HashMap<>();
        Map<Long, Pair<Boolean, Set<Long>>> mapWithThreeEntriesWithTargetingSetForTwoAndInclusionSetForOne = new HashMap<>();
        Set<Long> emptySet = (new ImmutableSet.Builder<Long>()).build();

        mapWithSingleEntryWithNoTargetingInclusionIsTrue.put(32L, ImmutablePair.of(true, emptySet));
        mapWithSingleEntryWithTargetingInclusionIsTrue.put(32L, ImmutablePair.of(true, ImmutableSet.of(129L, 169L)));

        mapWithTwoEntryWithTargetingSetForTwoAndInclusionSetForOne.put(32L, ImmutablePair.of(true, ImmutableSet
            .of(129L, 169L)));
        mapWithTwoEntryWithTargetingSetForTwoAndInclusionSetForOne.put(7L, ImmutablePair.of(false, ImmutableSet.of(64L)));

        mapWithThreeEntriesWithTargetingSetForTwoAndInclusionSetForOne.put(32L, ImmutablePair.of(true, ImmutableSet
            .of(64L)));
        mapWithThreeEntriesWithTargetingSetForTwoAndInclusionSetForOne.put(7L, ImmutablePair.of(true, emptySet));
        mapWithThreeEntriesWithTargetingSetForTwoAndInclusionSetForOne.put(75L, ImmutablePair.of(false, ImmutableSet
            .of(129L, 169L)));

        return new Object[][] {
                {"testNull", null, new HashMap<Short, Range<Double>>()},
                {"testEmpty", "", new HashMap<Short, Range<Double>>()},
                {"testEmptyJsonArray", "[]", new HashMap<Short, Range<Double>>()},
                {"testJsonArrayWithSingleEntryWithNoTargetingInclusionIsTrue", "[{\"manufId\":32, \"modelIds\":[], \"incl\":\"true\"}]", mapWithSingleEntryWithNoTargetingInclusionIsTrue},
                {"testJsonArrayWithSingleEntryWithNoTargetingInclusionIsFalse", "[{\"manufId\":32, \"modelIds\":[], \"incl\":\"false\"}]", new HashMap<Short, Range<Double>>()},
                {"testJsonArrayWithSingleEntryWithTargetingInclusionIsTrue", "[{\"manufId\":32, \"modelIds\":[129, 169], \"incl\":\"true\"}]", mapWithSingleEntryWithTargetingInclusionIsTrue},
                {"testJsonArrayWithTwoEntriesWithTargetingSetForTwoAndInclusionSetForOne", "[{\"manufId\":32, \"modelIds\":[129, 169], \"incl\":\"true\"}, {\"manufId\":7, \"modelIds\":[64], \"incl\":\"false\"}]", mapWithTwoEntryWithTargetingSetForTwoAndInclusionSetForOne},
                {"testJsonArrayWithThreeEntriesWithTargetingSetForTwoAndInclusionSetForOne", "[{\"manufId\":32, \"modelIds\":[64], \"incl\":\"true\"}, {\"manufId\":7, \"modelIds\":[], \"incl\":\"true\"}, {\"manufId\":75, \"modelIds\":[129,169], \"incl\":\"false\"}]", mapWithThreeEntriesWithTargetingSetForTwoAndInclusionSetForOne}
        };
    }

    @Test(dataProvider = "Manuf Model Targeting JSONs")
    public void testExtractManufModelTargeting(final String useCaseName, String manufModelTargetingJson,
                                               final Map<Long, Map<Boolean, Set<Long>>> metaData) throws Exception {
        Assert.assertTrue(metaData.equals(extractManufModelTargeting(manufModelTargetingJson)));
    }

    @DataProvider(name = "Sdk Version Targeting JSONs")
    public Object[][] paramDataProviderForSdkVersionTargeting() {
        Pair<Boolean, Set<Integer>> emptyExclusion = ImmutablePair.of(true, ImmutableSet.of());
        Pair<Boolean, Set<Integer>> emptyInclusion = ImmutablePair.of(false, ImmutableSet.of());
        Pair<Boolean, Set<Integer>> filledExclusion = ImmutablePair.of(true, ImmutableSet.of(400, 600));
        Pair<Boolean, Set<Integer>> filledInclusion = ImmutablePair.of(false, ImmutableSet.of(400, 600));

        return new Object[][] {
            {"testNull", null, emptyExclusion},
            {"testEmpty", "", emptyExclusion},
            {"testBlank", " ", emptyExclusion},
            {"testInclusion", "{inclusion:[400, 600]}", filledInclusion},
            {"testEmptyInclusion", "{inclusion:[]}", emptyInclusion},
            {"testExclusion", "{exclusion:[400, 600]}", filledExclusion},
            {"testEmptyExclusion", "{exclusion:[]}", emptyExclusion},
            {"testInclusionPriority", "{inclusion:[], exclusion:[400,600]}", emptyInclusion},
            {"testMalformed", "{incl:[], exon:[400,600]}", emptyExclusion},
            {"testExceptionThrown", "{incl:[, exon[400,600]}", emptyExclusion},
            {"testInclusionWithFaultyValues", "{inclusion:[400, poop, 600]}", filledInclusion}
        };
    }

    @Test(dataProvider = "Sdk Version Targeting JSONs")
    public void testExtractSdkVersionTargeting(final String useCaseName, String sdkVersionTargetingJson,
        final Pair<Boolean, Set<Integer>> metaData) throws Exception {
        Assert.assertTrue(metaData.equals(extractSdkVersionTargeting(sdkVersionTargetingJson)));
    }

    @DataProvider(name = "Third Party Tracker Json List")
    public Object[][] paramThirdPartyTrackerJsonList() {
        final String VIEWABILITY_TRACKER = "ViewabilityTracker";
        final String AUDIENCE_VERIFICATION_TRACKER = "AudienceVerification";
        final String THIRD_PARTY_IMPRESSION_TRACKER = "ThirdPartyImpressionTracker";
        final String THIRD_PARTY_CLICK_TRACKER = "ThirdPartyClickTracker";

        final  Map<String, String> trackerMap = new HashMap<>();
        trackerMap.put(AUDIENCE_VERIFICATION_TRACKER, "<script>var text = '\"audience\":\"tracker\"';\n" + "var a = 5;\n"
                + "var b = 6;\n" + "var d = a + b;\n" + "var e = a/b;\n" + "</script>");
        trackerMap.put(THIRD_PARTY_CLICK_TRACKER, "<script>var text = '\"tpct\":\"tracker\"';\n" + "var a = 5;\n"
                + "var b = 6;\n" + "var d = a + b;\n" + "var e = a/b;\n" + "</script>");
        trackerMap.put(THIRD_PARTY_IMPRESSION_TRACKER, "<script>var text = '\"tpit\":\"tracker\"';\n" + "var a = 5;\n"
                + "var b = 6;\n" + "var d = a + b;\n" + "var e = a/b;\n" + "</script>");
        trackerMap.put(VIEWABILITY_TRACKER, "<script>var text = '\"viewability\":\"tracker\"';\n"
                + "var a = 5;\n" + "var b = 6;\n" + "var d = a + b;\n" + "var e = a/b;\n" + "</script>");

        final String trackerValidJsonStr  =  "{\"ViewabilityTracker\":\"<script>var text = "
                + "'\\\"viewability\\\":\\\"tracker\\\"';\\nvar a = 5;\\nvar b = 6;\\nvar d = a + b;\\nvar e = a/b;"
                + "\\n</script>\",\"AudienceVerification\":\"<script>var text = '\\\"audience\\\":\\\"tracker\\\"';"
                + "\\nvar a = 5;\\nvar b = 6;\\nvar d = a + b;\\nvar e = a/b;\\n</script>\","
                + "\"ThirdPartyImpressionTracker\":\"<script>var text = '\\\"tpit\\\":\\\"tracker\\\"';\\nvar a = 5;"
                + "\\nvar b = 6;\\nvar d = a + b;\\nvar e = a/b;\\n</script>\","
                + "\"ThirdPartyClickTracker\":\"<script>var text = '\\\"tpct\\\":\\\"tracker\\\"';\\nvar a = 5;\\nvar"
                + " b = 6;\\nvar d = a + b;\\nvar e = a/b;\\n</script>\"}";

        final String validJsonList[]  = {trackerValidJsonStr, trackerValidJsonStr};
        final String validEmptyJsonList[] = {"{}", "{}", "{}"};
        final String nullList[] = {null, null, null};
        final String emptyStringList[] = {"", "", ""};
        final String nullWithEmptyStringList[] = {"", null, "", null, null};
        final String emptyNullAndValidJsonList[]  = {trackerValidJsonStr, "", "{}", null};
        final String invalidJsonList[]  = {"{", "}", "+123", null};
        final String invalidAndValidJsonList[]  = {"{", trackerValidJsonStr, "+123", null, "{}", ""};



        return new Object[][] {
                {"ValidJsonList", validJsonList, trackerMap},
                {"EmptyJsonObjectList", validEmptyJsonList, trackerMap},
                {"nullList", nullList, trackerMap},
                {"emptyStringList", emptyStringList, trackerMap},
                {"nullWithEmptyStringList", nullWithEmptyStringList, trackerMap},
                {"nullCheck", null, trackerMap},
                {"emptyNullAndValidJsonList", emptyNullAndValidJsonList, trackerMap},
                {"invalidJsonList", invalidJsonList, trackerMap},
                {"invalidAndValidJsonList", invalidAndValidJsonList, trackerMap},
        };
    }


    @Test(dataProvider = "Third Party Tracker Json List")
    public void testThirdPartyTrackerJson(final String useCaseName, final String[] thirdPartyTrackerJsonList,
            final  Map<String, String> trackerMap) throws Exception {

        Configuration mockConfig = createMock(Configuration.class);
        Logger mockLogger = createMock(Logger.class);
        EasyMock.expect(mockConfig.getString("query")).andReturn("select * from ix_packages;").once();
        EasyMock.expect(mockConfig.getInt("initialDelay")).andReturn(1).once();
        EasyMock.expect(mockConfig.getInt("refreshTime")).andReturn(300).once();
        EasyMock.replay(mockConfig);

        IXPackageRepository repository = new IXPackageRepository();
        repository.init(mockLogger, NoOpDataSource.getNoOpDataSource(), mockConfig, "dummy");

        List<Map<String, String>>
                thridPartyTrackerMapList = repository.getThirdPartyTrackerMapList(thirdPartyTrackerJsonList);
        System.out.println(thridPartyTrackerMapList.toString());
        thridPartyTrackerMapList.forEach(t-> {
            Assert.assertTrue(null != t);
            if (t.isEmpty()) {
                // when jsonStrin is notValidJson, validJsonButEmpty, emptyString or null  then the trackers will be null only
                trackerMap.forEach((k,v)-> Assert.assertEquals(t.get(k), null));
            } else {
                trackerMap.forEach((k, v) -> Assert.assertEquals(t.get(k), v));
            }
        });

    }
}
*/
