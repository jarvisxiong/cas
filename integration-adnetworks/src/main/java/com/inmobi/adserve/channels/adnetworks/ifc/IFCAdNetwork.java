package com.inmobi.adserve.channels.adnetworks.ifc;

import com.google.gson.JsonObject;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.CategoryList;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.util.List;


/**
 * 
 * @author Sandeep.Barange
 * 
 */
public class IFCAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(IFCAdNetwork.class);
    private static final String SUPPY_SOURCE = "DCP";

    HttpRequest httpRequest;

    private String requestId;
    private String deviceOsId; // Mandatory Param
    private String deviceOSVersion; // Mandatory Param
    private String handset; // Mandatory Param
    private String carrier; // Mandatory Param
    private String city;
    private String state;
    private String userAgent;
    private String gender;
    private String siteID; // Mandatory Param
    private String slotWidth; // Mandatory Param
    private String slotHeight; // Mandatory Param
    private String publisherID; // Mandatory Param
    private Boolean siteAllowBanner;
    private Boolean richMedia;
    private String adcode;

    private final String ifcURL;

    private String adGroupID;

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
        } catch (final Exception e) {
            LOG.debug("cannot get AllParametersJson from sasParams, {}", e);
        }
        if (null != jsonObject) {
            try {
                requestId = stringifyParam(jsonObject, "tid", true);
                deviceOsId = stringifyParam(jsonObject, "deviceOsId", false);
                deviceOSVersion = stringifyParam(jsonObject, "deviceOSVersion", false);
                city = stringifyParam(jsonObject, "city", false);
                state = stringifyParam(jsonObject, "state", false);
                publisherID = stringifyParam(jsonObject, "pub-id", true);
                richMedia = getFlagParams(jsonObject, "rich-media", false);
                siteAllowBanner = getFlagParams(jsonObject, "site-allowBanner", false);
                adcode = stringifyParam(jsonObject, "adcode", true);
                adGroupID = externalSiteId;
            } catch (final JSONException e) {
                LOG.info("IFC Configure Parameter returning false: {}", e);
                return false;
            }
            try {
                handset = getHandsetString(jsonObject.getJSONArray("handset"));
            } catch (final JSONException e) {
                LOG.info("IFC Configure Parameter returning false as mandatory param: handset is missing");
                return false;
            }
            try {
                carrier = getCarrierString(jsonObject.getJSONArray("carrier"));
            } catch (final JSONException e) {
                LOG.info("IFC Configure Parameter returning false as mandatory param: carrier is missing");
                return false;
            }

            // This is temporary fix, proper handling should be identified, bug
            // id:41499
            try {
                final String sdkVersion = stringifyParam(jsonObject, "sdk-version", false);
                if (sdkVersion != null
                        && (sdkVersion.toLowerCase().startsWith("i30") || sdkVersion.toLowerCase().startsWith("a30"))) {
                    LOG.info("IFC Configure Parameter returning false");
                    return false;
                }
                /*if ((sdkVersion == null || sdkVersion.toLowerCase().equals("0")) && adcode.equalsIgnoreCase("non-js")) {
                    return false;
                }*/
            } catch (final JSONException e) {
                LOG.info("Error while parsing 'sdk-version', {}", e);
            }
        } else {
            requestId = sasParams.getTid();
            deviceOsId = String.valueOf(sasParams.getOsId());
            if (null != sasParams.getCity()) {
                city = sasParams.getCity().toString();
            }
            if (null != sasParams.getState()) {
                state = sasParams.getState().toString();
            }
            publisherID = sasParams.getPubId();
            richMedia = sasParams.isRichMedia();
            siteAllowBanner = sasParams.getAllowBannerAds();
            adcode = sasParams.getAdcode();
            adGroupID = externalSiteId;
            handset = String.valueOf(sasParams.getHandsetInternalId());
            carrier = String.valueOf(sasParams.getCarrierId());
            deviceOSVersion = sasParams.getOsMajorVersion();

            final String tempSdkVersion = sasParams.getSdkVersion();

            if (tempSdkVersion != null
                    && (tempSdkVersion.toLowerCase().startsWith("i30") || tempSdkVersion.toLowerCase()
                            .startsWith("a30"))) {
                LOG.info("IFC Configure Parameter returning false");
                return false;
            }
        }

        if (!isEmpty(sasParams.getUserAgent())) {
            userAgent = getURLEncode(sasParams.getUserAgent(), format);
        }

        final String tempGender = sasParams.getGender();

        if (!isEmpty(tempGender) && ("m".equalsIgnoreCase(tempGender) || "male".equalsIgnoreCase(tempGender))) {
            gender = "male";
        } else if (!isEmpty(tempGender) && ("f".equalsIgnoreCase(tempGender) || "female".equalsIgnoreCase(tempGender))) {
            gender = "female";
        } else {
            gender = tempGender;
        }

        if (null != sasParams.getSiteId()) {
            siteID = sasParams.getSiteId();
        } else {
            LOG.info("IFC Configure Parameter returning false as mandatory param: SiteName is missing");
            return false;
        }
        if (null != selectedSlotId && SlotSizeMapping.getDimension(selectedSlotId) != null) {
            final Dimension dim = SlotSizeMapping.getDimension(selectedSlotId);
            slotHeight = String.valueOf(dim.getHeight());
            slotWidth = String.valueOf(dim.getWidth());
        } else {
            LOG.info("IFC Configure Parameter returning false as mandatory param: Slot is missing");
            return false;
        }
        if (null == beaconUrl) {
            LOG.info("IFC Configure Parameter returning false as mandatory param: BeaconURL is missing");
            return false;
        }
        return true;
    }

    private Boolean getFlagParams(final JSONObject jsonObject, final String field, final boolean isMandatory)
            throws JSONException {
        Boolean booleanVal = null;
        try {
            booleanVal = jsonObject.getBoolean(field);
        } catch (final JSONException e) {
            if (isMandatory) {
                LOG.info("IFC Mandatory Parameter missing: {} , raised exception {}", field, e);
                throw new JSONException("IFC Mandatory Parameter missing:" + field);
            }
        }
        return booleanVal;
    }

    private String stringifyParam(final JSONObject jObject, final String field, final boolean isMandatory)
            throws JSONException {
        try {
            return (String) jObject.get(field);
        } catch (final JSONException e) {
            if (isMandatory) {
                LOG.info("IFC Mandatory Parameter missing: {} , and exception thrown {}", field, e);
                throw new JSONException("IFC Mandatory Parameter missing:" + field);
            } else {
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

        final byte[] body = getRequestBody().getBytes(CharsetUtil.UTF_8);
        return new RequestBuilder("POST").setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
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
            final JSONObject log = new JSONObject();
            log.put("adv", getId());
            log.put("3psiteid", adGroupID);
            log.put("resp", adStatus);
            log.put("latency", getLatency());
            return log;
        } catch (final JSONException exception) {
            LOG.info("Error while constructing logline inside ifc adapter, exception raised {}", exception);
            return null;
        }
    }

    @Override
    public String getName() {
        return "ifc";
    }

    private String getRequestBody() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sdkVersion", sasParams.getSdkVersion());
        jsonObject.addProperty("requestId", requestId);
        jsonObject.addProperty("deviceOsId", deviceOsId);
        jsonObject.addProperty("deviceOSVersion", deviceOSVersion);
        jsonObject.addProperty("handset", handset);
        jsonObject.addProperty("carrier", carrier);
        jsonObject.addProperty("city", city);
        jsonObject.addProperty("state", state);
        jsonObject.addProperty("postalCode", casInternalRequestParameters.getZipCode());
        jsonObject.addProperty("publisherID", publisherID);
        jsonObject.addProperty("source", sasParams.getSource());
        jsonObject.addProperty("locSrc", sasParams.getLocSrc());
        jsonObject.addProperty("impressionId", casInternalRequestParameters.getImpressionId());
        jsonObject.addProperty("genderOrig", sasParams.getGender());
        jsonObject.addProperty("area", sasParams.getState());
        jsonObject.addProperty("beaconURL", beaconUrl);
        jsonObject.addProperty("categories", getCategoryString(sasParams.getCategories()));
        jsonObject.addProperty("remoteHostIp", sasParams.getRemoteHostIp());
        jsonObject.addProperty("userAgent", userAgent);
        jsonObject.addProperty("responseTime", String.valueOf(config.getInt("ifc.readtimeoutMillis")));
        jsonObject.addProperty("slotHeight", slotHeight);
        jsonObject.addProperty("slotWidth", slotWidth);
        jsonObject.addProperty("uid", casInternalRequestParameters.getUid());
        jsonObject.addProperty("age", sasParams.getAge());
        jsonObject.addProperty("gender", gender);
        jsonObject.addProperty("latLong", casInternalRequestParameters.getLatLong());
        jsonObject.addProperty("country", sasParams.getCountryCode());
        jsonObject.addProperty("siteID", siteID);
        jsonObject.addProperty("adGroupID", adGroupID);
        jsonObject.addProperty("richMedia", richMedia);
        jsonObject.addProperty("siteAllowBanner", siteAllowBanner);
        jsonObject.addProperty("adCode", adcode);
        jsonObject.addProperty("imaiBaseUrl", sasParams.getImaiBaseUrl());
        jsonObject.addProperty("supplySource", SUPPY_SOURCE);
        jsonObject.addProperty("blindedSiteId", blindedSiteId);
        jsonObject.addProperty("gpid", casInternalRequestParameters.getGpid());
        jsonObject.addProperty("uid", casInternalRequestParameters.getUid());
        jsonObject.addProperty("uidO1", casInternalRequestParameters.getUidO1());
        jsonObject.addProperty("uidMD5", casInternalRequestParameters.getUidMd5());
        jsonObject.addProperty("uidIFA", casInternalRequestParameters.getUidIFA());
        jsonObject.addProperty("uidIFV", casInternalRequestParameters.getUidIFV());
        jsonObject.addProperty("uidSO1", casInternalRequestParameters.getUidSO1());
        jsonObject.addProperty("uidIDUS1", casInternalRequestParameters.getUidIDUS1());
        jsonObject.addProperty("uidADT", casInternalRequestParameters.getUidADT());
        jsonObject.addProperty("uidWC", casInternalRequestParameters.getUidWC());
        jsonObject.addProperty("uuidFromUidCookie", casInternalRequestParameters.getUuidFromUidCookie());

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
            final StringBuilder categoryString = new StringBuilder();

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
            final StringBuilder handsetString = new StringBuilder();
            for (int i = 0; i < handset.length(); i++) {
                handsetString.append(handset.getString(i)).append(',');
            }
            return handsetString.toString();
        }
        return null;
    }

    private String getCarrierString(final JSONArray carrier) throws JSONException {
        if (null != carrier) {
            final StringBuilder carrierString = new StringBuilder();
            for (int i = 0; i < carrier.length(); i++) {
                carrierString.append(carrier.getString(i)).append(',');
            }
            return carrierString.toString();
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        if (null == response || status.code() != 200 || response.startsWith("<!--") || response.trim().isEmpty()) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        } else {
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
