package com.inmobi.adserve.contracts.iab;

/**
 * 
 * @author ritwik.kumar
 *
 */
public enum ApiFrameworks {
    VPAID_1_1(1), VPAID_2_0(2), MRAID_1(3), ORMMA(4), MRAID_2(5);

    private Integer value;

    private ApiFrameworks(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
