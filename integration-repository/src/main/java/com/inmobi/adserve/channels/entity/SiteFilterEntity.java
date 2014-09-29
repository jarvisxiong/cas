package com.inmobi.adserve.channels.entity;

import com.inmobi.adserve.channels.query.SiteFilterQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;


@Data
@ToString
public class SiteFilterEntity implements IdentifiableEntity<SiteFilterQuery> {

    private static final long serialVersionUID = -3778683319364509021L;

    private String            siteId;
    private String            pubId;
    private String[]          blockedCategories;
    private String[]          blockedAdvertisers;
    private Integer           ruleType;
    private boolean           isExpired;
    private Timestamp         modified_on;

    @Override
    public String getJSON() {
        return null;
    }

    @Override
    public SiteFilterQuery getId() {
        return new SiteFilterQuery(this.getSiteId(), this.getRuleType());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SiteFilterEntity other = (SiteFilterEntity) obj;
        if (ruleType == null) {
            if (other.ruleType != null)
                return false;
        }
        else if (!ruleType.equals(other.ruleType))
            return false;
        if (siteId == null) {
            if (other.siteId != null)
                return false;
        }
        else if (!siteId.equals(other.siteId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ruleType == null) ? 0 : ruleType.hashCode());
        result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
        return result;
    }

}
