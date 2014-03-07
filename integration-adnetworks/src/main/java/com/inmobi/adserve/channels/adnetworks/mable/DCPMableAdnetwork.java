package com.inmobi.adserve.channels.adnetworks.mable;

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
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPMableAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG          = LoggerFactory.getLogger(DCPMableAdnetwork.class);

    private int                 width;
    private int                 height;
    private String              latitude;
    private String              longitude;
    private final String        authKey;
    private String              uidType      = null;
    private static final String sizeFormat   = "%dx%d";
    private static final String udidFormat   = "UDID";
    private static final String odinFormat   = "ODIN1";
    private static final String sodin1Format = "SODIN1";
    private static final String ifaFormat    = "IFA";
    private Request             ningRequest;

    public DCPMableAdnetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
        this.authKey = config.getString("mable.authKey");
        this.host = config.getString("mable.host");
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for Mable so exiting adapter");
            return false;
        }

        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long)sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            LOG.debug("mandate parameters missing for Mable, so returning from adapter");
            return false;
        }

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }

        LOG.info("Configure parameters inside Mable returned true");
        return true;
    }

    @Override
    public String getName() {
        return "mable";
    }

    private String getRequestParams() {
        JSONObject request = new JSONObject();
        try {
            request.put("imp_beacon", "");
            request.put("auth_key", authKey);

            request.put("site_id", externalSiteId);
            request.put("site_category", getCategories(',', true, true));
            request.put("clk_track_url", clickUrl);
            request.put("client_agent", sasParams.getUserAgent());
            request.put("client_ip", sasParams.getRemoteHostIp());
            request.put("blind_id", blindedSiteId);
            String uid = getUid();
            if (uid != null) {
                request.put("device_id", uid);
                request.put("did_format", uidType);
            }
            request.put("slot_size", String.format(sizeFormat, width, height));
            if (!StringUtils.isEmpty(latitude)) {
                request.put("lat", latitude);
            }
            if (!StringUtils.isEmpty(longitude)) {
                request.put("long", longitude);
            }

            if (sasParams.getGender() != null) {
                request.put("gender", sasParams.getGender());
            }
            if (sasParams.getAge() != null) {
                request.put("age", sasParams.getAge());
            }
            if (sasParams.getCountryCode() != null) {
                request.put("country", sasParams.getCountryCode());
            }
            if (sasParams.getPostalCode() != null) {
                request.put("zip", sasParams.getPostalCode());
            }

        }
        catch (JSONException e) {
            LOG.info("Error while forming request object");
        }
        LOG.debug("Mable request {}", request);
        return request.toString();
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            String host = config.getString("mable.host");
            StringBuilder url = new StringBuilder(host);
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
        try {
            String uri = getRequestUri().toString();
            requestUrl = uri;
            setNingRequest(requestUrl);
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
            LOG.debug("Exception in {} makeAsyncRequest : {}", getName(), e.getMessage());
        }
        LOG.debug("{} returning from make NingRequest", getName());
        return true;
    }

    private void setNingRequest(final String requestUrl) {
        String requestParams = getRequestParams();
        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(requestParams, CharsetUtil.UTF_8);
        ningRequest = new RequestBuilder("POST").setUrl(requestUrl)
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us").setHeader(HttpHeaders.Names.REFERER, requestUrl)
                .setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
                .setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buffer.readableBytes()))
                .setBody(requestParams).setHeader("X-Forwarded-For", sasParams.getRemoteHostIp()).build();
        LOG.info("Mable request: {}", ningRequest);
        LOG.info("Mable request Body: {}", requestParams);
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        statusCode = status.getCode();
        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.error("Error parsing response from Mable : {}", exception);
                LOG.error("Response from Mable: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("mable.advertiserId"));
    }

    @Override
    public HttpRequest getHttpRequest() throws Exception {
        try {
            URI uri = getRequestUri();
            requestUrl = uri.toString();
            request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
            LOG.debug("host name is {}", uri.getHost());
            request.setHeader(HttpHeaders.Names.HOST, uri.getHost());
            LOG.debug("got the host");
            request.setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent());
            request.setHeader(HttpHeaders.Names.REFERER, uri.toString());
            request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES);
            request.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp());

            ChannelBuffer buffer = ChannelBuffers.copiedBuffer(getRequestParams(), CharsetUtil.UTF_8);
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buffer.readableBytes()));
            request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            request.setContent(buffer);
        }
        catch (Exception ex) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.HTTPREQUEST_ERROR;
            LOG.error("Error in making http request {}", ex);
        }
        return request;
    }

    @Override
    protected String getUid() {
        if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()
                && StringUtils.isNotEmpty(casInternalRequestParameters.uidIFA)) {
            uidType = ifaFormat;
            return casInternalRequestParameters.uidIFA;

        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidMd5)) {
            uidType = udidFormat;
            return casInternalRequestParameters.uidMd5;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uid)) {
            uidType = udidFormat;
            return casInternalRequestParameters.uid;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidO1)) {
            uidType = odinFormat;
            return casInternalRequestParameters.uidO1;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidSO1)) {
            uidType = sodin1Format;
            return casInternalRequestParameters.uidSO1;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidIDUS1)) {
            uidType = udidFormat;
            return casInternalRequestParameters.uidIDUS1;
        }
        return null;
    }
}
