package com.inmobi.adserve.channels.adnetworks.ajillion;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPAjillionAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPAjillionAdnetwork.class);

    private static final String FORMAT = "format";
    private static final String KEYWORD = "keyword";
    private static final String PUBID = "pubid";
    private static final String CLIENT_IP = "clientip";
    private static final String CLIENT_UA = "clientua";
    private static final String AGE = "age";
    private static final String IS_BEACON_RQD = "use_beacon";
    private static final String SLOT_FORMAT = "%s.slot_%s_%s";
    private static final String BEACON_REQUIRED_FLAG = "1";
    private String placementId = null;
    private String name;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverChannel
     */
    public DCPAjillionAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing so exiting adapter {}", name);
            LOG.info("Configure parameters inside {} returned false", name);
            return false;
        }
        host = config.getString(name + ".host");
        final String siteRating = ContentType.PERFORMANCE == sasParams.getSiteContentType() ? "p" : "fs";
        placementId = config.getString(String.format(SLOT_FORMAT, name, selectedSlotId, siteRating));
        if (StringUtils.isBlank(placementId)) {
            LOG.debug("Slot is not configured for {}", externalSiteId);
            LOG.info("Configure parameters inside {} returned false", name);
            return false;
        }

        host = String.format(host, placementId);
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder(host);
            appendQueryParam(url, FORMAT, "json", true);
            appendQueryParam(url, IS_BEACON_RQD, BEACON_REQUIRED_FLAG, false);
            appendQueryParam(url, KEYWORD, getURLEncode(getCategories(','), format), false);
            appendQueryParam(url, PUBID, blindedSiteId, false);
            appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
            appendQueryParam(url, CLIENT_UA, getURLEncode(sasParams.getUserAgent(), format), false);
            if (StringUtils.isNotBlank(sasParams.getGender())) {
                appendQueryParam(url, GENDER, sasParams.getGender(), false);
            }
            if (null != sasParams.getAge()) {
                appendQueryParam(url, AGE, sasParams.getAge().toString(), false);
            }
            LOG.debug("{} url is {}", name, url);
            return new URI(url.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        statusCode = status.code();
        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        } else {
            try {
                final JSONObject adResponse = new JSONObject(response);

                if (!"true".equals(adResponse.getString("success"))) {
                    adStatus = "NO_AD";
                    responseContent = "";
                    statusCode = 500;
                    return;
                }

                statusCode = status.code();
                final VelocityContext context = new VelocityContext();

                TemplateType t = TemplateType.IMAGE;
                if (adResponse.has("beacon_url")) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, adResponse.getString("beacon_url"));
                }
                if ("image".equalsIgnoreCase(adResponse.getString("creative_type"))) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, adResponse.getString("click_url"));
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, adResponse.getString("creative_url"));
                } else if ("3rdparty".equalsIgnoreCase(adResponse.getString("creative_type"))) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, adResponse.getString("creative_url"));
                    t = TemplateType.HTML;
                } else {
                    adStatus = "NO_AD";
                    responseContent = "";
                    return;
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            } catch (final Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response {} from {}  {}", response, name, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return config.getString(name + ".advertiserId");
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }
}
