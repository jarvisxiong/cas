package com.inmobi.castest.commons.deviceautomationhelper;

/**
 * Created by arshi on 8/3/16.
 */
public class AndroidAppHandles {
    private String AppSettingbyxPathID = "";
    private String EditAppId = "";
    private String MonetizationID = " ";
    private String AdNetworkID = " ";
    private String BtnSettingsID = " ";
    private String EditAdServerUrlID = " ";
    private String SlotSelectorID = " ";
    private String PlacementTypeID = " ";
    private String Slot15byXpathID = " ";
    private String Slot14byXpathID = " ";
    private String InterstitialbyXpathID = " ";
    private String EditKeyAesID = " ";
    private String EditKeyIvID = " ";
    private String RequestParamsId = "";
    private String BtnDoneID = " ";
    private String BtnReloadID = " ";
    private String BtnBackID = " ";
    private String BtnShowInterstitial = " ";

    public void setUIElements(String version) {
        switch (version) {
            case "454": {
                AppSettingbyxPathID =
                    "//android.view.View[1]/android.widget.FrameLayout[1]/android.view.View[1]/android.widget.LinearLayout[2]/android.widget.TextView[1]";
                // AppSettingbyxPathID = "//android.view.View[1]/android.widget.FrameLayout[1]/android.view.View[1]/android.widget.LinearLayout[1]/android.widget.TextView[1]";
                EditAppId = "com.inmobi.app.kitchensink452:id/editAppId";
                MonetizationID = "com.inmobi.app.kitchensink452:id/btnMonetization";
                AdNetworkID = "com.inmobi.app.kitchensink452:id/btnAdNetwork";
                BtnSettingsID = "com.inmobi.app.kitchensink452:id/btnSettings";
                EditAdServerUrlID = "com.inmobi.app.kitchensink452:id/editAdServer";
                SlotSelectorID = "com.inmobi.app.kitchensink452:id/spinnerAdSize";
                Slot15byXpathID = "//android.widget.ListView[1]/android.widget.CheckedTextView[7]";
                Slot14byXpathID = "//android.widget.ListView[1]/android.widget.CheckedTextView[6]";
                EditKeyAesID = "com.inmobi.app.kitchensink452:id/editKeyAes";
                EditKeyIvID = "com.inmobi.app.kitchensink452:id/editKeyIv";
                RequestParamsId = "com.inmobi.app.kitchensink452:id/editReqParams";
                BtnDoneID = "com.inmobi.app.kitchensink452:id/btnDone";
                BtnReloadID = "com.inmobi.app.kitchensink452:id/btnReload";
                BtnBackID = "com.inmobi.app.kitchensink452:id/btnBack";
                BtnShowInterstitial = "com.inmobi.app.kitchensink452:id/btnShowIntAd";

                break;

            }

        }

    }

    public String getRequestParamsId() {
        return RequestParamsId;
    }

    public String getMonetizationID() {
        return MonetizationID;
    }

    public String getEditAppId() {
        return EditAppId;
    }

    public String getAppSettingbyxPathID() {
        return AppSettingbyxPathID;
    }

    public String getAdNetworkID() {
        return AdNetworkID;
    }

    public String getBtnSettingsID() {
        return BtnSettingsID;
    }

    public String getEditAdServerUrlID() {
        return EditAdServerUrlID;
    }

    public String getSlotSelectorID() {
        return SlotSelectorID;
    }

    public String getSlot15byXpath() {
        return Slot15byXpathID;
    }

    public String getSlot14byXpath() {
        return Slot14byXpathID;
    }

    public String getEditKeyAesID() {
        return EditKeyAesID;
    }

    public String getEditKeyIvID() {
        return EditKeyIvID;
    }

    public String getBtnDoneID() {
        return BtnDoneID;
    }

    public String getBtnReloadID() {
        return BtnReloadID;
    }

    public String getBtnBackID() {
        return BtnBackID;
    }

    public String getBtnShowInterstitial() {
        return BtnShowInterstitial;
    }

}
