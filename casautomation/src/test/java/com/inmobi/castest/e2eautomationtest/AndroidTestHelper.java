package com.inmobi.castest.e2eautomationtest;

import com.inmobi.castest.commons.deviceautomationhelper.AndroidActions;
import com.inmobi.castest.commons.deviceautomationhelper.AndroidAppHandles;
import com.inmobi.castest.commons.iohelper.YamlDataIOHelper;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by navaneeth on 14/3/16.
 */
public class AndroidTestHelper {

    public static AppiumDriver driver;

    public static Date date = new Date();

    public static SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");

    public static boolean LogHelper() {
        //        driver = AndroidTest.getDriver();
        sd.setTimeZone(TimeZone.getTimeZone("IST"));
        List<LogEntry> logEntries = driver.manage().logs().get("logcat").getAll();
        String logline;

        //        System.out.println("\n" + date.toString().substring(11, 19) + "\n");
        driver.quit();

        for (int i = 0; i < logEntries.size(); i++) {
            logline = logEntries.get(i).toString();
            if (logline.contains("http://et2.w.inmobi.com/") && logline.contains("?m=1")) {

                if (logline.substring(
                    logline.lastIndexOf("[ALL]") + 12,
                    logline.lastIndexOf("[ALL]") + 19).compareTo(sd.format(date).substring(17, 24)) > 0) {
                    System.out.println("\n" + logline + "\n");
                    return true;
                }
            }

        }

        return false;


    }


    public static void Androidhelper(String testcase) throws IOException, InterruptedException {

        AndroidAppHandles app = new AndroidAppHandles();
        AndroidActions action = new AndroidActions();

        date = new Date();
        SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");



        final String dir = System.getProperty("user.dir");
        File appDir = new File(dir + "/Resource/Android");
        File app1 = new File(appDir, "454.apk");
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName", "Android");
        //        capabilities.setCapability("no-reset", "true");
        //        capabilities.setCapability("full-reset", "False");
        //            capabilities.setCapability("no-Reset","true");
        capabilities.setCapability("noReset", "true");
        capabilities.setCapability("fullReset", "false");
        //        capabilities.setCapability("full-Reset","False");
        capabilities.setCapability("app", app1.getAbsolutePath());
        //capabilities.setCapability("chromedriverExecutable","/usr/local/bin/chromedriver");
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        //        wait = new WebDriverWait(driver, 180);

        File configFile = new File("src/test/resources/Siteid.properties");
        if (!configFile.exists()) {
            configFile = new File("casautomation/src/test/resources/Siteid.properties");
            //   System.out.println("if");
        }
        Map<String, String> map = YamlDataIOHelper.readE2ETestParams(testcase);

        FileInputStream reader = new FileInputStream(configFile);
        Properties props = new Properties();
        props.load(reader);

        String Siteid = props.getProperty(testcase);
        app.setUIElements("454");
        action.clickInitialAppSettings(driver, app);

        action.editAppId(driver, app, Siteid);
        driver.hideKeyboard();
        action.clickBtnDone(driver, app);
        action.clickMonetizationButton(driver, app);
        action.clickAdNetworkButton(driver, app);
        action.clickBtSettingsButton(driver, app);
        action.clickEditAdServerUrl(driver, app, "http://10.14.144.119:8080/phoenix/phoenix");
        driver.hideKeyboard();
        action.clickSlotDropdown(driver, app);
        driver.scrollToExact("320 X 48 (9)");
        switch (Integer.parseInt(map.get("adpool_selectedslots"))) {
            case 15:
                action.clickSelectSlot15(driver, app);
                break;
            case 14:
                action.clickSelectSlot14(driver, app);
                break;
            default:
        }
        //action.clickSelectSlot15(driver, app);
        action.clickEditKeyAes(driver, app);
        driver.hideKeyboard();
        action.clickEditKeyIv(driver, app);
        driver.hideKeyboard();
        action.clickRequestParams(driver, app);
        driver.hideKeyboard();
        action.clickBtnDone(driver, app);
        action.clickBtnReload(driver, app);

        Thread.sleep(7000);
        if (map.get("adpool_selectedslots").equals("14")) {
            action.clickBtnShowInterstital(driver, app);
            Thread.sleep(33000);
        }

        Thread.sleep(10000);
    }


}
