package com.inmobi.adserve.channels.util;

import org.apache.commons.lang.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class GenericTemplateObject {
    public static final String AD_OBJECT_PREFIX = "ad";
    public static final String FIRST_OBJECT_PREFIX = "first";
    public static final String TOOL_OBJECT = "tool";
    
    public static final String VAST_CONTENT_JS_ESC = "VASTContentJSEsc";
    public static final String PARTNER_BEACON_URL = "PartnerBeaconUrl";
    private static final String EMPTY_ARRAY = "[]";
    private static final String ARRAY_PREFIX = "[\"";
    private static final String ARRAY_SUFFIX = "\"]";


    private String beaconUrl;
    private String clickServerUrl;
    @Getter(AccessLevel.NONE)
    private String[] billingUrlArray;
    @Getter(AccessLevel.NONE)
    private String[] clickUrlArray;
    private String ns;

    private int supplyWidth;
    private int supplyHeight;
    private String sdkVersion;
    private String sitePreferencesJson;
    private String requestJson;
    private boolean secure;
    private boolean viewability;

    //CAU AD Markup
    private String cauElementJsonObject;
    public static final String CAU_CONTENT_JS_ESC = "CAUContentJSEsc";
    private int width;
    private int height;

    public String getBillingUrlArray() {
        return billingUrlArray.length > 0 ? ARRAY_PREFIX + StringUtils.join(billingUrlArray, "\", \"") + ARRAY_SUFFIX : EMPTY_ARRAY;
    }
    public String getClickUrlArray() {
        return clickUrlArray.length > 0 ? ARRAY_PREFIX + StringUtils.join(clickUrlArray, "\", \"") + ARRAY_SUFFIX : EMPTY_ARRAY;
    }
}
