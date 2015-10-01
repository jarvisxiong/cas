package com.inmobi.brandtest.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;

import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.NetworkAdPoolImpressionInfo;

public class BrandValidator {

    public static void AssertIfParamsArePresent(final AdPoolResponse adPoolResponse,
            final HashMap<String, String> validationsMap) {
        final BrandResponseDeserializer bdeser = new BrandResponseDeserializer(adPoolResponse);
        final List<NetworkAdPoolImpressionInfo> listOfImpressionInfo = bdeser.getListOfAllAds();
        final int counter = 1;
        final Iterator<String> x = validationsMap.keySet().iterator();
        if (validationsMap.keySet() != null) {
            while (x.hasNext()) {
                Assert.assertTrue(adPoolResponse.toString().contains(validationsMap.get(x.next()))
                        || listOfImpressionInfo.contains(validationsMap.get(x.next())));
            }
        }
    }


    public static void AssertIfParamsAreAbsent(final AdPoolResponse adPoolResponse,
            final HashMap<String, String> validationsMap) {
        final BrandResponseDeserializer bdeser = new BrandResponseDeserializer(adPoolResponse);
        final List<NetworkAdPoolImpressionInfo> listOfImpressionInfo = bdeser.getListOfAllAds();
        final int counter = 1;
        final Iterator<String> x = validationsMap.keySet().iterator();
        if (validationsMap.keySet() != null) {
            while (x.hasNext()) {
                Assert.assertTrue(adPoolResponse.toString().contains(validationsMap.get(x.next()))
                        || listOfImpressionInfo.contains(validationsMap.get(x.next())));
            }
        }

    }
}
