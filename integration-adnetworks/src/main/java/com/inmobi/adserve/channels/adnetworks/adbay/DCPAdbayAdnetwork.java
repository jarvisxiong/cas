package com.inmobi.adserve.channels.adnetworks.adbay;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
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
            statusCode = status.code();
            final VelocityContext context = new VelocityContext();
            buildInmobiAdTracker();
            context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response);
            try {
                responseContent = Formatter.getResponseFromTemplate(Formatter.TemplateType.HTML, context, sasParams, getBeaconUrl());
                adStatus = AD_STRING;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from adbay: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }

        }

    }
}
