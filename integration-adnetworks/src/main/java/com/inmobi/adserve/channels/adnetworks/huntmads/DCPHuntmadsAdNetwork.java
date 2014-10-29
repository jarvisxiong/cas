package com.inmobi.adserve.channels.adnetworks.huntmads;

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
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPHuntmadsAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPHuntmadsAdNetwork.class);

    private transient String latitude;
    private transient String longitude;
    private int width;
    private int height;
    private boolean isapp;
    private static final String IDFA = "idfa";
    private static final String ANDROID_ID = "androidid";
    private static final String SITEID = "pubsiteid";


    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPHuntmadsAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.error("mandatory parameters missing for huntmads so exiting adapter");
            LOG.info("Configure parameters inside huntmads returned false");
            return false;
        }
        host = config.getString("huntmads.host");

        // blocking opera traffic
        if (sasParams.getUserAgent().toUpperCase().contains("OPERA")) {
            LOG.debug("Opera user agent found. So exiting the adapter");
            LOG.info("Configure parameters inside huntmads returned false");
            return false;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            final Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        isapp = StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource())
                        ? false : true;

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
            final StringBuilder url = new StringBuilder();
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

            if (casInternalRequestParameters.getZipCode() != null) {
                url.append("&zip=").append(casInternalRequestParameters.getZipCode());
            }

            if (isapp) {
                url.append("&isapp=yes");
                url.append("&isweb=no");
            } else {
                url.append("&isapp=no");
                url.append("&isweb=yes");

            }
            if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
                if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {

                    appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.getUidMd5(), false);
                } else if (null != casInternalRequestParameters.getUidO1()) {
                    appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.getUidO1(), false);
                }
                if (!StringUtils.isBlank(casInternalRequestParameters.getUid())) {
                    url.append("&udidtype=custom&udid=").append(casInternalRequestParameters.getUid());
                }
                if (null != casInternalRequestParameters.getGpid()) {
                    url.append("&androidaid=").append(casInternalRequestParameters.getGpid());
                    url.append("&adtracking=").append(casInternalRequestParameters.getUidADT());

                }

            }
            if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {

                if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA())
                        && "1".equals(casInternalRequestParameters.getUidADT())) {

                    appendQueryParam(url, IDFA, casInternalRequestParameters.getUidIFA(), false);
                }
                if (casInternalRequestParameters.getUidSO1() != null) {
                    url.append("&udidtype=odin1&udid=").append(casInternalRequestParameters.getUidSO1());
                } else if (casInternalRequestParameters.getUidMd5() != null) {
                    url.append("&udidtype=custom&udid=").append(casInternalRequestParameters.getUidMd5());
                } else if (!StringUtils.isBlank(casInternalRequestParameters.getUid())) {
                    url.append("&udidtype=custom&udid=").append(casInternalRequestParameters.getUid());
                }

            } else {
                final String uid = getUid();
                if (!StringUtils.isBlank(uid)) {
                    url.append("&udidtype=custom&udid=").append(casInternalRequestParameters.getUid());
                }
            }

            appendQueryParam(url, SITEID, blindedSiteId, false);

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

        if (StringUtils.isEmpty(response) || status.code() != 200 || !response.startsWith("[{\"")
                || response.startsWith("[{\"error")) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        } else {
            LOG.debug("beacon url inside huntmads is {}", beaconUrl);

            try {
                final JSONArray jArray = new JSONArray(response);
                final JSONObject adResponse = jArray.getJSONObject(0);
                final boolean textAd = !response.contains("type\": \"image");

                statusCode = status.code();
                final VelocityContext context = new VelocityContext();

                TemplateType t = TemplateType.HTML;
                if (adResponse.has("content") && StringUtils.isNotBlank(adResponse.getString("content"))) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, adResponse.getString("content"));
                } else {
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, adResponse.getString("url"));
                    final String partnerBeacon = adResponse.getString("track");
                    if (StringUtils.isNotBlank(partnerBeacon) && !"null".equalsIgnoreCase(partnerBeacon)) {
                        context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, adResponse.getString("track"));
                    }
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);

                    if (textAd && StringUtils.isNotBlank(adResponse.getString("text"))) {
                        context.put(VelocityTemplateFieldConstants.AD_TEXT, adResponse.getString("text"));
                        final String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
                        if (!StringUtils.isEmpty(vmTemplate)) {
                            context.put(VelocityTemplateFieldConstants.TEMPLATE, vmTemplate);
                            t = TemplateType.RICH;
                        } else {
                            t = TemplateType.PLAIN;
                        }
                    } else {
                        context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, adResponse.getString("img"));
                        t = TemplateType.IMAGE;
                    }
                }

                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            } catch (final JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from huntmads : {}", exception);
                LOG.info("Response from huntmads: {}", response);
            } catch (final Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from huntmads : {}", exception);
                LOG.info("Response from huntmads: {}", response);
                try {
                    throw exception;
                } catch (final Exception e) {
                    LOG.info("Error while rethrowing the exception : {}", e);
                }
            }
        }

        LOG.debug("response length is {} responseContent is {}", responseContent.length(), responseContent);
    }

    @Override
    public String getId() {
        return config.getString("huntmads.advertiserId");
    }
}
