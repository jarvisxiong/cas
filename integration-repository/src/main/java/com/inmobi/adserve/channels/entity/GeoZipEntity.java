package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Created by anshul.soni on 10/11/14.
 */

@Getter
public class GeoZipEntity implements IdentifiableEntity<Integer> {
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
        return null;
    }

}
