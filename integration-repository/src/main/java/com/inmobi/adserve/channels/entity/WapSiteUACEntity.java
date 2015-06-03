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
    private static final long IOS_SITE_TYPE = 21;
    public static final long ANDROID_SITE_TYPE = 22;

    private final String id;
    // Type of site (Android, IOS etc.)
    private final String marketId;
    // long id ios=21, android=22 etc
    private final long siteTypeId;
    // Content Rating of the App
    private final String contentRating;
    // Primary category of the App
    private final String appType;
    // List of secondary categories
    private final List<String> categories;
    // Coppa enabled flag
    private final boolean isCoppaEnabled;
    // flag for transparency, this will be true if transparency is enabled at both publisher and site level
    private final boolean isTransparencyEnabled;
    // list of DSP's, for which the site is blind. Picked from wap_site table if available, otherwise from wap_publisher
    private final List<Integer> blindList;
    // Site page, App store URL
    private final String siteUrl;
    // site nape as in wap_site
    private final String siteName;
    // App title as fetched from UAC
    private final String appTitle;
    // Bundle Id of App - e.g. com.rovio.angrybirds for both IOS and Android
    private final String bundleId;
    // last modified time of this table
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

    public boolean isAndroid() {
        return ANDROID_SITE_TYPE == siteTypeId;
    }

    public boolean isIOS() {
        return IOS_SITE_TYPE == siteTypeId;
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
