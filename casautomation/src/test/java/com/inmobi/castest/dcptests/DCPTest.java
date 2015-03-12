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
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_33_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_33_1(final String x) throws Exception {

        searchStringInLog = "response is";
        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_17_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_17_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_19_2", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_19_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_TAPIT_CONFIG_SUCCESS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test1_36_1", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_36_1(final String x) throws Exception {

        searchStringInLog = "response is";
        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "GoogleAdx", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_01(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_AD_SERVED));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "DMG", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_02(final String x) throws Exception {

        searchStringInLog = "response is";
        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(searchStringInLog);

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "AMOAD", dataProvider = "fender_dcp_dp", dataProviderClass = FenderDataProvider.class)
    public void Test1_03(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_DCP_AD_SERVED));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }
}
