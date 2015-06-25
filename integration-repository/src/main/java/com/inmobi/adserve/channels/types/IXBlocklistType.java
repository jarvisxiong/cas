package com.inmobi.adserve.channels.types;

/**
 * Created by ishanbhatnagar on 16/6/15.
 */
public enum IXBlocklistType {
    ADVERTISERS(0), INDUSTRY_IDS(1), CREATIVE_ATTRIBUTE_IDS(2), UNKNOWN(1000);

    private final int key;

    private IXBlocklistType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static IXBlocklistType getByValue(int key) throws IllegalArgumentException {
        IXBlocklistType ixBlocklistType;
        switch (key) {
            case 0:
                ixBlocklistType = ADVERTISERS;
                break;
            case 1 :
                ixBlocklistType =  INDUSTRY_IDS;
                break;
            case 2 :
                ixBlocklistType = CREATIVE_ATTRIBUTE_IDS;
                break;
            default:
                throw new IllegalArgumentException("Unknown IX Blocklist Type");
        }
        return ixBlocklistType;
    }
}
