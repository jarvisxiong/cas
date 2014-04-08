package com.inmobi.adserve.channels.adnetworks.ifc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.awt.Dimension;
import java.net.URI;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.CategoryList;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;


/**
 * 
 * @author Sandeep.Barange
 * 
 */
public class IFCAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG         = LoggerFactory.getLogger(IFCAdNetwork.class);

    private String              requestId;
    private String              deviceOs;                                                 // Mandatory Param
    private String              deviceOSVersion;                                          // Mandatory Param
    private String              handset;                                                  // Mandatory Param
    private String              carrier;                                                  // Mandatory Param
    private String              isTest;
    private String              zone;
    private String              listingCount;
    private String              city;
    private String              state;
    private String              ccid;
    private String              userAgent;
    private String              gender;
    private String              siteID;                                                   // Mandatory Param
    private String              slotWidth;                                                // Mandatory Param
    private String              slotHeight;                                               // Mandatory Param
    private String              appType;
    private String              publisherID;                                              // Mandatory Param
    private Boolean             siteAllowBanner;
    private Boolean             richMedia;
    private String              adcode;

    HttpRequest                 httpRequest;

    private static final String suppySource = "DCP";
    private final String        ifcURL;

    private String              adGroupID;

    public IFCAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        ifcURL = config.getString("ifc.host");
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public boolean configureParameters() {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(sasParams.getAllParametersJson());
        }
        catch (Exception e) {
            jsonObject = new JSONObject();
        }
        try {
            requestId = stringifyParam(jsonObject, "tid", true);
            deviceOs = stringifyParam(jsonObject, "deviceOs", false);
            deviceOSVersion = stringifyParam(jsonObject, "deviceOSVersion", false);
            isTest = stringifyParam(jsonObject, "isTest", false);
            zone = stringifyParam(jsonObject, "zone", false);
            listingCount = stringifyParam(jsonObject, "listingCount", false);
            city = stringifyParam(jsonObject, "city", false);
            state = stringifyParam(jsonObject, "state", false);
            ccid = stringifyParam(jsonObject, "ccid", false);
            appType = stringifyParam(jsonObject, "appType", false);
            publisherID = stringifyParam(jsonObject, "pub-id", true);
            richMedia = getFlagParams(jsonObject, "rich-media", false);
            siteAllowBanner = getFlagParams(jsonObject, "site-allowBanner", false);
            adcode = stringifyParam(jsonObject, "adcode", true);
            adGroupID = externalSiteId;
        }
        catch (JSONException e) {
            return false;
        }
        try {
            handset = getHandsetString(jsonObject.getJSONArray("handset"));
        }
        catch (JSONException e) {
            LOG.info("IFC Mandatory Parameter missing: handset");
            return false;
        }
        try {
            carrier = getCarrierString(jsonObject.getJSONArray("carrier"));
        }
        catch (JSONException e) {
            LOG.info("IFC Mandatory Parameter missing: carrier");
            return false;
        }

        // This is temporary fix, proper handling should be identified, bug
        // id:41499
        try {
            String sdkVersion = stringifyParam(jsonObject, "sdk-version", false);
            if (sdkVersion != null
                    && (sdkVersion.toLowerCase().startsWith("i30") || sdkVersion.toLowerCase().startsWith("a30"))) {
                return false;
            }
            if ((sdkVersion == null || sdkVersion.toLowerCase().equals("0")) && adcode.equalsIgnoreCase("non-js")) {
                return false;
            }
        }
        catch (JSONException e) {
            LOG.info("Error while parsing 'sdk-version'");
        }

        if (!isEmpty(sasParams.getUserAgent())) {
            userAgent = getURLEncode(sasParams.getUserAgent(), format);
        }
        if (!isEmpty(sasParams.getGender())
                && (sasParams.getGender().equalsIgnoreCase("m") || sasParams.getGender().equalsIgnoreCase("male"))) {
            gender = "male";
        }
        else if (!isEmpty(sasParams.getGender())
                && (sasParams.getGender().equalsIgnoreCase("f") || sasParams.getGender().equalsIgnoreCase("female"))) {
            gender = "female";
        }
        if (null != sasParams.getSiteId()) {
            siteID = sasParams.getSiteId();
        }
        else {
            LOG.info("IFC Mandatory Parameter missing: SiteName");
            return false;
        }
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            slotHeight = String.valueOf(dim.getHeight());
            slotWidth = String.valueOf(dim.getWidth());
        }
        else {
            LOG.info("IFC Mandatory Parameter missing: Slot");
            return false;
        }
        if (null == beaconUrl) {
            LOG.info("IFC Mandatory Parameter missing: BeaconURL");
            return false;
        }
        return true;
    }

    private Boolean getFlagParams(final JSONObject jsonObject, final String field, final boolean isMandatory)
            throws JSONException {
        Boolean booleanVal = null;
        try {
            booleanVal = jsonObject.getBoolean(field);
        }
        catch (JSONException e) {
            if (isMandatory) {
                LOG.info("IFC Mandatory Parameter missing: {}", field);
                throw new JSONException("IFC Mandatory Parameter missing:" + field);
            }
        }
        return booleanVal;
    }

    private String stringifyParam(final JSONObject jObject, final String field, final boolean isMandatory)
            throws JSONException {
        try {
            return (String) jObject.get(field);
        }
        catch (JSONException e) {
            if (isMandatory) {
                LOG.info("IFC Mandatory Parameter missing: {}", field);
                throw new JSONException("IFC Mandatory Parameter missing:" + field);
            }
            else {
                return null;
            }

        }
    }

    @Override
    protected Request getNingRequest() throws Exception {

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        byte[] body = getRequestBody().getBytes(CharsetUtil.UTF_8);
        return new RequestBuilder("POST").setURI(uri).setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
                .setHeader(HttpHeaders.Names.ACCEPT, "application/json")
                .setHeader(HttpHeaders.Names.HOST, uri.getHost()).setBody(body).build();
    }

    // Returns the Channel Id for the TPAN as in our database. This will be
    // hardcoded.
    @Override
    public String getId() {
        return config.getString("ifc.advertiserId");
    }

    // writing channel logs
    @Override
    public JSONObject getLogline() {
        try {
            JSONObject log = new JSONObject();
            log.put("adv", getId());
            log.put("3psiteid", adGroupID);
            log.put("resp", adStatus);
            log.put("latency", getLatency());
            return log;
        }
        catch (JSONException exception) {
            LOG.info("Error while constructing logline inside ifc adapter");
            return null;
        }
    }

    @Override
    public String getName() {
        return "ifc";
    }

    private String getRequestBody() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("requestId", requestId);
        jsonObject.addProperty("deviceOs", deviceOs);
        jsonObject.addProperty("deviceOSVersion", deviceOSVersion);
        jsonObject.addProperty("handset", handset);
        jsonObject.addProperty("carrier", carrier);
        jsonObject.addProperty("isTest", isTest);
        jsonObject.addProperty("zone", zone);
        jsonObject.addProperty("listingCount", listingCount);
        jsonObject.addProperty("city", city);
        jsonObject.addProperty("state", state);
        jsonObject.addProperty("postalCode", casInternalRequestParameters.zipCode);
        jsonObject.addProperty("ccid", ccid);
        jsonObject.addProperty("publisherID", publisherID);
        jsonObject.addProperty("source", sasParams.getSource());
        jsonObject.addProperty("locSrc", sasParams.getLocSrc());
        jsonObject.addProperty("impressionId", casInternalRequestParameters.impressionId);
        jsonObject.addProperty("genderOrig", sasParams.getGender());
        jsonObject.addProperty("area", sasParams.getState());
        jsonObject.addProperty("beaconURL", beaconUrl);
        jsonObject.addProperty("appType", appType);
        jsonObject.addProperty("categories", getCategoryString(sasParams.getCategories()));
        jsonObject.addProperty("remoteHostIp", sasParams.getRemoteHostIp());
        jsonObject.addProperty("userAgent", userAgent);
        jsonObject.addProperty("responseTime", String.valueOf(config.getInt("ifc.readtimeoutMillis")));
        jsonObject.addProperty("slotHeight", slotHeight);
        jsonObject.addProperty("slotWidth", slotWidth);
        jsonObject.addProperty("uid", casInternalRequestParameters.uid);
        jsonObject.addProperty("age", sasParams.getAge());
        jsonObject.addProperty("gender", gender);
        jsonObject.addProperty("latLong", casInternalRequestParameters.latLong);
        jsonObject.addProperty("country", sasParams.getCountryCode());
        jsonObject.addProperty("siteID", siteID);
        jsonObject.addProperty("adGroupID", adGroupID);
        jsonObject.addProperty("richMedia", richMedia);
        jsonObject.addProperty("siteAllowBanner", siteAllowBanner);
        jsonObject.addProperty("jsonFromNAS", sasParams.getAllParametersJson());
        jsonObject.addProperty("adCode", adcode);
        jsonObject.addProperty("imaiBaseUrl", sasParams.getImaiBaseUrl());
        jsonObject.addProperty("supplySource", suppySource);
        jsonObject.addProperty("blindedSiteId", blindedSiteId);
        jsonObject.addProperty("uidIFA", casInternalRequestParameters.uidIFA);
        jsonObject.addProperty("uidSO1", casInternalRequestParameters.uidSO1);
        jsonObject.addProperty("uidO1", casInternalRequestParameters.uidO1);
        jsonObject.addProperty("uidMD5", casInternalRequestParameters.uidMd5);
        jsonObject.addProperty("uidIDUS1", casInternalRequestParameters.uidIDUS1);
        jsonObject.addProperty("uidIFV", casInternalRequestParameters.uidIFV);

        return jsonObject.toString();
    }

    private boolean isEmpty(final String str) {
        if (null == str || str.isEmpty()) {
            return true;
        }
        return false;
    }

    private String getCategoryString(final List<Long> categories) {
        if (null != categories) {
            StringBuilder categoryString = new StringBuilder();

            for (int i = 0; i < categories.size(); i++) {
                if (null != CategoryList.getCategory(categories.get(i).intValue())) {
                    categoryString.append(CategoryList.getCategory(categories.get(i).intValue())).append(',');
                }
            }
            return categoryString.toString();
        }
        return null;
    }

    private String getHandsetString(final JSONArray handset) throws JSONException {
        if (null != handset) {
            StringBuilder handsetString = new StringBuilder();
            for (int i = 0; i < handset.length(); i++) {
                handsetString.append(handset.getString(i)).append(',');
            }
            return handsetString.toString();
        }
        return null;
    }

    private String getCarrierString(final JSONArray carrier) throws JSONException {
        if (null != carrier) {
            StringBuilder carrierString = new StringBuilder();
            for (int i = 0; i < carrier.length(); i++) {
                carrierString.append(carrier.getString(i)).append(',');
            }
            return carrierString.toString();
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        if ((null != response && (status.code() != 200 || response.startsWith("<!--") || response.trim().isEmpty()))) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            responseContent = response;
            statusCode = status.code();
            adStatus = "AD";
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.inmobi.adserve.channels.api.BaseAdNetworkImpl#getRequestUri()
     */
    @Override
    public URI getRequestUri() throws Exception {
        return new URI(ifcURL);
    }

}
