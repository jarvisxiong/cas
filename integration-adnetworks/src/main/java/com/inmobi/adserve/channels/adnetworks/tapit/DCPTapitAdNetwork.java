package com.inmobi.adserve.channels.adnetworks.tapit;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPTapitAdNetwork extends BaseAdNetworkImpl
{
    private final Configuration config;

    private String              latitude;
    private String              longitude;
    private double              width;
    private double              height;

    public DCPTapitAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent)
    {
        super(baseRequestHandler, serverEvent, logger);
        this.config = config;
        this.clientBootstrap = clientBootstrap;
        this.logger = logger;
    }

    @Override
    public boolean configureParameters()
    {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandate parameters missing for tapit so exiting adapter");
            return false;
        }
        if (sasParams.getUserAgent().toUpperCase().contains("OPERA")) {
            logger.debug("Opera user agent found. So exiting the adapter");
            return false;
        }
        host = config.getString("tapit.host");
        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = dim.getWidth();
            height = dim.getHeight();
        }
        logger.debug("Configure parameters inside tapit returned true");
        return true;
    }

    @Override
    public String getName()
    {
        return "tapit";
    }

    @Override
    public boolean isClickUrlRequired()
    {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception
    {
        try {
            StringBuilder url = new StringBuilder();
            url.append(host).append("?format=").append(config.getString("tapit.responseFormat")).append("&ip=");
            url.append(sasParams.getRemoteHostIp())
                        .append("&ua=")
                        .append(getURLEncode(sasParams.getUserAgent(), format));
            if ("1".equals(config.getString("tapit.test"))) {
                url.append("&mode=test");
            }
            url.append("&zone=").append(externalSiteId);
            if (!StringUtils.isBlank(latitude) && !StringUtils.isBlank(longitude)) {
                url.append("&lat=").append(latitude);
                url.append("&long=").append(longitude);
            }
            String uidParamName = getUidParamNameFromSourceType(sasParams.getSource());
            if (StringUtils.isNotEmpty(casInternalRequestParameters.uidIFA)) {
                url.append("&enctype=raw&ifa=").append(casInternalRequestParameters.uidIFA);
            }
            else if (null != uidParamName) {
                if (StringUtils.isNotEmpty(casInternalRequestParameters.uidO1)) {
                    url.append("&enctype=sha1&")
                                .append(uidParamName)
                                .append("=")
                                .append(casInternalRequestParameters.uidO1);
                }
                else if (StringUtils.isNotEmpty(casInternalRequestParameters.uidMd5)) {
                    url.append("&enctype=md5&")
                                .append(uidParamName)
                                .append("=")
                                .append(casInternalRequestParameters.uidMd5);
                }
            }
            if (width != 0 && height != 0) {
                url.append("&w=").append(width);
                url.append("&h=").append(height);
            }
            url.append("&tpsid=").append(blindedSiteId);

            logger.debug("Tapit url is ", url.toString());
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
        logger.debug("response is ", response);
        if (StringUtils.isEmpty(response) || status.getCode() != 200 || response.contains("{\"error")) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            logger.debug("beacon url inside mullah media is ", beaconUrl);
            try {
                statusCode = status.getCode();
                JSONObject adResponse = new JSONObject(response);
                VelocityContext context = new VelocityContext();
                TemplateType t;
                if (adResponse.getString("type").equals("html")) {
                    context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, adResponse.getString("html"));
                    t = TemplateType.HTML;
                }
                else {
                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("clickurl"));
                    context.put(VelocityTemplateFieldConstants.Width, adResponse.getString("adWidth"));
                    context.put(VelocityTemplateFieldConstants.Height, adResponse.getString("adHeight"));
                    context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                    if (adResponse.getString("type").equals("text")) {
                        context.put(VelocityTemplateFieldConstants.AdText, adResponse.getString("adtext"));
                        String vmTemplate = Formatter.getRichTextTemplateForSlot(slot);
                        if (StringUtils.isEmpty(vmTemplate)) {
                            t = TemplateType.PLAIN;
                        }
                        else {
                            context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                            t = TemplateType.RICH;
                        }
                    }
                    else {
                        context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("imageurl"));
                        t = TemplateType.IMAGE;
                    }
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl, logger);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from tapit : ", exception);
                logger.info("Response from tapit:", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from tapit : ", exception);
                logger.info("Response from tapit:", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    logger.info("Error while rethrowing the exception : ", e);
                }
            }
        }
        logger.debug("response length is ", responseContent.length());
    }

    @Override
    public String getId()
    {
        return (config.getString("tapit.advertiserId"));
    }

    private String getUidParamNameFromSourceType(String source)
    {
        if ("iphone".equalsIgnoreCase(source)) {
            return "udid";
        }
        else if ("android".equalsIgnoreCase(source)) {
            return "android_id";
        }
        return null;
    }
}