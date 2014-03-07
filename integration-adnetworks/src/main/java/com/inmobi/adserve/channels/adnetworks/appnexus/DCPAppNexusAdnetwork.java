package com.inmobi.adserve.channels.adnetworks.appnexus;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPAppNexusAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG             = LoggerFactory.getLogger(DCPAppNexusAdnetwork.class);

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
    // private static final String CLICKURL = "pubclick";

    private static final String sizeFormat      = "%dx%d";
    private static final String latlongFormat   = "%s,%s";
    private Request             ningRequest;
    private String              name;
    private boolean             isApp;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPAppNexusAdnetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for {} so exiting adapter", name);
            return false;
        }
        host = config.getString(name + ".host");

        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long)sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            LOG.debug("mandate parameters missing for {} so returning from adapter", name);
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

        LOG.info("Configure parameters inside {} returned true", name);
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
            if (null != sasParams.getPostalCode()) {
                appendQueryParam(url, POSTAL_CODE, sasParams.getPostalCode().toString(), false);
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

            // appendQueryParam(url, CLICKURL, getURLEncode(clickUrl, format), false);
            LOG.debug("{} url is {}", name, url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean makeAsyncRequest() {
        LOG.debug("In PayPal async");
        try {
            String uri = getRequestUri().toString();
            requestUrl = uri;
            setNingRequest(requestUrl);
            LOG.debug("Nexage uri : {}", uri);
            startTime = System.currentTimeMillis();
            baseRequestHandler.getAsyncClient().executeRequest(ningRequest, new AsyncCompletionHandler() {
                @Override
                public Response onCompleted(final Response response) throws Exception {
                    MDC.put("requestId", serverEvent.getChannel().getId().toString());
                    if (!isRequestCompleted()) {
                        LOG.debug("Operation complete for channel partner: {}", getName());
                        latency = System.currentTimeMillis() - startTime;
                        LOG.debug("{} operation complete latency {}", getName(), latency);
                        String responseStr = response.getResponseBody();
                        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(response.getStatusCode());
                        parseResponse(responseStr, httpResponseStatus);
                        processResponse();
                    }
                    return response;
                }

                @Override
                public void onThrowable(final Throwable t) {
                    MDC.put("requestId", serverEvent.getChannel().getId().toString());
                    if (isRequestComplete) {
                        return;
                    }

                    if (t instanceof java.util.concurrent.TimeoutException) {
                        latency = System.currentTimeMillis() - startTime;
                        LOG.debug("{} timeout latency {}", getName(), latency);
                        adStatus = "TIME_OUT";
                        processResponse();
                        return;
                    }

                    LOG.debug("{} error latency {}", getName(), latency);
                    adStatus = "TERM";
                    LOG.info("error while fetching response from: {} {}", getName(), t);
                    processResponse();
                    return;
                }
            });
        }
        catch (Exception e) {
            LOG.error("Exception in {} makeAsyncRequest :", getName(), e);
        }
        LOG.debug("{} returning from make NingRequest", getName());
        return true;
    }

    private void setNingRequest(final String requestUrl) {
        ningRequest = new RequestBuilder().setUrl(requestUrl)
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us").setHeader(HttpHeaders.Names.REFERER, requestUrl)
                .setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp()).build();
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);

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

                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from {} {}", name, exception);
                LOG.info("Response from {} {}", name, response);
            }
        }
    }

    @Override
    public String getId() {
        return (config.getString(name + ".advertiserId"));
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    // if we need to send click url
    @Override
    public boolean isClickUrlRequired() {
        return true;
    }
}
