package com.inmobi.adserve.channels.api;

import com.inmobi.types.adserving.Slot;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;


public class SlotSizeMapping {
    public static final Map<Long, Dimension> SLOT_MAP = new HashMap<Long, Dimension>();
    private static final Map<Short, Integer> IX_SLOT_ID_MAP = new HashMap<Short, Integer>();

    public static void init() {

        SLOT_MAP.put(1L, new Dimension(120, 20));
        SLOT_MAP.put(2L, new Dimension(168, 28));
        SLOT_MAP.put(3L, new Dimension(216, 36));
        SLOT_MAP.put(4L, new Dimension(300, 50));
        SLOT_MAP.put(9L, new Dimension(320, 48));
        SLOT_MAP.put(10L, new Dimension(300, 250));
        SLOT_MAP.put(11L, new Dimension(728, 90));
        SLOT_MAP.put(12L, new Dimension(468, 60));
        SLOT_MAP.put(13L, new Dimension(120, 600));
        SLOT_MAP.put(14L, new Dimension(320, 480));
        SLOT_MAP.put(15L, new Dimension(320, 50));
        SLOT_MAP.put(16L, new Dimension(768, 1024));
        SLOT_MAP.put(17L, new Dimension(800, 1280));
        SLOT_MAP.put(18L, new Dimension(160, 600));
        SLOT_MAP.put(19L, new Dimension(1024, 90));
        SLOT_MAP.put(21L, new Dimension(480, 75));
        SLOT_MAP.put(22L, new Dimension(768, 66));
        SLOT_MAP.put(23L, new Dimension(480, 60));
        SLOT_MAP.put(24L, new Dimension(320, 53));
        SLOT_MAP.put(26L, new Dimension(300, 30));
        SLOT_MAP.put(27L, new Dimension(500, 130));
        SLOT_MAP.put(28L, new Dimension(292, 60));
        SLOT_MAP.put(29L, new Dimension(250, 250));
        SLOT_MAP.put(30L, new Dimension(250, 125));
        SLOT_MAP.put(31L, new Dimension(320, 568));
        SLOT_MAP.put(32L, new Dimension(480, 320));
        SLOT_MAP.put(33L, new Dimension(1024, 768 ));
        SLOT_MAP.put(34L, new Dimension(1280, 800));
        SLOT_MAP.put(35L, new Dimension(320, 30));
        SLOT_MAP.put(36L, new Dimension(320, 26));
        SLOT_MAP.put(37L, new Dimension(320, 100));
        SLOT_MAP.put(38L, new Dimension(320, 568));
        SLOT_MAP.put(39L, new Dimension(568, 320));
        SLOT_MAP.put(40L, new Dimension(250, 300));
        SLOT_MAP.put(0L, new Dimension(0, 0));


        //Adding IX_SLOT_ID_MAP, which is a map from InMobi slot id's to Rubicon slot id's

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

    public static Dimension getDimension(Long slot) {
        return SLOT_MAP.get(slot);
    }

    public static boolean isIXSupportedSlot(short inmobiSlot) {
        return IX_SLOT_ID_MAP.containsKey(inmobiSlot);
    }

    public static Integer getIXMappedSlotId(short inmobiSlot) {
        return IX_SLOT_ID_MAP.get(inmobiSlot);
    }
}
