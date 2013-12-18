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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPHttPoolAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG        = LoggerFactory.getLogger(DCPHttPoolAdNetwork.class);

    private transient String    latitude;
    private transient String    longitude;
    private String              slotFormat;
    private boolean             acceptShop = false;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPHttPoolAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for httpool so exiting adapter");
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

        LOG.info("Configure parameters inside httpool returned true");
        return true;
    }

    @Override
    public String getName() {
        return "httpool";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
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
            LOG.debug("httpool url is {}", url.toString());

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

        if (StringUtils.isEmpty(response) || status.getCode() != 200) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            LOG.debug("beacon url inside httpool is {}", beaconUrl);

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
                            LOG.info("No template found for the slot");
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
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from httpool : {}", exception);
                LOG.info("Response from httpool: {}", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from httpool : {}", exception);
                LOG.info("Response from httpool: {}", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    LOG.info("Error while rethrowing the exception : {}", e);
                }
            }
        }

        LOG.debug("response length is {} responseContent is {}", responseContent.length(), responseContent);
    }

    @Override
    public String getId() {
        return (config.getString("httpool.advertiserId"));
    }
}