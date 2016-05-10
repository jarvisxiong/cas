package com.inmobi.castest.commons.deviceautomationhelper;

/**
 * Created by arshi on 8/3/16.
 */
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class AndroidActions {
    WebElement webElement;

    public void clickInitialAppSettings(AppiumDriver driver, AndroidAppHandles app) {
        while (driver.findElements(By.xpath(app.getAppSettingbyxPathID())).isEmpty()) {
            System.out.println("App Settings");
        }
        webElement = driver.findElement(By.xpath(app.getAppSettingbyxPathID()));
        webElement.click();
    }

    public void editAppId(AppiumDriver driver, AndroidAppHandles app, String appId) {
        webElement = driver.findElement(By.id(app.getEditAppId()));
        webElement.click();
        webElement.clear();
        webElement.sendKeys(appId);
    }

    public void clickMonetizationButton(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        while (driver.findElements(By.id(app.getMonetizationID())).isEmpty()) {
            System.out.println("Monetization");
        }
        webElement = driver.findElement(By.id(app.getMonetizationID()));
        webElement.click();
    }


    public void clickAdNetworkButton(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        webElement = driver.findElement(By.id(app.getAdNetworkID()));
        webElement.click();
    }


    public void clickRequestParams(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        webElement = driver.findElement(By.id(app.getRequestParamsId()));
        webElement.click();
        webElement.clear();
        webElement.sendKeys("mk-carrier=3.0.119.0");
    }

    public void clickBtSettingsButton(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        webElement = driver.findElement(By.id(app.getBtnSettingsID()));
        webElement.click();
    }


    public void clickEditAdServerUrl(AppiumDriver driver, AndroidAppHandles app, String AdServeURL) throws InterruptedException {
        while (driver.findElements(By.id(app.getEditAdServerUrlID())).isEmpty()) {
            System.out.println("AdServerUrl");
        }
        webElement = driver.findElement(By.id(app.getEditAdServerUrlID()));
        webElement.click();
        webElement.clear();
        webElement.sendKeys(AdServeURL);
    }


    public void clickSlotDropdown(AppiumDriver driver, AndroidAppHandles app) {
        webElement = driver.findElement(By.id(app.getSlotSelectorID()));
        webElement.click();
    }

    public void clickSelectSlot15(AppiumDriver driver, AndroidAppHandles app) {
        webElement = driver.findElement(By.xpath(app.getSlot15byXpath()));
        webElement.click();
    }

    public void clickSelectSlot14(AppiumDriver driver, AndroidAppHandles app) {
        webElement = driver.findElement(By.xpath(app.getSlot14byXpath()));
        webElement.click();
    }


    public void clickEditKeyAes(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        webElement = driver.findElement(By.id(app.getEditKeyAesID()));
        webElement.click();
        webElement.clear();
        webElement.sendKeys("abcdefghijklmnop");
    }


    public void clickEditKeyIv(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        webElement = driver.findElement(By.id(app.getEditKeyIvID()));
        webElement.click();
        webElement.clear();
        webElement.sendKeys("abcdefghijklmnop");
    }


    public void clickBtnDone(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        webElement = driver.findElement(By.id(app.getBtnDoneID()));
        webElement.click();
    }

    public void clickBtnReload(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        while (driver.findElements(By.id(app.getBtnReloadID())).isEmpty()) {
            System.out.println("Btn Reload");
        }
        webElement = driver.findElement(By.id(app.getBtnReloadID()));
        webElement.click();
    }

    public void clickBtnShowInterstital(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        while (driver.findElements(By.id(app.getBtnShowInterstitial())).isEmpty()) {
            System.out.println("Btn Reload");
        }
        webElement = driver.findElement(By.id(app.getBtnShowInterstitial()));
        webElement.click();
    }


    public void clickBtnBack(AppiumDriver driver, AndroidAppHandles app) throws InterruptedException {
        webElement = driver.findElement(By.id(app.getBtnBackID()));
        webElement.click();
    }

}
