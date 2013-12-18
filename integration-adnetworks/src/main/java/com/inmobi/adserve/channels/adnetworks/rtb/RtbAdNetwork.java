package com.inmobi.adserve.channels.adnetworks.rtb;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.*;
import com.inmobi.casthrift.rtb.*;
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
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Generic RTB adapter.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public class RtbAdNetwork extends BaseAdNetworkImpl {

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
    private boolean                        wnRequired;
    Integer                                auctionType                  = 2;
    Integer                                tmax                         = 200;
    private static final String            X_OPENRTB_VERSION            = "x-openrtb-version";
    private static final String            CONTENT_TYPE                 = "application/json";
    private static final String            DISPLAY_MANAGER_INMOBI_SDK   = "inmobi_sdk";
    private static final String            DISPLAY_MANAGER_INMOBI_JS    = "inmobi_js";
    private final DebugLogger              logger;
    private String                         advertiserId;
    public static ImpressionCallbackHelper impressionCallbackHelper;
    private IABCategoriesInterface         iabCategoriesInterface;
    private IABCountriesInterface          iabCountriesInterface;
    private IABCitiesInterface             iabCitiesInterface;
    private boolean                        siteBlinded;
    private String                         advertiserName;
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
    private String                         responseSeatId;
    private String                         responseImpressionId;
    private String                         responseAuctionId;
    private RepositoryHelper               repositoryHelper;
    private String                         bidderCurrency               = "USD";
    private static final String            USD                          = "USD";
    @Getter
    static List<String>                    currenciesSupported          = new ArrayList<String>(Arrays.asList("USD",
                                                                            "RMB"));

    public RtbAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent, String urlBase, String advertiserName,
            int tmax, RepositoryHelper repositoryHelper) {

        super(baseRequestHandler, serverEvent, logger);
        this.advertiserId = config.getString(advertiserName + ".advertiserId");
        this.urlArg = config.getString(advertiserName + ".urlArg");
        this.rtbVer = config.getString(advertiserName + ".rtbVer", "2.0");
        this.callbackUrl = config.getString(advertiserName + ".wnUrlback");
        this.rtbMethod = config.getString(advertiserName + ".rtbMethod");
        this.wnRequired = config.getBoolean(advertiserName + ".isWnRequired");
        this.siteBlinded = config.getBoolean(advertiserName + ".siteBlinded");
        this.logger = logger;
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

        logger.debug("inside configureParameters of RTB");
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandate parameters missing for dummy so exiting adapter");
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

    private boolean createBidRequestObject(List<Impression> impresssionlist, Site site, App app, User user,
            Device device) {
        bidRequest = new BidRequest(casInternalRequestParameters.auctionId, impresssionlist);
        bidRequest.setTmax(tmax);
        bidRequest.setAt(auctionType);
        bidRequest.setCur(Collections.<String> emptyList());
        if (casInternalRequestParameters != null) {
            logger.debug("blockedCategories are", casInternalRequestParameters.blockedCategories);
            logger.debug("blockedAdvertisers are", casInternalRequestParameters.blockedAdvertisers);
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
            logger.debug("casInternalRequestParameters is null, so not setting blocked advertisers and categories");
        }

        if (site != null) {
            bidRequest.setSite(site);
        }
        else if (app != null) {
            bidRequest.setApp(app);
        }
        else {
            logger.debug("App and Site both object can not be null so returning");
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
            logger.info("RTB request json is :", bidRequestJson);
        }
        catch (TException e) {
            logger.debug("Could not create json from bidrequest for partner", advertiserName);
            logger.info("Configure parameters inside rtb returned false", advertiserName);
            return false;
        }
        if (StringUtils.isNotBlank(beaconUrl)) {
            beaconUrl = beaconUrl + "&b=${WIN_BID}";
        }
        logger.info("Configure parameters inside rtb returned true");
        return true;
    }

    private Impression createImpressionObject(Banner banner, String displayManager, String displayManagerVersion) {
        Impression impression;
        if (null != casInternalRequestParameters.impressionId) {
            impression = new Impression(casInternalRequestParameters.impressionId);
        }
        else {
            logger.info("Impression id can not be null in sasparam");
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
            logger.debug("Bid floor is", new Double(impression.getBidfloor()).toString());
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
            logger.debug("Exception :", e.getMessage());
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
        return app;
    }

    private Device createDeviceObject(Geo geo) {
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
                device.setDnt(Integer.parseInt(casInternalRequestParameters.uidADT));
            }
            catch (NumberFormatException e) {
                logger.debug("Exception while parsing uidADT to integer", e.getMessage());
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
        logger.debug("Callback url is : ", callbackUrl);
        try {
            uriCallBack = new URI(callbackUrl);
        }
        catch (URISyntaxException e) {
            logger.debug("error in creating uri for callback");
        }
        StringBuilder content = new StringBuilder();
        content.append("{\"bidid\"=")
                    .append(bidResponse.bidid)
                    .append(",\"seat\"=")
                    .append(bidResponse.seatbid.get(0).getSeat());
        content.append(",\"bid\"=")
                    .append(bidResponse.seatbid.get(0).bid.get(0).id)
                    .append(",\"adid\"=")
                    .append(bidResponse.seatbid.get(0).bid.get(0).adid)
                    .append("}");
        HttpRequest callBackRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                uriCallBack.toASCIIString());
        callBackRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(content, Charset.defaultCharset());
        callBackRequest.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
        callBackRequest.setContent(buffer);
        boolean callbackResult = impressionCallbackHelper.writeResponse(clientBootstrap, logger, uriCallBack,
            callBackRequest);
        if (callbackResult) {
            logger.debug("Callback is sent successfully");
        }
        else {
            logger.debug("Could not send the callback");
        }
    }

    @SuppressWarnings("unused")
    private void setCallbackContent() {
        StringBuilder content = new StringBuilder();
        content.append("{\"bidid\"=")
                    .append(bidResponse.bidid)
                    .append(",\"seat\"=")
                    .append(bidResponse.seatbid.get(0).getSeat());
        content.append(",\"bid\"=")
                    .append(bidResponse.seatbid.get(0).bid.get(0).id)
                    .append(",\"price\"=")
                    .append(secondBidPriceInUsd)
                    .append(",\"adid\"=")
                    .append(bidResponse.seatbid.get(0).bid.get(0).adid)
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
            url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_SEAT_ID), bidResponse
                    .getSeatbid()
                        .get(0)
                        .getSeat());
        }
        if (null == bidRequest) {
            logger.info("bidrequest is null");
            return url;
        }
        url = url.replaceAll("(?i)" + Pattern.quote(RTBCallbackMacros.AUCTION_IMP_ID), bidRequest
                .getImp()
                    .get(0)
                    .getId());
        logger.debug("String after replaceMacros is ", url);
        return url;
    }

    @Override
    public HttpRequest getHttpRequest() throws Exception {

        HttpMethod httpRequestMethod;
        if (rtbMethod.equalsIgnoreCase("get")) {
            httpRequestMethod = HttpMethod.GET;
        }
        else {
            httpRequestMethod = HttpMethod.POST;
        }
        logger.debug("HttpRequest method is : ", httpRequestMethod);
        try {
            URI uri = getRequestUri();
            requestUrl = (uri.toString());
            request = (new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpRequestMethod, uri.toASCIIString()));
            logger.debug("host name is ", uri.getHost());
            request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            request.setHeader(X_OPENRTB_VERSION, rtbVer);
            request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.setHeader(HttpHeaders.Names.HOST, uri.getHost());
            if (null == bidRequest) {
                logger.debug("bidRequest is null so httpRequest is null");
                return null;
            }
            ChannelBuffer buffer = ChannelBuffers.copiedBuffer(bidRequestJson, CharsetUtil.UTF_8);
            request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
            request.setContent(buffer);
        }
        catch (Exception ex) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.HTTPREQUEST_ERROR;
            logger.info("Error in making http request for partner", advertiserName, ex);
        }
        logger.debug("content is ", request.getContent().toString(CharsetUtil.UTF_8));
        return request;
    }

    public URI getRequestUri() {
        StringBuilder url;
        url = new StringBuilder();
        if (rtbMethod.equalsIgnoreCase("get")) {
            url.append(urlBase).append('?').append(urlArg).append('=');
        }
        else {
            url.append(urlBase);
        }
        logger.debug(getName(), "url is", url.toString());
        return (URI.create(url.toString()));
    }

    public void parseResponse(String response, HttpResponseStatus status) {
        adStatus = "NO_AD";
        logger.debug("response is ", response);
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
                logger.info("Error in parsing rtb response");
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
            logger.debug("isWinRequired is", wnRequired, "and winfromconfig is", callbackUrl);
            if (wnRequired) {
                // setCallbackContent();
                // Win notification is required
                String nUrl = null;
                try {
                    nUrl = bidResponse.seatbid.get(0).getBid().get(0).getNurl();
                }
                catch (Exception e) {
                    logger.debug("Exception while parsing response");
                }
                logger.debug("nurl is", nUrl);
                if (!StringUtils.isEmpty(callbackUrl)) {
                    logger.debug("inside wn from config");
                    velocityContext.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, callbackUrl);
                }
                else if (!StringUtils.isEmpty(nUrl)) {
                    logger.debug("inside wn from nurl");
                    velocityContext.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, nUrl);
                }

            }
            velocityContext.put(VelocityTemplateFieldConstants.IMBeaconUrl, this.beaconUrl);
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.RTB_HTML, velocityContext, sasParams,
                    null, logger);
            }
            catch (Exception e) {
                adStatus = "NO_AD";
                logger.info("Some exception is caught while filling the velocity template for partner", advertiserName,
                    e.getMessage());
            }
        }
        logger.debug("response length is ", responseContent.length());
    }

    public boolean deserializeResponse(String response) {
        Gson gson = new Gson();
        try {
            bidResponse = gson.fromJson(response, BidResponse.class);
            logger.debug("Done with parsing of bidresponse");
            if (null == bidResponse || null == bidResponse.getSeatbid() || bidResponse.getSeatbidSize() == 0) {
                logger.debug("BidResponse does not have seat bid object");
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
            logger.info("Could not parse the rtb response from partner:", this.getName());
            return false;
        }
    }

    private double calculatePriceInUSD(double price, String currencyCode) {
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

    private double calculatePriceInLocal(double price) {
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
    public void setSecondBidPrice(Double price) {
        this.secondBidPriceInUsd = price;
        this.secondBidPriceInLocal = calculatePriceInLocal(price);
        logger.debug("responseContent before replaceMacros is", responseContent);
        this.responseContent = replaceRTBMacros(this.responseContent);
        ThirdPartyAdResponse adResponse = getResponseAd();
        adResponse.response = responseContent;
        logger.debug("responseContent after replaceMacros is", getResponseAd().response);
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
    public void setEncryptedBid(String encryptedBid) {
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
