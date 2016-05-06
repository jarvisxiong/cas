package com.inmobi.castest.dcptests;

import org.apache.commons.codec.binary.Base64;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.inmobi.castest.api.LogLines;
import com.inmobi.castest.casconfenums.def.CasConf.LogStringParams;
import com.inmobi.castest.casconfenums.impl.LogStringConf;
import com.inmobi.castest.commons.generichelper.LogParserHelper;
import com.inmobi.castest.dataprovider.FenderDataProvider;
import com.inmobi.castest.utils.common.ResponseBuilder;

public class DCPTest {

    private String searchStringInLog = new String();
    private String parserOutput = new String();

    @Test(testName = "Test1_13_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_13_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_33_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_33_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_17_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_17_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_19_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_19_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_36_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_36_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "GoogleAdx", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_01(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_AD_SERVED));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "DMG", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_02(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "AMOAD", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_03(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_AD_SERVED));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_1_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_1_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_3_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_3_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_4_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_4_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_5_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_5_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_6_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_6_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_7_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_7_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_8_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_8_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_9_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_9_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_10_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_10_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_11_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_11_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TERMINATE_CONFIG_SEARCH));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_12_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_12_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TERMINATE_CONFIG_SEARCH));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_15_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_15_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_16_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_16_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_17_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_17_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_18_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_18_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_19_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_19_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_21_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_21_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_33_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_33_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";
        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_33_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_33_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_34_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_34_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_34_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_34_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_35_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_35_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_35_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_35_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_36_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_36_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_36_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_36_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_37_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_37_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_37_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_37_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_38_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_38_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "response is";
        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_38_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_38_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_SDK500_NO_AD_PROG_369", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_SDK500_NO_AD_PROG_369(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput =
                LogParserHelper.logParser("sdk-version : a500", "Sending No ads",
                        "Wrapping in JSON for SDK > 500. Wrapped Response is: {\"requestId\":\"test\",\"ads\":[]}");

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_SDK500_AD_PROG_369", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_SDK500_AD_PROG_369(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("sdk-version : a500");

        final LogLines responseLogLine =
                LogParserHelper.queryForLogs("Wrapping in JSON for SDK > 500. Wrapped Response is:");
        final String responseGuid = responseLogLine.applyRegex("test");
        String pubContent = responseLogLine.applyRegex("\\{.+\\}", "pubContent\":\".+\"\\}", ":\".+\"", "\".+\"");
        final String googleAdxStaticTag =
                "<script type=\"text/javascript\">google_ad_client = \"ca-pub-4422296448758371"
                        + "\";google_ad_slot = \"9805306843\";google_ad_width = 320;google_ad_height = 50;</script>";

        if (null == responseGuid || null == pubContent) {
            parserOutput = "FAIL";
        }
        pubContent = new String(Base64.decodeBase64(pubContent));

        Reporter.log(parserOutput, true);

        Assert.assertEquals(pubContent.contains(googleAdxStaticTag), true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_SDK530_NON_NATIVE_DCP_WITHOUT_ENCRYPTION", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_SDK530_NON_NATIVE_DCP_WITHOUT_ENCRYPTION(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("sdk-version : a530");

        final LogLines responseLogLine =
            LogParserHelper.queryForLogs("Wrapping in JSON for SDK > 500. Wrapped Response is:");
        final String responseGuid = responseLogLine.applyRegex("test");
        String pubContent = responseLogLine.applyRegex("\\{.+\\}", "pubContent\":\".+\"\\}", ":\".+\"", "\".+\"");
        final String googleAdxStaticTag =
            "<html><head><title><\\/title><meta name=\\\"viewport\\\" content=\\\"user-scalable=0, minimum-scale=1.0,"
                + " maximum-scale=1.0\\\"\\/><style type=\\\"text\\/css\\\">body {margin: 0px; overflow: hidden;} "
                + "<\\/style><\\/head><body><script type=\\\"text\\/javascript\\\" src=\\\"mraid"
                + ".js\\\"><\\/script><script type=\\\"text\\/javascript\\\">google_ad_client = "
                + "\\\"ca-pub-4422296448758371\\\";google_ad_slot = \\\"9805306843\\\";google_ad_width = 320;"
                + "google_ad_height = 50;<\\/script><script type=\\\"text\\/javascript\\\" src=\\\"\\/\\/pagead2"
                + ".googlesyndication.com\\/pagead\\/show_ads.js\\\"><\\/script>";

        if (null == responseGuid || null == pubContent) {
            parserOutput = "FAIL";
        }
        Reporter.log(parserOutput, true);
        Assert.assertEquals(pubContent.contains(googleAdxStaticTag), true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // Commented till taboola response is hosted
    /*@Test(testName = "TEST1_SDK500_NATIVE_AD", dataProvider = "fender_dcp_dp", dataProviderClass =
            FenderDataProvider.class)
    public void TEST1_SDK500_NATIVE_AD(final String x, final ResponseBuilder responseBuilder)
            throws Exception {

        parserOutput = LogParserHelper.logParser("sdk-version : a500",
                "Rewrapping native JSON for DCP traffic. Wrapped Response is:");

        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_SDK450_NATIVE_AD", dataProvider = "fender_dcp_dp", dataProviderClass =
            FenderDataProvider.class)
    public void TEST1_SDK450_NATIVE_AD(final String x, final ResponseBuilder responseBuilder)
            throws Exception {

        parserOutput = LogParserHelper.logParser("sdk-version : a450",
                "Rewrapping native JSON for DCP traffic. Wrapped Response is:");

        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }*/
}
