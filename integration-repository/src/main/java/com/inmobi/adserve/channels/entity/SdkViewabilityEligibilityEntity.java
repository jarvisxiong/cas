package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.inmobi.adserve.channels.query.SdkViewabilityEligibilityQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created by ishan.bhatnagar on 04/11/15.
 */
@Data
@RequiredArgsConstructor
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class SdkViewabilityEligibilityEntity implements IdentifiableEntity<SdkViewabilityEligibilityQuery> {
    private static final long serialVersionUID = 1L;
    private final Integer countryId;
    private final String adType;
    private final Integer dst;
    private final Pair<Boolean, Set<Integer>> sdkViewabilityInclusionExclusion;
    private final Timestamp modifiedOn;

    @Override
    public SdkViewabilityEligibilityQuery getId() {
        return new SdkViewabilityEligibilityQuery(countryId, adType, dst);
    }

    @Override
    public String getJSON() {
        return String.format("{\"countryId\":\"%d\",\"adType\":\"%s\",\"dst\":%d,\"sdkViewabilityInclusionExclusion\":"
            + "\"%s\",\"modifiedOn\":\"%s\"}", countryId, adType, dst, sdkViewabilityInclusionExclusion.toString(),
            modifiedOn);
    }
}
