package com.inmobi.adserve.channels.server.requesthandler;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

/**
 * Created by avinash.kumar on 4/28/16.
 */
public class AdResponseTemplate {
    public static final String END_TAG = " ]]></Ad></Ads></AdResponse>";
    public static final String AD_IMAI_START_TAG = "<!DOCTYPE html>";
    public static final String NO_AD_IMAI = StringUtils.EMPTY;
    public static final String NO_AD_XHTML = "<AdResponse><Ads></Ads></AdResponse>";
    public static final String NO_AD_VAST = "<VAST version=\"2.0\"></VAST>"; // SERV-4724
    public static final String DCP_NATIVE_WRAPPING_AD_JSON = "{\"requestId\":\"%s\",\"ads\":[%s]}";

    public static final String NO_AD_HTML = "<!-- mKhoj: No advt for this position -->";
    public static final String NO_AD_JS_ADCODE = "<html><head><title></title><style type=\"text/css\">"
            + " body {margin: 0; overflow: hidden; background-color: transparent}"
            + " </style></head><body class=\"nofill\"><!-- NO FILL -->"
            + "<script type=\"text/javascript\" charset=\"utf-8\">"
            + "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";

    public static final String SDK_500_DCP_WRAPPING_AD_JSON =
            "{\"requestId\":\"%s\",\"ads\":[{\"pubContent\":\"%s\"}]}";

    public static final String START_TAG =
            "<AdResponse><Ads number=\"1\"><Ad type=\"rm\" width=\"%s\" height=\"%s\"><![CDATA[";

    public static Set<String> SUPPORTED_RESPONSE_FORMATS =
            Sets.newHashSet("html", "xhtml", "axml", "imai", "native", "json", "vast");
}
