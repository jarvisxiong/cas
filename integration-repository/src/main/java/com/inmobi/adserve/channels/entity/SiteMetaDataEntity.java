package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;


@Getter
public class SiteMetaDataEntity implements IdentifiableEntity<String>
{

    private static final long serialVersionUID = 1L;

    private final String      siteId;
    private final String      pubId;
    private final Timestamp   modified_on;
    private final Set<String> advertisersIncludedBySite;
    private final Set<String> advertisersIncludedByPublisher;

    public SiteMetaDataEntity(Builder builder)
    {
        this.siteId = builder.siteId;
        this.pubId = builder.pubId;
        this.modified_on = builder.modified_on;
        this.advertisersIncludedBySite = builder.advertisersIncludedBySite;
        this.advertisersIncludedByPublisher = builder.advertisersIncludedByPublisher;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    @Setter
    public static class Builder
    {
        private String      siteId;
        private String      pubId;
        private Timestamp   modified_on;
        private Set<String> advertisersIncludedBySite;
        private Set<String> advertisersIncludedByPublisher;

        public SiteMetaDataEntity build()
        {
            return new SiteMetaDataEntity(this);
        }
    }

    @Override
    public String getId()
    {
        return this.siteId;
    }

    @Override
    public String getJSON()
    {
        return null;
    }
}
