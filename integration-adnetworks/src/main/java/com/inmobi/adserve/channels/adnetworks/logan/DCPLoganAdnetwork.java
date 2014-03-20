package com.inmobi.adserve.channels.adnetworks.logan;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
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
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPLoganAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG        = LoggerFactory.getLogger(DCPLoganAdnetwork.class);

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

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPLoganAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for logan so exiting adapter");
            return false;
        }
        host = config.getString("logan.host");

        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        LOG.info("Configure parameters inside logan returned true");
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
            LOG.debug("logan url is {}", url);
            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);

        if (StringUtils.isEmpty(response) || status.code() != 200 || response.startsWith("[{\"error")) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            LOG.debug("beacon url inside logan is {}", beaconUrl);

            try {
                JSONArray jArray = null;
                if (response.endsWith(";")) {
                    jArray = new JSONArray(response.substring(0, response.length() - 1));
                }
                else {
                    jArray = new JSONArray(response);
                }
                JSONObject adResponse = jArray.getJSONObject(0);
                boolean textAd = response.contains("\"text\" :") && !response.contains("\"text\" : \"\"");
                boolean bannerAd = false;
                if (!textAd) {
                    bannerAd = response.contains("\"img\" :") && !response.contains("\"img\" : \"\"");
                }

                statusCode = status.code();
                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, adResponse.get("track"));
                TemplateType t;
                if (textAd || bannerAd) {
                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("url"));
                    context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
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
                }
                else {
                    context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, adResponse.getString("content"));
                    t = TemplateType.HTML;
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from logan : {}", exception);
                LOG.info("Response from logan: {}", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from logan : {}", exception);
                LOG.info("Response from logan: {}", response);
            }

        }

        LOG.debug("response length is {} responseContent is {}", responseContent.length(), responseContent);
    }

    @Override
    public String getId() {
        return (config.getString("logan.advertiserId"));
    }
}