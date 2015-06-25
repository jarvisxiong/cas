package com.inmobi.adserve.channels.types;

/**
 * Created by ishanbhatnagar on 1/6/15.
 */
public enum IXBlocklistKeyType {
    GLOBAL(0), SITE(1), COUNTRY(2), UNKNOWN(1000);

    private final int key;

    private IXBlocklistKeyType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public static IXBlocklistKeyType getByValue(int key) throws IllegalArgumentException {
        IXBlocklistKeyType ixBlocklistKeyType;
        switch (key) {
            case 0:
                ixBlocklistKeyType = GLOBAL;
                break;
            case 1 :
                ixBlocklistKeyType =  SITE;
                break;
            case 2 :
                ixBlocklistKeyType = COUNTRY;
                break;
            default:
                throw new IllegalArgumentException("Unknown IX Blocklist Key Type");
        }
        return ixBlocklistKeyType;
    }
}
