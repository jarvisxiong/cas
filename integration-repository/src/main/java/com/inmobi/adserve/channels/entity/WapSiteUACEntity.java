package com.inmobi.adserve.channels.entity;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import lombok.Data;
import lombok.Setter;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

/**
 * @author ritwik.kumar
 */
@Data
public class WapSiteUACEntity implements IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;

    private final String id;
    // Type of site (Android, IOS etc.)
    private final long siteTypeId;
    // Content Rating of the App
    private final String contentRating;
    // Primary category of the App
    private final String appType;
    // List of secondary categories
    private final List<String> categories;
    // Coppa enabled flag
    private final boolean isCoppaEnabled;
    // last modified time of this table
    private final Timestamp modifiedOn;
    //boolean
    private final List<Integer> blockList;
    //list of DSP's, to be blocked for a site
    private final boolean isTransparencyEnabled;
    //flag to see if transparency is enabled
    private final boolean isExchangeEnabled;
    //flag to see if exchange is enabled
    private final String pubId;//todo for debugging purpose only, remove later
    private final String marketId;
    //Bundle Id of App
    private final String siteUrl;
    //Site page, App store URL

    public WapSiteUACEntity(final Builder builder) {
        id = builder.id;
        siteTypeId = builder.siteTypeId;
        contentRating = builder.contentRating;
        appType = builder.appType;
        categories = builder.categories;
        isCoppaEnabled = builder.isCoppaEnabled;
        modifiedOn = builder.modifiedOn;
        blockList = builder.blockList;
        isTransparencyEnabled = builder.isTransparencyEnabled;
        isExchangeEnabled = builder.isExchangeEnabled;
        pubId = builder.pubId;
        marketId = builder.marketId;
        siteUrl = builder.siteUrl;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String id;
        private long siteTypeId;
        private String contentRating;
        private String appType;
        private List<String> categories;
        private Timestamp modifiedOn;
        private boolean isCoppaEnabled;
        private List<Integer> blockList;
        private boolean isTransparencyEnabled;
        private boolean isExchangeEnabled;
        private String pubId;
        private String marketId;
        private String siteUrl;

        public WapSiteUACEntity build() {
            return new WapSiteUACEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return String.format(
                "{\"siteId\":\"%s\",\"siteTypeId\":%s,\"contentRating\":\"%s\",\"isCoppaEnabled\":%s,\"appType\":\"%s\"}", id,
                siteTypeId, contentRating, isCoppaEnabled, appType);
    }

    @Override
    public String getId() {
        return id;
    }

}
