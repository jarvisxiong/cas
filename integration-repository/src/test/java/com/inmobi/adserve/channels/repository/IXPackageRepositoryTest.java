package com.inmobi.adserve.channels.repository;

import com.google.common.collect.ImmutableSet;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.phoenix.batteries.data.test.NoOpDataSource;
import com.inmobi.phoenix.batteries.data.test.ResultSetExpectationSetter;
import com.inmobi.segment.Segment;
import com.inmobi.segment.impl.Country;
import com.inmobi.segment.impl.DeviceOs;
import com.inmobi.segment.impl.InventoryType;
import com.inmobi.segment.impl.InventoryTypeEnum;
import com.inmobi.segment.impl.LatlongPresent;
import com.inmobi.segment.impl.NetworkType;
import com.inmobi.segment.impl.SiteCategory;
import com.inmobi.segment.impl.SiteCategoryEnum;
import com.inmobi.segment.impl.SlotId;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createNiceMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.powermock.api.easymock.PowerMock.createMock;

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
        String[] dealIds = {"1","2"};
        EasyMock.expect(rs.getArray("deal_ids")).andReturn(dealsArray).anyTimes();
        EasyMock.expect(dealsArray.getArray()).andReturn(dealIds).anyTimes();

        Array countriesArray = EasyMock.createNiceMock(Array.class);
        Integer[] countries = new Integer[] {94, 46};
        EasyMock.expect(rs.getArray("country_ids")).andReturn(countriesArray).anyTimes();
        EasyMock.expect(countriesArray.getArray()).andReturn(countries).anyTimes();

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
        String[] connTypes = new String[] {"WIFI", "NON_WIFI"};
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
        Double[] dealFloors = {};
        EasyMock.expect(rs.getArray("deal_floors")).andReturn(dealFloorsArray).anyTimes();
        EasyMock.expect(dealFloorsArray.getArray()).andReturn(dealFloors).anyTimes();

        Array todArray = EasyMock.createNiceMock(Array.class);
        Integer[] tods = {};
        EasyMock.expect(rs.getArray("scheduled_tods")).andReturn(todArray).anyTimes();
        EasyMock.expect(todArray.getArray()).andReturn(tods).anyTimes();

        Array placementAdTypeArray = EasyMock.createNiceMock(Array.class);
        String[] placementAdTypes = {};
        EasyMock.expect(rs.getArray("placement_ad_types")).andReturn(placementAdTypeArray).anyTimes();
        EasyMock.expect(placementAdTypeArray.getArray()).andReturn(placementAdTypes).anyTimes();


        ResultSetExpectationSetter.setExpectation(rs, "id", 1L, Types.BIGINT);
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
        EasyMock.replay(slotsArray);
        EasyMock.replay(inventoryTypeArray);
        EasyMock.replay(carrierIdArray);
        EasyMock.replay(siteCatArray);
        EasyMock.replay(connTypeArray);
        EasyMock.replay(appStoreCatArray);
        EasyMock.replay(sdkVersionArray);
        EasyMock.replay(zipCodesArray);
        EasyMock.replay(csIdsArray);
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

        Assert.assertEquals(packageEntity.getId(), 1L);
        Assert.assertEquals(packageEntity.getDmpId(), 0);
        Assert.assertEquals(packageEntity.getDmpVendorId(), 0);
        Assert.assertEquals(packageEntity.getDmpFilterSegmentExpression(), new HashSet<>());

        // build expected params
        Country e_country = new Country();
        e_country.init(ImmutableSet.copyOf(new Integer[] {94, 46}));

        DeviceOs e_os = new DeviceOs();
        e_os.init(ImmutableSet.copyOf(new Integer[] {3, 5}));

        SlotId e_slot = new SlotId();
        e_slot.init(ImmutableSet.copyOf(new Integer[] {14, 32}));

        InventoryType e_inventoryType = new InventoryType();
        e_inventoryType.init(ImmutableSet.copyOf(new InventoryTypeEnum[] {InventoryTypeEnum.BROWSER, InventoryTypeEnum.APP}));

        SiteCategory e_siteCategory = new SiteCategory();
        e_siteCategory.init(ImmutableSet.copyOf(new SiteCategoryEnum[] {SiteCategoryEnum.PERFORMANCE, SiteCategoryEnum.FAMILY_SAFE}));

        NetworkType e_networkType = new NetworkType();
        e_networkType.init(ImmutableSet.copyOf(new Integer[] {0, 1}));

        LatlongPresent e_latLongPresent = new LatlongPresent();
        e_latLongPresent.init(true);

        Segment.Builder repoSegmentBuilder = new Segment.Builder();
        Segment e_segment = repoSegmentBuilder
                .addSegmentParameter(e_country)
                .addSegmentParameter(e_os)
                .addSegmentParameter(e_slot)
                .addSegmentParameter(e_inventoryType)
                .addSegmentParameter(e_siteCategory)
                .addSegmentParameter(e_networkType)
                .addSegmentParameter(e_latLongPresent)
                .build();

        Assert.assertTrue(e_segment.isEqualTo(packageEntity.getSegment()));
    }
}
