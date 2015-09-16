/**
 *
 */
package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.api.BaseAdNetworkImpl.getHashedValue;
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
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.IMP_CB;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_BEACON_URL;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_CLICK_URL;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_GEO_CITY;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.OPEN_LP_FUN;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.RECORD_EVENT_FUN;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SDK_VERSION_ID;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SECURE;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SITE_PREFERENCES_JSON;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.USER_ID;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.USER_ID_MD5_HASHED;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.USER_ID_SHA1_HASHED;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.IM_BEACON_URL;
import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.IM_CLICK_URL;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.MD5;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.NON_WIFI;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.SHA1;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.WIFI;
import static com.inmobi.adserve.contracts.ix.request.nativead.Asset.AssetType.DATA;
import static com.inmobi.adserve.contracts.ix.request.nativead.Asset.AssetType.IMAGE;
import static com.inmobi.adserve.contracts.ix.request.nativead.Asset.AssetType.TITLE;
import static com.inmobi.adserve.contracts.ix.request.nativead.Asset.AssetType.VIDEO;
import static com.inmobi.adserve.contracts.ix.request.nativead.Data.DataAssetType.CTA_TEXT;
import static com.inmobi.adserve.contracts.ix.request.nativead.Data.DataAssetType.DESC;
import static com.inmobi.adserve.contracts.ix.request.nativead.Data.DataAssetType.DOWNLOADS;
import static com.inmobi.adserve.contracts.ix.request.nativead.Data.DataAssetType.RATING;

import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
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
import com.inmobi.adserve.channels.util.SproutTemplateConstants;
import com.inmobi.adserve.contracts.ix.request.Geo;
import com.inmobi.adserve.contracts.ix.request.nativead.Asset;
import com.inmobi.adserve.contracts.ix.request.nativead.Image;
import com.inmobi.adserve.contracts.ix.response.nativead.Native;
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

    private static final ImmutableMap<IXBlocklistType, String> inmobiPerfBlocklistMap = ImmutableMap.of(
            IXBlocklistType.ADVERTISERS, IX_PERF_ADVERTISER_BLOCKLIST_ID, IXBlocklistType.INDUSTRY_IDS,
            IX_PERF_INDUSTRY_BLOCKLIST_ID, IXBlocklistType.CREATIVE_ATTRIBUTE_IDS,
            IX_PERF_CREATIVE_ATTRIBUTE_BLOCKLIST_ID);
    private static final ImmutableMap<IXBlocklistType, String> inmobiFsBlocklistMap = ImmutableMap.of(
            IXBlocklistType.ADVERTISERS, IX_FS_ADVERTISER_BLOCKLIST_ID, IXBlocklistType.INDUSTRY_IDS,
            IX_FS_INDUSTRY_BLOCKLIST_ID, IXBlocklistType.CREATIVE_ATTRIBUTE_IDS, IX_FS_CREATIVE_ATTRIBUTE_BLOCKLIST_ID);
    private static final ImmutableList<IXBlocklistType> supportedBlocklistTypes = ImmutableList.of(
            IXBlocklistType.ADVERTISERS, IXBlocklistType.INDUSTRY_IDS, IXBlocklistType.CREATIVE_ATTRIBUTE_IDS);

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

        final String subsBeaconUrl = beaconUrl + "?b=${WIN_BID}${DEAL_GET_PARAM}";
        addSproutMacroToList(macros, substitutions, JS_ESC_BEACON_URL,
                StringEscapeUtils.escapeJavaScript(subsBeaconUrl));
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
            final com.inmobi.adserve.contracts.ix.response.nativead.Asset responseAsset,
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
                final com.inmobi.adserve.contracts.ix.response.nativead.Image img = responseAsset.getImg();
                final Integer width = img.getW() == null ? requestAsset.getImg().getWmin() : img.getW();
                final Integer height = img.getH() == null ? requestAsset.getImg().getHmin() : img.getH();
                if (Image.ImageAssetType.ICON.getId() == requestAsset.getImg().getType()) {
                    final Icon.Builder iconbuilder = Icon.newBuilder();
                    iconbuilder.setUrl(img.getUrl());
                    iconbuilder.setW(width);
                    iconbuilder.setH(height);
                    contextBuilder.setIcons(Arrays.asList(new Icon[] {(Icon) iconbuilder.build()}));
                } else if (Image.ImageAssetType.MAIN.getId() == requestAsset.getImg().getType()) {
                    final Screenshot.Builder screenshotBuilder = Screenshot.newBuilder();
                    screenshotBuilder.setUrl(img.getUrl());
                    if (width < requestAsset.getImg().getWmin() || height < requestAsset.getImg().getHmin()) {
                        LOG.debug("Image Constraints not met.");
                        return false;
                    }
                    screenshotBuilder.setW(width);
                    screenshotBuilder.setH(height);
                    final Double ar = (double) width / height;
                    screenshotBuilder.setAr(String.valueOf(ar));
                    contextBuilder.setScreenshots(Arrays.asList(new Screenshot[] {(Screenshot) screenshotBuilder
                            .build()}));
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

            final List<com.inmobi.adserve.contracts.ix.response.nativead.Asset> assetList = nativeObj.getAssets();
            for (final com.inmobi.adserve.contracts.ix.response.nativead.Asset asset : assetList) {
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
     * @param selectedSlotId
     * @param beaconUrl
     * @param clickUrl
     * @param adMarkup
     * @param winUrl
     * @return
     * @throws Exception
     */
    public static String videoAdBuilding(final TemplateTool tool, final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final Short selectedSlotId, final String beaconUrl,
            final String clickUrl, final String adMarkup, final String winUrl) throws Exception {
        LOG.debug("videoAdBuilding");
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(VAST_CONTENT_JS_ESC, StringEscapeUtils.escapeJavaScript(adMarkup));
        // JS escaped WinUrl for partner.
        if (StringUtils.isNotEmpty(winUrl)) {
            velocityContext.put(PARTNER_BEACON_URL, StringEscapeUtils.escapeJavaScript(winUrl));
        }

        // JS escaped BeaconUrl
        velocityContext.put(IM_BEACON_URL, beaconUrl);

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
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
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
        // Add object to velocityContext
        velocityContext.put(FIRST_OBJECT_PREFIX, vastTemplFirst);
        velocityContext.put(AD_OBJECT_PREFIX, vastTemplAd);
        velocityContext.put(TOOL_OBJECT, tool);

        return Formatter.getResponseFromTemplate(TemplateType.INTERSTITIAL_VIDEO, velocityContext, sasParams, null);
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
            final String beaconUrl, final String clickUrl, final String adMarkup, final String winUrl) throws Exception {
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

        final GenericTemplateObject templateFirst = new GenericTemplateObject();
        // Set CAU Element JSON
        templateFirst.setCauElementJsonObject(matchedSlot.getMatchedCau().getElementJson());
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
}
