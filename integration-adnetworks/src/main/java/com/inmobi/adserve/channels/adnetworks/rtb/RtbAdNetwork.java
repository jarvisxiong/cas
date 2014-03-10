package com.inmobi.adserve.channels.adnetworks.rtb;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.IABCategoriesInterface;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.IABCitiesInterface;
import com.inmobi.adserve.channels.util.IABCitiesMap;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.casthrift.rtb.App;
import com.inmobi.casthrift.rtb.Banner;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidRequest;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.Device;
import com.inmobi.casthrift.rtb.Geo;
import com.inmobi.casthrift.rtb.Impression;
import com.inmobi.casthrift.rtb.SeatBid;
import com.inmobi.casthrift.rtb.Site;
import com.inmobi.casthrift.rtb.User;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.RequestBuilder;


/**
 * Generic RTB adapter.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public class RtbAdNetwork extends BaseAdNetworkImpl {

    private final static Logger            LOG                          = LoggerFactory.getLogger(RtbAdNetwork.class);

    @Getter
    @Setter
    private String                         urlBase;
    @Getter
    @Setter
    private String                         urlArg;
    @Getter
    @Setter
    private String                         rtbMethod;
    @Getter
    @Setter
    private String                         rtbVer;
    @Getter
    @Setter
    private String                         callbackUrl;
    @Setter
    private double                         bidPriceInUsd;
    @Setter
    private double                         bidPriceInLocal;
    @Getter
    @Setter
    BidRequest                             bidRequest;
    @Getter
    @Setter
    BidResponse                            bidResponse;
    private final boolean                  wnRequired;
    private final int                      auctionType                  = 2;
    private int                            tmax                         = 200;
    private static final String            X_OPENRTB_VERSION            = "x-openrtb-version";
    private static final String            CONTENT_TYPE                 = "application/json";
    private static final String            DISPLAY_MANAGER_INMOBI_SDK   = "inmobi_sdk";
    private static final String            DISPLAY_MANAGER_INMOBI_JS    = "inmobi_js";
    private final String                   advertiserId;
    public static ImpressionCallbackHelper impressionCallbackHelper;
    private final IABCategoriesInterface   iabCategoriesInterface;
    private final IABCountriesInterface    iabCountriesInterface;
    private final IABCitiesInterface       iabCitiesInterface;
    private final boolean                  siteBlinded;
    private final String                   advertiserName;
    private double                         secondBidPriceInUsd          = 0;
    private double                         secondBidPriceInLocal        = 0;
    private String                         bidRequestJson               = "";
    protected static final String          mraid                        = "<script src=\"mraid.js\" ></script>";
    private String                         encryptedBid;
    private static List<String>            mimes                        = Arrays.asList("image/jpeg", "image/gif",
                                                                                "image/png");
    private static List<Integer>           fsBlockedAttributes          = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 13,
                                                                                15, 16);
    private static List<Integer>           performanceBlockedAttributes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                                                                                11, 12, 13, 14, 15, 16);
    private static final String            FAMILY_SAFE_RATING           = "1";
    private static final String            PERFORMANCE_RATING           = "0";
    private static final String            RATING_KEY                   = "fs";
    private String                         responseSeatId;
    private String                         responseImpressionId;
    private String                         responseAuctionId;
    private final RepositoryHelper         repositoryHelper;
    private String                         bidderCurrency               = "USD";
    private static final String            USD                          = "USD";
    @Getter
    static List<String>                    currenciesSupported          = new ArrayList<String>(Arrays.asList("USD",
                                                                                "RMB"));
    private static AsyncHttpClient         asyncHttpClient;

    @Inject
    private static ExecutorService         executorService;

    static {
        ConfigurationLoader configurationLoader = ConfigurationLoader
                .getInstance("/opt/mkhoj/conf/cas/channel-server.properties");

        AsyncHttpClientConfig asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                .setRequestTimeoutInMs(configurationLoader.getRtbConfiguration().getInt("RTBreadtimeoutMillis") - 100)
                .setConnectionTimeoutInMs(
                        configurationLoader.getRtbConfiguration().getInt("RTBreadtimeoutMillis") - 100)
                .setAllowPoolingConnection(true)
                .setMaximumConnectionsTotal(
                        configurationLoader.getServerConfiguration().getInt("rtbOutGoingMaxConnections", 200))
                .setExecutorService(executorService).build();
        asyncHttpClient = new AsyncHttpClient(asyncHttpClientConfig);

    }

    @Override
    protected AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }

    public RtbAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent, final String urlBase,
            final String advertiserName, final int tmax, final RepositoryHelper repositoryHelper) {

        super(baseRequestHandler, serverEvent);
        this.advertiserId = config.getString(advertiserName + ".advertiserId");
        this.urlArg = config.getString(advertiserName + ".urlArg");
        this.rtbVer = config.getString(advertiserName + ".rtbVer", "2.0");
        this.callbackUrl = config.getString(advertiserName + ".wnUrlback");
        this.rtbMethod = config.getString(advertiserName + ".rtbMethod");
        this.wnRequired = config.getBoolean(advertiserName + ".isWnRequired");
        this.siteBlinded = config.getBoolean(advertiserName + ".siteBlinded");
        this.clientBootstrap = clientBootstrap;
        this.urlBase = urlBase;
        this.setRtbPartner(true);
        this.iabCategoriesInterface = new IABCategoriesMap();
        this.iabCitiesInterface = new IABCitiesMap();
        this.iabCountriesInterface = new IABCountriesMap();
        this.advertiserName = advertiserName;
        this.tmax = tmax;
        this.repositoryHelper = repositoryHelper;
    }

    @Override
    protected boolean configureParameters() {

        LOG.debug("inside configureParameters of RTB");
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandate parameters missing for dummy so exiting adapter");
            return false;
        }

        // Creating site/app Object
        App app = null;
        Site site = null;
        if (null != sasParams.getSource() && null != sasParams.getSiteId()) {
            if (sasParams.getSource().equalsIgnoreCase("WAP")) {
                // Creating Site object
                site = createSiteObject();
            }
            else {
                // Creating App object
                app = createAppObject();
            }
        }

        // Creating Geo Object for device Object
        Geo geo = createGeoObject();
        // Creating Banner object
        Banner banner = createBannerObject();
        // Creating Device Object
        Device device = createDeviceObject(geo);
        // Creating User Object
        User user = createUserObject();
        // Creating Impression Object
        List<Impression> impresssionlist = new ArrayList<Impression>();
        String displayManager = null;
        String displayManagerVersion = null;
        if (null != sasParams.getSdkVersion()) {
            displayManager = DISPLAY_MANAGER_INMOBI_SDK;
            displayManagerVersion = sasParams.getSdkVersion();
        }
        else if (null != sasParams.getAdcode() && "JS".equalsIgnoreCase(sasParams.getAdcode())) {
            displayManager = DISPLAY_MANAGER_INMOBI_JS;
        }
        Impression impression = createImpressionObject(banner, displayManager, displayManagerVersion);
        if (null == impression) {
            return false;
        }
        impresssionlist.add(impression);

        // Creating BidRequest Object using unique auction id per auction
        boolean flag = createBidRequestObject(impresssionlist, site, app, user, device);
        if (!flag) {
            return false;
        }

        // Serializing the bidRequest Object
        return serializeBidRequest();
    }

    private boolean createBidRequestObject(final List<Impression> impresssionlist, final Site site, final App app,
            final User user, final Device device) {
        bidRequest = new BidRequest(casInternalRequestParameters.auctionId, impresssionlist);
        bidRequest.setTmax(tmax);
        bidRequest.setAt(auctionType);
        bidRequest.setCur(Collections.<String> emptyList());
        List<String> seatList = new ArrayList<String>();
        seatList.add(advertiserId);
        bidRequest.setWseat(seatList);
        if (casInternalRequestParameters != null) {
            LOG.debug("blockedCategories are {}", casInternalRequestParameters.blockedCategories);
            LOG.debug("blockedAdvertisers are {}", casInternalRequestParameters.blockedAdvertisers);
            bidRequest.setBcat(new ArrayList<String>());
            if (null != casInternalRequestParameters.blockedCategories) {
                bidRequest.setBcat(iabCategoriesInterface
                        .getIABCategories(casInternalRequestParameters.blockedCategories));
            }
            // Setting blocked categories
            if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
                bidRequest.getBcat().addAll(
                        iabCategoriesInterface.getIABCategories(IABCategoriesMap.PERFORMANCE_BLOCK_CATEGORIES));
            }
            else {
                bidRequest.getBcat().addAll(
                        iabCategoriesInterface.getIABCategories(IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES));
            }

            if (null != casInternalRequestParameters.blockedAdvertisers) {
                bidRequest.setBadv(casInternalRequestParameters.blockedAdvertisers);
            }
        }
        else {
            LOG.debug("casInternalRequestParameters is null, so not setting blocked advertisers and categories");
        }

        if (site != null) {
            bidRequest.setSite(site);
        }
        else if (app != null) {
            bidRequest.setApp(app);
        }
        else {
            LOG.debug("App and Site both object can not be null so returning");
            return false;
        }

        bidRequest.setDevice(device);
        bidRequest.setUser(user);
        return true;
    }

    private boolean serializeBidRequest() {
        TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        try {
            bidRequestJson = serializer.toString(bidRequest);
            LOG.info("RTB request json is : {}", bidRequestJson);
        }
        catch (TException e) {
            LOG.debug("Could not create json from bidrequest for partner {}", advertiserName);
            LOG.info("Configure parameters inside rtb returned false {}", advertiserName);
            return false;
        }
        if (StringUtils.isNotBlank(beaconUrl)) {
            beaconUrl = beaconUrl + "&b=${WIN_BID}";
        }
        LOG.info("Configure parameters inside rtb returned true");
        return true;
    }

    private Impression createImpressionObject(final Banner banner, final String displayManager,
            final String displayManagerVersion) {
        Impression impression;
        if (null != casInternalRequestParameters.impressionId) {
            impression = new Impression(casInternalRequestParameters.impressionId);
        }
        else {
            LOG.info("Impression id can not be null in sasparam");
            return null;
        }
        impression.setBanner(banner);
        impression.setBidfloorcur(USD);
        // Set interstitial or not
        if (null != sasParams.getRqAdType() && "int".equalsIgnoreCase(sasParams.getRqAdType())) {
            impression.setInstl(1);
        }
        else {
            impression.setInstl(0);
        }
        if (casInternalRequestParameters != null) {
            impression.setBidfloor(casInternalRequestParameters.rtbBidFloor);
            LOG.debug("Bid floor is {}", impression.getBidfloor());
        }
        if (null != displayManager) {
            impression.setDisplaymanager(displayManager);
        }
        if (null != displayManagerVersion) {
            impression.setDisplaymanagerver(displayManagerVersion);
        }
        return impression;
    }

    private Banner createBannerObject() {
        Banner banner = new Banner();
        banner.setId(casInternalRequestParameters.impressionId);
        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            banner.setW((int) dim.getWidth());
            banner.setH((int) dim.getHeight());
        }

        // api type is always mraid
        if (!StringUtils.isEmpty(sasParams.getSdkVersion()) && sasParams.getSdkVersion().length() > 1) {
            List<Integer> apis = new ArrayList<Integer>();
            apis.add(3);
            banner.setApi(apis);
        }

        // mime types a static list
        banner.setMimes(mimes);

        // Setting battributes
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            banner.setBattr(performanceBlockedAttributes);
        }
        else {
            banner.setBattr(fsBlockedAttributes);
        }
        return banner;
    }

    private Geo createGeoObject() {
        Geo geo = new Geo();
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            geo.setLat(Float.parseFloat(latlong[0]));
            geo.setLon(Float.parseFloat(latlong[1]));
        }
        if (null != sasParams.getCountry()) {
            geo.setCountry(iabCountriesInterface.getIabCountry(sasParams.getCountry()));
        }
        if (null != sasParams.getUserLocation() && null != iabCitiesInterface.getIABCity(sasParams.getUserLocation())) {
            geo.setCity(iabCitiesInterface.getIABCity(sasParams.getUserLocation()));
        }
        geo.setZip(casInternalRequestParameters.zipCode);
        // Setting type of geo data
        if ("derived-lat-lon".equalsIgnoreCase(sasParams.getLocSrc())) {
            geo.setType(1);
        }
        else if ("lat-lon".equalsIgnoreCase(sasParams.getLocSrc())) {
            geo.setType(2);
        }
        return geo;
    }

    private User createUserObject() {
        User user = new User();
        user.setGender(sasParams.getGenderOrig());
        if (casInternalRequestParameters.uid != null) {
            user.setId(casInternalRequestParameters.uid);
            user.setBuyeruid(casInternalRequestParameters.uid);
        }

        try {
            if (sasParams.getAge() != null) {
                int age = Integer.parseInt(sasParams.getAge());
                int year = Calendar.getInstance().get(Calendar.YEAR);
                int yob = year - age;
                user.setYob(yob);
            }
        }
        catch (NumberFormatException e) {
            LOG.debug("Exception : {}", e);
        }
        return user;
    }

    private Site createSiteObject() {
        Site site = null;
        if (siteBlinded) {
            site = new Site(getBlindedSiteId(sasParams.getSiteIncId(), entity.getIncId()));
        }
        else {
            site = new Site(sasParams.getSiteId());
        }
        if (null != sasParams.getKeywords()) {
            site.setKeywords(sasParams.getKeywords());
        }
        if (null != sasParams.getCategories()) {
            site.setCat(iabCategoriesInterface.getIABCategories(sasParams.getCategories()));
        }
        Map<String, String> siteExtensions = new HashMap<String, String>();
        String siteRating;
        if (!SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            // Family safe
            siteRating = FAMILY_SAFE_RATING;
        }
        else {
            siteRating = PERFORMANCE_RATING;
        }
        siteExtensions.put(RATING_KEY, siteRating);
        site.setExt(siteExtensions);

        return site;
    }

    private App createAppObject() {
        App app = null;
        if (siteBlinded) {
            app = new App(getBlindedSiteId(sasParams.getSiteIncId(), entity.getAdgroupIncId()));
        }
        else {
            app = new App(sasParams.getSiteId());
        }
        if (null != sasParams.getCategories()) {
            app.setCat(iabCategoriesInterface.getIABCategories(sasParams.getCategories()));
        }
        Map<String, String> appExtensions = new HashMap<String, String>();
        String appRating;
        if (!SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            // Family safe
            appRating = FAMILY_SAFE_RATING;
        }
        else {
            appRating = PERFORMANCE_RATING;
        }
        appExtensions.put(RATING_KEY, appRating);
        app.setExt(appExtensions);
        return app;
    }

    private Device createDeviceObject(final Geo geo) {
        Device device = new Device();
        device.setIp(sasParams.getRemoteHostIp());
        device.setUa(sasParams.getUserAgent());
        device.setGeo(geo);
        Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            device.setOs(HandSetOS.values()[sasParamsOsId - 1].toString());
        }
        // Setting do not track
        if (null != casInternalRequestParameters.uidADT) {
            try {
                device.setDnt(Integer.parseInt(casInternalRequestParameters.uidADT) == 0 ? 1 : 0);
            }
            catch (NumberFormatException e) {
                LOG.debug("Exception while parsing uidADT to integer {}", e);
            }
        }
        // Setting platform id sha1 hashed
        if (null != casInternalRequestParameters.uidSO1) {
            device.setDidsha1(casInternalRequestParameters.uidSO1);
            device.setDpidsha1(casInternalRequestParameters.uidSO1);
        }
        else if (null != casInternalRequestParameters.uidO1) {
            device.setDidsha1(casInternalRequestParameters.uidO1);
            device.setDpidsha1(casInternalRequestParameters.uidO1);
        }

        // Setting platform id md5 hashed
        if (null != casInternalRequestParameters.uidMd5) {
            device.setDidmd5(casInternalRequestParameters.uidMd5);
            device.setDpidmd5(casInternalRequestParameters.uidMd5);
        }
        else if (null != casInternalRequestParameters.uid) {
            device.setDidmd5(casInternalRequestParameters.uid);
            device.setDpidmd5(casInternalRequestParameters.uid);
        }

        // Setting Extension for idfa
        if (!StringUtils.isEmpty(casInternalRequestParameters.uidIFA)) {
            Map<String, String> deviceExtensions = device.getExt();
            if (null == deviceExtensions) {
                deviceExtensions = new HashMap<String, String>();
            }
            deviceExtensions.put("idfasha1", getHashedValue(casInternalRequestParameters.uidIFA, "SHA-1"));
            deviceExtensions.put("idfamd5", getHashedValue(casInternalRequestParameters.uidIFA, "MD5"));
            device.setExt(deviceExtensions);
        }
        return device;
    }

    @SuppressWarnings("unused")
    @Override
    public void impressionCallback() {
        URI uriCallBack = null;
        this.callbackUrl = replaceRTBMacros(this.callbackUrl);
        LOG.debug("Callback url is : {}", callbackUrl);
        try {
            uriCallBack = new URI(callbackUrl);
        }
        catch (URISyntaxException e) {
            LOG.debug("error in creating uri for callback");
        }
        StringBuilder content = new StringBuilder();
        content.append("{\"bidid\"=").append(bidResponse.bidid).append(",\"seat\"=")
                .append(bidResponse.seatbid.get(0).getSeat());
        content.append(",\"bid\"=").append(bidResponse.seatbid.get(0).bid.get(0).id).append(",\"adid\"=")
                .append(bidResponse.seatbid.get(0).bid.get(0).adid).append("}");
        HttpRequest callBackRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                uriCallBack.toASCIIString());
        callBackRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(content, Charset.defaultCharset());
        callBackRequest.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
        callBackRequest.setContent(buffer);
        boolean callbackResult = impressionCallbackHelper.writeResponse(clientBootstrap, uriCallBack, callBackRequest);
        if (callbackResult) {
            LOG.debug("Callback is sent successfully");
        }
        else {
            LOG.debug("Could not send the callback");
        }
    }

    @SuppressWarnings("unused")
    private void setCallbackContent() {
        StringBuilder content = new StringBuilder();
        content.append("{\"bidid\"=").append(bidResponse.bidid).append(",\"seat\"=")
                .append(bidResponse.seatbid.get(0).getSeat());
        content.append(",\"bid\"=").append(bidResponse.seatbid.get(0).bid.get(0).id).append(",\"price\"=")
                .append(secondBidPriceInUsd).append(",\"adid\"=").append(bidResponse.seatbid.get(0).bid.get(0).adid)
                .append("}");
    }

    public String replaceRTBMacros(String url) {
        url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_ID), bidResponse.id);
        url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_CURRENCY), bidderCurrency);
        if (6 != sasParams.getDst()) {
            url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_PRICE_ENCRYPTED), encryptedBid);
            url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_PRICE),
                    Double.toString(secondBidPriceInLocal));
        }
        if (null != bidResponse.getSeatbid().get(0).getBid().get(0).getAdid()) {
            url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_AD_ID),
                    bidResponse.getSeatbid().get(0).getBid().get(0).getAdid());
        }
        if (null != bidResponse.bidid) {
            url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_BID_ID), bidResponse.bidid);
        }
        if (null != bidResponse.getSeatbid().get(0).getSeat()) {
            url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_SEAT_ID), bidResponse.getSeatbid()
                    .get(0).getSeat());
        }
        if (null == bidRequest) {
            LOG.info("bidrequest is null");
            return url;
        }
        url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_IMP_ID), bidRequest.getImp().get(0)
                .getId());
        LOG.debug("String after replaceMacros is {}", url);
        return url;
    }

    @Override
    protected void setNingRequest(final String requestUrl) throws Exception {
        byte[] body = bidRequestJson.getBytes(CharsetUtil.UTF_8);

        ningRequest = new RequestBuilder("POST").setUrl(requestUrl)
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us").setHeader(HttpHeaders.Names.REFERER, requestUrl)
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                .addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
                .setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(body.length)).setBody(body)
                .setHeader(X_OPENRTB_VERSION, rtbVer).build();
    }

    @Override
    public URI getRequestUri() {
        StringBuilder url;
        url = new StringBuilder();
        if (rtbMethod.equalsIgnoreCase("get")) {
            url.append(urlBase).append('?').append(urlArg).append('=');
        }
        else {
            url.append(urlBase);
        }
        LOG.debug("{} url is {}", getName(), url.toString());
        return (URI.create(url.toString()));
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        adStatus = "NO_AD";
        LOG.debug("response is {}", response);
        if (status.getCode() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.getCode();
            boolean parsedResponse = deserializeResponse(response);
            if (!parsedResponse) {
                adStatus = "NO_AD";
                responseContent = "";
                statusCode = 500;
                LOG.info("Error in parsing rtb response");
                return;
            }
            adStatus = "AD";
            VelocityContext velocityContext = new VelocityContext();
            SeatBid seatBid = bidResponse.getSeatbid().get(0);
            Bid bid = seatBid.getBid().get(0);
            String image = bid.getAdm();
            if ("wap".equalsIgnoreCase(sasParams.getSource())) {
                velocityContext.put(VelocityTemplateFieldConstants.PartnerHtmlCode, image);
            }
            else {
                velocityContext.put(VelocityTemplateFieldConstants.PartnerHtmlCode, mraid + image);
                if (StringUtils.isNotBlank(sasParams.getImaiBaseUrl())) {
                    velocityContext.put(VelocityTemplateFieldConstants.IMAIBaseUrl, sasParams.getImaiBaseUrl());
                }
            }
            // Checking whether to send win notification
            LOG.debug("isWinRequired is {} and winfromconfig is {}", wnRequired, callbackUrl);
            if (wnRequired) {
                // setCallbackContent();
                // Win notification is required
                String nUrl = null;
                try {
                    nUrl = bidResponse.seatbid.get(0).getBid().get(0).getNurl();
                }
                catch (Exception e) {
                    LOG.debug("Exception while parsing response {}", e);
                }
                LOG.debug("nurl is {}", nUrl);
                if (!StringUtils.isEmpty(callbackUrl)) {
                    LOG.debug("inside wn from config");
                    velocityContext.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, callbackUrl);
                }
                else if (!StringUtils.isEmpty(nUrl)) {
                    LOG.debug("inside wn from nurl");
                    velocityContext.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, nUrl);
                }

            }
            velocityContext.put(VelocityTemplateFieldConstants.IMBeaconUrl, this.beaconUrl);
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.RTB_HTML, velocityContext, sasParams,
                        null);
            }
            catch (Exception e) {
                adStatus = "NO_AD";
                LOG.info("Some exception is caught while filling the velocity template for partner{} {}",
                        advertiserName, e);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    public boolean deserializeResponse(final String response) {
        Gson gson = new Gson();
        try {
            bidResponse = gson.fromJson(response, BidResponse.class);
            LOG.debug("Done with parsing of bidresponse");
            if (null == bidResponse || null == bidResponse.getSeatbid() || bidResponse.getSeatbidSize() == 0) {
                LOG.debug("BidResponse does not have seat bid object");
                return false;
            }
            if (!StringUtils.isEmpty(bidResponse.getCur())) {
                bidderCurrency = bidResponse.getCur();
            }
            setBidPriceInLocal(bidResponse.getSeatbid().get(0).getBid().get(0).getPrice());
            setBidPriceInUsd(calculatePriceInUSD(getBidPriceInLocal(), bidderCurrency));
            responseSeatId = bidResponse.getSeatbid().get(0).getSeat();
            responseImpressionId = bidResponse.getSeatbid().get(0).getBid().get(0).getImpid();
            responseAuctionId = bidResponse.getId();
            return true;
        }
        catch (NullPointerException e) {
            LOG.info("Could not parse the rtb response from partner: {}", this.getName());
            return false;
        }
    }

    private double calculatePriceInUSD(final double price, String currencyCode) {
        if (StringUtils.isEmpty(currencyCode)) {
            currencyCode = USD;
        }
        if (USD.equalsIgnoreCase(currencyCode)) {
            return price;
        }
        else {
            CurrencyConversionEntity currencyConversionEntity = repositoryHelper
                    .queryCurrencyConversionRepository(currencyCode);
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
        CurrencyConversionEntity currencyConversionEntity = repositoryHelper
                .queryCurrencyConversionRepository(bidderCurrency);
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
        return this.advertiserName;
    }

    @Override
    public void setSecondBidPrice(final Double price) {
        this.secondBidPriceInUsd = price;
        this.secondBidPriceInLocal = calculatePriceInLocal(price);
        LOG.debug("responseContent before replaceMacros is {}", responseContent);
        this.responseContent = replaceRTBMacros(this.responseContent);
        ThirdPartyAdResponse adResponse = getResponseAd();
        adResponse.response = responseContent;
        LOG.debug("responseContent after replaceMacros is {}", getResponseAd().response);
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
        return bidderCurrency;
    }

}
