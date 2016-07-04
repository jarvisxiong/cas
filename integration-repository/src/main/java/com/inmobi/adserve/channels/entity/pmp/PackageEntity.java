package com.inmobi.adserve.channels.entity.pmp;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;


@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public final class PackageEntity {
    private final int id;
    @Getter @Accessors(fluent = true)
    private final boolean enforceViewabilitySDKs;
    private final Set<Long> targetingSegmentIds;
}
