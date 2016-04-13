package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.List;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ishanbhatnagar on 26/2/15.
 */
@Getter
public class GeoRegionFenceMapEntity implements IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final String geoRegionName;
    private final Long countryId;
    private final List<Long> fenceIdsList;
    private final Timestamp modifiedOn;

    public GeoRegionFenceMapEntity(final Builder builder) {
        geoRegionName = builder.geoRegionName;
        countryId = builder.countryId;
        fenceIdsList = builder.fenceIdsList;
        modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String getId() {
        return geoRegionName + "_" + countryId;
    }

    @Setter
    public static class Builder {
        private String geoRegionName;
        private Long countryId;
        private List<Long> fenceIdsList;
        private Timestamp modifiedOn;

        public GeoRegionFenceMapEntity build() {
            return new GeoRegionFenceMapEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

}
