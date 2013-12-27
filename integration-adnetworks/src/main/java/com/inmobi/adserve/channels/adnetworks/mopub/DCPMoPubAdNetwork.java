package com.inmobi.adserve.channels.adnetworks.mopub;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;


public class DCPMoPubAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPMoPubAdNetwork.class);

    private String              deviceId;
    private static final String name             = "mopub";
    private static final String responseTemplate = "%s <img src='%s' height=1 width=1 border=0 style=\"display:none;\"/>";
    private Request             ningRequest;

    public DCPMoPubAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for MoPub so exiting adapter");
            return false;
        }

        if (HandSetOS.iPhone_OS.getValue() == sasParams.getOsId()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                deviceId = "ifa:" + casInternalRequestParameters.uidIFA + "&dnt=" + casInternalRequestParameters.uidADT;
            }
        }

        if (StringUtils.isBlank(deviceId) && StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            deviceId = "sha:" + casInternalRequestParameters.uidO1;
        }
        if (StringUtils.isBlank(deviceId)) {
            LOG.debug("Device id mandate parameters missing for MoPub, so returning from adapter");
            return false;
        }

        LOG.info("Configure parameters inside MoPub returned true");
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            String host = config.getString("mopub.host");
            StringBuilder url = new StringBuilder(host);
            url.append("?v=1&id=")
                        .append(externalSiteId)
                        .append("&ip=")
                        .append(sasParams.getRemoteHostIp())
                        .append("&udid=")
                        .append(deviceId)
                        .append("&q=")
                        .append(getURLEncode(getCategories(',', true, false), format));

            if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                    && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
                url.append("&ll=").append(getURLEncode(casInternalRequestParameters.latLong, format));
            }

            LOG.debug("MoPub url is {}", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
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
            responseContent = String.format(responseTemplate, response, beaconUrl);
            adStatus = "AD";
        }
        LOG.debug("response length is {}", response.length());
    }

    @Override
    public String getId() {
        return (config.getString("mopub.advertiserId"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean makeAsyncRequest() {
        LOG.debug("In mopub async");
        try {
            String uri = getRequestUri().toString();
            requestUrl = uri;
            setNingRequest(requestUrl);
            LOG.debug("Nexage uri : {}", uri);
            startTime = System.currentTimeMillis();
            baseRequestHandler.getAsyncClient().executeRequest(ningRequest, new AsyncCompletionHandler() {
                @Override
                public Response onCompleted(final Response response) throws Exception {
                    if (!isRequestCompleted()) {
                        LOG.debug("Operation complete for channel partner: {}", getName());
                        latency = System.currentTimeMillis() - startTime;
                        LOG.debug("{} operation complete latency {}", getName(), latency);
                        String responseStr = response.getResponseBody();
                        responseHeaders = new HashMap<String, String>();
                        responseHeaders.put("X-Clickthrough", response.getHeader("X-Clickthrough"));
                        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(response.getStatusCode());
                        parseResponse(responseStr, httpResponseStatus);
                        processResponse();
                    }
                    return response;
                }

                @Override
                public void onThrowable(final Throwable t) {
                    if (isRequestComplete) {
                        return;
                    }

                    if (t instanceof java.util.concurrent.TimeoutException) {
                        latency = System.currentTimeMillis() - startTime;
                        LOG.debug("{} timeout latency ", getName(), latency);
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
            LOG.debug("Exception in {} makeAsyncRequest : {}", getName(), e);
        }
        LOG.debug("{} returning from make NingRequest", getName());
        return true;
    }

    private void setNingRequest(final String requestUrl) {
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

}
