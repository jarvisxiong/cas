package com.inmobi.adserve.contracts.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
// https://github.corp.inmobi.com/ci/publisher-core/blob/master/src/main/java/com/inmobi/publisher/core/constant/enums/NativeAdContentUILayoutType.java
public enum NativeAdContentUILayoutType {
    NEWS_FEED(1), CONTENT_WALL(2), CONTENT_STREAM(3), CAROUSEL(4), CHAT_LIST(5), APP_WALL(6);

    private final int id;

    public static NativeAdContentUILayoutType findByValue(int value) throws IllegalArgumentException {
        switch (value) {
            case 1:
                return NEWS_FEED;
            case 2:
                return CONTENT_WALL;
            case 3:
                return CONTENT_STREAM;
            case 4:
                return CAROUSEL;
            case 5:
                return CHAT_LIST;
            case 6:
                return APP_WALL;
            default:
                throw new IllegalArgumentException("Illegal value passed for NativeAdContentLayoutType : " + value);
        }
    }
}
