package com.inmobi.adserve.channels.util;

import lombok.Data;

@Data
public class VASTTemplateObject {
    public static final String AD_OBJECT_PREFIX = "ad";
    public static final String FIRST_OBJECT_PREFIX = "first";
    
    public static final String VAST_CONTENT_JS_ESC = "VASTContentJSEsc";
    public static final String IM_WIN_URL = "IMWinUrl";
    public static final String PARTNER_BEACON_URL = "PartnerBeaconUrl";

    private String beaconUrl;
    private String clickServerUrl;
    private String ns;

    private int supplyWidth;
    private int supplyHeight;
    private String sdkVersion;
    private String sitePreferencesJson;
    private String requestJson;

}
