/**
 *
 */
package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.api.BaseAdNetworkHelper.getHashedValue;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.AD_OBJECT_PREFIX;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.CAU_CONTENT_JS_ESC;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.FIRST_OBJECT_PREFIX;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.PARTNER_BEACON_URL;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.TOOL_OBJECT;
import static com.inmobi.adserve.channels.util.GenericTemplateObject.VAST_CONTENT_JS_ESC;
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
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.IM_BEACON_URL;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.IM_CLICK_URL;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.VIEWABILITY_TRACKER;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.MD5;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.NON_WIFI;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.SHA1;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.WIFI;
import static com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType.DATA;
import static com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType.IMAGE;
import static com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType.TITLE;
import static com.inmobi.adserve.contracts.common.request.nativead.Asset.AssetType.VIDEO;
import static com.inmobi.adserve.contracts.common.request.nativead.Data.DataAssetType.CTA_TEXT;
import static com.inmobi.adserve.contracts.common.request.nativead.Data.DataAssetType.DESC;
import static com.inmobi.adserve.contracts.common.request.nativead.Data.DataAssetType.DOWNLOADS;
import static com.inmobi.adserve.contracts.common.request.nativead.Data.DataAssetType.RATING;

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
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.adserve.channels.util.GenericTemplateObject;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.SproutTemplateConstants;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.contracts.common.request.nativead.Asset;
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

    private static final ImmutableMap<IXBlocklistType, String> inmobiPerfBlocklistMap = ImmutableMap.of(
            IXBlocklistType.ADVERTISERS, IX_PERF_ADVERTISER_BLOCKLIST_ID, IXBlocklistType.INDUSTRY_IDS,
            IX_PERF_INDUSTRY_BLOCKLIST_ID, IXBlocklistType.CREATIVE_ATTRIBUTE_IDS,
            IX_PERF_CREATIVE_ATTRIBUTE_BLOCKLIST_ID);
    private static final ImmutableMap<IXBlocklistType, String> inmobiFsBlocklistMap = ImmutableMap.of(
            IXBlocklistType.ADVERTISERS, IX_FS_ADVERTISER_BLOCKLIST_ID, IXBlocklistType.INDUSTRY_IDS,
            IX_FS_INDUSTRY_BLOCKLIST_ID, IXBlocklistType.CREATIVE_ATTRIBUTE_IDS, IX_FS_CREATIVE_ATTRIBUTE_BLOCKLIST_ID);
    private static final ImmutableList<IXBlocklistType> supportedBlocklistTypes = ImmutableList.of(
            IXBlocklistType.ADVERTISERS, IXBlocklistType.INDUSTRY_IDS, IXBlocklistType.CREATIVE_ATTRIBUTE_IDS);

    private static final String IMPRESSION_TAG = "Impression";
    private static final String ERROR_TAG = "Error";
    private static final String TRACKING_TAG = "Tracking";
    private static final String CLICK_TRACKING_TAG = "ClickTracking";
    private static final String EVENT_ATTR = "event";
    private static final String START = "start";
    private static final String FIRST_QUARTILE = "firstQuartile";
    private static final String MIDPOINT = "midpoint";
    private static final String THIRD_QUARTILE = "thirdQuartile";
    private static final String COMPLETE = "complete";
    private static final String TRACKING_EVENTS_TAG = "TrackingEvents";
    private static final String IN_LINE_TAG = "InLine";
    private static final String WRAPPER_TAG = "Wrapper";
    private static final String VIDEO_CLICKS_TAG = "VideoClicks";

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

        addSproutMacroToList(macros, substitutions, JS_ESC_BEACON_URL,
                StringEscapeUtils.escapeJavaScript(beaconUrl));
        addSproutMacroToList(macros, substitutions, JS_ESC_CLICK_URL, StringEscapeUtils.escapeJavaScript(clickUrl));

        final String sdkVersion = sasParams.getSdkVersion();
        addSproutMacroToList(macros, substitutions, SDK_VERSION_ID, null != sdkVersion ? sdkVersion : StringUtils.EMPTY);

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

        final String sitePreferences =
                null != sasParams.getPubControlPreferencesJson()
                        ? sasParams.getPubControlPreferencesJson()
                        : StringUtils.EMPTY;
        addSproutMacroToList(macros, substitutions, SITE_PREFERENCES_JSON, sitePreferences);
        addSproutMacroToList(macros, substitutions, JS_ESC_SITE_PREFERENCES_JSON, javascriptEscape(sitePreferences));

        final String userId =
                StringUtils.isNotEmpty(casInternal.getUidIFA()) ? casInternal.getUidIFA() : StringUtils
                        .isNotEmpty(casInternal.getGpid()) ? casInternal.getGpid() : null;

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
        addSproutMacroToList(macros, substitutions, HANDSET_TYPE, ObjectUtils.defaultIfNull(
                getHandSetTypeNameFromId(sasParams.getDeviceType().getValue()), StringUtils.EMPTY));
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
     * 1) we receive a corresponding Asset object for every requested required Asset object with the same id and type<br>
     * 2) Response Asset object doesn't contain more than one of title, img, video or data objects <br>
     * 3) Height and width are present for IMAGE MAIN objects <br>
     * 4) Minimum width and height image constraints are met for IMAGE MAIN objects<br>
     *
     * @param requestAsset
     * @param responseAsset
     * @param contextBuilder
     * @return
     */
    public static boolean areRequestResponseAssetsValid(final Asset requestAsset,
            final com.inmobi.adserve.contracts.common.response.nativead.Asset responseAsset,
            final com.inmobi.template.context.App.Builder contextBuilder) {
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
            LOG.debug("Aborting as more than one or none of title, img, video or data were present in the "
                    + "same Asset object");
            return false;
        }
        if (requestObject != responseObject) {
            LOG.debug("Aborting as type mismatch between Asset objects with id: {}. Received {} instead " + "of {}",
                    responseAsset.getId(), responseObject.name(), requestObject.name());
            return false;
        }

        // note: Current implementation doesn't support multiple assets of the same type
        switch (requestObject) {
            case TITLE:
                contextBuilder.setTitle(responseAsset.getTitle().getText());
                break;
            case IMAGE:
                final com.inmobi.adserve.contracts.common.response.nativead.Image img = responseAsset.getImg();
                final Integer width = img.getW() == null ? requestAsset.getImg().getWmin() : img.getW();
                final Integer height = img.getH() == null ? requestAsset.getImg().getHmin() : img.getH();
                if (Image.ImageAssetType.ICON.getId() == requestAsset.getImg().getType()) {
                    final Icon.Builder iconbuilder = Icon.newBuilder();
                    iconbuilder.setUrl(img.getUrl());
                    iconbuilder.setW(width);
                    iconbuilder.setH(height);
                    contextBuilder.setIcons(Collections.singletonList((Icon) iconbuilder.build()));
                } else if (Image.ImageAssetType.MAIN.getId() == requestAsset.getImg().getType()) {
                    final Screenshot.Builder screenshotBuilder = Screenshot.newBuilder();
                    screenshotBuilder.setUrl(img.getUrl());
                    if (width < requestAsset.getImg().getWmin() || height < requestAsset.getImg().getHmin()) {
                        LOG.debug("Image Constraints not met.");
                        return false;
                    }
                    screenshotBuilder.setW(width);
                    screenshotBuilder.setH(height);
                    contextBuilder.setScreenshots(Collections.singletonList((Screenshot) screenshotBuilder.build()));
                }
                break;
            case VIDEO:
                LOG.debug("Video objects are currently not supported for native");
                break;
            case DATA:
                if (DESC.getId() == requestAsset.getData().getType()) {
                    contextBuilder.setDesc(responseAsset.getData().getValue());
                } else if (CTA_TEXT.getId() == requestAsset.getData().getType()) {
                    contextBuilder.setActionText(responseAsset.getData().getValue());
                } else if (DOWNLOADS.getId() == requestAsset.getData().getType()) {
                    contextBuilder.setDownloads(Integer.valueOf(responseAsset.getData().getValue()));
                } else if (RATING.getId() == requestAsset.getData().getType()) {
                    contextBuilder.setRating(responseAsset.getData().getValue());
                }
        }

        return true;
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

            final List<com.inmobi.adserve.contracts.common.response.nativead.Asset> assetList = nativeObj.getAssets();
            for (final com.inmobi.adserve.contracts.common.response.nativead.Asset asset : assetList) {
                final int assetId = asset.getId();

                // We need all of mandatory fields. And Requested type should be same as response
                if (mandatoryAssetMap.containsKey(assetId)) {
                    final boolean isReqValid =
                            areRequestResponseAssetsValid(mandatoryAssetMap.get(assetId), asset, contextBuilder);
                    if (isReqValid) {
                        mandatoryAssetMap.remove(assetId);
                        continue;
                    } else {
                        return null;
                    }
                }

                // We do not need all of mandatory fields. But Requested type should be same as response
                if (nonMandatoryAssetMap.containsKey(assetId)) {
                    final boolean isReqValid =
                            areRequestResponseAssetsValid(nonMandatoryAssetMap.get(assetId), asset, contextBuilder);
                    if (!isReqValid) {
                        return null;
                    }
                }
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
                    repositoryHelper.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE, blocklistType);

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
                        repositoryHelper.queryIXBlocklistRepository(String.valueOf(countryId),
                                IXBlocklistKeyType.COUNTRY, blocklistType);
                if (null != countryBlocklistEntity) {
                    if (0 != countryBlocklistEntity.getBlocklistSize()) {
                        blocklistName = countryBlocklistEntity.getBlocklistName();
                        LOG.debug(traceMarker, "Setting strategic {} blocklist at the country level, {}",
                                blocklistType, blocklistName);
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
     * @return
     * @throws Exception
     */
    public static String videoAdBuilding(final TemplateTool tool, final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final Short processedSlotId, final String beaconUrl,
            final String clickUrl, final String adMarkup, final String winUrl, final boolean isRewardedVideoRequest,
            final String viewabilityTracker, final boolean isViewabilityDeal) throws Exception {
        LOG.debug("videoAdBuilding");
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(VAST_CONTENT_JS_ESC, StringEscapeUtils.escapeJavaScript(adMarkup));
        // JS escaped WinUrl for partner.
        if (StringUtils.isNotEmpty(winUrl)) {
            velocityContext.put(PARTNER_BEACON_URL, StringEscapeUtils.escapeJavaScript(winUrl));
        }

        // JS escaped BeaconUrl
        velocityContext.put(IM_BEACON_URL, beaconUrl);

        // Viewability Tracker
        if (StringUtils.isNotBlank(viewabilityTracker)) {
            velocityContext.put(VIEWABILITY_TRACKER, viewabilityTracker);
        }

        final GenericTemplateObject vastTemplFirst = new GenericTemplateObject();
        // JS escaped IM beacon and click URLs.
        vastTemplFirst.setBeaconUrl(StringEscapeUtils.escapeJavaScript(beaconUrl));
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
                isRewardedVideoRequest
                        ? TemplateType.INTERSTITIAL_REWARDED_VAST_VIDEO
                        : TemplateType.INTERSTITIAL_VAST_VIDEO;

        return Formatter.getResponseFromTemplate(templateType, velocityContext, sasParams, null);
    }

    /**
     * ${first.ns} <br>
     * $first.supplyWidth <br>
     * $first.supplyHeight <br>
     * $first.cauElementJsonObject <br>
     * $CAUContentJSEsc
     *
     * @param sasParams
     * @param matchedSlot
     * @param beaconUrl
     * @param clickUrl
     * @param adMarkup
     * @param winUrl
     * @return
     * @throws Exception
     */
    public static String cauAdBuilding(final SASRequestParameters sasParams, final IXSlotMatcher matchedSlot,
            final String beaconUrl, final String clickUrl, final String adMarkup, final String winUrl,
            final String viewabilityTracker, final boolean isViewabilityDeal) throws Exception {
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

        // Viewability Tracker
        if (StringUtils.isNotBlank(viewabilityTracker)) {
            velocityContext.put(VIEWABILITY_TRACKER, viewabilityTracker);
        }
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

        return Formatter.getResponseFromTemplate(TemplateType.CAU, velocityContext, sasParams, null);
    }

    public static String pureVastAdBuilding(String adMarkUp, final String beaconUrl, final String clickUrl) throws
            ParserConfigurationException, IOException, SAXException, TransformerException {
        final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments(true);
        final DocumentBuilder builder = domFactory.newDocumentBuilder();
        adMarkUp = adMarkUp.replaceFirst("^([\\W]+)<", "<");  //remove some special char in start to parse xml
        final Document doc = builder.parse(new InputSource(new ByteArrayInputStream(adMarkUp.getBytes("utf-8"))));

        final NodeList trackingEventsNode = doc.getElementsByTagName(TRACKING_EVENTS_TAG);
        final NodeList inlineNode = doc.getElementsByTagName(IN_LINE_TAG);
        final NodeList wrapperNode = doc.getElementsByTagName(WRAPPER_TAG);

        final String impressionUriStr = beaconUrl + "?m=18";
        final String errorUriStr = beaconUrl + "?m=99&action=vast-error&label=[ERRORCODE]";

        if (0 != inlineNode.getLength()) {
            addInXml(doc, inlineNode, IMPRESSION_TAG, null, null, impressionUriStr);
            addInXml(doc, inlineNode, ERROR_TAG, null, null, errorUriStr);
            addClickTracking(doc, inlineNode, clickUrl, beaconUrl);
        } else if (0 != wrapperNode.getLength()) {
            addInXml(doc, wrapperNode, IMPRESSION_TAG, null, null, impressionUriStr);
            addInXml(doc, wrapperNode, ERROR_TAG, null, null, errorUriStr);
            addClickTracking(doc, wrapperNode, clickUrl, beaconUrl);
        } else {
            InspectorStats.incrementStatCount(InspectorStrings.TOTAL_PURE_VAST_RESPONSE_INLINE_OR_WRAPPER_MISSING);
            throw new ParserConfigurationException();
        }



        if (0 != trackingEventsNode.getLength()) {
            final String startUriStr = beaconUrl + "?m=10";
            final String billingUriStr = beaconUrl + "?b=${WIN_BID}${DEAL_GET_PARAM}";
            final String firstQuartileUriStr = beaconUrl + "?m=12&q=1&mid=video&__t=0";
            final String midPointUriStr = beaconUrl + "?m=12&q=2&mid=video&__t=0";
            final String thirdQuartileUriStr = beaconUrl + "?m=12&q=3&mid=video&__t=0";
            final String completeUriStr = beaconUrl + "?m=13&mid=video&__t=0";

            addInXml(doc, trackingEventsNode, TRACKING_TAG, EVENT_ATTR, START, startUriStr);
            addInXml(doc, trackingEventsNode, TRACKING_TAG, EVENT_ATTR, START, billingUriStr);
            addInXml(doc, trackingEventsNode, TRACKING_TAG, EVENT_ATTR, FIRST_QUARTILE, firstQuartileUriStr);
            addInXml(doc, trackingEventsNode, TRACKING_TAG, EVENT_ATTR, MIDPOINT, midPointUriStr);
            addInXml(doc, trackingEventsNode, TRACKING_TAG, EVENT_ATTR, THIRD_QUARTILE, thirdQuartileUriStr);
            addInXml(doc, trackingEventsNode, TRACKING_TAG, EVENT_ATTR, COMPLETE, completeUriStr);
        } else  {
            InspectorStats.incrementStatCount(InspectorStrings.TOTAL_PURE_VAST_RESPONSE_TRACKING_EVENTS_MISSING);
            throw new ParserConfigurationException();
        }

        final StreamResult result = new StreamResult(new StringWriter());
        final DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return result.getWriter().toString();
    }


    private static void addClickTracking(final Document doc, final NodeList parentNodeList, final String clickUrl, final String beaconUrl) {
        final NodeList videoClickNode = doc.getElementsByTagName(VIDEO_CLICKS_TAG);
        final String beaconClickuUriStr = beaconUrl + "?m=8";
        if (0 == videoClickNode.getLength()) {
            final Element docElm = doc.createElement(VIDEO_CLICKS_TAG);
            parentNodeList.item(0).appendChild(docElm);
            addInXml(doc, docElm, CLICK_TRACKING_TAG, null, null, clickUrl);
            addInXml(doc, docElm, CLICK_TRACKING_TAG, null, null, beaconClickuUriStr);
        } else {
            addInXml(doc, videoClickNode, CLICK_TRACKING_TAG, null, null, clickUrl);
            addInXml(doc, videoClickNode, CLICK_TRACKING_TAG, null, null, beaconClickuUriStr);
        }
    }

    // doc -> nodeList[0] -> EleementTag -> attributeKey, attributeValue -> data = uri
    private static void addInXml(final Document doc, final NodeList nodeList, final String elmStr,
                                 final String key, final String value, final String uri) {
        nodeList.item(0).appendChild(getDocElm(doc, elmStr, key, value, uri));
    }

    private static void addInXml(final Document doc, final Element element, final String elmStr,
                                 final String key, final String value, final String uri) {
        element.appendChild(getDocElm(doc, elmStr, key, value, uri));
    }

    private static Element getDocElm(final Document doc, final String elmStr, final String key, final String value, final String uri) {
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
}
