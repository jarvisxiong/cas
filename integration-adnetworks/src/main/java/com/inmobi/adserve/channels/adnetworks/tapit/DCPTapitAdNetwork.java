package com.inmobi.adserve.channels.adnetworks.tapit;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPTapitAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPTapitAdNetwork.class);

    private String latitude;
    private String longitude;
    private double width;
    private double height;

    public DCPTapitAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
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
        if (casInternalRequestParameters.getLatLong() != null
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != selectedSlotId && SlotSizeMapping.getDimension(selectedSlotId) != null) {
            final Dimension dim = SlotSizeMapping.getDimension(selectedSlotId);
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
            final StringBuilder url = new StringBuilder();
            url.append(host).append("?format=").append(config.getString("tapit.responseFormat")).append("&ip=");
            url.append(sasParams.getRemoteHostIp()).append("&ua=")
                    .append(getURLEncode(sasParams.getUserAgent(), format));
            if ("1".equals(config.getString("tapit.test"))) {
                url.append("&mode=test");
            }
            url.append("&zone=").append(externalSiteId);
            if (!StringUtils.isBlank(latitude) && !StringUtils.isBlank(longitude)) {
                url.append("&lat=").append(latitude);
                url.append("&long=").append(longitude);
            }

            if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidIFA())) {
                url.append("&enctype=raw&idfa=").append(casInternalRequestParameters.getUidIFA());
            }

            if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidO1())) {
                url.append("&enctype=sha1&udid=").append(casInternalRequestParameters.getUidO1());
            } else if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidMd5())) {
                url.append("&enctype=md5&udid=").append(casInternalRequestParameters.getUidMd5());
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
                appendQueryParam(url, "&enctype=sha1&udid=", casInternalRequestParameters.getUidIDUS1(), false);
            }
            final String gpid = getGPID();
            if (gpid != null) {
                url.append("&adid=").append(gpid);
            }


            if (width != 0 && height != 0) {
                url.append("&w=").append(width);
                url.append("&h=").append(height);
            }
            url.append("&tpsid=").append(blindedSiteId);

            LOG.debug("Tapit url is {}", url);
            return new URI(url.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        if (StringUtils.isEmpty(response) || status.code() != 200 || response.contains("{\"error")) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        } else {
            LOG.debug("beacon url inside mullah media is {}", beaconUrl);
            try {
                statusCode = status.code();
                final JSONObject adResponse = new JSONObject(response);
                final VelocityContext context = new VelocityContext();
                TemplateType t;
                if ("html".equals(adResponse.getString("type"))) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, adResponse.getString("html"));
                    t = TemplateType.HTML;
                } else {
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, adResponse.getString("clickurl"));
                    context.put(VelocityTemplateFieldConstants.WIDTH, adResponse.getString("adWidth"));
                    context.put(VelocityTemplateFieldConstants.HEIGHT, adResponse.getString("adHeight"));
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);
                    if ("text".equals(adResponse.getString("type"))) {
                        context.put(VelocityTemplateFieldConstants.AD_TEXT, adResponse.getString("adtext"));
                        final String vmTemplate = Formatter.getRichTextTemplateForSlot(selectedSlotId.toString());
                        if (StringUtils.isEmpty(vmTemplate)) {
                            t = TemplateType.PLAIN;
                        } else {
                            context.put(VelocityTemplateFieldConstants.TEMPLATE, vmTemplate);
                            t = TemplateType.RICH;
                        }
                    } else {
                        context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, adResponse.getString("imageurl"));
                        t = TemplateType.IMAGE;
                    }
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            } catch (final JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response {} from tapit: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            } catch (final Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response {} from tapit: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return config.getString("tapit.advertiserId");
    }
}
