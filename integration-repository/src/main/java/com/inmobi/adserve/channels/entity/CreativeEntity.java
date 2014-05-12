package com.inmobi.adserve.channels.entity;

import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.types.CreativeExposure;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Data;
import lombok.Setter;


@Data
public class CreativeEntity implements IdentifiableEntity<CreativeQuery> {

    private static final long              serialVersionUID              = 1L;
    private final String                    advertiserId;
    private final String                    creativeId;
    private final CreativeExposure          exposureLevel;
    private final String                    imageUrl;

    public CreativeEntity(Builder builder) {
        this.advertiserId = builder.advertiserId;
        this.creativeId = builder.creativeId;
        this.exposureLevel = builder.exposureLevel;
        this.imageUrl = builder.imageUrl;
    }

    public static CreativeEntity.Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String                  advertiserId;
        private String                  creativeId;
        private CreativeExposure        exposureLevel;
        private String                  imageUrl;

        public CreativeEntity build() {
            return new CreativeEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return null;
    }

    @Override
    public CreativeQuery getId() {
        return new CreativeQuery(this.advertiserId, this.creativeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CreativeEntity other = (CreativeEntity) obj;
        if (advertiserId == null) {
            if (other.advertiserId != null)
                return false;
        } else if (!advertiserId.equals(other.advertiserId))
            return false;
        if (creativeId == null) {
            if (other.creativeId != null)
                return false;
        }
        else if (!creativeId.equals(other.creativeId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((advertiserId == null) ? 0 : advertiserId.hashCode());
        result = prime * result + ((creativeId == null) ? 0 : creativeId.hashCode());
        return result;
    }
}
