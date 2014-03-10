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

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.server.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPAjillionAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG                = LoggerFactory.getLogger(DCPAjillionAdnetwork.class);

    private final String        FORMAT             = "format";
    private final String        KEYWORD            = "keyword";
    private final String        PUBID              = "pubid";
    private final String        CLIENT_IP          = "clientip";
    private final String        CLIENT_UA          = "clientua";
    private final String        AGE                = "age";
    private final String        IS_BEACON_RQD      = "use_beacon";
    private final String        slotFormat         = "%s.slot_%s_%s";
    private final String        beaconRequiredFlag = "1";
    private String              placementId        = null;
    private String              name;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
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
            return false;
        }
        host = config.getString(name + ".host");
        String siteRating = (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) ? "p" : "fs";
        placementId = config.getString(String.format(slotFormat, name, sasParams.getSlot(), siteRating));
        if (StringUtils.isBlank(placementId)) {
            LOG.debug("Slot is not configured for {}", externalSiteId);
            return false;
        }

        LOG.info("Configure parameters inside {} returned true", name);
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
            StringBuilder url = new StringBuilder(String.format(host, placementId));
            appendQueryParam(url, FORMAT, "json", true);
            appendQueryParam(url, IS_BEACON_RQD, beaconRequiredFlag, false);
            appendQueryParam(url, KEYWORD, getURLEncode(getCategories(','), format), false);
            appendQueryParam(url, PUBID, blindedSiteId, false);
            appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
            appendQueryParam(url, CLIENT_UA, getURLEncode(sasParams.getUserAgent(), format), false);
            if (StringUtils.isNotBlank(sasParams.getGender())) {
                appendQueryParam(url, GENDER, sasParams.getGender(), false);
            }
            if (StringUtils.isNotBlank(sasParams.getAge())) {
                appendQueryParam(url, AGE, sasParams.getAge(), false);
            }
            LOG.debug("{} url is {}", name, url);
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
        statusCode = status.code();
        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            try {
                JSONObject adResponse = new JSONObject(response);

                if (!"true".equals(adResponse.getString("success"))) {
                    adStatus = "NO_AD";
                    responseContent = "";
                    statusCode = 500;
                    return;
                }

                statusCode = status.code();
                VelocityContext context = new VelocityContext();

                TemplateType t = TemplateType.IMAGE;
                if (adResponse.has("beacon_url")) {
                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, adResponse.getString("beacon_url"));
                }
                if (adResponse.getString("creative_type").equalsIgnoreCase("image")) {
                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("click_url"));
                    context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("creative_url"));
                }
                else if (adResponse.getString("creative_type").equalsIgnoreCase("3rdparty")) {
                    context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, adResponse.getString("creative_url"));
                    t = TemplateType.HTML;
                }
                else {
                    adStatus = "NO_AD";
                    responseContent = "";
                    return;
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.error("Error parsing response {} from {}  {}", response, name, exception);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString(name + ".advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }
}