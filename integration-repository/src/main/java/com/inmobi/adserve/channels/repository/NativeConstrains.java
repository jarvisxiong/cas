package com.inmobi.adserve.channels.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.inmobi.casthrift.rtb.Image;

public class NativeConstrains {

    public static final int ICON = 0;
    public static final int MEDIA = 1;
    public static final int HEADLINE = 2;
    public static final int DESCRIPTION = 3;


    public enum Mandatory {
        ICON(NativeConstrains.ICON, "icon"), MEDIA(NativeConstrains.MEDIA, "Media"), HEADLINE(
                NativeConstrains.HEADLINE, "Headline"), DESCRIPTION(NativeConstrains.DESCRIPTION, "Description");

        private final int index;
        private final String name;

        private Mandatory(final int index, final String name) {
            this.index = index;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }
    }

    public static final String LAYOUT_CONSTRAINT_3 = "layoutConstraint.3";
    public static final String LAYOUT_CONSTRAINT_2 = "layoutConstraint.2";
    public static final String LAYOUT_CONSTRAINT_1 = "layoutConstraint.1";

    public static final String INM_TAG_A083 = "inmTag.a083";
    public static final String INM_TAG_A067 = "inmTag.a067";
    public static final String INM_TAG_A056 = "inmTag.a056";
    public static final String INM_TAG_A12 = "inmTag.a12";
    public static final String INM_TAG_A15 = "inmTag.a15";
    public static final String INM_TAG_A177 = "inmTag.a177";
    public static final String INM_TAG_A191 = "inmTag.a191";
    public static final String INM_TAG_A64 = "inmTag.a64";
    public static final String INM_TAG_A808 = "inmTag.a808";


    private static Map<String, List<Integer>> mandatoryMap = new HashMap<>();
    private static Map<String, Image> imageMap = new HashMap<>();

    static {
        mandatoryMap.put(LAYOUT_CONSTRAINT_1,
                Lists.asList(Mandatory.ICON.getIndex(), new Integer[] {Mandatory.HEADLINE.getIndex()}));
        mandatoryMap.put(
                LAYOUT_CONSTRAINT_2,
                Lists.asList(Mandatory.ICON.getIndex(), new Integer[] {Mandatory.HEADLINE.getIndex(),
                        Mandatory.DESCRIPTION.getIndex()}));
        mandatoryMap.put(
                LAYOUT_CONSTRAINT_3,
                Lists.asList(Mandatory.ICON.getIndex(),
                        new Integer[] {Mandatory.HEADLINE.getIndex(), Mandatory.MEDIA.getIndex()}));

        imageMap.put(INM_TAG_A056, getImage(0.56, 320, 720));
        imageMap.put(INM_TAG_A067, getImage(0.67, 320, 800));
        imageMap.put(INM_TAG_A083, getImage(0.83, 250, 250));
        imageMap.put(INM_TAG_A12, getImage(1.2, 300, 300));
        imageMap.put(INM_TAG_A15, getImage(1.5, 480, 1200));
        imageMap.put(INM_TAG_A177, getImage(1.77, 568, 1280));
        imageMap.put(INM_TAG_A191, getImage(1.91, 600, 1200));
        imageMap.put(INM_TAG_A64, getImage(6.4, 320, 320));
        imageMap.put(INM_TAG_A808, getImage(12, 728, 728));
    }


    private static Image getImage(final double ar, final int minW, final int maxW) {
        final Image img = new Image();
        img.setAspectratio(ar);
        img.setMaxwidth(maxW);
        img.setMinwidth(minW);
        return img;
    }

    public static boolean isMandatoryKey(final String key) {
        return mandatoryMap.containsKey(key);
    }

    public static boolean isImageKey(final String key) {
        return imageMap.containsKey(key);
    }


    public static List<Integer> getMandatoryList(final String key) {
        return mandatoryMap.get(key);
    }

    public static Image getImage(final String key) {
        final Image img = imageMap.get(key);
        if (img != null) {
            return img.deepCopy();
        }
        return img;
    }

}
