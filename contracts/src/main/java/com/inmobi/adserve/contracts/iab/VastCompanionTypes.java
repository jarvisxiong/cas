package com.inmobi.adserve.contracts.iab;

/**
 * 
 * @author ritwik.kumar
 *
 */
public enum VastCompanionTypes {
    STATIC_RESOURCE(1), HTML_RESOURCE(2), IFRAME_RESOURCE(3);

    private Integer value;

    private VastCompanionTypes(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
