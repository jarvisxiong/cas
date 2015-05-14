package com.inmobi.adserve.channels.entity;

import com.inmobi.adserve.channels.query.IXVideoTrafficQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@ToString
public class IXVideoTrafficEntity implements IdentifiableEntity<IXVideoTrafficQuery> {
    private static final long serialVersionUID = 1L;
    private String siteId;
    private Integer countryId;
    private Short trafficPercentage;
    private Boolean isActive;
    private Timestamp modifiedOn;

    public IXVideoTrafficEntity(final Builder builder) {
        siteId = builder.siteId;
        countryId = builder.countryId;
        trafficPercentage = builder.trafficPercentage;
        isActive = builder.isActive;
        modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String siteId;
        private Integer countryId;
        private Short trafficPercentage;
        private Boolean isActive;
        private Timestamp modifiedOn;

        public IXVideoTrafficEntity build() {
            return new IXVideoTrafficEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return null;
    }

    @Override
    public IXVideoTrafficQuery getId() {
        return new IXVideoTrafficQuery(getSiteId(), getCountryId());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IXVideoTrafficEntity that = (IXVideoTrafficEntity) o;

        if (countryId != null ? !countryId.equals(that.countryId) : that.countryId != null) {
            return false;
        }
        if (siteId != null ? !siteId.equals(that.siteId) : that.siteId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = siteId != null ? siteId.hashCode() : 0;
        result = 31 * result + (countryId != null ? countryId.hashCode() : 0);
        return result;
    }
}
