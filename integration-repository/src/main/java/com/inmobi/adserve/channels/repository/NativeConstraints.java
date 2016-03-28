package com.inmobi.adserve.channels.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.inmobi.adserve.contracts.common.request.nativead.Image.ImageAssetType;
import com.inmobi.adserve.contracts.common.request.nativead.Image;

/**
 *
 * @author ritwik.kumar
 */
public class NativeConstraints {

    public enum Mandatory {
        ICON, SCREEN_SHOT, TITLE, DESCRIPTION;
    }

    // https://github.corp.inmobi.com/ci/publisher-core/blob/master/src/main/java/com/inmobi/publisher/core/constant/enums/ICON.java
    // https://github.corp.inmobi.com/ci/publisher-core/blob/master/src/main/java/com/inmobi/publisher/core/constant/enums/NativeDemandLayout.java
    public static final String LAYOUT_ICON = "layoutConstraint.1"; // Icon
    public static final String LAYOUT_FEED = "layoutConstraint.2"; // Feed
    public static final String LAYOUT_STREAM = "layoutConstraint.3"; // In-Stream

    private static final List<Mandatory> MAND_ICON =
            Lists.newArrayList(Mandatory.ICON, Mandatory.TITLE, Mandatory.DESCRIPTION);
    private static final List<Mandatory> MAND_FEED =
            Lists.newArrayList(Mandatory.ICON, Mandatory.TITLE, Mandatory.DESCRIPTION);
    private static final List<Mandatory> MAND_STREAM =
            Lists.newArrayList(Mandatory.ICON, Mandatory.TITLE, Mandatory.DESCRIPTION, Mandatory.SCREEN_SHOT);

    // https://github.corp.inmobi.com/ci/publisher-core/blob/master/src/main/java/com/inmobi/publisher/core/constant/enums/SCREENSHOT.java
    // key, aspect ratio, minW, maxW, minH, maxH
    private static final String[] INM_TAG_A083 = new String[] {"inmTag.a083", "0.83", "250", "250", "300", "300"};
    private static final String[] INM_TAG_A067 = new String[] {"inmTag.a067", "0.67", "320", "800", "480", "1200"};
    private static final String[] INM_TAG_A056 = new String[] {"inmTag.a056", "0.56", "320", "720", "568", "1280"};
    private static final String[] INM_TAG_A12 = new String[] {"inmTag.a12", "1.2", "300", "300", "250", "250"};
    private static final String[] INM_TAG_A15 = new String[] {"inmTag.a15", "1.5", "480", "1200", "320", "800"};
    private static final String[] INM_TAG_A177 = new String[] {"inmTag.a177", "1.77", "568", "1280", "320", "720"};
    private static final String[] INM_TAG_A191 = new String[] {"inmTag.a191", "1.91", "600", "1200", "313", "627"};
    private static final String[] INM_TAG_A64 = new String[] {"inmTag.a64", "6.4", "320", "320", "50", "50"};
    private static final String[] INM_TAG_A808 = new String[] {"inmTag.a808", "12", "728", "728", "90", "90"};
    private static final String[][] IMAGE_ARR_KEYS = new String[][] {INM_TAG_A083, INM_TAG_A067, INM_TAG_A056,
            INM_TAG_A12, INM_TAG_A15, INM_TAG_A177, INM_TAG_A191, INM_TAG_A64, INM_TAG_A808};

    // private static final Map<String, List<Mandatory>> DCP_MANDATORY = new HashMap<>();
    private static final Map<String, List<Mandatory>> MANDATORY_MAP = new HashMap<>();
    private static final Map<String, Image> IMG_MAP = new HashMap<>();

    static {
        // IX Mandatory maps
        MANDATORY_MAP.put(LAYOUT_ICON, MAND_ICON);
        MANDATORY_MAP.put(LAYOUT_FEED, MAND_FEED);
        MANDATORY_MAP.put(LAYOUT_STREAM, MAND_STREAM);

        // Add image keys to IX and RTBD Maps
        for (final String[] arr : IMAGE_ARR_KEYS) {
            final String key = arr[0];
            // final double ar = Double.parseDouble(arr[1]);
            final int minW = Integer.parseInt(arr[2]);
            // final int maxW = Integer.parseInt(arr[3]);
            final int minH = Integer.parseInt(arr[4]);
            IMG_MAP.put(key, getNativeImage(minW, minH));

        }
    }

    /**
     *
     * @param wMin
     * @param hMin
     * @return
     */
    private static Image getNativeImage(final int wMin, final int hMin) {
        final Image image = new Image();
        image.setType(ImageAssetType.MAIN);
        // image.setWmin(wMin);
        image.setW(wMin);
        // image.setHmin(hMin);
        image.setH(hMin);
        return image;
    }

    /**
     * 
     * @param key
     * @return
     */
    public static boolean isMandatoryKey(final String key) {
        return MANDATORY_MAP.containsKey(key);
    }

    /**
     * 
     * @param key
     * @return
     */
    public static boolean isImageKey(final String key) {
        return IMG_MAP.containsKey(key);
    }

    /**
     * 
     * @param key
     * @return
     */
    public static List<Mandatory> getMandatoryList(final String key) {
        return MANDATORY_MAP.get(key);
    }

    /**
     *
     * @param key
     * @return
     */
    public static Image getImage(final String key) {
        final Image img = IMG_MAP.get(key);
        if (img != null) {
            return new Image(img);
        }
        return img;
    }


}
