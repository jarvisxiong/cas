package com.inmobi.adserve.channels.adnetworks.ix;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.user.photon.datatypes.attribute.brand.BrandAttributes;
import com.inmobi.user.photon.datatypes.commons.attribute.IntAttribute;
import com.inmobi.user.photon.datatypes.commons.attribute.ValueProperties;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.listenable.AbstractListenableFuture;

/**
 * Created by avinash.kumar on 6/9/16.
 */
public class EnrichmentHelperTest {

    public final InspectorStats inspectorStats = createNiceMock(InspectorStats.class);
    public final long startTime = 0;
    public final long curTime = 5;
    public final int timeout = 10;

    @BeforeTest
    public void before() {
        mockStaticNice(InspectorStats.class);
    }


    @DataProvider(name = "Enrich CSIIds when Brand Attribute is Not Null ANd umpCSITags Cant be Null")
    public Object[][] vastPositiveAdBuildingDataProvider() {
        final Set<Integer> bluekaiCSIIds = new HashSet<>();
        bluekaiCSIIds.add(1);
        bluekaiCSIIds.add(3);
        bluekaiCSIIds.add(8);

        final Set<Integer> geoCSIIds = new HashSet<>();
        geoCSIIds.add(11);
        geoCSIIds.add(16);
        geoCSIIds.add(19);

        final Set<Integer> pdsCSIIds = new HashSet<>();
        pdsCSIIds.add(23);
        pdsCSIIds.add(26);
        pdsCSIIds.add(28);

        final Set<Integer> sasCSiTags1 = new HashSet<>();
        sasCSiTags1.add(77);


        return new Object[][]{
                {"AllisNull", sasCSiTags1, bluekaiCSIIds, geoCSIIds, pdsCSIIds},
                {"valueType1", sasCSiTags1, bluekaiCSIIds, geoCSIIds, null},
                {"valueType2", sasCSiTags1, bluekaiCSIIds, null, pdsCSIIds},
                {"valueType3", sasCSiTags1, bluekaiCSIIds, null, null},
                {"valueType4", sasCSiTags1, null, geoCSIIds, pdsCSIIds},
                {"valueType5", sasCSiTags1, null, geoCSIIds, null},
                {"valueType6", sasCSiTags1, null, null, pdsCSIIds},
                {"valueType7", sasCSiTags1, null, null, null},
                {"valueType8", null, bluekaiCSIIds, geoCSIIds, null},
                {"valueType9", null, bluekaiCSIIds, null, pdsCSIIds},
                {"valueType10", null, bluekaiCSIIds, null, null},
                {"valueType11", null, null, geoCSIIds, pdsCSIIds},
                {"valueType12", null, null, geoCSIIds, null},
                {"valueType13", null, null, null, pdsCSIIds},
                {"valueType14", null, null, null, null},
        };
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "Enrich CSIIds when Brand Attribute is Not Null ANd umpCSITags Cant be Null")
    public void testEnrichCSIIdsWhenBrandAttrIsNotNull(final String testName, Set<Integer> sasCsiTags, Set<Integer> bluekaiCSIIds,
             Set<Integer> geoCookiesCSIIds, Set<Integer> pdsCSIIds) {
        try {
            final BrandAttributes brandAttr = new BrandAttributes();
            setCSIIds(0, bluekaiCSIIds, brandAttr);
            setCSIIds(1, geoCookiesCSIIds, brandAttr);
            setCSIIds(2, pdsCSIIds, brandAttr);

            final ListenableFuture<BrandAttributes> mockAttrFutureMock = createNiceMock(AbstractListenableFuture.class);
            expect(mockAttrFutureMock.get(EnrichmentHelper.getWaitTime(startTime, curTime), TimeUnit.MILLISECONDS)).andReturn(brandAttr).anyTimes();
            replay(mockAttrFutureMock);

            final Set<Integer> expectedSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(sasCsiTags)) {
                expectedSet.addAll(sasCsiTags);
            }
            if (CollectionUtils.isNotEmpty(bluekaiCSIIds)) {
                expectedSet.addAll(bluekaiCSIIds);
            }
            if (CollectionUtils.isNotEmpty(geoCookiesCSIIds)) {
                expectedSet.addAll(geoCookiesCSIIds);
            }if (CollectionUtils.isNotEmpty(pdsCSIIds)) {
                expectedSet.addAll(pdsCSIIds);
            }
            final Set<Integer> actualSet = EnrichmentHelper.getCSIIds(sasCsiTags, startTime, curTime, mockAttrFutureMock);
            System.out.println("prcessed : " + actualSet + ", expected" + expectedSet);
            Assert.assertEquals(actualSet, expectedSet);
        } catch (InterruptedException e) {
            Assert.assertFalse(true);
            e.printStackTrace();
        } catch (ExecutionException e) {
            Assert.assertFalse(true);
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void setCSIIds(int type, Set<Integer> csiIds, BrandAttributes brandAttr) {
        final Map<Integer, ValueProperties> idValueMap = new HashMap<>();
        final Map<String, Map<Integer, ValueProperties>> sourceValueMap = new HashMap<>();
        final IntAttribute intAttr = new IntAttribute();

        if (null != csiIds) {
            csiIds.forEach(t -> idValueMap.put(t, new ValueProperties()));
            sourceValueMap.put("BRAND", idValueMap);
            intAttr.setValueMap(sourceValueMap);
        }

        switch (type) {
            case 0: brandAttr.setBluekai_csids(null != csiIds ? intAttr : null);
                break;
            case 1: brandAttr.setGeocookie_csids(null != csiIds ? intAttr : null);
                break;
            case 2: brandAttr.setPds_csids(null != csiIds ? intAttr : null);
        }
    }

}