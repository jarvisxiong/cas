package com.inmobi.adserve.channels.adnetworks.paypal;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
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


public class DCPPayPalAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPPayPalAdNetwork.class);

    private String latitude = null;
    private String longitude = null;
    private int width;
    private int height;
    private String deviceId;
    private String responseFormat;

    public DCPPayPalAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for paypal so exiting adapter");
            LOG.info("Configure parameters inside paypal returned false");
            return false;
        }
        host = config.getString("paypal.host");
        responseFormat = config.getString("paypal.format");

        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            final Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        } else {
            LOG.debug("mandate parameters missing for paypal, so returning from adapter");
            LOG.info("Configure parameters inside paypal returned false");
            return false;
        }
        if (sasParams.getOsId() == HandSetOS.Android.getValue() || sasParams.getOsId() == HandSetOS.iOS.getValue()) {
            deviceId = getUid();
            if (StringUtils.isBlank(deviceId) || deviceId == null) {
                LOG.debug("mandate parameters missing for paypal, so returning from adapter");
                LOG.info("Configure parameters inside paypal returned false");
                return false;
            }
        }
        if (casInternalRequestParameters.getLatLong() != null
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        return true;
    }

    @Override
    public String getName() {
        return "paypal";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder(host);
            url.append("&format=").append(responseFormat);
            url.append("&ip=").append(sasParams.getRemoteHostIp());
            url.append("&pubid=").append(externalSiteId);
            url.append("&site=").append(blindedSiteId);
            url.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
            url.append("&width=").append(width);
            url.append("&placementtype=").append(width).append("x").append(height);

            if (sasParams.getGender() != null) {
                url.append("&gender=").append(sasParams.getGender());
            }
            if (sasParams.getAge() != null) {
                url.append("&age=").append(sasParams.getAge());
            }
            if (!StringUtils.isEmpty(latitude)) {
                url.append("&lat=").append(latitude);
                url.append("&lng=").append(longitude);
            }
            if (casInternalRequestParameters.getZipCode() != null) {
                url.append("&zip=").append(casInternalRequestParameters.getZipCode());
            }

            if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
                if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA())) {
                    url.append("&idfa=").append(casInternalRequestParameters.getUidIFA());
                    url.append("&ate=").append(casInternalRequestParameters.getUidADT());
                } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFV())) {
                    url.append("&idfv=").append(casInternalRequestParameters.getUidIFV());
                    url.append("&ate=").append(casInternalRequestParameters.getUidADT());
                }
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidSO1())) {
                url.append("&odn1=").append(casInternalRequestParameters.getUidSO1());
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
                url.append("&ms1=").append(casInternalRequestParameters.getUidO1());
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                url.append("&u=").append(casInternalRequestParameters.getUidMd5());
            }

            url.append("&cat=").append(getURLEncode(getCategories(','), format));

            LOG.debug("paypal url is {}", url);

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

        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        } else {
            statusCode = status.code();
            try {
                final VelocityContext context = new VelocityContext();
                TemplateType t;

                if (responseFormat.equalsIgnoreCase(TemplateType.HTML.name())) {
                    t = TemplateType.HTML;
                    context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response);
                } else {
                    t = TemplateType.IMAGE;
                    final JSONObject adResponse =
                            new JSONObject(response).getJSONObject("adresponse").getJSONObject("imp");
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, adResponse.getString("clickurl"));
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, adResponse.getString("imgurl"));
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            } catch (final JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response {} from paypal: {}", response, exception);
            } catch (final Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response {} from paypal: {}", response, exception);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return config.getString("paypal.advertiserId");
    }
}
