package com.inmobi.adserve.channels.adnetworks.widerplanet;

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
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPWiderPlanetAdnetwork extends BaseAdNetworkImpl
{
    private final Configuration config;
    private String              inmobiCookieId;

    public DCPWiderPlanetAdnetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent)
    {
        super(baseRequestHandler, serverEvent, logger);
        this.config = config;
        this.logger = logger;
        this.clientBootstrap = clientBootstrap;
    }

    @Override
    public boolean configureParameters()
    {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandatory parameters missing for widerplanet so exiting adapter");
            return false;
        }
        if (!"WAP".equalsIgnoreCase(sasParams.getSource())) {
            logger.debug("Only WAP traffic allowed. So exiting the adapter");
        }
        try {
            JSONObject userParams = new JSONObject(sasParams.getUidParams());
            inmobiCookieId = userParams.getString("imuc__5");
            if (StringUtils.isBlank(inmobiCookieId))
                inmobiCookieId = userParams.getString("WC");
            if (StringUtils.isEmpty(inmobiCookieId)) {
                logger.debug("imucId is not present. So exiting the adapter");
                return false;
            }

        }
        catch (Exception e) {
            logger.debug("imucId is not present. So exiting the adapter");
            return false;
        }

        logger.info("Configure parameters inside Wider Planet returned true");
        return true;
    }

    @Override
    public String getName()
    {
        return "widerplanet";
    }

    @Override
    public URI getRequestUri() throws Exception
    {
        try {
            String host = config.getString("widerplanet.host");
            StringBuilder url = new StringBuilder(host);
            url.append("?zoneid=")
                        .append(externalSiteId)
                        .append("&useragent=")
                        .append(getURLEncode(sasParams.getUserAgent(), format))
                        .append("&uip=")
                        .append(sasParams.getRemoteHostIp())
                        .append("&wuid=")
                        .append(getHashedValue(inmobiCookieId, "MD5"));
            if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)) {
                url.append("&location=").append(getURLEncode(casInternalRequestParameters.latLong, format));
            }
            String uid = getUid();
            if (null != uid) {
                url.append("&duid=").append(uid);
            }
            logger.debug("WiderPlanet url is", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info(exception.getMessage());
        }
        return null;
    }

    @Override
    public void parseResponse(String response, HttpResponseStatus status)
    {
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
                if (StringUtils.isNotBlank(adResponse.getString("response"))) {
                    VelocityContext context = new VelocityContext();
                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("landingUrl"));
                    context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                    TemplateType t = TemplateType.IMAGE;
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("imageUrl"));

                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, adResponse.getString("beacon"));

                    if (response.contains("beacon_ext1")) {
                        context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl1,
                            adResponse.getString("beacon_ext1"));
                    }
                    if (response.contains("beacon_ext2")) {
                        context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl2,
                            adResponse.getString("beacon_ext2"));
                    }
                    responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl, logger);
                    adStatus = "AD";
                }
                else {
                    statusCode = 500;
                    adStatus = "NO_AD";
                    responseContent = "";
                    return;
                }
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from widerplanet : ", exception);
                logger.info("Response from wider planet:", response);

            }

        }
        logger.debug("response length is", responseContent.length());
    }

    @Override
    public String getId()
    {
        return (config.getString("widerplanet.advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired()
    {
        return true;
    }

}
