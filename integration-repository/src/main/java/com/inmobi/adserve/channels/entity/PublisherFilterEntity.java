package com.inmobi.adserve.channels.entity;

import com.inmobi.adserve.channels.query.PublisherFilterQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;


@Data
@ToString
public class PublisherFilterEntity implements IdentifiableEntity<PublisherFilterQuery>
{

    private static final long serialVersionUID = 6433325928036900792L;

    private String            siteId;
    private String            pubId;
    private Long[]            blockedCategories;
    private String[]          blockedAdvertisers;
    private Integer           ruleType;
    private boolean           isExpired;
    private Timestamp         modified_on;

    @Override
    public String getJSON()
    {
        return null;
    }

    @Override
    public PublisherFilterQuery getId()
    {
        return new PublisherFilterQuery(this.getSiteId(), this.getRuleType());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PublisherFilterEntity other = (PublisherFilterEntity) obj;
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
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ruleType == null) ? 0 : ruleType.hashCode());
        result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
        return result;
    }

}
