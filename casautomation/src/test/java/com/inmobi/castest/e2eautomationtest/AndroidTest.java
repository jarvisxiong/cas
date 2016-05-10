package com.inmobi.castest.e2eautomationtest;

/**
 * Created by navaneeth on 8/3/16.
 */

import com.inmobi.castest.casconfenums.def.CasConf;
import com.inmobi.castest.casconfenums.impl.LogStringConf;
import com.inmobi.castest.commons.deviceautomationhelper.AndroidActions;
import com.inmobi.castest.commons.deviceautomationhelper.AndroidAppHandles;
import com.inmobi.castest.commons.generichelper.LogParserHelper;
import com.inmobi.castest.dataprovider.FenderDataProvider;
import io.appium.java_client.AppiumDriver;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class AndroidTest {
    static Process p;
    private String parserOutput = new String();
    public static AppiumDriver driver;

    public static AppiumDriver getDriver() {
        return driver;
    }


    @BeforeTest
    public static void connect() throws IOException, InterruptedException {

        p = Runtime.getRuntime().exec("appium");
        new Thread(new Runnable() {
            public void run() {
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                try {
                    int i = 0;
                    while ((line = input.readLine()) != null) {
                        if (i < 3) {
                            System.out.println(line);
                        }
                        i++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        p.waitFor(5, TimeUnit.SECONDS);



    }


    @Test(testName = "TEST_FOR_ANDROID_BANNER", dataProvider = "fender_e2e_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_FOR_ANDROID_BANNER(String testCaseName) throws Exception {
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(CasConf.LogStringParams.MSG_E2E_IX_ANDROID_BANNER));



        boolean verifyBeacon = AndroidTestHelper.LogHelper();
        System.out.println(verifyBeacon);

        Reporter.log(parserOutput, true);


        Assert.assertTrue(parserOutput.equals("PASS") && verifyBeacon);
    }

    @Test(testName = "TEST_FOR_ANDROID_INTERSTITAL", dataProvider = "fender_e2e_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_FOR_ANDROID_INTERSTITAL(String testCaseName) throws Exception {
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(CasConf.LogStringParams.MSG_E2E_IX_ANDROID_INTERSTITAL));

        boolean verifyBeacon = AndroidTestHelper.LogHelper();

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS") && verifyBeacon);
    }

    @Test(testName = "TEST_FOR_ANDROID_VIDEO", dataProvider = "fender_e2e_dp", dataProviderClass = FenderDataProvider.class)
    public void TEST_FOR_ANDROID_VIDEO(String testCaseName) throws Exception {
        parserOutput =
            LogParserHelper.logParser(LogStringConf.getLogString(CasConf.LogStringParams.MSG_E2E_IX_ANDROID_INTERSTITAL));

        boolean verifyBeacon = AndroidTestHelper.LogHelper();

        Reporter.log(parserOutput, true);

        Assert.assertTrue(parserOutput.equals("PASS") && verifyBeacon);
    }

    @AfterTest
    public void tearDown() throws Exception {

        p.destroy();
    }


}
