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
    private String parserOutput = new String();

    @Test(testName = "TEST2_65_2", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_65_2(final String x) throws Exception {

        searchStringInLog = "interests:[],";

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log("Searching for " + searchStringInLog + "in the logs for test case :" + x + "\n", true);

        Reporter.log("\n Parser OutPut: " + parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_65_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_65_1(final String x) throws Exception {

        // AdPoolRequestHelper
        // .fireAdPoolRequestForRTBD("Test2_65_1".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        searchStringInLog = "interests:[someinterestsvalue],";

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_65_3", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_65_3(final String x) throws Exception {

        // AdPoolRequestHelper
        // .fireAdPoolRequestForRTBD("Test2_65_3".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        searchStringInLog = "interests:[  ],";

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(searchStringInLog,
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_41_1", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_41_1(final String x) throws Exception {

        // AdPoolRequestHelper
        // .fireAdPoolRequestForRTBD("Test2_41_1".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        final String searchString1 = "uidParams:UidParams(rawUidValues:{UM5=somevalue},";
        final String searchString2 = "tUidParams={UM5=somevalue},";

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(searchString1, searchString2,
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));
        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test2_63_4", dataProvider = "fender_rtbd_dp", dataProviderClass = FenderDataProvider.class)
    public void Test2_63_4(final String x) throws Exception {

        // AdPoolRequestHelper
        // .fireAdPoolRequestForRTBD("Test2_63_4".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_RTBD_RESPONSE),
                    LogStringConf.getLogString(LogStringParams.MSG_RTBD_AdRR_FLAG));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }
}
