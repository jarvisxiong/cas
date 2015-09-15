package com.inmobi.adserve.channels.util;

import com.google.common.collect.ImmutableList;

/**
 * @author ishanbhatnagar
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

        // Non sprout macros
        public static final String IMP_CB = "IMP_CB";
        public static final String USER_ID = "USER_ID";
        public static final String USER_ID_MD5_HASHED = "USER_ID_MD5_HASHED";
        public static final String USER_ID_SHA1_HASHED = "USER_ID_SHA1_HASHED";

        public static final ImmutableList<Character> escapeCharacterList = ImmutableList.of('$', '#');
}
