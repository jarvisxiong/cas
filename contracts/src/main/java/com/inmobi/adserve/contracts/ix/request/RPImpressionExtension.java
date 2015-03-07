package com.inmobi.adserve.contracts.ix.request;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@Data
public final class RPImpressionExtension {
    @NonNull
    private final String zone_id;
    private String enc;
    private Integer pmptier;
    private Integer dpf = 1;
    private RPTargetingExtension target;
    private RPTargetingExtension track;
    private RPTargetingExtension rtb;
    private RPTargetingExtension nolog;
}
