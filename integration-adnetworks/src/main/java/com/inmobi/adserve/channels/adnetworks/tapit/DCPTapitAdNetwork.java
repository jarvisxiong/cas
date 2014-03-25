package com.inmobi.adserve.channels.adnetworks.tapit;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
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

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPTapitAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPTapitAdNetwork.class);

    private String              latitude;
    private String              longitude;
    private double              width;
    private double              height;

    public DCPTapitAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandate parameters missing for tapit so exiting adapter");
            return false;
        }
        if (sasParams.getUserAgent().toUpperCase().contains("OPERA")) {
            LOG.debug("Opera user agent found. So exiting the adapter");
            return false;
        }
        host = config.getString("tapit.host");
        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long)sasParams.getSlot());
            width = dim.getWidth();
            height = dim.getHeight();
        }
        LOG.debug("Configure parameters inside tapit returned true");
        return true;
    }

    @Override
    public String getName() {
        return "tapit";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
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
            
            if (StringUtils.isNotEmpty(casInternalRequestParameters.uidIFA)) {
                url.append("&enctype=raw&idfa=").append(casInternalRequestParameters.uidIFA);
            }
            if (StringUtils.isNotEmpty(casInternalRequestParameters.uidO1)) {
                url.append("&enctype=sha1&udid=").append(casInternalRequestParameters.uidO1);
            }
            else if (StringUtils.isNotEmpty(casInternalRequestParameters.uidMd5)) {
                url.append("&enctype=md5&udid=").append(casInternalRequestParameters.uidMd5);
            }
        
            if (width != 0 && height != 0) {
                url.append("&w=").append(width);
                url.append("&h=").append(height);
            }
            url.append("&tpsid=").append(blindedSiteId);

            LOG.debug("Tapit url is {}", url);
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
        if (StringUtils.isEmpty(response) || status.getCode() != 200 || response.contains("{\"error")) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            LOG.debug("beacon url inside mullah media is {}", beaconUrl);
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
                        String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
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
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from tapit : {}", exception);
                LOG.info("Response from tapit: {}", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from tapit : {}", exception);
                LOG.info("Response from tapit: {}", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    LOG.info("Error while rethrowing the exception : {}", e);
                }
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("tapit.advertiserId"));
    }
}