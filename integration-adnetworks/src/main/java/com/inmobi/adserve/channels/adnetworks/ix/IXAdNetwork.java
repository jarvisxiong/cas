package com.inmobi.adserve.channels.adnetworks.ix;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;

import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.api.template.NativeTemplateAttributeFinder;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.*;
import com.inmobi.casthrift.ix.*;
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.velocity.VelocityContext;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.reflections.Reflections;

import javax.inject.Inject;

import java.awt.*;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

import java.lang.reflect.Field;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Generic IX adapter.
 *
 * @author Anshul Soni(anshul.soni@inmobi.com)
 */
public class IXAdNetwork extends BaseAdNetworkImpl {

    private final static Logger            LOG                          = LoggerFactory.getLogger(IXAdNetwork.class);

    @Getter
    @Setter
    private String                         urlBase;
    @Getter
    @Setter
    private String                         urlArg;
    @Getter
    @Setter
    private String ixMethod;
    @Getter
    @Setter
    private String                         callbackUrl;
    @Setter
    private double                         bidPriceInUsd;
    @Setter
    private double                         bidPriceInLocal;
    @Getter
    @Setter
    IXBidRequest                           bidRequest;
    @Getter
    @Setter
    IXBidResponse                          bidResponse;
    private final String                   userName;
    private final String                   password;
    private final Integer                  accountId;
    private final boolean                  wnRequired;
    private final int                      auctionType                  = 2;
    private int                            tmax                         = 200;
    private boolean                        templateWN                   = true;
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
    private String                         buyer;
    private String                         dealId;
    private Double                         adjustbid;
    private String                         creativeId;
    private Integer                        pmptier;
    private Integer                        estimated;
    private String                         aqid;
    private String                         sampleImageUrl;
    private List<String>                   advertiserDomains;
    private List<Integer>                  creativeAttributes;
    private boolean                        logCreative                  = false;
    private String                         adm;
    private final RepositoryHelper         repositoryHelper;
    private String                         bidderCurrency               = "USD";
    private static final String            USD                          = "USD";
    private static final String BLOCKLIST_PARAM = "p_block_keys";
    private static final String SITE_BLOCKLIST_FORMAT="blk%s";
    private static final String RUBICON_PERF_BLOCKLIST_ID = "InMobiPERF";
    private static final String RUBICON_FS_BLOCKLIST_ID = "InMobiFS";
    private WapSiteUACEntity wapSiteUACEntity;
    private boolean isWapSiteUACEntity = false;

    private List<String> blockedAdvertisers = Lists.newArrayList(); ;

    @Getter
    static List<String>                    currenciesSupported          = new ArrayList<String>(Arrays.asList("USD",
            "CNY","JPY","EUR","KRW","RUB"));
    @Getter
    static List<String>                    blockedAdvertiserList        = new ArrayList<String>(Arrays.asList("king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com", "supercell.com"));

    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;

    @Inject
    private static NativeTemplateAttributeFinder nativeTemplateAttributeFinder;

    private static Map<String, String> replaceKeys;

    static {

        LOG.debug("using reflections to retrieve keys, they will be replaced in json");

        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.inmobi.casthrift.ix"))));

        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        replaceKeys = new HashMap<>();

        for (Class<? extends  Object> class1:allClasses) {

            Field[] fields = class1.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                if (Modifier.isPublic(fields[i].getModifiers()) && fields[i].getName().contains("__")) {

                    LOG.debug(fields[i].getName());
                    replaceKeys.put(fields[i].getName(),fields[i].getName().replaceAll("__","."));
                }
            }
        }

        Set<String> keys= replaceKeys.keySet();
        for(String tempKey:keys)
        {
            LOG.debug(tempKey+" "+replaceKeys.get(tempKey));
        }

    }


    private static Map<Short, Integer> slotIdMap;
    static {
        slotIdMap = new HashMap<Short, Integer>();
        slotIdMap.put((short) 4, 44);
        // Mapping 320x48 to 320x50
        slotIdMap.put((short) 9, 43);
        slotIdMap.put((short) 10, 15);
        slotIdMap.put((short) 11, 2);
        slotIdMap.put((short) 12, 1);
        slotIdMap.put((short) 13, 8);
        slotIdMap.put((short) 14, 67);
        slotIdMap.put((short) 15, 43);
        slotIdMap.put((short) 16, 102);
        slotIdMap.put((short) 18, 9);
        slotIdMap.put((short) 19, 50);
        slotIdMap.put((short) 21, 45);
        slotIdMap.put((short) 23, 46);
        slotIdMap.put((short) 29, 14);
        slotIdMap.put((short) 32, 101);

    }


    private static final String nativeString = "native";

    @Override
    protected AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClientProvider.getRtbAsyncHttpClient();
    }

    public IXAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
                       final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String urlBase,
                       final String advertiserName, final int tmax, final RepositoryHelper repositoryHelper, final boolean templateWinNotification) {

        super(baseRequestHandler, serverChannel);
        this.advertiserId = config.getString(advertiserName + ".advertiserId");
        this.urlArg = config.getString(advertiserName + ".urlArg");
        //this.rtbVer = config.getString(advertiserName + ".rtbVer", "2.0");
        this.callbackUrl = config.getString(advertiserName + ".wnUrlback");
        this.ixMethod = config.getString(advertiserName + ".ixMethod");
        this.wnRequired = config.getBoolean(advertiserName + ".isWnRequired");
        this.siteBlinded = config.getBoolean(advertiserName + ".siteBlinded");
        this.clientBootstrap = clientBootstrap;
        this.urlBase = urlBase;
        this.setIxPartner(true);
        this.iabCategoriesInterface = new IABCategoriesMap();
        this.iabCountriesInterface = new IABCountriesMap();
        this.advertiserName = advertiserName;
        this.tmax = tmax;
        this.repositoryHelper = repositoryHelper;
        this.templateWN = templateWinNotification;
        this.isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", true);
        this.isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", false);
        this.blockedAdvertisers.addAll(blockedAdvertiserList);
        this.userName = config.getString(advertiserName + ".userName");
        this.password = config.getString(advertiserName + ".password");
        this.accountId = config.getInt(advertiserName + ".accountId");
    }

    @Override
    protected boolean configureParameters() {

        LOG.debug("inside configureParameters of IX");
        if (StringUtils.isBlank(sasParams.getRemoteHostIp())
                || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)
                || !isRequestFormatSupported()) {
            LOG.debug("mandate parameters missing or request format is not compaitable to partner supported response for dummy so exiting adapter");
            return false;
        }

        if(sasParams.getWapSiteUACEntity() != null)
        {
            this.wapSiteUACEntity = sasParams.getWapSiteUACEntity();
            this.isWapSiteUACEntity=true;
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
        //Creating Regs Object
        Regs regs = createRegsObject();
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
        ProxyDemand proxyDemand = createProxyDemandObject();
        Impression impression = createImpressionObject(banner, displayManager, displayManagerVersion,proxyDemand);
        if (null == impression) {
            return false;
        }
        impresssionlist.add(impression);

        // Creating BidRequest Object using unique auction id per auction
        boolean flag = createBidRequestObject(impresssionlist, site, app, user, device, regs);
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
                                           final User user, final Device device,final Regs regs) {
        bidRequest = new IXBidRequest(casInternalRequestParameters.auctionId, impresssionlist);
        bidRequest.setTmax(tmax);

        LOG.debug("INSIDE CREATE BID REQUEST OBJECT");

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
        bidRequest.setRegs(regs);
        return true;
    }

    private boolean serializeBidRequest() {

        TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());

        try {

            bidRequestJson = serializer.toString(bidRequest);
            if(isNativeRequest()){
                bidRequestJson = bidRequestJson.replaceFirst("nativeObject", "native");
            }
            Set<String> keys= replaceKeys.keySet();
            //replacing all __ with .
            for(String tempKey:keys)
            {
                bidRequestJson = bidRequestJson.replaceFirst(tempKey , replaceKeys.get(tempKey));
            }


            LOG.info("IX request json is : {}", bidRequestJson);
        }
        catch (TException e) {
            LOG.debug("Could not create json from bidrequest for partner {}", advertiserName);
            LOG.info("Configure parameters inside IX returned false {}", advertiserName);
            return false;
        }
        LOG.info("return true");


        return true;
    }


    private Regs createRegsObject()
    {
        Regs regs= new Regs();
        if(isWapSiteUACEntity)
        {
            if (wapSiteUACEntity.isCoppaEnabled()) {
                regs.setCoppa(1);
            }
            else
            {
                regs.setCoppa(0);
            }
        }
        return regs;
    }

    private ProxyDemand createProxyDemandObject() {
        ProxyDemand proxyDemand = new ProxyDemand();
        proxyDemand.setMarketrate(sasParams.getMarketRate());
        return proxyDemand;
    }


    private Impression createImpressionObject(final Banner banner, final String displayManager,
                                              final String displayManagerVersion,final ProxyDemand proxyDemand) {
        Impression impression = new Impression();
        impression.setId(impressionId);
        LOG.debug("INSIDE CREATE IMPRESSION");
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
        impression.setProxydemand(proxyDemand);
        //impression.setBidfloorcur(USD);
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


        ImpressionExtensions impExt = new ImpressionExtensions();

        JSONObject additionalParams= entity.getAdditionalParams();
        String zoneId=getZoneId(additionalParams);
        if(null != zoneId) {
            impExt.setRp__zone_id(zoneId);
        }


        impression.setExt(impExt);

        return impression;
    }

    public String getZoneId(JSONObject additionalParams) {
        String categoryZoneId = null;
        try {
            if (sasParams.getCategories() != null) {
                for (int index = 0; index < sasParams.getCategories().size(); index++) {
                    String categoryIdKey = sasParams.getCategories().get(index)
                            .toString();
                    if (additionalParams.has(categoryIdKey)) {
                        categoryZoneId = additionalParams
                                .getString(categoryIdKey);
                        LOG.debug("category Id is {}", categoryZoneId);
                    }
                    if (categoryZoneId != null) {
                        return categoryZoneId;
                    }
                }
            }
            if (additionalParams.has("default")) {
                categoryZoneId = additionalParams.getString("default");
            }

        } catch (JSONException exception) {
            LOG.error("Unable to get zone_id for Rubicon ");
        }
        return categoryZoneId;
    }


    private Banner createBannerObject() {
        Banner banner = new Banner();
        banner.setId(casInternalRequestParameters.impressionId);
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            banner.setW((int) dim.getWidth());
            banner.setH((int) dim.getHeight());
        }

        final BannerExtensions ext= new BannerExtensions();
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            if (slotIdMap.containsKey(sasParams.getSlot())) {
                ext.setRp__size_id(slotIdMap.get(sasParams.getSlot()));
            }
        }

        banner.setExt(ext);
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

        geo.setZip(casInternalRequestParameters.zipCode);

        return geo;
    }

    private User createUserObject() {
        User user = new User();

        user.setId(getUid());
        if (casInternalRequestParameters.uid != null) {
            user.setId(casInternalRequestParameters.uid);
            user.setBuyeruid(casInternalRequestParameters.uid);
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

        String category = null;

        if (isWapSiteUACEntity &&
                StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())) {
            site.setName(wapSiteUACEntity.getAppType());
        }else if ((category = getCategories(',', false)) != null) {
            site.setName(category);
        }

        List <String> blockedList= getBlockedList();
        site.setBlocklists(blockedList);
        final Publisher publisher = new Publisher();
        publisher.setCat(iabCategoriesInterface.getIABCategories(sasParams.getCategories()));

        publisher.setId(blindedSiteId);
        final PublisherExtensions publisherExtensions = new PublisherExtensions();
        publisherExtensions.setRp__account_id(accountId);
        publisher.setExt(publisherExtensions);
        site.setPublisher(publisher);

        final AdQuality adQuality = createAdQuality();

        site.setAq(adQuality);

        final SiteExtensions ext= new SiteExtensions();


        JSONObject additionalParams= entity.getAdditionalParams();
        try {
            Integer siteId = Integer.parseInt(additionalParams.getString("site"));
            ext.setRp__site_id(siteId);
        } catch (JSONException e) {
            LOG.debug("Site Id is not configured in rubicon so exiting adapter");
        }

        site.setExt(ext);

        return site;
    }

    private AdQuality createAdQuality()
    {
        AdQuality adQuality = new AdQuality();
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            adQuality.setSensitivity("low");
        }
        else {
            adQuality.setSensitivity("high");
        }
        return adQuality;
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
        if (isWapSiteUACEntity &&
                StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())) {
            app.setName(wapSiteUACEntity.getAppType());
        }else if ((category = getCategories(',', false)) != null) {
            app.setName(category);
        }


        List <String> blockedList= getBlockedList();
        app.setBlocklists(blockedList);

        final AdQuality adQuality = createAdQuality();

        app.setAq(adQuality);

        final AppExt ext= new AppExt();



        JSONObject additionalParams= entity.getAdditionalParams();
        try {
            Integer siteId = Integer.parseInt(additionalParams.getString("site"));
            ext.setRp__site_id(siteId);
        } catch (JSONException e) {
            LOG.debug("Site Id is not configured in rubicon so exiting adapter");
        }

        app.setExt(ext);

        return app;
    }


    public List <String> getBlockedList()
    {
        List<String> blockedList = Lists.newArrayList();
        blockedList.add(String.format(SITE_BLOCKLIST_FORMAT, sasParams.getSiteIncId()));
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {

            blockedList.add(RUBICON_PERF_BLOCKLIST_ID);

        } else {

            blockedList.add(RUBICON_FS_BLOCKLIST_ID);

        }
        return blockedList;
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

        if(StringUtils.isNotBlank(sasParams.getOsMajorVersion())){
            device.setOsv(sasParams.getOsMajorVersion());
        }

        // Setting do not track
        if (null != casInternalRequestParameters.uidADT) {
            try {
                device.setDnt(Integer.parseInt(casInternalRequestParameters.uidADT) == 0 ? 1 : 0);
                device.setLmt(Integer.parseInt(casInternalRequestParameters.uidADT) == 0 ? 1 : 0);
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

        // Setting Extension for ifa
        if (!StringUtils.isEmpty(casInternalRequestParameters.uidIFA)) {

            device.setIfa(casInternalRequestParameters.uidIFA);

        }

        //  if (!StringUtils.isEmpty(casInternalRequestParameters.gpid)) {

        final DeviceExtensions ext= new DeviceExtensions();

        ext.setRp__xff(sasParams.getRemoteHostIp());
        device.setExt(ext);

        return device;
    }





    public String replaceIXMacros(String url) {
        url = url.replaceAll(RTBCallbackMacros.AUCTION_ID_INSENSITIVE, bidResponse.id);
        url = url.replaceAll(RTBCallbackMacros.AUCTION_CURRENCY_INSENSITIVE, bidderCurrency);
        if (6 != sasParams.getDst()) {
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_ENCRYPTED_INSENSITIVE, encryptedBid);
            url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_INSENSITIVE,
                    Double.toString(secondBidPriceInLocal));
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
        if (ixMethod.equalsIgnoreCase("get")) {
            httpRequestMethod = "GET";
        }
        else {
            httpRequestMethod = "POST";
        }

        String authStr = userName + ":" + password;
        String authEncoded = new String(Base64.encodeBase64(authStr.getBytes()));
        LOG.debug("INSIDE GET NING REQUEST");

        return new RequestBuilder(httpRequestMethod).setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json").setBody(body)
                .setHeader("Authorization", "Basic " + authEncoded)
                .setHeader(HttpHeaders.Names.HOST, uri.getHost()).build();
    }

    @Override
    public URI getRequestUri() throws URISyntaxException {
        StringBuilder url = new StringBuilder();
        if (ixMethod.equalsIgnoreCase("get")) {
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
                LOG.info("Error in parsing ix response");
                return;
            }
            adStatus = "AD";
            if(isNativeRequest()){
                Integer removeThis=1;

                // nativeAdBuilding();
                LOG.debug("we do not support native request");
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

    public boolean deserializeResponse(final String response) {
        Gson gson = new Gson();
        try {
            bidResponse = gson.fromJson(response, IXBidResponse.class);
            LOG.debug("Done with parsing of bidresponse");
            if (null == bidResponse || null == bidResponse.getSeatbid() || bidResponse.getSeatbidSize() == 0) {
                LOG.debug("BidResponse does not have seat bid object");
                return false;
            }
            //bidderCurrency is to USD by default
            SeatBid seatBid=bidResponse.getSeatbid().get(0);
            setBidPriceInLocal(seatBid.getBid().get(0).getPrice());
            setBidPriceInUsd(calculatePriceInUSD(getBidPriceInLocal(), bidderCurrency));
            responseSeatId = seatBid.getSeat();
            Bid bid =  seatBid.getBid().get(0);
            adm = bid.getAdm();
            responseImpressionId = bid.getImpid();
            creativeId = bid.getCrid();
            responseAuctionId = bidResponse.getId();
            pmptier = bid.getPmptier();
            //estimated = bid.getEstimated(); //Not used currently
            aqid = bid.getAqid();
            adjustbid = bid.getAdjustbid();
            if(null == adjustbid)
            {
                LOG.debug("yahan aaya h :D :D");
            }
            dealId = bid.getDealid();
            buyer = seatBid.getBuyer();
            return true;
        }
        catch (NullPointerException e) {
            LOG.info("Could not parse the ix response from partner: {}", this.getName());
            return false;
        }
    }

    @Override
    public double returnAdjustBid() { return adjustbid; }

    @Override
    public String returnDealId()
    {
        return dealId;
    }

    @Override
    public String returnBuyer()
    {
        return buyer;
    }

    @Override
    public Integer returnPmptier() { return pmptier; }

    @Override
    public String returnAqid() { return aqid; }

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
        this.responseContent = replaceIXMacros(this.responseContent);
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