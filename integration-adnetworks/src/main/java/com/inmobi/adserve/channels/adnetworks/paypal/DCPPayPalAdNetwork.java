package com.inmobi.adserve.channels.adnetworks.paypal;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;


public class DCPPayPalAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG       = LoggerFactory.getLogger(DCPPayPalAdNetwork.class);

    private String              latitude  = null;
    private String              longitude = null;
    private int                 width;
    private int                 height;
    private String              deviceId;
    private String              responseFormat;
    private Request             ningRequest;

    public DCPPayPalAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for paypal so exiting adapter");
            return false;
        }
        host = config.getString("paypal.host");
        responseFormat = config.getString("paypal.format");

        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            LOG.debug("mandate parameters missing for paypal, so returning from adapter");
            return false;
        }
        if (sasParams.getOsId() == HandSetOS.Android.getValue()
                || sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
            deviceId = getUid();
            if (StringUtils.isBlank(deviceId) || deviceId == null) {
                LOG.debug("mandate parameters missing for paypal, so returning from adapter");
                return false;
            }
        }
        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        LOG.info("Configure parameters inside paypal returned true");
        return true;
    }

    @Override
    public String getName() {
        return "paypal";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder(host);
            url.append("&format=").append(responseFormat);
            url.append("&ip=").append(sasParams.getRemoteHostIp());
            url.append("&pubid=").append(externalSiteId);
            url.append("&site=").append(blindedSiteId);
            url.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
            url.append("&width=").append(width);
            url.append("&placementtype=").append(width).append("x").append(height);

            if (sasParams.getGender() != null) {
                url.append("&gender=").append(sasParams.getGender());
            }
            if (sasParams.getAge() != null) {
                url.append("&age=").append(sasParams.getAge());
            }
            if (!StringUtils.isEmpty(latitude)) {
                url.append("&lat=").append(latitude);
                url.append("&lng=").append(longitude);
            }
            if (casInternalRequestParameters.zipCode != null) {
                url.append("&zip=").append(casInternalRequestParameters.zipCode);
            }

            if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
                if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                    url.append("&idfa=").append(casInternalRequestParameters.uidIFA);
                    url.append("&ate=").append(casInternalRequestParameters.uidADT);
                }
                else if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFV)) {
                    url.append("&idfv=").append(casInternalRequestParameters.uidIFV);
                    url.append("&ate=").append(casInternalRequestParameters.uidADT);
                }
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
                url.append("&odn1=").append(casInternalRequestParameters.uidSO1);
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
                url.append("&ms1=").append(casInternalRequestParameters.uidO1);
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                url.append("&u=").append(casInternalRequestParameters.uidMd5);
            }

            url.append("&cat=").append(getURLEncode(getCategories(','), format));

            LOG.debug("paypal url is {}", url);

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
            LOG.debug("Nexage uri :", uri);
            startTime = System.currentTimeMillis();
            baseRequestHandler.getAsyncClient().executeRequest(ningRequest, new AsyncCompletionHandler() {
                @Override
                public Response onCompleted(final Response response) throws Exception {
                    MDC.put("requestId", String.valueOf(serverChannel.hashCode()));

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
                    MDC.put("requestId", String.valueOf(serverChannel.hashCode()));

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
                    LOG.info("error while fetching response from:{} {}", getName(), t);
                    processResponse();
                    return;
                }
            });
        }
        catch (Exception e) {
            LOG.debug("Exception in {} makeAsyncRequest :", getName(), e);
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

        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.code();
            try {
                VelocityContext context = new VelocityContext();
                TemplateType t;

                if (responseFormat.equalsIgnoreCase(TemplateType.HTML.name())) {
                    t = TemplateType.HTML;
                    context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response);
                }
                else {
                    t = TemplateType.IMAGE;
                    JSONObject adResponse = new JSONObject(response).getJSONObject("adresponse").getJSONObject("imp");
                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("clickurl"));
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("imgurl"));
                    context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from paypal : {}", exception);
                LOG.info("Response from paypal: {}", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from paypal : {}", exception);
                LOG.info("Response from paypal: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("paypal.advertiserId"));
    }
}