package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.List;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class GeoRegionFenceMapEntity implements IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final String geoRegionName;
    private final Long countryId;
    private final List<Long> fenceIdsList;
    private final Timestamp modifiedOn;

    @Override
    public String getId() {
        return geoRegionName + '_' + countryId;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

}
