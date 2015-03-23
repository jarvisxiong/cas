package com.inmobi.castest.rtbdtests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.inmobi.castest.casconfenums.def.CasConf.LogStringParams;
import com.inmobi.castest.casconfenums.impl.LogStringConf;
import com.inmobi.castest.commons.generichelper.LogParserHelper;
import com.inmobi.castest.dataprovider.FenderDataProvider;

public class RTBD_NewTest {

    private String searchStringInLog = new String();
    private String searchStringInLog1 = new String();
    private String searchStringInLog2 = new String();
    private String searchStringInLog3 = new String();
    private String parserOutput = new String();

    @Test(testName = "Test2_11_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_11_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(
                        "contentRatingDeprecated:PERFORMANCE,",
                        "siteContentType=PERFORMANCE,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        // System.out.println("ParserOutput : " + parserOutput);
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_11_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_11_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "contentRatingDeprecated:FAMILY_SAFE,",
                        "siteContentType=FAMILY_SAFE,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_11_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_11_3(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "contentRatingDeprecated:MATURE,",
                        "siteContentType=MATURE,",
                        "Terminating request as incompatible content type"
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_11_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_11_4(final String x) throws Exception {


        parserOutput =
                LogParserHelper.logParser(
                        "siteContentType=FAMILY_SAFE,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_11_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_11_5(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteContentType=FAMILY_SAFE,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_11_6", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_11_6(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteContentType=FAMILY_SAFE,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_12_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_12_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTags:[70], ",
                        "categories=[70],",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_12_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_12_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTags:[70, 71],",
                        "categories=[70, 71],",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_12_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_12_3(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTags:[0],",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_12_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_12_4(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTags:[10000],",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_12_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_12_5(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTags:[],",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_12_7", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_12_7(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTags:[],",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_13_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_13_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTaxonomies:[190]",
                        LogStringConf.getLogString(LogStringParams.MSG_SENDING_NO_AD)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_13_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_13_3(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTaxonomies:[0]",
                        LogStringConf.getLogString(LogStringParams.MSG_SENDING_NO_AD)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_13_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_13_4(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTaxonomies:[10000]",
                        "categories=[10000],",
                        LogStringConf.getLogString(LogStringParams.MSG_SENDING_NO_AD)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_13_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_13_5(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTaxonomies:[]",
                        "categories=[], ",
                        LogStringConf.getLogString(LogStringParams.MSG_SENDING_NO_AD)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_13_7", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_13_7(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "siteTaxonomies:[]",
                        "categories=[], ",
                        LogStringConf.getLogString(LogStringParams.MSG_SENDING_NO_AD)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_14_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_14_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "userAgent:,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTB_MANDATE_PARAM_MISSING)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_14_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_14_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "userAgent:,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTB_MANDATE_PARAM_MISSING)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_14_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_14_4(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "userAgent:justlikethat,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_15_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_15_4(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "modelId:0,",
                        "modelId=0,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_15_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_15_5(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "modelId:1199999,",
                        " modelId=1199999,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_16_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_16_4(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "manufacturerId:0,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_16_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_16_5(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "manufacturerId:1010199,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_17_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_17_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "osId:3,",
                        "osId=3,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_17_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_17_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "osId:5,",
                        "osId=5,",
                        LogStringConf.getLogString(LogStringParams.MSG_SENDING_NO_AD)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_17_6", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_17_6(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "osId:101,",
                        "osId=101,",
                        LogStringConf.getLogString(LogStringParams.MSG_SENDING_NO_AD)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_18_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_18_4(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "osMajorVersion:0.0,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_18_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_18_5(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "osMajorVersion:10.0,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_18_6", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_18_6(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "osMajorVersion:10.02",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_19_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_19_4(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "browserId:0,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_19_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_19_5(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "browserId:1010,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_20_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_20_4(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "browserMajorVersion:0.0,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_20_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_20_5(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "browserMajorVersion:10.0,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_20_6", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_20_6(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(
                        "browserMajorVersion:10.02,",
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG)
                );

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_51_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_51_1(final String x) throws Exception {

        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{GID=somevalue},";
        searchStringInLog2 = "tUidParams={GID=somevalue},";

        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_51_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_51_2(final String x) throws Exception {

        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{GID=},";
        searchStringInLog2 = "tUidParams={GID=},";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_51_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_51_3(final String x) throws Exception {

        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{GID=  },";
        searchStringInLog2 = "tUidParams={GID=  },";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_51_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_51_4(final String x) throws Exception {

        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{},";
        searchStringInLog2 = "tUidParams={},";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }


    @Test(testName = "TEST2_52_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_52_1(final String x) throws Exception {

        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{WC=somevalue},";
        searchStringInLog2 = "tUidParams={WC=somevalue},";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_52_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_52_2(final String x) throws Exception {

        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{WC=},";
        searchStringInLog2 = "tUidParams={WC=},";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_52_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_52_3(final String x) throws Exception {

        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{WC= },";
        searchStringInLog2 = "tUidParams={WC= },";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_52_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_52_4(final String x) throws Exception {

        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{},";
        searchStringInLog2 = "tUidParams={},";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    //    Commented #53 Because is was flakey
    //    @Test(testName = "TEST2_53_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    //    public void Test2_53_1(final String x) throws Exception {
    //
    //        searchStringInLog1 = "uidParams:UidParams(rawUidValues:{WC=somevalue},";
    //        searchStringInLog2 = "tUidParams={WC=somevalue},";
    //
    //        
    //        parserOutput =
    //                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,
    //                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
    //                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));
    //
    //        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);
    //
    //        Reporter.log("\n Parser OutPut: " + parserOutput, true);
    //        
    //
    //        Assert.assertTrue(parserOutput.equals("PASS"));
    //    }

    @Test(testName = "TEST2_54_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_54_1(final String x) throws Exception {

        searchStringInLog = "udidFromRequest:somevalue,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_54_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_54_2(final String x) throws Exception {

        searchStringInLog = "udidFromRequest:,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_54_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_54_3(final String x) throws Exception {

        searchStringInLog = "udidFromRequest: ,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_54_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_54_4(final String x) throws Exception {


        parserOutput =
                LogParserHelper.logParser(
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_55_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_55_1(final String x) throws Exception {

        searchStringInLog = "uuidFromUidCookie:somevalue,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_55_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_55_2(final String x) throws Exception {

        searchStringInLog = "uuidFromUidCookie:,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_55_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_55_3(final String x) throws Exception {

        searchStringInLog = "uuidFromUidCookie: ,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_55_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_55_4(final String x) throws Exception {


        parserOutput =
                LogParserHelper.logParser(
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_56_1",
            dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_56_1(final String x) throws Exception {

        searchStringInLog = "limitIOSAdTracking:true),";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_56_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_56_2(final String x) throws Exception {

        searchStringInLog = "limitIOSAdTracking:false),";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_57_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_57_1(final String x) throws Exception {

        searchStringInLog = "dataVendorId:10011010,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_57_5", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_57_5(final String x) throws Exception {

        searchStringInLog = "dataVendorId:1,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_58_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_58_1(final String x) throws Exception {

        searchStringInLog = "dataVendorName:somevendorname,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_58_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_58_2(final String x) throws Exception {

        searchStringInLog = "dataVendorName:,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_58_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_58_3(final String x) throws Exception {

        searchStringInLog = "dataVendorName: ,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_58_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_58_4(final String x) throws Exception {


        parserOutput =
                LogParserHelper.logParser(
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_59_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_59_1(final String x) throws Exception {

        searchStringInLog = "yearOfBirth:1900,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_59_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_59_2(final String x) throws Exception {

        searchStringInLog = "yearOfBirth:0,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_59_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_59_3(final String x) throws Exception {

        searchStringInLog = "yearOfBirth:2015,";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }
    @Test(testName = "TEST2_60_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_60_1(final String x) throws Exception {

        searchStringInLog1 = "gender:MALE,";
        searchStringInLog2 = "gender=M,";
        searchStringInLog3 = "gender:MALE),";



        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,searchStringInLog3,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_60_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_60_2(final String x) throws Exception {

        searchStringInLog1 = "gender:FEMALE,";
        searchStringInLog2 = "gender=F,";
        searchStringInLog3 = "gender:FEMALE),";



        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,searchStringInLog3,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST2_60_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_60_3(final String x) throws Exception {

        searchStringInLog1 = "gender:UNKNOWN,";
        searchStringInLog2 = "gender=U,";
        searchStringInLog3 = "gender:UNKNOWN),";



        parserOutput =
                LogParserHelper.logParser(searchStringInLog1,searchStringInLog2,searchStringInLog3,
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    //    TODO:Commenting #60_6 until futher investigation is done
    //    @Test(testName = "TEST2_60_6", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    //    public void Test2_60_6(final String x) throws Exception {
    //
    //
    //        
    //        parserOutput =
    //                LogParserHelper.logParser(
    //                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
    //                        LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));
    //
    //        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);
    //
    //        Reporter.log("\n Parser OutPut: " + parserOutput, true);
    //        
    //
    //        Assert.assertTrue(parserOutput.equals("PASS"));
    //    }


    @Test(testName = "TEST2_65_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_65_2(final String x) throws Exception {

        searchStringInLog = "interests:[],";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_65_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_65_1(final String x) throws Exception {

        // AdPoolRequestHelper
        // .fireAdPoolRequestForRTBD("Test2_65_1".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        searchStringInLog = "interests:[someinterestsvalue],";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log(parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_65_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_65_3(final String x) throws Exception {

        // AdPoolRequestHelper
        // .fireAdPoolRequestForRTBD("Test2_65_3".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        searchStringInLog = "interests:[ ],";


        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log(parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_41_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_41_1(final String x) throws Exception {

        // AdPoolRequestHelper
        // .fireAdPoolRequestForRTBD("Test2_41_1".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        final String searchString1 = "uidParams:UidParams(rawUidValues:{UM5=somevalue},";
        final String searchString2 = "tUidParams={UM5=somevalue},";


        parserOutput =
                LogParserHelper.logParser(searchString1, searchString2,
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));
        Reporter.log(parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_63_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_63_4(final String x) throws Exception {

        // AdPoolRequestHelper
        // .fireAdPoolRequestForRTBD("Test2_63_4".toUpperCase());
        // /* Set up a search string that this test needs to search for */


        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log(parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS"));
    }
}
