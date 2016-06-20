package com.inmobi.castest.ixtests;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.inmobi.castest.casconfenums.def.CasConf.LogStringParams;
import com.inmobi.castest.casconfenums.impl.LogStringConf;
import com.inmobi.castest.commons.generichelper.LogParserHelper;
import com.inmobi.castest.dataprovider.FenderDataProvider;
import com.inmobi.castest.utils.common.ResponseBuilder;

public class IXTest {

    private String searchStringInLog = new String();
    private String parserOutput = new String();
    private String parserOutput1 = new String();
    private String parserOutput2 = new String();
    private String response = new String();

    @Test(testName = "Test3_1_1", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_1_2", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TERMREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_1_3", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TERMREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_1_4", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TERMREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_1_5", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_1_5(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TERMREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_2_1", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_2_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_2_2", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_2_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_2_3", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_2_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_2_4", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_2_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_1", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER), LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_2", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_FORMREQ), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_3", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER), LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_4", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER), LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_5", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_5(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_MULTSLOTICL));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_6", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_6(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER), LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_7", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_7(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER), LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_8", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_8(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_FORMREQ), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_9", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_9(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER), LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_10", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_10(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER), LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_11", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_11(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_MULTSLOTICL), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void Test3_4_12(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_SLOTFILTER), LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRNOADS));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }


    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_3_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_3_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_3_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_3_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_DST));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // =======

    // //Request JSON For : --- PERF/Android/APP ---
    // ******************************************************************************************************//

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_EXCHANGEREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_APPOBJ), LogStringConf.getLogString(LogStringParams.MSG_IX_TMAX));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ANDRD), LogStringConf.getLogString(LogStringParams.MSG_IX_AVRSN));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_LATLONG), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_5(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_UA), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_6(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_RPACNTID), LogStringConf.getLogString(LogStringParams.MSG_IX_RPSIZEID));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // hereddone till here

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_7_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDLOW), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDLOW1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_7_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDHGH), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDHGH1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_7_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDSME), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDSME1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_8_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPP), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_8_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPP), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_5_8_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSAPP2), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

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
    public void TEST3_6_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_EXCHANGEREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_WAPOBJ), LogStringConf.getLogString(LogStringParams.MSG_IX_TMAX));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ANDRD), LogStringConf.getLogString(LogStringParams.MSG_IX_AVRSN));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_LATLONG), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_5(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_UA), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_6(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_RPACNTID), LogStringConf.getLogString(LogStringParams.MSG_IX_RPSIZEID));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_7_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDLOW), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDLOW1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_7_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDHGH), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDHGH1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_7_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDSME), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDSME1));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_8_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAP), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_8_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAP), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_6_8_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSWAP2), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

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
    public void TEST3_7_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_EXCHANGEREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_APPOBJ), LogStringConf.getLogString(LogStringParams.MSG_IX_TMAX));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_IOS), LogStringConf.getLogString(LogStringParams.MSG_IX_IVRSN));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_LATLONG), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_5(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_UA), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_6(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_RPACNTID), LogStringConf.getLogString(LogStringParams.MSG_IX_RPSIZEID));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_7_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDLOW), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDLOW));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_7_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDHGH), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDHGH));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_7_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDSME), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDSME));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_8_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPP), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_8_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPP), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_7_8_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSAPP2), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDAPPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp",
    // dataProviderClass = FenderDataProvider.class)
    // public void TEST3_7_8_4(final String x, ResponseBuilder responseBuilder)
    // throws Exception {
    //
    // /* Deriving the parser output to assert for */
    // parserOutput =
    // LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRANS),
    // LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSAPP),
    // LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSIDNAME),
    // LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSSTOREURL),
    // LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSAPPEXTBUNDLE));
    //
    // Reporter.log(parserOutput, true);
    // // System.out.println("ParserOutput : " + parserOutput);
    //
    // Assert.assertTrue(parserOutput.equals("PASS"));
    // }

    // Request JSON For : --- PERF/iOS/WAP ---
    // ******************************************************************************************************//

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADRRFLAG2), LogStringConf.getLogString(LogStringParams.MSG_IX_EXCHANGEREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_WAPOBJ), LogStringConf.getLogString(LogStringParams.MSG_IX_TMAX));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_IOS), LogStringConf.getLogString(LogStringParams.MSG_IX_IVRSN));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_LATLONG), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_5(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_UA), LogStringConf.getLogString(LogStringParams.MSG_IX_SENDREQ));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_6(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_RPACNTID), LogStringConf.getLogString(LogStringParams.MSG_IX_RPSIZEID));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_7_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDLOW), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDLOW));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_7_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDHGH), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDHGH));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(enabled = false, testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_7_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADPGBIDSME), LogStringConf.getLogString(LogStringParams.MSG_IX_GBIDSME));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_8_1(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAP), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_8_2(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAP), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_4_12", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_8_8_3(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_BLIND), LogStringConf.getLogString(LogStringParams.MSG_IX_TRANSWAP2), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPBUNDLE), LogStringConf.getLogString(LogStringParams.MSG_IX_BLINDWAPEXTBUNDLE));

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

    // public String resetWilburyIntKey(final String oldImpressionId, final long adId) {
    // return WilburyUUID.setIntKey(oldImpressionId, (int) adId).toString();
    // }
    //
    // @Test(testName = "Tests that the auction id can be regenerated from the new impression id", dataProvider =
    // "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    // public void TEST3_ImpressionIdChange_PROG382(final String x, final ResponseBuilder responseBuilder)
    // throws Exception {
    // parserOutput = "PASS";
    //
    // final LogLines siteIncIdLog =
    // LogParserHelper
    // .queryForLogs("c.i.a.c.s.r.ThriftRequestParser: Successfully parsed tObject, SAS params are");
    // final LogLines oldImpressionIdLog = LogParserHelper.queryForLogs("Old impression id:");
    // final LogLines newImpressionIdLog = LogParserHelper.queryForLogs("Replaced impression id to new value");
    // final LogLines auctionIdLog =
    // LogParserHelper.queryForLogs("c.i.a.c.server.servlet.BaseServlet: Auction id generated");
    // final LogLines beaconUrlLog =
    // LogParserHelper.queryForLogs("c.i.a.c.u.Utils.ClickUrlsRegenerator: New Beacon Url:");
    // final LogLines clickUrlLog =
    // LogParserHelper.queryForLogs("c.i.a.c.u.Utils.ClickUrlsRegenerator: New Click Url:");
    //
    // final String siteIncIdString = siteIncIdLog.applyRegex("siteIncId:[0-9]+,", "[0-9]+");
    // Long siteIncId;
    // try {
    // siteIncId = Long.valueOf(siteIncIdString);
    // } catch (final NumberFormatException nfe) {
    // siteIncId = null;
    // }
    // final String oldImpressionId = oldImpressionIdLog.applyRegex(LogLinesRegex.UUID.getRegex());
    // final String newImpressionId = newImpressionIdLog.applyRegex(LogLinesRegex.UUID.getRegex());
    // final String auctionId = auctionIdLog.applyRegex(LogLinesRegex.UUID.getRegex());
    // final String impressionFromBeaconUrl = beaconUrlLog.applyRegex(LogLinesRegex.UUID.getRegex());
    // final String impressionFromClickUrl = clickUrlLog.applyRegex(LogLinesRegex.UUID.getRegex());
    //
    // if (null == siteIncId || null == oldImpressionId || null == newImpressionId || null == auctionId
    // || null == impressionFromBeaconUrl || null == impressionFromClickUrl) {
    // parserOutput = "FAIL";
    // }
    //
    // Reporter.log(parserOutput, true);
    //
    // Assert.assertTrue(parserOutput.equals("PASS"));
    // Assert.assertEquals(auctionId, resetWilburyIntKey(oldImpressionId, siteIncId));
    // Assert.assertEquals(auctionId, resetWilburyIntKey(newImpressionId, siteIncId));
    // Assert.assertEquals(auctionId, resetWilburyIntKey(impressionFromBeaconUrl, siteIncId));
    // Assert.assertEquals(auctionId, resetWilburyIntKey(impressionFromClickUrl, siteIncId));
    // }
    @Test(testName = "Test3_Native_Layout_1", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_NATIVE_LAYOUT_1(final String x, final ResponseBuilder responseBuilder) throws Exception {
        searchStringInLog = "\"native\":{\"requestobj\":{\"layout\":3";
        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_CREATIVE_NATIVE), searchStringInLog);

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_Native_Layout_2", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_NATIVE_LAYOUT_2(final String x, final ResponseBuilder responseBuilder) throws Exception {
        searchStringInLog = "\"native\":{\"requestobj\":{\"layout\":1";
        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_CREATIVE_NATIVE), searchStringInLog);

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_Native_Layout_3", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_NATIVE_LAYOUT_3(final String x, final ResponseBuilder responseBuilder) throws Exception {
        searchStringInLog = "\"native\":{\"requestobj\":{\"layout\":6";

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_CREATIVE_NATIVE), searchStringInLog);

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_Native_Layout_4", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_NATIVE_LAYOUT_4(final String x, final ResponseBuilder responseBuilder) throws Exception {

        /* Deriving the parser output to assert for */
        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_CREATIVE_NATIVE));

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_Native_Layout_5", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_NATIVE_LAYOUT_5(final String x, final ResponseBuilder responseBuilder) throws Exception {
        searchStringInLog = "\"native\":{\"requestobj\":{\"layout\":4";

        /* Deriving the parser output to assert for */
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_CREATIVE_NATIVE), searchStringInLog);

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "Test3_Native_Layout_6", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST3_NATIVE_LAYOUT_6(final String x, final ResponseBuilder responseBuilder) throws Exception {
        searchStringInLog = "\"native\":{\"requestobj\":{\"layout\":2";

        /* Deriving the parser output to assert for */

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_CREATIVE_NATIVE), searchStringInLog);

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_NATIVE_NON_NATIVE_SITE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_NATIVE_NON_NATIVE_SITE(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "This placement id 123456 doesn't have native template";
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADAPTER_CONFIG_FAIL_IMP_OBJ_NULL), LogStringConf.getLogString(LogStringParams.MSG_IX_CREATIVE_NATIVE), searchStringInLog);

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_VAST_HAPPY", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_HAPPY(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */
        final String responseString1 = "<VASTAdTagURI>";
        final String responseString2 = "<\\/VASTAdTagURI>";

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
        Assert.assertTrue(response.contains(responseString1), "Did not find " + responseString1 + "in the response");
        Assert.assertTrue(response.contains(responseString2), "Did not find " + responseString2 + "in the response");
    }

    @Test(testName = "TEST_VAST_HAPPY_ALTERNATE_SIZES1", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_HAPPY_ALTERNATE_SIZES1(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */
        final String responseString1 = "<VASTAdTagURI>";
        final String responseString2 = "<\\/VASTAdTagURI>";

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
        Assert.assertTrue(response.contains(responseString1), "Did not find " + responseString1 + "in the response");
        Assert.assertTrue(response.contains(responseString2), "Did not find " + responseString2 + "in the response");
    }

    @Test(testName = "TEST_VAST_HAPPY_ALTERNATE_SIZES2", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_HAPPY_ALTERNATE_SIZES2(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */
        final String responseString1 = "<VASTAdTagURI>";
        final String responseString2 = "<\\/VASTAdTagURI>";

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
        Assert.assertTrue(response.contains(responseString1), "Did not find " + responseString1 + "in the response");
        Assert.assertTrue(response.contains(responseString2), "Did not find " + responseString2 + "in the response");
    }

    @Test(testName = "TEST_VAST_HAPPY_ALTERNATE_SIZES3", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_HAPPY_ALTERNATE_SIZES3(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */
        final String responseString1 = "<VASTAdTagURI>";
        final String responseString2 = "<\\/VASTAdTagURI>";

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
        Assert.assertTrue(response.contains(responseString1), "Did not find " + responseString1 + "in the response");
        Assert.assertTrue(response.contains(responseString2), "Did not find " + responseString2 + "in the response");
    }

    @Test(testName = "TEST_REWARDED_HAPPY", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_REWARDED_HAPPY(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */
        final String responseString1 = "<VASTAdTagURI>";
        final String responseString2 = "<\\/VASTAdTagURI>";

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
        Assert.assertTrue(response.contains(responseString1), "Did not find " + responseString1 + "in the response");
        Assert.assertTrue(response.contains(responseString2), "Did not find " + responseString2 + "in the response");
    }

    @Test(testName = "TEST_VAST_SDKVERSION_440", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_SDKVERSION_440(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */
        final String responseString1 = "<VASTAdTagURI>";
        final String responseString2 = "<\\/VASTAdTagURI>";

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
        Assert.assertFalse(response.contains(responseString1), "Found " + responseString1 + "in the response");
        Assert.assertFalse(response.contains(responseString2), "Found " + responseString2 + "in the response");
    }

    @Test(testName = "TEST_VAST_PUBCONTROLS_BANNER_450", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_PUBCONTROLS_BANNER_450(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */
        final String responseString1 = "<VASTAdTagURI>";
        final String responseString2 = "<\\/VASTAdTagURI>";

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);
        // System.out.println("ParserOutput : " + parserOutput);

        Assert.assertTrue(parserOutput.equals("PASS"));
        Assert.assertFalse(response.contains(responseString1), "Found " + responseString1 + "in the response");
        Assert.assertFalse(response.contains(responseString2), "Found " + responseString2 + "in the response");
    }

    @Test(testName = "TEST_IX_JSAC_INTEGRATION_TYPE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_JSAC_INTEGRATION_TYPE(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_IX_API_ANDROID_INTEGRATION_TYPE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_API_ANDROID_INTEGRATION_TYPE(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }


    @Test(testName = "TEST_IX_API_IOS_INTEGRATION_TYPE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_API_IOS_INTEGRATION_TYPE(final String x, final ResponseBuilder responseBuilder) throws Exception {
        /* Deriving the parser output to assert for */

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_AD_SERVED));
        response = new String(responseBuilder.getResponseData());

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_STUDIO_POSITIVE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_STUDIO_POSITIVE(final String x, final ResponseBuilder responseBuilder) throws Exception {

        final String searchStringInLog = "Sprout Ad Received";
        final String searchStringInLog2 = "Replaced Sprout Macros";
        parserOutput = LogParserHelper.logParser(searchStringInLog, searchStringInLog2);

        Assert.assertTrue(parserOutput.equals("PASS"));
        // Ensuring that beacons are properly replaced
        Assert.assertTrue(!LogParserHelper.queryForLogs("loadBeacons : []").isNotEmpty());
    }

    @Test(testName = "TEST_STUDIO_NEGATIVE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_STUDIO_NEGATIVE(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "Sprout Ad Received";
        final String searchStringInLog2 = "Replaced Sprout Macros";

        response = new String(responseBuilder.getResponseData());

        Assert.assertFalse(response.contains(searchStringInLog), "Found " + searchStringInLog + "in the response");
        Assert.assertFalse(response.contains(searchStringInLog2), "Found " + searchStringInLog + "in the response");
    }

    @Test(testName = "TEST_IX_PACKAGES_NORMALDEAL", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_PACKAGES_NORMALDEAL(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_PACKAGE_NORMAL));
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST IX PACKAGES DROPPED IN SDK VERSION FOR BANNER", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_PACKAGES_SDK_VERSION_BANNER(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("Package 10011 dropped in SDK Version Filter");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST IX PACKAGES DROPPED IN SDK VERSION FOR NATIVE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_PACKAGES_SDK_VERSION_NATIVE(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("Package 10011 dropped in SDK Version Filter");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST IX PACKAGES DROPPED IN SDK VERSION FOR VAST", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_PACKAGES_SDK_VERSION_VAST(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("Package 10011 dropped in SDK Version Filter");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST IX NEW PACKAGES DROPPED IN SDK VERSION FOR BANNER", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_NEW_PACKAGES_SDK_VERSION_BANNER(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("10014 dropped in SDK Version Filter");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST IX NEW PACKAGES DROPPED IN SDK VERSION FOR NATIVE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_NEW_PACKAGES_SDK_VERSION_NATIVE(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("10014 dropped in SDK Version Filter");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST IX NEW PACKAGES DROPPED IN SDK VERSION FOR VAST", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_NEW_PACKAGES_SDK_VERSION_VAST(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("10014 dropped in SDK Version Filter");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_IX_NEW_PACKAGE_COUNTRY_EXCLUSION", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_NEW_PACKAGE_COUNTRY_EXCLUSION(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("Targeting Segment 100004 dropped in Country City Filter");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }


    @Test(testName = "TEST_RTBD_PMP_OLD& NEW_PACKAGES", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_RTBD_PMP_NEW_PACKAGES(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("\"target\":{\"packages\":[\"10001\",\"10013\"]}");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_RTBD_PMP_VIEWABILITY_NEW_PACKAGES", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_RTBD_PMP_VIEWABILITY_NEW_PACKAGES(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("https://ViewabilityTracker_thirdpartyjson.com");
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_IX_TRACKERDEAL_WITHOUT_VIEWABILITY", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_TRACKERDEAL_WITHOUT_VIEWABILITY(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_WITHOUT_VIEWABILITY));
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_IX_TRACKERDEAL_VIEWABILITY_TRACKER_INSTEAD_JSON", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_TRACKERDEAL_VIEWABILITY_TRACKER_INSTEAD_JSON(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_VIEWABILITY_TRACKER_INSTEAD_JSON));
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "TEST_IX_TRACKERDEAL_VIEWABILITY_TRACKER_FROM_JSON", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_TRACKERDEAL_VIEWABILITY_TRACKER_FROM_JSON(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_VIEWABILITY_TRACKER_FROM_JSON));
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_IX_TRACKERDEAL_AUDIENCE_LIMIT_AD_TRACKING_MACRO", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_TRACKERDEAL_AUDIENCE_LIMIT_AD_TRACKING_MACRO(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_AUDIENCE_LIMIT_AD_TRACKING_MACRO));
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "TEST_IX_TRACKERDEAL_AUDIENCE_IMP_CB_MACRO", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_TRACKERDEAL_AUDIENCE_IMP_CB_MACRO(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_AUDIENCE_IMP_CB_MACRO));
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "TEST_IX_TRACKERDEAL_AUDIENCE_USER_ID_SHA256_HASHED_MACRO", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_TRACKERDEAL_AUDIENCE_USER_ID_SHA256_HASHED_MACRO(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_AUDIENCE_USER_ID_SHA256_HASHED_MACRO));
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "TEST_IX_TRACKERDEAL_AUDIENCE_WITH_MULTIPLE_MACRO", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_TRACKERDEAL_AUDIENCE_WITH_MULTIPLE_MACRO(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_AUDIENCE_LIMIT_AD_TRACKING_MACRO));
        Reporter.log(parserOutput, true);
        parserOutput1 =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_AUDIENCE_USER_ID_SHA256_HASHED_MACRO));
        Reporter.log(parserOutput, true);
        parserOutput2 =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_TRACKERDEAL_AUDIENCE_IMP_CB_MACRO));
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("FAIL") && parserOutput1.equals("FAIL") && parserOutput2.equals("FAIL"));
    }

    @Test(testName = "TEST_IX_PACKAGES_CSIDDEAL", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_PACKAGES_CSIDDEAL(final String x, final ResponseBuilder responseBuilder) throws Exception {

        String parserOutput2;

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_PACKAGE_CSID));
        Reporter.log(parserOutput, true);
        parserOutput2 = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_PACKAGE_CSID2));
        Assert.assertTrue(parserOutput.equals("PASS") || parserOutput2.equals("PASS"));

    }

    @Test(testName = "TEST_IX_PACKAGES_CSIDDEAL_NEGATIVE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_PACKAGES_CSIDDEAL_NEGATIVE(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_PACKAGE_CSID));
        response = new String(responseBuilder.getResponseData());
        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "TEST_IX_PACKAGES_CSIDDEAL", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_NEW_PACKAGES_CSIDDEAL(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("\"target\":{\"packages\":[\"10016\",\"10015\"]}");
        Reporter.log(parserOutput, true);
        parserOutput2 = LogParserHelper.logParser("\"target\":{\"packages\":[\"10015\",\"10016\"]}");
        Reporter.log(parserOutput2, true);
        Assert.assertTrue(parserOutput.equals("PASS") || parserOutput2.equals("PASS"));

    }

    @Test(testName = "TEST_IX_PACKAGES_CSIDDEAL_NEGATIVE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_IX_NEW_PACKAGES_CSIDDEAL_NEGATIVE(final String x, final ResponseBuilder responseBuilder) throws Exception {

        parserOutput = LogParserHelper.logParser("\"target\":{\"packages\":[\"10016\",\"10015\"]}");
        Reporter.log(parserOutput, true);
        parserOutput2 = LogParserHelper.logParser("\"target\":{\"packages\":[\"10015\",\"10016\"]}");
        Reporter.log(parserOutput2, true);
        Assert.assertTrue(parserOutput.equals("FAIL") && parserOutput2.equals("FAIL"));

    }

    @Test(testName = "TEST_DEVICE_LANGUAGE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_DEVICE_LANGUAGE(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput =
            LogParserHelper.logParser("locale:en", "language=en", "Package 10007 dropped in Language Targeting Filter", "Packages selected: [10006]");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_DEVICE_LANGUAGE_NULL", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_DEVICE_LANGUAGE_NULL(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput = LogParserHelper.logParser("language=null", "IX request json",
            "10006 dropped in Language " + "Targeting Filter");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_DEVICE_LANGUAGE_API", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_DEVICE_LANGUAGE_API(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput =
            LogParserHelper.logParser("locale:en", "language=en", "Package 10007 dropped in Language Targeting Filter",
                "Packages selected:" + " [10006]");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_DEVICE_LANGUAGE_NULL_API", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_DEVICE_LANGUAGE_NULL_API(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput = LogParserHelper.logParser("language=null", "IX request json",
            "10006 dropped in Language " + "Targeting Filter");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }


    // @Test(testName = "TEST_IX_PACKAGES_MANUFDEAL", dataProvider = "fender_ix_dp", dataProviderClass =
    // FenderDataProvider.class)
    // public void TEST_IX_PACKAGES_MANUFDEAL(final String x, final ResponseBuilder responseBuilder) throws Exception {
    //
    // // searchStringInLog = "Sprout Ad Received";
    // // String searchStringInLog2 = "Replaced Sprout Macros";
    // //
    // // response = new String(responseBuilder.getResponseData());
    // //
    // // Assert.assertFalse(response.contains(searchStringInLog), "Found " + searchStringInLog + "in the response");
    // // Assert.assertFalse(response.contains(searchStringInLog2), "Found " + searchStringInLog + "in the response");
    // }
    //
    // @Test(testName = "TEST_IX_PACKAGES_MODELID_WIFI_DEAL", dataProvider = "fender_ix_dp", dataProviderClass =
    // FenderDataProvider.class)
    // public void TEST_IX_PACKAGES_MODELID_WIFI_DEAL(final String x, final ResponseBuilder responseBuilder)
    // throws Exception {
    //
    // // searchStringInLog = "Sprout Ad Received";
    // // String searchStringInLog2 = "Replaced Sprout Macros";
    // //
    // // response = new String(responseBuilder.getResponseData());
    // //
    // // Assert.assertFalse(response.contains(searchStringInLog), "Found " + searchStringInLog + "in the response");
    // // Assert.assertFalse(response.contains(searchStringInLog2), "Found " + searchStringInLog + "in the response");
    // }
    @Test(testName = "TEST_ZONE_ID_FAIL", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_ZONE_ID_FAIL(final String x, final ResponseBuilder responseBuilder) throws Exception {

        searchStringInLog = "zone id not present, will say false";

        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(LogStringParams.MSG_IX_ADAPTER_CONFIG_FAIL_IMP_OBJ_NULL), searchStringInLog);

        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_KILOO", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_KILOO(final String x, final ResponseBuilder responseBuilder) throws Exception {

        // searchStringInLog = "zone id not present, will say false";


        String responseToEncrypt = new String();
        final String grepFrom = "<html>";
        final String grepUpto = "</html>";

        final String response = new String(responseBuilder.getResponseData());
        // System.out.println("\n\n***********************\n\n");
        // System.out.println("RESPONSE IS : " + response);
        // System.out.println("***********************\n\n");

        if (response.contains("<html>")) {
            responseToEncrypt =
                response.substring(response.indexOf(grepFrom), response.indexOf(grepUpto) + grepUpto.length());
        }
        System.out.println("\n\n***********************\n\n");
        // System.out.println("RESPONSE IS : " + response);
        System.out.println("***********************\n\n");
        System.out.println(responseToEncrypt);
        // else {
        // System.out
        // .println("The response received does not contain the expected String.Get a life (and another VAST xml)!");
        // }
        // System.out.println("-----\nENCRYPTED RESPONSE :\n" + responseToEncrypt);
        //
        // TemplateEncrypter.getEncryptedResponseHosted(responseToEncrypt);
    }

    @Test(testName = "TESTIOS9", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TESTIOS9(final String x, final ResponseBuilder responseBuilder) throws Exception {


    }

    @Test(testName = "TEST_RP_ALT_SLOT_IDS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_RP_ALT_SLOT_IDS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput =
            LogParserHelper.logParser("SAS params are", "selectedSlots:[9, 4]", "IX request json is", "\"size_id\":43", "\"alt_size_ids\":[44]");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_ADGROUP_AD_INC_ID_AND_APP_BUNDLE_ID_IOS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_ADGROUP_AD_INC_ID_AND_APP_BUNDLE_ID_IOS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput = LogParserHelper.logParser("AdGroupIncId 123456789", "appBundleId:TEST_MARKET_ID_IOS");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_ADGROUP_AD_INC_ID_AND_APP_BUNDLE_ID_NOT_IOS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_ADGROUP_AD_INC_ID_AND_APP_BUNDLE_ID_NOT_IOS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput = LogParserHelper.logParser("AdGroupIncId 123456790", "appBundleId:TEST_MARKET_ID_NOT_IOS");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_ADGROUP_AD_INC_ID_AND_APP_NO_BUNDLE_ID", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_ADGROUP_AD_INC_ID_AND_APP_NO_BUNDLE_ID(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput = LogParserHelper.logParser("AdGroupIncId 123456791", "appBundleId=null");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    public void VastPassAssert() throws Exception {
        final String impression = "Impression added in VAST xml";
        final String error = "Error added in VAST xml";
        final String clickTracking = "ClickTracking added in VAST xml";
        final String start = "Tracking added in VAST xml key is : event and value is : start";
        final String firstQuartile = "Tracking added in VAST xml key is : event and value is : firstQuartile";
        final String midPoint = "Tracking added in VAST xml key is : event and value is : midpoint";
        final String thirdQuartile = "Tracking added in VAST xml key is : event and value is : thirdQuartile";
        final String complete = "Tracking added in VAST xml key is : event and value is : complete";

        parserOutput =
            LogParserHelper.logParser(impression, error, clickTracking, start, firstQuartile, midPoint, thirdQuartile, complete);
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_VAST_REQUEST_API_WAP_PASS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_REQUEST_API_WAP_PASS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        VastPassAssert();
    }

    @Test(testName = "TEST_VAST_REQUEST_API_WAP_SDK_INTG_VERSION_PASS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_REQUEST_API_WAP_SDK_INTG_VERSION_PASS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        VastPassAssert();
    }

    @Test(testName = "TEST_VAST_REQUEST_API_APP_PASS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_REQUEST_API_APP_PASS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        VastPassAssert();
    }

    @Test(testName = "TEST_VAST_REQUEST_SDK_APP_PASS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_REQUEST_SDK_APP_PASS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        VastPassAssert();
    }

    @Test(testName = "TEST_VAST_REQUEST_SDK_APP_NO_INTG_VERSION_PASS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_REQUEST_SDK_APP_NO_INTG_VERSION_PASS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        VastPassAssert();
    }

    @Test(testName = "TEST_VAST_AD_TYPE_TARGETING_FAIL", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_AD_TYPE_TARGETING_FAIL(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput =
            LogParserHelper.logParser("Failed in filter: AdGroupAdTypeTargetingFilter, adgroup: TEST_VAST_AD_TYPE_TARGETING_FAIL");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_VAST_VIDEO_SLOT_PASS", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_VAST_VIDEO_SLOT_PASS(final String x, final ResponseBuilder responseBuilder) throws Exception {
        VastPassAssert();
    }

    @Test(testName = "TEST_SECURE_SEGMENT_FLAG", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_SECURE_SEGMENT_FLAG(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput = LogParserHelper.logParser("AdGroup : TEST_SECURE_SEGMENT_FLAG");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    @Test(testName = "TEST_SECURE_SEGMENT_FLAG_NEGATIVE", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_SECURE_SEGMENT_FLAG_NEGATIVE(final String x, final ResponseBuilder responseBuilder) throws Exception {
        parserOutput = LogParserHelper.logParser("AdGroup : TEST_SECURE_SEGMENT_FLAG_NEGATIVE");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("FAIL"));
    }

    @Test(testName = "TEST_PHOTON_UID_PRESENT", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_PHOTON_UID_PRESENT(final String x, final ResponseBuilder responseBuilder)
            throws Exception {
        parserOutput = LogParserHelper.logParser("AdGroup : TEST_PHOTON_UID_PRESENT");
        Reporter.log(parserOutput, true);
        Assert.assertTrue(parserOutput.equals("PASS"));
    }

    // @Test(testName = "TEST_RENDER_UNIT_ID_FOR_NATIVE_STRANDS", dataProvider = "fender_ix_dp", dataProviderClass =
    // FenderDataProvider.class)
    // public void TEST_RENDER_UNIT_ID_FOR_NATIVE_STRANDS(final String x, final ResponseBuilder responseBuilder)
    // throws Exception {
    //
    // final LogLines renderUnitInfo = LogParserHelper.queryForLogs("Impression Info Object");
    // final long renderUnitGuidHighBits = Long.parseLong(renderUnitInfo.applyRegex("id_high:[0-9/-]+", "[0-9/-]+"));
    // final long renderUnitGuidLowBits = Long.parseLong(renderUnitInfo.applyRegex("id_low:[0-9/-]+", "[0-9/-]+"));
    // final String renderUnitId = new UUID(renderUnitGuidHighBits, renderUnitGuidLowBits).toString();
    //
    // final LogLines impressionInfo = LogParserHelper.queryForLogs("Replaced impression id to new value");
    // final String impressionId = impressionInfo.applyRegex(CasConf.LogLinesRegex.UUID.getRegex());
    // final String impressionIdWithResetIntKey = resetWilburyIntKey(impressionId, 0);
    //
    // final LogLines adPoolResponse = LogParserHelper.queryForLogs("IX response json to RE is AdPoolResponse");
    // final long adPoolResponseRenderUnitGuidHighBits =
    // Long.parseLong(adPoolResponse.applyRegex("renderUnitId:GUID\\(id_high:[0-9/-]+", "[0-9/-]+"));
    // final long adPoolResponseRenderUnitGuidLowBits =
    // Long.parseLong(adPoolResponse.applyRegex("renderUnitId:GUID\\(id_high:[0-9/-]+, id_low:[0-9/-]+",
    // "id_low:[0-9/-]+", "[0-9/-]+"));
    // final String adPoolResponseRenderUnitId =
    // new UUID(adPoolResponseRenderUnitGuidHighBits, adPoolResponseRenderUnitGuidLowBits).toString();
    //
    // Assert.assertTrue(impressionIdWithResetIntKey.equalsIgnoreCase(renderUnitId),
    // "RenderUnitId must equal impression id with empty int key");
    // Assert.assertTrue(adPoolResponseRenderUnitId.equalsIgnoreCase(renderUnitId),
    // "RenderUnitId in the beacon and AdPoolResponse must match");
    // }
}
