package com.inmobi.adserve.channels.adnetworks.mvp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.hosted.HostedBidRequest;
import com.ning.http.client.RequestBuilder;

/**
 * Created by ishanbhatnagar on 11/11/14.
 */


/**
 * Useful Initialisms:
 * HAS : Hosted Ad Server
 * RFM : Rubicon For Mobile
 */

public class HostedAdNetwork extends BaseAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(HostedAdNetwork.class);

    // Constants used while creating Bid Request
    @Getter
    private static final String NATIVE_STRING = "native";
    @Getter
    private static final String LATLON = "LATLON";
    private static final String BSSID_DERIVED = "BSSID_DERIVED";
    private static final String VISIBLE_BSSID = "VISIBLE_BSSID";
    private static final String CCID = "CCID";
    private static final String WIFI = "WIFI";
    private static final String DERIVED_LAT_LON = "DERIVED_LAT_LON";
    private static final String CELL_TOWER = "CELL_TOWER";
    private static final String CLT = "INMB_SERVER_NATIVE_1.0.0";
    private static final String RTYP = "nativejson";
    private static final short TYP_NATIVE = 4; // Denotes Native Ad
    private static final short LTYP_GPS = 1;
    private static final short LTYP_IP = 2;
    // TUD stands for Type of UDID
    private static final short TUD_UDID = 2;
    private static final short TUD_IDFA = 3;
    private static final short TUD_GAID = 4;
    // EUD denotes the type of encryption of the UDID
    private static final short EUD_NONE = 0;
    private static final short EUD_MD5 = 1;
    private static final short EUD_SHA1 = 2;

    // Constants used while creating Request URL
    private static final String CONTENT_TYPE_VALUE = "application/json";
    private static final String HTTP_POST = "POST";

    // Constants used while handling RFM Error Codes
    private static final String RFM_RESPONSE_ERROR = "FAIL";

    // Constants used during Native Ad Building
    private static final String NO_AD = "NO_AD";

    // Used for NativeAdBuilding
    @Inject
    private static NativeResponseMaker nativeResponseMaker;

    private final String advertiserId;
    private final String advertiserName;
    private final String userName;
    private final String password;

    @Getter
    private final double bidToUmpInUSD;

    // Parameters set during intialisation
    private String urlBase;

    // Redundant Parameters but cannot be removed as they are mandatory
    // Used for BannerAdBuilding
    private final boolean templateWN;
    private final boolean wnRequired;

    // Request specific parameters
    private HostedBidRequest bidRequest;
    @Getter
    private String bidRequestJson;
    @Getter
    private Long requestId;    // Not the same as requestAuctionId in RTB but it serves the same purpose

    // Response specific parameters
    @Getter
    private Long responseId;    // Not the same as responseAuctionId in RTB but it serves the same purpose
    private String adm;
    @Setter
    private double bidPriceInUsd;
    @Setter
    private double bidPriceInLocal;

    // Auction specific parameters
    private double secondBidPriceInUsd;
    private double secondBidPriceInLocal;

    // Logging specific parameters
    private boolean logCreative = false;

    public HostedAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
                           final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel,
                           final String urlBase, final String advertiserName, final int tmax,
                           final boolean templateWinNotification) {

        super(baseRequestHandler, serverChannel);
        setRtbPartner(true);

        this.advertiserId = config.getString(advertiserName + ".advertiserId");
        this.wnRequired = config.getBoolean(advertiserName + ".isWnRequired");   // Redundant but mandatory param
        this.bidToUmpInUSD = config.getDouble(advertiserName + ".bidToUmpInUSD");

        this.clientBootstrap = clientBootstrap;
        this.urlBase = urlBase;
        this.advertiserName = advertiserName;
        this.templateWN = templateWinNotification;    // Redundant but mandatory param

        this.isHTMLResponseSupported = config.getBoolean(advertiserName + ".htmlSupported", false);
        this.isNativeResponseSupported = config.getBoolean(advertiserName + ".nativeSupported", true);

        this.userName = config.getString(advertiserName + ".userName");
        this.password = config.getString(advertiserName + ".password");

        this.clickUrl = null;   // Click Url has been nullified
    }

    @Override
    protected boolean configureParameters() {
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_HOSTED_REQUESTS);

        LOG.debug(traceMarker, "Inside configureParameters of Hosted Ad Server");

        if (!checkIfBasicParamsAvailable()) {
            LOG.info(traceMarker, "Configure parameters inside Hosted returned false {}: BasicParams not available.",
                    advertiserName);
            return false;
        }

        if (!createHostedBidRequestObject()) {
            return false;
        }

        return serializeBidRequest();
    }

    private boolean checkIfBasicParamsAvailable() {

        if (null == casInternalRequestParameters || null == sasParams) {
            LOG.debug(traceMarker, "casInternalRequestParams or sasParams cannot be null");
            return false;
        }
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId) || !isRequestFormatSupported()) {
            LOG.debug(traceMarker, "mandate parameters missing or request format is not compatible to partner "
                    + "supported response for dummy so exiting adapter");
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

    @Override
    protected boolean isNativeRequest() {
        return NATIVE_STRING.equals(sasParams.getRFormat()) && "APP".equalsIgnoreCase(sasParams.getSource());
    }

    private boolean createHostedBidRequestObject() {
        // Setting Unique Request Identifier
        requestId = Math.abs(ImpressionIdGenerator.getInstance().getUniqueId(sasParams.getSiteIncId()));
        long id = requestId;

        // Setting Placement Identifier for RFM
        String app = getRFMPlacementIdentifier();

        if (null == app) {
            LOG.info(traceMarker, "ConfigureParameters inside Hosted Ad Server returned False as app is null.");
            return false;
        }

        // Setting mandatory parameters for RP Hosted Bid Request
        bidRequest = new HostedBidRequest(id, app, CLT, RTYP);

        // Setting Ad Typ
        bidRequest.setTyp(TYP_NATIVE);

        // Setting Location Type
        setLocationParams();

        // Setting IP
        // Null check not needed as remoteHostIP is a mandatory parameter in the AdPoolRequest
        bidRequest.setIp(sasParams.getRemoteHostIp());

        // Key-Values are not being set in this release

        // Setting Device Unique Identifiers
        setDeviceUIDParams();

        return true;
    }

    private void setLocationParams() {
        if (null == bidRequest) {
            return;
        }

        // Null check is not required as default value is LATLON
        if (LATLON.equals(sasParams.getLocSrc()) || BSSID_DERIVED.equals(sasParams.getLocSrc())
                || VISIBLE_BSSID.equals(sasParams.getLocSrc())) {
            bidRequest.setLtyp(LTYP_GPS);
        } else if (CCID.equals(sasParams.getLocSrc()) || WIFI.equals(sasParams.getLocSrc())
                || DERIVED_LAT_LON.equals(sasParams.getLocSrc()) || CELL_TOWER.equals(sasParams.getLocSrc())) {
            bidRequest.setLtyp(LTYP_IP);
        } // else Location Source is equal to 'NO_TARGETING'

        // If Location Source is not 'NO_TARGETING'
        if (bidRequest.isSetLtyp()) {
            // Setting Lat and Lng
            if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                    && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
                final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
                double lat = Double.parseDouble(String.format("%.4f", Double.parseDouble(latlong[0])));
                double lng = Double.parseDouble(String.format("%.4f", Double.parseDouble(latlong[1])));
                bidRequest.setLat(lat);
                bidRequest.setLng(lng);
            }
        }
    }

    private void setDeviceUIDParams() {
        if (null == bidRequest) {
            return;
        }

        // If Limit Ad Tracking Flag is set then no unique device identifiers are passed
        if ("0".equals(casInternalRequestParameters.getUidADT())) {
            // If iOS IDFA is available then send that
            if (null != casInternalRequestParameters.getUidIFA()) {
                bidRequest.setUdid(casInternalRequestParameters.getUidIFA());
                bidRequest.setTud(TUD_IDFA);
                bidRequest.setEud(EUD_NONE);
            }
            // If Android GPID is available then send that
            else if (null != casInternalRequestParameters.getGpid()) {
                bidRequest.setUdid(casInternalRequestParameters.getGpid());
                bidRequest.setTud(TUD_GAID);
                bidRequest.setEud(EUD_NONE);
            }
            // iOS only
            // If MD5 of UDID is available then send that
            else if (null != casInternalRequestParameters.getUidMd5()) {
                bidRequest.setUdid(casInternalRequestParameters.getUidMd5());
                bidRequest.setTud(TUD_UDID);
                bidRequest.setEud(EUD_MD5);
            }
            // iOS only
            // If SHA1 of UDID is available then send that
            else if (null != casInternalRequestParameters.getUidIDUS1()) {
                bidRequest.setUdid(casInternalRequestParameters.getUidIDUS1());
                bidRequest.setTud(TUD_UDID);
                bidRequest.setEud(EUD_SHA1);
            }
            // Else nothing no device identifiers are set
        }
    }

    /**
     * Gets the Placement Identifier for Rubicon For Mobile
     */
    private String getRFMPlacementIdentifier() {
        String app = null;
        Boolean errorFlag = false;
        try {
            final JSONObject additionalParams = entity.getAdditionalParams();
            if (null != additionalParams) {
                Object value = additionalParams.get("app");
                if (null == value) {
                    errorFlag = true;
                }
                app = (String) value;
            } else {
                errorFlag = true;
            }
        } catch (JSONException e) {
            errorFlag = true;
        }

        if (errorFlag) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.RFM_PLACEMENT_IDENTIFIER_ERROR);
            LOG.error(traceMarker, "No RFM Placement Identifier found for site id: {}", sasParams.getSiteId());
        }

        return app;
    }

    private boolean serializeBidRequest() {
        final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        try {
            bidRequestJson = serializer.toString(bidRequest);
            LOG.info(traceMarker, "Hosted Ad Server request json is: {}", bidRequestJson);
        } catch (final TException e) {
            LOG.debug(traceMarker, "Could not create json from bidRequest for Hosted Ad Server");
            LOG.info(traceMarker, "ConfigureParameters inside Hosted Ad Server returned False, exception raised: {}",
                    advertiserName, e);
            return false;
        }

        return true;
    }

    @Override
    public URI getRequestUri() throws URISyntaxException {
        final StringBuilder url = new StringBuilder();
        url.append(urlBase);

        LOG.debug(traceMarker, "{} url is {}", getName(), url.toString());
        return URI.create(url.toString());
    }

    @Override
    protected RequestBuilder getNingRequestBuilder() throws Exception {
        final byte[] body = bidRequestJson.getBytes(CharsetUtil.UTF_8);

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        final String authStr = userName + ":" + password;
        LOG.debug(traceMarker, "Inside get Ning Request");

        return new RequestBuilder(HTTP_POST).setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE_VALUE)
                .setHeader(HttpHeaders.Names.AUTHORIZATION, authStr)
                .setHeader(HttpHeaders.Names.HOST, uri.getHost())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setBody(body);
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        adStatus = "NO_AD";
        LOG.info(traceMarker, "Response is {}", response);
        InspectorStats.incrementStatCount(InspectorStrings.HOSTED_RESPONSES);
        InspectorStats.incrementStatCount(getName(), InspectorStrings.HOSTED_RESPONSES);

        if (isValidResponse(response, status)) {
            final boolean parsedResponse = deserializeResponse(response);
            if (!parsedResponse) {
                adStatus = "NO_AD";
                responseContent = "";
                statusCode = 500;
                InspectorStats.incrementStatCount(getName(), InspectorStrings.DESERIALISATION_ERROR);
                LOG.info(traceMarker, "Error in parsing Hosted Ad Server response");
                return;
            }
            adStatus = "AD";

            if (isNativeRequest()) {
                nativeAdBuilding();
            } else {
                LOG.debug(traceMarker, "Hosted Ad Server currently does not support non-native requests");
            }

            LOG.debug(traceMarker, "Response length is: {}", responseContent.length());
            LOG.debug(traceMarker, "Response is:\n{}", responseContent);
        }
    }

    private boolean deserializeResponse(String response) {
        responseId = null;
        adm = null;
        JSONObject bidResponseJson = null;
        try {
            bidResponseJson = new JSONObject(response);
            responseId = Long.parseLong(bidResponseJson.getString("id"));
            adm = bidResponseJson.getJSONArray("ads").get(0).toString();
        } catch (JSONException e) {
            // If AdMarkup is not present then checking whether the response was an Error Code
            try {
                if (RFM_RESPONSE_ERROR.equalsIgnoreCase(bidResponseJson.getString("status"))) {
                    String errorMsg = bidResponseJson.getString("error_msg");
                    switch (bidResponseJson.getInt("error_code")) {
                        case 1001:
                            InspectorStats.incrementStatCount(getName(),
                                    InspectorStrings.RFM_INVALID_CREDENTIALS);
                            InspectorStats.incrementStatCount(InspectorStrings.RFM_INVALID_CREDENTIALS);
                            LOG.info(traceMarker, "Could not parse Hosted Ad Server Response. RFM Error Message was "
                                    + ": {}", errorMsg);
                            break;
                        case 1002: /* NO_AD */
                            break;
                        case 1003:
                            InspectorStats.incrementStatCount(getName(),
                                    InspectorStrings.RFM_AD_SELECTION_ERROR + errorMsg);
                            LOG.info(traceMarker, "Could not parse Hosted Ad Server Response. RFM Error Message was "
                                    + ": {}", errorMsg);
                            break;
                        default:
                            InspectorStats.incrementStatCount(getName(),
                                    InspectorStrings.RFM_ERROR + errorMsg);
                            LOG.info(traceMarker, "Could not parse Hosted Ad Server Response. RFM Error Message was"
                                    + ": {}", errorMsg);
                            break;
                    }
                }
            } catch (JSONException e1) {
                LOG.info(traceMarker,
                        "Could not parse Hosted Ad Server response as mandatory field Id or Ads is missing; "
                                + "Or Error Code JSON is incorrect."
                                + " Exception thrown: ", e);
            }
            return false;
        } catch (NullPointerException e) {
            LOG.info(traceMarker, "Could not parse Hosted Ad Server response as Ads is empty. Exception thrown: ", e);
            return false;
        } catch (Exception e) {
            LOG.info(traceMarker,
                    "Could not parse Hosted Ad Server response as ResponseId cannot be converted to a Long."
                            + " Exception thrown: ", e);
            return false;
        }
        setBidPriceInLocal(bidToUmpInUSD);
        setBidPriceInUsd(bidToUmpInUSD);
        return true;
    }

    protected void nativeAdBuilding() {
        // No WIN_BID Macro has been appended
        final String winUrl = beaconUrl;

        final Map<String, String> params = new HashMap<String, String>();
        params.put("beaconUrl", beaconUrl);
        params.put("winUrl", winUrl);
        params.put("impressionId", impressionId);
        params.put("siteId", sasParams.getSiteId());
        if ("APP".equalsIgnoreCase(sasParams.getSource())) {
            // Does not support Blinded Site Ids neither do they make sense in HAS
            params.put("appId", sasParams.getSiteId());
        }

        try {
            responseContent = nativeResponseMaker.makeHostedResponse(
                    adm, params, repositoryHelper.queryNativeAdTemplateRepository(sasParams.getSiteId()));
        } catch (final Exception e) {

            adStatus = NO_AD;
            responseContent = "";
            LOG.error("Some exception is caught while filling the native template for siteId = {}, advertiser = {}, "
                    + "exception = {}", sasParams.getSiteId(), advertiserName, e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_PARSE_RESPONSE_EXCEPTION);
        }
    }

    @Override
    public void setSecondBidPrice(final Double price) {
        secondBidPriceInUsd = price;
        secondBidPriceInLocal = price;
        // No RTBMacros are replaced
        final ThirdPartyAdResponse adResponse = getResponseAd();
        adResponse.setResponse(responseContent);
    }

    @Override
    public String getId() {
        return advertiserId;
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
    public boolean isLogCreative() {
        return logCreative;
    }

}

