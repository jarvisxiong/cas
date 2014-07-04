package com.inmobi.adserve.channels.adnetworks.rtb;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.*;
import com.inmobi.casthrift.rtb.*;
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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;


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
    private boolean                        templateWN                   = true;
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
    private static List<Integer>           fsBlockedAttributes          = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13,
                                                                                14, 15, 16);
    private static List<Integer>           performanceBlockedAttributes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                                                                                11, 12, 13, 14, 15, 16);
    private static final String            FAMILY_SAFE_RATING           = "1";
    private static final String            PERFORMANCE_RATING           = "0";
    private static final String            RATING_KEY                   = "fs";
    private String                         responseSeatId;
    private String                         responseImpressionId;
    private String                         responseAuctionId;
    private String                         creativeId;
    private String                         sampleImageUrl;
    private List<String>                   advertiserDomains;
    private List<Integer>                  creativeAttributes;
    private boolean                        logCreative                  = false;
    private String                         adm;
    private final RepositoryHelper         repositoryHelper;
    private String                         bidderCurrency               = "USD";
    private static final String            USD                          = "USD";
    private static final List<String> blockedAdvertisers = Lists.newArrayList("king.com", "supercell.net");
    
    @Getter
    static List<String>                    currenciesSupported          = new ArrayList<String>(Arrays.asList("USD",
                                                                                "CNY","JPY","EUR","KRW","RUB"));
    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;
    
    @Inject
    private static NativeTemplateAttributeFinder nativeTemplateAttributeFinder;
    
    @Inject
    private static NativeTemplateFormatter nativeTemplateFormatter;
    
    
    private static final String nativeString = "native";
    
    @Override
    protected AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClientProvider.getRtbAsyncHttpClient();
    }

    public RtbAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String urlBase,
            final String advertiserName, final int tmax, final RepositoryHelper repositoryHelper, final boolean templateWinNotification) {

        super(baseRequestHandler, serverChannel);
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
        this.templateWN = templateWinNotification;
        this.isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", true);
        this.isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", false);
    }

    @Override
    protected boolean configureParameters() {

        LOG.debug("inside configureParameters of RTB");
        if (StringUtils.isBlank(sasParams.getRemoteHostIp())
        		|| StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)
                || !isRequestFormatSupported()) {
            LOG.debug("mandate parameters missing or request format is not compaitable to partner supported response for dummy so exiting adapter");
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
    
    
    private boolean isRequestFormatSupported(){
    	if(isNativeRequest()){
    		return isNativeResponseSupported;
    	}else if(!isNativeRequest()){
    		return isHTMLResponseSupported;
    	}
    	
    	return false;
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
                casInternalRequestParameters.blockedAdvertisers.addAll(blockedAdvertisers);
            }else {
              casInternalRequestParameters.blockedAdvertisers = blockedAdvertisers;
            }
            bidRequest.setBadv(casInternalRequestParameters.blockedAdvertisers);
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
            if(isNativeRequest()){
            	bidRequestJson = bidRequestJson.replaceFirst("nativeObject", "native");
            }
            LOG.info("RTB request json is : {}", bidRequestJson);
        }
        catch (TException e) {
            LOG.debug("Could not create json from bidrequest for partner {}", advertiserName);
            LOG.info("Configure parameters inside rtb returned false {}", advertiserName);
            return false;
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
        
        if(!isNativeRequest()){
        	impression.setBanner(banner);
        }
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
        
        if(isNativeResponseSupported && isNativeRequest()){
        	impression.setExt(createNativeExtensionObject());
        }
        return impression;
    }
    
    
    private ImpressionExtensions createNativeExtensionObject(){
    	Native nat = new Native();
    	nat.setMandatory(nativeTemplateAttributeFinder.findAttribute(new MandatoryNativeAttributeType()));
    	nat.setImage(nativeTemplateAttributeFinder.findAttribute(new ImageNativeAttributeType()));
    	
    	//TODO: for native currently there is no way to identify MRAID traffic/container supported by publisher.
//    	if(!StringUtils.isEmpty(sasParams.getSdkVersion())){
//    	   nat.api.add(3);
//    	}
    	nat.setBattr(nativeTemplateAttributeFinder.findAttribute(new BAttrNativeType()));
    	nat.setSuggested(nativeTemplateAttributeFinder.findAttribute(new SuggestedNativeAttributeType()));
    	nat.setBtype(nativeTemplateAttributeFinder.findAttribute(new BTypeNativeAttributeType()));
    	
    	ImpressionExtensions iext = new ImpressionExtensions();
    	iext.setNativeObject(nat);
    	
    	return iext;
    }
    
   
    private Banner createBannerObject() {
        Banner banner = new Banner();
        banner.setId(casInternalRequestParameters.impressionId);
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
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
            geo.setLat(Double.parseDouble(String.format("%.4f", Double.parseDouble(latlong[0]))));
            geo.setLon(Double.parseDouble(String.format("%.4f", Double.parseDouble(latlong[1]))));
        }
        if (null != sasParams.getCountryCode()) {
            geo.setCountry(iabCountriesInterface.getIabCountry(sasParams.getCountryCode()));
        }
        /*if (null != iabCitiesInterface.getIABCity(sasParams.getCity() + "")) {
            geo.setCity(iabCitiesInterface.getIABCity(sasParams.getCity() + ""));
        }*/
        geo.setZip(casInternalRequestParameters.zipCode);
        // Setting type of geo data
        if ("DERIVED_LAT_LON".equalsIgnoreCase(sasParams.getLocSrc())) {
            geo.setType(1);
        }
        else if ("LATLON".equalsIgnoreCase(sasParams.getLocSrc())) {
            geo.setType(2);
        }
        return geo;
    }

    private User createUserObject() {
        User user = new User();
        String gender = sasParams.getGender();
        if ( StringUtils.isNotEmpty(gender));
        {
            user.setGender(gender);  
        }
        
        if (casInternalRequestParameters.uid != null) {
            user.setId(casInternalRequestParameters.uid);
            user.setBuyeruid(casInternalRequestParameters.uid);
        }

        try {
            if (sasParams.getAge() != null) {
                int age = sasParams.getAge();
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
        String appRating;
        if (!SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            // Family safe
            appRating = FAMILY_SAFE_RATING;
        }
        else {
            appRating = PERFORMANCE_RATING;
        }
        
        // set App Ext fields
        final AppExt ext = new AppExt();
        ext.setFs(appRating);
        final WapSiteUACEntity entity = sasParams.getWapSiteUACEntity();
        if(entity != null) {
        	final AppStore store = new AppStore();
        	if(!StringUtils.isEmpty(entity.getContentRating())) {
        		store.setRating(entity.getContentRating());
        	}
        	if(!StringUtils.isEmpty(entity.getAppType())) {
        		store.setCat(entity.getAppType());
        	}
        	if(entity.getCategories() != null && !entity.getCategories().isEmpty()) {
        		store.setSeccat(entity.getCategories());
        	}
        	ext.setStore(store);
        }
        app.setExt(ext);
        return app;
    }

    private Device createDeviceObject(final Geo geo) {
        Device device = new Device();
        device.setDevicetype(1);//Tablets and Mobiles
        device.setIp(sasParams.getRemoteHostIp());
        device.setUa(sasParams.getUserAgent());
        device.setGeo(geo);
        Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            device.setOs(HandSetOS.values()[sasParamsOsId - 1].toString());
        }
        
        if(StringUtils.isNotBlank(sasParams.getOsMajorVersion())){
            device.setOsv(sasParams.getOsMajorVersion());
        }
        if(NetworkType.WIFI == sasParams.getNetworkType()){
            device.setConnectiontype(2);
        }
        else{
            device.setConnectiontype(0);
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
        	final  Map<String, String> deviceExtensions = getDeviceExt(device);
            deviceExtensions.put("idfa", casInternalRequestParameters.uidIFA);
            deviceExtensions.put("idfasha1", getHashedValue(casInternalRequestParameters.uidIFA, "SHA-1"));
            deviceExtensions.put("idfamd5", getHashedValue(casInternalRequestParameters.uidIFA, "MD5"));
        }
        
        if (!StringUtils.isEmpty(casInternalRequestParameters.gpid)) {
        	final  Map<String, String> deviceExtensions = getDeviceExt(device);
       	 	deviceExtensions.put("gpid", casInternalRequestParameters.gpid);
       	}
        return device;
    }
    
    private  Map<String, String> getDeviceExt(final Device device) {
    	 Map<String, String> deviceExtensions = device.getExt();
         if (null == deviceExtensions) {
             deviceExtensions = new HashMap<String, String>();
             device.setExt(deviceExtensions);
         }
         return deviceExtensions;
    }

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

        byte[] body = content.toString().getBytes(CharsetUtil.UTF_8);

        Request ningRequest = new RequestBuilder().setUrl(uriCallBack.toASCIIString())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE)
                .setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(body.length)).setBody(body)
                .setHeader(HttpHeaders.Names.HOST, uriCallBack.getHost()).build();

        boolean callbackResult = impressionCallbackHelper.writeResponse(uriCallBack, ningRequest, getAsyncHttpClient());
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
        url = url.replaceAll(RTBCallbackMacros.AUCTION_ID_INSENSITIVE, bidResponse.id);
        url = url.replaceAll(RTBCallbackMacros.AUCTION_CURRENCY_INSENSITIVE, bidderCurrency);
        if (6 != sasParams.getDst()) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_ENCRYPTED_INSENSITIVE, encryptedBid);
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_INSENSITIVE,
                    Double.toString(secondBidPriceInLocal));
        }
        if (null != bidResponse.getSeatbid().get(0).getBid().get(0).getAdid()) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_AD_ID_INSENSITIVE,
                    bidResponse.getSeatbid().get(0).getBid().get(0).getAdid());
        }
        if (null != bidResponse.bidid) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_BID_ID_INSENSITIVE, bidResponse.bidid);
        }
        if (null != bidResponse.getSeatbid().get(0).getSeat()) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_SEAT_ID_INSENSITIVE, bidResponse.getSeatbid()
                    .get(0).getSeat());
        }
        if (null == bidRequest) {
            LOG.info("bidrequest is null");
            return url;
        }
        url = url.replaceAll(RTBCallbackMacros.AUCTION_IMP_ID_INSENSITIVE, bidRequest.getImp().get(0)
                .getId());

        LOG.debug("String after replaceMacros is {}", url);
        return url;
    }

    @Override
    protected Request getNingRequest() throws Exception {
        byte[] body = bidRequestJson.getBytes(CharsetUtil.UTF_8);

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        String httpRequestMethod;
        if (rtbMethod.equalsIgnoreCase("get")) {
            httpRequestMethod = "GET";
        }
        else {
            httpRequestMethod = "POST";
        }

        return new RequestBuilder(httpRequestMethod).setURI(uri)
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json").setBody(body)
                .setHeader(X_OPENRTB_VERSION, rtbVer).setHeader(HttpHeaders.Names.HOST, uri.getHost()).build();
    }

    @Override
    public URI getRequestUri() throws URISyntaxException {
        StringBuilder url = new StringBuilder();
        if (rtbMethod.equalsIgnoreCase("get")) {
            url.append(urlBase).append('?').append(urlArg).append('=');
        }
        else {
            url.append(urlBase);
        }
        LOG.debug("{} url is {}", getName(), url.toString());
        return URI.create(url.toString());
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        adStatus = "NO_AD";
        LOG.debug("response is {}", response);
        if (status.code() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.code();
            boolean parsedResponse = deserializeResponse(response);
            if (!parsedResponse) {
                adStatus = "NO_AD";
                responseContent = "";
                statusCode = 500;
                LOG.info("Error in parsing rtb response");
                return;
            }
            adStatus = "AD";
            if(isNativeRequest()){
            	nativeAdBuilding();
            }else{
            	nonNativeAdBuilding();
            }

        }
        LOG.debug("response length is {}", responseContent.length());
    }
    
    
    private void nonNativeAdBuilding(){
    	
        VelocityContext velocityContext = new VelocityContext();
        
        String admContent = getADMContent();

        int admSize = admContent.length();
        if (!templateWN) {
            String winUrl = this.beaconUrl + "?b=${WIN_BID}";
            admContent = admContent.replace(RTBCallbackMacros.AUCTION_WIN_URL, winUrl);
        }
        int admAfterMacroSize = admContent.length();

        if ("wap".equalsIgnoreCase(sasParams.getSource())) {
            velocityContext.put(VelocityTemplateFieldConstants.PartnerHtmlCode, admContent);
        }
        else {
            velocityContext.put(VelocityTemplateFieldConstants.PartnerHtmlCode, mraid + admContent);
            if (StringUtils.isNotBlank(sasParams.getImaiBaseUrl())) {
                velocityContext.put(VelocityTemplateFieldConstants.IMAIBaseUrl, sasParams.getImaiBaseUrl());
            }
        }
        // Checking whether to send win notification
        LOG.debug("isWinRequired is {} and winfromconfig is {}", wnRequired, callbackUrl);
        createWin(velocityContext);
        
        if (templateWN || (admAfterMacroSize ==  admSize)) {
            velocityContext.put(VelocityTemplateFieldConstants.IMBeaconUrl, this.beaconUrl);
        }
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
    
    private String getADMContent(){
    	
    	  SeatBid seatBid = bidResponse.getSeatbid().get(0);
          Bid bid = seatBid.getBid().get(0);
          String admContent = bid.getAdm();
          return admContent;
    	
    }
    
    private void createWin(VelocityContext velocityContext){
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
    }
    
    @Override
    protected boolean isNativeRequest(){
    	return nativeString.equals(sasParams.getRFormat());
    }
    
    private void nativeAdBuilding(){
    	
    	App app = bidRequest.getApp();
    	
    	Map<String, String> params = new HashMap<String, String>();
    	params.put("beaconUrl",beaconUrl);
    	if(app!=null){
    		params.put("appId",app.getId());
    	}
    	try {
    		responseContent = nativeTemplateFormatter.getFormatterValue(null, bidResponse, params);
		} catch (Exception e) {
			 adStatus = "NO_AD";
	         LOG.error("Some exception is caught while filling the native template for partner{} {}",
	                    advertiserName, e);
		}
    	
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
            Bid bid =  bidResponse.getSeatbid().get(0).getBid().get(0);
            responseImpressionId = bid.getImpid();
            creativeId = bid.getCrid();
            sampleImageUrl = bid.getIurl();
            advertiserDomains = bid.getAdomain();
            creativeAttributes = bid.getAttr();
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
        return  creativeAttributes;
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
    public void setLogCreative(boolean logCreative) {
        this.logCreative = logCreative;
    }

    @Override
    public String getAdMarkUp() {
        return adm;
    }

}
