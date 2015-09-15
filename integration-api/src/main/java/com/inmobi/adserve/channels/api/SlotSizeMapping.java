package com.inmobi.adserve.channels.api;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author ritwik.kumar<br>
 * <br>
 *         Rubicon Supported Size Ids <br>
 *         Size ID 44: Mobile banner 2 (300x50)<br>
 *         Size ID 43: Mobile Banner 1 (320x50)<br>
 *         Size ID 15: Med Rec (300x250)<br>
 *         Size ID 2: Leaderboard (728x90)<br>
 *         Size ID 1: Banner (468x60)<br>
 *         Size ID 8: Skyscraper (120x600)<br>
 *         Size ID 67: Mobile App Portrait (320x480)<br>
 *         Size ID 102: Tablet Interstitial Port (768x1024)<br>
 *         Size ID 9: Wide Skyscraper (160x600)<br>
 *         Size ID 50: Tablet Landscape Banner (1024x90)<br>
 *         Size ID 45: Mobile Banner 3 (480x75)<br>
 *         Size ID 46: Mobile Banner 4 (480x60)<br>
 *         Size ID 14: Square (250x250)<br>
 *         Size ID 101: Mobile Interstitial Land (480x320)<br>
 *         Size ID 53: Tablet Full Page (1024x768)<br>
 *         Size ID 51: Tablet Portrait Banner (768x900)<br>
 *         Size ID 61: Full page (1000x1000)<br>
 * <br>
 *         InMobi supported slots<br>
 *         wap_prod_adserve=> select id, width, height, ad_type from serving_unit;<br>
 */
public class SlotSizeMapping {
    private static final Map<Short, Integer> IX_SLOT_ID_MAP = new HashMap<Short, Integer>();
    public static final Map<Integer, Dimension> RP_SLOT_DIMENSION = new HashMap<Integer, Dimension>();

    static {
        // Adding IX_SLOT_ID_MAP, which is a map from InMobi slot id's to Rubicon slot id's
        IX_SLOT_ID_MAP.put((short) 0, 0);
        IX_SLOT_ID_MAP.put((short) 4, 44);
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

        // Rubicon Slot Id to Dimension Map
        RP_SLOT_DIMENSION.put(44, new Dimension(300, 50));
        RP_SLOT_DIMENSION.put(43, new Dimension(320, 50));
        RP_SLOT_DIMENSION.put(15, new Dimension(300, 250));
        RP_SLOT_DIMENSION.put(2, new Dimension(728, 90));
        RP_SLOT_DIMENSION.put(1, new Dimension(468, 60));
        RP_SLOT_DIMENSION.put(8, new Dimension(120, 600));
        RP_SLOT_DIMENSION.put(67, new Dimension(320, 480));
        RP_SLOT_DIMENSION.put(102, new Dimension(768, 1024));
        RP_SLOT_DIMENSION.put(9, new Dimension(160, 600));
        RP_SLOT_DIMENSION.put(50, new Dimension(1024, 90));
        RP_SLOT_DIMENSION.put(45, new Dimension(480, 75));
        RP_SLOT_DIMENSION.put(46, new Dimension(480, 60));
        RP_SLOT_DIMENSION.put(14, new Dimension(250, 250));
        RP_SLOT_DIMENSION.put(101, new Dimension(480, 320));
        RP_SLOT_DIMENSION.put(53, new Dimension(1024, 768));
        RP_SLOT_DIMENSION.put(51, new Dimension(768, 900));
        RP_SLOT_DIMENSION.put(61, new Dimension(1000, 1000));
    }

    /**
     * 
     * @param inmobiSlot
     * @return
     */
    public static boolean isIXSupportedSlot(final short inmobiSlot) {
        return IX_SLOT_ID_MAP.containsKey(inmobiSlot);
    }

    /**
     * 
     * @param inmobiSlot
     * @return - Rubicon Size Id
     */
    public static Integer getIXMappedSlotId(final short inmobiSlot) {
        return IX_SLOT_ID_MAP.get(inmobiSlot);
    }
}
