package com.inmobi.adserve.channels.adnetworks.logan;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPLoganAdnetwork extends BaseAdNetworkImpl {
    private final Configuration config;
    private transient String    latitude;
    private transient String    longitude;
    private int                 width;
    private int                 height;
    private static final String ZONE       = "zone";
    private static final String SITE       = "site";
    private static final String UDID       = "udid";
    private static final String SIZE_X     = "size_x";
    private static final String SIZE_Y     = "size_y";
    private static final String MIN_SIZE_X = "min_size_x";
    private static final String MIN_SIZE_Y = "min_size_y";

    public DCPLoganAdnetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
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
            logger.debug("mandatory parameters missing for logan so exiting adapter");
            return false;
        }
        host = config.getString("logan.host");

        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long)sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        logger.info("Configure parameters inside logan returned true");
        return true;
    }

    @Override
    public String getName() {
        return "logan";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder(host);
            appendQueryParam(url, IP, sasParams.getRemoteHostIp(), false);
            appendQueryParam(url, ZONE, externalSiteId, false);
            appendQueryParam(url, SITE, blindedSiteId, false);
            appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
            if (!StringUtils.isBlank(latitude) && !StringUtils.isBlank(longitude)) {
                appendQueryParam(url, LAT, latitude, false);
                appendQueryParam(url, LONG, longitude, false);
            }
            String udid = null;
            if (casInternalRequestParameters.uidO1 != null) {
                udid = casInternalRequestParameters.uidO1;
            }
            else if (casInternalRequestParameters.uidIFA != null) {
                udid = casInternalRequestParameters.uidIFA;
            }
            else if (casInternalRequestParameters.uidMd5 != null) {
                udid = casInternalRequestParameters.uidMd5;
            }
            else if (!StringUtils.isBlank(casInternalRequestParameters.uid)) {
                udid = casInternalRequestParameters.uid;
            }
            if (udid != null) {
                appendQueryParam(url, UDID, udid, false);
            }
            if (casInternalRequestParameters.zipCode != null) {
                appendQueryParam(url, ZIP, casInternalRequestParameters.zipCode, false);
            }
            if (sasParams.getCountryId() != null) {
                appendQueryParam(url, COUNTRY, sasParams.getCountryId().toString(), false);
            }
            if (width != 0 && height != 0) {
                appendQueryParam(url, MIN_SIZE_X, (int) (width * .9) + "", false);
                appendQueryParam(url, MIN_SIZE_Y, (int) (height * .9) + "", false);
                appendQueryParam(url, SIZE_X, width + "", false);
                appendQueryParam(url, SIZE_Y, height + "", false);
            }
            logger.debug("logan url is ", url.toString());
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
        logger.debug("response is ", response);

        if (StringUtils.isEmpty(response) || status.getCode() != 200 || response.startsWith("[{\"error")) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            logger.debug("beacon url inside logan is ", beaconUrl);

            try {
                JSONArray jArray = new JSONArray(response);
                JSONObject adResponse = jArray.getJSONObject(0);
                boolean textAd = !response.contains("type\": \"image");

                statusCode = status.getCode();
                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("url"));
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, adResponse.get("track"));
                TemplateType t;
                if (textAd && StringUtils.isNotBlank(adResponse.getString("text"))) {
                    context.put(VelocityTemplateFieldConstants.AdText, adResponse.getString("text"));
                    String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
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
                logger.info("Error parsing response from logan : ", exception);
                logger.info("Response from logan:", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from logan : ", exception);
                logger.info("Response from logan:", response);
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug("response length is ", responseContent.length(), "responseContent is", responseContent);
        }
    }

    @Override
    public String getId() {
        return (config.getString("logan.advertiserId"));
    }
}
