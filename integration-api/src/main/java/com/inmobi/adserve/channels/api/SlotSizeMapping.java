package com.inmobi.adserve.channels.api;

import java.awt.*;
import java.util.HashMap;


public class SlotSizeMapping {
    public static final HashMap<Long, Dimension> slotMap = new HashMap<Long, Dimension>();

    public static void init() {
        slotMap.put(1L, new Dimension(120, 20));
        slotMap.put(2L, new Dimension(168, 28));
        slotMap.put(3L, new Dimension(216, 36));
        slotMap.put(4L, new Dimension(300, 50));
        slotMap.put(9L, new Dimension(320, 48));
        slotMap.put(10L, new Dimension(300, 250));
        slotMap.put(11L, new Dimension(728, 90));
        slotMap.put(12L, new Dimension(468, 60));
        slotMap.put(13L, new Dimension(120, 600));
        slotMap.put(14L, new Dimension(320, 480));
        slotMap.put(15L, new Dimension(320, 50));
        slotMap.put(16L, new Dimension(768, 1024));
        slotMap.put(17L, new Dimension(800, 1280));
        slotMap.put(18L, new Dimension(160, 600));
        slotMap.put(19L, new Dimension(1024, 90));
        slotMap.put(21L, new Dimension(480, 75));
        slotMap.put(22L, new Dimension(768, 66));
        slotMap.put(23L, new Dimension(480, 60));
        slotMap.put(24L, new Dimension(320, 53));
        slotMap.put(26L, new Dimension(300, 30));
        slotMap.put(27L, new Dimension(500, 130));
        slotMap.put(28L, new Dimension(292, 60));
        slotMap.put(29L, new Dimension(250, 250));
        slotMap.put(30L, new Dimension(250, 125));
        slotMap.put(31L, new Dimension(320, 568));
        slotMap.put(32L, new Dimension(480, 320));
        slotMap.put(33L, new Dimension(1024,768 ));
        slotMap.put(34L, new Dimension(1280, 800));
        slotMap.put(35L, new Dimension(320, 30));
        slotMap.put(36L, new Dimension(320, 26));
        slotMap.put(0L, new Dimension(0, 0));
    }

    public static Dimension getDimension(Long slot) {
        return (slotMap.get(slot));
    }
}
