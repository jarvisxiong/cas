package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Getter
@ToString
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class SiteEcpmEntity implements IdentifiableEntity<SiteEcpmQuery> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final String siteId;
    private final Integer countryId;
    private final Integer osId;
    private final double ecpm;
    private final double networkEcpm;
    private final Timestamp modifiedOn;

    @Override
    public SiteEcpmQuery getId() {
        return new SiteEcpmQuery(siteId, countryId, osId);
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }
}
