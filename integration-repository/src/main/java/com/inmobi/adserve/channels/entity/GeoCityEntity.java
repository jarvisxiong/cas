package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class GeoCityEntity implements IdentifiableEntity<Integer> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();

    private final Integer id;
    private final String name;
    private final Timestamp modifiedOn;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }
}
