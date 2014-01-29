package com.inmobi.adserve.channels.adnetworks.huntmads;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPHuntmadsAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPHuntmadsAdNetwork.class);

    private transient String    latitude;
    private transient String    longitude;
    private int                 width;
    private int                 height;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPHuntmadsAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for huntmads so exiting adapter");
            return false;
        }
        host = config.getString("huntmads.host");
        // blocking opera traffic
        if (sasParams.getUserAgent().toUpperCase().contains("OPERA")) {
            LOG.debug("Opera user agent found. So exiting the adapter");
            return false;
        }
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
        LOG.info("Configure parameters inside huntmads returned true");
        return true;
    }

    @Override
    public String getName() {
        return "huntmads";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
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
            if (sasParams.getCountryCode() != null) {
                url.append("&country=").append(sasParams.getCountryCode().toUpperCase());
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
            LOG.debug("Huntmads url is {}", url);

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
            LOG.debug("beacon url inside huntmads is {}", beaconUrl);

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
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from huntmads : {}", exception);
                LOG.info("Response from huntmads: {}", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from huntmads : {}", exception);
                LOG.info("Response from huntmads: {}", response);
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
        return (config.getString("huntmads.advertiserId"));
    }
}