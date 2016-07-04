package com.inmobi.adserve.channels.adnetworks.microsoft;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.contracts.microsoft.request.App;
import com.inmobi.adserve.contracts.microsoft.request.BidRequest;
import com.inmobi.adserve.contracts.microsoft.request.Content;
import com.inmobi.adserve.contracts.microsoft.request.Device;
import com.inmobi.adserve.contracts.microsoft.request.Geo;
import com.inmobi.adserve.contracts.microsoft.request.Imp;
import com.inmobi.adserve.contracts.microsoft.request.Native;
import com.inmobi.adserve.contracts.microsoft.request.NativeRequest;
import com.inmobi.adserve.contracts.microsoft.request.Publisher;
import com.inmobi.adserve.contracts.microsoft.request.Regs;
import com.inmobi.adserve.contracts.microsoft.request.User;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.types.DeviceType;
import com.ning.http.client.RequestBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.DISPLAY_MANAGER_INMOBI_JS;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.DISPLAY_MANAGER_INMOBI_SDK;

/**
 * Created by ghanshyam_sv on 25/5/16.
 */
public class DCPMicrosoftAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final String ADS_KEY = "ads";
    private static final String TARGETURL_KEY = "targetUrl";
    private static final String DESCRIPTION_KEY = "description";
    private static final String DISPLAYURL_KEY = "displayUrl";
    private static final String TITLE_KEY = "title";
    private static final String IMAGES_KEY = "images";
    private static final String IMAGEURL_KEY = "imageUrl";
    private static final String IMAGETYPE_KEY = "imageType";
    private static final String IMAGETYPEFAVICON_KEY = "Favicon";
    private static final String IMAGE_TYPE_ADVERTISER_UPLOAD_IMAGE_KEY = "AdvertiserUploadImage";
    private static final String DECORATIONS_KEY = "decorations";
    private static final String DECORATIONSTYPE_KEY = "_type";
    private static final String DECORAITONSTYPE_CALL = "Ads/CallExtension";
    private static final String DECORAITONSTYPE_LOCATION = "Ads/LocationExtension";
    private static final String PHONENUMBER_KEY = "PhoneNumber";
    private static final String PHONENUMBERURL_KEY = "PhoneNumberUrl";
    private static final String PHONEICONURL = "https://i.l.inmobicdn.net/banners/programmatic/call.png";
    private static final String DIRECTIONICONURL = "https://i.l.inmobicdn.net/banners/programmatic/direction.png";
    private static final String intentLinks = "intentLinks";



    private WapSiteUACEntity wapSiteUACEntity;
    private static final String DEVICE_TYPE_PHONE = "4";
    private static final String DEVICE_TYPE_TABLET = "5";
    private boolean isWapSiteUACEntity = false;
    protected boolean isCoppaSet = false;
    private String bidRequestJson = DEFAULT_EMPTY_STRING;
    private static final short AGE_LIMIT_FOR_COPPA = 8;
    private static Gson gson;
    private static String faviconImageUrl = "";
    private static String logoImageUrl = "";
    private static String logoCallUrl = "";
    private static String logoLocationUrl = "";
    private int height;
    private int width;
    @Inject
    protected static TemplateConfiguration templateConfiguration;

    @Getter
    @Setter
    BidRequest bidRequest;



    private static final Logger LOG = LoggerFactory.getLogger(DCPMicrosoftAdnetwork.class);

    public DCPMicrosoftAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
                                 final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        gson = new Gson();

    }


    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
            || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for microsoftdcp so exiting adapter");
            LOG.info("Configure parameters inside microsoftdcp returned false");
            return false;
        }
        host = config.getString("microsoft.host");
        if (sasParams.getWapSiteUACEntity() != null) {
            wapSiteUACEntity = sasParams.getWapSiteUACEntity();
            isWapSiteUACEntity = true;
        }

        Imp imp = createImpObject();
        App app = createAppObject();
        Device device = createDeviceObject();
        User user = createUserObject();
        Regs regs = createRegsObject();


        // Creating BidRequest Object using unique auction id per auction
        final boolean flag = createBidRequestObject(imp, app, device, user, regs);
        if (!flag) {
            return false;
        }

        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) dim.getWidth();
            height = (int)dim.getHeight();
        }

        // Serializing the bidRequest Object
        return serializeBidRequest();
    }

    private Imp createImpObject() {
        final Imp impObject = new Imp();
        impObject.setId(casInternalRequestParameters.getImpressionId());
        if (null != sasParams.getSdkVersion()) {
            impObject.setDisplaymanager(DISPLAY_MANAGER_INMOBI_SDK);
            impObject.setDisplaymanagerver(sasParams.getSdkVersion());
        } else if (null != sasParams.getAdcode() && "JS".equalsIgnoreCase(sasParams.getAdcode())) {
            impObject.setDisplaymanager(DISPLAY_MANAGER_INMOBI_JS);
        }

        final NativeRequest nativeRequestObject = new NativeRequest();
        if(!isNativeRequest()) {
            if (height == 480) {
                nativeRequestObject.setMaxnumberofads(2);
            } else {
                nativeRequestObject.setMaxnumberofads(1);
            }

            HashMap<String, String> templateMap = new HashMap<>();
            templateMap.put("728x90", "inmobi_b_728x90");
            templateMap.put("320x50", "inmobi_b_320x50");
            templateMap.put("300x250", "inmobi_b_300x250");
            templateMap.put("320x480", "inmobi_b_320x480");
            nativeRequestObject.setTemplateset(templateMap.get(width + "x" + height));
        }else{
            //create native request object
            nativeRequestObject.setMaxnumberofads(1);
            nativeRequestObject.setTemplateset("inmobi_native");
        }
        final Native nativeObject = new Native();
        nativeObject.setRequest(nativeRequestObject);
        impObject.setNat(nativeObject);

        return impObject;

    }


    private App createAppObject() {
        final App appObject = new App();
        String category = null;

        if (StringUtils.isBlank(appObject.getName()) && isWapSiteUACEntity && StringUtils.isNotEmpty(wapSiteUACEntity.getAppType())) {
            appObject.setName(wapSiteUACEntity.getAppType());
        } else if (StringUtils.isBlank(appObject.getName()) && (category = getCategories(',', false)) != null) {
            appObject.setName(category);
        }
        if (null != sasParams.getCategories()) {
            appObject.setCat(IABCategoriesMap.getIABCategories(sasParams.getCategories()));
        }


        appObject.setBundle(sasParams.getAppBundleId());
        appObject.setLanguage(sasParams.getLanguage());
        appObject.setCountry(sasParams.getCountryCode());
        appObject.setStoreurl(sasParams.getAppUrl());

        final Publisher pub = createPublisherObject();
        appObject.setPublisher(pub);

        final Content content = createContentObject();
        appObject.setContent(content);

        final Map<String, String> appExt = new HashMap<String, String>();
        appExt.put("RequestAgent", "MyAdnetworks");
        appExt.put("PreferredLanguage", "es");
        appObject.setExt(appExt);


        return appObject;
    }

    private Publisher createPublisherObject() {
        Publisher pubObject = new Publisher();
        pubObject.setName("InMobi");
        return pubObject;
    }



    private Content createContentObject() {
        Content contentObj = new Content();

        //contentObj.setCat(sasParams.getCategories());
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
        final Map<String, List<String>> contentExtensions = new HashMap<String, List<String>>();
        contentExtensions.put("bcat", bCatList);
        contentObj.setExt(contentExtensions);

        return contentObj;
    }

    private Device createDeviceObject() {
        final Device deviceObject = new Device();

        // Spec says - If “0”, then do not track Is set to false, if “1”, then do no track is set to true in browser.
        deviceObject.setDnt(String.valueOf(casInternalRequestParameters.isTrackingAllowed() ? 0 : 1));

        // lmt = 0 is false (i.e. lmt not enabled and is default). lmt = 1 is true (i.e. lmt is enabled)
        deviceObject.setLmt(String.valueOf(casInternalRequestParameters.isTrackingAllowed() ? 0 : 1));
        deviceObject.setUa(sasParams.getUserAgent());
        deviceObject.setIp(sasParams.getRemoteHostIp());
        deviceObject.setIpv6("");
        String id;
        if (StringUtils.isNotEmpty(id = getUidIFA(false))) {
            // Set to UIDIFA for IOS Device
            deviceObject.setIfa(id);
        } else if (StringUtils.isNotEmpty(id = getGPID(false))) {
            // Set to GPID for Android Device
            deviceObject.setIfa(id);
        }
        deviceObject.setLanguage(sasParams.getLanguage());
        deviceObject.setMake(sasParams.getDeviceMake());
        deviceObject.setHwv(sasParams.getDeviceModel());
        deviceObject.setOs(sasParams.getOsMajorVersion());

        final Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            deviceObject.setOs(SASRequestParameters.HandSetOS.values()[sasParamsOsId - 1].toString());
        }

        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            deviceObject.setOsv(sasParams.getOsMajorVersion());
        }

        deviceObject.setOsvname("");

        final ConnectionType sasParamConnectionType = sasParams.getConnectionType();
        if (null != sasParamConnectionType) {
            deviceObject.setConnectiontype(String.valueOf(sasParamConnectionType.getValue()));
        } else {
            deviceObject.setConnectiontype(String.valueOf(ConnectionType.UNKNOWN.getValue()));
        }

        //        final CcidMapEntity ccidMapEntity = repositoryHelper. (sasParams.getCarrierId());
        //        if (null != ccidMapEntity) {
        //            deviceObject.setCarrier(ccidMapEntity.getCarrier());
        //        }

        if (DeviceType.TABLET == sasParams.getDeviceType()) {
            deviceObject.setDevicetype(DEVICE_TYPE_TABLET);
        } else {
            deviceObject.setDevicetype(DEVICE_TYPE_PHONE); // SmartPhones and FeaturePhones
        }
        deviceObject.setW(String.valueOf(width));
        deviceObject.setH(String.valueOf(height));
        if (null != sasParams.getDerivedDeviceDensity()) {
            deviceObject.setPpi(sasParams.getDerivedDeviceDensity().toString());
        }
        final Geo geo = createGeoObject();
        deviceObject.setGeo(geo);
        return deviceObject;
    }

    private Geo createGeoObject() {
        final Geo geoObject = new Geo();
        if (!isCoppaSet && StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
            && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            geoObject.setLat(String.format("%.4f", Double.parseDouble(latlong[0])));
            geoObject.setLon(String.format("%.4f", Double.parseDouble(latlong[1])));
        }
        if (null != sasParams.getCountryCode()) {
            geoObject.setCountry(IABCountriesMap.getIabCountry(sasParams.getCountryCode()));
        }
        geoObject.setCity("");
        geoObject.setRegion("");
        geoObject.setZip(casInternalRequestParameters.getZipCode());
        geoObject.setUtcoffset("");

        return geoObject;
    }

    private User createUserObject() {
        final User user = new User();
        final String gender = sasParams.getGender();
        if (StringUtils.isNotEmpty(gender)) {
            user.setGender(gender);
        }
        user.setYob(getYearofBirthMicrosoft());
        List<String> keywords = Lists.newArrayList();
        user.setKeywords(keywords);
        return user;
    }

    private String getYearofBirthMicrosoft() {
        try {
            if (sasParams.getAge() != null && sasParams.getAge().toString().matches("\\d+")) {
                final Calendar cal = new GregorianCalendar();
                return String.valueOf(cal.get(Calendar.YEAR) - sasParams.getAge());
            }
        } catch (final Exception e) {}
        return null;
    }

    private Regs createRegsObject() {
        final Regs regsObject = new Regs();
        isCoppaSet = isWapSiteUACEntity && wapSiteUACEntity.isCoppaEnabled()
            || sasParams.getAge() != null && sasParams.getAge() <= AGE_LIMIT_FOR_COPPA;
        regsObject.setCoppa(isCoppaSet ? "1" : "0");

        Map<String, String> extRegs = new HashMap<>();
        extRegs.put("IsDesignedForFamilies", "");
        regsObject.setExt(extRegs);
        return regsObject;
    }

    private boolean createBidRequestObject(final Imp imp, final App app, final Device device, final User user, final Regs regs) {
        bidRequest = new BidRequest();
        bidRequest.set_type("Ads/NativeAdsRequest");
        bidRequest.setImp(imp);
        bidRequest.setApp(app);
        bidRequest.setDevice(device);
        bidRequest.setUser(user);
        bidRequest.setRegs(regs);
        bidRequest.setTest(1);
        bidRequest.setQuery("games");
        final List<String> contentList = new ArrayList<String>();
        bidRequest.setContent(contentList);
        bidRequest.setURL("http://www.inmobi.com/en-us/autos/?test=23");
        bidRequest.setReferralURL("http://inmobi.com");
        bidRequest.setQueryType("AdsRequest");
        final Map<String, String> pubDataMap = new HashMap<String, String>();
        pubDataMap.put("providerId", "inmobi");
        bidRequest.setPublisherData(pubDataMap);
        return true;
    }

    private boolean serializeBidRequest() {
        try {
            bidRequestJson = gson.toJson(bidRequest);
            LOG.info(traceMarker, "Microsoft request json is: {}", bidRequestJson);
            return true;
        } catch (final Exception e) {
            LOG.debug(traceMarker, "Could not create json from bidRequest for partner Microsoft");
            LOG.info(traceMarker, "Configure parameters inside Microsoft returned false , exception thrown {}", e);
            return false;
        }
    }

    @Override
    public String getName() {
        return "microsoftDCP";
    }

    @Override
    public String getId() {
        return config.getString("microsoft.advertiserId");
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            LOG.debug("MicrosoftDCP url is {}", host);
            return new URI(host);
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    public RequestBuilder getNingRequestBuilder() throws Exception {
        URI uri = getRequestUri();

        final String requestParams = bidRequestJson;
        final RequestBuilder ningRequestBuilder = new RequestBuilder(POST).setUrl(uri.toString())
            .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
            .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
            .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
            .setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(requestParams.length()))
            .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent()).setHeader(HttpHeaders.Names.HOST,
                "www.bing.com").setBody(requestParams);
        LOG.debug("Microsoft request: {}", ningRequestBuilder);
        LOG.debug("Microsoft request Body: {}", requestParams);
        return ningRequestBuilder;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        if (StringUtils.isBlank(response) || status.code() != 200) {
            statusCode = 500;
            responseContent = "";
        } else {
            try {
                final JSONObject adResponse = new JSONObject(response);
                final VelocityContext context = new VelocityContext();
                statusCode = status.code();
                if (adResponse.has("_type")&&adResponse.get("_type").toString().contains("TwoClickAd")) {
                        twoClickAdparseResponse(adResponse,context);
                }
                final JSONArray responseAd = adResponse.getJSONArray(ADS_KEY);
                if (responseAd.length() > 0) {
                    JSONObject Response = responseAd.getJSONObject(0);
                    if (Response.has("_type")) {
                        if (Response.get("_type").toString().contains("OneClickAd")) {
                            oneClickAdparseResponse(responseAd,context);
                        }
                        else {
                            adStatus = NO_AD;
                            LOG.info("Error parsing response {} from microsoft: {}", response);
                        }
                    } else {
                        adStatus = NO_AD;
                        LOG.info("Error parsing response {} from microsoft: {}", response);
                    }
                }
            } catch (final JSONException exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from microsoft: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from microsoft: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }
    }

    private void oneClickAdparseResponse(JSONArray responseAd,final VelocityContext context) {
        buildInmobiAdTracker();
        try {
            Formatter.TemplateType t = null;
            if (width == 320 && height == 50)
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_ONE_CLICK_320x50;
            if (width == 728 && height == 90)
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_ONE_CLICK_728x90;
            if (width == 300 && height == 250)
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_ONE_CLICK_300x250;
            if (width == 320 && height == 480)
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_ONE_CLICK_320x480;
            if (width == 480 && height == 320)
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_ONE_CLICK_480x320;
            for(int index = 0; index< responseAd.length(); index++) {
                final JSONObject responseAdObj = responseAd.getJSONObject(index);
                String AdClickUrl = responseAdObj.getString(TARGETURL_KEY);
                String logoCallClickUrl = "";
                String phoneNumber;
                String logoLocationClickUrl = "";
                String adText = responseAdObj.getString(TITLE_KEY);
                String displayUrl = responseAdObj.getString(DISPLAYURL_KEY);
                String displayClickUrl = displayUrl;

                if (!StringUtils.contains(displayClickUrl, "www.")) {
                    displayClickUrl = "www." + displayClickUrl;
                }
                if (!StringUtils.contains(displayClickUrl, "http://")) {
                    displayClickUrl = "http://" + displayClickUrl;
                }
                String description = responseAdObj.getString(DESCRIPTION_KEY);
                String logoImageClickUrl = "";
                if (responseAdObj.has(IMAGES_KEY)) {
                    final JSONArray imagesArray = responseAdObj.getJSONArray(IMAGES_KEY);
                    JSONObject imageObject;
                    for (int i = 0; i < imagesArray.length(); i++) {
                        imageObject = imagesArray.getJSONObject(i);
                        if (imageObject.getString(IMAGETYPE_KEY).equalsIgnoreCase(IMAGETYPEFAVICON_KEY)) {
                            faviconImageUrl = imageObject.getString(IMAGEURL_KEY);
                        }
                        if (imageObject.getString(IMAGETYPE_KEY)
                            .equalsIgnoreCase(IMAGE_TYPE_ADVERTISER_UPLOAD_IMAGE_KEY)) {
                            logoImageUrl = imageObject.getString(IMAGEURL_KEY);
                            logoImageClickUrl = AdClickUrl;
                        }
                    }
                }


                if (responseAdObj.has(DECORATIONS_KEY)) {
                    JSONObject decorationsObject;
                    final JSONArray decorationsArray = responseAdObj.getJSONArray(DECORATIONS_KEY);
                    for (int i = 0; i < decorationsArray.length(); i++) {
                        decorationsObject = decorationsArray.getJSONObject(i);
                        if (decorationsObject.getString(DECORATIONSTYPE_KEY).equalsIgnoreCase(DECORAITONSTYPE_CALL)) {
                            logoCallUrl = PHONEICONURL;
                            logoCallClickUrl = decorationsObject.getString(PHONENUMBERURL_KEY);
                            phoneNumber = decorationsObject.getString(PHONENUMBER_KEY);
                            logoCallClickUrl = !logoCallClickUrl.isEmpty() && logoCallClickUrl.contains("tel:")
                                ? logoCallClickUrl : (!phoneNumber.isEmpty() ? "tel:" + phoneNumber : "");
                        }
                        if(decorationsObject.getString(DECORATIONSTYPE_KEY).equalsIgnoreCase(DECORAITONSTYPE_LOCATION)){
                            logoLocationUrl = DIRECTIONICONURL;
                            logoLocationClickUrl = "https://www.google.com/maps/place/"
                                + URLEncoder.encode(decorationsObject.getString("AddressLine1"), "UTF-8")
                                + "," + URLEncoder.encode(decorationsObject.getString("AddressLine2"), "UTF-8")
                                + "," + decorationsObject.getString("City")
                                + "," + decorationsObject.getString("ProvinceName")
                                + "," + decorationsObject.getString("Zip");
                        }
                    }
                } else {
                    LOG.debug("No Decorations by microsoft");
                }

                if(index==0) {
                    if (!faviconImageUrl.isEmpty()) {
                        context.put(VelocityTemplateFieldConstants.PARTNER_FAVICON_IMG_URL, faviconImageUrl);
                    }
                    if (!logoImageUrl.isEmpty()) {
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_URL.get(0), logoImageUrl);
                    }
                    if(height<=90) {
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_URL.get(1), !logoCallClickUrl.isEmpty() ?
                            logoCallUrl : !logoLocationClickUrl.isEmpty() ? logoLocationUrl : logoImageUrl);
                        context.put(VelocityTemplateFieldConstants.PARTNER_IMG_CLICK_URL.get(1),
                            !logoCallClickUrl.isEmpty() ? logoCallClickUrl : !logoLocationClickUrl.isEmpty() ?
                                logoLocationClickUrl :logoImageClickUrl);
                    }
                    else {
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_URL.get(1), !logoCallClickUrl.isEmpty() ?
                            logoCallUrl : !logoLocationClickUrl.isEmpty() ? logoLocationUrl : "");
                        context.put(VelocityTemplateFieldConstants.PARTNER_IMG_CLICK_URL.get(1),
                            !logoCallClickUrl.isEmpty() ? logoCallClickUrl : !logoLocationClickUrl.isEmpty() ?
                                logoLocationClickUrl : "");
                    }

                    context.put(VelocityTemplateFieldConstants.AD_TEXT, adText);
                    context.put(VelocityTemplateFieldConstants.DESCRIPTION, description);
                    context.put(VelocityTemplateFieldConstants.PARTNER_AD_CLICK_URL, AdClickUrl);
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());
                    context.put(VelocityTemplateFieldConstants.PARTNER_DISPLAY_URL, displayUrl);
                    context.put(VelocityTemplateFieldConstants.PARTNER_DISPLAY_CLICK_URL, displayClickUrl);
                }
                else {
                    if (!faviconImageUrl.isEmpty()) {
                        context.put(VelocityTemplateFieldConstants.PARTNER_FAVICON_IMG_URL1, faviconImageUrl);
                    }
                    if (!logoImageUrl.isEmpty()) {
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_URL1.get(0), logoImageUrl);
                    }
                    if(height<=90) {
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_URL1.get(1), !logoCallClickUrl.isEmpty() ?
                            logoCallUrl : !logoLocationClickUrl.isEmpty() ? logoLocationUrl : logoImageUrl);
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_CLICK_URL1.get(1),
                            !logoCallClickUrl.isEmpty() ? logoCallClickUrl : !logoLocationClickUrl.isEmpty() ?
                                logoLocationClickUrl :logoImageClickUrl);
                    }
                    else {
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_URL1.get(1), !logoCallClickUrl.isEmpty() ?
                            logoCallUrl : !logoLocationClickUrl.isEmpty() ? logoLocationUrl : "");
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_CLICK_URL1.get(1),
                            !logoCallClickUrl.isEmpty() ? logoCallClickUrl : !logoLocationClickUrl.isEmpty() ?
                                logoLocationClickUrl : "");
                    }

                    context.put(VelocityTemplateFieldConstants.AD_TEXT1, adText);
                    context.put(VelocityTemplateFieldConstants.DESCRIPTION1, description);
                    context.put(VelocityTemplateFieldConstants.PARTNER_AD_CLICK_URL1, AdClickUrl);
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());
                    context.put(VelocityTemplateFieldConstants.PARTNER_DISPLAY_URL1, displayUrl);
                    context.put(VelocityTemplateFieldConstants.PARTNER_DISPLAY_CLICK_URL1, displayClickUrl);
                }
            }
            responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, getBeaconUrl());
            LOG.debug("response content length is {} and the response is {}", responseContent.length(), responseContent);
            adStatus = AD_STRING;
        } catch (final JSONException exception) {
            adStatus = NO_AD;
            LOG.info("Error parsing response {} from microsoft: {}", responseAd, exception);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
        } catch (final Exception exception) {
            adStatus = NO_AD;
            LOG.info("Error parsing response {} from microsoft: {}", responseAd, exception);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
        }
    }

    private void twoClickAdparseResponse(JSONObject responseAdObj,final VelocityContext context) {
        buildInmobiAdTracker();
        try {
            Formatter.TemplateType t = null;
            if(width == 320 && height == 50)
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_TWO_CLICK_320x50;
            if(width == 728 && height == 90)
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_TWO_CLICK_728x90;
            if(width == 300 && height == 250)
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_TWO_CLICK_300x250;
            if(width == 320 && height == 480){
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_TWO_CLICK_320x480;
            }
            if(width == 480 && height == 320){
                t = Formatter.TemplateType.MICROSOFT_RICH_TEXT_TWO_CLICK_480x320;
            }
            JSONArray responseAd = responseAdObj.getJSONArray(intentLinks);
            for(int index = 0; index< responseAd.length(); index++) {
                String AdClickUrl = responseAd.getJSONObject(index).getString(TARGETURL_KEY);
                String adText = responseAd.getJSONObject(index).getString(TITLE_KEY);
                JSONObject imgObj = responseAd.getJSONObject(index);
                if (imgObj.has("image")) {
                    final JSONObject imageObj = imgObj.getJSONObject("image");
                    if (imageObj.getString(IMAGETYPE_KEY).equalsIgnoreCase(IMAGE_TYPE_ADVERTISER_UPLOAD_IMAGE_KEY)) {
                        logoImageUrl = imageObj.getString(IMAGEURL_KEY);
                    }
                } else {
                    LOG.debug("No image url");
                }


                if(index==0) {
                    if (!logoImageUrl.isEmpty()) {
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_URL.get(0), logoImageUrl);
                    }
                    context.put(VelocityTemplateFieldConstants.AD_TEXT, adText);
                    context.put(VelocityTemplateFieldConstants.PARTNER_AD_CLICK_URL, AdClickUrl);
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());
                } else{
                    if (!logoImageUrl.isEmpty()) {
                        context.put(VelocityTemplateFieldConstants.MICROSOFT_PARTNER_IMG_URL1.get(0), logoImageUrl);
                    }
                    context.put(VelocityTemplateFieldConstants.AD_TEXT1, adText);
                    context.put(VelocityTemplateFieldConstants.PARTNER_AD_CLICK_URL1, AdClickUrl);
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());
                }
            }
            responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, getBeaconUrl());
            LOG.debug("response content length is {} and the response is {}", responseContent.length(), responseContent);
            adStatus = AD_STRING;
        }catch (final JSONException exception) {
            adStatus = NO_AD;
            LOG.info("Error parsing response {} from microsoft: {}", responseAdObj, exception);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
        } catch (final Exception exception) {
            adStatus = NO_AD;
            LOG.info("Error parsing response {} from microsoft: {}", responseAdObj, exception);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
        }
    }
}
