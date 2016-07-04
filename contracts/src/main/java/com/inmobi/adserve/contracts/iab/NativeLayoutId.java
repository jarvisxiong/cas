package com.inmobi.adserve.contracts.iab;

import com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */
public enum NativeLayoutId {
    CONTENT_WALL((short) 1), APP_WALL((short) 2), NEWS_FEED((short) 3), CHAT_LIST((short) 4), CAROUSEL(
            (short) 5), CONTENT_STREAM((short) 6);

    private final short key;

    NativeLayoutId(short key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static NativeLayoutId findByValue(short value) {
        switch (value) {
            case 1:
                return CONTENT_WALL;
            case 2:
                return APP_WALL;
            case 3:
                return NEWS_FEED;
            case 4:
                return CHAT_LIST;
            case 5:
                return CAROUSEL;
            case 6:
                return CONTENT_STREAM;
            default:
                throw new IllegalArgumentException("Illegal value passed for IAB Native Layout Id: " + value);
        }
    }

    public static NativeLayoutId findByInmobiNativeUILayoutType(NativeAdContentUILayoutType uiLayoutType) {
        if (null == uiLayoutType) {
            return null;
        }

        switch (uiLayoutType) {
            case CONTENT_WALL:
                return CONTENT_WALL;
            case APP_WALL:
                return APP_WALL;
            case NEWS_FEED:
                return NEWS_FEED;
            case CONTENT_STREAM:
                return CONTENT_STREAM;
            case CAROUSEL:
                return CAROUSEL;
            case CHAT_LIST:
                return CHAT_LIST;
            default:
                return null;
        }
    }
}
