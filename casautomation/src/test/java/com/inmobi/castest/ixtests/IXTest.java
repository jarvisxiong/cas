package com.inmobi.castest.ixtests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.inmobi.castest.casconfenums.def.CasConf.LogStringParams;
import com.inmobi.castest.casconfenums.impl.LogStringConf;
import com.inmobi.castest.commons.generichelper.LogParserHelper;
import com.inmobi.castest.dataprovider.FenderDataProvider;

public class IXTest {

    private final String searchStringInLog = new String();
    private String parserOutput = new String();

    @Test(testName = "Test3_1_1", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_1(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_1", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_1".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_1_2", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_2(final String x) throws Exception {

        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_2", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_2".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TERMREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_1_3", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_3(final String x) throws Exception {

        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_3", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_3".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TERMREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_1_4", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_4(final String x) throws Exception {

        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_4", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_4".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TERMREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_1_5", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_5(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TERMREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_2_1", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_2_1(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_2_2", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_2_2(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_2_3", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_2_3(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_2_4", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_2_4(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_1", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_1(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_2", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_2(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_FORMREQ),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_3", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_3(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_4", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_4(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_5", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_5(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_MULTSLOTICL));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_6", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_6(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_7", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_7(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_8", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_8(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_FORMREQ),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_9", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_9(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_10", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_10(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_11", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_11(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_MULTSLOTICL),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_12(final String x) throws Exception {
        //
        // /* Set up the DB for Demand Supply Data */
        // UpdateDBWithWAPAdGroupData.updateDBWithData("Test3_1_5", true);
        //
        // AdPoolRequestHelper.fireAdPoolRequestForIX("Test3_1_5".toUpperCase());
        // /* Set up a search string that this test needs to search for */

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    /*
     *
     * Regex copy starts here
     */

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_3_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_3_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_3_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_3_4(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // =======

    // //Request JSON For : --- PERF/Android/APP ---
    // ******************************************************************************************************//

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_EXCHANGEREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_APPOBJ),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TMAX));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ANDRD),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_AVRSN));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_4(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_LATLONG),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_5(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_UA),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_6(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_RPACNTID),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_RPSIZEID));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // hereddone till here

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_7_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDLOW),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDLOW1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_7_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDHGH),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDHGH1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_7_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDSME),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDSME1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_8_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_8_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_8_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSAPP2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp",
    // dataProviderClass = FenderDataProvider.class)
    // public void TEST3_6_8_4(String x) throws Exception {
    //
    // /* Deriving the parser output to assert for */
    // parserOutput = LogParserHelper.logParser(
    // LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
    // LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));
    //
    // Reporter.log(parserOutput, true);
    // // System.out.println("ParserOutput : " + parserOutput);
    //
    // Assert.assertTrue(parserOutput.equals("PASS"));
    // }

    // //Request JSON For : --- PERF/Android/WAP ---

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_EXCHANGEREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_WAPOBJ),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TMAX));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ANDRD),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_AVRSN));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_4(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_LATLONG),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_5(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_UA),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_6(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_RPACNTID),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_RPSIZEID));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // hereddone till here

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_7_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDLOW),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDLOW1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_7_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDHGH),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDHGH1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_7_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDSME),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDSME1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_8_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_8_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_8_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSWAP2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp",
    // dataProviderClass = FenderDataProvider.class)
    // public void TEST3_6_8_4(String x) throws Exception {
    //
    // /* Deriving the parser output to assert for */
    // parserOutput = LogParserHelper.logParser(
    // LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
    // LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));
    //
    // Reporter.log(parserOutput, true);
    // // System.out.println("ParserOutput : " + parserOutput);
    //
    // Assert.assertTrue(parserOutput.equals("PASS"));
    // }

    // Request JSON For : --- PERF/iOS/APP ---

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_EXCHANGEREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_APPOBJ),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TMAX));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_IOS),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_IVRSN));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_4(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_LATLONG),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_5(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_UA),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_6(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_RPACNTID),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_RPSIZEID));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_7_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDLOW),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDLOW));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_7_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDHGH),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDHGH));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_7_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDSME),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDSME));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_8_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_8_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_8_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSAPP2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_8_4(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRANS),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSAPP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSIDNAME),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSSTOREURL),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // Request JSON For : --- PERF/iOS/WAP ---
    // ******************************************************************************************************//

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_EXCHANGEREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_WAPOBJ),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TMAX));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_IOS),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_IVRSN));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_4(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_LATLONG),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_5(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_UA),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_6(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_RPACNTID),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_RPSIZEID));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // d
    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_7_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDLOW),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDLOW));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_7_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDHGH),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDHGH));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_7_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDSME),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDSME));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_8_1(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_8_2(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAP),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_8_3(final String x) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
                LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSWAP2),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE),
                    LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp",
    // dataProviderClass = FenderDataProvider.class)
    // public void TEST3_8_8_4(String x) throws Exception {
    //
    // /* Deriving the parser output to assert for */
    // parserOutput = LogParserHelper.logParser(
    // LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER),
    // LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));
    //
    // Reporter.log(parserOutput, true);
    // // System.out.println("ParserOutput : " + parserOutput);
    //
    // Assert.assertTrue(parserOutput.equals("PASS"));
    // }
}
