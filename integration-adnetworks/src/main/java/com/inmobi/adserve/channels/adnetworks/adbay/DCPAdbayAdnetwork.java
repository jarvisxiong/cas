package com.inmobi.adserve.channels.adnetworks.adbay;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by deepak on 28/5/15.
 */
public class DCPAdbayAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPAdbayAdnetwork.class);

    private static final String CREATIVES_KEY = "creatives";
    private static final String IMG_KEY = "src";
    private static final String CLICK_KEY = "click";
    private static final String TEXT_KEY = "txt";
    private static final String LOGO_IMG_KEY = "logo";
    private static final String LOGO_CLICK_KEY = "redirect";
    public DCPAdbayAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for adbaydcp so exiting adapter");
            LOG.info("Configure parameters inside adbaydcp returned false");
            return false;
        }
        host = config.getString("adbaydcp.host");
        return true;
    }

    @Override
    public String getName() {
        return "adbayDCP";
    }

    @Override
    public String getId() {
        return config.getString("adbaydcp.advertiserId");
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final String uri = host;
            LOG.debug("AdbayDCP url is {}", uri);
            return new URI(uri.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        if (StringUtils.isBlank(response) || status.code() != 200) {
            statusCode = 500;
            responseContent = "";
            return;
        } else {
            try {
                final JSONObject adResponse = new JSONObject(response);
                statusCode = status.code();
                final VelocityContext context = new VelocityContext();
                Formatter.TemplateType t = Formatter.TemplateType.ADBAY_HTML;
                final JSONArray responseAd = adResponse.getJSONArray(CREATIVES_KEY);
                if (responseAd.length() > 0) {
                    buildInmobiAdTracker();
                    String logoImageUrl = adResponse.getString(LOGO_IMG_KEY);
                    String logoClickUrl = adResponse.getString(LOGO_CLICK_KEY);
                    final JSONObject responseAdObj = responseAd.getJSONObject(0);
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, responseAdObj.getString(IMG_KEY));
                    context.put(VelocityTemplateFieldConstants.AD_TEXT, responseAdObj.getString(TEXT_KEY));
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, responseAdObj.getString(CLICK_KEY));
                    context.put(VelocityTemplateFieldConstants.PARTNER_LOGO_CLICK_URL, logoClickUrl);
                    context.put(VelocityTemplateFieldConstants.PARTNER_LOGO_IMG_URL, logoImageUrl);
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());
                    responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, getBeaconUrl());
                    LOG.debug("response content length is {} and the response is {}", responseContent.length(), responseContent);
                    adStatus = AD_STRING;
                }
            } catch (final JSONException exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from adbay: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from adbay: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }

        }
    }
}
