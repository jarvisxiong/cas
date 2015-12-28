package com.inmobi.adserve.channels.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.inmobi.adserve.contracts.common.request.nativead.Image.ImageAssetType;
import com.inmobi.casthrift.rtb.Image;

/**
 *
 * @author ritwik.kumar
 */
public class NativeConstraints {
    public static final int ICON_INDEX = 0;
    public static final int SCREEN_SHOT_INDEX = 1;
    public static final int TITLE_INDEX = 2;
    public static final int DESCRIPTION_INDEX = 3;

    public enum Mandatory {
        ICON(ICON_INDEX), SCREEN_SHOT(SCREEN_SHOT_INDEX), TITLE(TITLE_INDEX), DESCRIPTION(DESCRIPTION_INDEX);

        private final int index;

        private Mandatory(final int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    // https://github.corp.inmobi.com/ci/publisher-core/blob/master/src/main/java/com/inmobi/publisher/core/constant/enums/ICON.java
    // https://github.corp.inmobi.com/ci/publisher-core/blob/master/src/main/java/com/inmobi/publisher/core/constant/enums/NativeDemandLayout.java
    public static final String LAYOUT_ICON = "layoutConstraint.1"; // Icon
    public static final String LAYOUT_FEED = "layoutConstraint.2"; // Feed
    public static final String LAYOUT_STREAM = "layoutConstraint.3"; // In-Stream

    private static final List<Mandatory> MAND_ICON = Lists.newArrayList(Mandatory.ICON, Mandatory.TITLE,
        Mandatory.DESCRIPTION);
    private static final List<Mandatory> MAND_FEED = Lists.newArrayList(Mandatory.ICON, Mandatory.TITLE,
        Mandatory.DESCRIPTION);
    private static final List<Mandatory> MAND_STREAM = Lists.newArrayList(Mandatory.ICON, Mandatory.TITLE,
        Mandatory.DESCRIPTION, Mandatory.SCREEN_SHOT);

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

    private static final Map<String, List<Integer>> RTB_MANDATORY = new HashMap<>();
    private static final Map<String, List<Mandatory>> DCP_MANDATORY = new HashMap<>();
    private static final Map<String, List<Mandatory>> IX_MANDATORY = new HashMap<>();
    private static final Map<String, Image> RTB_IMG = new HashMap<>();
    private static final Map<String, com.inmobi.adserve.contracts.common.request.nativead.Image> DCP_IMG = new HashMap<>();
    private static final Map<String, com.inmobi.adserve.contracts.common.request.nativead.Image> IX_IMG = new HashMap<>();

    static {
        RTB_MANDATORY.put(LAYOUT_ICON, getIndexList(MAND_ICON));
        RTB_MANDATORY.put(LAYOUT_FEED, getIndexList(MAND_FEED));
        RTB_MANDATORY.put(LAYOUT_STREAM, getIndexList(MAND_STREAM));

        DCP_MANDATORY.put(LAYOUT_ICON, MAND_ICON);
        DCP_MANDATORY.put(LAYOUT_FEED, MAND_FEED);
        DCP_MANDATORY.put(LAYOUT_STREAM, MAND_STREAM);

        // IX Mandatory maps
        IX_MANDATORY.put(LAYOUT_ICON, MAND_ICON);
        IX_MANDATORY.put(LAYOUT_FEED, MAND_FEED);
        IX_MANDATORY.put(LAYOUT_STREAM, MAND_STREAM);

        // Add image keys to IX and RTBD Maps
        for (final String[] arr : IMAGE_ARR_KEYS) {
            final String key = arr[0];
            final double ar = Double.parseDouble(arr[1]);
            final int minW = Integer.parseInt(arr[2]);
            final int maxW = Integer.parseInt(arr[3]);
            final int minH = Integer.parseInt(arr[4]);
            RTB_IMG.put(key, getRTBImage(ar, minW, maxW));
            IX_IMG.put(key, getIXImage(minW, minH));
            DCP_IMG.put(key, getDCPImage(minW, minH));

        }
    }

    /**
     *
     * @param mandList
     * @return
     */
    private static List<Integer> getIndexList(final List<Mandatory> mandList) {
        final List<Integer> intList = new ArrayList<>();
        for (final Mandatory mand : mandList) {
            intList.add(mand.getIndex());
        }
        return intList;
    }


    /**
     * Get RTB Image
     *
     * @param ar
     * @param minW
     * @param maxW
     * @return
     */
    private static Image getRTBImage(final double ar, final int minW, final int maxW) {
        final Image img = new Image();
        img.setAspectratio(ar);
        img.setMaxwidth(maxW);
        img.setMinwidth(minW);
        return img;
    }

    /**
     *
     * @param wMin
     * @param hMin
     * @return
     */

    private static com.inmobi.adserve.contracts.common.request.nativead.Image getDCPImage(final int wMin, final int hMin) {
        final com.inmobi.adserve.contracts.common.request.nativead.Image
            image = new com.inmobi.adserve.contracts.common.request.nativead.Image();
        image.setType(ImageAssetType.MAIN);
        image.setWmin(wMin);
        image.setHmin(hMin);
        return image;
    }

    /**
     *
     * @param wMin
     * @param hMin
     * @return
     */
    private static com.inmobi.adserve.contracts.common.request.nativead.Image getIXImage(final int wMin, final int hMin) {
        final com.inmobi.adserve.contracts.common.request.nativead.Image image =
                new com.inmobi.adserve.contracts.common.request.nativead.Image();
        image.setType(ImageAssetType.MAIN);
        image.setWmin(wMin);
        image.setHmin(hMin);
        return image;
    }

    public static boolean isMandatoryKey(final String key) {
        return RTB_MANDATORY.containsKey(key);
    }

    public static boolean isImageKey(final String key) {
        return RTB_IMG.containsKey(key);
    }

    public static List<Integer> getRTBDMandatoryList(final String key) {
        return RTB_MANDATORY.get(key);
    }

    public static Image getRTBImage(final String key) {
        final Image img = RTB_IMG.get(key);
        if (img != null) {
            return img.deepCopy();
        }
        return img;
    }

    /**
     *
     * @param key
     * @return
     */
    public static com.inmobi.adserve.contracts.common.request.nativead.Image getIXImage(final String key) {
        final com.inmobi.adserve.contracts.common.request.nativead.Image img = IX_IMG.get(key);
        if (img != null) {
            return new com.inmobi.adserve.contracts.common.request.nativead.Image(img);
        }
        return img;
    }

    /**
     *
     * @param key
     * @return
     */
    public static com.inmobi.adserve.contracts.common.request.nativead.Image getDCPImage(final String key) {
        final com.inmobi.adserve.contracts.common.request.nativead.Image img = DCP_IMG.get(key);
        if (img != null) {
            return new com.inmobi.adserve.contracts.common.request.nativead.Image(img);
        }
        return img;
    }

    /**
     *
     * @param key
     * @return
     */
    public static List<Mandatory> getIXMandatoryList(final String key) {
        return IX_MANDATORY.get(key);
    }

    public static List<Mandatory> getDCPMandatoryList(final String key) {
        return DCP_MANDATORY.get(key);
    }

}
