package com.inmobi.adserve.channels.repository;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.spi.RootLogger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created by ishan.bhatnagar on 09/04/16.
 */
public class ParameterisedWapSiteUACRepositoryTest {

    @DataProvider(name = "Init Override Data Provider")
    public Object[][] dataProviderForMinimumOSVersionForVideoTests() throws Exception {

        final Configuration config1 = new BaseConfiguration();
        config1.addProperty("override.site.someSite.bundleId", "bundle");
        config1.addProperty("override.site.someSite.marketId", "market");
        config1.addProperty("override.site.someSite.siteUrl", "url");

        final Map<String, Map<String, String>> expectedOutput1 = new HashMap<>();
        final Map<String, String> temp1 = new HashMap<>();
        temp1.put("bundleId", "bundle");
        temp1.put("marketId", "market");
        temp1.put("siteUrl", "url");
        expectedOutput1.put("someSite", temp1);

        final Configuration config2 = new BaseConfiguration();
        config2.addProperty("override.site.someSite.bundleId.dummy", "bundle");

        final Configuration config3 = new BaseConfiguration();
        config3.addProperty("override.site.someSite", "bundle");

        final Configuration config4 = new BaseConfiguration();
        config4.addProperty("override.site.someSite.unknownField", "bundle");

        return new Object[][] {
                {"testPositive", config1, expectedOutput1},
                {"testNegativeFaultyKeyStructure", config2, new HashMap<>()},
                {"testNegativeFaultyKeyStructure2", config3, new HashMap<>()},
                {"testNegativeUnsupportedKey", config4, new HashMap<>()},
        };
    }

    @Test(dataProvider = "Init Override Data Provider")
    public void testInitOverrides(final String testCaseName, final Configuration config,
            final Map<String, Map<String, String>> expectedOutput) {

        final WapSiteUACRepository wapSiteUACRepository = new WapSiteUACRepository();
        wapSiteUACRepository.initOverrides(config, new RootLogger(null));
        Assert.assertEquals(wapSiteUACRepository.overrides, expectedOutput);
    }

}
