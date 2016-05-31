package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.COMPLETE;
import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.CREATIVE_VIEW;
import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.FIRST_QUARTILE;
import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.MEDIA_PAUSE;
import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.MEDIA_UNPAUSE;
import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.MIDPOINT;
import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.SKIP;
import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.START;
import static com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper.VastTrackingEventsTypeEnum.THIRD_QUARTILE;
import static com.inmobi.adserve.channels.api.BaseAdNetworkHelper.getHashedValue;
import static com.inmobi.adserve.channels.api.Formatter.TemplateType.CAU;
import static com.inmobi.adserve.channels.api.Formatter.TemplateType.INTERSTITIAL_REWARDED_VAST_VIDEO;
import static com.inmobi.adserve.channels.api.Formatter.TemplateType.INTERSTITIAL_VAST_VIDEO;
import static com.inmobi.adserve.channels.types.IXBlocklistKeyType.COUNTRY;
import static com.inmobi.adserve.channels.types.IXBlocklistKeyType.SITE;
import static com.inmobi.adserve.channels.types.IXBlocklistType.ADVERTISERS;
import static com.inmobi.adserve.channels.types.IXBlocklistType.CREATIVE_ATTRIBUTE_IDS;
import static com.inmobi.adserve.channels.types.IXBlocklistType.INDUSTRY_IDS;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.AD_OBJECT_PREFIX;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.CAU_CONTENT_JS_ESC;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.FIRST_OBJECT_PREFIX;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.PARTNER_BEACON_URL;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.TOOL_OBJECT;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.VAST_CONTENT_JS_ESC;
import static com.inmobi.adserve.channels.util.InspectorStrings.ALL_NATIVE_ASSETS_DEFAULT;
import static com.inmobi.adserve.channels.util.InspectorStrings.NATIVE_IMAGE_AR_DIFF_MORE_10_PERCENT;
import static com.inmobi.adserve.channels.util.InspectorStrings.NATIVE_IMAGE_NOT_PROPER;
import static com.inmobi.adserve.channels.util.InspectorStrings.NATIVE_IMAGE_WIDTH_MODIFIED;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PURE_VAST_RESPONSE_INLINE_OR_WRAPPER_MISSING;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PURE_VAST_RESPONSE_TRACKING_EVENTS_MISSING;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.GEO_CC;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.GEO_LAT;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.GEO_LNG;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.GEO_ZIP;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.HANDSET_NAME;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.HANDSET_TYPE;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.IMP_CB;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_BEACON_URL;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_CLICK_URL;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_GEO_CITY;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_SITE_PREFERENCES_JSON;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.OPEN_LP_FUN;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.RECORD_EVENT_FUN;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SDK_VERSION_ID;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SECURE;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SITE_PREFERENCES_JSON;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SI_BLIND;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.USER_ID;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.USER_ID_MD5_HASHED;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.USER_ID_SHA1_HASHED;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.AUDIENCE_VERIFICATION_TRACKER;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.IM_BEACON_URL;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.IM_CLICK_URL;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.THIRD_PARTY_CLICK_TRACKER;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.THIRD_PARTY_IMPRESSION_TRACKER;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.MD5;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.NON_WIFI;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.SDK_TIMESTAMP_SUFFIX;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.SHA1;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.UTF_8;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.WIFI;
import static com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType.DATA;
import static com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType.IMAGE;
import static com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType.TITLE;
import static com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType.VIDEO;
import static com.inmobi.adserve.contracts.common.request.nativead.Data.DataAssetType.CTA_TEXT;
import static com.inmobi.adserve.contracts.common.request.nativead.Data.DataAssetType.DESC;
import static com.inmobi.adserve.contracts.common.request.nativead.Data.DataAssetType.DOWNLOADS;
import static com.inmobi.adserve.contracts.common.request.nativead.Data.DataAssetType.RATING;
import static com.inmobi.adserve.contracts.common.response.nativead.DefaultResponses.DEFAULT_CTA;
import static com.inmobi.adserve.contracts.common.response.nativead.DefaultResponses.DEFAULT_DESC;
import static com.inmobi.adserve.contracts.common.response.nativead.DefaultResponses.DEFAULT_DOWNLOAD;
import static com.inmobi.adserve.contracts.common.response.nativead.DefaultResponses.DEFAULT_ICON;
import static com.inmobi.adserve.contracts.common.response.nativead.DefaultResponses.DEFAULT_RATING;
import static com.inmobi.adserve.contracts.common.response.nativead.DefaultResponses.DEFAULT_TITLE;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.IXBlocklistEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.adserve.channels.util.GenericTemplateObject;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.SproutTemplateConstants;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.contracts.common.request.nativead.Asset;
import com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType;
import com.inmobi.adserve.contracts.common.request.nativead.Image;
import com.inmobi.adserve.contracts.common.response.nativead.Native;
import com.inmobi.adserve.contracts.ix.request.Geo;
import com.inmobi.template.context.App;
import com.inmobi.template.context.Icon;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.tool.TemplateTool;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author ritwik.kumar
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IXAdNetworkHelper {
    private static final Logger LOG = LoggerFactory.getLogger(IXAdNetworkHelper.class);
    private static final String PUBLISHER_BLOCKLIST_FORMAT = "blk%s";
    private static final String IX_PERF_ADVERTISER_BLOCKLIST_ID = "InMobiPERFAdv";
    private static final String IX_PERF_INDUSTRY_BLOCKLIST_ID = "InMobiPERFInd";
    private static final String IX_PERF_CREATIVE_ATTRIBUTE_BLOCKLIST_ID = "InMobiPERFCre";
    private static final String IX_FS_ADVERTISER_BLOCKLIST_ID = "InMobiFSAdv";
    private static final String IX_FS_INDUSTRY_BLOCKLIST_ID = "InMobiFSInd";
    private static final String IX_FS_CREATIVE_ATTRIBUTE_BLOCKLIST_ID = "InMobiFSCre";

    private static final long RANDOM_SALT = 41768l;

    private static final ImmutableMap<IXBlocklistType, String> inmobiPerfBlocklistMap =
            ImmutableMap.of(ADVERTISERS, IX_PERF_ADVERTISER_BLOCKLIST_ID, INDUSTRY_IDS, IX_PERF_INDUSTRY_BLOCKLIST_ID,
                    CREATIVE_ATTRIBUTE_IDS, IX_PERF_CREATIVE_ATTRIBUTE_BLOCKLIST_ID);

    private static final ImmutableMap<IXBlocklistType, String> inmobiFsBlocklistMap =
            ImmutableMap.of(ADVERTISERS, IX_FS_ADVERTISER_BLOCKLIST_ID, INDUSTRY_IDS, IX_FS_INDUSTRY_BLOCKLIST_ID,
                    CREATIVE_ATTRIBUTE_IDS, IX_FS_CREATIVE_ATTRIBUTE_BLOCKLIST_ID);

    private static final ImmutableList<IXBlocklistType> supportedBlocklistTypes =
            ImmutableList.of(ADVERTISERS, INDUSTRY_IDS, CREATIVE_ATTRIBUTE_IDS);

    // below are the suffix to base beacon URL for generating various beacon events.
    private static final String RENDER_BEACON_SUFFIX = "?m=18";
    private static final String CLICK_BEACON_SUFFIX = "?m=8";
    private static final String ERROR_BEACON_SUFFIX = "?m=99&action=vast-error&label=[ERRORCODE]";
    private static final String BILLING_BEACON_SUFFIX = "?b=${WIN_BID}${DEAL_GET_PARAM}";
    private static final String MEDIA_START_BEACON_SUFFIX = "?m=10";
    private static final String CREATIVE_VIEW_BEACON_SUFFIX = "?m=1";
    private static final String MEDIA_PAUSE_BEACON_SUFFIX = "?m=14";
    private static final String MEDIA_UNPAUSE_BEACON_SUFFIX = "?m=17";
    private static final String MEDIA_SKIP_BEACON_SUFFIX = "?m=99&action=skip";
    private static final String Q1_BEACON_SUFFIX = "?m=12&q=1&mid=video";
    private static final String Q2_BEACON_SUFFIX = "?m=12&q=2&mid=video";
    private static final String Q3_BEACON_SUFFIX = "?m=12&q=3&mid=video";
    private static final String Q4_BEACON_SUFFIX = "?m=13&mid=video";

    private static Transformer transformer;

    static {
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (final Exception e) {
            LOG.error("VAST PARSING EXCEPTION WHILE WRITING TO STRING BACK");
        }
    }


    /**
     *
     * @param adm
     * @param casInternal
     * @param sasParams
     * @param isCoppaSet
     * @param clickUrl
     * @param beaconUrl
     * @return
     */
    public static String replaceSproutMacros(final String adm, final CasInternalRequestParameters casInternal,
            final SASRequestParameters sasParams, final boolean isCoppaSet, final String clickUrl,
            final String beaconUrl) {
        final List<String> macros = new ArrayList<>();
        final List<String> substitutions = new ArrayList<>();

        addSproutMacroToList(macros, substitutions, JS_ESC_BEACON_URL, StringEscapeUtils.escapeJavaScript(beaconUrl));
        addSproutMacroToList(macros, substitutions, JS_ESC_CLICK_URL, StringEscapeUtils.escapeJavaScript(clickUrl));

        final String sdkVersion = sasParams.getSdkVersion();
        addSproutMacroToList(macros, substitutions, SDK_VERSION_ID,
                null != sdkVersion ? sdkVersion : StringUtils.EMPTY);

        // default value for replacement of macros is an empty string
        final Geo geo = createSproutGeoObject(casInternal, sasParams, isCoppaSet);
        final String lat = null != geo.getLat() ? String.valueOf(geo.getLat()) : StringUtils.EMPTY;
        final String lng = null != geo.getLon() ? String.valueOf(geo.getLon()) : StringUtils.EMPTY;
        final String zip = null != geo.getZip() ? geo.getZip() : StringUtils.EMPTY;
        final String cc = null != geo.getCountry() ? geo.getCountry() : StringUtils.EMPTY;

        addSproutMacroToList(macros, substitutions, GEO_LAT, lat);
        addSproutMacroToList(macros, substitutions, GEO_LNG, lng);
        addSproutMacroToList(macros, substitutions, GEO_ZIP, zip);
        addSproutMacroToList(macros, substitutions, GEO_CC, cc);
        addSproutMacroToList(macros, substitutions, SECURE, String.valueOf(sasParams.isSecureRequest()));

        // JS_ESC_GEO_CITY is not currently being set
        addSproutMacroToList(macros, substitutions, JS_ESC_GEO_CITY, StringUtils.EMPTY);
        // No function is being provided
        addSproutMacroToList(macros, substitutions, RECORD_EVENT_FUN, StringUtils.EMPTY);
        // No function is being provided
        addSproutMacroToList(macros, substitutions, OPEN_LP_FUN, StringUtils.EMPTY);

        final String sitePreferences = null != sasParams.getPubControlPreferencesJson()
                ? sasParams.getPubControlPreferencesJson()
                : StringUtils.EMPTY;
        addSproutMacroToList(macros, substitutions, SITE_PREFERENCES_JSON, sitePreferences);
        addSproutMacroToList(macros, substitutions, JS_ESC_SITE_PREFERENCES_JSON, javascriptEscape(sitePreferences));

        final String userId = StringUtils.isNotEmpty(casInternal.getUidIFA())
                ? casInternal.getUidIFA()
                : StringUtils.isNotEmpty(casInternal.getGpid()) ? casInternal.getGpid() : null;

        // Non Sprout Macros
        addSproutMacroToList(macros, substitutions, IMP_CB, casInternal.getAuctionId());

        if (null != userId) {
            final String userIdMD5 = getHashedValue(userId, MD5);
            final String userIdSHA1 = getHashedValue(userId, SHA1);

            addSproutMacroToList(macros, substitutions, USER_ID, userIdMD5);
            addSproutMacroToList(macros, substitutions, USER_ID_MD5_HASHED, userIdMD5);
            addSproutMacroToList(macros, substitutions, USER_ID_SHA1_HASHED, userIdSHA1);
        }

        final String[] macroArray = macros.toArray(new String[macros.size()]);
        final String[] substitutionsArray = substitutions.toArray(new String[substitutions.size()]);
        return StringUtils.replaceEach(adm, macroArray, substitutionsArray);
    }

    public static String replaceViewabilityTrackerMacros(final String viewabilityTracker,
            final CasInternalRequestParameters casInternal, final SASRequestParameters sasParams) {
        final List<String> macros = new ArrayList<>();
        final List<String> substitutions = new ArrayList<>();

        addSproutMacroToList(macros, substitutions, SDK_VERSION_ID,
                ObjectUtils.defaultIfNull(sasParams.getSdkVersion(), StringUtils.EMPTY));
        addSproutMacroToList(macros, substitutions, IMP_CB, casInternal.getAuctionId());
        addSproutMacroToList(macros, substitutions, HANDSET_NAME,
                ObjectUtils.defaultIfNull(sasParams.getHandsetName(), StringUtils.EMPTY));
        addSproutMacroToList(macros, substitutions, HANDSET_TYPE, ObjectUtils
                .defaultIfNull(getHandSetTypeNameFromId(sasParams.getDeviceType().getValue()), StringUtils.EMPTY));
        addSproutMacroToList(macros, substitutions, SI_BLIND, String.valueOf(sasParams.getSiteIncId() + RANDOM_SALT));

        final String[] macroArray = macros.toArray(new String[macros.size()]);
        final String[] substitutionsArray = substitutions.toArray(new String[substitutions.size()]);
        return StringUtils.replaceEach(viewabilityTracker, macroArray, substitutionsArray);
    }

    private static String getHandSetTypeNameFromId(final int deviceTypeId) {
        switch (deviceTypeId) {
            case 1:
                return "smart_phone";
            case 2:
                return "feature_phone";
            case 3:
                return "tablet";
            default:
                return "connected_device";
        }
    }

    private static String javascriptEscape(final Object string) {
        return string == null ? null : StringEscapeUtils.escapeJavaScript(String.valueOf(string));
    }

    /**
     * Helper function to populate macro and substitution lists
     *
     * @param macros
     * @param substitutions
     * @param macro
     * @param substitution
     */
    private static void addSproutMacroToList(final List<String> macros, final List<String> substitutions,
            final String macro, final String substitution) {
        for (final Character character : SproutTemplateConstants.escapeCharacterList) {
            macros.add(character + macro);
            substitutions.add(substitution);
        }
    }

    /**
     * Function used to populate the Geo object for Sprout Macro Replacement
     *
     * @param casIntenal
     * @param sasParams
     * @param isCoppaSet
     *
     * @return
     */
    private static Geo createSproutGeoObject(final CasInternalRequestParameters casIntenal,
            final SASRequestParameters sasParams, final boolean isCoppaSet) {
        final Geo geo = new Geo();
        if (!isCoppaSet) {
            try {
                if (StringUtils.isNotBlank(casIntenal.getLatLong())
                        && 1 == StringUtils.countMatches(casIntenal.getLatLong(), ",")) {
                    final String[] latlong = casIntenal.getLatLong().split(",");
                    geo.setLat(Double.parseDouble(latlong[0]));
                    geo.setLon(Double.parseDouble(latlong[1]));
                }
            } catch (final NumberFormatException nfe) {
                // Not possible as type is already checked during deserialisation of AdPoolRequest
            }
            final String countryCode = sasParams.getCountryCode();
            if (null != countryCode) {
                geo.setCountry(countryCode);
            }
            final String zipCode = casIntenal.getZipCode();
            if (null != zipCode) {
                geo.setZip(zipCode);
            }
        }

        return geo;
    }

    /**
     * Verifies that <br>
     * 1) we receive a corresponding Asset object for every requested required Asset object with the same id and type
     * <br>
     * 2) Response Asset object doesn't contain more than one of title, img, video or data objects <br>
     * 3) Height and width are present for IMAGE MAIN objects <br>
     * 4) Minimum width and height image constraints are met for IMAGE MAIN objects<br>
     *
     * @param requestAsset
     * @param responseAsset
     * @param contextBuilder
     * @param defaultRequired
     * @param isAnyAssetNonDefault
     * @return
     */
    public static boolean areRequestResponseAssetsValid(final Asset requestAsset,
            final com.inmobi.adserve.contracts.common.response.nativead.Asset responseAsset,
            final com.inmobi.template.context.App.Builder contextBuilder, final boolean defaultRequired,
            Boolean isAnyAssetNonDefault) {
        final AssetType requestObject = verifyAsset(requestAsset, responseAsset);
        if (requestObject == null) {
            return false;
        }
        // note: Current implementation doesn't support multiple assets of the same type
        switch (requestObject) {
            case IMAGE:
                final Image reqImg = requestAsset.getImg();
                com.inmobi.adserve.contracts.common.response.nativead.Image respImg = responseAsset.getImg();

                // Set default Icon if not present
                final boolean isIcon = Image.ImageAssetType.ICON.getId() == reqImg.getType();
                final boolean imageUrlBlank = StringUtils.isBlank(respImg.getUrl());
                respImg = isIcon && imageUrlBlank ? DEFAULT_ICON : respImg;

                Integer respWidth = respImg.getW() != null ? respImg.getW() : reqImg.getWmin();
                final Integer respHeight = respImg.getH() != null ? respImg.getH() : reqImg.getHmin();
                if (imageUrlBlank || respWidth < reqImg.getWmin() || respHeight < reqImg.getHmin()) {
                    LOG.error("Image Width/Height/URL not proper {}", reqImg.getType());
                    InspectorStats.incrementStatCount(NATIVE_IMAGE_NOT_PROPER + reqImg.getType());
                    return false;
                }

                final double requestedAr = (double) reqImg.getWmin() / (double) reqImg.getHmin();
                final double responseAr = (double) respWidth / (double) respHeight;
                final double deltaInArPercentage = Math.abs(requestedAr - responseAr) * 100 / requestedAr;
                if (deltaInArPercentage > 0 && deltaInArPercentage <= 10) {
                    InspectorStats.incrementStatCount(NATIVE_IMAGE_WIDTH_MODIFIED + reqImg.getType());
                    respWidth = (int) (requestedAr * respHeight);
                } else {
                    InspectorStats.incrementStatCount(NATIVE_IMAGE_AR_DIFF_MORE_10_PERCENT + reqImg.getType());
                }

                if (!imageUrlBlank) {
                    LOG.debug("Found Non Default IMAGE of type {}", reqImg.getType());
                }
                if (isIcon) {
                    final Icon.Builder iconbuilder = Icon.newBuilder();
                    iconbuilder.setUrl(respImg.getUrl());
                    iconbuilder.setW(respWidth);
                    iconbuilder.setH(respHeight);
                    contextBuilder.setIcons(Collections.singletonList((Icon) iconbuilder.build()));
                } else if (Image.ImageAssetType.MAIN.getId() == reqImg.getType()) {
                    final Screenshot.Builder screenshotBuilder = Screenshot.newBuilder();
                    screenshotBuilder.setUrl(respImg.getUrl());
                    screenshotBuilder.setW(respWidth);
                    screenshotBuilder.setH(respHeight);
                    contextBuilder.setScreenshots(Collections.singletonList((Screenshot) screenshotBuilder.build()));
                }
                break;
            case VIDEO:
                LOG.debug("Video objects are currently not supported for native");
                break;
            case TITLE:
                final String title = responseAsset.getTitle().getText();
                final boolean isTitleBlank = StringUtils.isBlank(title);
                if (!isTitleBlank) {
                    isAnyAssetNonDefault = true;
                    LOG.debug("Found Non Default Title -> {}", title);
                }
                contextBuilder.setTitle(isTitleBlank ? DEFAULT_TITLE : title);
                break;
            case DATA:
                final Integer type = requestAsset.getData().getType();
                final String value = responseAsset.getData().getValue();
                final boolean isValBlank = StringUtils.isBlank(value);
                if (!isValBlank) {
                    isAnyAssetNonDefault = true;
                    LOG.debug("Found Non Default DATA of type {}", type);
                }
                if (defaultRequired || !isValBlank) {
                    if (DESC.getId() == type) {
                        contextBuilder.setDesc(isValBlank ? DEFAULT_DESC : value);
                    } else if (CTA_TEXT.getId() == type) {
                        contextBuilder.setActionText(isValBlank ? DEFAULT_CTA : value);
                    } else if (DOWNLOADS.getId() == type) {
                        contextBuilder.setDownloads(isValBlank ? DEFAULT_DOWNLOAD : Integer.valueOf(value));
                    } else if (RATING.getId() == type) {
                        contextBuilder.setRating(isValBlank ? DEFAULT_RATING : value);
                    }
                }
        }
        return true;
    }

    /**
     * Verifies that <br>
     * 1) we receive a corresponding Asset object for every requested required Asset object with the same id and type
     * <br>
     * 2) Response Asset object doesn't contain more than one of title, img, video or data objects <br>
     *
     * @param requestAsset
     * @param responseAsset
     * @return
     */
    private static AssetType verifyAsset(final Asset requestAsset,
            final com.inmobi.adserve.contracts.common.response.nativead.Asset responseAsset) {
        int requestAssetCount = 0;
        int responseAssetCount = 0;
        Asset.AssetType requestObject = null;
        Asset.AssetType responseObject = null;

        if (null != requestAsset.getTitle()) {
            ++requestAssetCount;
            requestObject = TITLE;
        }
        if (null != requestAsset.getImg()) {
            ++requestAssetCount;
            requestObject = IMAGE;
        }
        if (null != requestAsset.getVideo()) {
            ++requestAssetCount;
            requestObject = VIDEO;
        }
        if (null != requestAsset.getData()) {
            ++requestAssetCount;
            requestObject = DATA;
        }

        if (null != responseAsset.getTitle()) {
            ++responseAssetCount;
            responseObject = TITLE;
        }
        if (null != responseAsset.getImg()) {
            ++responseAssetCount;
            responseObject = IMAGE;
        }
        if (null != responseAsset.getVideo()) {
            ++responseAssetCount;
            responseObject = VIDEO;
        }
        if (null != responseAsset.getData()) {
            ++responseAssetCount;
            responseObject = DATA;
        }

        if (1 != requestAssetCount || 1 != responseAssetCount) {
            LOG.error("Aborting as more than one or none of title, img, video or data were present in the "
                    + "same Asset object");
            return null;
        }
        if (requestObject != responseObject) {
            LOG.error("Aborting as type mismatch between Asset objects with id: {}. Received {} instead " + "of {}",
                    responseAsset.getId(), responseObject.name(), requestObject.name());
            return null;
        }
        return requestObject;
    }

    /**
     * Builds the interfacing object between the adapter and the native response maker.
     *
     * @param nativeObj
     * @param mandatoryAssetMap
     * @param nonMandatoryAssetMap
     * @param impressionId
     */
    public static App validateAndBuildTemplateContext(final Native nativeObj,
            final Map<Integer, Asset> mandatoryAssetMap, final Map<Integer, Asset> nonMandatoryAssetMap,
            final String impressionId) {
        if (null == nativeObj) {
            LOG.debug("Dropping native request as native object was null");
            return null;
        }

        LOG.debug("Starting building NativeResponseMaker interfacing object from response native object");
        final com.inmobi.template.context.App.Builder contextBuilder = com.inmobi.template.context.App.newBuilder();
        try {
            contextBuilder.setOpeningLandingUrl(nativeObj.getLink().getUrl());
            contextBuilder.setClickUrls(nativeObj.getLink().getClicktrackers());
            contextBuilder.setPixelUrls(nativeObj.getImptrackers());
            contextBuilder.setAdImpressionId(impressionId);

            final Boolean isAnyAssetNonDefault = false;
            for (final com.inmobi.adserve.contracts.common.response.nativead.Asset asset : nativeObj.getAssets()) {
                final int assetId = asset.getId();
                // We need all of mandatory fields. And Requested type should be same as response
                if (mandatoryAssetMap.containsKey(assetId)) {
                    final boolean isReqValid = areRequestResponseAssetsValid(mandatoryAssetMap.get(assetId), asset,
                            contextBuilder, true, isAnyAssetNonDefault);
                    if (isReqValid) {
                        mandatoryAssetMap.remove(assetId);
                        continue;
                    } else {
                        return null;
                    }
                }
                // We do not need all of mandatory fields. But Requested type should be same as response
                if (nonMandatoryAssetMap.containsKey(assetId)) {
                    areRequestResponseAssetsValid(nonMandatoryAssetMap.get(assetId), asset, contextBuilder, false,
                            isAnyAssetNonDefault);
                }
            }
            if (!isAnyAssetNonDefault) {
                InspectorStats.incrementStatCount(ALL_NATIVE_ASSETS_DEFAULT);
            }
            if (mandatoryAssetMap.isEmpty()) {
                LOG.debug("NativeResponseMaker interfacing object built successfully");
                return (App) contextBuilder.build();
            } else {
                LOG.error("Native Ad Building failed as all required assets were not present.");
                return null;
            }
        } catch (final Exception e) {
            LOG.error("Exception encountered while building IX native template context. Exception: {}", e);
            return null;
        }
    }

    /**
     * Validates whether the ADM content is a VALID XML.
     *
     * @param adm
     * @return
     */
    public static boolean isAdmValidXML(final String adm) {
        if (StringUtils.isEmpty(adm)) {
            return false;
        }
        // Validate the XML by parsing it.
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final InputSource source = new InputSource(new StringReader(adm));
        try {
            final DocumentBuilder db = factory.newDocumentBuilder();
            db.setErrorHandler(null);
            db.parse(source);
            return true;
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOG.debug("VAST response is NOT a valid XML.", e);
            return false;
        }
    }

    /**
     * This function returns the list of applicable ix blocklists. <br>
     * 1) Publisher defined blocklists (advertisers and industries) are always included <br>
     * 2) Advertiser, Industry and Creative Attribute blocklists are checked individually first at the site level, <br>
     * if not found then at the country level, otherwise global defaults are used. <br>
     * 3) Blocklists with empty blocklists, imply that nothing is to be blocked.
     *
     * @param sasParams
     * @param repositoryHelper
     * @param traceMarker
     * @return The list of publisher and inmobi defined ix blocklists
     */
    public static List<String> getBlocklists(final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final Marker traceMarker) {
        final String siteId = sasParams.getSiteId();
        final long siteIncId = sasParams.getSiteIncId();
        final long countryId = sasParams.getCountryId();
        final ContentType siteContentRating = sasParams.getSiteContentType();

        LOG.debug(traceMarker, "Setting publisher and strategic blocklists based on: siteId: {}, siteIncId: {}, "
                + "countryId: {}, siteContentRating: {}", siteId, siteIncId, countryId, siteContentRating);

        final ImmutableList.Builder<String> blocklistBuilder = new ImmutableList.Builder<>();
        String blocklistName;

        blocklistName = String.format(PUBLISHER_BLOCKLIST_FORMAT, sasParams.getSiteIncId());
        blocklistBuilder.add(blocklistName);
        LOG.debug(traceMarker, "Setting publisher defined blocklist, {}", blocklistName);

        for (final IXBlocklistType blocklistType : supportedBlocklistTypes) {
            blocklistName = null;
            final IXBlocklistEntity siteBlocklistEntity =
                    repositoryHelper.queryIXBlocklistRepository(siteId, SITE, blocklistType);

            if (null != siteBlocklistEntity) {
                if (0 != siteBlocklistEntity.getBlocklistSize()) {
                    blocklistName = siteBlocklistEntity.getBlocklistName();
                    LOG.debug(traceMarker, "Setting strategic {} blocklist at the site level, {}", blocklistType,
                            blocklistName);
                } else {
                    LOG.debug(traceMarker, "No strategic {} blocklists", blocklistType);
                }
            } else {
                final IXBlocklistEntity countryBlocklistEntity =
                        repositoryHelper.queryIXBlocklistRepository(String.valueOf(countryId), COUNTRY, blocklistType);
                if (null != countryBlocklistEntity) {
                    if (0 != countryBlocklistEntity.getBlocklistSize()) {
                        blocklistName = countryBlocklistEntity.getBlocklistName();
                        LOG.debug(traceMarker, "Setting strategic {} blocklist at the country level, {}", blocklistType,
                                blocklistName);
                    } else {
                        LOG.debug(traceMarker, "No strategic {} blocklists", blocklistType);
                    }
                } else if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
                    blocklistName = inmobiPerfBlocklistMap.get(blocklistType);
                    LOG.debug(traceMarker, "Setting strategic {} blocklist at the global level, {}", blocklistType,
                            blocklistName);
                } else {
                    blocklistName = inmobiFsBlocklistMap.get(blocklistType);
                    LOG.debug(traceMarker, "Setting strategic {} blocklist at the global level, {}", blocklistType,
                            blocklistName);
                }
            }
            if (null != blocklistName) {
                blocklistBuilder.add(blocklistName);
            }
        }

        return blocklistBuilder.build();
    }

    /**
     *
     * Macros used -
     *
     * $VASTContentJSEsc <br>
     * ${first.ns}<br>
     * $tool.jsInline($first.beaconUrl)<br>
     * $tool.jsInline($first.clickServerUrl)<br>
     *
     * $ad.supplyWidth<br>
     * $ad.supplyHeight<br>
     * ${ad.sdkVersion}<br>
     * ${ad.sitePreferencesJson}<br>
     * ${ad.requestJson}
     *
     * @param sasParams
     * @param repositoryHelper
     * @param processedSlotId
     * @param beaconUrl
     * @param clickUrl
     * @param adMarkup
     * @param winUrl
     * @param thirdPartyTrackerMap
     * @return
     * @throws Exception
     */
    public static String videoAdBuilding(final TemplateTool tool, final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final Short processedSlotId, final String beaconUrl,
            final String clickUrl, final String adMarkup, final String winUrl, final boolean isRewardedVideoRequest,
            final Map<String, String> thirdPartyTrackerMap, final boolean isViewabilityDeal) throws Exception {
        LOG.debug("videoAdBuilding");
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(VAST_CONTENT_JS_ESC, StringEscapeUtils.escapeJavaScript(adMarkup));
        // JS escaped WinUrl for partner.
        if (StringUtils.isNotEmpty(winUrl)) {
            velocityContext.put(PARTNER_BEACON_URL, StringEscapeUtils.escapeJavaScript(winUrl));
        }

        // JS escaped BeaconUrl
        velocityContext.put(IM_BEACON_URL, beaconUrl);

        //adding all the third party trackers
        thirdPartyTrackerMap.forEach(velocityContext::put);

        final GenericTemplateObject vastTemplFirst = new GenericTemplateObject();
        // JS escaped IM beacon and click URLs.
        vastTemplFirst.setBeaconUrl(StringEscapeUtils.escapeJavaScript(beaconUrl));

        // adding billing, audience verification and impression tracker into billing array for velocity macro
        final List<String> billingUrlList = new ArrayList<>();

        billingUrlList.add(StringEscapeUtils.escapeJavaScript(beaconUrl + BILLING_BEACON_SUFFIX));

        final String audienceVerification = thirdPartyTrackerMap.get(AUDIENCE_VERIFICATION_TRACKER);
        if (StringUtils.isNotBlank(audienceVerification)) {
            billingUrlList.add(StringEscapeUtils.escapeJavaScript(audienceVerification));
        }

        final String thirdPartyImpressionTracker = thirdPartyTrackerMap.get(THIRD_PARTY_IMPRESSION_TRACKER);
        if (StringUtils.isNotBlank(thirdPartyImpressionTracker)) {
            billingUrlList.add(StringEscapeUtils.escapeJavaScript(thirdPartyImpressionTracker));
        }

        String[] billingUrlArray = new String[billingUrlList.size()];
        vastTemplFirst.setBillingUrlArray(billingUrlList.toArray(billingUrlArray));

        // adding third party click tracker into click array for velocity macro
        final List<String> clickUrlList = new ArrayList<>();
        final String thirdPartyClickTracker = thirdPartyTrackerMap.get(THIRD_PARTY_CLICK_TRACKER);
        if (StringUtils.isNotBlank(thirdPartyClickTracker)) {
            clickUrlList.add(StringEscapeUtils.escapeJavaScript(thirdPartyClickTracker));
        }
        final String clickUrlArray[] = new String[clickUrlList.size()];
        vastTemplFirst.setClickUrlArray(clickUrlList.toArray(clickUrlArray));

        vastTemplFirst.setClickServerUrl(StringEscapeUtils.escapeJavaScript(clickUrl));

        // Namespace
        vastTemplFirst.setNs(Formatter.getIXNamespace());

        final GenericTemplateObject vastTemplAd = new GenericTemplateObject();
        // SDK version
        vastTemplAd.setSdkVersion(sasParams.getSdkVersion());
        // Sprout related parameters.
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(processedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            vastTemplAd.setSupplyWidth((int) dim.getWidth());
            vastTemplAd.setSupplyHeight((int) dim.getHeight());
        }

        final ConnectionType connectionType = sasParams.getConnectionType();
        final String connectionTypeString =
                null != connectionType && ConnectionType.WIFI == connectionType ? WIFI : NON_WIFI;
        final String requestNetworkTypeJson = "{\"networkType\":\"" + connectionTypeString + "\"}";
        // Publisher control settings
        vastTemplAd.setRequestJson(requestNetworkTypeJson);
        vastTemplAd.setSitePreferencesJson(sasParams.getPubControlPreferencesJson());

        // iOS-9 ATS
        vastTemplAd.setSecure(sasParams.isSecureRequest());
        vastTemplAd.setViewability(isViewabilityDeal);

        // Add object to velocityContext
        velocityContext.put(FIRST_OBJECT_PREFIX, vastTemplFirst);
        velocityContext.put(AD_OBJECT_PREFIX, vastTemplAd);
        velocityContext.put(TOOL_OBJECT, tool);

        final TemplateType templateType =
                isRewardedVideoRequest ? INTERSTITIAL_REWARDED_VAST_VIDEO : INTERSTITIAL_VAST_VIDEO;
        return Formatter.getResponseFromTemplate(templateType, velocityContext, sasParams, null);
    }

    /**
     * ${first.ns} <br>
     * $first.supplyWidth <br>
     * $first.supplyHeight <br>
     * $first.cauElementJsonObject <br>
     * $CAUContentJSEsc
     */
    public static String cauAdBuilding(final SASRequestParameters sasParams, final IXSlotMatcher matchedSlot,
            final String beaconUrl, final String clickUrl, final String adMarkup, final String winUrl,
            final Map<String, String> thirdPartyTrackerMap, final boolean isViewabilityDeal) throws Exception {
        LOG.debug("cauAdBuilding");
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(CAU_CONTENT_JS_ESC, adMarkup);
        // JS escaped WinUrl for partner.
        if (StringUtils.isNotEmpty(winUrl)) {
            velocityContext.put(PARTNER_BEACON_URL, StringEscapeUtils.escapeJavaScript(winUrl));
        }

        // JS escaped IMWinUrl
        velocityContext.put(IM_BEACON_URL, beaconUrl);
        velocityContext.put(IM_CLICK_URL, StringEscapeUtils.escapeJavaScript(clickUrl));

        //adding all the third party trackers
        thirdPartyTrackerMap.forEach(velocityContext::put);

        velocityContext.put(VelocityTemplateFieldConstants.VIEWABILE, isViewabilityDeal);

        final GenericTemplateObject templateFirst = new GenericTemplateObject();
        // Set CAU Element JSON
        templateFirst.setCauElementJsonObject(matchedSlot.getMatchedCau().getElementSecureJson());
        // JS escaped IM beacon and click URLs.
        templateFirst.setClickServerUrl(StringEscapeUtils.escapeJavaScript(clickUrl));
        // Set height and width
        templateFirst.setHeight((int) matchedSlot.getMatchedRPDimension().getHeight());
        templateFirst.setWidth((int) matchedSlot.getMatchedRPDimension().getWidth());
        // Namespace
        templateFirst.setNs(Formatter.getIXNamespace());
        // Add object to velocityContext
        velocityContext.put(FIRST_OBJECT_PREFIX, templateFirst);

        return Formatter.getResponseFromTemplate(CAU, velocityContext, sasParams, null);
    }

    static String pureVastAdBuilding(String adMarkUp, final String beaconUrl, final String clickUrl, boolean addSdkTimeStamp)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {
        final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments(true);
        adMarkUp = adMarkUp.replaceFirst("^([\\W]+)<", "<");  //remove some special char in start to parse xml
        
        final DocumentBuilder builder = domFactory.newDocumentBuilder();
        final Document doc = builder.parse(new InputSource(new ByteArrayInputStream(adMarkUp.getBytes(UTF_8))));
        final NodeList trackingEventsNode = doc.getElementsByTagName(VastHtmlTags.TRACKING_EVENTS_TAG.htmlTag);
        final NodeList inlineNode = doc.getElementsByTagName(VastHtmlTags.IN_LINE_TAG.htmlTag);
        final NodeList wrapperNode = doc.getElementsByTagName(VastHtmlTags.WRAPPER_TAG.htmlTag);

        final StringBuilder impressionUriStr = new StringBuilder(beaconUrl).append(RENDER_BEACON_SUFFIX);
        final StringBuilder errorUriStr = new StringBuilder(beaconUrl).append(ERROR_BEACON_SUFFIX);

        if (addSdkTimeStamp) {
            impressionUriStr.append(SDK_TIMESTAMP_SUFFIX);
            errorUriStr.append(SDK_TIMESTAMP_SUFFIX);
        }

        if (0 != inlineNode.getLength()) {
            addInXml(doc, inlineNode, VastHtmlTags.IMPRESSION_TAG.htmlTag, null, null, impressionUriStr.toString());
            addInXml(doc, inlineNode, VastHtmlTags.ERROR_TAG.htmlTag, null, null, errorUriStr.toString());
            addClickTracking(doc, inlineNode, clickUrl, beaconUrl);
        } else if (0 != wrapperNode.getLength()) {
            addInXml(doc, wrapperNode, VastHtmlTags.IMPRESSION_TAG.htmlTag, null, null, impressionUriStr.toString());
            addInXml(doc, wrapperNode, VastHtmlTags.ERROR_TAG.htmlTag, null, null, errorUriStr.toString());
            addClickTracking(doc, wrapperNode, clickUrl, beaconUrl);
        } else {
            InspectorStats.incrementStatCount(TOTAL_PURE_VAST_RESPONSE_INLINE_OR_WRAPPER_MISSING);
            throw new ParserConfigurationException();
        }

        if (0 != trackingEventsNode.getLength()) {
            final StringBuilder startUriStr = new StringBuilder(beaconUrl).append(MEDIA_START_BEACON_SUFFIX);
            final StringBuilder billingUriStr = new StringBuilder(beaconUrl).append(BILLING_BEACON_SUFFIX);
            final StringBuilder firstQuartileUriStr = new StringBuilder(beaconUrl).append(Q1_BEACON_SUFFIX);
            final StringBuilder midPointUriStr = new StringBuilder(beaconUrl).append(Q2_BEACON_SUFFIX);
            final StringBuilder thirdQuartileUriStr = new StringBuilder(beaconUrl).append(Q3_BEACON_SUFFIX);
            final StringBuilder completeUriStr = new StringBuilder(beaconUrl).append(Q4_BEACON_SUFFIX);
            final StringBuilder viewUriStr = new StringBuilder(beaconUrl).append(CREATIVE_VIEW_BEACON_SUFFIX);
            final StringBuilder pauseUriStr = new StringBuilder(beaconUrl).append(MEDIA_PAUSE_BEACON_SUFFIX);
            final StringBuilder unpauseUriStr = new StringBuilder(beaconUrl).append(MEDIA_UNPAUSE_BEACON_SUFFIX);
            final StringBuilder skipUriStr = new StringBuilder(beaconUrl).append(MEDIA_SKIP_BEACON_SUFFIX);

            if (addSdkTimeStamp) {
                startUriStr.append(SDK_TIMESTAMP_SUFFIX);
                billingUriStr.append(SDK_TIMESTAMP_SUFFIX);
                firstQuartileUriStr.append(SDK_TIMESTAMP_SUFFIX);
                midPointUriStr.append(SDK_TIMESTAMP_SUFFIX);
                thirdQuartileUriStr.append(SDK_TIMESTAMP_SUFFIX);
                completeUriStr.append(SDK_TIMESTAMP_SUFFIX);
                viewUriStr.append(SDK_TIMESTAMP_SUFFIX);
                pauseUriStr.append(SDK_TIMESTAMP_SUFFIX);
                unpauseUriStr.append(SDK_TIMESTAMP_SUFFIX);
                skipUriStr.append(SDK_TIMESTAMP_SUFFIX);
            }

            final String eventTag = VastHtmlTags.EVENT_ATTR.htmlTag;
            final String trackingTag = VastHtmlTags.TRACKING_TAG.htmlTag;
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, START.eventTag, startUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, START.eventTag, billingUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, FIRST_QUARTILE.eventTag, firstQuartileUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, MIDPOINT.eventTag, midPointUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, THIRD_QUARTILE.eventTag, thirdQuartileUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, COMPLETE.eventTag, completeUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, CREATIVE_VIEW.eventTag, viewUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, MEDIA_PAUSE.eventTag, pauseUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, MEDIA_UNPAUSE.eventTag, unpauseUriStr.toString());
            addInXml(doc, trackingEventsNode, trackingTag, eventTag, SKIP.eventTag, skipUriStr.toString());
        } else {
            InspectorStats.incrementStatCount(TOTAL_PURE_VAST_RESPONSE_TRACKING_EVENTS_MISSING);
            throw new ParserConfigurationException();
        }

        final StreamResult result = new StreamResult(new StringWriter());
        final DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return result.getWriter().toString();
    }


    private static void addClickTracking(final Document doc, final NodeList parentNodeList, final String clickUrl,
            final String beaconUrl) {
        final NodeList videoClickNode = doc.getElementsByTagName(VastHtmlTags.VIDEO_CLICKS_TAG.htmlTag);
        final String beaconClickuUriStr = beaconUrl + CLICK_BEACON_SUFFIX;
        if (0 == videoClickNode.getLength()) {
            final Element docElm = doc.createElement(VastHtmlTags.VIDEO_CLICKS_TAG.htmlTag);
            parentNodeList.item(0).appendChild(docElm);
            addInXml(doc, docElm, VastHtmlTags.CLICK_TRACKING_TAG.htmlTag, null, null, clickUrl);
            addInXml(doc, docElm, VastHtmlTags.CLICK_TRACKING_TAG.htmlTag, null, null, beaconClickuUriStr);
        } else {
            addInXml(doc, videoClickNode, VastHtmlTags.CLICK_TRACKING_TAG.htmlTag, null, null, clickUrl);
            addInXml(doc, videoClickNode, VastHtmlTags.CLICK_TRACKING_TAG.htmlTag, null, null, beaconClickuUriStr);
        }
    }

    // doc -> nodeList[0] -> EleementTag -> attributeKey, attributeValue -> data = uri
    private static void addInXml(final Document doc, final NodeList nodeList, final String elmStr, final String key,
            final String value, final String uri) {
        nodeList.item(0).appendChild(getDocElm(doc, elmStr, key, value, uri));
    }

    private static void addInXml(final Document doc, final Element element, final String elmStr, final String key,
            final String value, final String uri) {
        element.appendChild(getDocElm(doc, elmStr, key, value, uri));
    }

    private static Element getDocElm(final Document doc, final String elmStr, final String key, final String value,
            final String uri) {
        final CDATASection cdata = doc.createCDATASection(uri);
        final Element docElm = doc.createElement(elmStr);
        if (null != key && null != value) {
            docElm.setAttribute(key, value);
        }
        docElm.appendChild(cdata);

        if (null == key) {
            LOG.debug("{} added in VAST xml", elmStr);
        } else {
            LOG.debug("{} added in VAST xml key is : {} and value is : {}", elmStr, key, value);
        }
        return docElm;
    }

    static String replaceAudienceVerificationTrackerMacros(String audienceVerificationTracker,
            final Map<String, String> macroData) {
        audienceVerificationTracker = StringUtils.replace(audienceVerificationTracker, MacroData.$LIMIT_AD_TRACKING,
            macroData.get(MacroData.$LIMIT_AD_TRACKING));
        audienceVerificationTracker =
            StringUtils.replace(audienceVerificationTracker, MacroData.$IMP_CB, macroData.get(MacroData.$IMP_CB));
        audienceVerificationTracker = StringUtils.replace(audienceVerificationTracker, MacroData.$USER_ID_SHA256_HASHED,
            DigestUtils.sha256Hex(macroData.get(MacroData.$USER_ID)));
        return audienceVerificationTracker;
    }

    enum VastTrackingEventsTypeEnum {
        START("start"),
        FIRST_QUARTILE("firstQuartile"),
        MIDPOINT("midpoint"),
        THIRD_QUARTILE("thirdQuartile"),
        COMPLETE("complete"),
        CREATIVE_VIEW("creativeView"),
        MEDIA_PAUSE("pause"),
        MEDIA_UNPAUSE("resume"),
        SKIP("skip");

        private final String eventTag;

        VastTrackingEventsTypeEnum(String eventTag) {
            this.eventTag = eventTag;
        }
    }

    private enum VastHtmlTags {
        IMPRESSION_TAG("Impression"),
        ERROR_TAG("Error"),
        TRACKING_TAG("Tracking"),
        CLICK_TRACKING_TAG("ClickTracking"),
        EVENT_ATTR("event"),
        TRACKING_EVENTS_TAG("TrackingEvents"),
        IN_LINE_TAG("InLine"),
        WRAPPER_TAG("Wrapper"),
        VIDEO_CLICKS_TAG("VideoClicks");

        private final String htmlTag;

        VastHtmlTags(String htmlTag) {
            this.htmlTag = htmlTag;
        }
    }

}
