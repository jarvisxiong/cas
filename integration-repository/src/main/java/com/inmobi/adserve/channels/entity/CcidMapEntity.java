package com.inmobi.adserve.channels.entity;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Data;


@Data
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class CcidMapEntity implements IdentifiableEntity<Integer> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final Integer countryCarrierId;
    private final String country;
    private final String carrier;

    @Override
    public Integer getId() {
        return countryCarrierId;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }
}
