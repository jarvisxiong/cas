package com.inmobi.adserve.channels.adnetworks.httpool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.config.GlobalConstant;


public class DCPHttPoolAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPHttPoolAdNetwork.class);

    private transient String latitude;
    private transient String longitude;
    private String slotFormat;
    private boolean acceptShop = false;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPHttPoolAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for httpool so exiting adapter");
            LOG.info("Configure parameters inside HttPool returned false");
            return false;
        }
        host = config.getString("httpool.host");

        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (repositoryHelper.querySlotSizeMapRepository(selectedSlotId) != null) {
            Short slotSize = selectedSlotId;
            // Httpool doesnt support 320x48 & 320x53. so mapping to 320x50
            if (slotSize == (short)9 || slotSize == (short)24) {
                slotSize = 15;
            }
            final Dimension dim = repositoryHelper.querySlotSizeMapRepository(slotSize).getDimension();
            acceptShop = dim.getWidth() > 299;
            slotFormat = String.format("%dx%d", (int) Math.ceil(dim.getWidth()), (int) Math.ceil(dim.getHeight()));
        }

        return true;
    }

    @Override
    public String getName() {
        return "httpoolDCP";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder(host);

            url.append("type=rich%2Ctpt");
            if (acceptShop) {
                url.append("%2Cshop");
            }
            url.append("&uip=").append(sasParams.getRemoteHostIp());
            url.append("&zid=").append(externalSiteId);
            url.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
            if (GlobalConstant.ONE.equals(config.getString("httpool.test"))) {
                url.append("&test=1");
            }
            if (!StringUtils.isBlank(latitude) && !StringUtils.isBlank(longitude)) {
                url.append("&geo_lat=").append(latitude);
                url.append("&geo_lng=").append(longitude);
            }

            String did = getUid();
            if (StringUtils.isEmpty(did) || "null".equals(did)) {
                did = "nodeviceid-1234567890";
            }
            url.append("&did=").append(did);
            if (null != selectedSlotId) {
                url.append("&format=").append(slotFormat);
            }
            final String category = getCategories(';');
            if (!StringUtils.isEmpty(category)) {
                url.append("&ct=").append(getURLEncode(category, format));
            }
            final String gender = sasParams.getGender();
            if (StringUtils.isNotBlank(gender)) {
                url.append("&dd_gnd=").append(GlobalConstant.GENDER_FEMALE.equalsIgnoreCase(gender) ? 2 : 1);
            }
            LOG.debug("httpool url is {}", url.toString());

            return new URI(url.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);

        if (StringUtils.isEmpty(response) || status.code() != 200) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            LOG.debug("beacon url inside httpool is {}", beaconUrl);

            try {
                final JSONObject adResponse = new JSONObject(response);
                if (adResponse.getInt("status") == 0) {
                    statusCode = 500;
                    responseContent = DEFAULT_EMPTY_STRING;
                    return;
                }
                statusCode = status.code();
                TemplateType t;
                final VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);
                context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, adResponse.getString("impression_url"));
                final String adType = adResponse.getString("ad_type");
                if ("tpt".equalsIgnoreCase(adType)) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, adResponse.getString("content"));
                    t = TemplateType.HTML;
                } else {
                    final String landingUrl =
                            adResponse.getString("click_url") + "&url=" + adResponse.getString("redirect_url");
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, landingUrl);
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, adResponse.getString("image_url"));
                    if ("shop".equalsIgnoreCase(adType)) {
                        context.put(VelocityTemplateFieldConstants.AD_TEXT, adResponse.getString("content"));
                        final String vmTemplate = Formatter.getRichTextTemplateForSlot(selectedSlotId.toString());
                        if (StringUtils.isEmpty(vmTemplate)) {
                            LOG.info("No template found for the slot");
                            adStatus = NO_AD;
                            return;
                        } else {
                            context.put(VelocityTemplateFieldConstants.TEMPLATE, vmTemplate);
                            t = TemplateType.RICH;
                        }
                    } else {
                        t = TemplateType.IMAGE;
                    }
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                LOG.debug("response length is {} responseContent is {}", responseContent.length(), responseContent);
                adStatus = AD_STRING;
            } catch (final JSONException exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from httpool: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from httpool: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }


    }

    @Override
    public String getId() {
        return config.getString("httpool.advertiserId");
    }
}
