package com.inmobi.adserve.channels.api;

import java.awt.*;
import java.util.HashMap;


public class SlotSizeMapping {
    public static final HashMap<Long, Dimension> slotMap = new HashMap<Long, Dimension>();

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
        slotMap.put(35l, new Dimension(320, 30));
        slotMap.put(36l, new Dimension(320, 26));
    }

    public static Dimension getDimension(Long slot) {
        return (slotMap.get(slot));
    }
}
