package com.inmobi.adserve.channels.adnetworks.httpool;

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


public class DCPHttPoolAdNetwork extends BaseAdNetworkImpl
{
    private final Configuration config;
    private transient String    latitude;
    private transient String    longitude;
    private String              slotFormat;
    private boolean             acceptShop = false;

    public DCPHttPoolAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
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
            logger.debug("mandatory parameters missing for httpool so exiting adapter");
            return false;
        }
        host = config.getString("httpool.host");

        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Long slotSize = Long.parseLong(sasParams.getSlot());
            // Httpool doesnt support 320x48 & 320x53. so mapping to 320x50
            if (slotSize == 9l || slotSize == 24l) {
                slotSize = 15l;
            }
            Dimension dim = SlotSizeMapping.getDimension(slotSize);
            acceptShop = dim.getWidth() > 299;
            slotFormat = String.format("%dx%d", (int) Math.ceil(dim.getWidth()), (int) Math.ceil(dim.getHeight()));
        }

        logger.info("Configure parameters inside httpool returned true");
        return true;
    }

    @Override
    public String getName()
    {
        return "httpool";
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
            StringBuilder url = new StringBuilder(host);

            url.append("type=rich%2Ctpt");
            if (acceptShop) {
                url.append("%2Cshop");
            }
            url.append("&uip=").append(sasParams.getRemoteHostIp());
            url.append("&zid=").append(externalSiteId);
            url.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
            if ("1".equals(config.getString("httpool.test"))) {
                url.append("&test=1");
            }
            if (!StringUtils.isBlank(latitude) && !StringUtils.isBlank(longitude)) {
                url.append("&geo_lat=").append(latitude);
                url.append("&geo_lng=").append(longitude);
            }

            String did = getUid();
            if (StringUtils.isEmpty(did) || did.equals("null")) {
                did = "nodeviceid-1234567890";
            }
            url.append("&did=").append(did);
            if (!StringUtils.isEmpty(slot)) {
                url.append("&format=").append(slotFormat);
            }
            String category = getCategories(';');
            if (!StringUtils.isEmpty(category)) {
                url.append("&ct=").append(getURLEncode(category, format));
            }
            String gender = sasParams.getGender();
            if (StringUtils.isNotBlank(gender)) {
                url.append("&dd_gnd=").append(gender.equalsIgnoreCase("f") ? 2 : 1);
            }
            logger.debug("httpool url is ", url.toString());

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

        if (StringUtils.isEmpty(response) || status.getCode() != 200) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            logger.debug("beacon url inside httpool is ", beaconUrl);

            try {
                JSONObject adResponse = new JSONObject(response);
                if (adResponse.getInt("status") == 0) {
                    statusCode = 500;
                    responseContent = "";
                    return;
                }
                statusCode = status.getCode();
                TemplateType t;
                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, adResponse.getString("impression_url"));
                String adType = adResponse.getString("ad_type");
                if ("tpt".equalsIgnoreCase(adType)) {
                    context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, adResponse.getString("content"));
                    t = TemplateType.HTML;
                }
                else {
                    String landingUrl = adResponse.getString("click_url") + "&url="
                            + adResponse.getString("redirect_url");
                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, landingUrl);
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("image_url"));
                    if ("shop".equalsIgnoreCase(adType)) {
                        context.put(VelocityTemplateFieldConstants.AdText, adResponse.getString("content"));
                        String vmTemplate = Formatter.getRichTextTemplateForSlot(slot);
                        if (StringUtils.isEmpty(vmTemplate)) {
                            logger.info("No template found for the slot");
                            adStatus = "NO_AD";
                            return;
                        }
                        else {
                            context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                            t = TemplateType.RICH;
                        }
                    }
                    else {
                        t = TemplateType.IMAGE;
                    }
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl, logger);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from httpool : ", exception);
                logger.info("Response from httpool:", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from httpool : ", exception);
                logger.info("Response from httpool:", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    logger.info("Error while rethrowing the exception : ", e);
                }
            }
        }

        logger.debug("response length is ", responseContent.length(), "responseContent is", responseContent);
    }

    @Override
    public String getId()
    {
        return (config.getString("httpool.advertiserId"));
    }
}