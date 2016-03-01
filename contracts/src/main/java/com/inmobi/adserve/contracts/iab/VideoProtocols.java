/**
 * Copyright (c) 2015. InMobi, All Rights Reserved.
 */
package com.inmobi.adserve.contracts.iab;

/**
 * @author ritwik.kumar
 *
 */
public enum VideoProtocols {

    VAST_1_0(1), VAST_2_0(2), VAST_3_0(3), VAST_1_0_WRAPPER(4), VAST_2_0_WRAPPER(5), VAST_3_0_WRAPPER(6);

    private Integer value;

    private VideoProtocols(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

}
