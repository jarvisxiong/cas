package com.inmobi.adserve.channels.api;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;


public class SlotSizeMapping {
    @Getter
    private static final Map<Short, Dimension> SLOT_MAP = new HashMap<Short, Dimension>();
    @Getter
    private static final Map<Short, Integer> IX_SLOT_ID_MAP = new HashMap<Short, Integer>();

    static {
        SLOT_MAP.put((short) 1, new Dimension(120, 20));
        SLOT_MAP.put((short) 2, new Dimension(168, 28));
        SLOT_MAP.put((short) 3, new Dimension(216, 36));
        SLOT_MAP.put((short) 4, new Dimension(300, 50));
        SLOT_MAP.put((short) 9, new Dimension(320, 48));
        SLOT_MAP.put((short) 10, new Dimension(300, 250));
        SLOT_MAP.put((short) 11, new Dimension(728, 90));
        SLOT_MAP.put((short) 12, new Dimension(468, 60));
        SLOT_MAP.put((short) 13, new Dimension(120, 600));
        SLOT_MAP.put((short) 14, new Dimension(320, 480));
        SLOT_MAP.put((short) 15, new Dimension(320, 50));
        SLOT_MAP.put((short) 16, new Dimension(768, 1024));
        SLOT_MAP.put((short) 17, new Dimension(800, 1280));
        SLOT_MAP.put((short) 18, new Dimension(160, 600));
        SLOT_MAP.put((short) 19, new Dimension(1024, 90));
        SLOT_MAP.put((short) 21, new Dimension(480, 75));
        SLOT_MAP.put((short) 22, new Dimension(768, 66));
        SLOT_MAP.put((short) 23, new Dimension(480, 60));
        SLOT_MAP.put((short) 24, new Dimension(320, 53));
        SLOT_MAP.put((short) 26, new Dimension(300, 30));
        SLOT_MAP.put((short) 27, new Dimension(500, 130));
        SLOT_MAP.put((short) 28, new Dimension(292, 60));
        SLOT_MAP.put((short) 29, new Dimension(250, 250));
        SLOT_MAP.put((short) 30, new Dimension(250, 125));
        SLOT_MAP.put((short) 31, new Dimension(320, 568));
        SLOT_MAP.put((short) 32, new Dimension(480, 320));
        SLOT_MAP.put((short) 33, new Dimension(1024, 768));
        SLOT_MAP.put((short) 34, new Dimension(1280, 800));
        SLOT_MAP.put((short) 35, new Dimension(320, 30));
        SLOT_MAP.put((short) 36, new Dimension(320, 26));
        SLOT_MAP.put((short) 37, new Dimension(320, 100));
        SLOT_MAP.put((short) 38, new Dimension(320, 568));
        SLOT_MAP.put((short) 39, new Dimension(568, 320));
        SLOT_MAP.put((short) 40, new Dimension(250, 300));
        SLOT_MAP.put((short) 0, new Dimension(0, 0));


        // Adding IX_SLOT_ID_MAP, which is a map from InMobi slot id's to Rubicon slot id's

        IX_SLOT_ID_MAP.put((short) 4, 44);
        // Mapping 320x48 to 320x50
        IX_SLOT_ID_MAP.put((short) 9, 43);
        IX_SLOT_ID_MAP.put((short) 10, 15);
        IX_SLOT_ID_MAP.put((short) 11, 2);
        IX_SLOT_ID_MAP.put((short) 12, 1);
        IX_SLOT_ID_MAP.put((short) 13, 8);
        IX_SLOT_ID_MAP.put((short) 14, 67);
        IX_SLOT_ID_MAP.put((short) 15, 43);
        IX_SLOT_ID_MAP.put((short) 16, 102);
        IX_SLOT_ID_MAP.put((short) 18, 9);
        IX_SLOT_ID_MAP.put((short) 19, 50);
        IX_SLOT_ID_MAP.put((short) 21, 45);
        IX_SLOT_ID_MAP.put((short) 23, 46);
        IX_SLOT_ID_MAP.put((short) 29, 14);
        IX_SLOT_ID_MAP.put((short) 32, 101);
        IX_SLOT_ID_MAP.put((short) 33, 53);

    }

    public static Dimension getDimension(final short slot) {
        return SLOT_MAP.get(slot);
    }

    public static boolean isIXSupportedSlot(final short inmobiSlot) {
        return IX_SLOT_ID_MAP.containsKey(inmobiSlot);
    }

    public static Integer getIXMappedSlotId(final short inmobiSlot) {
        return IX_SLOT_ID_MAP.get(inmobiSlot);
    }
}
