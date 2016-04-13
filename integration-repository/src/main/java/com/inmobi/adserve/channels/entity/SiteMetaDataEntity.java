package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Getter
public class SiteMetaDataEntity implements IdentifiableEntity<String> {
    private final static long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final String siteId;
    private final String pubId;
    private final Boolean backFillEnabled;
    private final AccountType accountTypesAllowed;
    private final Timestamp modified_on;
    private final Set<String> advertisersIncludedBySite;
    private final Set<String> advertisersIncludedByPublisher;

    public SiteMetaDataEntity(final Builder builder) {
        siteId = builder.siteId;
        pubId = builder.pubId;
        backFillEnabled = builder.backFillEnabled;
        AccountType accountsAllowed = AccountType.MANAGED;
        if (builder.selfServeAllowed) {
            accountsAllowed = AccountType.SELF_SERVE;
        }
        accountTypesAllowed = accountsAllowed;
        modified_on = builder.modified_on;
        advertisersIncludedBySite = builder.advertisersIncludedBySite;
        advertisersIncludedByPublisher = builder.advertisersIncludedByPublisher;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String siteId;
        private String pubId;
        private Boolean backFillEnabled;
        private Boolean selfServeAllowed;
        private Timestamp modified_on;
        private Set<String> advertisersIncludedBySite;
        private Set<String> advertisersIncludedByPublisher;

        public SiteMetaDataEntity build() {
            return new SiteMetaDataEntity(this);
        }
    }

    @Override
    public String getId() {
        return siteId;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }
}
