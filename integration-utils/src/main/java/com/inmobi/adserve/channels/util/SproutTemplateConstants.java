package com.inmobi.adserve.channels.util;

import com.google.common.collect.ImmutableList;

/**
 * @author ishanbhatnagar
 * Please refer:
 * 1) https://github.corp.inmobi.com/adserving/network-adpool/blob/develop/network-adpool-runtimes/src/main/java/com/inmobi/adserve/networkadpool/utils/MacroDataCreator.java
 * 2) https://github.corp.inmobi.com/adserving/adserve-commons-phoenix_components-urlbuilder/blob/master/src/main/java/com/inmobi/adserve/macros/MacroValueFinder.java
 */
public class SproutTemplateConstants {
    public static final String JS_ESC_BEACON_URL = "JS_ESC_BEACON_URL";
    public static final String SDK_VERSION_ID = "SDK_VERSION_ID";
    public static final String JS_ESC_CLICK_URL = "JS_ESC_CLICK_URL";
    public static final String RECORD_EVENT_FUN = "RECORD_EVENT_FUN";
    public static final String GEO_LAT = "GEO_LAT";
    public static final String GEO_LNG = "GEO_LNG";
    public static final String GEO_CC = "GEO_CC";
    public static final String GEO_ZIP = "GEO_ZIP";
    public static final String JS_ESC_GEO_CITY = "JS_ESC_GEO_CITY";
    public static final String OPEN_LP_FUN = "OPEN_LP_FUN";
    public static final String SITE_PREFERENCES_JSON = "SITE_PREFERENCES_JSON";
    public static final String JS_ESC_SITE_PREFERENCES_JSON = "JS_ESC_SITE_PREFERENCES_JSON";
    public static final String SECURE = "SECURE";

    // Non sprout macros
    public static final String IMP_CB = "IMP_CB";
    public static final String USER_ID = "USER_ID";
    public static final String USER_ID_MD5_HASHED = "USER_ID_MD5_HASHED";
    public static final String USER_ID_SHA1_HASHED = "USER_ID_SHA1_HASHED";
    public static final String HANDSET_NAME = "HANDSET_NAME";
    public static final String HANDSET_TYPE = "HANDSET_TYPE";
    public static final String SI_BLIND = "SI_BLIND";


    public static final ImmutableList<Character> escapeCharacterList = ImmutableList.of('$', '#');
}
