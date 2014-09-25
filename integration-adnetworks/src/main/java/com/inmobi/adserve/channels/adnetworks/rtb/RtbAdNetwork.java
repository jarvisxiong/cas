package com.inmobi.adserve.channels.adnetworks.rtb;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.attribute.BAttrNativeType;
import com.inmobi.adserve.channels.api.attribute.BTypeNativeAttributeType;
import com.inmobi.adserve.channels.api.attribute.SuggestedNativeAttributeType;
import com.inmobi.adserve.channels.api.natives.NativeBuilder;
import com.inmobi.adserve.channels.api.natives.NativeBuilderFactory;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.api.template.NativeTemplateAttributeFinder;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.IABCategoriesInterface;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.Utils.ClickUrlMakerV6;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.casthrift.rtb.App;
import com.inmobi.casthrift.rtb.AppExt;
import com.inmobi.casthrift.rtb.AppStore;
import com.inmobi.casthrift.rtb.Banner;
import com.inmobi.casthrift.rtb.BannerExtVideo;
import com.inmobi.casthrift.rtb.BannerExtensions;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidExtensions;
import com.inmobi.casthrift.rtb.BidRequest;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.Device;
import com.inmobi.casthrift.rtb.Geo;
import com.inmobi.casthrift.rtb.Impression;
import com.inmobi.casthrift.rtb.ImpressionExtensions;
import com.inmobi.casthrift.rtb.Native;
import com.inmobi.casthrift.rtb.Site;
import com.inmobi.casthrift.rtb.User;
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
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.UrlValidator;
import org.apache.http.client.utils.URIBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final boolean                  siteBlinded;
    private final String                   advertiserName;
    private double                         secondBidPriceInUsd          = 0;
    private double                         secondBidPriceInLocal        = 0;
    private String                         bidRequestJson               = "";
    protected static final String          mraid                        = "<script src=\"mraid.js\" ></script>";
    private String                         encryptedBid;
    private static List<String>            image_mimes                  = Arrays.asList("image/jpeg", "image/gif",
                                                                                "image/png");
    private static List<Integer>           fsBlockedAttributes          = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13,
                                                                                14, 15, 16);
    private static List<Integer>           performanceBlockedAttributes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                                                                                11, 12, 13, 14, 15, 16);
    private static List<Integer>           videoBlockedAttributes       = Arrays.asList(7, 8, 9, 10, 14);
    private static List<Integer>           videoBlockCreativeType       = Arrays.asList(4);  // iframe

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
    private List<String> blockedAdvertisers = Lists.newArrayList();

    private static final List<String>      VIDEO_MIMES                  = Arrays.asList("video/mp4");
    private static final int               EXT_VIDEO_LINEARITY          = 1;   // only linear ads
    private static final int               EXT_VIDEO_MINDURATION        = 15;  // in secs.
    private static final int               EXT_VIDEO_MAXDURATION        = 30;  // in secs.
    private static final List<String>      EXT_VIDEO_TYPE               = Arrays.asList("VAST 2.0", "VAST 3.0", "VAST 2.0 Wrapper", "VAST 3.0 Wrapper");

    @Getter
    static List<String>                    currenciesSupported          = new ArrayList<String>(Arrays.asList("USD",
                                                                                "CNY","JPY","EUR","KRW","RUB"));
    @Getter
    static List<String>                    blockedAdvertiserList        = new ArrayList<String>(Arrays.asList("king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com", "supercell.com"));

    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;

    @Inject
    private static NativeTemplateAttributeFinder nativeTemplateAttributeFinder;

    @Inject
    private static NativeBuilderFactory    nativeBuilderfactory;

    @Inject
    private static NativeResponseMaker     nativeResponseMaker;


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
        this.iabCountriesInterface = new IABCountriesMap();
        this.advertiserName = advertiserName;
        this.tmax = tmax;
        this.repositoryHelper = repositoryHelper;
        this.templateWN = templateWinNotification;
        this.isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", true);
        this.isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", false);
        this.isBannerVideoResponseSupported = config.getBoolean(advertiserName + ".bannerVideoSupported", false);
        this.blockedAdvertisers.addAll(blockedAdvertiserList);
    }

    @Override
    protected boolean configureParameters() {

        LOG.debug("inside configureParameters of RTB");
        if (StringUtils.isBlank(sasParams.getRemoteHostIp())
        		|| StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)
                || !isRequestFormatSupported()) {
            LOG.debug("mandate parameters missing or request format is not compatible to partner supported response for dummy so exiting adapter");
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

    private boolean isRequestFormatSupported() {
        if (isNativeRequest()) {
            return isNativeResponseSupported;
        } else {
            return isHTMLResponseSupported;
        }
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
                blockedAdvertisers.addAll(casInternalRequestParameters.blockedAdvertisers);
            }
            bidRequest.setBadv(blockedAdvertisers);
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
            LOG.info("Impression id can not be null in casInternal Request Params");
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
            impression.setBidfloor(casInternalRequestParameters.auctionBidFloor);
            LOG.debug("Bid floor is {}", impression.getBidfloor());
        }
        if (null != displayManager) {
            impression.setDisplaymanager(displayManager);
        }
        if (null != displayManagerVersion) {
            impression.setDisplaymanagerver(displayManagerVersion);
        }
        
        if(isNativeResponseSupported && isNativeRequest()){
        	ImpressionExtensions impExt = createNativeExtensionObject();
        	
        	if(impExt ==null){
        		return null;
        	}
        	impression.setExt(impExt);
        }
        return impression;
    }
    
    
    private ImpressionExtensions createNativeExtensionObject(){
//    	Native nat = new Native();
//    	nat.setMandatory(nativeTemplateAttributeFinder.findAttribute(new MandatoryNativeAttributeType()));
//    	nat.setImage(nativeTemplateAttributeFinder.findAttribute(new ImageNativeAttributeType()));
    	NativeAdTemplateEntity templateEntity = repositoryHelper.queryNativeAdTemplateRepository(sasParams.getSiteId());
    	if(templateEntity == null){
    		LOG.info(String.format("This site id %s doesn't have native template :",sasParams.getSiteId()));
    		return null;
    	}
    	NativeBuilder nb = nativeBuilderfactory.create(templateEntity);
    	Native nat = nb.build();
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
        banner.setMimes(image_mimes);

        // Setting battributes
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            banner.setBattr(performanceBlockedAttributes);
        }
        else {
            banner.setBattr(fsBlockedAttributes);
        }

        // This request supports in-Banner Video on interstitial slot and this partner supports video.
        if (sasParams.isBannerVideoSupported() && isBannerVideoResponseSupported) {
            // Set video specific attributes to the Banner object
            banner.setBattr(videoBlockedAttributes);
            banner.setBtype(videoBlockCreativeType);
            banner.setPos(1); // above the fold

            List<String> allMimes = new ArrayList<String>(VIDEO_MIMES);
            allMimes.addAll(image_mimes);
            banner.setMimes(allMimes);

            BannerExtensions ext = createBannerExtensionsObject();
            banner.setExt(ext);
        }
        return banner;
    }

    private BannerExtensions createBannerExtensionsObject() {
        // Create Banner->ext->video Object
        BannerExtVideo video = new BannerExtVideo();
        video.setLinearity(EXT_VIDEO_LINEARITY);
        video.setMinduration(EXT_VIDEO_MINDURATION);
        video.setMaxduration(EXT_VIDEO_MAXDURATION);
        video.setType(EXT_VIDEO_TYPE);

        BannerExtensions bannerExtensions = new BannerExtensions();
        bannerExtensions.setVideo(video);

        return bannerExtensions;
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
            site = new Site(getBlindedSiteId(sasParams.getSiteIncId(), entity.getIncId(getCreativeType())));
        }
        else {
            site = new Site(sasParams.getSiteId());
        }
        if (null != sasParams.getCategories()) {
            site.setCat(iabCategoriesInterface.getIABCategories(sasParams.getCategories()));
        }
        String category = null;
        if (sasParams.getWapSiteUACEntity() != null &&
            StringUtils.isNotEmpty(sasParams.getWapSiteUACEntity().getAppType())) {
          site.setName(sasParams.getWapSiteUACEntity().getAppType());
        }else if ((category = getCategories(',', false)) != null) {
          site.setName(category);
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
        String category = null;
        if (sasParams.getWapSiteUACEntity() != null &&
          StringUtils.isNotEmpty(sasParams.getWapSiteUACEntity().getAppType())) {
          app.setName(sasParams.getWapSiteUACEntity().getAppType());
        }else if ((category = getCategories(',', false)) != null) {
          app.setName(category);
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

        Request ningRequest = new RequestBuilder().setUrl(uriCallBack.toASCIIString()).setMethod("POST")
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

        // Condition changed from sasParams.getDst() != 6 to == 2 to avoid unnecessary IX RTBMacro Replacements
        if (2 == sasParams.getDst()) {
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

        return new RequestBuilder(httpRequestMethod).setUrl(uri.toString())
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
            if (isNativeRequest()) {
                nativeAdBuilding();
            } else if (isVideoResponseReceived) {
                bannerVideoAdBuilding();
            } else {
                bannerAdBuilding();
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }
    
    
    private void bannerAdBuilding(){
    	
        VelocityContext velocityContext = new VelocityContext();

        String admContent = getAdMarkUp();

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
        String partnerWinUrl = getPartnerWinUrl();
        if (StringUtils.isNotEmpty(partnerWinUrl)){
            velocityContext.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, partnerWinUrl);
        }
        
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

    private void bannerVideoAdBuilding() {
        VelocityContext velocityContext = new VelocityContext();

        String vastContentJSEsc = StringEscapeUtils.escapeJavaScript(getAdMarkUp());
        velocityContext.put(VelocityTemplateFieldConstants.VASTContentJSEsc, vastContentJSEsc);

        // JS escaped WinUrl for partner.
        String partnerWinUrl = getPartnerWinUrl();
        if (StringUtils.isNotEmpty(partnerWinUrl)) {
            velocityContext.put(VelocityTemplateFieldConstants.PartnerBeaconUrl,
                    StringEscapeUtils.escapeJavaScript(partnerWinUrl));
        }

        // JS escaped IMWinUrl
        String imWinUrl = this.beaconUrl + "?b=${WIN_BID}";
        velocityContext.put(VelocityTemplateFieldConstants.IMWinUrl, StringEscapeUtils.escapeJavaScript(imWinUrl));

        // JS escaped IM beacon and click URLs.
        velocityContext.put(VelocityTemplateFieldConstants.IMBeaconUrl, StringEscapeUtils.escapeJavaScript(this.beaconUrl));
        velocityContext.put(VelocityTemplateFieldConstants.IMClickUrl, StringEscapeUtils.escapeJavaScript(this.clickUrl));

        // SDK version
        velocityContext.put(VelocityTemplateFieldConstants.IMSDKVersion, sasParams.getSdkVersion());

        // Namespace
        velocityContext.put(VelocityTemplateFieldConstants.Namespace, Formatter.getNamespace());

        // IMAIBaseUrl
        velocityContext.put(VelocityTemplateFieldConstants.IMAIBaseUrl, sasParams.getImaiBaseUrl());

        try {
            responseContent = Formatter.getResponseFromTemplate(TemplateType.RTB_BANNER_VIDEO, velocityContext, sasParams,
                    null);
        } catch (Exception e) {
            adStatus = "NO_AD";
            LOG.info("Some exception is caught while filling the velocity template for partner{} {}",
                    advertiserName, e);
        }
    }

    private String getPartnerWinUrl(){
        String winUrl = "";
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
                winUrl = callbackUrl;
            }
            else if (!StringUtils.isEmpty(nUrl)) {
                LOG.debug("inside wn from nurl");
                winUrl = nUrl;
            }
        }
        return winUrl;
    }
    
    @Override
    protected boolean isNativeRequest(){
    	return nativeString.equals(sasParams.getRFormat());
    }
    
    private void nativeAdBuilding(){
    	
    	App app = bidRequest.getApp();

    	Map<String, String> params = new HashMap<String, String>();
    	String winUrl = this.beaconUrl + "?b=${WIN_BID}";
    	params.put("beaconUrl", this.beaconUrl);
    	params.put("winUrl",  winUrl);
    	params.put("impressionId", this.impressionId);
    	if(app!=null){
    		params.put("appId",app.getId());
    	}
    	try {
    		params.put("siteId", this.sasParams.getSiteId());
    		responseContent = nativeResponseMaker.makeResponse(bidResponse, params, repositoryHelper.queryNativeAdTemplateRepository(sasParams.getSiteId()));
		} catch (Exception e) {
			
			if(LOG.isDebugEnabled()){
				e.printStackTrace();
			}
			 adStatus = "NO_AD";
			 responseContent = "";
	         LOG.error("Some exception is caught while filling the native template for partner "+e.getLocalizedMessage(),
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
            adm = bid.getAdm();
            responseImpressionId = bid.getImpid();
            creativeId = bid.getCrid();
            sampleImageUrl = bid.getIurl();
            advertiserDomains = bid.getAdomain();
            creativeAttributes = bid.getAttr();
            responseAuctionId = bidResponse.getId();

            // Check bid response for video
            if (sasParams.isBannerVideoSupported() && isBannerVideoResponseSupported) {
                return checkBidResponseForBannerVideo(bid.getExt());
            }

            return true;
        }
        catch (NullPointerException e) {
            LOG.info("Could not parse the rtb response from partner: {}", this.getName());
            return false;
        }
    }

    /**
     * Checks if the RTB response is for video and contains a valid VAST response.
     * @return
     *      false - When a video response is received and it is NOT valid.
     *      true  - 1) When the response does not contain video (banner response)
     *              2) It contains video response which is a valid XML/URL.
     */
    private boolean checkBidResponseForBannerVideo(BidExtensions ext) {

        if (ext != null && ext.getVideo() != null) {
            LOG.debug("Received video response of type {}.", ext.getVideo().getType());

            // Validate the adm content for a valid URL/XML.
            if (!isValidURL(adm) && !isValidXMLFormat(adm)) {
                LOG.info("Invalid VAST response adm - {}", adm);
                return false;
            }

            // Validate supported VAST type.
            if (!EXT_VIDEO_TYPE.contains(ext.getVideo().getType())) {
                LOG.info("Unsupported VAST type - {}", ext.getVideo().getType());
                return false;
            }

            // Validate supported video duration.
            if (ext.getVideo().duration < EXT_VIDEO_MINDURATION
                    || ext.getVideo().duration > EXT_VIDEO_MAXDURATION) {
                LOG.info("VAST response video duration {} should be within {} and {}.", ext.getVideo().getDuration(), EXT_VIDEO_MINDURATION, EXT_VIDEO_MAXDURATION);
                return false;
            }

            // Validate Linearity
            if (ext.getVideo().linearity != EXT_VIDEO_LINEARITY) {
                LOG.info("Linearity {} is not supported for the VAST response.", ext.getVideo().linearity);
                return false;
            }

            // A valid video response is received. Set the flag.
            isVideoResponseReceived = true;

            String newImpressionId = this.casInternalRequestParameters.impressionIdForVideo;
            if (StringUtils.isNotEmpty(newImpressionId)) {

                // Update the response impression id so that this doesn't get filtered in AuctionImpressionIdFilter.
                if (this.impressionId.equalsIgnoreCase(responseImpressionId)) {
                    responseImpressionId = newImpressionId;
                }

                // Update beacon and click URLs to refer to the video Ads.
                this.beaconUrl = ClickUrlMakerV6.ClickUrlsRegenerator.regenerateBeaconUrl(this.beaconUrl, this.getImpressionId(), newImpressionId);
                this.clickUrl  = ClickUrlMakerV6.ClickUrlsRegenerator.regenerateClickUrl(this.clickUrl, this.getImpressionId(), newImpressionId);
                this.impressionId = newImpressionId;

                LOG.debug("Replaced impression id to new value {}.", newImpressionId);
            }
        }
        return true;
    }

    private boolean isValidURL(final String url) {
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    private boolean isValidXMLFormat(final String encodedXmlStr) {
        if (StringUtils.isEmpty(encodedXmlStr)) {
            return false;
        }

        /* The XML content is expected to be in encoded format, which can be:
         * 1) URLEncoded String
         * 2) JSEscaped String
         */
        String xmlStr;

        // If the string doesn't contain any space, consider it to be in URL encoded format.
        if (!encodedXmlStr.contains(" ")) {
            try {
                xmlStr = URIUtil.decode(encodedXmlStr);
            } catch (URIException e) {
                LOG.info("VAST XML response is NOT properly URL encode. {}", e.getMessage());
                return false;
            }
        } else {
            xmlStr = StringEscapeUtils.unescapeJavaScript(encodedXmlStr);
        }

        // Validate the XML by parsing it.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputSource source = new InputSource(new StringReader(xmlStr));
        try {
            DocumentBuilder db = factory.newDocumentBuilder();
            db.setErrorHandler(null);
            db.parse(source);

            // Initially adm was URL encoded XML string. Replace it with decoded value.
            this.adm = xmlStr;
            return true;
        } catch (SAXException | ParserConfigurationException | IOException e) {
            LOG.debug("VAST response is NOT a valid XML - {}", e.getMessage());
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
