package com.inmobi.adserve.channels.adnetworks.ajillion;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPAjillionAdnetwork extends BaseAdNetworkImpl {
    private final Configuration config;
    private final String        FORMAT      = "format";
    private final String        KEYWORD     = "keyword";
    private final String        PUBID       = "pubid";
    private final String        CLIENT_IP   = "clientip";
    private final String        CLIENT_UA   = "clientua";
    private final String        AGE         = "age";
    private final String        slotFormat  = "%s.slot_%s_%s";
    private String              placementId = null;
    private String              name;

    public DCPAjillionAdnetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent) {
        super(baseRequestHandler, serverEvent, logger);
        this.config = config;
        this.logger = logger;
        this.clientBootstrap = clientBootstrap;
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandatory parameters missing so exiting adapter ", name);
            return false;
        }
        host = config.getString(name + ".host");
        String siteRating = (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) ? "p" : "fs";
        placementId = config.getString(String.format(slotFormat, name, sasParams.getSlot(), siteRating));
        if (StringUtils.isBlank(placementId)) {
            logger.debug("Slot is not configured for ", externalSiteId);
            return false;
        }

        logger.info("Configure parameters inside ", name, " returned true");
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder(String.format(host, placementId));
            appendQueryParam(url, FORMAT, "json", true);
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
            logger.debug(name, " url is", url);
            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info(exception.getMessage());
        }
        return null;
    }

    @Override
    public void parseResponse(String response, HttpResponseStatus status) {
        logger.debug("response is", response);
        statusCode = status.getCode();
        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
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

                statusCode = status.getCode();
                VelocityContext context = new VelocityContext();

                TemplateType t = TemplateType.IMAGE;
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
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl, logger);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from ", name, exception);
                logger.info("Response from ", name, response);
            }
        }
        logger.debug("response length is", responseContent.length());
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