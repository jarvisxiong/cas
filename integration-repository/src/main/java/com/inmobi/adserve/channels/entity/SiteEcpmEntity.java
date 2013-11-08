package com.inmobi.adserve.channels.entity;

import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;


@Getter
@ToString
public class SiteEcpmEntity implements IdentifiableEntity<SiteEcpmQuery>
{

    private static final long serialVersionUID = 1L;

    private final String      siteId;
    private final Integer     countryId;
    private final Integer     osId;
    private final double      ecpm;
    private final double      networkEcpm;
    private final Timestamp   modifiedOn;

    public SiteEcpmEntity(Builder builder)
    {
        this.siteId = builder.siteId;
        this.countryId = builder.countryId;
        this.osId = builder.osId;
        this.ecpm = builder.ecpm;
        this.networkEcpm = builder.networkEcpm;
        this.modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    @Setter
    public static class Builder
    {
        private String    siteId;
        private Integer   countryId;
        private Integer   osId;
        private double    ecpm;
        private double    networkEcpm;
        private Timestamp modifiedOn;

        public SiteEcpmEntity build()
        {
            return new SiteEcpmEntity(this);
        }
    }

    @Override
    public SiteEcpmQuery getId()
    {
        return new SiteEcpmQuery(this.siteId, this.countryId, this.osId);
    }

    @Override
    public String getJSON()
    {
        return null;
    }
}
