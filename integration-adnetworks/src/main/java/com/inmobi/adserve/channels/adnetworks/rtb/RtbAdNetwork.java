package com.inmobi.adserve.channels.adnetworks.rtb;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.Utils.ClickUrlsRegenerator;
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

/**
 * Generic RTB adapter.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public class RtbAdNetwork extends BaseAdNetworkImpl {

  private final static Logger LOG = LoggerFactory.getLogger(RtbAdNetwork.class);

  @Getter
  @Setter
  private String urlBase;
  @Getter
  @Setter
  private String urlArg;
  @Getter
  @Setter
  private String rtbMethod;
  @Getter
  @Setter
  private String rtbVer;
  @Getter
  @Setter
  private String callbackUrl;
  @Setter
  private double bidPriceInUsd;
  @Setter
  private double bidPriceInLocal;
  @Getter
  @Setter
  BidRequest bidRequest;
  @Getter
  @Setter
  BidResponse bidResponse;
  private final boolean wnRequired;
  private static final int AUCTION_TYPE = 2;
  private int tmax = 200;
  private boolean templateWN = true;
  private static final String X_OPENRTB_VERSION = "x-openrtb-version";
  private static final String CONTENT_TYPE = "application/json";
  private static final String DISPLAY_MANAGER_INMOBI_SDK = "inmobi_sdk";
  private static final String DISPLAY_MANAGER_INMOBI_JS = "inmobi_js";
  private final String advertiserId;
  public static ImpressionCallbackHelper impressionCallbackHelper;
  private final IABCategoriesInterface iabCategoriesInterface;
  private final IABCountriesInterface iabCountriesInterface;
  private final boolean siteBlinded;
  private final String advertiserName;
  private double secondBidPriceInUsd = 0;
  private double secondBidPriceInLocal = 0;
  private String bidRequestJson = "";
  protected static final String MRAID = "<script src=\"mraid.js\" ></script>";
  private String encryptedBid;
  private static List<String> image_mimes = Arrays.asList("image/jpeg", "image/gif", "image/png");
  private static List<Integer> fsBlockedAttributes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13, 14, 15, 16);
  private static List<Integer> performanceBlockedAttributes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
      14, 15, 16);
  private static List<Integer> videoBlockedAttributes = Arrays.asList(7, 8, 9, 10, 14);
  private static List<Integer> videoBlockCreativeType = Arrays.asList(4); // iframe

  private static final String FAMILY_SAFE_RATING = "1";
  private static final String PERFORMANCE_RATING = "0";
  private static final String RATING_KEY = "fs";
  private static final String NO_AD = "NO_AD";
  private String responseSeatId;
  private String responseImpressionId;
  private String responseAuctionId;
  private String creativeId;
  private String sampleImageUrl;
  private List<String> advertiserDomains;
  private List<Integer> creativeAttributes;
  private boolean logCreative = false;
  private String adm;
  private final RepositoryHelper repositoryHelper;
  private String bidderCurrency = "USD";
  private static final String USD = "USD";
  private final List<String> blockedAdvertisers = Lists.newArrayList();

  private static final List<String> VIDEO_MIMES = Arrays.asList("video/mp4");
  private static final int EXT_VIDEO_LINEARITY = 1; // only linear ads
  private static final int EXT_VIDEO_MINDURATION = 15; // in secs.
  private static final int EXT_VIDEO_MAXDURATION = 30; // in secs.
  private static final List<String> EXT_VIDEO_TYPE = Arrays.asList("VAST 2.0", "VAST 3.0", "VAST 2.0 Wrapper",
      "VAST 3.0 Wrapper");

  @Getter
  static List<String> currenciesSupported = new ArrayList<String>(Arrays.asList("USD", "CNY", "JPY", "EUR", "KRW",
      "RUB"));
  @Getter
  static List<String> blockedAdvertiserList = new ArrayList<String>(Arrays.asList("king.com", "supercell.net",
      "paps.com", "fhs.com", "china.supercell.com", "supercell.com"));

  @Inject
  private static AsyncHttpClientProvider asyncHttpClientProvider;

  @Inject
  private static NativeTemplateAttributeFinder nativeTemplateAttributeFinder;

  @Inject
  private static NativeBuilderFactory nativeBuilderfactory;

  @Inject
  private static NativeResponseMaker nativeResponseMaker;


  private static final String NATIVE_STRING = "native";

  @Override
  protected AsyncHttpClient getAsyncHttpClient() {
    return asyncHttpClientProvider.getRtbAsyncHttpClient();
  }

  public RtbAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
      final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String urlBase,
      final String advertiserName, final int tmax, final RepositoryHelper repositoryHelper,
      final boolean templateWinNotification) {

    super(baseRequestHandler, serverChannel);
    advertiserId = config.getString(advertiserName + ".advertiserId");
    urlArg = config.getString(advertiserName + ".urlArg");
    rtbVer = config.getString(advertiserName + ".rtbVer", "2.0");
    callbackUrl = config.getString(advertiserName + ".wnUrlback");
    rtbMethod = config.getString(advertiserName + ".rtbMethod");
    wnRequired = config.getBoolean(advertiserName + ".isWnRequired");
    siteBlinded = config.getBoolean(advertiserName + ".siteBlinded");
    this.clientBootstrap = clientBootstrap;
    this.urlBase = urlBase;
    setRtbPartner(true);
    iabCategoriesInterface = new IABCategoriesMap();
    iabCountriesInterface = new IABCountriesMap();
    this.advertiserName = advertiserName;
    this.tmax = tmax;
    this.repositoryHelper = repositoryHelper;
    templateWN = templateWinNotification;
    isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", true);
    isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", false);
    isBannerVideoResponseSupported = config.getBoolean(advertiserName + ".bannerVideoSupported", false);
    blockedAdvertisers.addAll(blockedAdvertiserList);
  }

  @Override
  protected boolean configureParameters() {

    LOG.debug(traceMarker, "inside configureParameters of RTB");

    if (!checkIfBasicParamsAvailable()) {
      return false;
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
    String displayManager = null;
    String displayManagerVersion = null;
    if (null != sasParams.getSdkVersion()) {
      displayManager = DISPLAY_MANAGER_INMOBI_SDK;
      displayManagerVersion = sasParams.getSdkVersion();
    } else if (null != sasParams.getAdcode() && "JS".equalsIgnoreCase(sasParams.getAdcode())) {
      displayManager = DISPLAY_MANAGER_INMOBI_JS;
    }
    final Impression impression = createImpressionObject(banner, displayManager, displayManagerVersion);
    if (null == impression) {
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
    if (isNativeRequest()) {
      return isNativeResponseSupported;
    } else {
      return isHTMLResponseSupported;
    }
  }

  private boolean createBidRequestObject(final List<Impression> impresssionlist, final Site site, final App app,
      final User user, final Device device) {
    // nullcheck for casInternalRequestParams and sasParams done while configuring adapter
    bidRequest = new BidRequest(casInternalRequestParameters.getAuctionId(), impresssionlist);
    bidRequest.setTmax(tmax);
    bidRequest.setAt(AUCTION_TYPE);
    bidRequest.setCur(Collections.<String>emptyList());
    final List<String> seatList = new ArrayList<String>();
    seatList.add(advertiserId);
    bidRequest.setWseat(seatList);
    final HashSet<String> bCatSet = new HashSet<>();

    if (null != casInternalRequestParameters.getBlockedIabCategories()) {
      bCatSet.addAll(casInternalRequestParameters.getBlockedIabCategories());
      LOG.debug(traceMarker, "blockedCategories are {}", casInternalRequestParameters.getBlockedIabCategories());
    }
    // Setting blocked categories
    if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
      bCatSet.addAll(iabCategoriesInterface.getIABCategories(IABCategoriesMap.PERFORMANCE_BLOCK_CATEGORIES));
    } else {
      bCatSet.addAll(iabCategoriesInterface.getIABCategories(IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES));
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
        bidRequestJson = bidRequestJson.replaceFirst("nativeObject", "native");
      }
      LOG.info(traceMarker, "RTB request json is : {}", bidRequestJson);
    } catch (final TException e) {
      LOG.debug(traceMarker, "Could not create json from bidrequest for partner {}", advertiserName);
      LOG.info(traceMarker, "Configure parameters inside rtb returned false {}, exception raised {}", advertiserName, e);
      return false;
    }
    LOG.info(traceMarker, "Configure parameters inside rtb returned true");
    return true;
  }

  private Impression createImpressionObject(final Banner banner, final String displayManager,
      final String displayManagerVersion) {
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
    impression.setBidfloorcur(USD);
    // Set interstitial or not
    if (null != sasParams.getRqAdType() && "int".equalsIgnoreCase(sasParams.getRqAdType())) {
      impression.setInstl(1);
    } else {
      impression.setInstl(0);
    }

    impression.setBidfloor(casInternalRequestParameters.getAuctionBidFloor());
    LOG.debug(traceMarker, "Bid floor is {}", impression.getBidfloor());

    if (null != displayManager) {
      impression.setDisplaymanager(displayManager);
    }
    if (null != displayManagerVersion) {
      impression.setDisplaymanagerver(displayManagerVersion);
    }

    if (isNativeResponseSupported && isNativeRequest()) {
      final ImpressionExtensions impExt = createNativeExtensionObject();

      if (impExt == null) {
        return null;
      }
      impression.setExt(impExt);
    }
    return impression;
  }


  private ImpressionExtensions createNativeExtensionObject() {
    // Native nat = new Native();
    // nat.setMandatory(nativeTemplateAttributeFinder.findAttribute(new MandatoryNativeAttributeType()));
    // nat.setImage(nativeTemplateAttributeFinder.findAttribute(new ImageNativeAttributeType()));
    final NativeAdTemplateEntity templateEntity =
        repositoryHelper.queryNativeAdTemplateRepository(sasParams.getSiteId());
    if (templateEntity == null) {
      LOG.info(traceMarker, String.format("This site id %s doesn't have native template :", sasParams.getSiteId()));
      return null;
    }
    final NativeBuilder nb = nativeBuilderfactory.create(templateEntity);
    final Native nat = nb.build();
    // TODO: for native currently there is no way to identify MRAID traffic/container supported by publisher.
    // if(!StringUtils.isEmpty(sasParams.getSdkVersion())){
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
    if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
      final Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
      banner.setW((int) dim.getWidth());
      banner.setH((int) dim.getHeight());
    }

    // api type is always mraid
    if (!StringUtils.isEmpty(sasParams.getSdkVersion()) && sasParams.getSdkVersion().length() > 1) {
      final List<Integer> apis = new ArrayList<Integer>();
      apis.add(3);
      banner.setApi(apis);
    }

    // mime types a static list
    banner.setMimes(image_mimes);

    // Setting battributes
    if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
      banner.setBattr(performanceBlockedAttributes);
    } else {
      banner.setBattr(fsBlockedAttributes);
    }

    // This request supports in-Banner Video on interstitial slot and this partner supports video.
    if (sasParams.isBannerVideoSupported() && isBannerVideoResponseSupported) {
      // Set video specific attributes to the Banner object
      banner.setBattr(videoBlockedAttributes);
      banner.setBtype(videoBlockCreativeType);
      banner.setPos(1); // above the fold

      final List<String> allMimes = new ArrayList<String>(VIDEO_MIMES);
      allMimes.addAll(image_mimes);
      banner.setMimes(allMimes);

      final BannerExtensions ext = createBannerExtensionsObject();
      banner.setExt(ext);
    }
    return banner;
  }

  private BannerExtensions createBannerExtensionsObject() {
    // Create Banner->ext->video Object
    final BannerExtVideo video = new BannerExtVideo();
    video.setLinearity(EXT_VIDEO_LINEARITY);
    video.setMinduration(EXT_VIDEO_MINDURATION);
    video.setMaxduration(EXT_VIDEO_MAXDURATION);
    video.setType(EXT_VIDEO_TYPE);

    final BannerExtensions bannerExtensions = new BannerExtensions();
    bannerExtensions.setVideo(video);

    return bannerExtensions;
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
      geo.setCountry(iabCountriesInterface.getIabCountry(sasParams.getCountryCode()));
    }
    /*if (null != iabCitiesInterface.getIABCity(sasParams.getCity() + "")) {
        geo.setCity(iabCitiesInterface.getIABCity(sasParams.getCity() + ""));
    }*/
    geo.setZip(casInternalRequestParameters.getZipCode());
    // Setting type of geo data
    if ("DERIVED_LAT_LON".equalsIgnoreCase(sasParams.getLocSrc())) {
      geo.setType(1);
    } else if ("LATLON".equalsIgnoreCase(sasParams.getLocSrc())) {
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

    try {
      if (sasParams.getAge() != null) {
        final int age = sasParams.getAge();
        final int year = Calendar.getInstance().get(Calendar.YEAR);
        final int yob = year - age;
        user.setYob(yob);
      }
    } catch (final NumberFormatException e) {
      LOG.debug(traceMarker, "Exception : {}", e);
    }
    return user;
  }

  private Site createSiteObject() {
    Site site = null;
    if (siteBlinded) {
      site = new Site(getBlindedSiteId(sasParams.getSiteIncId(), entity.getIncId(getCreativeType())));
    } else {
      site = new Site(sasParams.getSiteId());
    }
    if (null != sasParams.getCategories()) {
      site.setCat(iabCategoriesInterface.getIABCategories(sasParams.getCategories()));
    }
    String category = null;
    if (sasParams.getWapSiteUACEntity() != null && StringUtils.isNotEmpty(sasParams.getWapSiteUACEntity().getAppType())) {
      site.setName(sasParams.getWapSiteUACEntity().getAppType());
    } else if ((category = getCategories(',', false)) != null) {
      site.setName(category);
    }
    final Map<String, String> siteExtensions = new HashMap<String, String>();
    String siteRating;
    if (!SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
      // Family safe
      siteRating = FAMILY_SAFE_RATING;
    } else {
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
    } else {
      app = new App(sasParams.getSiteId());
    }
    if (null != sasParams.getCategories()) {
      app.setCat(iabCategoriesInterface.getIABCategories(sasParams.getCategories()));
    }
    String category = null;
    final WapSiteUACEntity entity = sasParams.getWapSiteUACEntity();

    if (entity != null && StringUtils.isNotEmpty(entity.getAppType())) {
      app.setName(entity.getAppType());
    } else if ((category = getCategories(',', false)) != null) {
      app.setName(category);
    }

    // set App Ext fields
    final AppExt ext = createAppExt(entity);

    app.setExt(ext);
    return app;
  }

  private AppExt createAppExt(final WapSiteUACEntity entity) {
    final AppExt ext = new AppExt();

    String appRating;
    if (!SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
      // Family safe
      appRating = FAMILY_SAFE_RATING;
    } else {
      appRating = PERFORMANCE_RATING;
    }

    ext.setFs(appRating);

    if (entity != null) {
      final AppStore store = new AppStore();
      if (!StringUtils.isEmpty(entity.getContentRating())) {
        store.setRating(entity.getContentRating());
      }
      if (!StringUtils.isEmpty(entity.getAppType())) {
        store.setCat(entity.getAppType());
      }
      if (entity.getCategories() != null && !entity.getCategories().isEmpty()) {
        store.setSeccat(entity.getCategories());
      }
      ext.setStore(store);
    }
    return ext;
  }

  private Device createDeviceObject(final Geo geo) {
    final Device device = new Device();
    device.setDevicetype(1);// Tablets and Mobiles
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
    if (NetworkType.WIFI == sasParams.getNetworkType()) {
      device.setConnectiontype(2);
    } else {
      device.setConnectiontype(0);
    }

    // Setting do not track
    if (null != casInternalRequestParameters.getUidADT()) {
      try {
        device.setDnt(Integer.parseInt(casInternalRequestParameters.getUidADT()) == 0 ? 1 : 0);
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

    // Setting Extension for idfa
    if (!StringUtils.isEmpty(casInternalRequestParameters.getUidIFA())) {
      final Map<String, String> deviceExtensions = getDeviceExt(device);
      deviceExtensions.put("idfa", casInternalRequestParameters.getUidIFA());
      deviceExtensions.put("idfasha1", getHashedValue(casInternalRequestParameters.getUidIFA(), "SHA-1"));
      deviceExtensions.put("idfamd5", getHashedValue(casInternalRequestParameters.getUidIFA(), "MD5"));
    }

    if (!StringUtils.isEmpty(casInternalRequestParameters.getGpid())) {
      final Map<String, String> deviceExtensions = getDeviceExt(device);
      deviceExtensions.put("gpid", casInternalRequestParameters.getGpid());
    }
    return device;
  }

  private Map<String, String> getDeviceExt(final Device device) {
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
        new RequestBuilder().setUrl(uriCallBack.toASCIIString()).setMethod("POST")
            .setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE)
            .setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(body.length)).setBody(body)
            .setHeader(HttpHeaders.Names.HOST, uriCallBack.getHost()).build();

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
      url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_ENCRYPTED_INSENSITIVE, encryptedBid);
      url = url.replaceAll(RTBCallbackMacros.AUCTION_PRICE_INSENSITIVE, Double.toString(secondBidPriceInLocal));
    }
    if (null != bidResponse.getSeatbid().get(0).getBid().get(0).getAdid()) {
      url =
          url.replaceAll(RTBCallbackMacros.AUCTION_AD_ID_INSENSITIVE, bidResponse.getSeatbid().get(0).getBid().get(0)
              .getAdid());
    }
    if (null != bidResponse.bidid) {
      url = url.replaceAll(RTBCallbackMacros.AUCTION_BID_ID_INSENSITIVE, bidResponse.bidid);
    }
    if (null != bidResponse.getSeatbid().get(0).getSeat()) {
      url = url.replaceAll(RTBCallbackMacros.AUCTION_SEAT_ID_INSENSITIVE, bidResponse.getSeatbid().get(0).getSeat());
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
  protected Request getNingRequest() throws Exception {
    final byte[] body = bidRequestJson.getBytes(CharsetUtil.UTF_8);

    URI uri = getRequestUri();
    if (uri.getPort() == -1) {
      uri = new URIBuilder(uri).setPort(80).build();
    }

    String httpRequestMethod;
    if ("get".equalsIgnoreCase(rtbMethod)) {
      httpRequestMethod = "GET";
    } else {
      httpRequestMethod = "POST";
    }

    return new RequestBuilder(httpRequestMethod).setUrl(uri.toString())
        .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json").setBody(body)
        .setHeader(X_OPENRTB_VERSION, rtbVer).setHeader(HttpHeaders.Names.HOST, uri.getHost()).build();
  }

  @Override
  public URI getRequestUri() throws URISyntaxException {
    final StringBuilder url = new StringBuilder();
    if ("get".equalsIgnoreCase(rtbMethod)) {
      url.append(urlBase).append('?').append(urlArg).append('=');
    } else {
      url.append(urlBase);
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
      responseContent = "";
      return;
    } else {
      statusCode = status.code();
      final boolean parsedResponse = deserializeResponse(response);
      if (!parsedResponse) {
        adStatus = NO_AD;
        responseContent = "";
        statusCode = 500;
        LOG.info(traceMarker, "Error in parsing rtb response");
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
    LOG.debug(traceMarker, "response length is {}", responseContent.length());
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
      responseContent = Formatter.getResponseFromTemplate(TemplateType.RTB_HTML, velocityContext, sasParams, null);
    } catch (final Exception e) {
      adStatus = NO_AD;
      LOG.info(traceMarker, "Some exception is caught while filling the velocity template for partner{} {}",
          advertiserName, e);
    }

  }

  private void bannerVideoAdBuilding() {
    final VelocityContext velocityContext = new VelocityContext();

    final String vastContentJSEsc = StringEscapeUtils.escapeJavaScript(getAdMarkUp());
    velocityContext.put(VelocityTemplateFieldConstants.VAST_CONTENT_JS_ESC, vastContentJSEsc);

    // JS escaped WinUrl for partner.
    final String partnerWinUrl = getPartnerWinUrl();
    if (StringUtils.isNotEmpty(partnerWinUrl)) {
      velocityContext.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL,
          StringEscapeUtils.escapeJavaScript(partnerWinUrl));
    }

    // JS escaped IMWinUrl
    final String imWinUrl = beaconUrl + "?b=${WIN_BID}";
    velocityContext.put(VelocityTemplateFieldConstants.IM_WIN_URL, StringEscapeUtils.escapeJavaScript(imWinUrl));

    // JS escaped IM beacon and click URLs.
    velocityContext.put(VelocityTemplateFieldConstants.IM_BEACON_URL, StringEscapeUtils.escapeJavaScript(beaconUrl));
    velocityContext.put(VelocityTemplateFieldConstants.IM_CLICK_URL, StringEscapeUtils.escapeJavaScript(clickUrl));

    // SDK version
    velocityContext.put(VelocityTemplateFieldConstants.IMSDK_VERSION, sasParams.getSdkVersion());

    // Namespace
    velocityContext.put(VelocityTemplateFieldConstants.NAMESPACE, Formatter.getNamespace());

    // IMAIBaseUrl
    velocityContext.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());

    try {
      responseContent =
          Formatter.getResponseFromTemplate(TemplateType.RTB_BANNER_VIDEO, velocityContext, sasParams, null);
    } catch (final Exception e) {
      adStatus = NO_AD;
      LOG.info(traceMarker, "Some exception is caught while filling the velocity template for partner{} {}",
          advertiserName, e);
    }
  }

  private String getPartnerWinUrl() {
    String winUrl = "";
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

  @Override
  protected boolean isNativeRequest() {
    return NATIVE_STRING.equals(sasParams.getRFormat());
  }

  private void nativeAdBuilding() {

    final App app = bidRequest.getApp();

    final Map<String, String> params = new HashMap<String, String>();
    final String winUrl = beaconUrl + "?b=${WIN_BID}";
    params.put("beaconUrl", beaconUrl);
    params.put("winUrl", winUrl);
    params.put("impressionId", impressionId);
    if (app != null) {
      params.put("appId", app.getId());
    }
    try {
      params.put("siteId", sasParams.getSiteId());
      responseContent =
          nativeResponseMaker.makeResponse(bidResponse, params,
              repositoryHelper.queryNativeAdTemplateRepository(sasParams.getSiteId()));
    } catch (final Exception e) {

      adStatus = NO_AD;
      responseContent = "";
      LOG.error(
          "Some exception is caught while filling the native template for partner {}, advertiser = {}, exception = {}",
          e.getLocalizedMessage(), advertiserName, e);
    }

  }


  public boolean deserializeResponse(final String response) {
    final Gson gson = new Gson();
    try {
      bidResponse = gson.fromJson(response, BidResponse.class);
      LOG.debug(traceMarker, "Done with parsing of bidresponse");
      if (null == bidResponse || null == bidResponse.getSeatbid() || bidResponse.getSeatbidSize() == 0) {
        LOG.debug(traceMarker, "BidResponse does not have seat bid object");
        return false;
      }
      if (!StringUtils.isEmpty(bidResponse.getCur())) {
        bidderCurrency = bidResponse.getCur();
      }
      setBidPriceInLocal(bidResponse.getSeatbid().get(0).getBid().get(0).getPrice());
      setBidPriceInUsd(calculatePriceInUSD(getBidPriceInLocal(), bidderCurrency));
      responseSeatId = bidResponse.getSeatbid().get(0).getSeat();
      final Bid bid = bidResponse.getSeatbid().get(0).getBid().get(0);
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
    } catch (final NullPointerException e) {
      LOG.info(traceMarker, "Could not parse the rtb response from partner: {}, exception thrown {}", getName(), e);
      return false;
    }
  }

  /**
   * Checks if the RTB response is for video and contains a valid VAST response.
   * 
   * @return false - When a video response is received and it is NOT valid. true - 1) When the response does not contain
   *         video (banner response) 2) It contains video response which is a valid XML/URL.
   */
  private boolean checkBidResponseForBannerVideo(final BidExtensions ext) {

    if (ext != null && ext.getVideo() != null) {
      LOG.debug(traceMarker, "Received video response of type {}.", ext.getVideo().getType());

      if (!checkRequiredParametersInVideoResponse(ext)) {
        return false;
      }

      // A valid video response is received. Set the flag.
      isVideoResponseReceived = true;

      final String newImpressionId = casInternalRequestParameters.getImpressionIdForVideo();
      if (StringUtils.isNotEmpty(newImpressionId)) {

        // Update the response impression id so that this doesn't get filtered in AuctionImpressionIdFilter.
        if (impressionId.equalsIgnoreCase(responseImpressionId)) {
          responseImpressionId = newImpressionId;
        }

        // Update beacon and click URLs to refer to the video Ads.
        beaconUrl =
            ClickUrlsRegenerator.regenerateBeaconUrl(beaconUrl, getImpressionId(), newImpressionId,
                sasParams.isRichMedia());
        clickUrl = ClickUrlsRegenerator.regenerateClickUrl(clickUrl, getImpressionId(), newImpressionId);
        impressionId = newImpressionId;

        LOG.debug(traceMarker, "Replaced impression id to new value {}.", newImpressionId);
      }
    }
    return true;
  }

  private boolean checkRequiredParametersInVideoResponse(final BidExtensions ext) {
    // Validate the adm content for a valid URL/XML.
    if (!isValidURL(adm) && !isValidXMLFormat(adm)) {
      LOG.info(traceMarker, "Invalid VAST response adm - {}", adm);
      return false;
    }

    // Validate supported VAST type.
    if (!EXT_VIDEO_TYPE.contains(ext.getVideo().getType())) {
      LOG.info(traceMarker, "Unsupported VAST type - {}", ext.getVideo().getType());
      return false;
    }

    // Validate supported video duration.
    if (ext.getVideo().duration < EXT_VIDEO_MINDURATION || ext.getVideo().duration > EXT_VIDEO_MAXDURATION) {
      LOG.info(traceMarker, "VAST response video duration {} should be within {} and {}.",
          ext.getVideo().getDuration(), EXT_VIDEO_MINDURATION, EXT_VIDEO_MAXDURATION);
      return false;
    }

    // Validate Linearity
    if (ext.getVideo().linearity != EXT_VIDEO_LINEARITY) {
      LOG.info(traceMarker, "Linearity {} is not supported for the VAST response.", ext.getVideo().linearity);
      return false;
    }
    return true;
  }



  private boolean isValidURL(final String url) {
    final UrlValidator urlValidator = new UrlValidator();
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
      } catch (final URIException e) {
        LOG.info(traceMarker, "VAST XML response is NOT properly URL encode. {}", e);
        return false;
      }
    } else {
      xmlStr = StringEscapeUtils.unescapeJavaScript(encodedXmlStr);
    }

    // Validate the XML by parsing it.
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final InputSource source = new InputSource(new StringReader(xmlStr));
    try {
      final DocumentBuilder db = factory.newDocumentBuilder();
      db.setErrorHandler(null);
      db.parse(source);

      // Initially adm was URL encoded XML string. Replace it with decoded value.
      adm = xmlStr;
      return true;
    } catch (SAXException | ParserConfigurationException | IOException e) {
      LOG.debug(traceMarker, "VAST response is NOT a valid XML - {}", e);
      return false;
    }
  }

  private double calculatePriceInUSD(final double price, String currencyCode) {
    if (StringUtils.isEmpty(currencyCode)) {
      currencyCode = USD;
    }
    if (USD.equalsIgnoreCase(currencyCode)) {
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
  public void setSecondBidPrice(final Double price) {
    secondBidPriceInUsd = price;
    secondBidPriceInLocal = calculatePriceInLocal(price);
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
