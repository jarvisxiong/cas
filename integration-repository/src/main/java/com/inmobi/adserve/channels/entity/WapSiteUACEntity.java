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
    private final String pubId;//todo for debugging purpose only, remove later

    private final String marketId;
    //Bundle Id of App
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
    private final boolean isTransparencyEnabled;
    //flag to see if transparency is enabled
    private final List<Integer> blockList;
    //list of DSP's, to be blocked for a site
    private final String siteUrl;
    //Site page, App store URL
    private final String siteName;
    private final String appTitle;
    private final Timestamp modifiedOn;


    public WapSiteUACEntity(final Builder builder) {
        id = builder.id;
        pubId = builder.pubId;
        marketId = builder.marketId;
        siteTypeId = builder.siteTypeId;
        contentRating = builder.contentRating;
        appType = builder.appType;
        categories = builder.categories;
        isCoppaEnabled = builder.isCoppaEnabled;
        isTransparencyEnabled = builder.isTransparencyEnabled;
        blockList = builder.blockList;
        siteUrl = builder.siteUrl;
        siteName = builder.siteName;
        appTitle = builder.appTitle;
        modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String id;
        private String pubId;
        private String marketId;
        private long siteTypeId;
        private String contentRating;
        private String appType;
        private List<String> categories;
        private boolean isCoppaEnabled;
        private boolean isTransparencyEnabled;
        private boolean isExchangeEnabled;
        private List<Integer> blockList;
        private String siteUrl;
        private String siteName;
        private String appTitle;
        private Timestamp modifiedOn;
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
