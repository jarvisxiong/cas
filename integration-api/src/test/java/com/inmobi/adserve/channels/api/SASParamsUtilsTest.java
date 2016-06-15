package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.channels.api.SASParamsUtils.MIN_SDK_WITH_MRAID;
import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android;
import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.iOS;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.IntegrationDetails;

public class SASParamsUtilsTest {


    @DataProvider(name = "deeplinkingDP")
    public Object[][] deeplinkingDP() {
        final String faultyOsMajorVer = "dummy";
        final String preIOS9_2 = "9.1";
        final String IOS9_2 = "9.2";

        final int oldSDK = MIN_SDK_WITH_MRAID - 1;
        final int firstSDKWithMRAID = MIN_SDK_WITH_MRAID;
        final int sdk500 = 500;
        final int futureSDK = 1500;

        return new Object[][] {
                {"NonSDK.1", false, 0, 0, null, false},
                {"NonSDK.2", false, 1, 0, null, false},
                {"NonSDK.3", false, 0, 1, null, false},
                {"NonSDK.4", false, 0, 0, faultyOsMajorVer, false},

                {"SDK.Android.OldSDK", true, oldSDK, Android.getValue(), null, false},
                {"SDK.Android.OldSDK.faultyOsMajorVer", true, oldSDK, Android.getValue(), faultyOsMajorVer, false},
                {"SDK.Android.firstSDKWithMRAID", true, firstSDKWithMRAID, Android.getValue(), null, true},
                {"SDK.Android.firstSDKWithMRAID.faultyOsMajorVer", true, firstSDKWithMRAID, Android.getValue(), faultyOsMajorVer, true},
                {"SDK.Android.sdk500", true, sdk500, Android.getValue(), null, true},
                {"SDK.Android.sdk500.faultyOsMajorVer", true, sdk500, Android.getValue(), faultyOsMajorVer, true},
                {"SDK.Android.futureSDK", true, futureSDK, Android.getValue(), null, true},
                {"SDK.Android.futureSDK.faultyOsMajorVer", true, futureSDK, Android.getValue(), faultyOsMajorVer, true},

                {"SDK.IOS.OldSDK", true, oldSDK, iOS.getValue(), null, false},
                {"SDK.IOS.OldSDK.faultyOsMajorVer", true, oldSDK, iOS.getValue(), faultyOsMajorVer, false},
                {"SDK.IOS.firstSDKWithMRAID", true, firstSDKWithMRAID, iOS.getValue(), null, false},
                {"SDK.IOS.firstSDKWithMRAID.faultyOsMajorVer", true, firstSDKWithMRAID, iOS.getValue(), faultyOsMajorVer, false},
                {"SDK.IOS.sdk500", true, sdk500, iOS.getValue(), null, true},
                {"SDK.IOS.sdk500.faultyOsMajorVer", true, sdk500, iOS.getValue(), faultyOsMajorVer, true},
                {"SDK.IOS.futureSDK", true, futureSDK, iOS.getValue(), null, true},
                {"SDK.IOS.futureSDK.faultyOsMajorVer", true, futureSDK, iOS.getValue(), faultyOsMajorVer, true},
                {"SDK.IOS.preIOS9_2.sdk500", true, sdk500, iOS.getValue(), preIOS9_2, true},
                {"SDK.IOS.preIOS9_2.futureSDK", true, futureSDK, iOS.getValue(), preIOS9_2, true},
                {"SDK.IOS.IOS9_2.sdk500", true, sdk500, iOS.getValue(), IOS9_2, true},
                {"SDK.IOS.IOS9_2.futureSDK", true, futureSDK, iOS.getValue(), IOS9_2, true},
                {"SDK.IOS.firstSDKWithMRAID", true, firstSDKWithMRAID, iOS.getValue(), null, false},
                {"SDK.IOS.firstSDKWithMRAID.faultyOsMajorVer", true, firstSDKWithMRAID, iOS.getValue(), faultyOsMajorVer, false},
                {"SDK.IOS.preIOS9_2.firstSDKWithMRAID", true, firstSDKWithMRAID, iOS.getValue(), preIOS9_2, true},
                {"SDK.IOS.IOS9_2.firstSDKWithMRAID", true, firstSDKWithMRAID, iOS.getValue(), IOS9_2, false},
        };
    }

    @Test(dataProvider = "deeplinkingDP")
    public void testIsDeeplinkingSupported(final String testCase, final boolean isSDK, final int intgVersion,
            final int osId, final String osMajorVer, final boolean expected) throws Exception {

        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setRequestFromSDK(isSDK);
        sasParams.setOsId(osId);
        sasParams.setOsMajorVersion(osMajorVer);

        final IntegrationDetails details = new IntegrationDetails();
        details.setIntegrationVersion(intgVersion);
        sasParams.setIntegrationDetails(details);
        
        Assert.assertEquals(SASParamsUtils.isDeeplinkingSupported(sasParams), expected);
    }

}