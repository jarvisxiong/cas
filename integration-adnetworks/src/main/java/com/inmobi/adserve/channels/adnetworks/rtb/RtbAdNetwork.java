package com.inmobi.adserve.channels.adnetworks.rtb;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.MD5;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.SHA1;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.UTF_8;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.attribute.BAttrNativeType;
import com.inmobi.adserve.channels.api.attribute.BTypeNativeAttributeType;
import com.inmobi.adserve.channels.api.attribute.SuggestedNativeAttributeType;
import com.inmobi.adserve.channels.api.natives.NativeBuilder;
import com.inmobi.adserve.channels.api.natives.NativeBuilderFactory;
import com.inmobi.adserve.channels.api.natives.RtbdNativeBuilderFactory;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.api.template.NativeTemplateAttributeFinder;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.entity.CcidMapEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.casthrift.rtb.App;
import com.inmobi.casthrift.rtb.AppExt;
import com.inmobi.casthrift.rtb.AppStore;
import com.inmobi.casthrift.rtb.Banner;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidRequest;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.Device;
import com.inmobi.casthrift.rtb.Geo;
import com.inmobi.casthrift.rtb.Impression;
import com.inmobi.casthrift.rtb.ImpressionExtensions;
import com.inmobi.casthrift.rtb.Native;
import com.inmobi.casthrift.rtb.Site;
import com.inmobi.casthrift.rtb.User;
import com.inmobi.types.DeviceType;
import com.inmobi.types.LocationSource;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic RTB adapter.
 *
 * @author Devi Chand(devi.chand@inmobi.com)
 * @author ritwik.kumar
 */
public class RtbAdNetwork extends BaseAdNetworkImpl {
    public static ImpressionCallbackHelper impressionCallbackHelper;
    public static final List<String> CURRENCIES_SUPPORTED = new ArrayList<>(Arrays.asList(USD, "CNY", "JPY", "EUR",
            "KRW", "RUB"));
    private static final List<String> BLOCKED_ADVERTISER_LIST = new ArrayList<>(Arrays.asList("king.com",
            "supercell.net", "paps.com", "fhs.com", "china.supercell.com", "supercell.com"));

    private static final Logger LOG = LoggerFactory.getLogger(RtbAdNetwork.class);
    private static final int AUCTION_TYPE = 2;
    private static final String X_OPENRTB_VERSION = "x-openrtb-version";
    private static final int DEVICE_TYPE_PHONE = 4;
    private static final int DEVICE_TYPE_TABLET = 5;
    private static List<String> image_mimes = Arrays.asList("image/jpeg", "image/gif", "image/png");
    private static List<Integer> fsBlockedAttributes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13, 14, 15, 16);
    private static List<Integer> performanceBlockedAttributes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
            13, 14, 15, 16);

    private static final String FAMILY_SAFE_RATING = GlobalConstant.ONE;
    private static final String PERFORMANCE_RATING = GlobalConstant.ZERO;
    private static final String RATING_KEY = "fs";

    private static final String BLIND_BUNDLE_APP_FORMAT = "com.ix.%s";
    private static final String BLIND_DOMAIN_SITE_FORMAT = "http://www.ix.com/%s";
    private static final String BLIND_STORE_URL_FORMAT = "http://www.ix.com/%s";

    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;

    @Inject
    private static NativeTemplateAttributeFinder nativeTemplateAttributeFinder;

    @Inject
    @RtbdNativeBuilderFactory
    private static NativeBuilderFactory nativeBuilderfactory;

    @Inject
    private static NativeResponseMaker nativeResponseMaker;

    @Getter
    @Setter
    BidRequest bidRequest;

    @Setter
    BidResponse bidResponse;

    @Setter
    private String urlArg;
    private final String rtbMethod;
    private final String rtbVer;
    @Getter
    @Setter
    private String callbackUrl;
    private double bidPriceInUsd;
    private double bidPriceInLocal;

    private final boolean wnRequired;
    private final int tmax = 200;
    private boolean templateWN = true;
    private final String advertiserId;
    private final String advertiserName;
    private double secondBidPriceInUsd = 0;
    private double secondBidPriceInLocal = 0;
    private String bidRequestJson = DEFAULT_EMPTY_STRING;
    private String encryptedBid;
    private boolean siteBlinded;
    private String responseSeatId;
    private String responseImpressionId;
    private String responseAuctionId;
    private String creativeId;
    private String nurl;
    private String sampleImageUrl;
    private List<String> advertiserDomains;
    private List<Integer> creativeAttributes;
    private boolean logCreative = false;
    private String adm;
    private String bidderCurrency;
    private final List<String> blockedAdvertisers = Lists.newArrayList();
    private WapSiteUACEntity wapSiteUACEntity;
    private boolean isWapSiteUACEntity = false;
    private NativeAdTemplateEntity templateEntity;

    @Override
    protected AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClientProvider.getRtbAsyncHttpClient();
    }

    public RtbAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String host,
            final String advertiserName, final boolean templateWinNotification) {
        super(baseRequestHandler, serverChannel);
        advertiserId = config.getString(advertiserName + ".advertiserId");
        urlArg = config.getString(advertiserName + ".urlArg");
        rtbVer = config.getString(advertiserName + ".rtbVer", "2.0");
        callbackUrl = config.getString(advertiserName + ".wnUrlback");
        rtbMethod = config.getString(advertiserName + ".rtbMethod");
        wnRequired = config.getBoolean(advertiserName + ".isWnRequired");
        siteBlinded = config.getBoolean(advertiserName + ".siteBlinded");
        this.clientBootstrap = clientBootstrap;
        this.host = host;
        isRtbPartner = true;
        this.advertiserName = advertiserName;
        templateWN = templateWinNotification;
        isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", true);
        isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", false);
        blockedAdvertisers.addAll(BLOCKED_ADVERTISER_LIST);
        bidderCurrency = config.getString(advertiserName + ".currency", USD);
    }

    @Override
    protected boolean configureParameters() {
        LOG.debug(traceMarker, "inside configureParameters of RTB");
        if (!checkIfBasicParamsAvailable()) {
            LOG.info(traceMarker, "Configure parameters inside rtb returned false {}, Basic Params Not Available",
                    advertiserName);
            return false;
        }

        if (sasParams.getWapSiteUACEntity() != null) {
            wapSiteUACEntity = sasParams.getWapSiteUACEntity();
            isWapSiteUACEntity = true;
        }

        // Creating site/app Object
        App app = null;
        Site site = null;
        if (null != sasParams.getSource() && null != sasParams.getSiteId()) {
            if (WAP.equalsIgnoreCase(sasParams.getSource())) {
                // Creating Site object
                site = createSiteObject();
            } else {
                // Creating App object
                app = createAppObject();
            }
        }

        // Creating Geo Object for device Object
        final Geo geo = createGeoObject();
        // Creating Banner object
        final Banner banner = createBannerObject();
        // Creating Device Object
        final Device device = createDeviceObject(geo);
        // Creating User Object
        final User user = createUserObject();
        // Creating Impression Object
        final List<Impression> impresssionlist = new ArrayList<Impression>();
        final Impression impression = createImpressionObject(banner);
        if (null == impression) {
            LOG.info(traceMarker, "Configure parameters inside rtb returned false {}, Impression Obj is null",
                    advertiserName);
            return false;
        }
        impresssionlist.add(impression);

        // Creating BidRequest Object using unique auction id per auction
        final boolean flag = createBidRequestObject(impresssionlist, site, app, user, device);
        if (!flag) {
            return false;
        }

        // Serializing the bidRequest Object
        return serializeBidRequest();
    }

    private boolean checkIfBasicParamsAvailable() {
        if (null == casInternalRequestParameters || null == sasParams) {
            LOG.debug(traceMarker, "casInternalRequestParams or sasParams cannot be null");
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
        return isNativeRequest ? isNativeResponseSupported : isHTMLResponseSupported;
    }

    private boolean createBidRequestObject(final List<Impression> impresssionlist, final Site site, final App app,
            final User user, final Device device) {
        bidRequest = new BidRequest(casInternalRequestParameters.getAuctionId(), impresssionlist);
        bidRequest.setTmax(tmax);
        bidRequest.setAt(AUCTION_TYPE);
        bidRequest.setCur(Collections.<String>emptyList());
        final List<String> seatList = new ArrayList<String>();
        seatList.add(advertiserId);
        bidRequest.setWseat(seatList);
        final HashSet<String> bCatSet = new HashSet<String>();

        if (null != casInternalRequestParameters.getBlockedIabCategories()) {
            bCatSet.addAll(casInternalRequestParameters.getBlockedIabCategories());
            LOG.debug(traceMarker, "blockedCategories are {}", casInternalRequestParameters.getBlockedIabCategories());
        }
        // Setting blocked categories
        if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
            bCatSet.addAll(IABCategoriesMap.getIABCategories(IABCategoriesMap.PERFORMANCE_BLOCK_CATEGORIES));
        } else {
            bCatSet.addAll(IABCategoriesMap.getIABCategories(IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES));
        }

        final List<String> bCatList = new ArrayList<String>(bCatSet);
        bidRequest.setBcat(bCatList);

        if (null != casInternalRequestParameters.getBlockedAdvertisers()) {
            blockedAdvertisers.addAll(casInternalRequestParameters.getBlockedAdvertisers());
            LOG.debug(traceMarker, "blockedAdvertisers are {}", casInternalRequestParameters.getBlockedAdvertisers());
        }
        bidRequest.setBadv(blockedAdvertisers);


        if (site != null) {
            bidRequest.setSite(site);
        } else if (app != null) {
            bidRequest.setApp(app);
        } else {
            LOG.debug(traceMarker, "App and Site both object can not be null so returning");
            return false;
        }

        bidRequest.setDevice(device);
        bidRequest.setUser(user);
        return true;
    }

    private boolean serializeBidRequest() {
        final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        try {
            bidRequestJson = serializer.toString(bidRequest);
            if (isNativeRequest()) {
                bidRequestJson = bidRequestJson.replaceFirst("nativeObject", GlobalConstant.NATIVE_STRING);
            }
            LOG.info(traceMarker, "RTB request json is : {}", bidRequestJson);
        } catch (final TException e) {
            LOG.debug(traceMarker, "Could not create json from bidrequest for partner {}", advertiserName);
            LOG.info(traceMarker, "Configure parameters inside rtb returned false {}, exception raised {}",
                    advertiserName, e);
            return false;
        }
        return true;
    }

    private Impression createImpressionObject(final Banner banner) {
        // nullcheck for casInternalRequestParams and sasParams done while configuring adapter
        Impression impression;
        if (null != casInternalRequestParameters.getImpressionId()) {
            impression = new Impression(casInternalRequestParameters.getImpressionId());
        } else {
            LOG.info(traceMarker, "Impression id can not be null in casInternal Request Params");
            return null;
        }
        if (!isNativeRequest()) {
            impression.setBanner(banner);
        }
        impression.setSecure(sasParams.isSecureRequest() ? 1 : 0);
        impression.setBidfloorcur(bidderCurrency);
        // Set interstitial or not
        impression.setInstl(RequestedAdType.INTERSTITIAL == sasParams.getRequestedAdType() ? 1 : 0);
        forwardedBidFloor = casInternalRequestParameters.getAuctionBidFloor();
        forwardedBidGuidance = sasParams.getMarketRate();
        impression.setBidfloor(calculatePriceInLocal(forwardedBidFloor));
        LOG.debug(traceMarker, "Bid floor is {} {}", impression.getBidfloor(), impression.getBidfloorcur());

        if (null != sasParams.getSdkVersion()) {
            impression.setDisplaymanager(GlobalConstant.DISPLAY_MANAGER_INMOBI_SDK);
            impression.setDisplaymanagerver(sasParams.getSdkVersion());
        } else if (null != sasParams.getAdcode() && "JS".equalsIgnoreCase(sasParams.getAdcode())) {
            impression.setDisplaymanager(GlobalConstant.DISPLAY_MANAGER_INMOBI_JS);
        }

        if (isNativeResponseSupported && isNativeRequest()) {
            final ImpressionExtensions impExt = createNativeExtensionObject();
            if (impExt == null) {
                return null;
            }
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_NATIVE_REQUESTS);
            impression.setExt(impExt);
        }
        return impression;
    }


    private ImpressionExtensions createNativeExtensionObject() {
        // Native nat = new Native();
        // nat.setMandatory(nativeTemplateAttributeFinder.findAttribute(new MandatoryNativeAttributeType()));
        // nat.setImage(nativeTemplateAttributeFinder.findAttribute(new ImageNativeAttributeType()));
        templateEntity = repositoryHelper.queryNativeAdTemplateRepository(sasParams.getPlacementId());
        if (templateEntity == null) {
            LOG.info(traceMarker,
                    String.format("This site id %s doesn't have native template :", sasParams.getSiteId()));
            return null;
        }
        final NativeBuilder nb = nativeBuilderfactory.create(templateEntity);
        final Native nat = (Native) nb.buildNative();
        // TODO: for native currently there is no way to identify MRAID traffic/container supported by publisher.
        // if(StringUtils.isNotEmpty(sasParams.getSdkVersion())){
        // nat.api.add(3);
        // }
        nat.setBattr(nativeTemplateAttributeFinder.findAttribute(new BAttrNativeType()));
        nat.setSuggested(nativeTemplateAttributeFinder.findAttribute(new SuggestedNativeAttributeType()));
        nat.setBtype(nativeTemplateAttributeFinder.findAttribute(new BTypeNativeAttributeType()));

        final ImpressionExtensions iext = new ImpressionExtensions();
        iext.setNativeObject(nat);

        return iext;
    }

    private Banner createBannerObject() {
        final Banner banner = new Banner();
        banner.setId(casInternalRequestParameters.getImpressionId());
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            banner.setW((int) dim.getWidth());
            banner.setH((int) dim.getHeight());
        }

        // api type is always mraid
        if (StringUtils.isNotEmpty(sasParams.getSdkVersion()) && sasParams.getSdkVersion().length() > 1) {
            final List<Integer> apis = new ArrayList<>();
            apis.add(3);
            banner.setApi(apis);
        }

        // mime types a static list
        banner.setMimes(image_mimes);

        // Setting battributes
        if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
            banner.setBattr(performanceBlockedAttributes);
        } else {
            banner.setBattr(fsBlockedAttributes);
        }
        return banner;
    }

    private Geo createGeoObject() {
        final Geo geo = new Geo();
        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            geo.setLat(Double.parseDouble(String.format("%.4f", Double.parseDouble(latlong[0]))));
            geo.setLon(Double.parseDouble(String.format("%.4f", Double.parseDouble(latlong[1]))));
        }
        if (null != sasParams.getCountryCode()) {
            geo.setCountry(IABCountriesMap.getIabCountry(sasParams.getCountryCode()));
        }
        geo.setZip(casInternalRequestParameters.getZipCode());
        // Setting type of geo data
        if (LocationSource.DERIVED_LAT_LON == sasParams.getLocationSource()) {
            geo.setType(1);
        } else if (LocationSource.LATLON == sasParams.getLocationSource()) {
            geo.setType(2);
        }
        return geo;
    }

    private User createUserObject() {
        final User user = new User();
        final String gender = sasParams.getGender();
        if (StringUtils.isNotEmpty(gender)) {
            user.setGender(gender);
        }

        if (null != casInternalRequestParameters.getUid()) {
            user.setId(casInternalRequestParameters.getUid());
            user.setBuyeruid(casInternalRequestParameters.getUid());
        }
        user.setYob(getYearofBirth());
        return user;
    }

    private Site createSiteObject() {
        final Site site = new Site();
        if (isRequestBlinded()) {
            setParamsForBlindSite(site);
        } else {
            setParamsForTransparentSite(site);
        }
        if (null != sasParams.getCategories()) {
            site.setCat(IABCategoriesMap.getIABCategories(sasParams.getCategories()));
        }


        final Map<String, String> siteExtensions = new HashMap<String, String>();
        String siteRating;
        if (ContentType.FAMILY_SAFE == sasParams.getSiteContentType()) {
            // Family safe
            siteRating = FAMILY_SAFE_RATING;
        } else {
            siteRating = PERFORMANCE_RATING;
        }
        siteExtensions.put(RATING_KEY, siteRating);
        site.setExt(siteExtensions);

        return site;
    }

    private void setParamsForTransparentSite(final Site site) {
        site.setId(sasParams.getSiteId());

        final String siteUrl = wapSiteUACEntity.getSiteUrl();
        if (StringUtils.isNotEmpty(siteUrl)) {
            site.setPage(siteUrl);
            site.setDomain(siteUrl);
        }

        if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
            site.setName(wapSiteUACEntity.getSiteName());
        }
    }

    private void setParamsForBlindSite(final Site site) {
        String category = null;
        if (isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())) {
            site.setName(wapSiteUACEntity.getAppType());
        } else if ((category = getCategories(',', false)) != null) {
            site.setName(category);
        }

        final String blindId = getBlindedSiteId(sasParams.getSiteIncId());
        site.setId(blindId);

        final String blindDomain = String.format(BLIND_DOMAIN_SITE_FORMAT, blindId);
        site.setPage(blindDomain);
        site.setDomain(blindDomain);
    }

    /**
     * Blind App - Send Category as App Name <br>
     * Transparent App - Set Proper App Name if available otherwise send Category.
     * 
     * @return
     */
    private App createAppObject() {
        final App app = new App();
        if (isRequestBlinded()) {
            setParamsForBlindApp(app);
        } else {
            // will set App Name too
            setParamsForTransparentApp(app);
        }
        if (null != sasParams.getCategories()) {
            app.setCat(IABCategoriesMap.getIABCategories(sasParams.getCategories()));
        }
        String category = null;

        if (!app.isSetName() && isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())) {
            app.setName(wapSiteUACEntity.getAppType());
        } else if (!app.isSetName() && (category = getCategories(',', false)) != null) {
            app.setName(category);
        }

        // set App Ext fields
        final AppExt ext = createAppExt();
        app.setExt(ext);
        return app;
    }

    private boolean isRequestBlinded() {
        if (siteBlinded) {
            return true;
        }
        boolean isRequestBlind = true;
        if (isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            isRequestBlind = false;
        }
        return isRequestBlind;
    }


    private void setParamsForTransparentApp(final App app) {
        app.setId(sasParams.getSiteId());
        if (isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getSiteUrl())) {
            app.setStoreurl(wapSiteUACEntity.getSiteUrl());
        }

        // Set either of title or Name, giving priority to title
        if (isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppTitle())) {
            app.setName(wapSiteUACEntity.getAppTitle());
        } else if (isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
            app.setName(wapSiteUACEntity.getSiteName());
        }

        String marketId = isWapSiteUACEntity ? wapSiteUACEntity.getMarketId() : null;
        marketId = StringUtils.isNotEmpty(marketId) ? marketId : sasParams.getAppBundleId();
        if (StringUtils.isNotEmpty(marketId)) {
            app.setBundle(marketId);
        }
    }

    private void setParamsForBlindApp(final App app) {
        final String blindId = getBlindedSiteId(sasParams.getSiteIncId());
        app.setId(blindId);

        final String blindBundle = String.format(BLIND_BUNDLE_APP_FORMAT, blindId);
        app.setBundle(blindBundle);
        final String storeUrl = String.format(BLIND_STORE_URL_FORMAT, blindId);
        app.setStoreurl(storeUrl);
    }


    private AppExt createAppExt() {
        final AppExt ext = new AppExt();

        String appRating;
        if (ContentType.FAMILY_SAFE == sasParams.getSiteContentType()) {
            // Family safe
            appRating = FAMILY_SAFE_RATING;
        } else {
            appRating = PERFORMANCE_RATING;
        }
        ext.setFs(appRating);

        if (isWapSiteUACEntity) {
            final AppStore store = new AppStore();
            if (StringUtils.isNotEmpty(wapSiteUACEntity.getContentRating())) {
                store.setRating(wapSiteUACEntity.getContentRating());
            }
            if (StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())) {
                store.setCat(wapSiteUACEntity.getAppType());
            }
            if (wapSiteUACEntity.getCategories() != null && !wapSiteUACEntity.getCategories().isEmpty()) {
                store.setSeccat(wapSiteUACEntity.getCategories());
            }
            ext.setStore(store);
        }
        return ext;
    }

    private Device createDeviceObject(final Geo geo) {
        final Device device = new Device();

        if (DeviceType.TABLET == sasParams.getDeviceType()) {
            device.setDevicetype(DEVICE_TYPE_TABLET);
        } else {
            device.setDevicetype(DEVICE_TYPE_PHONE); // SmartPhones and FeaturePhones
        }
        device.setModel(sasParams.getDeviceModel());
        device.setMake(sasParams.getDeviceMake());
        device.setIp(sasParams.getRemoteHostIp());
        device.setUa(sasParams.getUserAgent());
        device.setGeo(geo);

        final CcidMapEntity ccidMapEntity = repositoryHelper.queryCcidMapRepository(sasParams.getCarrierId());
        if (null != ccidMapEntity) {
            device.setCarrier(ccidMapEntity.getCarrier());
        }

        final Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            device.setOs(HandSetOS.values()[sasParamsOsId - 1].toString());
        }

        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            device.setOsv(sasParams.getOsMajorVersion());
        }
        if (ConnectionType.WIFI == sasParams.getConnectionType()) {
            device.setConnectiontype(2);
        } else {
            device.setConnectiontype(0);
        }

        // Spec says - If “0”, then do not track Is set to false, if “1”, then do no track is set to true in browser.
        device.setDnt(casInternalRequestParameters.isTrackingAllowed() ? 0 : 1);

        // Setting platform id sha1 hashed and md5 hashed
        if (null != casInternalRequestParameters.getUidSO1()) {
            device.setDidsha1(casInternalRequestParameters.getUidSO1());
            device.setDpidsha1(casInternalRequestParameters.getUidSO1());
        } else if (null != casInternalRequestParameters.getUidO1()) {
            device.setDidsha1(casInternalRequestParameters.getUidO1());
            device.setDpidsha1(casInternalRequestParameters.getUidO1());
        } else if (null != casInternalRequestParameters.getUidMd5()) {
            device.setDidsha1(casInternalRequestParameters.getUidMd5());
            device.setDpidsha1(casInternalRequestParameters.getUidMd5());
        } else if (null != casInternalRequestParameters.getUid()) {
            device.setDidsha1(casInternalRequestParameters.getUid());
            device.setDpidsha1(casInternalRequestParameters.getUid());
        }


        if (null != casInternalRequestParameters.getIem()) {
            final String value = casInternalRequestParameters.getIem();
            if (StringUtils.isNotBlank(value)) {
                device.setDidsha1(DigestUtils.sha1Hex(value));
                device.setDidmd5(DigestUtils.md5Hex(value));
                device.setDpidsha1(null);
            }
        } else if(StringUtils.isBlank(casInternalRequestParameters.getIem())) {
            final String imei = getIMEI();
            if (imei != null) {
                device.setDidmd5(imei);
                device.setDpidmd5(imei);
                device.setDidsha1(null);
                device.setDpidsha1(null);
            }
        }
        

        final Map<String, String> deviceExtensions = getDeviceExt(device);
        final String ifa = getUidIFA(false);
        // Setting Extension for idfa
        if (StringUtils.isNotEmpty(ifa)) {
            deviceExtensions.put("idfa", ifa);
            deviceExtensions.put("idfasha1", getHashedValue(ifa, SHA1));
            deviceExtensions.put("idfamd5", getHashedValue(ifa, MD5));
        }

        final String gpId = getGPID(false);
        if (StringUtils.isNotEmpty(gpId)) {
            deviceExtensions.put("gpid", gpId);
        }
        if (StringUtils.isNotBlank(sasParams.getLanguage())) {
            device.setLanguage(sasParams.getLanguage());
        }
        return device;
    }

    private Map<String, String> getDeviceExt(final Device device) {
        final Map<String, String> deviceExtensions = new HashMap<>();
        if (null != sasParams.getAge()) {
            deviceExtensions.put("age", sasParams.getAge().toString());
        }
        if (null != sasParams.getGender()) {
            deviceExtensions.put("gender", sasParams.getGender().toString());
        }
        device.setExt(deviceExtensions);
        return deviceExtensions;
    }

    @Override
    public void impressionCallback() {
        URI uriCallBack = null;
        callbackUrl = replaceRTBMacros(callbackUrl);
        LOG.debug(traceMarker, "Callback url is : {}", callbackUrl);
        try {
            uriCallBack = new URI(callbackUrl);
        } catch (final URISyntaxException e) {
            LOG.debug(traceMarker, "error in creating uri for callback");
        }

        final StringBuilder content = new StringBuilder();
        content.append("{\"bidid\"=").append(bidResponse.bidid).append(",\"seat\"=")
                .append(bidResponse.seatbid.get(0).getSeat());
        content.append(",\"bid\"=").append(bidResponse.seatbid.get(0).bid.get(0).id).append(",\"adid\"=")
                .append(bidResponse.seatbid.get(0).bid.get(0).adid).append("}");

        final byte[] body = content.toString().getBytes(CharsetUtil.UTF_8);

        final Request ningRequest =
                new RequestBuilder().setUrl(uriCallBack.toASCIIString()).setMethod(POST)
                        .setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE_VALUE)
                        .setHeader(HttpHeaders.Names.CONTENT_ENCODING, UTF_8)
                        .setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(body.length))
                        .setHeader(HttpHeaders.Names.HOST, uriCallBack.getHost()).setBody(body).setBodyEncoding(UTF_8)
                        .build();

        final boolean callbackResult =
                impressionCallbackHelper.writeResponse(uriCallBack, ningRequest, getAsyncHttpClient());
        if (callbackResult) {
            LOG.debug(traceMarker, "Callback is sent successfully");
        } else {
            LOG.debug(traceMarker, "Could not send the callback");
        }
    }

    @SuppressWarnings("unused")
    private void setCallbackContent() {
        final StringBuilder content = new StringBuilder();
        content.append("{\"bidid\"=").append(bidResponse.bidid).append(",\"seat\"=")
                .append(bidResponse.seatbid.get(0).getSeat());
        content.append(",\"bid\"=").append(bidResponse.seatbid.get(0).bid.get(0).id).append(",\"price\"=")
                .append(secondBidPriceInUsd).append(",\"adid\"=").append(bidResponse.seatbid.get(0).bid.get(0).adid)
                .append("}");
    }

    public String replaceRTBMacros(String url) {
        url = url.replaceAll(RTBCallbackMacros.AUCTION_ID_INSENSITIVE, bidResponse.id);
        url = url.replaceAll(RTBCallbackMacros.AUCTION_CURRENCY_INSENSITIVE, bidderCurrency);

        // Condition changed from sasParams.getDst() != 6 to == 2 to avoid unnecessary IX RTBMacro Replacements
        if (2 == sasParams.getDst()) {
            LOG.info("replaceRTBMacros for DST=2, URL->{}", url);
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_ENCRYPTED_INSENSITIVE, encryptedBid);
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_INSENSITIVE, Double.toString(secondBidPriceInLocal));
        }
        if (null != bidResponse.getSeatbid().get(0).getBid().get(0).getAdid()) {
            url =
                    url.replaceAll(RTBCallbackMacros.AUCTION_AD_ID_INSENSITIVE, bidResponse.getSeatbid().get(0)
                            .getBid().get(0).getAdid());
        }
        if (null != bidResponse.bidid) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_BID_ID_INSENSITIVE, bidResponse.bidid);
        }
        if (null != bidResponse.getSeatbid().get(0).getSeat()) {
            url =
                    url.replaceAll(RTBCallbackMacros.AUCTION_SEAT_ID_INSENSITIVE, bidResponse.getSeatbid().get(0)
                            .getSeat());
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
        final String httpRequestMethod = GET.equalsIgnoreCase(rtbMethod) ? GET : POST;
        return new RequestBuilder(httpRequestMethod).setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(body).setBodyEncoding(UTF_8)
                .setHeader(X_OPENRTB_VERSION, rtbVer).setHeader(HttpHeaders.Names.HOST, uri.getHost());
    }

    @Override
    public URI getRequestUri() throws URISyntaxException {
        final StringBuilder url = new StringBuilder(host);
        if (GET.equalsIgnoreCase(rtbMethod)) {
            url.append('?').append(urlArg).append('=');
        }
        LOG.debug(traceMarker, "{} url is {}", getName(), url.toString());
        return URI.create(url.toString());
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        adStatus = NO_AD;
        LOG.debug(traceMarker, "response is {}", response);
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
                adStatus = NO_AD;
                responseContent = DEFAULT_EMPTY_STRING;
                statusCode = 500;
                LOG.info(traceMarker, "Error in parsing rtb response");
                return;
            }

            buildInmobiAdTracker();
            adStatus = AD_STRING;
            if (isNativeRequest()) {
                nativeAdBuilding();
            } else {
                bannerAdBuilding();
            }
        }
        LOG.debug(traceMarker, "response length is {}", responseContent.length());
    }


    private void bannerAdBuilding() {
        final VelocityContext velocityContext = new VelocityContext();
        final String beaconUrl = getBeaconUrl();
        String admContent = getAdMarkUp();

        final int admSize = admContent.length();
        if (!templateWN) {
            final String winUrl = beaconUrl + RTBCallbackMacros.WIN_BID_GET_PARAM;
            admContent = admContent.replace(RTBCallbackMacros.AUCTION_WIN_URL, winUrl);
        }
        final int admAfterMacroSize = admContent.length();

        if (WAP.equalsIgnoreCase(sasParams.getSource())) {
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, admContent);
        } else {
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, MRAID + admContent);
            if (StringUtils.isNotBlank(sasParams.getImaiBaseUrl())) {
                velocityContext.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());
            }
        }
        // Checking whether to send win notification
        LOG.debug(traceMarker, "isWinRequired is {} and winfromconfig is {}", wnRequired, callbackUrl);
        final String partnerWinUrl = getPartnerWinUrl();
        if (StringUtils.isNotEmpty(partnerWinUrl)) {
            velocityContext.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, partnerWinUrl);
        }

        if (templateWN || admAfterMacroSize == admSize) {
            velocityContext.put(VelocityTemplateFieldConstants.IM_BEACON_URL, beaconUrl);
        }
        try {
            responseContent =
                    Formatter.getResponseFromTemplate(TemplateType.RTB_HTML, velocityContext, sasParams, null);
        } catch (final Exception e) {
            adStatus = NO_AD;
            LOG.info(traceMarker, "Some exception is caught while filling the velocity template for partner{} {}",
                    advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.BANNER_PARSE_RESPONSE_EXCEPTION);
        }

    }

    private String getPartnerWinUrl() {
        String winUrl = DEFAULT_EMPTY_STRING;
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
            if (StringUtils.isNotEmpty(callbackUrl)) {
                LOG.debug(traceMarker, "inside wn from config");
                winUrl = callbackUrl;
            } else if (StringUtils.isNotEmpty(nUrl)) {
                LOG.debug(traceMarker, "inside wn from nurl");
                winUrl = nUrl;
            }
        }
        return winUrl;
    }

    protected void nativeAdBuilding() {
        InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_NATIVE_RESPONSES);
        try {
            final Map<String, String> params = new HashMap<String, String>();
            params.put("beaconUrl", getBeaconUrl());
            params.put("winUrl", getBeaconUrl() + RTBCallbackMacros.WIN_BID_GET_PARAM);
            params.put("impressionId", impressionId);
            params.put("placementId", String.valueOf(sasParams.getPlacementId()));
            params.put("nUrl", nurl);
            params.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());

            responseContent = nativeResponseMaker.makeResponse(bidResponse, params, templateEntity);
        } catch (final Exception e) {
            adStatus = NO_AD;
            responseContent = DEFAULT_EMPTY_STRING;

            LOG.error(
                    "Some exception is caught while filling the native template for placementId = {}, advertiser = {}, "
                            + "exception = {}", sasParams.getPlacementId(), advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_PARSE_RESPONSE_EXCEPTION);
        }
    }


    private boolean deserializeResponse(final String response) {
        final Gson gson = new Gson();
        try {
            bidResponse = gson.fromJson(response, BidResponse.class);
            LOG.debug(traceMarker, "Done with parsing of bidresponse");
            if (null == bidResponse || null == bidResponse.getSeatbid() || bidResponse.getSeatbidSize() == 0) {
                LOG.debug(traceMarker, "BidResponse does not have seat bid object");
                return false;
            }
            if (StringUtils.isNotEmpty(bidResponse.getCur())) {
                bidderCurrency = bidResponse.getCur();
            }
            bidPriceInLocal = bidResponse.getSeatbid().get(0).getBid().get(0).getPrice();
            bidPriceInUsd = calculatePriceInUSD(getBidPriceInLocal(), bidderCurrency);
            responseSeatId = bidResponse.getSeatbid().get(0).getSeat();
            final Bid bid = bidResponse.getSeatbid().get(0).getBid().get(0);
            adm = bid.getAdm();
            nurl = bid.getNurl();
            responseImpressionId = bid.getImpid();
            creativeId = bid.getCrid();
            sampleImageUrl = bid.getIurl();
            advertiserDomains = bid.getAdomain();
            creativeAttributes = bid.getAttr();
            responseAuctionId = bidResponse.getId();

            return true;
        } catch (final NullPointerException e) {
            LOG.info(traceMarker, "Could not parse the rtb response from partner: {}, exception thrown {}", getName(), e);
            return false;
        }
    }

    private double calculatePriceInUSD(final double price, final String currencyCode) {
        if (StringUtils.isEmpty(currencyCode) || USD.equalsIgnoreCase(currencyCode)) {
            return price;
        } else {
            final CurrencyConversionEntity currencyConversionEntity =
                    repositoryHelper.queryCurrencyConversionRepository(currencyCode);
            if (null != currencyConversionEntity && null != currencyConversionEntity.getConversionRate()
                    && currencyConversionEntity.getConversionRate() > 0.0) {
                return price / currencyConversionEntity.getConversionRate();
            }
        }
        return price;
    }

    private double calculatePriceInLocal(final double price) {
        if (USD.equalsIgnoreCase(bidderCurrency)) {
            return price;
        }
        final CurrencyConversionEntity currencyConversionEntity =
                repositoryHelper.queryCurrencyConversionRepository(bidderCurrency);
        if (null != currencyConversionEntity && null != currencyConversionEntity.getConversionRate()) {
            return price * currencyConversionEntity.getConversionRate();
        }
        return price;
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

            if(isNativeRequest && null != templateEntity) {
                trackerBuilder.setNativeTemplateId(templateEntity.getId());
            }
        }
    }

    @Override
    public void setSecondBidPrice(final Double price) {
        secondBidPriceInUsd = price;
        secondBidPriceInLocal = calculatePriceInLocal(price);
        LOG.debug("secondBidPriceInUsd {}, secondBidPriceInLocal {} {}", secondBidPriceInUsd, secondBidPriceInLocal,
                bidderCurrency);
        LOG.debug(traceMarker, "responseContent before replaceMacros is {}", responseContent);
        responseContent = replaceRTBMacros(responseContent);
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
    public double getBidPriceInUsd() {
        return bidPriceInUsd;
    }

    @Override
    public double getBidPriceInLocal() {
        return bidPriceInLocal;
    }

    @Override
    public String getCurrency() {
        return bidderCurrency;
    }

    @Override
    public String getCreativeId() {
        return creativeId;
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

    public void setSiteBlinded(final boolean value) {
        siteBlinded = value;
    }

}
