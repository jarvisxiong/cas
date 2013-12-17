package com.inmobi.adserve.channels.adnetworks.appnexus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;


public class DCPAppNexusAdnetwork extends BaseAdNetworkImpl {
    private final Configuration config;
    private String              latitude        = null;
    private String              longitude       = null;
    private int                 width;
    private int                 height;
    private static final String ID              = "id";
    private static final String APP_ID          = "appid";
    private static final String SIZE            = "size";
    private static final String LOCATION        = "loc";
    private static final String POSTAL_CODE     = "pcode";
    private static final String IDFA            = "idfa";
    private static final String ANDROID_ID_SHA1 = "md5udid";
    private static final String ANDROID_ID_MD5  = "sha1udid";
    private static final String ODIN1           = "sha1mac";
//    private static final String CLICKURL        = "pubclick";

    private static final String sizeFormat      = "%dx%d";
    private static final String latlongFormat   = "%s,%s";
    private Request             ningRequest;
    private String              name;
    private boolean             isApp;

    public DCPAppNexusAdnetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent) {
        super(baseRequestHandler, serverEvent, logger);
        this.config = config;
        this.logger = logger;
        this.clientBootstrap = clientBootstrap;
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandatory parameters missing for ", name, " so exiting adapter");
            return false;
        }
        host = config.getString(name + ".host");

        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            logger.debug("mandate parameters missing for ", name, " so returning from adapter");
            return false;
        }

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }

        if (sasParams.getOsId() == HandSetOS.Android.getValue()
                || sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
            isApp = true;
        }
        else {
            isApp = false;
        }

        logger.info("Configure parameters inside ", name, "returned true");
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder(host);
            appendQueryParam(url, IP, sasParams.getRemoteHostIp(), false);
            appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
            appendQueryParam(url, ID, externalSiteId, false);
            if (isApp) {
                appendQueryParam(url, APP_ID, blindedSiteId, false);
            }
            appendQueryParam(url, SIZE, String.format(sizeFormat, width, height), false);

            if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
                appendQueryParam(url, LOCATION,
                    getURLEncode(String.format(latlongFormat, latitude, longitude), format), false);
            }
            if (StringUtils.isNotBlank(sasParams.getPostalCode())) {
                appendQueryParam(url, POSTAL_CODE, sasParams.getPostalCode(), false);
            }

            if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
                if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                    appendQueryParam(url, ANDROID_ID_MD5, getURLEncode(casInternalRequestParameters.uidMd5, format),
                        false);
                }
                else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
                    appendQueryParam(url, ANDROID_ID_MD5, getURLEncode(casInternalRequestParameters.uid, format), false);
                }
                if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
                    appendQueryParam(url, ANDROID_ID_SHA1, getURLEncode(casInternalRequestParameters.uidIDUS1, format),
                        false);
                }

            }
            if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
                if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
                    appendQueryParam(url, ODIN1, getURLEncode(casInternalRequestParameters.uidO1, format), false);
                }
                else if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
                    appendQueryParam(url, ODIN1, getURLEncode(casInternalRequestParameters.uidSO1, format), false);
                }
                if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                    appendQueryParam(url, IDFA, getURLEncode(casInternalRequestParameters.uidIFA, format), false);
                }
            }

            //appendQueryParam(url, CLICKURL, getURLEncode(clickUrl, format), false);
            logger.debug(name, "url is", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info(exception.getMessage());
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean makeAsyncRequest() {
        logger.debug("In PayPal async");
        try {
            String uri = getRequestUri().toString();
            requestUrl = uri;
            setNingRequest(requestUrl);
            logger.debug("Nexage uri :", uri);
            startTime = System.currentTimeMillis();
            baseRequestHandler.getAsyncClient().executeRequest(ningRequest, new AsyncCompletionHandler() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    if (!isRequestCompleted()) {
                        logger.debug("Operation complete for channel partner: ", getName());
                        latency = System.currentTimeMillis() - startTime;
                        logger.debug(getName(), "operation complete latency", latency);
                        String responseStr = response.getResponseBody();
                        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(response.getStatusCode());
                        parseResponse(responseStr, httpResponseStatus);
                        processResponse();
                    }
                    return response;
                }

                @Override
                public void onThrowable(Throwable t) {
                    if (isRequestComplete) {
                        return;
                    }

                    if (t instanceof java.util.concurrent.TimeoutException) {
                        latency = System.currentTimeMillis() - startTime;
                        logger.debug(getName(), "timeout latency ", latency);
                        adStatus = "TIME_OUT";
                        processResponse();
                        return;
                    }

                    logger.debug(getName(), "error latency ", latency);
                    adStatus = "TERM";
                    logger.info("error while fetching response from:", getName(), t.getMessage());
                    processResponse();
                    return;
                }
            });
        }
        catch (Exception e) {
            logger.debug("Exception in", getName(), "makeAsyncRequest :", e.getMessage());
        }
        logger.debug(getName(), "returning from make NingRequest");
        return true;
    }

    private void setNingRequest(String requestUrl) {
        ningRequest = new RequestBuilder()
                .setUrl(requestUrl)
                    .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                    .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                    .setHeader(HttpHeaders.Names.REFERER, requestUrl)
                    .setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
                    .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                    .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                    .build();
    }

    @Override
    public void parseResponse(String response, HttpResponseStatus status) {
        logger.debug("response is", response);

        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.getCode();
            VelocityContext context = new VelocityContext();
            try {
                JSONObject responseJson = new JSONObject(response);
                JSONArray responseArray = responseJson.getJSONArray("ads");
                if (responseArray.length() == 0) {
                    responseContent = "";
                    statusCode = 500;
                    adStatus = "NO_AD";
                    return;
                }
                JSONObject adsJson = responseArray.getJSONObject(0);
                context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, adsJson.getString("content"));

                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl,
                    logger);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from ", name, exception);
                logger.info("Response from ", name, response);
            }
        }
    }

    @Override
    public String getId() {
        return (config.getString(name + ".advertiserId"));
    }

    public void setName(String name) {
        this.name = name;
    }

    // if we need to send click url
    @Override
    public boolean isClickUrlRequired() {
        return true;
    }
}
