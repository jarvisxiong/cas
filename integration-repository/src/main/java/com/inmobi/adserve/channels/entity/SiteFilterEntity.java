package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import lombok.Data;
import lombok.ToString;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.query.SiteFilterQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Data
@ToString
public class SiteFilterEntity implements IdentifiableEntity<SiteFilterQuery> {
    private static final long serialVersionUID = -3778683319364509021L;
    private final static Gson GSON = new Gson();
    private String siteId;
    private String pubId;
    private String[] blockedIabCategories;
    private String[] blockedAdvertisers;
    private Integer ruleType;
    private boolean isExpired;
    private Timestamp modified_on;

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

    @Override
    public SiteFilterQuery getId() {
        return new SiteFilterQuery(getSiteId(), getRuleType());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SiteFilterEntity other = (SiteFilterEntity) obj;

        if (null == ruleType) {
            if (null != other.ruleType) {
                return false;
            }
        } else if (!ruleType.equals(other.ruleType)) {
            return false;
        }
        if (null == siteId) {
            if (null != other.siteId) {
                return false;
            }
        } else if (!siteId.equals(other.siteId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (ruleType == null ? 0 : ruleType.hashCode());
        result = prime * result + (siteId == null ? 0 : siteId.hashCode());
        return result;
    }

}
