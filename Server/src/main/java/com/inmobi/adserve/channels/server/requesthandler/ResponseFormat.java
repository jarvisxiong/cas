package com.inmobi.adserve.channels.server.requesthandler;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Created by avinash.kumar on 4/28/16.
 */
public enum ResponseFormat {
    XHTML("axml",
        "xhtml"), HTML("html"), IMAI("imai"), NATIVE("native"), JS_AD_CODE("jsAdCode"), JSON("json"), VAST("vast");

    private String[] formats;
    private static final Map<String, ResponseFormat> STRING_TO_FORMAT_MAP = Maps.newHashMap();

    static {
        for (final ResponseFormat responseFormat : ResponseFormat.values()) {
            for (final String format : responseFormat.formats) {
                STRING_TO_FORMAT_MAP.put(format.toLowerCase(), responseFormat);
            }
        }
    }

    private ResponseFormat(final String... formats) {
        this.formats = formats;
    }

    public static ResponseFormat getValue(final String format) {
        return STRING_TO_FORMAT_MAP.get(format.toLowerCase());
    }
}
