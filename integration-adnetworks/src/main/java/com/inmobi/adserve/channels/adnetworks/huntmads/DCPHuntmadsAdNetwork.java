package com.inmobi.adserve.channels.adnetworks.huntmads;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONArray;
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


public class DCPHuntmadsAdNetwork extends BaseAdNetworkImpl
{
    private final Configuration config;
    private transient String    latitude;
    private transient String    longitude;
    private int                 width;
    private int                 height;

    public DCPHuntmadsAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
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
            logger.debug("mandatory parameters missing for huntmads so exiting adapter");
            return false;
        }
        host = config.getString("huntmads.host");
        // blocking opera traffic
        if (sasParams.getUserAgent().toUpperCase().contains("OPERA")) {
            logger.debug("Opera user agent found. So exiting the adapter");
            return false;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        logger.info("Configure parameters inside huntmads returned true");
        return true;
    }

    @Override
    public String getName()
    {
        return "huntmads";
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
            url.append(host).append("?ip=").append(sasParams.getRemoteHostIp());
            url.append("&track=1&timeout=500&rmtype=none&key=6&type=3&over_18=0&zone=").append(externalSiteId);
            url.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
            if ("1".equals(config.getString("huntmads.test"))) {
                url.append("&test=1");
            }
            if (!StringUtils.isBlank(latitude) && !StringUtils.isBlank(longitude)) {
                url.append("&lat=").append(latitude);
                url.append("&long=").append(longitude);
            }

            if (casInternalRequestParameters.uidO1 != null) {
                url.append("&udidtype=odin1&udid=").append(casInternalRequestParameters.uidO1);
            }
            else if (casInternalRequestParameters.uidIFA != null) {
                url.append("&udidtype=ifa&udid=").append(casInternalRequestParameters.uidIFA);
            }
            else if (casInternalRequestParameters.uidMd5 != null) {
                url.append("&udidtype=custom&udid=").append(casInternalRequestParameters.uidMd5);
            }
            else if (!StringUtils.isBlank(casInternalRequestParameters.uid)
                    && !casInternalRequestParameters.uid.equals("null")) {
                url.append("&udidtype=custom&udid=").append(casInternalRequestParameters.uid);
            }

            if (casInternalRequestParameters.zipCode != null) {
                url.append("&zip=").append(casInternalRequestParameters.zipCode);
            }
            if (sasParams.getCountry() != null) {
                url.append("&country=").append(sasParams.getCountry().toUpperCase());
            }

            if (width != 0 && height != 0) {
                url.append("&min_size_x=").append((int) (width * .9));
                url.append("&min_size_y=").append((int) (height * .9));
                url.append("&size_x=").append(width);
                url.append("&size_y=").append(height);

                if (width > 460 || height > 200) {
                    url.append("&format=").append(width).append('x').append(height);
                }
            }
            url.append("&keywords=").append(getURLEncode(getCategories(','), format));
            logger.debug("Huntmads url is ", url.toString());

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

        if (StringUtils.isEmpty(response) || status.getCode() != 200 || !response.startsWith("[{\"")
                || response.startsWith("[{\"error")) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            logger.debug("beacon url inside huntmads is ", beaconUrl);

            try {
                JSONArray jArray = new JSONArray(response);
                JSONObject adResponse = jArray.getJSONObject(0);
                boolean textAd = !response.contains("type\": \"image");

                statusCode = status.getCode();
                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("url"));
                String partnerBeacon = adResponse.getString("track");
                if (StringUtils.isNotBlank(partnerBeacon) && !"null".equalsIgnoreCase(partnerBeacon)) {
                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, adResponse.getString("track"));
                }
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                TemplateType t;
                if (textAd && StringUtils.isNotBlank(adResponse.getString("text"))) {
                    context.put(VelocityTemplateFieldConstants.AdText, adResponse.getString("text"));
                    String vmTemplate = Formatter.getRichTextTemplateForSlot(slot);
                    if (!StringUtils.isEmpty(vmTemplate)) {
                        context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                        t = TemplateType.RICH;
                    }
                    else {
                        t = TemplateType.PLAIN;
                    }
                }
                else {
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("img"));
                    t = TemplateType.IMAGE;
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl, logger);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from huntmads : ", exception);
                logger.info("Response from huntmads:", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from huntmads : ", exception);
                logger.info("Response from huntmads:", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    logger.info("Error while rethrowing the exception : ", e);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("response length is ", responseContent.length(), "responseContent is", responseContent);
        }
    }

    @Override
    public String getId()
    {
        return (config.getString("huntmads.advertiserId"));
    }
}