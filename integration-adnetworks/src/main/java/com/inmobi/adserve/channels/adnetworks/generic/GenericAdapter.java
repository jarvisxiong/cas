package com.inmobi.adserve.channels.adnetworks.generic;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;


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

    private static final Logger LOG = LoggerFactory.getLogger(GenericAdapter.class);

    private String advertiserName = "";
    private final Configuration config;
    private String requestMethod = "";
    private String responseFormat = "";

    public GenericAdapter(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String advertiserName) {
        super(baseRequestHandler, serverChannel);
        this.config = config;
        this.clientBootstrap = clientBootstrap;
        this.advertiserName = advertiserName;
        host = config.getString(advertiserName.concat(MacrosAndStrings.HOST));
        // get or post
        requestMethod = config.getString(advertiserName.concat(MacrosAndStrings.REQUEST_METHOD));
        // html or json
        responseFormat = config.getString(advertiserName.concat(MacrosAndStrings.RESPONSE_FORMAT));
    }

    @Override
    public boolean configureParameters() {
        if (isMandateParamAbsent()) {
            LOG.debug(traceMarker, "mandate parameters missing for", advertiserName, "so returning from adapter");
            return false;
        }
        return true;
    }

    // checking if any of the mandatory parameter is absent
    public boolean isMandateParamAbsent() {
        final String mandateParams = config.getString(advertiserName.concat(MacrosAndStrings.MANDATORY_PARAMETERS));
        final String[] listParams = mandateParams.split("&");
        for (int i = 0; i < listParams.length; i++) {
            if (StringUtils.isBlank(expandMacro(listParams[i]))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return advertiserName;
    }

    @Override
    public boolean isBeaconUrlRequired() {
        return config.getString(advertiserName.concat(MacrosAndStrings.IS_BEACON_REQUIRED)).equals(
                MacrosAndStrings.TRUE);
    }

    @Override
    public boolean isClickUrlRequired() {
        return config.getString(advertiserName.concat(MacrosAndStrings.IS_CLICK_REQUIRED))
                .equals(MacrosAndStrings.TRUE);
    }

    @Override
    public String getId() {
        return config.getString(advertiserName.concat(MacrosAndStrings.ADVERTISER_ID));
    }

    @Override
    public URI getRequestUri() throws Exception {
        String finalUrl = "";
        if (requestMethod.equals(MacrosAndStrings.GET)) {
            finalUrl = host + "?" + getRequestParams();
        } else {
            finalUrl = host;
        }
        LOG.debug(traceMarker, "url inside{} : {}", advertiserName, finalUrl);
        try {
            return new URI(finalUrl);
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info(traceMarker, "Error Forming Url inside {} {}", advertiserName, exception);
        }
        return null;
    }

    public String getRequestParams() {
        final StringBuilder requestParams =
                new StringBuilder(config.getString(advertiserName + MacrosAndStrings.REQUEST_PARAMETERS));
        final String[] urlParams = requestParams.toString().split("&");
        requestParams.delete(0, requestParams.length());
        for (int i = 0; i < urlParams.length; i++) {
            final String[] paramValue = urlParams[i].split("=");
            if (paramValue[1].startsWith("$")) {
                final String expMacro = expandMacro(paramValue[1]);
                paramValue[1] = expMacro;
            }
            if (paramValue[1] != null) {
                if (i == 0) {
                    requestParams.append(paramValue[0]).append('=').append(paramValue[1]);
                } else {
                    requestParams.append('&').append(paramValue[0]).append('=').append(paramValue[1]);
                }
            }
        }
        return requestParams.toString();
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug(traceMarker, "response is {} and response length is {}", response, response.length());
        if (responseFormat.equals(MacrosAndStrings.JSON)) {
            if (status.code() != 200 || StringUtils.isBlank(response)) {
                statusCode = status.code();
                if (200 == statusCode) {
                    statusCode = 500;
                }
                responseContent = "";
                return;
            } else {
                try {
                    final JSONObject responseInJson = new JSONObject(response);
                    if (responseInJson.getString(config.getString(advertiserName + MacrosAndStrings.RESPONSE_STATUS))
                            .equals(config.getString(advertiserName + MacrosAndStrings.STATUS_NO_AD))) {
                        statusCode = 500;
                        responseContent = "";
                    } else {
                        statusCode = status.code();
                        adStatus = "AD";
                        final String responseWithoutImpressionUrl =
                                responseInJson.getString(config.getString(advertiserName + MacrosAndStrings.CONTENT));
                        final String impressionUrl =
                                responseInJson.getString(config.getString(advertiserName
                                        .concat(MacrosAndStrings.IMPRESSION_URL_FIELD)));
                        responseContent =
                                responseWithoutImpressionUrl.replaceAll(MacrosAndStrings.HTML_ENDING, "<img src=\""
                                        + impressionUrl + "\" height=1 width=1 border=0 />"
                                        + MacrosAndStrings.HTML_ENDING);
                    }
                } catch (final Exception e) {
                    LOG.debug(traceMarker, "Exception in converting json object from response {}", e);
                    InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                    return;
                }
            }
        } else if (responseFormat.equals(MacrosAndStrings.HTML)) {
            if (status.code() != 200 || StringUtils.isBlank(response)) {
                statusCode = status.code();
                if (200 == statusCode) {
                    statusCode = 500;
                }
                responseContent = "";
                return;
            } else {
                statusCode = status.code();
                adStatus = "AD";
                final StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append(MacrosAndStrings.HTML_STARTING);
                responseBuilder.append(response);
                responseBuilder.append("<img src=\"").append(beaconUrl).append("\" height=1 width=1 border=0 />");
                responseBuilder.append(MacrosAndStrings.HTML_ENDING);
                responseContent = responseBuilder.toString();
            }
        } else {
            statusCode = 500;
            responseContent = "";
        }
        LOG.debug(traceMarker, "response length is {}", responseContent.length());
    }

    public String expandMacro(final String macro) {

        if (macro.equals(MacrosAndStrings.IMPRESSION_ID)) {
            return casInternalRequestParameters.getImpressionId();
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
            return casInternalRequestParameters.getUid();
        }
        if (macro.equals(MacrosAndStrings.FORMAT)) {
            final Dimension format = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot().toString()));
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
        } else {
            return null;
        }
    }

    public void debug(final Object... os) {
        System.out.println(Arrays.deepToString(os));
    }
}
