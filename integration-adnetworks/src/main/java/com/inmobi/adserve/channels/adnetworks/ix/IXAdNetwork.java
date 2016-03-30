package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.DISPLAY_MANAGER_INMOBI_JS;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.DISPLAY_MANAGER_INMOBI_SDK;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.ONE;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.UTF_8;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.googlecode.cqengine.resultset.common.NoSuchObjectException;
import com.googlecode.cqengine.resultset.common.NonUniqueObjectException;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.adnetworks.rtb.RTBCallbackMacros;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.BaseAdNetworkHelper;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.natives.CommonNativeBuilderFactory;
import com.inmobi.adserve.channels.api.natives.NativeBuilder;
import com.inmobi.adserve.channels.api.natives.NativeBuilderFactory;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilder;
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
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.contracts.common.request.nativead.Asset;
import com.inmobi.adserve.contracts.common.request.nativead.Native;
import com.inmobi.adserve.contracts.iab.ApiFrameworks;
import com.inmobi.adserve.contracts.iab.VastCompanionTypes;
import com.inmobi.adserve.contracts.iab.VideoProtocols;
import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.adserve.contracts.ix.request.AdQuality;
import com.inmobi.adserve.contracts.ix.request.App;
import com.inmobi.adserve.contracts.ix.request.Banner;
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
import com.inmobi.adserve.contracts.ix.response.Bid;
import com.inmobi.adserve.contracts.ix.response.BidResponse;
import com.inmobi.adserve.contracts.ix.response.SeatBid;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.types.LocationSource;
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
    private static final String MIME_HTML = "text/html";
    private static final int INMOBI_SDK_VERSION_370 = 370;

    private static final int IX_MRAID2_VALUE = 1001;
    private static final int IX_MRAID1_VALUE = 1000;
    private static final List<Integer> MRAID_FRAMEWORK_VALUES = Lists.newArrayList(ApiFrameworks.MRAID_2.getValue(),
            ApiFrameworks.MRAID_1.getValue(), IX_MRAID2_VALUE, IX_MRAID1_VALUE);

    private static final String BLIND_BUNDLE_APP_FORMAT = "com.ix.%s";
    private static final String BLIND_DOMAIN_SITE_FORMAT = "http://www.ix.com/%s";
    private static final short AGE_LIMIT_FOR_COPPA = 8;

    // The following section is related to VIDEO.
    private static final Integer VIDEO_MIN_DURATION = 0;
    private static final Integer VIDEO_MAX_DURATION = 30;
    private static final Integer VIDEO_MAX_BITRATE = 2000;
    private static final List<Integer> VAST_COMPANION_TYPES =
            Lists.newArrayList(VastCompanionTypes.STATIC_RESOURCE.getValue());
    private static final List<Integer> VIDEO_PROTOCOLS = Lists.newArrayList(VideoProtocols.VAST_2_0_WRAPPER.getValue());
    private static final List<String> VIDEO_MIMES = Lists.newArrayList("video/mp4"); // Supported video mimes
    private static final String RIGHT_TO_FIRST_REFUSAL_DEAL = "RIGHT_TO_FIRST_REFUSAL_DEAL";
    @Inject
    protected static TemplateConfiguration templateConfiguration;
    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;
    @Inject
    @CommonNativeBuilderFactory
    private static NativeBuilderFactory nativeBuilderfactory;
    @Inject
    private static NativeResponseMaker nativeResponseMaker;
    protected final Gson gson;
    private final String userName;
    private final String password;
    private final Integer accountId;
    private final List<String> globalBlindFromConfig;
    @Getter
    private final int bidFloorPercent;
    @Setter
    BidResponse bidResponse;
    @Getter
    private BidRequest bidRequest;
    @Setter
    private String urlArg;
    private final String ixMethod;
    @Getter
    private double originalBidPriceInUsd;
    private double bidPriceInUsd;
    private double bidPriceInLocal;
    protected boolean isSproutSupported = false;
    private boolean altSizeIdsSet = false;

    private final String unknownAdvertiserId;
    private final String advertiserId;
    private final String advertiserName;
    private double secondBidPriceInUsd = 0;
    private String bidRequestJson = DEFAULT_EMPTY_STRING;
    private String encryptedBid;
    private String responseImpressionId;
    private String responseAuctionId;
    @Getter
    private String dspId;
    @Getter
    private String seatId;
    @Getter
    private String advId;
    @Getter
    private Integer winningPackageId;
    @Getter
    private String dealId;
    @Getter
    private Double dealFloor;
    @Getter
    private Double agencyRebatePercentage;
    @Getter
    private Double dataVendorCost;
    @Getter
    private Double adjustbid;
    @Getter
    private String aqid;
    private String nurl;
    private String viewabilityTracker;
    protected boolean isCoppaSet = false;
    private String sampleImageUrl;
    private List<String> advertiserDomains;
    private List<Integer> creativeAttributes;
    private boolean logCreative = false;
    private String adm;
    private com.inmobi.adserve.contracts.common.response.nativead.Native nativeObj;
    @Getter
    private int impressionObjCount;
    @Getter
    private int responseBidObjCount;
    @Getter
    private boolean isAgencyRebateDeal;
    @Getter
    private boolean isExternalPersonaDeal;
    private boolean hasViewabilityDeal;
    @Getter
    private boolean isTrumpDeal = false;
    @Getter
    private Set<Integer> usedCsIds;
    @Getter
    private List<Integer> packageIds;
    private List<String> iabCategories;
    private final String sproutUniqueIdentifierRegex;

    private WapSiteUACEntity wapSiteUACEntity;
    private boolean isWapSiteUACEntity = false;
    @Getter
    private ChannelSegmentEntity dspChannelSegmentEntity;
    private Map<Integer, Asset> mandatoryAssetMap;
    private Map<Integer, Asset> nonMandatoryAssetMap;
    private IXSlotMatcher matchedCAU;
    private NativeAdTemplateEntity templateEntity;

    @SuppressWarnings("unchecked")
    public IXAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String host,
            final String advertiserName, final boolean templateWinNotification) {
        super(baseRequestHandler, serverChannel);
        advertiserId = config.getString(advertiserName + ".advertiserId");
        unknownAdvertiserId = config.getString(advertiserName + ".unknownAdvId");
        urlArg = config.getString(advertiserName + ".urlArg");
        ixMethod = config.getString(advertiserName + ".ixMethod");
        this.clientBootstrap = clientBootstrap;
        this.host = host;
        isIxPartner = true;
        this.advertiserName = advertiserName;
        isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", true);
        isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", true);
        userName = config.getString(advertiserName + ".userName");
        password = config.getString(advertiserName + ".password");
        accountId = config.getInt(advertiserName + ".accountId");
        globalBlindFromConfig = config.getList(advertiserName + ".globalBlind");
        bidFloorPercent = config.getInt(advertiserName + ".bidFloorPercent", 100);
        sproutUniqueIdentifierRegex =
                config.getString(advertiserName + ".sprout.uniqueIdentifierRegex", "(?s).*data-creative[iI]d.*");
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
            LOG.debug(traceMarker,
                    "mandate parameters missing or request format is not compatible to partner supported response for "
                            + "dummy so exiting adapter");
            return false;
        }
        return true;
    }

    private boolean isRequestFormatSupported() {
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
        // creating extentsion
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
        isCoppaSet = isWapSiteUACEntity && wapSiteUACEntity.isCoppaEnabled()
                || sasParams.getAge() != null && sasParams.getAge() <= AGE_LIMIT_FOR_COPPA;
        regs.setCoppa(isCoppaSet ? 1 : 0);
        return regs;
    }

    private ProxyDemand createProxyDemandObject() {
        final ProxyDemand proxyDemand = new ProxyDemand();
        proxyDemand.setMarketrate(forwardedBidGuidance);
        return proxyDemand;
    }

    private Impression createImpressionObject() {
        Impression impression;
        if (null != casInternalRequestParameters.getImpressionId()) {
            // In order to conform to the rubicon spec, we are passing a unique integer identifier whose value starts
            // with 1, and increments up to n for n impressions.
            impression = new Impression(ONE);
        } else {
            LOG.info(traceMarker, "Impression id can not be null in Cas Internal Request Params");
            return null;
        }
        impression.setSecure(sasParams.isSecureRequest() ? 1 : 0);
        // Set Banner OR Video OR Native object.
        if (isVideoRequest || isRewardedVideoRequest || isPureVastRequest) {
            final Video video = createVideoObject();
            impression.setVideo(video);
            if (video != null) {
                final String statName = isVideoRequest
                        ? InspectorStrings.TOTAL_VAST_VIDEO_REQUESTS
                        : isPureVastRequest
                                ? InspectorStrings.TOTAL_PURE_VAST_REQUESTS
                                : InspectorStrings.TOTAL_REWARDED_VAST_VIDEO_REQUESTS;
                if (isPureVastRequest) {
                    if (null != sasParams.getIntegrationDetails()
                            && (IntegrationMethod.API != sasParams.getIntegrationDetails().getIntegrationMethod()
                                    && IntegrationMethod.API_VAST != sasParams.getIntegrationDetails()
                                            .getIntegrationMethod())) {
                        InspectorStats.incrementStatCount(getName(),
                                InspectorStrings.TOTAL_PURE_VAST_REQUESTS_FOR_OTHER_THAN_API);
                    }

                    if (null != sasParams.getVastProtocols()) {
                        sasParams.getVastProtocols().parallelStream()
                                .forEach(t -> InspectorStats.incrementStatCount(getName(),
                                        InspectorStrings.TOTAL_PURE_VAST_REQUESTS_FOR_PROTOCOL + t));
                    }

                }
                InspectorStats.incrementStatCount(getName(), statName);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VIDEO_REQUESTS);
            }
        } else if (isNativeRequest) {
            final Native nativeIx = createNativeObject();
            impression.setNat(nativeIx);
            if (nativeIx != null) {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_NATIVE_REQUESTS);
                mandatoryAssetMap = new HashMap<>();
                nonMandatoryAssetMap = new HashMap<>();
                for (final Asset asset : nativeIx.getRequestobj().getAssets()) {
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

        forwardedBidFloor = casInternalRequestParameters.getAuctionBidFloor() * bidFloorPercent / 100;
        forwardedBidGuidance = Math.max(sasParams.getMarketRate(), forwardedBidFloor);
        impression.setProxydemand(createProxyDemandObject());
        // Set interstitial or not, but for video int shoud be 1
        final boolean isInterstitial = RequestedAdType.INTERSTITIAL == sasParams.getRequestedAdType();
        impression.setInstl(isInterstitial || isVideoRequest || isPureVastRequest || isRewardedVideoRequest ? 1 : 0);
        impression.setBidfloor(forwardedBidFloor);
        LOG.debug(traceMarker, "Bid floor is {}", impression.getBidfloor());

        if (null != sasParams.getSdkVersion()) {
            impression.setDisplaymanager(DISPLAY_MANAGER_INMOBI_SDK);
            impression.setDisplaymanagerver(sasParams.getSdkVersion());
        } else if (null != sasParams.getAdcode() && "JS".equalsIgnoreCase(sasParams.getAdcode())) {
            impression.setDisplaymanager(DISPLAY_MANAGER_INMOBI_JS);
        }

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

        // Find matching packages
        final long startTime = System.currentTimeMillis();
        packageIds = IXPackageMatcher.findMatchingPackageIds(sasParams, repositoryHelper, processedSlotId, entity);
        final long endTime = System.currentTimeMillis();
        InspectorStats.updateYammerTimerStats(DemandSourceType.findByValue(sasParams.getDst()).name(),
                InspectorStrings.IX_PACKAGE_MATCH_LATENCY, endTime - startTime);

        if (CollectionUtils.isNotEmpty(packageIds)) {
            final RPImpressionExtension rp =
                    impExt.getRp() == null ? new RPImpressionExtension(zoneId) : impExt.getRp();

            final RPTargetingExtension target = new RPTargetingExtension();
            target.setPackages(Lists.transform(packageIds, Functions.toStringFunction()));
            rp.setTarget(target);
            impExt.setRp(rp);

            // Update the stats
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_DEAL_REQUESTS);
        }
        return impExt;
    }

    private String getZoneId(final JSONObject additionalParams) {
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
        templateEntity = repositoryHelper.queryNativeAdTemplateRepository(sasParams.getPlacementId());
        if (templateEntity == null) {
            LOG.info(traceMarker,
                    String.format("This placement id %d doesn't have native template: ", sasParams.getPlacementId()));
            return null;
        }
        final NativeBuilder nb = nativeBuilderfactory.create(templateEntity);
        return (Native) nb.buildNative();
    }

    private Banner createBannerObject() {
        final Banner banner = new Banner();
        // Presently only one banner object per impression object is being sent
        // When multiple banner objects will be supported,banner ids will begin at 1 and end at n for n banner objects
        banner.setId(ONE);

        Dimension dim = null;
        Integer rpSlot = null;
        final List<Integer> rpSlots = new ArrayList<Integer>();
        if (isCAURequest()) {
            LOG.debug(traceMarker, "Request for CAU, so find matching slot");
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_UMP_CAU_REQUESTS);
            matchedCAU = new IXSlotMatcher(repositoryHelper);
            rpSlot = matchedCAU.getMatchingSlotForCAU(sasParams.getCauMetadataSet());
            if (rpSlot != null) {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_CAU_REQUESTS);
                dim = matchedCAU.getMatchedRPDimension();
            }
        } else {
            rpSlot = SlotSizeMapping.getIXMappedSlotId(processedSlotId);
            final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(processedSlotId);
            if (null != slotSizeMapEntity) {
                dim = slotSizeMapEntity.getDimension();
            }

            final List<Short> sasParamSlotList = sasParams.getProcessedMkSlot();
            if (null != sasParamSlotList) {
                for (final short slot : sasParamSlotList) {
                    final Integer mappedSlot = SlotSizeMapping.getIXMappedSlotId(slot);
                    if (null != mappedSlot && !mappedSlot.equals(new Integer(rpSlot))) {
                        rpSlots.add(mappedSlot);
                    }
                }
            }
        }

        if (null != dim) {
            banner.setW((int) dim.getWidth());
            banner.setH((int) dim.getHeight());
        }

        // Don't send API for CAU and Lower SDK Versions to get static ads
        if (!isCAURequest() && StringUtils.isNotBlank(sasParams.getSdkVersion())) {
            final int sdkVersion = Integer.parseInt(sasParams.getSdkVersion().substring(1));
            if (sdkVersion >= INMOBI_SDK_VERSION_370) {
                banner.setApi(MRAID_FRAMEWORK_VALUES);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_RICH_MEDIA_REQUESTS);
                isSproutSupported = true;
            }
        }

        final BannerExtension ext = new BannerExtension();
        if (rpSlot != null) {
            final RPBannerExtension rp = new RPBannerExtension();
            rp.setMime(MIME_HTML);
            rp.setSize_id(rpSlot);
            rp.setUsenurl(true);

            if (rpSlots.size() > 0) {
                InspectorStats.incrementStatCount(getName(),
                        InspectorStrings.TOTAL_ALT_SLOT_SIZE_REQUESTS + ".rpSlot" + rpSlot);
                altSizeIdsSet = true;
                rp.setAlt_size_ids(rpSlots);
            }
            ext.setRp(rp);
        } else {
            // We can't take risk of sending request without size id so return null
            LOG.error("Dropping request as no matching RP Size ID is found for processedSlotId {}", processedSlotId);
            return null;
        }
        banner.setExt(ext);
        return banner;
    }

    private Video createVideoObject() {
        LOG.debug(traceMarker, "createVideoObject");
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

        final Banner banner = new Banner();
        banner.setId(ONE);

        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(processedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            video.setW((int) dim.getWidth());
            video.setH((int) dim.getHeight());

            banner.setW((int) dim.getWidth());
            banner.setH((int) dim.getHeight());
        }
        video.setCompanionad(Lists.newArrayList(banner));
        video.setCompaniontype(VAST_COMPANION_TYPES);

        final VideoExtension ext = new VideoExtension();
        ext.setSkip(isSkippable ? 1 : 0);
        ext.setSkipdelay(5); // Default 5 secs.
        video.setExt(ext);
        return video;
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

        final LocationSource locSrc = sasParams.getLocationSource();
        if (LocationSource.LATLON == locSrc || LocationSource.BSSID_DERIVED == locSrc
                || LocationSource.VISIBLE_BSSID == locSrc) {
            geo.setType(1);
        } else if (LocationSource.CCID == locSrc || LocationSource.WIFI == locSrc
                || LocationSource.DERIVED_LAT_LON == locSrc || LocationSource.CELL_TOWER == locSrc) {
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
        user.setId(getUid(false));
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

        final List<String> blockedList = IXAdNetworkHelper.getBlocklists(sasParams, repositoryHelper, traceMarker);
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
        site.setId(sasParams.getSiteId());
        final String tempSiteUrl = wapSiteUACEntity.getSiteUrl();
        if (StringUtils.isNotEmpty(tempSiteUrl)) {
            site.setPage(tempSiteUrl);
            site.setDomain(tempSiteUrl);
        }
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
            site.setName(wapSiteUACEntity.getSiteName());
        }

        final String blindId = BaseAdNetworkHelper.getBlindedSiteId(sasParams.getSiteIncId());
        final String blindDomain = String.format(BLIND_DOMAIN_SITE_FORMAT, blindId);

        final Blind blindForSite = new Blind();
        blindForSite.setPage(blindDomain);
        blindForSite.setDomain(blindDomain);
        ext.setBlind(blindForSite);
    }

    private void setParamsForBlindSite(final Site site, final CommonExtension ext) {
        final String blindId = BaseAdNetworkHelper.getBlindedSiteId(sasParams.getSiteIncId());
        site.setId(sasParams.getSiteId());
        final String category = isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())
                ? wapSiteUACEntity.getAppType()
                : getCategories(',', false);
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
        publisher.setName("InMobi");
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
        adQuality.setSensitivity(ContentType.PERFORMANCE == sasParams.getSiteContentType() ? "low" : "high");
        return adQuality;
    }

    private Transparency createTransparency() {
        final Transparency transparency = new Transparency();
        if (isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_SENT_AS_TRANSPARENT);
            transparency.setBlind(0);
            if (null != wapSiteUACEntity.getBlindList()) {
                transparency.setBlindbuyers(wapSiteUACEntity.getBlindList());
            } else if (!globalBlindFromConfig.isEmpty() && !globalBlindFromConfig.get(0).isEmpty()) {
                final List<Integer> globalBlind = Lists.newArrayList();
                for (final String gbConfig : globalBlindFromConfig) {
                    globalBlind.add(Integer.valueOf(gbConfig));
                }
                transparency.setBlindbuyers(globalBlind);
            }
        } else {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.IX_SENT_AS_BLIND);
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

        final List<String> blockedList = IXAdNetworkHelper.getBlocklists(sasParams, repositoryHelper, traceMarker);
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

    public String getAppBundleId() {
        if (null != wapSiteUACEntity) {
            if (wapSiteUACEntity.isIOS()) {
                return StringUtils.defaultIfEmpty(wapSiteUACEntity.getMarketId(), sasParams.getAppBundleId());
            } else {
                return StringUtils.defaultIfEmpty(sasParams.getAppBundleId(), wapSiteUACEntity.getMarketId());
            }
        } else {
            return sasParams.getAppBundleId();
        }
    }

    private void setParamsForTransparentApp(final App app, final CommonExtension ext) {
        app.setId(sasParams.getSiteId());
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteUrl())) {
            app.setStoreurl(wapSiteUACEntity.getSiteUrl());
        }

        final String marketId;
        if (wapSiteUACEntity.isIOS()) {
            marketId = StringUtils.defaultIfEmpty(wapSiteUACEntity.getMarketId(), sasParams.getAppBundleId());
        } else {
            marketId = StringUtils.defaultIfEmpty(sasParams.getAppBundleId(), wapSiteUACEntity.getMarketId());
        }
        if (StringUtils.isNotEmpty(marketId)) {
            app.setBundle(marketId);
        }

        // Set either of title or Name, giving priority to title
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getAppTitle())) {
            app.setName(wapSiteUACEntity.getAppTitle());
        } else if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
            app.setName(wapSiteUACEntity.getSiteName());
        }

        final String blindId = BaseAdNetworkHelper.getBlindedSiteId(sasParams.getSiteIncId());
        final String blindBundle = String.format(BLIND_BUNDLE_APP_FORMAT, blindId);
        final Blind blindForApp = new Blind();
        blindForApp.setBundle(blindBundle);
        ext.setBlind(blindForApp);
    }

    private void setParamsForBlindApp(final App app, final CommonExtension ext) {
        app.setId(sasParams.getSiteId());
        final String category = isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())
                ? wapSiteUACEntity.getAppType()
                : getCategories(',', false);
        app.setName(category);

        final String blindId = BaseAdNetworkHelper.getBlindedSiteId(sasParams.getSiteIncId());
        final String blindBundle = String.format(BLIND_BUNDLE_APP_FORMAT, blindId);
        app.setBundle(blindBundle);

        final Blind blindForApp = new Blind();
        blindForApp.setBundle(blindBundle);
        ext.setBlind(blindForApp);
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

        final ConnectionType sasParamConnectionType = sasParams.getConnectionType();
        if (null != sasParamConnectionType) {
            device.setConnectiontype(sasParamConnectionType.getValue());
        } else {
            device.setConnectiontype(ConnectionType.UNKNOWN.getValue());
        }
        // lmt = 0 is false (i.e. lmt not enabled and is default). lmt = 1 is true (i.e. lmt is enabled)
        device.setLmt(casInternalRequestParameters.isTrackingAllowed() ? 0 : 1);

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
            if (StringUtils.isNotEmpty(id = getUidIFA(false))) {
                // Set to UIDIFA for IOS Device
                device.setIfa(id);
            } else if (StringUtils.isNotEmpty(id = getGPID(false))) {
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

    private String replaceIXMacros(String url) {
        url = url.replaceAll(RTBCallbackMacros.AUCTION_ID_INSENSITIVE, bidResponse.getId());
        url = url.replaceAll(RTBCallbackMacros.AUCTION_CURRENCY_INSENSITIVE, USD);
        url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_ENCRYPTED_INSENSITIVE, encryptedBid);
        url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_INSENSITIVE, Double.toString(secondBidPriceInUsd));

        if (null != bidResponse.getBidid()) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_BID_ID_INSENSITIVE, bidResponse.getBidid());
        }
        if (null != seatId) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_SEAT_ID_INSENSITIVE, seatId);
        }
        if (null != dealId) {
            url = url.replaceAll(RTBCallbackMacros.DEAL_ID_INSENSITIVE, "&d-id=" + dealId);
        } else {
            url = url.replaceAll(RTBCallbackMacros.DEAL_ID_INSENSITIVE, StringUtils.EMPTY);
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
        final String httpRequestMethod = GET.equalsIgnoreCase(ixMethod) ? GET : POST;
        final String authStr = userName + ":" + password;
        final String authEncoded = new String(Base64.encodeBase64(authStr.getBytes(CharsetUtil.UTF_8)));
        LOG.debug(traceMarker, "INSIDE GET NING REQUEST");
        return new RequestBuilder(httpRequestMethod).setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE_VALUE)
                .setHeader(HttpHeaders.Names.CONTENT_ENCODING, UTF_8)
                .setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(body.length))
                .setHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + authEncoded)
                .setHeader(HttpHeaders.Names.HOST, uri.getHost()).setBody(body).setBodyEncoding(UTF_8);
    }

    @Override
    public URI getRequestUri() throws URISyntaxException {
        final StringBuilder url = new StringBuilder(host);
        if (GET.equalsIgnoreCase(ixMethod)) {
            url.append('?').append(urlArg).append('=');
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
            buildInmobiAdTracker();
            responseContent = DEFAULT_EMPTY_STRING;
            adStatus = AdStatus.AD.name();

            if (isNativeRequest()) {
                nativeAdBuilding();
            } else if (isPureVastRequest) {
                pureVastAdBuilding();
            } else if (isVideoRequest || isRewardedVideoRequest) {
                videoAdBuilding();
            } else if (isCAURequest()) {
                cauAdBuilding();
            } else {
                bannerAdBuilding();
            }
        }
        LOG.debug(traceMarker, "response content length is {}", responseContent.length());
        LOG.debug(traceMarker, "response content is {}", responseContent);
    }

    protected boolean isSproutAd() {
        final String adm = getAdMarkUp();
        boolean isSproutAd = false;
        if (null != adm) {
            try {
                isSproutAd = adm.matches(sproutUniqueIdentifierRegex);
            } catch (final Exception ignored) {
                isSproutAd = false;
            }
        }
        return isSproutAd;
    }

    protected boolean updateRPAccountInfo(final String rpAccIdStr) {
        LOG.debug(traceMarker, "Inside updateRPAccountInfo");
        String inmobiAccId = unknownAdvertiserId;
        try {
            final Long rpAccId = Long.parseLong(rpAccIdStr);
            // Get Inmobi account id for the DSP on Rubicon side
            final IXAccountMapEntity ixAccountMapEntity = repositoryHelper.queryIXAccountMapRepository(rpAccId);
            if (ixAccountMapEntity != null && ixAccountMapEntity.getInmobiAccountId() != null) {
                inmobiAccId = ixAccountMapEntity.getInmobiAccountId();
            }
        } catch (final Exception exp) {
            if (LOG.isInfoEnabled()) {
                final String msg = String.format("Error in updateRPAccountInfo for rpAccIdStr %s, msg ->%s", rpAccIdStr,
                        exp.getMessage());
                LOG.info(traceMarker, msg, exp);
            }
        }
        if (unknownAdvertiserId.equals(inmobiAccId)) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.UNKNOWN_ADV_ID);
        }

        // Get collection of Channel Segment Entities for the particular Inmobi account id
        final ChannelAdGroupRepository channelAdGroupRepository = repositoryHelper.getChannelAdGroupRepository();
        if (null == channelAdGroupRepository) {
            LOG.debug("Channel AdGroup Repository is null.");
            return false;
        }

        final Collection<ChannelSegmentEntity> adGroupMap = channelAdGroupRepository.getEntities(inmobiAccId);
        if (null == adGroupMap || adGroupMap.isEmpty()) {
            // If collection is empty
            LOG.error("Channel Segment Entity collection for Rubicon DSP is empty: RP Acc id:{}, inmobi account id:{}",
                    rpAccIdStr, inmobiAccId);
            return false;
        } else {
            // Else picking up the first channel segment entity and assuming that to be the correct entity
            dspChannelSegmentEntity = adGroupMap.iterator().next();

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
        LOG.debug(traceMarker, "bannerAdBuilding");
        final VelocityContext velocityContext = new VelocityContext();
        final String beaconUrl = getBeaconUrl();
        String admContent = getAdMarkUp();

        if (altSizeIdsSet) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_ALT_SLOT_SIZE_RESPONSES);
        }

        if (WAP.equalsIgnoreCase(sasParams.getSource())) {
            if (isSproutAd()) {
                sproutAdNotSupported(WAP);
                return;
            }
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, admContent);
        } else {
            if (isSproutAd()) {
                if (!isSproutSupported) {
                    sproutAdNotSupported("SDK<370");
                    return;
                }
                LOG.debug(traceMarker, "Sprout Ad Received");
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VALID_SPROUT_RESPONSES);
                admContent = IXAdNetworkHelper.replaceSproutMacros(admContent, casInternalRequestParameters, sasParams,
                        isCoppaSet, getClickUrl(), beaconUrl);
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

        final String partnerWinUrl = getWinUrl();
        if (StringUtils.isNotEmpty(partnerWinUrl)) {
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, partnerWinUrl);
        }

        velocityContext.put(VelocityTemplateFieldConstants.IM_BEACON_URL, beaconUrl);
        velocityContext.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());

        // Viewability Tracker
        if (StringUtils.isNotBlank(viewabilityTracker)) {
            velocityContext.put(VelocityTemplateFieldConstants.VIEWABILITY_TRACKER, viewabilityTracker);
        }
        velocityContext.put(VelocityTemplateFieldConstants.VIEWABILE, hasViewabilityDeal);

        try {
            responseContent = Formatter.getResponseFromTemplate(TemplateType.IX_HTML, velocityContext, sasParams, null);
        } catch (final Exception e) {
            adStatus = NO_AD;
            responseContent = DEFAULT_EMPTY_STRING;
            LOG.info(traceMarker, "Some exception is caught while filling the velocity template for partner: {} {}",
                    advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.BANNER_PARSE_RESPONSE_EXCEPTION);
        }
    }

    private void sproutAdNotSupported(final String where) {
        LOG.debug(traceMarker, "Sprout Ads not supported on {}", where);
        InspectorStats.incrementStatCount(getName(), InspectorStrings.DROPPED_AS_SPROUT_ADS_ARE_NOT_SUPPORTED + where);
        responseContent = DEFAULT_EMPTY_STRING;
        adStatus = NO_AD;
        return;
    }

    private void pureVastAdBuilding() {
        LOG.debug(traceMarker, "vastAdBuilding");
        try {
            responseContent = IXAdNetworkHelper.pureVastAdBuilding(getAdMarkUp(), getBeaconUrl(), getClickUrl());
        } catch (final Exception e) {
            adStatus = NO_AD;
            responseContent = DEFAULT_EMPTY_STRING;
            LOG.info(traceMarker, "Some exception is caught while adding Inmobi Ad Tracker to VAST XML:{} " + "{}",
                    advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.PURE_VAST_PARSE_RESPONSE_EXCEPTION);
        }
    }

    private void videoAdBuilding() {
        LOG.debug(traceMarker, "videoAdBuilding");
        if (isSproutAd()) {
            sproutAdNotSupported("VAST");
            return;
        }
        try {
            responseContent = IXAdNetworkHelper.videoAdBuilding(templateConfiguration.getTemplateTool(), sasParams,
                    repositoryHelper, processedSlotId, getBeaconUrl(), getClickUrl(), getAdMarkUp(), getWinUrl(),
                    isRewardedVideoRequest, viewabilityTracker, hasViewabilityDeal);
        } catch (final Exception e) {
            adStatus = NO_AD;
            responseContent = DEFAULT_EMPTY_STRING;
            LOG.info(traceMarker, "Some exception is caught while filling the velocity template for " + "partner:{} {}",
                    advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.VIDEO_PARSE_RESPONSE_EXCEPTION);
        }
    }

    private void cauAdBuilding() {
        LOG.debug(traceMarker, "cauAdBuilding");
        if (isSproutAd()) {
            sproutAdNotSupported("CAU");
            return;
        }
        InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_CAU_RESPONSES);
        try {
            responseContent = IXAdNetworkHelper.cauAdBuilding(sasParams, matchedCAU, getBeaconUrl(), getClickUrl(),
                    getAdMarkUp(), getWinUrl(), viewabilityTracker, hasViewabilityDeal);
        } catch (final Exception e) {
            adStatus = NO_AD;
            responseContent = DEFAULT_EMPTY_STRING;
            LOG.info(traceMarker, "Some exception is caught while filling the CAU template for partner:{} {}",
                    advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.CAU_PARSE_RESPONSE_EXCEPTION);
        }
    }

    private String getWinUrl() {
        String winUrl = null;
        LOG.debug(traceMarker, "nurl is {}", nurl);

        if (StringUtils.isNotEmpty(nurl)) {
            LOG.debug(traceMarker, "inside wn from nurl");
            winUrl = nurl;
        }
        return winUrl;
    }

    protected void nativeAdBuilding() {
        LOG.debug(traceMarker, "nativeAdBuilding");
        InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_NATIVE_RESPONSES);
        try {
            final App app = bidRequest.getApp();
            final com.inmobi.template.context.App templateContext = IXAdNetworkHelper
                    .validateAndBuildTemplateContext(nativeObj, mandatoryAssetMap, nonMandatoryAssetMap, impressionId);
            if (null == templateContext) {
                adStatus = TERM;
                responseContent = DEFAULT_EMPTY_STRING;
                LOG.debug(traceMarker, "Native Ad Building failed as native object failed validation");
                // Raising exception in parse response if native object failed validation.
                InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_PARSE_RESPONSE_EXCEPTION);
                return;
            }
            final Map<String, String> params = new HashMap<>();
            params.put("beaconUrl", getBeaconUrl());
            params.put("clickUrl", getClickUrl());
            params.put("winUrl",
                    getBeaconUrl() + RTBCallbackMacros.WIN_BID_GET_PARAM + RTBCallbackMacros.DEAL_GET_PARAM);
            params.put("appId", app.getId());
            params.put("placementId", String.valueOf(sasParams.getPlacementId()));
            params.put("nUrl", nurl);
            params.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());
            responseContent = nativeResponseMaker.makeIXResponse(templateContext, params);
        } catch (final Exception e) {
            adStatus = TERM;
            responseContent = DEFAULT_EMPTY_STRING;
            LOG.error("Some exception is caught while filling the native template for placementId = {}, advertiser = {}"
                    + ", exception = {}", sasParams.getPlacementId(), advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_VM_TEMPLATE_ERROR);
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
        } catch (final Exception jpe) {
            LOG.error(traceMarker, "Deserialisation failed as response does not conform to gson contract: {}",
                    jpe.getMessage());
            InspectorStats.incrementStatCount(getName(), InspectorStrings.RESPONSE_CONTRACT_NOT_HONOURED);
            // TODO: Figure out why adStatus is always reverted back to NO_AD
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
            final String responseReason = handleResponseStatusCode(responseStatusCode);
            LOG.debug("NO_AD from RP with responseStatusCode={}, and reason={}", responseStatusCode, responseReason);
            /* Commenting to reduce stats, un-comment on need basis */
            // InspectorStats.incrementStatCount(getName(), responseReason);
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
    private boolean deserializeResponse(final String response) {
        // In-case of unexpected error in deserializeResponse(), the adStatus is set to TERM. Otherwise, it is already
        // set as NO_AD.
        LOG.debug("Started deserialising response from RP");

        if (StringUtils.isEmpty(response)) {
            return false;
        }

        if (!conformsToContract(response) || responseHasNonZeroStatusCode() || !conformsToValidBidStructure()) {
            return false;
        }

        // Configuring of adapter from bid response
        final SeatBid seatBid = bidResponse.getSeatbid().get(0);
        final Bid bid = seatBid.getBid().get(0);
        dspId = seatBid.getBuyer();
        seatId = seatBid.getSeat();

        responseAuctionId = bidResponse.getId();
        responseBidObjCount = seatBid.getBid().size();
        responseImpressionId = bid.getImpid();
        dealId = bid.getDealid();
        isExternalPersonaDeal = false;
        isAgencyRebateDeal = false;

        // bidderCurrency is set to USD by default
        bidPriceInLocal = bidPriceInUsd = originalBidPriceInUsd = bid.getPrice();

        if (dealId != null) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_DEAL_RESPONSES);
            InspectorStats.incrementStatCount(InspectorStrings.PACKAGE_AND_DEAL_STATS,
                    InspectorStrings.IX_DEAL_RESPONSES_FOR_ID + dealId);
            setDealRelatedMetadata();
        }
        nurl = bid.getNurl();

        // creativeId = bid.getCrid(); // Replaced with aqid
        aqid = bid.getAqid();
        adjustbid = bid.getAdjustbid();

        adm = bid.getAdm();
        if (null != bid.getAdmobject()) {
            nativeObj = bid.getAdmobject().getNativeObj();
        }
        if (null != nativeObj) {
            // TODO: who consumes sampleAdvertiser log data
            // Done to maintain consistency in logging (See sampleAdvertiserLogging)
            adm = nativeObj.toString();
        }
        
        // Fetch bid.ext.rp.advid.
        if (null != bid.getExt() && null != bid.getExt().getRp()) {
            final RubiconExtension rp = bid.getExt().getRp();
            advId = rp.getAdvid();
        }

        // For video requests, validate that a valid XML is received.
        if (isVideoRequest || isRewardedVideoRequest || isPureVastRequest) {
            if (IXAdNetworkHelper.isAdmValidXML(getAdMarkUp())) {
                final String statName = isVideoRequest
                        ? InspectorStrings.TOTAL_VAST_VIDEO_RESPONSES
                        : isPureVastRequest
                                ? InspectorStrings.TOTAL_PURE_VAST_RESPONSE
                                : InspectorStrings.TOTAL_REWARDED_VAST_VIDEO_RESPONSES;
                InspectorStats.incrementStatCount(getName(), statName);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VIDEO_RESPONSES);
            } else {
                InspectorStats.incrementStatCount(getName(), InspectorStrings.INVALID_VIDEO_RESPONSE_COUNT);
                adStatus = TERM;
                return false;
            }
        }

        if (updateRPAccountInfo(dspId)) {
            LOG.debug(traceMarker, "Response successfully deserialised");
            return true;
        } else {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.DROPPED_INVALID_DSP_ID);
            adStatus = TERM;
            return false;
        }
    }

    private String handleResponseStatusCode(final Integer responseStatusCode) {
        // TODO: Remove Magic numbers
        switch (responseStatusCode) {
            case 3:
                return InspectorStrings.IX_INVALID_REQUEST;
            case 10:
                return InspectorStrings.IX_NO_MATCH;
            case 15:
                return InspectorStrings.IX_REFERRER_NOT_ALLOWED;
            case 16:
                return InspectorStrings.IX_INVENTORY_IDENTIFIER_INVALID;
            case 17:
                return InspectorStrings.IX_SUSPECTED_SPIDER;
            case 18:
                return InspectorStrings.IX_SUSPECTED_BOTNET;
            case 21:
                return InspectorStrings.IX_REFERRER_BLOCKED;
            case 27:
                return InspectorStrings.IX_NOT_AUTHORIZED;
            case 32:
                return InspectorStrings.IX_PROXY_BID_WINS;
            default:
                return InspectorStrings.IX_OTHER_ERRORS;
        }
    }


    protected void setDealRelatedMetadata() {
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
        // Setting deal floor
        dealFloor = matchedPackageEntity.getDealFloors().size() > indexOfDealId
                ? matchedPackageEntity.getDealFloors().get(indexOfDealId)
                : 0.0;

        // Setting used csids and data vendor cost
        dataVendorCost = matchedPackageEntity.getDataVendorCost();
        if (dataVendorCost > 0.0) {
            isExternalPersonaDeal = true;
            usedCsIds = new HashSet<>();
            final Set<Set<Integer>> csIdInPackages = matchedPackageEntity.getDmpFilterSegmentExpression();
            for (final Set<Integer> smallSet : csIdInPackages) {
                for (final Integer csIdInSet : smallSet) {
                    if (sasParams.getCsiTags().contains(csIdInSet)) {
                        usedCsIds.add(csIdInSet);
                    }
                }
            }
        }

        // TODO: Clean up trump logic in ResponseSender
        final String dealType = matchedPackageEntity.getAccessTypes().size() > indexOfDealId
                ? matchedPackageEntity.getAccessTypes().get(indexOfDealId)
                : RIGHT_TO_FIRST_REFUSAL_DEAL;
        if (RIGHT_TO_FIRST_REFUSAL_DEAL.contentEquals(dealType)) {
            isTrumpDeal = true;
        }

        hasViewabilityDeal = matchedPackageEntity.isViewable();
        if (hasViewabilityDeal) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_VIEWABILITY_RESPONSES);
            LOG.debug("Viewability enabled for this request.");
        }

        if (matchedPackageEntity.getViewabilityTrackers().size() > indexOfDealId) {
            viewabilityTracker = matchedPackageEntity.getViewabilityTrackers().get(indexOfDealId);
            if (StringUtils.isNotBlank(viewabilityTracker)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Viewability Trackers detected.");
                    LOG.debug("Viewability Trackers before substitution: {}", viewabilityTracker);
                }
                viewabilityTracker = IXAdNetworkHelper.replaceViewabilityTrackerMacros(viewabilityTracker,
                        casInternalRequestParameters, sasParams);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Viewability Trackers after substitution: {}", viewabilityTracker);
                }
                InspectorStats.incrementStatCount(getName(),
                        InspectorStrings.TOTAL_RESPONSES_WITH_THIRD_PARTY_VIEWABILITY_TRACKERS);
            }
        }

        // Applying if agency rebate is applicable
        agencyRebatePercentage = matchedPackageEntity.getAgencyRebatePercentages().size() > indexOfDealId
                ? matchedPackageEntity.getAgencyRebatePercentages().get(indexOfDealId)
                : null;
        if (null != agencyRebatePercentage) {
            if (agencyRebatePercentage <= 0 || agencyRebatePercentage > 100) {
                agencyRebatePercentage = null;
                LOG.debug("Agency rebate percentage out of range. DealId: {}", dealId);
            }
        }

        // Setting agency/seat Id if not present in RP response
        if (null != agencyRebatePercentage) {
            isAgencyRebateDeal = true;
            final String dealMetaDataSeatId = matchedPackageEntity.getRpAgencyIds().size() > indexOfDealId
                    && null != matchedPackageEntity.getRpAgencyIds().get(indexOfDealId)
                            ? String.valueOf(matchedPackageEntity.getRpAgencyIds().get(indexOfDealId))
                            : null;
            if (StringUtils.isEmpty(seatId)) {
                InspectorStats.incrementStatCount(getName(),
                        InspectorStrings.AGENCY_ID_MISSING_IN_REBATE_DEAL_RESPONSE);
                seatId = dealMetaDataSeatId;
                LOG.info(
                        "Agency Id missing in Agency Rebate Deal Response; replacing with the deal metadata agency id. DealId: {}",
                        dealId);
                if (StringUtils.isEmpty(seatId)) {
                    // This has been enforced in the UI and DB.
                    InspectorStats.incrementStatCount(getName(),
                            InspectorStrings.AGENCY_ID_CANNOT_BE_DETERMINED_IN_REBATE_DEAL_RESPONSE);
                    LOG.error("Agency Id cannot be determined for Agency Rebate Deal.");
                    agencyRebatePercentage = null;
                    isAgencyRebateDeal = false;
                }
            } else if (!seatId.equals(dealMetaDataSeatId)) {
                InspectorStats.incrementStatCount(getName(),
                        InspectorStrings.AGENCY_ID_MISMATCH_IN_REBATE_DEAL_RESPONSE);
                LOG.error("Agency Id mismatch between response and deal metadata. DealId: {}, ReceivedSeatId: {}, "
                        + "DealMetadataSeatId: {}", dealId, seatId, dealMetaDataSeatId);
                agencyRebatePercentage = null;
                isAgencyRebateDeal = false;
            }
        }

        if (isAgencyRebateDeal) {
            // Setting bidPriceInLocal and bidPriceInUsd to the net bid.
            bidPriceInLocal = bidPriceInUsd = originalBidPriceInUsd * (1.0 - agencyRebatePercentage / 100.0);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_AGENCY_REBATE_DEAL_RESPONSES);
            LOG.info(traceMarker, "Agency Rebate Applied, dealId: {}, agencyId: {}, originalBid: {}, newBid: {}",
                    dealId, seatId, originalBidPriceInUsd, bidPriceInUsd);
        }
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
    protected void overrideInmobiAdTracker(final InmobiAdTrackerBuilder builder) {
        if (builder instanceof DefaultLazyInmobiAdTrackerBuilder) {
            final DefaultLazyInmobiAdTrackerBuilder trackerBuilder = (DefaultLazyInmobiAdTrackerBuilder) builder;

            // Setting agency rebate
            if (isAgencyRebateDeal) {
                trackerBuilder.setAgencyRebatePercentage(agencyRebatePercentage);
            }
            if (CollectionUtils.isNotEmpty(usedCsIds) && null != dataVendorCost && dataVendorCost > 0) {
                trackerBuilder.setMatchedCsids(ImmutableList.copyOf(usedCsIds));
                trackerBuilder.setEnrichmentCost(dataVendorCost);
            }

            if (isNativeRequest && null != templateEntity) {
                trackerBuilder.setNativeTemplateId(templateEntity.getId());
            }

            trackerBuilder.setChargedBid(originalBidPriceInUsd);
        }
    }

    @Override
    public void setSecondBidPrice(final Double price) {
        secondBidPriceInUsd = price;
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
    public void setEncryptedBid(final String encryptedBid) {
        this.encryptedBid = encryptedBid;
    }

    @Override
    public double getSecondBidPriceInUsd() {
        return secondBidPriceInUsd;
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
