package com.inmobi.adserve.channels.server.constants;

/**
 * @author abhishek.parwal
 * 
 */
public enum FilterOrder {

    FIRST(0),
    SECOND(1),
    THIRD(2),
    FOURTH(3),
    FIFTH(4),
    SIXTH(5),
    SEVENTH(6),
    EIGHT(7),
    NINTH(8),
    TENTH(9),
    DEFAULT(100),
    THIRD_LAST(998),
    SECOND_LAST(999),
    LAST(1000);

    private int value;

    private FilterOrder(final int value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

}
