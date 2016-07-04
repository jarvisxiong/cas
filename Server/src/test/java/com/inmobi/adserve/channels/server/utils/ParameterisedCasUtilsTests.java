package com.inmobi.adserve.channels.server.utils;

import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android;
import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.iOS;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.segment.impl.AdTypeEnum;

public class ParameterisedCasUtilsTests {

    @DataProvider(name = "Minimum OS Version for Video")
    public Object[][] dataProviderForMinimumOSVersionForVideoTests() throws ConfigurationException {
        final double minimumSupportedAndroidVersionForVideo = 4.2;
        final double minimumSupportedIOSVersionForVideo = 8;

        final Configuration serverConfig = new BaseConfiguration();
        serverConfig.addProperty(CasUtils.MINIMUM_SUPPORTED_ANDROID_VERSION_FOR_VIDEO_CONFIG_KEY, minimumSupportedAndroidVersionForVideo);
        serverConfig.addProperty(CasUtils.MINIMUM_SUPPORTED_IOS_VERSION_FOR_VIDEO_CONFIG_KEY, minimumSupportedIOSVersionForVideo);

        return new Object[][] {
                {"testNullOsMajorVersionStr", 0, null, null, false},
                {"testEmptyOsMajorVersionStr", 0, "", null, false},
                {"testNonAndroidIOSOsId", 0, "0", null, false},
                // Android Tests
                {"testAndroidBelowMin", Android.getValue(), String.valueOf(minimumSupportedAndroidVersionForVideo - 1), serverConfig, false},
                {"testAndroidAtMin", Android.getValue(), String.valueOf(minimumSupportedAndroidVersionForVideo), serverConfig, true},
                {"testAndroidAboveMin", Android.getValue(), String.valueOf(minimumSupportedAndroidVersionForVideo + 1), serverConfig, true},
                // IOS Tests
                {"testIOSBelowMin", iOS.getValue(), String.valueOf(minimumSupportedIOSVersionForVideo - 1), serverConfig, false},
                {"testIOSAtMin", iOS.getValue(), String.valueOf(minimumSupportedIOSVersionForVideo), serverConfig, true},
                {"testIOSAboveMin", iOS.getValue(), String.valueOf(minimumSupportedIOSVersionForVideo + 1), serverConfig, true},
        };
    }

    @Test(dataProvider = "Minimum OS Version for Video")
    public void testMinimumOSVersionForVideo(final String testCaseName, final int osId, final String osMajorVersionStr, final Configuration serverConfig, final boolean result) {
        Assert.assertEquals(result, CasUtils.checkMinimumOSVersionForAdType(osId, osMajorVersionStr, serverConfig, AdTypeEnum.VAST));
    }
}
