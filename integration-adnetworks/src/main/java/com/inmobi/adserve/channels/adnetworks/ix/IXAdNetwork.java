package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.util.SproutTemplateConstants.SPROUT_UNIQUE_STRING;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.googlecode.cqengine.resultset.common.NoSuchObjectException;
import com.googlecode.cqengine.resultset.common.NonUniqueObjectException;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.adnetworks.rtb.RTBCallbackMacros;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.natives.NativeBuilder;
import com.inmobi.adserve.channels.api.natives.NativeBuilderFactory;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
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
import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.adserve.contracts.ix.request.AdQuality;
import com.inmobi.adserve.contracts.ix.request.App;
import com.inmobi.adserve.contracts.ix.request.Banner;
import com.inmobi.adserve.contracts.ix.request.Banner.API_FRAMEWORKS;
import com.inmobi.adserve.contracts.ix.request.BannerExtension;
import com.inmobi.adserve.contracts.ix.request.BidRequest;
import com.inmobi.adserve.contracts.ix.request.Blind;
import com.inmobi.adserve.contracts.ix.request.Device;
import com.inmobi.adserve.contracts.ix.request.Geo;
import com.inmobi.adserve.contracts.ix.request.Impression;
import com.inmobi.adserve.contracts.ix.request.ImpressionExtension;
import com.inmobi.adserve.contracts.ix.request.ProxyDemand;
import com.inmobi.adserve.contracts.ix.request.Publisher;
import com.inmobi.adserve.contracts.ix.request.RPBannerExtension;
import com.inmobi.adserve.contracts.ix.request.RPImpressionExtension;
import com.inmobi.adserve.contracts.ix.request.RPTargetingExtension;
import com.inmobi.adserve.contracts.ix.request.Regulations;
import com.inmobi.adserve.contracts.ix.request.RubiconExtension;
import com.inmobi.adserve.contracts.ix.request.Site;
import com.inmobi.adserve.contracts.ix.request.Transparency;
import com.inmobi.adserve.contracts.ix.request.User;
import com.inmobi.adserve.contracts.ix.request.Video;
import com.inmobi.adserve.contracts.ix.request.VideoExtension;
import com.inmobi.adserve.contracts.ix.request.nativead.Asset;
import com.inmobi.adserve.contracts.ix.request.nativead.Native;
import com.inmobi.adserve.contracts.ix.response.Bid;
import com.inmobi.adserve.contracts.ix.response.BidResponse;
import com.inmobi.adserve.contracts.ix.response.SeatBid;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.segment.impl.AdTypeEnum;
import com.inmobi.template.interfaces.TemplateConfiguration;
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
 * @author ritwik.kumar
 */
public class IXAdNetwork extends BaseAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(IXAdNetwork.class);
    private static final String USD = "USD";
    private static final String SITE_BLOCKLIST_FORMAT = "blk%s";
    private static final String RUBICON_PERF_BLOCKLIST_ID = "InMobiPERF";
    private static final String RUBICON_FS_BLOCKLIST_ID = "InMobiFS";
    private static final String BSSID_DERIVED = "BSSID_DERIVED";
    private static final String VISIBLE_BSSID = "VISIBLE_BSSID";
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
    protected static TemplateConfiguration templateConfiguration;
    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;
    @Inject
    private static NativeBuilderFactory nativeBuilderfactory;
    @Inject
    private static NativeResponseMaker nativeResponseMaker;
    protected final Gson gson;
    private final String userName;
    private final String password;
    private final Integer accountId;
    private final boolean wnRequired;
    private final List<String> globalBlindFromConfig;
    @Getter
    private final int bidFloorPercent;
    @Setter
    @Getter
    BidResponse bidResponse;
    @Getter
    @Setter
    private BidRequest bidRequest;
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
    private boolean templateWN = true;
    protected boolean isSproutSupported = false;

    private final String advertiserId;
    private final String advertiserName;
    private double secondBidPriceInUsd = 0;
    private double secondBidPriceInLocal = 0;
    private String bidRequestJson = DEFAULT_EMPTY_STRING;
    private String encryptedBid;
    private String responseImpressionId;
    private String responseAuctionId;
    @Getter
    private String dspId;
    @Getter
    private String advId;
    @Getter
    private Integer winningPackageId;
    @Getter
    private String dealId;
    private Double dealFloor;
    private Double dataVendorCost;
    private Double adjustbid;
    private int pmptier;
    private String aqid;
    private String nurl;
    protected boolean isCoppaSet = false;
    private String sampleImageUrl;
    private List<String> advertiserDomains;
    private List<Integer> creativeAttributes;
    private boolean logCreative = false;
    private String adm;
    private com.inmobi.adserve.contracts.ix.response.nativead.Native admobject;
    @Getter
    private int impressionObjCount;
    @Getter
    private int responseBidObjCount;
    @Getter
    private boolean isExternalPersonaDeal;
    private Set<Integer> usedCsIds;
    @Getter
    private List<Integer> packageIds;
    private List<String> iabCategories;
    private int minimumSdkVerForVAST;


    private WapSiteUACEntity wapSiteUACEntity;
    private boolean isWapSiteUACEntity = false;
    private ChannelSegmentEntity dspChannelSegmentEntity;
    private Map<Integer, Asset> mandatoryAssetMap;
    private Map<Integer, Asset> nonMandatoryAssetMap;

    @SuppressWarnings("unchecked")
    public IXAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String host,
            final String advertiserName, final boolean templateWinNotification) {
        super(baseRequestHandler, serverChannel);
        advertiserId = config.getString(advertiserName + ".advertiserId");
        urlArg = config.getString(advertiserName + ".urlArg");
        callbackUrl = config.getString(advertiserName + ".wnUrlback");
        ixMethod = config.getString(advertiserName + ".ixMethod");
        wnRequired = config.getBoolean(advertiserName + ".isWnRequired");
        this.clientBootstrap = clientBootstrap;
        this.host = host;
        this.isIxPartner = true;
        this.advertiserName = advertiserName;
        templateWN = templateWinNotification;
        isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", true);
        isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", true);
        userName = config.getString(advertiserName + ".userName");
        password = config.getString(advertiserName + ".password");
        accountId = config.getInt(advertiserName + ".accountId");
        globalBlindFromConfig = config.getList(advertiserName + ".globalBlind");
        bidFloorPercent = config.getInt(advertiserName + ".bidFloorPercent", 100);
        minimumSdkVerForVAST = config.getInt(advertiserName + ".vast.minimumSupportedSdkVersion", 450);
        gson = templateConfiguration.getGsonManager().getGsonInstance();
    }

    @Override
    protected AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClientProvider.getRtbAsyncHttpClient();
    }

    @Override
    protected boolean configureParameters() {
        LOG.debug(traceMarker, "inside configureParameters of IX");
        if (!checkIfBasicParamsAvailable()) {
            LOG.info(traceMarker, "Configure parameters inside IX returned false {}: BasicParams Not Available",
                    advertiserName);
            return false;
        }

        isVideoRequest = isRequestQualifiedForVideo();
        if (sasParams.getWapSiteUACEntity() != null) {
            wapSiteUACEntity = sasParams.getWapSiteUACEntity();
            isWapSiteUACEntity = true;
        }

        // If UAC is set try fetching IAB Categories based on it, set iabCategories before creating app/site
        if (isWapSiteUACEntity) {
            iabCategories = IABCategoriesMap.getIABCategoriesFromUAC(wapSiteUACEntity.getCategories());
        }
        // Still if iabCategories is not set try to set it from sasParams
        if (iabCategories == null || iabCategories.isEmpty()) {
            iabCategories = IABCategoriesMap.getIABCategories(sasParams.getCategories());
        }

        // Only 1 impression object is being generated. Creating Impression Object
        final List<Impression> impresssionlist = new ArrayList<Impression>();
        final Impression impression = createImpressionObject();
        if (null == impression) {
            LOG.info(traceMarker, "Configure parameters inside IX returned false {}: Impression Obj is null",
                    advertiserName);
            return false;
        }
        impresssionlist.add(impression);
        impressionObjCount = impresssionlist.size();


        // Creating BidRequest Object using unique auction id per auction
        bidRequest = createBidRequestObject(impresssionlist);
        if (null == bidRequest) {
            LOG.info(traceMarker, "Configure parameters inside IX returned false {}: Failed inside createBidRequest",
                    advertiserName);
            return false;
        }
        if (isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_SENT_AS_TRANSPARENT);
        } else {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_SENT_AS_BLIND);
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
                || StringUtils.isBlank(sasParams.getSiteId()) || StringUtils.isBlank(externalSiteId)
                || !isRequestFormatSupported()) {
            LOG.debug(
                    traceMarker,
                    "mandate parameters missing or request format is not compatible to partner supported response for dummy so exiting adapter");
            return false;
        }
        return true;
    }

    private boolean isRequestFormatSupported() {
        isNativeRequest = NATIVE_STRING.equals(sasParams.getRFormat()) && APP.equalsIgnoreCase(sasParams.getSource());
        return isNativeRequest ? isNativeResponseSupported : isHTMLResponseSupported;
    }

    private BidRequest createBidRequestObject(final List<Impression> impresssionlist) {
        LOG.debug(traceMarker, "INSIDE CREATE BID REQUEST OBJECT");
        final BidRequest tempBidRequest = new BidRequest(impresssionlist);
        if (WAP.equalsIgnoreCase(sasParams.getSource())) {
            // Set Site object
            tempBidRequest.setSite(createSiteObject());
        } else {
            tempBidRequest.setApp(createAppObject());
        }
        if (tempBidRequest.getApp() == null && tempBidRequest.getSite() == null) {
            // Both can't be null in a bid request
            return null;
        }

        tempBidRequest.setId(casInternalRequestParameters.getAuctionId());
        // Creating Regulations Object
        final Regulations regs = createRegsObject();
        // Creating Geo Object for device Object
        final Geo geo = createGeoObject();
        // Creating Device Object
        final Device device = createDeviceObject(geo);
        // Creating User Object
        final User user = createUserObject();
        tempBidRequest.setDevice(device);
        tempBidRequest.setUser(user);
        tempBidRequest.setRegs(regs);
        return tempBidRequest;
    }

    private String serializeBidRequest() {
        try {
            final String json = gson.toJson(bidRequest);
            LOG.info(traceMarker, "IX request json is: {}", json);
            return json;
        } catch (final Exception e) {
            LOG.debug(traceMarker, "Could not create json from bidRequest for partner {}", advertiserName);
            LOG.info(traceMarker, "Configure parameters inside IX returned false {} , exception thrown {}",
                    advertiserName, e);
            return null;
        }
    }

    private Regulations createRegsObject() {
        final Regulations regs = new Regulations();
        if (isWapSiteUACEntity) {
            if (wapSiteUACEntity.isCoppaEnabled() || sasParams.getAge() != null
                    && sasParams.getAge() <= AGE_LIMIT_FOR_COPPA) {
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
        proxyDemand.setMarketrate(sasParams.getMarketRate());
        return proxyDemand;
    }

    private Impression createImpressionObject() {
        Impression impression;
        if (null != casInternalRequestParameters.getImpressionId()) {
            // In order to conform to the rubicon spec, we are passing a unique integer identifier whose value starts
            // with 1, and increments up to n for n impressions.
            impression = new Impression("1");
        } else {
            LOG.info(traceMarker, "Impression id can not be null in Cas Internal Request Params");
            return null;
        }

        // Set Banner OR Video OR Native object.
        if (isVideoRequest) {
            final Video video = createVideoObject();
            impression.setVideo(video);
            if (video != null) {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VIDEO_REQUESTS);
            }
        } else if (isNativeRequest) {
            final Native nativeIx = createNativeObject();
            impression.setNat(nativeIx);
            if (nativeIx != null) {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_REQUESTS);
                mandatoryAssetMap = new HashMap<>();
                nonMandatoryAssetMap = new HashMap<>();
                for (final Asset asset : nativeIx.getAssets()) {
                    if (1 == asset.getRequired()) {
                        mandatoryAssetMap.put(asset.getId(), asset);
                    } else {
                        nonMandatoryAssetMap.put(asset.getId(), asset);
                    }
                }
            }
        } else {
            impression.setBanner(createBannerObject());
        }
        if (impression.getNat() == null && impression.getVideo() == null && impression.getBanner() == null) {
            // Either of Banner OR Video OR Native object must be present
            return null;
        }

        impression.setProxydemand(createProxyDemandObject());
        // Set interstitial or not, but for video int shoud be 1
        impression.setInstl(null != sasParams.getRqAdType() && "int".equalsIgnoreCase(sasParams.getRqAdType())
                || isVideoRequest ? 1 : 0);
        // note: bidFloorPercent logic has been duplicated in rrLogging
        impression.setBidfloor(casInternalRequestParameters.getAuctionBidFloor() * bidFloorPercent / 100);
        LOG.debug(traceMarker, "Bid floor is {}", impression.getBidfloor());
        final ImpressionExtension ext = getImpExt();
        impression.setExt(ext);
        // If ext is null, that means zone id is missing so do not proceed
        return ext == null ? null : impression;
    }

    private ImpressionExtension getImpExt() {
        final ImpressionExtension impExt = new ImpressionExtension();
        final JSONObject additionalParams = entity.getAdditionalParams();
        String zoneId = null;
        if (null != additionalParams) {
            zoneId = getZoneId(additionalParams);
            if (null == zoneId) {
                LOG.debug(traceMarker, "zone id not present, will say false");
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_ZONE_ID_NOT_PRESENT);
                // zoneID not available so returning NULL
                return null;
            }
            final RPImpressionExtension rp = new RPImpressionExtension(zoneId);
            impExt.setRp(rp);
        }

        // Find matching packages for Banner. NOTE: Packages are not yet supported for video & Native
        if (!(isVideoRequest || isNativeRequest)) {
            final long startTime = System.currentTimeMillis();
            packageIds = IXPackageMatcher.findMatchingPackageIds(sasParams, repositoryHelper, selectedSlotId);
            final long endTime = System.currentTimeMillis();
            InspectorStats.updateYammerTimerStats(DemandSourceType.findByValue(sasParams.getDst()).name(),
                    InspectorStrings.IX_PACKAGE_MATCH_LATENCY, endTime - startTime);

            if (!packageIds.isEmpty()) {
                final RPImpressionExtension rp =
                        impExt.getRp() == null ? new RPImpressionExtension(zoneId) : impExt.getRp();

                final RPTargetingExtension target = new RPTargetingExtension();
                target.setPackages(Lists.transform(packageIds, Functions.toStringFunction()));
                rp.setTarget(target);
                impExt.setRp(rp);

                // Update the stats
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_DEAL_REQUESTS);
            }
        }
        return impExt;
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

    private Native createNativeObject() {
        final NativeAdTemplateEntity templateEntity =
                repositoryHelper.queryNativeAdTemplateRepository(sasParams.getSiteId());
        if (templateEntity == null) {
            LOG.info(traceMarker,
                    String.format("This site id %s doesn't have native template: ", sasParams.getSiteId()));
            return null;
        }
        final NativeBuilder nb = nativeBuilderfactory.create(templateEntity);
        return nb.buildNativeIX();
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

        final BannerExtension ext = new BannerExtension();
        if (null != selectedSlotId) {
            final Integer rpSlot = SlotSizeMapping.getIXMappedSlotId(selectedSlotId);
            if (rpSlot != null) {
                final RPBannerExtension rp = new RPBannerExtension();
                rp.setMime(MIME_HTML);
                rp.setSize_id(rpSlot);
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
            final JSONObject siteVideoPreferencesJson = new JSONObject(sasParams.getPubControlPreferencesJson());
            soundOn = siteVideoPreferencesJson.getJSONObject("video").getBoolean("soundOn");
            isSkippable = siteVideoPreferencesJson.getJSONObject("video").getBoolean("skippable");
        } catch (final Exception e) {
            LOG.error("Caught Exception while fetching skippable/soundOn settings for site {}.", sasParams.getSiteId());
            InspectorStats.incrementStatCount(getName(), InspectorStrings.INVALID_MEDIA_PREFERENCES_JSON);
        }

        final Video video = new Video(VIDEO_MIMES, VIDEO_MIN_DURATION, VIDEO_MAX_DURATION);

        final List<Integer> playbackMethod = new ArrayList<>(1);
        playbackMethod.add(soundOn ? 1 : 2);
        video.setPlaybackmethod(playbackMethod);

        video.setBoxingallowed(0); // Always set to false
        video.setProtocols(VIDEO_PROTOCOLS);
        video.setMaxbitrate(VIDEO_MAX_BITRATE);

        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            video.setW((int) dim.getWidth());
            video.setH((int) dim.getHeight());
        }

        final VideoExtension ext = new VideoExtension();
        ext.setSkip(isSkippable ? 1 : 0);
        ext.setSkipdelay(5); // Default 5 secs.
        video.setExt(ext);
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

        // Check for minimum sdk version
        if (!Formatter.isRequestFromSdkVersionOnwards(sasParams, minimumSdkVerForVAST)) {
            return false;
        }

        boolean isQualifiedForVideo = false;
        final List<AdTypeEnum> supportedAdTypes = sasParams.getPubControlSupportedAdTypes();
        if (null != supportedAdTypes && supportedAdTypes.contains(AdTypeEnum.VIDEO)) {
            // If this site supports only Video, qualify this request for video.
            // No need to further look up in the VideoTraffic Repository.
            if (supportedAdTypes.size() == 1) {
                isQualifiedForVideo = true;
            } else {
                final int videoTrafficPercentage =
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
        final CommonExtension geoExt = new CommonExtension();
        final RubiconExtension rpExt = new RubiconExtension();
        rpExt.setConsent(1);
        geoExt.setRp(rpExt);
        geo.setExt(geoExt);
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
            InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_SITE_ID_NOT_PRESENT);
            return null;
        }
        site.setCat(iabCategories);

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
            InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_SITE_ID_NOT_PRESENT);
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
        final Device device = new Device(sasParams.getUserAgent(), sasParams.getRemoteHostIp());
        device.setGeo(geo);
        final Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            device.setOs(HandSetOS.values()[sasParamsOsId - 1].toString());
        }

        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            device.setOsv(sasParams.getOsMajorVersion());
        }

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
        // Setting Extension for ifa if Coppa is not set, only then set IFA
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

        final RubiconExtension rpForDevice = new RubiconExtension();
        rpForDevice.setXff(sasParams.getRemoteHostIp());
        final CommonExtension ext = new CommonExtension();
        ext.setRp(rpForDevice);
        device.setExt(ext);
        return device;
    }

    public String replaceIXMacros(String url) {
        url = url.replaceAll(RTBCallbackMacros.AUCTION_ID_INSENSITIVE, bidResponse.getId());
        url = url.replaceAll(RTBCallbackMacros.AUCTION_CURRENCY_INSENSITIVE, USD);
        if (6 != sasParams.getDst()) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_ENCRYPTED_INSENSITIVE, encryptedBid);
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_INSENSITIVE, Double.toString(secondBidPriceInLocal));
        }
        if (null != bidResponse.getBidid()) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_BID_ID_INSENSITIVE, bidResponse.getBidid());
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
        adStatus = NO_AD;
        LOG.info(traceMarker, "Original RP response is {}", response);
        if (status.code() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            statusCode = status.code();
            final boolean parsedResponse = deserializeResponse(response);
            if (!parsedResponse) {
                responseContent = DEFAULT_EMPTY_STRING;
                statusCode = 500;
                LOG.info(traceMarker, "Could not return an Ad as parsing of ix response failed");
                return;
            }

            responseContent = DEFAULT_EMPTY_STRING;
            adStatus = AdStatus.AD.name();

            if (isNativeRequest()) {
                nativeAdBuilding();
            } else if (isVideoRequest) {
                videoAdBuilding();
            } else {
                bannerAdBuilding();
            }

        }
        LOG.debug(traceMarker, "response content length is {}", responseContent.length());
        LOG.debug(traceMarker, "response content is {}", responseContent);
    }


    protected boolean isSproutAd() {
        final String adm = getAdMarkUp();
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
            LOG.debug(traceMarker, "Modifying existing impression id with new int key: incId {}", incId);
            final String newImpressionId =
                    ImpressionIdGenerator.getInstance().resetWilburyIntKey(oldImpressionId, incId);

            if (StringUtils.isNotEmpty(newImpressionId)) {
                // Update beacon and click URLs
                beaconUrl =
                        ClickUrlsRegenerator.regenerateBeaconUrl(beaconUrl, oldImpressionId, newImpressionId,
                                sasParams.isRichMedia());
                clickUrl = ClickUrlsRegenerator.regenerateClickUrl(clickUrl, oldImpressionId, newImpressionId);
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

        if (WAP.equalsIgnoreCase(sasParams.getSource())) {
            if (isSproutAd()) {
                LOG.debug(traceMarker, "Sprout Ads not supported on WAP");
                InspectorStats.incrementStatCount(getName(),
                        InspectorStrings.DROPPED_AS_SPROUT_ADS_ARE_NOT_SUPPORTED_ON_WAP);
                responseContent = DEFAULT_EMPTY_STRING;
                adStatus = NO_AD;
                return;
            }
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, admContent);
        } else {
            if (isSproutAd()) {
                if (!isSproutSupported) {
                    LOG.debug(traceMarker, "Sprout Ads not supported on SDK < 370");
                    InspectorStats.incrementStatCount(getName(),
                            InspectorStrings.DROPPED_AS_SPROUT_ADS_ARE_NOT_SUPPORTED_ON_SDK370);
                    responseContent = DEFAULT_EMPTY_STRING;
                    adStatus = NO_AD;
                    return;
                }
                LOG.debug(traceMarker, "Sprout Ad Received");
                admContent =
                        IXAdNetworkHelper.replaceSproutMacros(admContent, casInternalRequestParameters, sasParams,
                                isCoppaSet, clickUrl, beaconUrl);
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

        final int admAfterMacroSize = admContent.length();
        if (templateWN || admAfterMacroSize == admSize) {
            velocityContext.put(VelocityTemplateFieldConstants.IM_BEACON_URL, beaconUrl);
        }
        try {
            responseContent = Formatter.getResponseFromTemplate(TemplateType.IX_HTML, velocityContext, sasParams, null);
        } catch (final Exception e) {
            adStatus = NO_AD;
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
        velocityContext.put(VelocityTemplateFieldConstants.NAMESPACE, Formatter.getRTBDNamespace());

        // IMAIBaseUrl
        velocityContext.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());

        // Sprout related parameters.
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            velocityContext.put(VelocityTemplateFieldConstants.WIDTH, (int) dim.getWidth());
            velocityContext.put(VelocityTemplateFieldConstants.HEIGHT, (int) dim.getHeight());
        }

        final String networkType =
                null != sasParams.getNetworkType() ? sasParams.getNetworkType().name() : NetworkType.NON_WIFI.name();
        final String requestNetworkTypeJson = "{\"networkType\":\"" + networkType + "\"}";
        // Publisher control settings
        velocityContext.put(VelocityTemplateFieldConstants.REQUEST_JSON, requestNetworkTypeJson);
        velocityContext.put(VelocityTemplateFieldConstants.SITE_PREFERENCES_JSON,
                sasParams.getPubControlPreferencesJson());

        try {
            responseContent =
                    Formatter
                            .getResponseFromTemplate(TemplateType.INTERSTITIAL_VIDEO, velocityContext, sasParams, null);
        } catch (final Exception e) {
            adStatus = NO_AD;
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
            LOG.debug(traceMarker, "nurl is {}", nurl);
            if (!StringUtils.isEmpty(callbackUrl)) {
                LOG.debug(traceMarker, "inside wn from config");
                winUrl = callbackUrl;
            } else if (!StringUtils.isEmpty(nurl)) {
                LOG.debug(traceMarker, "inside wn from nurl");
                winUrl = nurl;
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

    protected void nativeAdBuilding() {
        InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_NATIVE_RESPONSES);
        try {
            final App app = bidRequest.getApp();
            final Map<String, String> params = new HashMap<>();
            params.put("beaconUrl", beaconUrl);
            params.put("winUrl", beaconUrl + "?b=${WIN_BID}");
            params.put("appId", app.getId());
            params.put("siteId", sasParams.getSiteId());
            params.put("nUrl", nurl);

            final com.inmobi.template.context.App templateContext =
                    IXAdNetworkHelper.validateAndBuildTemplateContext(admobject, mandatoryAssetMap,
                            nonMandatoryAssetMap, impressionId);
            if (null == templateContext) {
                adStatus = TERM;
                responseContent = DEFAULT_EMPTY_STRING;
                LOG.debug(traceMarker, "Native Ad Building failed as admobject failed validation");
                // Raising exception in parse response if native admobject failed validation.
                InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_PARSE_RESPONSE_EXCEPTION);
                return;
            }
            responseContent = nativeResponseMaker.makeIXResponse(templateContext, params);
        } catch (final Exception e) {
            adStatus = TERM;
            responseContent = DEFAULT_EMPTY_STRING;
            LOG.error("Some exception is caught while filling the native template for siteId = {}, advertiser = {}, "
                    + "exception = {}", sasParams.getSiteId(), advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_PARSE_RESPONSE_EXCEPTION);
        }
    }

    /**
     * Checks whether the response string structurally conforms to the response contract. This is enforced by
     * GsonContractDeserialiser
     * 
     * @param response Response string received from partner
     * @return true if response string conforms to the response contract
     */
    protected boolean conformsToContract(final String response) {
        try {
            // Null and empty String checks are handled by gson itself
            bidResponse = gson.fromJson(response, BidResponse.class);
            if (null == bidResponse) {
                LOG.debug("Returning from conformsToContract as response was null/empty");
                return false;
            }
        } catch (final JsonParseException jpe) {
            LOG.error(traceMarker, "Deserialisation failed as response does not conform to gson contract: {}",
                    jpe.getMessage());
            InspectorStats.incrementStatCount(getName(), InspectorStrings.JSON_PARSING_ERROR);
            // TODO: Figure out why adStatus is always reverted back to NO_AD
            adStatus = AdStatus.TERM.name();
            return false;
        } catch (final Exception e) {
            LOG.error(traceMarker, "Deserialisation failed as response does not conform to gson contract: {}",
                    e.toString());
            adStatus = AdStatus.TERM.name();
            return false;
        }
        return true;
    }

    /**
     * Checks whether the response object has a non zero status code and logs it.
     * 
     * @return true if response object has a non-zero status code
     */
    private boolean responseHasNonZeroStatusCode() {
        if (null == bidResponse) {
            return false;
        }

        final Integer responseStatusCode = bidResponse.getStatuscode();
        if (null != responseStatusCode && 0 != responseStatusCode) {
            handleResponseStatusCode(responseStatusCode);
            return true;
        }
        return false;
    }

    /**
     * Checks whether the response object also conforms to the following conditions: <br>
     * 1) There is at least one SeatBid object <br>
     * 2) There is at least one Bid object <br>
     * 3) Buyer is not empty/null <br>
     * 4) Both adm and admobject are not simultaneously empty/null nor simultaneously set
     * 
     * @return true if the above conditions are met
     */
    protected boolean conformsToValidBidStructure() {
        if (null == bidResponse) {
            adStatus = AdStatus.NO_AD.name();
            return false;
        }

        if (CollectionUtils.isEmpty(bidResponse.getSeatbid())) {
            LOG.error(traceMarker, "Deserialisation failed: List of SeatBid objects was empty");
            adStatus = AdStatus.TERM.name();
            return false;
        }
        final SeatBid seatBid = bidResponse.getSeatbid().get(0);

        if (CollectionUtils.isEmpty(seatBid.getBid())) {
            LOG.error(traceMarker, "Deserialisation failed: List of Bid objects was empty");
            adStatus = AdStatus.TERM.name();
            return false;
        }
        final Bid bid = seatBid.getBid().get(0);

        final String buyer = seatBid.getBuyer();
        if (StringUtils.isEmpty(buyer)) {
            LOG.error(traceMarker, "Deserialisation failed: Missing required field: buyer");
            adStatus = AdStatus.TERM.name();
            return false;
        }

        // No Need for making this required, since we have AbstractAuctionFilter
        // final String aqid = bid.getAqid();
        // if (StringUtils.isEmpty(aqid)) {
        // LOG.error(traceMarker, "Deserialisation failed: Missing required field: aqid");
        // adStatus = AdStatus.TERM.name();
        // return false;
        // }

        if (StringUtils.isEmpty(bid.getAdm()) && null == bid.getAdmobject()) {
            LOG.error(traceMarker, "Deserialisation failed: Both adm and admobject are missing");
            adStatus = AdStatus.TERM.name();
            return false;
        }

        if (StringUtils.isNotEmpty(bid.getAdm()) && null != bid.getAdmobject()) {
            LOG.error(traceMarker, "Deserialisation failed: Both adm and admobject are present");
            adStatus = AdStatus.TERM.name();
            return false;
        }

        return true;
    }

    /**
     * Deserialises and validates the bid response object and configures the adapter
     * 
     * @param response
     * @return
     */
    protected boolean deserializeResponse(final String response) {
        // In-case of unexpected error in deserializeResponse(), the adStatus is set to TERM. Otherwise, it is already
        // set as NO_AD.
        LOG.debug("Started deserialising response from RP");

        if (StringUtils.isEmpty(response)) {
            return false;
        }
        // Hack for IX beacon discrepancy fix
        final String replacedResponse = response.replace("src=\"//beacon", "src=\"http://beacon");
        if (!response.equals(replacedResponse)) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_BEACON_WITHOUT_HTTP);
        }
        if (!conformsToContract(replacedResponse) || responseHasNonZeroStatusCode() || !conformsToValidBidStructure()) {
            return false;
        }

        // Configuring of adapter from bid response
        final SeatBid seatBid = bidResponse.getSeatbid().get(0);
        final Bid bid = seatBid.getBid().get(0);
        dspId = seatBid.getBuyer();

        responseAuctionId = bidResponse.getId();
        responseBidObjCount = seatBid.getBid().size();
        responseImpressionId = bid.getImpid();
        dealId = bid.getDealid();
        isExternalPersonaDeal = false;
        if (dealId != null) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_DEAL_RESPONSES);
            setFloorVendorUsedCsids();
        }
        pmptier = bid.getPmptier();
        nurl = bid.getNurl();
        // creativeId = bid.getCrid(); // Replaced with aqid
        aqid = bid.getAqid();
        adjustbid = bid.getAdjustbid();
        // bidderCurrency is set to USD by default
        setBidPriceInLocal(bid.getPrice());
        setBidPriceInUsd(getBidPriceInLocal());

        adm = bid.getAdm();
        admobject = bid.getAdmobject();
        if (null != admobject) {
            // TODO: who consumes sampleAdvertiser log data
            // Done to maintain consistency in logging (See sampleAdvertiserLogging)
            adm = admobject.toString();
        }

        // Fetch bid.ext.rp.advid.
        if (null != bid.getExt() && null != bid.getExt().getRp()) {
            advId = bid.getExt().getRp().getAdvid();
        }


        // For video requests, validate that a valid XML is received.
        if (isVideoRequest) {
            if (IXAdNetworkHelper.isAdmValidXML(getAdMarkUp())) {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VIDEO_RESPONSES);
            } else {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.INVALID_VIDEO_RESPONSE_COUNT);
                adStatus = TERM;
                return false;
            }
        }
        /* The following fields are not being used:
            BidResponse->bidid
            SeatBid->seat
            Bid->estimated
            Bid->adomain
            Bid->w and Bid->h
        */

        if (updateDSPAccountInfo(dspId)) {
            LOG.debug(traceMarker, "Response successfully deserialised");
            return true;
        } else {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.INVALID_DSP_ID);
            adStatus = TERM;
            return false;
        }
    }

    private void handleResponseStatusCode(final Integer responseStatusCode) {
        // TODO: Remove Magic numbers
        switch (responseStatusCode) {
            case 3:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_INVALID_REQUEST);
                LOG.debug("RP returned NO_AD as request was invalid");
                break;
            case 10:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_NO_MATCH);
                LOG.debug("RP returned NO_AD as no ad matched the request criteria");
                break;
            case 15:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_REFERRER_NOT_ALLOWED);
                LOG.debug("RP returned NO_AD as referrer was not allowed on this request");
                break;
            case 16:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_INVENTORY_IDENTIFIER_INVALID);
                LOG.debug("RP returned NO_AD as combination of inventory identifiers was not valid. This generally "
                        + "occurs if there is a mismatch between account_id, site_id, zone_id and size_id fields");
                break;
            case 17:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_SUSPECTED_SPIDER);
                LOG.debug("RP returned NO_AD as the user agent indicated a suspected spider");
                break;
            case 18:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_SUSPECTED_BOTNET);
                LOG.debug("RP returned NO_AD as the IP indicated a suspected botnet");
                break;
            case 21:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_REFERRER_BLOCKED);
                LOG.debug("RP returned NO_AD as referrer was blocked due to suspected bad traffic");
                break;
            case 27:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_NOT_AUTHORIZED);
                LOG.debug("RP returned NO_AD as request was not authorized");
                break;
            case 32:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_PROXY_BID_WINS);
                LOG.debug("RP returned NO_AD as proxy bid won");
                break;
            default:
                InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_OTHER_ERRORS);
                LOG.debug("RP returned NO_AD. Refer to status code: {} for more info", statusCode);
                break;
        };
    }


    private void setFloorVendorUsedCsids() {
        IXPackageEntity matchedPackageEntity;

        try {
            matchedPackageEntity = repositoryHelper.queryIxPackageByDeal(dealId);
            winningPackageId = matchedPackageEntity.getId();
        } catch (final NoSuchObjectException exception) {
            LOG.error("Rubicon DealId not stored in ix_package_deals table, {}", dealId);
            return;
        } catch (final NonUniqueObjectException exception) {
            LOG.error("Rubicon DealId not unique in ix_package_deals table, {}", dealId);
            return;
        }

        final int indexOfDealId = matchedPackageEntity.getDealIds().indexOf(dealId);
        dealFloor =
                matchedPackageEntity.getDealFloors().size() > indexOfDealId ? matchedPackageEntity.getDealFloors().get(
                        indexOfDealId) : 0;
        dataVendorCost = matchedPackageEntity.getDataVendorCost();
        if (dataVendorCost > 0.0) {
            isExternalPersonaDeal = true;

            usedCsIds = new HashSet<Integer>();

            final Set<Set<Integer>> csIdInPackages = matchedPackageEntity.getDmpFilterSegmentExpression();
            for (final Set<Integer> smallSet : csIdInPackages) {
                for (final Integer csIdInSet : smallSet) {
                    if (sasParams.getCsiTags().contains(csIdInSet)) {
                        usedCsIds.add(csIdInSet);
                    }
                }
            }
        }
        return;
    }

    public double returnAdjustBid() {
        return adjustbid;
    }

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

    public int returnPmpTier() {
        return pmptier;
    }

    public String returnAqid() {
        return aqid;
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
        return null;
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

    private enum AdStatus {
        NO_AD, TERM, AD
    }

}
