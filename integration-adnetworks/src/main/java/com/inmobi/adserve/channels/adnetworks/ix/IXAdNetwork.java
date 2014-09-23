package com.inmobi.adserve.channels.adnetworks.ix;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.api.template.NativeTemplateAttributeFinder;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.IABCategoriesInterface;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.ix.AdQuality;
import com.inmobi.casthrift.ix.App;
import com.inmobi.casthrift.ix.Banner;
import com.inmobi.casthrift.ix.Bid;
import com.inmobi.casthrift.ix.CommonExtension;
import com.inmobi.casthrift.ix.Device;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private String                         ixMethod;
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
    private final String                   advertiserName;
    private double                         secondBidPriceInUsd          = 0;
    private double                         secondBidPriceInLocal        = 0;
    private String                         bidRequestJson               = "";
    protected static final String          mraid                        = "<script src=\"mraid.js\" ></script>";
    private String                         encryptedBid;
    private String                         responseSeatId;
    private String                         responseImpressionId;
    private String                         responseAuctionId;
    private String                         dealId;
    private Double                         adjustbid;
    private String                         creativeId;
    private Integer                        pmptier;
    private String                         aqid;
    private boolean                        isCoppaSet                   = false;
    private String                         sampleImageUrl;
    private List<String>                   advertiserDomains;
    private List<Integer>                  creativeAttributes;
    private boolean                        logCreative                  = false;
    private String                         adm;
    public final RepositoryHelper          repositoryHelper;
    private static final String            USD                          = "USD";
    @Getter
    private int                            impressionObjCount;
    @Getter
    private int                            responseBidObjCount;

    private static final String SITE_BLOCKLIST_FORMAT="blk%s";
    private static final String RUBICON_PERF_BLOCKLIST_ID = "InMobiPERF";
    private static final String RUBICON_FS_BLOCKLIST_ID = "InMobiFS";
    private WapSiteUACEntity wapSiteUACEntity;
    private boolean isWapSiteUACEntity = false;
    private List<String> globalBlindFromConfig;

    private List<String> blockedAdvertisers = Lists.newArrayList();

    @Getter
    static List<String>                    currenciesSupported          = new ArrayList<String>(Arrays.asList("USD",
            "CNY","JPY","EUR","KRW","RUB"));
    @Getter
    static List<String>                    blockedAdvertiserList        = new ArrayList<String>(Arrays.asList("king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com", "supercell.com"));

    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;

    @Inject
    private static NativeTemplateAttributeFinder nativeTemplateAttributeFinder;

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
    private ChannelSegmentEntity dspChannelSegmentEntity;


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
        this.globalBlindFromConfig = config.getList(advertiserName + ".globalBlind");
    }


    @Override
    protected boolean configureParameters() {

        LOG.debug("inside configureParameters of IX");
        if (StringUtils.isBlank(sasParams.getRemoteHostIp())
                || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)
                || !isRequestFormatSupported()) {
            LOG.debug("mandate parameters missing or request format is not compatible to partner supported response for dummy so exiting adapter");
            return false;
        }

        if(sasParams.getWapSiteUACEntity() != null){
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
            } else {
                // Creating App object
                app = createAppObject();
            }
        }
        //Creating Regs Object
        Regs regs = createRegsObject();
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
        } else if (null != sasParams.getAdcode() && "JS".equalsIgnoreCase(sasParams.getAdcode())) {
            displayManager = DISPLAY_MANAGER_INMOBI_JS;
        }
        ProxyDemand proxyDemand = createProxyDemandObject();

        // Only 1 impression object is being generated.
        Impression impression = createImpressionObject(banner, displayManager, displayManagerVersion,proxyDemand);
        if (null == impression) {
            return false;
        }
        impresssionlist.add(impression);
        this.impressionObjCount = impresssionlist.size();

        // Creating BidRequest Object using unique auction id per auction
        bidRequest = createBidRequestObject(impresssionlist, site, app, user, device, regs);

        if (null == bidRequest) {
            LOG.debug("Failed inside createBidRequest");
            return false;
        }

        if(isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            InspectorStats.incrementStatCount(InspectorStrings.IX_SENT_AS_TRANSPARENT);
        } else {
            InspectorStats.incrementStatCount(InspectorStrings.IX_SENT_AS_BLIND);
        }

        // Serializing the bidRequest Object
        bidRequestJson = serializeBidRequest();
        if(null == bidRequestJson){
            return false;
        }
        return true;
    }


    private boolean isRequestFormatSupported(){
        if(isNativeRequest()){
            return isNativeResponseSupported;
        } else if(!isNativeRequest()){
            return isHTMLResponseSupported;
        }

        return false;
    }


    private IXBidRequest createBidRequestObject(final List<Impression> impresssionlist, final Site site, final App app,
                                                final User user, final Device device,final Regs regs) {
        IXBidRequest tempBidRequest = new IXBidRequest(impresssionlist);

        tempBidRequest.setId(casInternalRequestParameters.auctionId);
        tempBidRequest.setTmax(tmax);

        LOG.debug("INSIDE CREATE BID REQUEST OBJECT");

        if (site != null) {
            tempBidRequest.setSite(site);
        } else if (app != null) {
            tempBidRequest.setApp(app);
        } else {
            LOG.debug("App and Site both object can not be null so returning");
            return null;
        }

        tempBidRequest.setDevice(device);
        tempBidRequest.setUser(user);
        tempBidRequest.setRegs(regs);
        return tempBidRequest;
    }


    private String serializeBidRequest() {

        TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());

        String tempBidRequestJson;

        try {

            tempBidRequestJson = serializer.toString(bidRequest);
            if(isNativeRequest()){
                tempBidRequestJson = tempBidRequestJson.replaceFirst("nativeObject", "native");
            }

            LOG.info("IX request json is : {}", tempBidRequestJson);
        }
        catch (TException e) {
            LOG.debug("Could not create json from bidrequest for partner {}", advertiserName);
            LOG.info("Configure parameters inside IX returned false {}", advertiserName);
            return null;
        }
        LOG.info("return true");

        return tempBidRequestJson;
    }


    private Regs createRegsObject()
    {
        Regs regs= new Regs();
        if(isWapSiteUACEntity) {
            if (wapSiteUACEntity.isCoppaEnabled()) {
                regs.setCoppa(1);
                isCoppaSet = true;
            } else {
                regs.setCoppa(0);
            }
        }
        return regs;
    }


    private ProxyDemand createProxyDemandObject() {
        ProxyDemand proxyDemand = new ProxyDemand();
        proxyDemand.setMarketrate(Math.max(sasParams.getMarketRate(),casInternalRequestParameters.auctionBidFloor));
        return proxyDemand;
    }


    private Impression createImpressionObject(final Banner banner, final String displayManager,
                                              final String displayManagerVersion, final ProxyDemand proxyDemand) {

        Impression impression;

        if (null != casInternalRequestParameters.impressionId) {
            /**
             * We were originally passing the guid impression id in the RP response, but in order to conform to the
             * rubicon spec, we are now passing a unique identifier whose value starts with 1, and increments up to n
             * for n impressions).
             * impression = new Impression(casInternalRequestParameters.impressionId);
             */
            impression = new Impression("1");
        } else {
            LOG.info("Impression id can not be null in Cas Internal Request Params");
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
        } else {
            impression.setInstl(0);
        }

        impression.setBidfloor(casInternalRequestParameters.auctionBidFloor);

        LOG.debug("Bid floor is {}", impression.getBidfloor());



        CommonExtension impExt = new CommonExtension();

        JSONObject additionalParams= entity.getAdditionalParams();

        if (null != additionalParams) {
            String zoneId = getZoneId(additionalParams);
            if (null != zoneId) {
                RubiconExtension rp = new RubiconExtension();
                rp.setZone_id(zoneId);
                impExt.setRp(rp);
            } else{
                LOG.debug("zone id not present, will say false");
                InspectorStats.incrementStatCount(InspectorStrings.IX_ZONE_ID_NOT_PRESENT);
                return null;
                //zoneID not available so returning NULL
            }
        }
        impression.setExt(impExt);

        return impression;
    }


    public String getZoneId(JSONObject additionalParams) {
        String categoryZoneId = null;
        boolean isCategorySet = false;

        try {
            if (sasParams.getCategories() != null) {
                for (int index = 0; index < sasParams.getCategories().size(); index++) {
                    String categoryIdKey = sasParams.getCategories().get(index).toString();
                    if (additionalParams.has(categoryIdKey)) {
                        categoryZoneId = additionalParams.getString(categoryIdKey);
                        LOG.debug("category Id is {}", categoryZoneId);
                    }
                    if (categoryZoneId != null) {
                        isCategorySet = true;
                        break;
                    }
                }
            }
            if (isCategorySet == false && additionalParams.has("default")) {
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

        final CommonExtension ext= new CommonExtension();
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            if (slotIdMap.containsKey(sasParams.getSlot())) {
                final RubiconExtension rp = new RubiconExtension();
                rp.setSize_id(slotIdMap.get(sasParams.getSlot()));
                ext.setRp(rp);
            }
        }

        banner.setExt(ext);
        return banner;
    }


    private Geo createGeoObject() {
        Geo geo = new Geo();
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0
                && (!isCoppaSet)) {
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

        return user;
    }


    private Site createSiteObject() {
        Site site = new Site();

        JSONObject additionalParams= entity.getAdditionalParams();
        Integer rubiconSiteId;
        try {
            rubiconSiteId = Integer.parseInt(additionalParams.getString("site"));
        } catch (JSONException e) {
            LOG.debug("Site Id is not configured");
            InspectorStats.incrementStatCount(InspectorStrings.IX_SITE_ID_NOT_PRESENT);
            return null;
        }

        if(isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()) {
            site.setId(sasParams.getSiteId());
            String tempSiteUrl = wapSiteUACEntity.getSiteUrl();
            if (StringUtils.isNotEmpty(tempSiteUrl)) {
                site.setPage(tempSiteUrl);
                site.setDomain(tempSiteUrl);
            }
            if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())){
                site.setName(wapSiteUACEntity.getSiteName());
            }

        } else {
            site.setId(getBlindedSiteId(sasParams.getSiteIncId(), entity.getIncId(getCreativeType())));

            if (isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())) {
                site.setName(wapSiteUACEntity.getAppType());
            } else {
                String category = getCategories(',',false);
                if(category != null) {
                    site.setName(category);
                }
            }
        }

        List <String> blockedList= getBlockedList();
        site.setBlocklists(blockedList);
        final Publisher publisher = new Publisher();
        if(null != sasParams.getCategories()){
            publisher.setCat(iabCategoriesInterface.getIABCategories(sasParams.getCategories()));
        }

        final CommonExtension publisherExtensions = new CommonExtension();
        final RubiconExtension rpForPub = new RubiconExtension();
        rpForPub.setAccount_id(accountId);
        publisherExtensions.setRp(rpForPub);
        publisher.setExt(publisherExtensions);
        site.setPublisher(publisher);

        final AdQuality adQuality = createAdQuality();
        site.setAq(adQuality);

        final Transparency transparency = createTransparency();
        site.setTransparency(transparency);

        final CommonExtension ext= new CommonExtension();



        final RubiconExtension rpForSite = new RubiconExtension();
        rpForSite.setSite_id(rubiconSiteId);
        ext.setRp(rpForSite);

        site.setExt(ext);
        return site;
    }


    private AdQuality createAdQuality() {
        AdQuality adQuality = new AdQuality();
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            adQuality.setSensitivity("low");
        } else {
            adQuality.setSensitivity("high");
        }
        return adQuality;
    }


    private Transparency createTransparency() {

        Transparency transparency = new Transparency();
        if(isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled())
        {
            transparency.setBlind(0);
            if(null != wapSiteUACEntity.getBlindList()) {
                transparency.setBlindbuyers(wapSiteUACEntity.getBlindList());
            } else if (globalBlindFromConfig.size() > 0 && !globalBlindFromConfig.get(0).isEmpty()) {
                List<Integer> globalBlind = Lists.newArrayList();
                for (String s : globalBlindFromConfig){
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
        App app = new App();
        JSONObject additionalParams= entity.getAdditionalParams();
        Integer rubiconSiteId;
        try {
            rubiconSiteId = Integer.parseInt(additionalParams.getString("site"));
        } catch (JSONException e) {
            LOG.debug("Site Id is not configured");
            InspectorStats.incrementStatCount(InspectorStrings.IX_SITE_ID_NOT_PRESENT);
            return null;
        }

        if(isWapSiteUACEntity && wapSiteUACEntity.isTransparencyEnabled()){
            app.setId(sasParams.getSiteId());
            if(StringUtils.isNotEmpty(wapSiteUACEntity.getSiteUrl())) {
                app.setStoreurl(wapSiteUACEntity.getSiteUrl());
            }
            if(StringUtils.isNotEmpty(wapSiteUACEntity.getMarketId())) {
                app.setBundle(wapSiteUACEntity.getBundleId());
            }
            if(StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
                app.setName(wapSiteUACEntity.getSiteName());
            }
        } else {
            app.setId(getBlindedSiteId(sasParams.getSiteIncId(), entity.getIncId(getCreativeType())));

            if (isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())) {
                app.setName(wapSiteUACEntity.getAppType());
            } else {
                String category = getCategories(',',false);
                if(category != null) {
                    app.setName(category);
                }
            }
        }
        List <Long> tempSasCategories = sasParams.getCategories();

        if(null != tempSasCategories){
            app.setCat(iabCategoriesInterface.getIABCategories(tempSasCategories));
        }

        List <String> blockedList= getBlockedList();
        app.setBlocklists(blockedList);


        final Publisher publisher = new Publisher();
        if(null != tempSasCategories) {
            publisher.setCat(iabCategoriesInterface.getIABCategories(tempSasCategories));
        }

        final CommonExtension publisherExtensions = new CommonExtension();
        final RubiconExtension rpForPub = new RubiconExtension();
        rpForPub.setAccount_id(accountId);
        publisherExtensions.setRp(rpForPub);
        publisher.setExt(publisherExtensions);
        app.setPublisher(publisher);

        final AdQuality adQuality = createAdQuality();
        app.setAq(adQuality);

        final Transparency transparency = createTransparency();
        app.setTransparency(transparency);

        final CommonExtension ext= new CommonExtension();

        final RubiconExtension rpForApp = new RubiconExtension();
        rpForApp.setSite_id(rubiconSiteId);
        ext.setRp(rpForApp);

        app.setExt(ext);

        return app;
    }


    public List <String> getBlockedList() {
        List<String> blockedList = Lists.newArrayList();
        LOG.debug("{}",sasParams.getSiteIncId());
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
        } else if (null != casInternalRequestParameters.uidO1) {
            device.setDidsha1(casInternalRequestParameters.uidO1);
            device.setDpidsha1(casInternalRequestParameters.uidO1);
        }

        // Setting platform id md5 hashed
        if (null != casInternalRequestParameters.uidMd5) {
            device.setDidmd5(casInternalRequestParameters.uidMd5);
            device.setDpidmd5(casInternalRequestParameters.uidMd5);
        } else if (null != casInternalRequestParameters.uid) {
            device.setDidmd5(casInternalRequestParameters.uid);
            device.setDpidmd5(casInternalRequestParameters.uid);
        }

        // Setting Extension for ifa
        if (!StringUtils.isEmpty(casInternalRequestParameters.uidIFA) && (!isCoppaSet)) {
            device.setIfa(casInternalRequestParameters.uidIFA);
        }

        //  if (!StringUtils.isEmpty(casInternalRequestParameters.gpid)) {

        final CommonExtension ext= new CommonExtension();

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
        } else {
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
        } else {
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
        } else {
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
                // TODO add nativeAdBuilding();
                LOG.debug("we do not support native request");
            } else {
                nonNativeAdBuilding();
            }

        }
        LOG.debug("response length is {}", responseContent.length());
        LOG.debug("response is {}",responseContent);
    }


    public boolean updateDSPAccountInfo(String buyer) {
        LOG.debug("Inside updateDSPAccountInfo");
        // Get Inmobi account id for the DSP on Rubicon side
        IXAccountMapEntity ixAccountMapEntity = repositoryHelper.queryIXAccountMapRepository(Long.parseLong(buyer));
        if (null == ixAccountMapEntity) {
            LOG.error("Invalid Rubicon DSP id: DSP id:{}", buyer);
            return false;
        }
        String accountId = ixAccountMapEntity.getInmobiAccountId();

        // Get collection of Channel Segment Entities for the particular Inmobi account id
        ChannelAdGroupRepository channelAdGroupRepository = repositoryHelper.getChannelAdGroupRepository();
        if (null == channelAdGroupRepository) {
            LOG.error("Channel AdGroup Repository is null.");
            return false;
        }

        Collection<ChannelSegmentEntity> adGroupMap = channelAdGroupRepository.getEntities(accountId);

        if (adGroupMap.isEmpty()) {
            // If collection is empty
            LOG.error("Channel Segment Entity collection for Rubicon DSP is empty: DSP id:{}, inmobi account id:{}", buyer, accountId);
            return false;
        } else {
            // Else picking up the first channel segment entity and assuming that to be the correct entity
            this.dspChannelSegmentEntity = adGroupMap.iterator().next();

            // Create a new ChannelSegment with DSP information. So that, all the logging happens on DSP Id.
            // this.auctionResponse = new ChannelSegment(dspChannelSegmentEntity, null, null, null, null,
            //        auctionResponse.getAdNetworkInterface(), -1L);

            // Get response creative type and get the incId for the respective response creative type
            ADCreativeType responseCreativeType = this.getCreativeType();
            long incId = this.dspChannelSegmentEntity.getIncId(responseCreativeType);

            String oldImpressionId = this.getImpressionId();
            LOG.debug("Old impression id: {}", oldImpressionId);

            // Generating new impression id
            LOG.debug("Creating new impression id from incId: {}", incId);
            String newImpressionId = ImpressionIdGenerator.getInstance().getImpressionId(incId);

            if (StringUtils.isNotEmpty(newImpressionId)) {
                // Update beacon and click URLs to refer to the video Ads.
                this.beaconUrl = this.beaconUrl.replace(this.getImpressionId(), newImpressionId);
                this.clickUrl = this.clickUrl.replace(this.getImpressionId(), newImpressionId);
                this.impressionId = newImpressionId;

                LOG.debug("Replaced impression id to new value {}.", newImpressionId);
            }

            return true;
        }
    }


    @Override
    public void processResponse() {
        LOG.debug("Inside process Response for the partner: {}", getName());
        if (isRequestComplete) {
            LOG.debug("Already cleaned up so returning from process response");
            return;
        }
        LOG.debug("Inside process Response for the partner: {}", getName());
        getResponseAd();
        isRequestComplete = true;
        if (baseRequestHandler.getAuctionEngine().areAllChannelSegmentRequestsComplete()) {
            LOG.debug("areAllChannelSegmentRequestsComplete is true");
            if (baseRequestHandler.getAuctionEngine().isAuctionComplete()) {
                LOG.debug("IX Auction has run already");
                if (baseRequestHandler.getAuctionEngine().isAuctionResponseNull()) {
                    LOG.debug("IX Auction has returned null");
                    // Sending no ad response and cleaning up channel
                    // (processDcpPartner is skipped because the selected adNetworkInterface
                    // will always be the last entry and a No Ad Response will be sent)
                    baseRequestHandler.sendNoAdResponse(serverChannel);
                    baseRequestHandler.cleanUp();
                    return;
                }
                LOG.debug("IX Auction response is not null so sending auction response");
                return;
            } else {
                AdNetworkInterface highestBid = baseRequestHandler.getAuctionEngine().runAuctionEngine();
                if (highestBid != null) {
                    // Update Response ChannelSegment
                    baseRequestHandler.getAuctionEngine().updateIXChannelSegment(dspChannelSegmentEntity);

                    LOG.debug("Sending IX auction response of {}", highestBid.getName());
                    baseRequestHandler.sendAdResponse(highestBid, serverChannel);
                    // highestBid.impressionCallback();
                    LOG.debug("Sent IX auction response");
                    return;
                } else {
                    LOG.debug("IX auction has returned null");
                    // Sending no ad response and cleaning up channel
                    // processDcpList is skipped
                    baseRequestHandler.sendNoAdResponse(serverChannel);
                    baseRequestHandler.cleanUp();
                }
            }
        }
        LOG.debug("IX Auction has not run so waiting....");
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
        } else {
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
        } catch (Exception e) {
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


    //This function not used, for future use
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
                LOG.error("BidResponse does not have seat bid object");
                return false;
            }
            //bidderCurrency is to USD by default
            SeatBid seatBid=bidResponse.getSeatbid().get(0);
            if(null == seatBid.getBid() || seatBid.getBidSize() == 0) {
                LOG.error("Seat bid object does not have bid object");
                return false;
            }
            setBidPriceInLocal(seatBid.getBid().get(0).getPrice());
            setBidPriceInUsd(getBidPriceInLocal());
            responseSeatId = seatBid.getSeat();
            responseBidObjCount = seatBid.getBid().size();
            Bid bid =  seatBid.getBid().get(0);
            adm = bid.getAdm();
            responseImpressionId = bid.getImpid();
            creativeId = bid.getCrid();
            responseAuctionId = bidResponse.getId();
            pmptier = bid.getPmptier();
            //estimated = bid.getEstimated(); //Not used currently
            aqid = bid.getAqid();
            adjustbid = bid.getAdjustbid();
            dealId = bid.getDealid();

            return updateDSPAccountInfo(seatBid.getBuyer());
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
    public Integer returnPmpTier() { return pmptier; }


    @Override
    public String returnAqid() { return aqid; }


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
        this.secondBidPriceInLocal = price;
        LOG.debug("responseContent before replaceMacros is {}", this.responseContent);
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


    public void setImpressionId(String impressionId) {
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