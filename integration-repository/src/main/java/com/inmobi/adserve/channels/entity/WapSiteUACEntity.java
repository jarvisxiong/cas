package com.inmobi.adserve.channels.entity;

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
    private final String marketId;
    // Bundle Id of App
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
    // flag to see if transparency is enabled, this will be true if transparency is enabled at both publisher and site
    // level
    private final List<Integer> blindList;
    // list of DSP's, for which the site is blind. Picked from wap_site table if available, otherwise from wap_publisher
    // table
    private final String siteUrl;
    // Site page, App store URL
    private final String siteName;
    private final String appTitle;
    private final String bundleId;
    private final Timestamp modifiedOn;


    public WapSiteUACEntity(final Builder builder) {
        id = builder.id;
        marketId = builder.marketId;
        siteTypeId = builder.siteTypeId;
        contentRating = builder.contentRating;
        appType = builder.appType;
        categories = builder.categories;
        isCoppaEnabled = builder.isCoppaEnabled;
        isTransparencyEnabled = builder.isTransparencyEnabled;
        blindList = builder.blindList;
        siteUrl = builder.siteUrl;
        siteName = builder.siteName;
        appTitle = builder.appTitle;
        bundleId = builder.bundleId;
        modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String id;
        private String marketId;
        private long siteTypeId;
        private String contentRating;
        private String appType;
        private List<String> categories;
        private boolean isCoppaEnabled;
        private boolean isTransparencyEnabled;
        private List<Integer> blindList;
        private String siteUrl;
        private String siteName;
        private String appTitle;
        private String bundleId;
        private Timestamp modifiedOn;

        public WapSiteUACEntity build() {
            return new WapSiteUACEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return String
                .format("{\"siteId\":\"%s\",\"siteTypeId\":%s,\"contentRating\":\"%s\",\"isCoppaEnabled\":%s,\"appType\":\"%s\"}",
                        id, siteTypeId, contentRating, isCoppaEnabled, appType);
    }

    @Override
    public String getId() {
        return id;
    }

}
