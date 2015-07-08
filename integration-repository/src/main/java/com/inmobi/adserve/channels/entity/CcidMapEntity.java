package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Data;

/**
 * Created by ishanbhatnagar on 30/4/15.
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class CcidMapEntity implements IdentifiableEntity<Integer> {
    private static final long serialVersionUID = 1L;
    private final Integer countryCarrierId;
    private final String country;
    private final String carrier;

    @Override
    public Integer getId() {
        return countryCarrierId;
    }

    @Override
    public String getJSON() {
        return String.format("{\"countryCarrierId\":\"%d\",\"country\":%s,\"carrier\":\"%s\"}",
                countryCarrierId, country, carrier);
    }
}
