package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

/**
 * Created by anshul.soni on 10/11/14.
 */

@Getter
public class GeoZipEntity implements IdentifiableEntity<Integer> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final Integer zipId;
    private final String zipCode;
    private final Timestamp modifiedOn;

    public GeoZipEntity(final Builder builder) {
        zipId = builder.zipId;
        zipCode = builder.zipCode;
        modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }



    @Setter
    public static class Builder {
        private Integer zipId;
        private String zipCode;
        private Timestamp modifiedOn;

        public GeoZipEntity build() {
            return new GeoZipEntity(this);
        }

    }

    @Override
    public Integer getId() {
        return zipId;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

}
