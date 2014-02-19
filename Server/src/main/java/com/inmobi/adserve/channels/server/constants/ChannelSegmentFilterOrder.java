package com.inmobi.adserve.channels.server.constants;

/**
 * @author abhishek.parwal
 * 
 */
public enum ChannelSegmentFilterOrder {

    FIRST(0),
    SECOND(1),
    THIRD(2),
    DEFAULT(100),
    THIRD_LAST(998),
    SECOND_LAST(999),
    LAST(1000);

    private int value;

    private ChannelSegmentFilterOrder(final int value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

}
