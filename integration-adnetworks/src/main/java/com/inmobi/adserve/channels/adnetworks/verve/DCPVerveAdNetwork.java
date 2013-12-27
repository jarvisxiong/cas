package com.inmobi.adserve.channels.adnetworks.verve;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPVerveAdNetwork extends BaseAdNetworkImpl {
    private final Configuration config;
    private transient String    latitude;
    private transient String    longitude;
    private int                 width;
    private int                 height;
    private String              portalKeyword;
    private static final String IPHONE_KEYWORD     = "iphn";
    private static final String ANDROID_KEYWORD    = "anap";
    private static final String WAP_KEYWORD        = "ptnr";
    private static final String WAP                = "wap";
    private static final String DERIVED_LAT_LONG   = "derived-lat-lon";
    private static final String TRUE_LAT_LONG_ONLY = "trueLatLongOnly";
    private boolean             sendTrueLatLongOnly;

    public DCPVerveAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
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
            logger.debug("mandatory parameters missing for verve so exiting adapter");
            return false;
        }
        host = config.getString("verve.host");

        try {
            // TRUE_LAT_LONG_ONLY is configured as the additional param in the segment table
            sendTrueLatLongOnly = Boolean.parseBoolean(entity.getAdditionalParams().getString(TRUE_LAT_LONG_ONLY));
        }
        catch (JSONException e) {
            sendTrueLatLongOnly = false;
            logger
                    .info("trueLatLong is not configured for the segment:{}", entity.getExternalSiteKey(),
                        this.getName());
        }

        if (sendTrueLatLongOnly) {
            if (DERIVED_LAT_LONG.equalsIgnoreCase(sasParams.getLocSrc())) {
                return false;
            }
            else if (casInternalRequestParameters.latLong != null
                    && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
                String[] latlong = casInternalRequestParameters.latLong.split(",");
                latitude = latlong[0];
                longitude = latlong[1];
            }
            else {
                return false;
            }
            if (StringUtils.isBlank(latitude) || StringUtils.isBlank(longitude)) {
                return false;
            }
        }
        else if (!DERIVED_LAT_LONG.equalsIgnoreCase(sasParams.getLocSrc())
                && StringUtils.isNotBlank(sasParams.getLocSrc())) { // request has true lat-long
            return false;
        }
        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long)sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        if (WAP.equalsIgnoreCase(sasParams.getSource())) {
            portalKeyword = WAP_KEYWORD;
        }
        else if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
            portalKeyword = IPHONE_KEYWORD;
        }
        else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
            if (StringUtils.isBlank(sasParams.getSdkVersion())
                    || sasParams.getSdkVersion().toLowerCase().startsWith("a35")) {
                logger.info("Blocking traffic for 3.5.* android version");
                return false;
            }
            portalKeyword = ANDROID_KEYWORD;
        }
        else {
            logger.info("param source ", sasParams.getSource());
            logger.info("Configure parameters inside verve returned false");
            return false;
        }

        logger.info("Configure parameters inside verve returned true");
        return true;
    }

    @Override
    public String getName() {
        return "verve";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder();
            url.append(host).append("?ip=").append(sasParams.getRemoteHostIp());
            url.append("&p=").append(portalKeyword);
            url.append("&b=").append(externalSiteId);
            url.append("&site=").append(blindedSiteId);
            if (!StringUtils.isEmpty(sasParams.getGender())) {
                url.append("&ei=gender=").append(sasParams.getGender().toLowerCase());
            }
            if (null != sasParams.getAge()) {
                url.append(";age=").append(sasParams.getAge());
            }
            url.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
            if (sendTrueLatLongOnly) {
                url.append("&lat=").append(latitude);
                url.append("&long=").append(longitude);
            }

            if (!"wap".equalsIgnoreCase(sasParams.getSource())) {
                if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
                    if (casInternalRequestParameters.uidIFA != null) {
                        url.append("&uis=a&ui=").append(casInternalRequestParameters.uidIFA);
                    }
                    else if (casInternalRequestParameters.uidO1 != null) {
                        url.append("&uis=us&ui=").append(casInternalRequestParameters.uidO1);
                    }
                    else if (casInternalRequestParameters.uidMd5 != null) {
                        url.append("&uis=u&ui=").append(casInternalRequestParameters.uidMd5);
                    }
                    else if (!StringUtils.isBlank(casInternalRequestParameters.uid)
                            && !casInternalRequestParameters.uid.equals("null")) {
                        url.append("&uis=v&ui=").append(casInternalRequestParameters.uid);
                    }
                }
                else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
                    if (casInternalRequestParameters.uidO1 != null) {
                        url.append("&uis=ds&ui=").append(casInternalRequestParameters.uidO1);
                    }
                    else if (casInternalRequestParameters.uidMd5 != null) {
                        url.append("&uis=dm&ui=").append(casInternalRequestParameters.uidMd5);
                    }
                    else if (!StringUtils.isBlank(casInternalRequestParameters.uid)
                            && !casInternalRequestParameters.uid.equals("null")) {
                        url.append("&uis=v&ui=").append(casInternalRequestParameters.uid);
                    }
                }
            }

            if (casInternalRequestParameters.zipCode != null) {
                url.append("&z=").append(casInternalRequestParameters.zipCode);
            }

            url.append("&c=97");// get category map

            if (width != 0 && height != 0) {
                url.append("&adunit=").append(width).append('x').append(height);
            }

            logger.debug("Verve url is ", url);
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
        logger.debug("response is ", response, "and response length is ", response.length());
        if (status.getCode() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.getCode();
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.IMBeaconUrl, beaconUrl);
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl,
                    logger);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from verve : ", exception);
                logger.info("Response from verve : ", response);
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
    public String getId() {
        return (config.getString("verve.advertiserId"));
    }
}
