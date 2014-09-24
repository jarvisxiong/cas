package com.inmobi.adserve.channels.adnetworks.miaozhen;

import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.adnetworks.rubicon.DCPRubiconAdnetwork;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.util.Date;

public class DCPMiaozhenAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPRubiconAdnetwork.class);
    private static final String APP = "m_app";
    private static final String OPEN_UDID = "m0";
    private static final String ANDROID_ID = "m1";
    private static final String IDFA = "m5";
    private static final String WIDTH = "m_adw";
    private static final String HEIGHT = "m_adh";
    private static final String DISPLAY_TYPE = "m_int";
    private static final String DEVICE_OS = "m_os";
    private static final String OS_VERSION = "m_osv";
    private static final String NETWORK_TYPE = "m_net";
    private static final String LOCAL_TIMESTAMP = "m_ts";
    private static final String POSITION = "m_pos";
    private static final String CLIENT_IP = "m_ip";
    private static final String UA = "m_ua";
    private static final String AD_POSITION = "l";
    private static final String BUNDLE_ID_TEMPLATE = "com.inmobi-exchange.%s";
    private String latitude;
    private String longitude;
    private String position;
    private String localTime;
    private int width;
    private int height;
    private int networkType;
    private boolean isValidOS;

    public DCPMiaozhenAdNetwork(final Configuration config,
                                final Bootstrap clientBootstrap,
                                final HttpRequestHandlerBase baseRequestHandler,
                                final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        // Checking if the mandatory parameters are available.
        // openUDID and IDFA check.
        if (isIOS()) {
            if (StringUtils.isBlank(casInternalRequestParameters.uidIDUS1)
                && StringUtils.isBlank(casInternalRequestParameters.uidIFA)) {
                LOG.debug("mandatory parameters missing for miaozhen so exiting adapter");
                return false;
            }
        }

        // Android ID check.
        if (isAndroid()) {
            if (StringUtils.isBlank(casInternalRequestParameters.uidMd5)
            && StringUtils.isBlank(casInternalRequestParameters.uid)
            && StringUtils.isBlank(casInternalRequestParameters.uidO1)) {
                LOG.debug("mandatory parameters missing for miaozhen so exiting adapter");
                return false;
            }
        }

        // Operating system check.
        int sasParamsOsId = sasParams.getOsId();
        isValidOS = (sasParamsOsId >= HandSetOS.Others.getValue()
            && sasParamsOsId <= HandSetOS.Windows_RT.getValue()); // Check if the OS ID is valid.
        if (!isValidOS) {
            LOG.debug("mandatory parameters missing for miaozhen so exiting adapter");
            return false;
        }

        // IP check.
        if(StringUtils.isBlank(sasParams.getRemoteHostIp())) {
            LOG.debug("mandatory parameters missing for miaozhen so exiting adapter");
            return false;
        }

        // Get latitude & longitude.
        host = config.getString("miaozhen.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
            && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];

            // Construct the position string.
            position = latitude + "," + longitude;
            if (latlong.length > 2) {
                position = position + "," + latlong[2];
            }
        }

        // Get width & height.
        if (null != sasParams.getSlot()
            && null != SlotSizeMapping.getDimension((long) sasParams.getSlot())) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        // Get local time stamp.
        Date current = new Date();
        localTime = String.valueOf(current.getTime());

        // Find the network type.
        if (NetworkType.WIFI == sasParams.getNetworkType()) {
            networkType = 1;
        } else {
            networkType = 2;
        }

        // Configuration successful.
        LOG.info("Configure parameters inside Miaozhen returned true");
        return true;
    }

    @Override
    public String getName() {
        return "miaozhen";
    }

    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder url = new StringBuilder(host);

        if(isApp()) {
            appendQueryParam(url, APP, String.format(BUNDLE_ID_TEMPLATE, blindedSiteId), false);
        }

        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(url, WIDTH, width, false);
        appendQueryParam(url, HEIGHT, height, false);
        appendQueryParam(url, POSITION, getURLEncode(position, format), false);
        appendQueryParam(url, NETWORK_TYPE, networkType, false);

        Integer sasParamsOsId = sasParams.getOsId();
        if (isValidOS) {
            appendQueryParam(url, DEVICE_OS, HandSetOS.values()[sasParamsOsId - 1].toString(), false);
        }
        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            appendQueryParam(url, OS_VERSION, sasParams.getOsMajorVersion(), false);
        }

        if (isAndroid()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.uidMd5, false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.uid, false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.uidO1, false);
            }
        }

        if (isIOS()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
                appendQueryParam(url, OPEN_UDID, casInternalRequestParameters.uidIDUS1, false);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                appendQueryParam(url, IDFA, casInternalRequestParameters.uidIFA, false);
            }
        }

        if (isInterstitial()) {
            appendQueryParam(url, DISPLAY_TYPE, 1, false);
        } else {
            appendQueryParam(url, DISPLAY_TYPE, 0, false);
        }

        appendQueryParam(url, AD_POSITION, externalSiteId, false);
        appendQueryParam(url, LOCAL_TIMESTAMP, localTime, false);

        LOG.debug("Miaozhen url is {}", url);
        return new URI(url.toString());
    }

    @Override
    public void parseResponse(final String response,
                              final HttpResponseStatus status) {
        LOG.debug("Miaozhen response is {}", response);

        if (isValidResponse(response, status)){
            statusCode = status.code();
            VelocityContext context = new VelocityContext();
            try {
                JSONObject ad = new JSONObject(response);
                if ("I".equalsIgnoreCase(ad.getString("type"))) {

                    // Image URL.
                    if (ad.has("src")) {
                        String imageURL = ad.getString("src");
                        context.put(VelocityTemplateFieldConstants.PartnerImgUrl, imageURL);
                    }

                    // Landing page URL.
                    if (ad.has("ldp")) {
                        String landingPageURL = ad.getString("ldp");
                        context.put(VelocityTemplateFieldConstants.PartnerClickUrl, landingPageURL);
                    }

                    // Click Macro.
                    if (ad.has("cm")) {
                        if (ad.getJSONArray("cm").length() > 0) {
                            String partnerClickBeacon = ad.getJSONArray("cm").getString(0);
                            context.put(VelocityTemplateFieldConstants.PartnerClickBeacon, partnerClickBeacon);
                        }
                    }

                    // Partner Macro.
                    if (ad.has("pm")) {
                        try {
                            JSONObject partnerBeacons = ad.getJSONObject("pm");

                            if (partnerBeacons.has("0")) {
                                JSONArray partnerBeaconArray = partnerBeacons.getJSONArray("0");

                                if (partnerBeaconArray.length() > 0) {
                                    String beaconURL = partnerBeaconArray.getString(0);
                                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, beaconURL);
                                }

                                if (partnerBeaconArray.length() > 1) {
                                    String beaconURL = partnerBeaconArray.getString(1);
                                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl1, beaconURL);
                                }

                                if (partnerBeaconArray.length() > 2) {
                                    String beaconURL = partnerBeaconArray.getString(2);
                                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl2, beaconURL);
                                }
                            }
                        } catch (Exception exception) {
                            adStatus = "NO_AD";
                            LOG.info("Error parsing response from Miaozhen");
                            LOG.info("Response from Miaozhen {}", response);
                            return;
                        }
                    }

                    // Width.
                    if (ad.has("adw")) {
                        Integer width = ad.getInt("adw");
                        context.put(VelocityTemplateFieldConstants.Width, width);
                    }

                    // Height.
                    if (ad.has("adh")) {
                        Integer height = ad.getInt("adh");
                        context.put(VelocityTemplateFieldConstants.Height,
                            height);
                    }

                    responseContent = Formatter.getResponseFromTemplate(TemplateType.IMAGE, context, sasParams, beaconUrl);
                    adStatus = "AD";
                } else {
                    adStatus = "NO_AD";
                }
            } catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from Miaozhen");
                LOG.info("Response from Miaozhen {}", response);
            }
        }
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public String getId() {
        return (config.getString("miaozhen.advertiserId"));
    }
}
