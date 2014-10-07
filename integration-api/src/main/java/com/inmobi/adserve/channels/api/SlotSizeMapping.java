package com.inmobi.adserve.channels.api;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;


public class SlotSizeMapping {
    public static final HashMap<Long, Dimension> slotMap = new HashMap<Long, Dimension>();
    private static final Map<Short, Integer> IX_SLOT_ID_MAP = new HashMap<Short, Integer>();

    public static void init() {
        slotMap.put(1l, new Dimension(120, 20));
        slotMap.put(2l, new Dimension(168, 28));
        slotMap.put(3l, new Dimension(216, 36));
        slotMap.put(4l, new Dimension(300, 50));
        slotMap.put(9l, new Dimension(320, 48));
        slotMap.put(10l, new Dimension(300, 250));
        slotMap.put(11l, new Dimension(728, 90));
        slotMap.put(12l, new Dimension(468, 60));
        slotMap.put(13l, new Dimension(120, 600));
        slotMap.put(14l, new Dimension(320, 480));
        slotMap.put(15l, new Dimension(320, 50));
        slotMap.put(16l, new Dimension(768, 1024));
        slotMap.put(17l, new Dimension(800, 1280));
        slotMap.put(18l, new Dimension(160, 600));
        slotMap.put(19l, new Dimension(1024, 90));
        slotMap.put(21l, new Dimension(480, 75));
        slotMap.put(22l, new Dimension(768, 66));
        slotMap.put(23l, new Dimension(480, 60));
        slotMap.put(24l, new Dimension(320, 53));
        slotMap.put(26l, new Dimension(300, 30));
        slotMap.put(27l, new Dimension(500, 130));
        slotMap.put(28l, new Dimension(292, 60));
        slotMap.put(29l, new Dimension(250, 250));
        slotMap.put(30l, new Dimension(250, 125));
        slotMap.put(31l, new Dimension(320, 568));
        slotMap.put(32l, new Dimension(480, 320));
        slotMap.put(33l, new Dimension(1024, 768 ));
        slotMap.put(34l, new Dimension(1280, 800));
        slotMap.put(35l, new Dimension(320, 30));
        slotMap.put(36l, new Dimension(320, 26));
        slotMap.put(37l, new Dimension(320, 100));
        slotMap.put(38l, new Dimension(320, 568));
        slotMap.put(39l, new Dimension(568, 320));
        slotMap.put(40l, new Dimension(250, 300));
        slotMap.put(0l,  new Dimension(0, 0));

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
        return (slotMap.get(slot));
    }

    public static boolean isIXSupportedSlot(short inmobiSlot) {
        return IX_SLOT_ID_MAP.containsKey(inmobiSlot);
    }

    public static Integer getIXMappedSlotId(short inmobiSlot) {
        return IX_SLOT_ID_MAP.get(inmobiSlot);
    }
}
