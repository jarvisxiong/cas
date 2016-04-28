/**
 * Copyright (c) 2015. InMobi, All Rights Reserved.
 */
package com.inmobi.adserve.contracts.common.response.nativead;

/**
 * @author ritwik.kumar
 *
 */
public class DefaultResponses {
    public static final String DEFAULT_CTA = "Know More";
    public static final String DEFAULT_DESC = "Tap to learn more";
    public static final String DEFAULT_TITLE = "Sponsored Ad";
    public static final String DEFAULT_RATING = "4";
    public static final int DEFAULT_DOWNLOAD = 0;
    public static final Image DEFAULT_ICON = new Image();

    static {
        DEFAULT_ICON.setH(180);
        DEFAULT_ICON.setW(180);
        DEFAULT_ICON.setUrl("https://i.l.inmobicdn.net/banners/programmatic/taboola.png");
    }
}
