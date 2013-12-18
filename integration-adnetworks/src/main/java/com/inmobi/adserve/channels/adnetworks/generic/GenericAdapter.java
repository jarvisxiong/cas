package com.inmobi.adserve.channels.adnetworks.generic;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.DebugLogger;


/**
 * Sample config for a partner baseRequestHandler, serverEvent); this.config = config; this.clientBoots
 * 
 * adapter.httpool.advertiserId= adapter.httpool.mandateParams= $uId&$zoneId&$format&$userIp&$userAgent
 * adapter.httpool.host=http://a.mobile.toboads.com/get adapter.httpool.requestParams
 * =did=$useId&zid=$externalSiteKey&format=$format&sdkid =api&sdkver=100&uip=$userIp&ua=$userAgent&ormma=0&fh=1&test=0
 * adapter.httpool.requestMethod=get adapter.httpool.status=on adapter.httpool.isClickRequired=true
 * adapter.httpool.isBeaconRequired=true adapter.httpool.responseFormat=json adapter.httpool.responseStatus=status
 * adapter.httpool.content=content adapter.httpool.statusAd=1337 adapter.httpool.statusNoAd=0
 * adapter.httpool.impressionUrlField=impression_url@author devashish
 * 
 */

public class GenericAdapter extends BaseAdNetworkImpl {

    private String        advertiserName = "";
    private Configuration config;
    private String        requestMethod  = "";
    private String        responseFormat = "";

    public GenericAdapter(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent, String advertiserName) {
        super(baseRequestHandler, serverEvent, logger);
        this.config = config;
        this.clientBootstrap = clientBootstrap;
        this.advertiserName = advertiserName;
        this.host = config.getString(advertiserName.concat(MacrosAndStrings.HOST));
        // get or post
        this.requestMethod = config.getString(advertiserName.concat(MacrosAndStrings.REQUEST_METHOD));
        // html or json
        this.responseFormat = config.getString(advertiserName.concat(MacrosAndStrings.RESPONSE_FORMAT));
    }

    @Override
    public boolean configureParameters() {
        if (isMandateParamAbsent()) {
            logger.debug("mandate parameters missing for", advertiserName, "so returning from adapter");
            return false;
        }
        return true;
    }

    // checking if any of the mandatory parameter is absent
    public boolean isMandateParamAbsent() {
        String mandateParams = config.getString(advertiserName.concat(MacrosAndStrings.MANDATORY_PARAMETERS));
        String[] listParams = mandateParams.split("&");
        for (int i = 0; i < listParams.length; i++) {
            if (StringUtils.isBlank(expandMacro(listParams[i]))) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return advertiserName;
    }

    public boolean isBeaconUrlRequired() {
        return config.getString(advertiserName.concat(MacrosAndStrings.IS_BEACON_REQUIRED)).equals(
            MacrosAndStrings.TRUE);
    }

    public boolean isClickUrlRequired() {
        return config
                .getString(advertiserName.concat(MacrosAndStrings.IS_CLICK_REQUIRED))
                    .equals(MacrosAndStrings.TRUE);
    }

    public String getId() {
        return (config.getString(advertiserName.concat(MacrosAndStrings.ADVERTISER_ID)));
    }

    public HttpRequest getHttpRequest() throws Exception {
        HttpRequest httpRequest = null;
        URI uri;
        try {
            uri = getRequestUri();
        }
        catch (URISyntaxException e) {
            return null;
        }
        logger.debug("host name is", uri.getHost());
        if (requestMethod.equals(MacrosAndStrings.GET)) {
            try {
                httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString());
                httpRequest.setHeader(HttpHeaders.Names.HOST, uri.getHost());
                logger.debug("got the host");
                httpRequest.setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent());
                httpRequest.setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us");
                httpRequest.setHeader(HttpHeaders.Names.REFERER, uri.toString());
                httpRequest.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
                httpRequest.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES);
                httpRequest.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp());
            }
            catch (Exception ex) {
                errorStatus = ThirdPartyAdResponse.ResponseStatus.HTTPREQUEST_ERROR;
                logger.info("Error in making http request", ex.getMessage());
            }
        }
        else {
            logger.debug("got uri inside", advertiserName, ", uri is", uri.toString());
            httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
            ChannelBuffer buffer = ChannelBuffers.copiedBuffer(getRequestParams(), CharsetUtil.UTF_8);
            httpRequest.setHeader(HttpHeaders.Names.HOST, uri.getHost());
            httpRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buffer.readableBytes()));
            httpRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-www-form-urlencoded");
            httpRequest.setContent(buffer);
        }
        return httpRequest;
    }

    public URI getRequestUri() throws Exception {
        String finalUrl = "";
        if (requestMethod.equals(MacrosAndStrings.GET)) {
            finalUrl = host + "?" + getRequestParams();
        }
        else {
            finalUrl = host;
        }
        logger.debug("url inside", advertiserName, ":", finalUrl);
        try {
            return (new URI(finalUrl));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info("Error Forming Url inside", advertiserName, exception.getMessage());
        }
        return null;
    }

    public String getRequestParams() {
        StringBuilder requestParams = new StringBuilder(config.getString(advertiserName
                + MacrosAndStrings.REQUEST_PARAMETERS));
        String[] urlParams = requestParams.toString().split("&");
        requestParams.delete(0, requestParams.length());
        for (int i = 0; i < urlParams.length; i++) {
            String[] paramValue = urlParams[i].split("=");
            if (paramValue[1].startsWith("$")) {
                String expMacro = expandMacro(paramValue[1]);
                paramValue[1] = expMacro;
            }
            if (paramValue[1] != null) {
                if (i == 0) {
                    requestParams.append(paramValue[0]).append('=').append(paramValue[1]);
                }
                else {
                    requestParams.append('&').append(paramValue[0]).append('=').append(paramValue[1]);
                }
            }
        }
        return requestParams.toString();
    }

    public void parseResponse(String response, HttpResponseStatus status) {
        logger.debug("response is", response, "and response length is", response.length());
        if (responseFormat.equals(MacrosAndStrings.JSON)) {
            if (status.getCode() != 200 || StringUtils.isBlank(response)) {
                statusCode = status.getCode();
                if (200 == statusCode) {
                    statusCode = 500;
                }
                responseContent = "";
                return;
            }
            else {
                try {
                    JSONObject responseInJson = new JSONObject(response);
                    if (responseInJson
                            .getString(config.getString(advertiserName + MacrosAndStrings.RESPONSE_STATUS))
                                .equals(config.getString(advertiserName + MacrosAndStrings.STATUS_NO_AD))) {
                        statusCode = 500;
                        responseContent = "";
                    }
                    else {
                        statusCode = status.getCode();
                        adStatus = "AD";
                        String responseWithoutImpressionUrl = responseInJson.getString(config.getString(advertiserName
                                + MacrosAndStrings.CONTENT));
                        String impressionUrl = responseInJson.getString(config.getString(advertiserName
                                .concat(MacrosAndStrings.IMPRESSION_URL_FIELD)));
                        responseContent = responseWithoutImpressionUrl.replaceAll(MacrosAndStrings.HTML_ENDING,
                            "<img src=\"" + impressionUrl + "\" height=1 width=1 border=0 />"
                                    + MacrosAndStrings.HTML_ENDING);
                    }
                }
                catch (Exception e) {
                    logger.debug("Exception in converting json object from response", e.getMessage());
                }
            }
        }
        else if (responseFormat.equals(MacrosAndStrings.HTML)) {
            if (status.getCode() != 200 || StringUtils.isBlank(response)) {
                statusCode = status.getCode();
                if (200 == statusCode) {
                    statusCode = 500;
                }
                responseContent = "";
                return;
            }
            else {
                statusCode = status.getCode();
                adStatus = "AD";
                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append(MacrosAndStrings.HTML_STARTING);
                responseBuilder.append(response);
                responseBuilder.append("<img src=\"").append(beaconUrl).append("\" height=1 width=1 border=0 />");
                responseBuilder.append(MacrosAndStrings.HTML_ENDING);
                responseContent = responseBuilder.toString();
            }
        }
        else {
            statusCode = 500;
            responseContent = "";
        }
        logger.debug("response length is", responseContent.length());
    }

    public String expandMacro(String macro) {

        if (macro.equals(MacrosAndStrings.IMPRESSION_ID)) {
            return casInternalRequestParameters.impressionId;
        }
        if (macro.equals(MacrosAndStrings.CLICK_URL)) {
            return clickUrl;
        }
        if (macro.equals(MacrosAndStrings.BEACON_URL)) {
            return beaconUrl;
        }
        if (macro.equals(MacrosAndStrings.EXTERNAL_SITE_KEY)) {
            return externalSiteId;
        }
        if (macro.equals(MacrosAndStrings.USER_ID)) {
            return casInternalRequestParameters.uid;
        }
        if (macro.equals(MacrosAndStrings.FORMAT)) {
            Dimension format = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            if (format != null) {
                return (int) format.getWidth() + "x" + (int) format.getHeight();
            }
            return null;
        }
        if (macro.equals(MacrosAndStrings.USER_IP)) {
            return sasParams.getRemoteHostIp();
        }
        if (macro.equals(MacrosAndStrings.USER_AGENT)) {
            return sasParams.getUserAgent();
        }
        else {
            return null;
        }
    }

    public void debug(Object... os) {
        System.out.println(Arrays.deepToString(os));
    }
}