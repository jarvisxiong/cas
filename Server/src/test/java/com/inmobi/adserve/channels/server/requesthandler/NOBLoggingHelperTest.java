package com.inmobi.adserve.channels.server.requesthandler;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.adpool.IntegrationOrigin;
import com.inmobi.adserve.adpool.IntegrationType;

/**
 * Created by ishan.bhatnagar on 31/12/15.
 */
public class NOBLoggingHelperTest {

    @DataProvider(name = "IntegrationFamilyDataProvider")
    public Object[][] paramIntegrationFamilyDataProvider() {
        return new Object[][] {
            {"testANDROID_SDK", IntegrationType.ANDROID_SDK, "Android Sdk"},
            {"testIOS_SDK", IntegrationType.IOS_SDK, "iOS Sdk"},
            {"testWINDOWS_CSHARP_SDK", IntegrationType.WINDOWS_CSHARP_SDK, "Windows C# Sdk"},
            {"testWINDOWS_JS_SDK", IntegrationType.WINDOWS_JS_SDK, "Windows JS Sdk"},
            {"testJSAC", IntegrationType.JSAC, "JS AdCode"},
            {"testPHP", IntegrationType.PHP, "PHP"},
            {"testPERL", IntegrationType.PERL, "PERL"},
            {"testJSP", IntegrationType.JSP, "JSP"},
            {"testASP", IntegrationType.ASP, "ASP"},
            {"testASP_NET", IntegrationType.ASP_NET, "ASP.net"},
            {"testRUBY", IntegrationType.RUBY, "RUBY"},
            {"testHTML_OLD", IntegrationType.HTML_OLD, "Old Html"},
            {"testSPECS", IntegrationType.SPECS, "SPECS"},
            {"testANDROID_API", IntegrationType.ANDROID_API, "Android"},
            {"testIOS_API", IntegrationType.IOS_API, "iOS"},
            {"testUNKNOWN", IntegrationType.UNKNOWN, "Unknown"},
            {"testNULL", null, "Unknown"}
        };
    }

    @Test(dataProvider = "IntegrationFamilyDataProvider")
    public void testGetIntegrationFamily(final String testCase, final IntegrationType integrationType,
            final String expectedFamilyName) throws Exception {
        Assert.assertTrue(NOBLoggingHelper.getIntegrationFamily(integrationType).equalsIgnoreCase(expectedFamilyName));
    }

    @DataProvider(name = "IntegrationOriginDataProvider")
    public Object[][] paramIntegrationOriginDataProvider() {
        return new Object[][] {
                {"testCLIENT", IntegrationOrigin.CLIENT, "cl"},
                {"testSERVER", IntegrationOrigin.SERVER, "svr"},
                {"testUNKNOWN", IntegrationOrigin.UNKNOWN, "uk"},
                {"testNULL", null, "uk"}
        };
    }

    @Test(dataProvider = "IntegrationOriginDataProvider")
    public void testGetIntegrationOrigin(final String testCase, final IntegrationOrigin integrationOrigin,
            final String expectedOrigin) throws Exception {
        Assert.assertTrue(NOBLoggingHelper.getIntegrationOrigin(integrationOrigin).equalsIgnoreCase(expectedOrigin));
    }

    @DataProvider(name = "IntegrationVersionDataProvider")
    public Object[][] paramIntegrationVersionDataProvider() {
        return new Object[][] {
                {"testNormal", 451, "4.5.1"},
                {"testNull", 0, "0"},
                {"testNormalSize2", 45, "4.5"}
        };
    }

    @Test(dataProvider = "IntegrationVersionDataProvider")
    public void testGetIntegrationVersion(final String testCase, final int integrationVersion,
            final String expectedVersionStr) throws Exception {
        System.out.println(NOBLoggingHelper.getIntegrationVersion(integrationVersion));
        Assert.assertTrue(NOBLoggingHelper.getIntegrationVersion(integrationVersion).equalsIgnoreCase(expectedVersionStr));
    }

    @DataProvider(name = "IntegrationMethodDataProvider")
    public Object[][] paramIntegrationMethodDataProvider() {
        return new Object[][] {
                {"testSDK", IntegrationMethod.SDK, "sdk"},
                {"testAPI", IntegrationMethod.API, "api"},
                {"testAD_CODE", IntegrationMethod.AD_CODE, "adc"},
                {"testUNKNOWN", IntegrationMethod.UNKNOWN, "uk"},
                {"testNULL", null, "uk"}
        };
    }

    @Test(dataProvider = "IntegrationMethodDataProvider")
    public void testGetIntegrationMethod(final String testCase, final IntegrationMethod adPoolIntegrationMethod,
            final String expecteMethod) throws Exception {
        Assert.assertTrue(NOBLoggingHelper.getIntegrationMethod(adPoolIntegrationMethod).equalsIgnoreCase(expecteMethod));
    }
}