package com.inmobi.adserve.channels.api;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;


public class SlotSizeMapping {
    @Getter
    private static final Map<Short, Integer> IX_SLOT_ID_MAP = new HashMap<Short, Integer>();

    static {
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

    public static boolean isIXSupportedSlot(final short inmobiSlot) {
        return IX_SLOT_ID_MAP.containsKey(inmobiSlot);
    }

    public static Integer getIXMappedSlotId(final short inmobiSlot) {
        return IX_SLOT_ID_MAP.get(inmobiSlot);
    }
}
