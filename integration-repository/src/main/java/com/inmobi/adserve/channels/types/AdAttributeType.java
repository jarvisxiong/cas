package com.inmobi.adserve.channels.types;

/**
 * These enum values are mapped with ad_attribute table in wap_prod_adserve db.
 */
public enum AdAttributeType {
    DEFAULT(0),  // NOTE: Banner is included in default.
    VIDEO(2);

    private int value;

    private AdAttributeType(final int value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }
}
