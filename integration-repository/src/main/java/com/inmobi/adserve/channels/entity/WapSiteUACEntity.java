package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.List;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author ritwik.kumar
 */
@Data
@RequiredArgsConstructor
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class WapSiteUACEntity implements IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();
    private static final long IOS_SITE_TYPE = 21;
    public static final long ANDROID_SITE_TYPE = 22;

    private final String id;
    // Type of site (Android, IOS etc.)
    private final String marketId;
    private final boolean overrideMarketId;
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
    // Bundle Id of App - e.g. com.rovio.angrybirds for both IOS and Android source is wap_site_uac
    private final String bundleId;
    // last modified time of this table
    private final Timestamp modifiedOn;

    public boolean isAndroid() {
        return ANDROID_SITE_TYPE == siteTypeId;
    }

    public boolean isIOS() {
        return IOS_SITE_TYPE == siteTypeId;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

    @Override
    public String getId() {
        return id;
    }

}
