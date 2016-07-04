package com.inmobi.adserve.channels.query;

import java.util.Set;

import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.types.InventoryType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class TargetingSegmentQueryBuilder {
    final DemandSourceType dst;
    final String dsp;
    final Set<Short> slots;
    final SecondaryAdFormatConstraints adFormat;
    final InventoryType inventoryType;
    final Integer os;
    final Integer country;
}
