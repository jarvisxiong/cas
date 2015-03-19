package com.inmobi.castest.dcptests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.inmobi.castest.casconfenums.def.CasConf.LogStringParams;
import com.inmobi.castest.casconfenums.impl.LogStringConf;
import com.inmobi.castest.commons.generichelper.LogParserHelper;
import com.inmobi.castest.dataprovider.FenderDataProvider;

public class DCPTest {

    private String searchStringInLog = new String();
    private String parserOutput = new String();

    @Test(testName = "Test1_13_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_13_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_33_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_33_1(final String x) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_17_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_17_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_19_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_19_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_36_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_36_1(final String x) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "GoogleAdx", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_01(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_AD_SERVED));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "DMG", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_02(final String x) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "AMOAD", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_03(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_AD_SERVED));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_1_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_1_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_3_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_3_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_4_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_4_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_5_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_5_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_6_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_6_1(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_7_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_7_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_8_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_8_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_9_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_9_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_10_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_10_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_11_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_11_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TERMINATE_CONFIG_SEARCH));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_12_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_12_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TERMINATE_CONFIG_SEARCH));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_15_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_15_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_16_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_16_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_17_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_17_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_18_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_18_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_19_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_19_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_21_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_21_2(final String x) throws Exception {

        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_33_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_33_1(final String x) throws Exception {

        searchStringInLog = "response is";
        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_33_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_33_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_34_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_34_1(final String x) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_34_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_34_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_35_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_35_1(final String x) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_35_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_35_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_36_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_36_1(final String x) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_36_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_36_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_37_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_37_1(final String x) throws Exception {

        searchStringInLog = "response is";

        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_37_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_37_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_38_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_38_1(final String x) throws Exception {

        searchStringInLog = "response is";
        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST1_38_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST1_38_2(final String x) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_SENDING_NO_AD));

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }
}
