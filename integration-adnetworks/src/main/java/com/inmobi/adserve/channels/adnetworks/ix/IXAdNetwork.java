package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.util.SproutTemplateConstants.GEO_CC;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.GEO_LAT;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.GEO_LNG;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.GEO_ZIP;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_BEACON_URL;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_CLICK_URL;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.JS_ESC_GEO_CITY;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.OPEN_LP_FUN;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.RECORD_EVENT_FUN;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SPROUT_UNIQUE_STRING;
import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SDK_VERSION_ID;

import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.googlecode.cqengine.resultset.common.NoSuchObjectException;
import com.googlecode.cqengine.resultset.common.NonUniqueObjectException;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ClickUrlsRegenerator;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.casthrift.ix.API_FRAMEWORKS;
import com.inmobi.casthrift.ix.AdQuality;
import com.inmobi.casthrift.ix.App;
import com.inmobi.casthrift.ix.Banner;
import com.inmobi.casthrift.ix.Bid;
import com.inmobi.casthrift.ix.Blind;
import com.inmobi.casthrift.ix.CommonExtension;
import com.inmobi.casthrift.ix.Device;
import com.inmobi.casthrift.ix.ExtRubiconTarget;
import com.inmobi.casthrift.ix.Geo;
import com.inmobi.casthrift.ix.IXBidRequest;
import com.inmobi.casthrift.ix.IXBidResponse;
import com.inmobi.casthrift.ix.Impression;
import com.inmobi.casthrift.ix.ProxyDemand;
import com.inmobi.casthrift.ix.Publisher;
import com.inmobi.casthrift.ix.Regs;
import com.inmobi.casthrift.ix.RubiconExtension;
import com.inmobi.casthrift.ix.SeatBid;
import com.inmobi.casthrift.ix.Site;
import com.inmobi.casthrift.ix.Transparency;
import com.inmobi.casthrift.ix.User;
import com.inmobi.casthrift.ix.Video;
import com.inmobi.casthrift.ix.VideoExtension;
import com.inmobi.segment.impl.AdTypeEnum;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.Setter;


/**
 * Generic IX adapter.
 * 
 * @author Anshul Soni(anshul.soni@inmobi.com)
 */
public class IXAdNetwork extends BaseAdNetworkImpl {

    protected static final String MRAID = "<script src=\"mraid.js\" ></script>";
    private static final Logger LOG = LoggerFactory.getLogger(IXAdNetwork.class);
    private static final String CONTENT_TYPE_VALUE = "application/json; charset=utf-8";
    private static final String DISPLAY_MANAGER_INMOBI_SDK = "inmobi_sdk";
    private static final String DISPLAY_MANAGER_INMOBI_JS = "inmobi_js";
    private static final String USD = "USD";
    private static final String SITE_BLOCKLIST_FORMAT = "blk%s";
    private static final String RUBICON_PERF_BLOCKLIST_ID = "InMobiPERF";
    private static final String RUBICON_FS_BLOCKLIST_ID = "InMobiFS";

    private static final String LATLON = "LATLON";
    private static final String BSSID_DERIVED = "BSSID_DERIVED";
    private static final String VISIBLE_BSSID = "VISIBLE_BSSID";
    private static final String CCID = "CCID";
    private static final String WIFI = "WIFI";
    private static final String DERIVED_LAT_LON = "DERIVED_LAT_LON";
    private static final String CELL_TOWER = "CELL_TOWER";
    private static final String MIME_HTML = "text/html";
    private static final int INMOBI_SDK_VERSION_370 = 370;
    private static final int IX_MRAID_VALUE = 1001;
    private static final List<Integer> MRAID_FRAMEWORK_VALUES = Lists.newArrayList(API_FRAMEWORKS.MRAID_2.getValue(),
            IX_MRAID_VALUE);
    private static final String BLIND_BUNDLE_APP_FORMAT = "com.ix.%s";
    private static final String BLIND_DOMAIN_SITE_FORMAT = "http://www.ix.com/%s";
    private static final short AGE_LIMIT_FOR_COPPA = 8;

    // The following section is related to VIDEO.
    private static final Integer VIDEO_MIN_DURATION = 0;
    private static final Integer VIDEO_MAX_DURATION = 30;
    private static final Integer VIDEO_MAX_BITRATE = 2000;
    private static final List<Integer> VIDEO_PROTOCOLS = Arrays.asList(5); // VAST 2.0 Wrapper
    private static final List<String> VIDEO_MIMES = Arrays.asList("video/mp4"); // Supported video mimes
    private static final List<Short> VIDEO_SUPPORTED_SLOT_IDS = Arrays.asList((short) 14, (short) 32);

    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;

    private static final String NATIVE_STRING = "native";

    @Getter
    @Setter
    IXBidRequest bidRequest;
    @Getter
    @Setter
    private String urlArg;
    @Getter
    @Setter
    private String ixMethod;
    @Getter
    @Setter
    private String callbackUrl;
    @Setter
    private double bidPriceInUsd;
    @Setter
    private double bidPriceInLocal;
    @Getter
    @Setter
    IXBidResponse bidResponse;
    private final String userName;
    private final String password;
    private final Integer accountId;
    private final boolean wnRequired;
    // private int tmax = 200;
    private boolean templateWN = true;
    protected boolean isSproutSupported = false;

    private final String advertiserId;
    private final String advertiserName;
    private double secondBidPriceInUsd = 0;
    private double secondBidPriceInLocal = 0;
    private String bidRequestJson = "";
    private String encryptedBid;
    private String responseSeatId;
    private String responseImpressionId;
    private String responseAuctionId;
    private String dealId;
    private Double dealFloor;
    private Double dataVendorCost;
    private List<String> packageIds;
    private Double adjustbid;
    private Integer pmptier;
    private String aqid;
    protected boolean isCoppaSet = false;
    private String sampleImageUrl;
    private List<String> advertiserDomains;
    private List<Integer> creativeAttributes;
    private boolean logCreative = false;
    private String adm;
    @Getter
    private int impressionObjCount;
    @Getter
    private int responseBidObjCount;
    @Getter
    private boolean isExternalPersonaDeal;
    private Set<Integer> usedCsIds;
    private List<String> iabCategories;


    private WapSiteUACEntity wapSiteUACEntity;
    private boolean isWapSiteUACEntity = false;
    private final List<String> globalBlindFromConfig;

    private ChannelSegmentEntity dspChannelSegmentEntity;


    @Override
    protected AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClientProvider.getRtbAsyncHttpClient();
    }


    @SuppressWarnings("unchecked")
    public IXAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String host,
            final String advertiserName, final int tmax, final boolean templateWinNotification) {
        super(baseRequestHandler, serverChannel);
        advertiserId = config.getString(advertiserName + ".advertiserId");
        urlArg = config.getString(advertiserName + ".urlArg");
        callbackUrl = config.getString(advertiserName + ".wnUrlback");
        ixMethod = config.getString(advertiserName + ".ixMethod");
        wnRequired = config.getBoolean(advertiserName + ".isWnRequired");
        this.clientBootstrap = clientBootstrap;
        this.host = host;
        setIxPartner(true);
        this.advertiserName = advertiserName;
        // this.tmax = tmax;
        templateWN = templateWinNotification;
        isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", true);
        isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", false);
        userName = config.getString(advertiserName + ".userName");
        password = config.getString(advertiserName + ".password");
        accountId = config.getInt(advertiserName + ".accountId");
        globalBlindFromConfig = config.getList(advertiserName + ".globalBlind");
    }


    @Override
    protected boolean configureParameters() {

        LOG.debug(traceMarker, "inside configureParameters of IX");

        if (!checkIfBasicParamsAvailable()) {
            LOG.info(traceMarker, "Configure parameters inside IX returned false {}: BasicParams Not Available",
                    advertiserName);
            return false;
        }

        if (sasParams.getWapSiteUACEntity() != null) {
            wapSiteUACEntity = sasParams.getWapSiteUACEntity();
            isWapSiteUACEntity = true;
        }
        isVideoRequest = isRequestQualifiedForVideo();

        //If UAC is set try fetching IAB Categories based on it, set iabCategories before creating app/site
        if (isWapSiteUACEntity) {
            iabCategories = IABCategoriesMap.getIABCategoriesFromUAC(wapSiteUACEntity.getCategories());
        }
        // Still if iabCategories is not set try to set it from sasParams
        if (iabCategories == null || iabCategories.isEmpty()) {
            iabCategories = IABCategoriesMap.getIABCategories(sasParams.getCategories());
        }

        // Creating site/app Object
        App app = null;
        Site site = null;
        if (null != sasParams.getSource() && null != sasParams.getSiteId()) {
            if ("WAP".equalsIgnoreCase(sasParams.getSource())) {
                // Creating Site object
                site = createSiteObject();
            } else {
                // Creating App object
                app = createAppObject();
            }
        }
        // Creating Regs Object
        final Regs regs = createRegsObject();
        // Creating Geo Object for device Object
        final Geo geo = createGeoObject();

        // Creating Banner OR Video object.
        final Video video;
        final Banner banner;
        if (isVideoRequest) {
            video = createVideoObject();
            banner = null;
        } else {
            banner = createBannerObject();
            video = null;
        }

        // Creating Device Object
        final Device device = createDeviceObject(geo);
        // Creating User Object
        final User user = createUserObject();
        // Creating Impression Object
        final List<Impression> impresssionlist = new ArrayList<Impression>();
        String displayManager = null;
        String displayManagerVersion = null;
        if (null != sasParams.getSdkVersion()) {
            displayManager = DISPLAY_MANAGER_INMOBI_SDK;
            displayManagerVersion = sasParams.getSdkVersion();
        } else if (null != sasParams.getAdcode() && "JS".equalsIgnoreCase(sasParams.getAdcode())) {
            displayManager = DISPLAY_MANAGER_INMOBI_JS;
        }
        final ProxyDemand proxyDemand = createProxyDemandObject();

        // Only 1 impression object is being generated.
        final Impression impression =
                createImpressionObject(banner, video, displayManager, displayManagerVersion, proxyDemand);
        if (null == impression) {
            LOG.info(traceMarker, "Configure parameters inside IX returned false {}: Impression Obj is null",
                    advertiserName);
            return false;
        }
        impresssionlist.add(impression);
        impressionObjCount = impresssionlist.size();

        // Creating BidRequest Object using unique auction id per auction
        bidRequest = createBidRequestObject(impresssionlist, site, app, user, device, regs);

        if (null == bidRequest) {
            LOG.info(traceMarker, "Configure parameters inside IX returned false {}: Failed inside createBidRequest",
                    advertiserName);
            return false;
        }

        if (isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            InspectorStats.incrementStatCount(InspectorStrings.IX_SENT_AS_TRANSPARENT);
        } else {
            InspectorStats.incrementStatCount(InspectorStrings.IX_SENT_AS_BLIND);
        }

        // Serializing the bidRequest Object
        bidRequestJson = serializeBidRequest();
        if (null == bidRequestJson) {
            return false;
        }
        return true;
    }

    private boolean checkIfBasicParamsAvailable() {

        if (null == casInternalRequestParameters || null == sasParams) {
            LOG.debug("casInternalRequestParams or sasParams cannot be null");
            return false;
        }
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId) || !isRequestFormatSupported()) {
            LOG.debug(
                    traceMarker,
                    "mandate parameters missing or request format is not compatible to partner supported response for dummy so exiting adapter");
            return false;
        }
        return true;
    }


    private boolean isRequestFormatSupported() {
        if (isNativeRequest()) {
            return isNativeResponseSupported;
        } else if (!isNativeRequest()) {
            return isHTMLResponseSupported;
        }

        return false;
    }


    private IXBidRequest createBidRequestObject(final List<Impression> impresssionlist, final Site site, final App app,
            final User user, final Device device, final Regs regs) {
        final IXBidRequest tempBidRequest = new IXBidRequest(impresssionlist);

        tempBidRequest.setId(casInternalRequestParameters.getAuctionId());
        // Disabling it for now, later this will be removed completely
        // tempBidRequest.setTmax(tmax);

        LOG.debug(traceMarker, "INSIDE CREATE BID REQUEST OBJECT");

        if (site != null) {
            tempBidRequest.setSite(site);
        } else if (app != null) {
            tempBidRequest.setApp(app);
        } else {
            LOG.debug(traceMarker, "App and Site both object can not be null so returning");
            return null;
        }

        tempBidRequest.setDevice(device);
        tempBidRequest.setUser(user);
        tempBidRequest.setRegs(regs);
        return tempBidRequest;
    }

    private String serializeBidRequest() {
        final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        String tempBidRequestJson;
        try {
            tempBidRequestJson = serializer.toString(bidRequest);
            // TODO: Unnecessary check for native in IX
            if (isNativeRequest()) {
                tempBidRequestJson = tempBidRequestJson.replaceFirst("nativeObject", "native");
            }
            LOG.info(traceMarker, "IX request json is: {}", tempBidRequestJson);
        } catch (final TException e) {
            LOG.debug(traceMarker, "Could not create json from bidRequest for partner {}", advertiserName);
            LOG.info(traceMarker, "Configure parameters inside IX returned false {} , exception thrown {}",
                    advertiserName, e);
            return null;
        }
        return tempBidRequestJson;
    }


    private Regs createRegsObject() {
        final Regs regs = new Regs();
        if (isWapSiteUACEntity) {
            if (wapSiteUACEntity.isCoppaEnabled()
                    || (sasParams.getAge() != null && sasParams.getAge() <= AGE_LIMIT_FOR_COPPA)) {
                regs.setCoppa(1);
                isCoppaSet = true;
            } else {
                regs.setCoppa(0);
            }
        }
        return regs;
    }


    private ProxyDemand createProxyDemandObject() {
        final ProxyDemand proxyDemand = new ProxyDemand();
        proxyDemand
                .setMarketrate(Math.max(sasParams.getMarketRate(), casInternalRequestParameters.getAuctionBidFloor()));
        return proxyDemand;
    }

    private Impression createImpressionObject(final Banner banner, final Video video, final String displayManager,
            final String displayManagerVersion, final ProxyDemand proxyDemand) {
        Impression impression;
        if (null != casInternalRequestParameters.getImpressionId()) {
            /**
             * In order to conform to the rubicon spec, we are passing a unique integer identifier whose value starts
             * with 1, and increments up to n for n impressions.
             */
            impression = new Impression("1");
        } else {
            LOG.info(traceMarker, "Impression id can not be null in Cas Internal Request Params");
            return null;
        }
        if (!isNativeRequest()) {
            // Only BANNER or VIDEO object can be set, not both.
            if (isVideoRequest) {
                impression.setVideo(video);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VIDEO_REQUESTS);
            } else {
                impression.setBanner(banner);
            }
        }
        impression.setProxydemand(proxyDemand);
        // Set interstitial or not
        if ((null != sasParams.getRqAdType() && "int".equalsIgnoreCase(sasParams.getRqAdType())) || video != null) {
            impression.setInstl(1);
        } else {
            impression.setInstl(0);
        }
        impression.setBidfloor(casInternalRequestParameters.getAuctionBidFloor());
        LOG.debug(traceMarker, "Bid floor is {}", impression.getBidfloor());
        final CommonExtension impExt = new CommonExtension();
        final JSONObject additionalParams = entity.getAdditionalParams();
        if (null != additionalParams) {
            final String zoneId = getZoneId(additionalParams);
            final RubiconExtension rp = new RubiconExtension();
            if (null != zoneId) {
                rp.setZone_id(zoneId);
                impExt.setRp(rp);
            } else {
                LOG.debug(traceMarker, "zone id not present, will say false");
                InspectorStats.incrementStatCount(InspectorStrings.IX_ZONE_ID_NOT_PRESENT);
                // zoneID not available so returning NULL
                return null;
            }
        }

        /*
         * Find matching packages for Banner (Non-video) Requests.
         * NOTE: Packages are not yet supported for video requests.
         */
        if (!isVideoRequest) {
            final long startTime = System.currentTimeMillis();
            packageIds = IXPackageMatcher.findMatchingPackageIds(sasParams, repositoryHelper, selectedSlotId);
            final long endTime = System.currentTimeMillis();
            InspectorStats.updateYammerTimerStats(DemandSourceType.findByValue(sasParams.getDst()).name(),
                    InspectorStrings.IX_PACKAGE_MATCH_LATENCY, endTime - startTime);

            if (!packageIds.isEmpty()) {
                LOG.debug("No. of matching deal packages - {}", packageIds.size());
                final RubiconExtension rp = impExt.getRp() == null ? new RubiconExtension() : impExt.getRp();
                final ExtRubiconTarget target = new ExtRubiconTarget();
                target.packages = packageIds;
                impExt.setRp(rp.setTarget(target));

                // Update the stats
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_DEAL_REQUESTS);
            }
        }
        impression.setExt(impExt);
        return impression;
    }

    public String getZoneId(final JSONObject additionalParams) {
        String categoryZoneId = null;
        boolean isCategorySet = false;

        try {
            if (sasParams.getCategories() != null) {
                for (int index = 0; index < sasParams.getCategories().size(); index++) {
                    final String categoryIdKey = sasParams.getCategories().get(index).toString();
                    if (additionalParams.has(categoryIdKey)) {
                        categoryZoneId = additionalParams.getString(categoryIdKey);
                        LOG.debug(traceMarker, "category Id is {}", categoryZoneId);
                    }
                    if (categoryZoneId != null) {
                        isCategorySet = true;
                        break;
                    }
                }
            }
            if (!isCategorySet && additionalParams.has("default")) {
                categoryZoneId = additionalParams.getString("default");
            }

        } catch (final JSONException exception) {
            LOG.info("Unable to get zone_id for Rubicon, exception thrown {}", exception);
        }
        return categoryZoneId;
    }


    private Banner createBannerObject() {
        final Banner banner = new Banner();
        // Presently only one banner object per impression object is being sent
        // When multiple banner objects will be supported,banner ids will begin at 1 and end at n for n banner objects
        banner.setId("1");
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            banner.setW((int) dim.getWidth());
            banner.setH((int) dim.getHeight());
        }

        if (StringUtils.isNotBlank(sasParams.getSdkVersion())) {
            final int sdkVersion = Integer.parseInt(sasParams.getSdkVersion().substring(1));

            if (sdkVersion >= INMOBI_SDK_VERSION_370) {
                banner.setApi(MRAID_FRAMEWORK_VALUES);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_RICH_MEDIA_REQUESTS);
                isSproutSupported = true;
            }
        }

        final CommonExtension ext = new CommonExtension();
        if (null != selectedSlotId) {
            if (SlotSizeMapping.isIXSupportedSlot(selectedSlotId)) {
                final RubiconExtension rp = new RubiconExtension();
                rp.setMime(MIME_HTML);
                rp.setSize_id(SlotSizeMapping.getIXMappedSlotId(selectedSlotId));
                ext.setRp(rp);
            }
        }
        banner.setExt(ext);
        return banner;
    }

    private Video createVideoObject() {
        boolean soundOn = false;
        boolean isSkippable = true;
        try {
            JSONObject siteVideoPreferencesJson = new JSONObject(sasParams.getPubControlPreferencesJson());
            soundOn = siteVideoPreferencesJson.getJSONObject("video").getBoolean("soundOn");
            isSkippable = siteVideoPreferencesJson.getJSONObject("video").getBoolean("skippable");
        } catch (Exception e) {
            LOG.error("Caught Exception while fetching skippable/soundOn settings for site {}.", sasParams.getSiteId());
            InspectorStats.incrementStatCount(getName(), InspectorStrings.INVALID_MEDIA_PREFERENCES_JSON);
        }

        Video video = new Video();

        List<Integer> playbackMethod = new ArrayList<>(1);
        playbackMethod.add(soundOn ? 1 : 2);
        video.setPlaybackmethod(playbackMethod);

        video.setBoxingallowed(0); // Always set to false
        video.setMinduration(VIDEO_MIN_DURATION);
        video.setMaxduration(VIDEO_MAX_DURATION);
        video.setProtocols(VIDEO_PROTOCOLS);
        video.setMimes(VIDEO_MIMES);
        video.setMaxbitrate(VIDEO_MAX_BITRATE);

        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            video.setW((int) dim.getWidth());
            video.setH((int) dim.getHeight());
        }

        video.ext = new VideoExtension();
        video.ext.setSkip(isSkippable ? 1 : 0);
        video.ext.setSkipdelay(5); // Default 5 secs.

        return video;
    }

    /**
     * Determines whether this request is selected to serve VIDEO Ad. Video serving capability is determined based on
     * the following: 1) The VIDEO serving prerequisites (OS, sdk version and slot) are met. 2) This site support Video
     * as per the SiteControls repository. 3) Honor the video traffic % as defined in IXVideoTraffic Repository.
     */
    private boolean isRequestQualifiedForVideo() {
        // Check the Video support prerequisites.
        if (!sasParams.isVideoSupported() || !VIDEO_SUPPORTED_SLOT_IDS.contains(selectedSlotId)) {
            return false;
        }

        boolean isQualifiedForVideo = false;
        List<AdTypeEnum> supportedAdTypes = sasParams.getPubControlSupportedAdTypes();
        if (null != supportedAdTypes && supportedAdTypes.contains(AdTypeEnum.VIDEO)) {
            // If this site supports only Video, qualify this request for video.
            // No need to further look up in the VideoTraffic Repository.
            if (supportedAdTypes.size() == 1) {
                isQualifiedForVideo = true;
            } else {
                int videoTrafficPercentage =
                        repositoryHelper.queryIXVideoTrafficEntity(sasParams.getSiteId(), sasParams.getCountryId()
                                .intValue());
                // Based on the traffic percentage, determine whether this request should be selected for VIDEO or not.
                if (videoTrafficPercentage > ThreadLocalRandom.current().nextInt(0, 100)) {
                    isQualifiedForVideo = true;
                }
            }
        }
        return isQualifiedForVideo;
    }

    private Geo createGeoObject() {
        final Geo geo = new Geo();
        // If Coppa is not set, only then send latLong
        if (!isCoppaSet && StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            geo.setLat(Double.parseDouble(String.format("%.4f", Double.parseDouble(latlong[0]))));
            geo.setLon(Double.parseDouble(String.format("%.4f", Double.parseDouble(latlong[1]))));
        }

        if (LATLON.equals(sasParams.getLocSrc()) || BSSID_DERIVED.equals(sasParams.getLocSrc())
                || VISIBLE_BSSID.equals(sasParams.getLocSrc())) {
            geo.setType(1);
        } else if (CCID.equals(sasParams.getLocSrc()) || WIFI.equals(sasParams.getLocSrc())
                || DERIVED_LAT_LON.equals(sasParams.getLocSrc()) || CELL_TOWER.equals(sasParams.getLocSrc())) {
            geo.setType(2);
        }

        if (null != sasParams.getCountryCode()) {
            geo.setCountry(IABCountriesMap.getIabCountry(sasParams.getCountryCode()));
        }

        geo.setZip(casInternalRequestParameters.getZipCode());

        return geo;
    }

    /**
     * Function used to populate the Geo object for Sprout Macro Replacement
     * 
     * @return
     */
    private Geo createSproutGeoObject() {
        final Geo geo = new Geo();

        if (!isCoppaSet) {
            try {
                if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                        && 1 == StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",")) {
                    final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
                    geo.setLat(Double.parseDouble(latlong[0]));
                    geo.setLon(Double.parseDouble(latlong[1]));
                }
            } catch (NumberFormatException nfe) {
                // Not possible as type is already checked during deserialisation of AdPoolRequest
            }
            String countryCode = sasParams.getCountryCode();
            if (null != countryCode) {
                geo.setCountry(countryCode);
            }
            String zipCode = casInternalRequestParameters.getZipCode();
            if (null != zipCode) {
                geo.setZip(zipCode);
            }
        }

        return geo;
    }


    private User createUserObject() {
        final User user = new User();

        user.setId(getUid());

        return user;
    }


    private Site createSiteObject() {
        final Site site = new Site();

        final JSONObject additionalParams = entity.getAdditionalParams();
        Integer rubiconSiteId;
        try {
            rubiconSiteId = Integer.parseInt(additionalParams.getString("site"));
        } catch (final JSONException e) {
            LOG.debug(traceMarker, "Site Id is not configured, exception thrown {}", e);
            InspectorStats.incrementStatCount(InspectorStrings.IX_SITE_ID_NOT_PRESENT);
            return null;
        }

        final CommonExtension ext = new CommonExtension();

        if (isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            setParamsForTransparentSite(site, ext);
        } else {
            setParamsForBlindSite(site, ext);
        }

        final List<String> blockedList = getBlockedList();
        site.setBlocklists(blockedList);

        final Publisher publisher = createPublisher();
        site.setPublisher(publisher);

        final AdQuality adQuality = createAdQuality();
        site.setAq(adQuality);

        final Transparency transparency = createTransparency();
        site.setTransparency(transparency);

        final RubiconExtension rpForSite = new RubiconExtension();
        rpForSite.setSite_id(rubiconSiteId);
        ext.setRp(rpForSite);

        site.setExt(ext);
        return site;
    }

    private void setParamsForTransparentSite(final Site site, final CommonExtension ext) {
        final String blindId = getBlindedSiteId(sasParams.getSiteIncId());
        site.setId(sasParams.getSiteId());
        final String tempSiteUrl = wapSiteUACEntity.getSiteUrl();
        if (StringUtils.isNotEmpty(tempSiteUrl)) {
            site.setPage(tempSiteUrl);
            site.setDomain(tempSiteUrl);
        }
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
            site.setName(wapSiteUACEntity.getSiteName());
        }
        final String blindDomain = String.format(BLIND_DOMAIN_SITE_FORMAT, blindId);
        final Blind blindForSite = new Blind();
        blindForSite.setPage(blindDomain);
        blindForSite.setDomain(blindDomain);
        ext.setBlind(blindForSite);
    }

    private void setParamsForBlindSite(final Site site, final CommonExtension ext) {
        final String blindId = getBlindedSiteId(sasParams.getSiteIncId());
        site.setId(sasParams.getSiteId());
        final String category =
                isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType()) ? wapSiteUACEntity
                        .getAppType() : getCategories(',', false);
        site.setName(category);
        final String blindDomain = String.format(BLIND_DOMAIN_SITE_FORMAT, blindId);
        site.setPage(blindDomain);
        site.setDomain(blindDomain);
        final Blind blindForSite = new Blind();
        blindForSite.setPage(blindDomain);
        blindForSite.setDomain(blindDomain);
        ext.setBlind(blindForSite);
    }

    private Publisher createPublisher() {
        final Publisher publisher = new Publisher();
        publisher.setCat(iabCategories);

        final CommonExtension publisherExtensions = new CommonExtension();
        final RubiconExtension rpForPub = new RubiconExtension();
        rpForPub.setAccount_id(accountId);
        publisherExtensions.setRp(rpForPub);
        publisher.setExt(publisherExtensions);
        return publisher;
    }


    private AdQuality createAdQuality() {
        final AdQuality adQuality = new AdQuality();
        if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
            adQuality.setSensitivity("low");
        } else {
            adQuality.setSensitivity("high");
        }
        return adQuality;
    }


    private Transparency createTransparency() {
        final Transparency transparency = new Transparency();
        if (isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            transparency.setBlind(0);
            if (null != wapSiteUACEntity.getBlindList()) {
                transparency.setBlindbuyers(wapSiteUACEntity.getBlindList());
            } else if (!globalBlindFromConfig.isEmpty() && !globalBlindFromConfig.get(0).isEmpty()) {
                final List<Integer> globalBlind = Lists.newArrayList();
                for (final String s : globalBlindFromConfig) {
                    globalBlind.add(Integer.valueOf(s));
                }
                transparency.setBlindbuyers(globalBlind);
            }
        } else {
            transparency.setBlind(1);
        }
        return transparency;
    }


    private App createAppObject() {
        final App app = new App();
        final JSONObject additionalParams = entity.getAdditionalParams();
        Integer rubiconSiteId;
        try {
            rubiconSiteId = Integer.parseInt(additionalParams.getString("site"));
        } catch (final JSONException e) {
            LOG.debug(traceMarker, "Site Id is not configured, exception thrown {}", e);
            InspectorStats.incrementStatCount(InspectorStrings.IX_SITE_ID_NOT_PRESENT);
            // add trace here
            return null;
        }

        final CommonExtension ext = new CommonExtension();
        if (isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            setParamsForTransparentApp(app, ext);
        } else {
            setParamsForBlindApp(app, ext);
        }
        app.setCat(iabCategories);

        final List<String> blockedList = getBlockedList();
        app.setBlocklists(blockedList);

        final Publisher publisher = createPublisher();
        app.setPublisher(publisher);

        final AdQuality adQuality = createAdQuality();
        app.setAq(adQuality);

        final Transparency transparency = createTransparency();
        app.setTransparency(transparency);

        final RubiconExtension rpForApp = new RubiconExtension();
        rpForApp.setSite_id(rubiconSiteId);
        ext.setRp(rpForApp);

        app.setExt(ext);

        return app;
    }

    private void setParamsForTransparentApp(final App app, final CommonExtension ext) {
        final String blindId = getBlindedSiteId(sasParams.getSiteIncId());
        app.setId(sasParams.getSiteId());
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteUrl())) {
            app.setStoreurl(wapSiteUACEntity.getSiteUrl());
        }
        String bundleId = wapSiteUACEntity.getBundleId();
        if (StringUtils.isEmpty(bundleId) && wapSiteUACEntity.isAndroid()) {
            bundleId = wapSiteUACEntity.getMarketId();
        }
        if (StringUtils.isNotEmpty(bundleId)) {
            app.setBundle(bundleId);
        }

        // Set either of title or Name, giving priority to title
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getAppTitle())) {
            app.setName(wapSiteUACEntity.getAppTitle());
        } else if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
            app.setName(wapSiteUACEntity.getSiteName());
        }

        final String blindBundle = String.format(BLIND_BUNDLE_APP_FORMAT, blindId);
        final Blind blindForApp = new Blind();
        blindForApp.setBundle(blindBundle);
        ext.setBlind(blindForApp);
    }

    private void setParamsForBlindApp(final App app, final CommonExtension ext) {
        final String blindId = getBlindedSiteId(sasParams.getSiteIncId());
        app.setId(sasParams.getSiteId());
        final String category =
                isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType()) ? wapSiteUACEntity
                        .getAppType() : getCategories(',', false);
        app.setName(category);
        final String blindBundle = String.format(BLIND_BUNDLE_APP_FORMAT, blindId);
        app.setBundle(blindBundle);
        final Blind blindForApp = new Blind();
        blindForApp.setBundle(blindBundle);
        ext.setBlind(blindForApp);
    }

    public List<String> getBlockedList() {
        final List<String> blockedList = Lists.newArrayList();
        LOG.debug(traceMarker, "{}", sasParams.getSiteIncId());
        blockedList.add(String.format(SITE_BLOCKLIST_FORMAT, sasParams.getSiteIncId()));
        if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
            blockedList.add(RUBICON_PERF_BLOCKLIST_ID);
        } else {
            blockedList.add(RUBICON_FS_BLOCKLIST_ID);
        }
        return blockedList;
    }


    private Device createDeviceObject(final Geo geo) {
        final Device device = new Device();
        device.setIp(sasParams.getRemoteHostIp());
        device.setUa(sasParams.getUserAgent());
        device.setGeo(geo);
        final Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            device.setOs(HandSetOS.values()[sasParamsOsId - 1].toString());
        }

        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            device.setOsv(sasParams.getOsMajorVersion());
        }

        // TODO Add enums in thrift: 0:UNKNOWN,2:WIFI
        if (com.inmobi.adserve.adpool.NetworkType.WIFI == sasParams.getNetworkType()) {
            device.setConnectiontype(2);
        } else {
            device.setConnectiontype(0);
        }

        // Setting do not track
        if (null != casInternalRequestParameters.getUidADT()) {
            try {
                device.setLmt(Integer.parseInt(casInternalRequestParameters.getUidADT()) == 0 ? 1 : 0);
            } catch (final NumberFormatException e) {
                LOG.debug(traceMarker, "Exception while parsing uidADT to integer {}", e);
            }
        }
        // Setting platform id sha1 hashed
        if (null != casInternalRequestParameters.getUidSO1()) {
            device.setDidsha1(casInternalRequestParameters.getUidSO1());
            device.setDpidsha1(casInternalRequestParameters.getUidSO1());
        } else if (null != casInternalRequestParameters.getUidO1()) {
            device.setDidsha1(casInternalRequestParameters.getUidO1());
            device.setDpidsha1(casInternalRequestParameters.getUidO1());
        }

        // Setting platform id md5 hashed
        if (null != casInternalRequestParameters.getUidMd5()) {
            device.setDidmd5(casInternalRequestParameters.getUidMd5());
            device.setDpidmd5(casInternalRequestParameters.getUidMd5());
        } else if (null != casInternalRequestParameters.getUid()) {
            device.setDidmd5(casInternalRequestParameters.getUid());
            device.setDpidmd5(casInternalRequestParameters.getUid());
        }

        // Setting Extension for ifa
        // if Coppa is not set, only then set IFA
        String id;
        if (!isCoppaSet) {
            if (!StringUtils.isEmpty(id = casInternalRequestParameters.getUidIFA())) {
                // Set to UIDIFA for IOS Device
                device.setIfa(id);
            } else if (!StringUtils.isEmpty(id = getGPID())) {
                // Set to GPID for Android Device
                device.setIfa(id);
            }
        }

        final CommonExtension ext = new CommonExtension();

        final RubiconExtension rpForDevice = new RubiconExtension();
        rpForDevice.setXff(sasParams.getRemoteHostIp());
        ext.setRp(rpForDevice);

        device.setExt(ext);

        return device;
    }


    public String replaceIXMacros(String url) {
        url = url.replaceAll(RTBCallbackMacros.AUCTION_ID_INSENSITIVE, bidResponse.id);
        url = url.replaceAll(RTBCallbackMacros.AUCTION_CURRENCY_INSENSITIVE, USD);
        if (6 != sasParams.getDst()) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_ENCRYPTED_INSENSITIVE, encryptedBid);
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_INSENSITIVE, Double.toString(secondBidPriceInLocal));
        }
        if (null != bidResponse.bidid) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_BID_ID_INSENSITIVE, bidResponse.bidid);
        }
        if (null != bidResponse.getSeatbid().get(0).getSeat()) {
            url =
                    url.replaceAll(RTBCallbackMacros.AUCTION_SEAT_ID_INSENSITIVE, bidResponse.getSeatbid().get(0)
                            .getSeat());
        }
        if (isExternalPersonaDeal) {
            url = url.replaceAll(RTBCallbackMacros.DEAL_ID_INSENSITIVE, "&d-id=" + dealId);
        } else {
            url = url.replaceAll(RTBCallbackMacros.DEAL_ID_INSENSITIVE, "");
        }

        if (null == bidRequest) {
            LOG.info(traceMarker, "bidrequest is null");
            return url;
        }
        url = url.replaceAll(RTBCallbackMacros.AUCTION_IMP_ID_INSENSITIVE, bidRequest.getImp().get(0).getId());

        LOG.debug(traceMarker, "String after replaceMacros is {}", url);
        return url;
    }


    @Override
    protected RequestBuilder getNingRequestBuilder() throws Exception {
        final byte[] body = bidRequestJson.getBytes(CharsetUtil.UTF_8);

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        String httpRequestMethod;
        if ("get".equalsIgnoreCase(ixMethod)) {
            httpRequestMethod = "GET";
        } else {
            httpRequestMethod = "POST";
        }

        final String authStr = userName + ":" + password;
        final String authEncoded = new String(Base64.encodeBase64(authStr.getBytes(CharsetUtil.UTF_8)));
        LOG.debug(traceMarker, "INSIDE GET NING REQUEST");

        return new RequestBuilder(httpRequestMethod).setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(body)
                .setHeader("Authorization", "Basic " + authEncoded).setHeader(HttpHeaders.Names.HOST, uri.getHost());
    }


    @Override
    public URI getRequestUri() throws URISyntaxException {
        final StringBuilder url = new StringBuilder();
        if ("get".equalsIgnoreCase(ixMethod)) {
            url.append(host).append('?').append(urlArg).append('=');
        } else {
            url.append(host);
        }
        LOG.debug(traceMarker, "{} url is {}", getName(), url.toString());
        return URI.create(url.toString());
    }


    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        adStatus = "NO_AD";
        LOG.info(traceMarker, "response is {}", response);
        if (status.code() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        } else {
            statusCode = status.code();
            final boolean parsedResponse = deserializeResponse(response);
            if (!parsedResponse) {
                // Incase of unexpected error in deserializeResponse(), the adStatus is set to TERM.
                // Otherwise, it is already set as NO_AD.
                responseContent = "";
                statusCode = 500;
                LOG.info(traceMarker, "Could not get an Ad by parsing ix response. Ad status: {}", adStatus);
                return;
            }
            adStatus = "AD";

            if (isNativeRequest()) {
                // TODO add nativeAdBuilding
                LOG.debug(traceMarker, "we do not support native request");
            } else if (isVideoRequest) {
                videoAdBuilding();
            } else {
                bannerAdBuilding();
            }

        }
        LOG.debug(traceMarker, "response length is {}", responseContent.length());
        LOG.debug(traceMarker, "response is {}", responseContent);
    }

    private String replaceSproutMacros(String adm) {
        if (null == adm) {
            return null;
        }

        InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VALID_SPROUT_RESPONSES);

        List<String> macros = new ArrayList<>(8);
        List<String> substitutions = new ArrayList<>(8);

        macros.add(JS_ESC_BEACON_URL);
        substitutions.add(StringEscapeUtils.escapeJavaScript(beaconUrl + "?b=${WIN_BID}${DEAL_GET_PARAM}"));

        macros.add(JS_ESC_CLICK_URL);
        substitutions.add(StringEscapeUtils.escapeJavaScript(clickUrl));

        macros.add(SDK_VERSION_ID);
        String sdkVersion = sasParams.getSdkVersion();
        substitutions.add(null != sdkVersion ? sdkVersion : "");

        // default value for replacement of macros is an empty string
        Geo geo = createSproutGeoObject();
        String lat = geo.isSetLat() ? String.valueOf(geo.getLat()) : "";
        String lng = geo.isSetLon() ? String.valueOf(geo.getLon()) : "";
        String zip = geo.isSetZip() ? geo.getZip() : "";
        String cc = geo.isSetCountry() ? geo.getCountry() : "";

        macros.add(GEO_LAT);
        substitutions.add(lat);

        macros.add(GEO_LNG);
        substitutions.add(lng);

        macros.add(GEO_ZIP);
        substitutions.add(zip);

        macros.add(GEO_CC);
        substitutions.add(cc);

        macros.add(JS_ESC_GEO_CITY);
        substitutions.add(""); // JS_ESC_GEO_CITY is not currently being set

        macros.add(RECORD_EVENT_FUN);
        substitutions.add(""); // No function is being provided

        macros.add(OPEN_LP_FUN);
        substitutions.add(""); // No function is being provided

        String[] macroArray = macros.toArray(new String[macros.size()]);
        String[] substitutionsArray = substitutions.toArray((new String[substitutions.size()]));
        return StringUtils.replaceEach(adm, macroArray, substitutionsArray);
    }

    protected boolean isSproutAd() {
        String adm = getAdMarkUp();
        if (null == adm) {
            return false;
        } else {
            return adm.contains(SPROUT_UNIQUE_STRING);
        }
    }

    public boolean updateDSPAccountInfo(final String buyer) {
        LOG.debug(traceMarker, "Inside updateDSPAccountInfo");

        Long buyerId;
        try {
            buyerId = Long.parseLong(buyer);
        } catch (final NumberFormatException e) {
            LOG.debug("NumberFormatException: Invalid DSP Buyer ID Format");
            return false;
        }

        // Get Inmobi account id for the DSP on Rubicon side
        final IXAccountMapEntity ixAccountMapEntity = repositoryHelper.queryIXAccountMapRepository(buyerId);
        if (null == ixAccountMapEntity) {
            LOG.error("Invalid Rubicon DSP id: {}", buyer);
            return false;
        }
        final String dspAccountId = ixAccountMapEntity.getInmobiAccountId();

        if (StringUtils.isEmpty(dspAccountId)) {
            LOG.error("Inmobi Account ID is null or empty for Rubicon DSP id: {}", buyer);
            return false;
        }

        // Get collection of Channel Segment Entities for the particular Inmobi account id
        final ChannelAdGroupRepository channelAdGroupRepository = repositoryHelper.getChannelAdGroupRepository();
        if (null == channelAdGroupRepository) {
            LOG.debug("Channel AdGroup Repository is null.");
            return false;
        }

        final Collection<ChannelSegmentEntity> adGroupMap = channelAdGroupRepository.getEntities(dspAccountId);

        if (null == adGroupMap || adGroupMap.isEmpty()) {
            // If collection is empty
            LOG.error("Channel Segment Entity collection for Rubicon DSP is empty: DSP id:{}, inmobi account id:{}",
                    buyer, dspAccountId);
            return false;
        } else {
            // Else picking up the first channel segment entity and assuming that to be the correct entity
            dspChannelSegmentEntity = adGroupMap.iterator().next();

            // Create a new ChannelSegment with DSP information. So that, all the logging happens on DSP Id.
            // this.auctionResponse = new ChannelSegment(dspChannelSegmentEntity, null, null, null, null,
            // auctionResponse.getAdNetworkInterface(), -1L);

            // Get response creative type and get the incId for the respective response creative type
            final ADCreativeType responseCreativeType = getCreativeType();
            final long incId = dspChannelSegmentEntity.getIncId(responseCreativeType);

            final String oldImpressionId = getImpressionId();
            LOG.debug(traceMarker, "Old impression id: {}", oldImpressionId);

            // Generating new impression id
            LOG.debug(traceMarker, "Creating new impression id from incId: {}", incId);
            final String newImpressionId = ImpressionIdGenerator.getInstance().getImpressionId(incId);

            if (StringUtils.isNotEmpty(newImpressionId)) {
                // Update beacon and click URLs
                beaconUrl =
                        ClickUrlsRegenerator.regenerateBeaconUrl(beaconUrl, getImpressionId(), newImpressionId,
                                sasParams.isRichMedia());
                clickUrl = ClickUrlsRegenerator.regenerateClickUrl(clickUrl, getImpressionId(), newImpressionId);
                impressionId = newImpressionId;

                LOG.debug(traceMarker, "Replaced impression id to new value {}.", newImpressionId);
            }

            return true;
        }
    }


    @Override
    public void processResponse() {
        LOG.debug(traceMarker, "Inside process Response for the partner: {}", getName());
        if (isRequestComplete) {
            LOG.debug(traceMarker, "Already cleaned up so returning from process response");
            return;
        }
        LOG.debug(traceMarker, "Inside process Response for the partner: {}", getName());
        getResponseAd();
        isRequestComplete = true;
        if (baseRequestHandler.getAuctionEngine().areAllChannelSegmentRequestsComplete()) {
            LOG.debug(traceMarker, "areAllChannelSegmentRequestsComplete is true");
            if (baseRequestHandler.getAuctionEngine().isAuctionComplete()) {
                LOG.debug(traceMarker, "IX Auction has run already");
                if (baseRequestHandler.getAuctionEngine().isAuctionResponseNull()) {
                    LOG.debug(traceMarker, "IX Auction has returned null");
                    // Sending no ad response and cleaning up channel
                    // (processDcpPartner is skipped because the selected adNetworkInterface
                    // will always be the last entry and a No Ad Response will be sent)
                    baseRequestHandler.sendNoAdResponse(serverChannel);
                    baseRequestHandler.cleanUp();
                    return;
                }
                LOG.debug(traceMarker, "IX Auction response is not null so sending auction response");
                return;
            } else {
                final AdNetworkInterface highestBid = baseRequestHandler.getAuctionEngine().runAuctionEngine();
                if (highestBid != null) {
                    // Update Response ChannelSegment
                    baseRequestHandler.getAuctionEngine().updateIXChannelSegment(dspChannelSegmentEntity);

                    LOG.debug(traceMarker, "Sending IX auction response of {}", highestBid.getName());
                    baseRequestHandler.sendAdResponse(highestBid, serverChannel);
                    // highestBid.impressionCallback();
                    LOG.debug(traceMarker, "Sent IX auction response");
                    return;
                } else {
                    LOG.debug(traceMarker, "IX auction has returned null");
                    // Sending no ad response and cleaning up channel
                    // processDcpList is skipped
                    baseRequestHandler.sendNoAdResponse(serverChannel);
                    baseRequestHandler.cleanUp();
                }
            }
        }
        LOG.debug(traceMarker, "IX Auction has not run so waiting....");
    }


    private void bannerAdBuilding() {

        final VelocityContext velocityContext = new VelocityContext();

        String admContent = getAdMarkUp();

        final int admSize = admContent.length();
        if (!templateWN) {
            final String winUrl = beaconUrl + "?b=${WIN_BID}";
            admContent = admContent.replace(RTBCallbackMacros.AUCTION_WIN_URL, winUrl);
        }
        final int admAfterMacroSize = admContent.length();

        if ("wap".equalsIgnoreCase(sasParams.getSource())) {
            if (isSproutAd()) {
                LOG.debug(traceMarker, "Sprout Ads not supported on WAP");
                InspectorStats.incrementStatCount(getName(),
                        InspectorStrings.DROPPED_AS_SPROUT_ADS_ARE_NOT_SUPPORTED_ON_WAP);
                responseContent = "";
                adStatus = "NO_AD";
                return;
            }
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, admContent);
        } else {
            if (isSproutAd()) {
                if (!isSproutSupported) {
                    LOG.debug(traceMarker, "Sprout Ads not supported on SDK < 370");
                    InspectorStats.incrementStatCount(getName(),
                            InspectorStrings.DROPPED_AS_SPROUT_ADS_ARE_NOT_SUPPORTED_ON_SDK370);
                    responseContent = "";
                    adStatus = "NO_AD";
                    return;
                }
                LOG.debug(traceMarker, "Sprout Ad Received");
                admContent = replaceSproutMacros(admContent);
                velocityContext.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, admContent);
                velocityContext.put(VelocityTemplateFieldConstants.SPROUT, true);
                LOG.debug(traceMarker, "Replaced Sprout Macros");
            } else {
                velocityContext.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, MRAID + admContent);
            }
            if (StringUtils.isNotBlank(sasParams.getImaiBaseUrl())) {
                velocityContext.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());
            }
        }
        // Checking whether to send win notification
        LOG.debug(traceMarker, "isWinRequired is {} and winfromconfig is {}", wnRequired, callbackUrl);
        final String winUrl = getWinUrl();
        if (StringUtils.isNotEmpty(winUrl)) {
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, winUrl);
        }

        if (templateWN || admAfterMacroSize == admSize) {
            velocityContext.put(VelocityTemplateFieldConstants.IM_BEACON_URL, beaconUrl);
        }
        try {
            responseContent = Formatter.getResponseFromTemplate(TemplateType.IX_HTML, velocityContext, sasParams, null);
        } catch (final Exception e) {
            adStatus = "NO_AD";
            LOG.info(traceMarker, "Some exception is caught while filling the velocity template for partner: {} {}",
                    advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.BANNER_PARSE_RESPONSE_EXCEPTION);
        }

    }

    private void videoAdBuilding() {
        final VelocityContext velocityContext = new VelocityContext();

        final String vastContentJSEsc = StringEscapeUtils.escapeJavaScript(getAdMarkUp());
        velocityContext.put(VelocityTemplateFieldConstants.VAST_CONTENT_JS_ESC, vastContentJSEsc);

        // JS escaped WinUrl for partner.
        final String partnerWinUrl = getWinUrl();
        if (StringUtils.isNotEmpty(partnerWinUrl)) {
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL,
                    StringEscapeUtils.escapeJavaScript(partnerWinUrl));
        }

        // JS escaped IMWinUrl
        final String imWinUrl = beaconUrl + "?b=${WIN_BID}";
        velocityContext.put(VelocityTemplateFieldConstants.IM_WIN_URL, StringEscapeUtils.escapeJavaScript(imWinUrl));

        // JS escaped IM beacon and click URLs.
        velocityContext
                .put(VelocityTemplateFieldConstants.IM_BEACON_URL, StringEscapeUtils.escapeJavaScript(beaconUrl));
        velocityContext.put(VelocityTemplateFieldConstants.IM_CLICK_URL, StringEscapeUtils.escapeJavaScript(clickUrl));

        // SDK version
        velocityContext.put(VelocityTemplateFieldConstants.IMSDK_VERSION, sasParams.getSdkVersion());

        // Namespace
        velocityContext.put(VelocityTemplateFieldConstants.NAMESPACE, Formatter.getNamespace());

        // IMAIBaseUrl
        velocityContext.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());

        // Sprout related parameters.
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            velocityContext.put(VelocityTemplateFieldConstants.WIDTH, (int) dim.getWidth());
            velocityContext.put(VelocityTemplateFieldConstants.HEIGHT, (int) dim.getHeight());
        }

        String networkType =
                (null != sasParams.getNetworkType()) ? sasParams.getNetworkType().name() : NetworkType.NON_WIFI.name();
        String requestNetworkTypeJson = "{\"networkType\":\"" + networkType + "\"}";
        // Publisher control settings
        velocityContext.put(VelocityTemplateFieldConstants.REQUEST_JSON, requestNetworkTypeJson);
        velocityContext.put(VelocityTemplateFieldConstants.SITE_PREFERENCES_JSON,
                sasParams.getPubControlPreferencesJson());

        try {
            responseContent =
                    Formatter
                            .getResponseFromTemplate(TemplateType.INTERSTITIAL_VIDEO, velocityContext, sasParams, null);
        } catch (final Exception e) {
            adStatus = "NO_AD";
            LOG.info(traceMarker, "Some exception is caught while filling the velocity template for partner:{} {}",
                    advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.VIDEO_PARSE_RESPONSE_EXCEPTION);
        }
    }

    private String getWinUrl() {
        String winUrl = null;
        if (wnRequired) {
            // setCallbackContent();
            // Win notification is required
            String nUrl = null;
            try {
                nUrl = bidResponse.seatbid.get(0).getBid().get(0).getNurl();
            } catch (final Exception e) {
                LOG.debug(traceMarker, "Exception while parsing response {}", e);
            }
            LOG.debug(traceMarker, "nurl is {}", nUrl);
            if (!StringUtils.isEmpty(callbackUrl)) {
                LOG.debug(traceMarker, "inside wn from config");
                winUrl = callbackUrl;
            } else if (!StringUtils.isEmpty(nUrl)) {
                LOG.debug(traceMarker, "inside wn from nurl");
                winUrl = nUrl;
            }
        }
        return winUrl;
    }

    /**
     * Generates blinded site uuid from siteIncId. For a given site Id, the generated blinded SiteId will always be
     * same.
     * <p/>
     * NOTE: RTB uses a different logic where the blinded SiteId is a function of siteIncId+AdGroupIncId.
     */
    private String getBlindedSiteId(final long siteIncId) {
        final byte[] byteArr = ByteBuffer.allocate(8).putLong(siteIncId).array();
        return UUID.nameUUIDFromBytes(byteArr).toString();
    }

    // This function not used, for future use
    @Override
    protected boolean isNativeRequest() {
        return NATIVE_STRING.equals(sasParams.getRFormat()) && "APP".equalsIgnoreCase(sasParams.getSource());
    }


    public boolean deserializeResponse(final String response) {
        final Gson gson = new Gson();
        try {
            bidResponse = gson.fromJson(response, IXBidResponse.class);
            LOG.debug(traceMarker, "Done with parsing of bidresponse");
            if (null == bidResponse || null == bidResponse.getSeatbid() || bidResponse.getSeatbidSize() == 0) {
                LOG.info("BidResponse does not have seat bid object");
                return false;
            }
            final SeatBid seatBid = bidResponse.getSeatbid().get(0);
            if (null == seatBid.getBid() || seatBid.getBidSize() == 0) {
                LOG.info("Seat bid object does not have bid object");
                return false;
            }
            // bidderCurrency is to USD by default
            setBidPriceInLocal(seatBid.getBid().get(0).getPrice());
            setBidPriceInUsd(getBidPriceInLocal());
            responseSeatId = seatBid.getSeat();
            responseBidObjCount = seatBid.getBid().size();
            final Bid bid = seatBid.getBid().get(0);
            adm = bid.getAdm();

            // TODO: hack for IX beacon discrepancy fix
            adm = adm.replace("src=\"//beacon", "src=\"http://beacon");

            responseImpressionId = bid.getImpid();
            responseAuctionId = bidResponse.getId();
            pmptier = bid.getPmptier();
            // estimated = bid.getEstimated(); //Not used currently
            aqid = bid.getAqid();
            adjustbid = bid.getAdjustbid();
            dealId = bid.getDealid();
            isExternalPersonaDeal = false;
            if (dealId != null) {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_DEAL_RESPONSES);
                setFloorVendorUsedCsids();
            }
            // For video requests, validate that a valid XML is received.
            if (isVideoRequest) {
                if (isAdmValidXML()) {
                    InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VIDEO_RESPONSES);
                } else {
                    InspectorStats.incrementStatCount(getName(), InspectorStrings.INVALID_VIDEO_RESPONSE_COUNT);
                    return false;
                }
            }

            final boolean result = updateDSPAccountInfo(seatBid.getBuyer());
            if (!result) {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.INVALID_DSP_ID);
            }
            return result;
        } catch (final Exception e) {
            // Incase of JSON parsing error, increase the respective stat counter.
            if (e instanceof JsonParseException) {
                LOG.error(traceMarker, "Caught JSON parsing exception while parsing the IX response", e);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.JSON_PARSING_ERROR);
            } else {
                LOG.error(traceMarker, "Caught Exception while parsing the ix response.", e);
            }
            // Set AdStatus as TERM incase of unexpected errors in deserializing the response.
            adStatus = "TERM";
            return false;
        }
    }

    /**
     * Validates whether the ADM content is a VALID XML.
     */
    private boolean isAdmValidXML() {
        if (StringUtils.isEmpty(adm)) {
            return false;
        }
        // Validate the XML by parsing it.
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final InputSource source = new InputSource(new StringReader(getAdMarkUp()));
        try {
            final DocumentBuilder db = factory.newDocumentBuilder();
            db.setErrorHandler(null);
            db.parse(source);
            return true;
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOG.debug(traceMarker, "VAST response is NOT a valid XML.", e);
            return false;
        }
    }

    private void setFloorVendorUsedCsids() {
        IXPackageEntity matchedPackageEntity;

        try {
            matchedPackageEntity = repositoryHelper.queryIxPackageByDeal(dealId);
        } catch (NoSuchObjectException exception) {
            LOG.error("Rubicon DealId not stored in ix_package_deals table, {}", dealId);
            return;
        } catch (NonUniqueObjectException exception) {
            LOG.error("Rubicon DealId not unique in ix_package_deals table, {}", dealId);
            return;
        }

        int indexOfDealId = matchedPackageEntity.getDealIds().indexOf(dealId);
        dealFloor =
                matchedPackageEntity.getDealFloors().size() > indexOfDealId ? matchedPackageEntity.getDealFloors().get(
                        indexOfDealId) : 0;
        dataVendorCost = matchedPackageEntity.getDataVendorCost();
        if (dataVendorCost > 0.0) {
            isExternalPersonaDeal = true;

            usedCsIds = new HashSet<Integer>();

            Set<Set<Integer>> csIdInPackages = matchedPackageEntity.getDmpFilterSegmentExpression();
            for (Set<Integer> smallSet : csIdInPackages) {
                for (Integer csIdInSet : smallSet) {
                    if (sasParams.getCsiTags().contains(csIdInSet)) {
                        usedCsIds.add(csIdInSet);
                    }
                }
            }
        }
        return;
    }

    @Override
    public double returnAdjustBid() {
        return adjustbid;
    }


    @Override
    public String returnDealId() {
        return dealId;
    }

    public double returndealFloor() {
        return dealFloor;
    }

    public double returnDataVendorCost() {
        return dataVendorCost;
    }

    public Set<Integer> returnUsedCsids() {
        return usedCsIds;
    }


    @Override
    public Integer returnPmpTier() {
        return pmptier;
    }


    @Override
    public String returnAqid() {
        return aqid;
    }

    public List<String> getPackageIds() {
        return packageIds;
    }

    @Override
    public String getId() {
        return advertiserId;
    }


    @Override
    public String getName() {
        return advertiserName;
    }


    @Override
    public void setSecondBidPrice(final Double price) {
        secondBidPriceInUsd = price;
        secondBidPriceInLocal = price;
        LOG.debug(traceMarker, "responseContent before replaceMacros is {}", responseContent);
        responseContent = replaceIXMacros(responseContent);
        final ThirdPartyAdResponse adResponse = getResponseAd();
        adResponse.setResponse(responseContent);
        LOG.debug(traceMarker, "responseContent after replaceMacros is {}", getResponseAd().getResponse());
    }


    @Override
    public String getAuctionId() {
        return responseAuctionId;
    }


    @Override
    public String getRtbImpressionId() {
        return responseImpressionId;
    }


    public void setImpressionId(final String impressionId) {
        this.impressionId = impressionId;
    }


    @Override
    public String getSeatId() {
        return responseSeatId;
    }


    @Override
    public void setEncryptedBid(final String encryptedBid) {
        this.encryptedBid = encryptedBid;
    }


    @Override
    public double getSecondBidPriceInUsd() {
        return secondBidPriceInUsd;
    }


    @Override
    public double getSecondBidPriceInLocal() {
        return secondBidPriceInLocal;
    }


    @Override
    public double getBidPriceInUsd() {
        return bidPriceInUsd;
    }


    @Override
    public double getBidPriceInLocal() {
        return bidPriceInLocal;
    }


    @Override
    public String getCurrency() {
        return USD;
    }

    @Override
    public String getCreativeId() {
        // NOTE: crid is an optional field in IX and we are using aqid instead. Refer to Jira: PROG-292
        return aqid;
    }

    @Override
    public String getIUrl() {
        return sampleImageUrl;
    }


    @Override
    public List<Integer> getAttribute() {
        return creativeAttributes;
    }


    @Override
    public List<String> getADomain() {
        return advertiserDomains;
    }


    @Override
    public boolean isLogCreative() {
        return logCreative;
    }


    @Override
    public void setLogCreative(final boolean logCreative) {
        this.logCreative = logCreative;
    }

    @Override
    public String getAdMarkUp() {
        return adm;
    }

}
