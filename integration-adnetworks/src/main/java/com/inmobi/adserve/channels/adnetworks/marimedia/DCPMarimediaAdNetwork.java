package com.inmobi.adserve.channels.adnetworks.marimedia;

// Created by Dhanasekaran K P on 23/9/14.

import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.adnetworks.rubicon.DCPRubiconAdnetwork;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;

public class DCPMarimediaAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPRubiconAdnetwork.class);
    private static final String UA = "u";
    private static final String APP_ID = "a";
    private static final String IP_ADDRESS = "i";
    private static final String RESOLUTION = "r";
    private static final String AD_TYPE = "t";

    private static final String ANDROID_ID = "tt_android_id";
    private static final String ANDROID_ID_SHA1 = "tt_android_id_sha1";
    private static final String ANDROID_ID_MD5 = "tt_android_id_md5";
    private static final String ANDROID_ADVERTISING_ID = "tt_advertising_id";

    private static final String IDFA = "tt_idfa";
    private static final String UDID = "tt_udid";
    private static final String UDID_MD5 = "tt_udid_md5";

    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";
    private static final String NETWORK_TYPE = "nt";

    private static final String GENDER = "gender";
    private static final String AGE = "age";

    private int width;
    private int height;
    private String resolution;
    private String networkType;

    public DCPMarimediaAdNetwork(final Configuration config,
                                 final Bootstrap clientBootstrap,
                                 final HttpRequestHandlerBase baseRequestHandler,
                                 final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        // Get the host.
        host = config.getString("marimedia.host");

        // Check User Agent.
        if(StringUtils.isBlank(sasParams.getUserAgent())) {
            LOG.debug("Mandatory parameters missing for Marimedia so exiting adapter");
            return false;
        }

        // Check App ID.
        // i.e. Check externalSiteId.
        if(StringUtils.isBlank(externalSiteId)) {
            LOG.debug("Mandatory parameters missing for Marimedia so exiting adapter");
            return false;
        }

        // Check IP Address.
        if(StringUtils.isBlank(sasParams.getRemoteHostIp())) {
            LOG.debug("Mandatory parameters missing for Marimedia so exiting adapter");
            return false;
        }

        // Check Resolution.
        if (null == sasParams.getSlot()
                || null == SlotSizeMapping.getDimension((long) sasParams.getSlot())) {
            LOG.debug("Mandatory parameters missing for Marimedia so exiting adapter");
            return false;
        }
        else {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());

            resolution = width + "x" + height;
        }

        // Check Ad type.
        // Will be set in getRequestUri().
        // No video format supported, either "Interstitial" or "Banner".

        // Find the network type.
        if (NetworkType.WIFI == sasParams.getNetworkType()) {
            networkType = "wifi";
        } else {
            networkType = "carrier";
        }

        // Configuration successful.
        LOG.info("Configure parameters inside Marimedia returned true");
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder url = new StringBuilder(host);

        // Set User Agent.
        if(StringUtils.isNotBlank(sasParams.getUserAgent())) {
            appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        }

        // Set App ID.
        if(StringUtils.isNotBlank(externalSiteId)) {
            appendQueryParam(url, APP_ID, getURLEncode(externalSiteId, format), false);
        }

        // Set IP.
        if(StringUtils.isNotBlank(sasParams.getRemoteHostIp())) {
            appendQueryParam(url, IP_ADDRESS, getURLEncode(sasParams.getRemoteHostIp(), format), false);
        }

        // Set Ad type.
        if (isInterstitial()) {
            // Interstitial.
            appendQueryParam(url, AD_TYPE, getURLEncode("3", format), false);
        } else {
            // Banner.
            appendQueryParam(url, AD_TYPE, getURLEncode("2", format), false);
        }

        // Set Resolution.
        if(StringUtils.isNotBlank(resolution)) {
            appendQueryParam(url, RESOLUTION, getURLEncode(resolution, format), false);
        }

        // Set Latitude & Longitude.
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");

            appendQueryParam(url, LATITUDE, getURLEncode(latlong[0], format), false);
            appendQueryParam(url, LONGITUDE, getURLEncode(latlong[1], format), false);
        }

        // Set Android ID.
        if (isAndroid()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.uidMd5, false);
                appendQueryParam(url, ANDROID_ID_MD5, casInternalRequestParameters.uidMd5, false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.uid, false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.uidO1, false);
                appendQueryParam(url, ANDROID_ID_SHA1, casInternalRequestParameters.uidO1, false);
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.gpid)) {
                appendQueryParam(url, ANDROID_ADVERTISING_ID, casInternalRequestParameters.gpid, false);
            }
        }

        // Set IDFA and UDID.
        if (isIOS()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
                appendQueryParam(url, UDID, casInternalRequestParameters.uidIDUS1, false);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                appendQueryParam(url, IDFA, casInternalRequestParameters.uidIFA, false);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                appendQueryParam(url, UDID_MD5, casInternalRequestParameters.uidMd5, false);
            }
        }

        // Set Network Type.
        appendQueryParam(url, NETWORK_TYPE, networkType, false);

        // Set Age.
        if(null != sasParams.getAge()) {
            appendQueryParam(url, AGE, getURLEncode(sasParams.getAge().toString(), format), false);
        }

        // Set Gender.
        if(null != sasParams.getGender()) {
            appendQueryParam(url, GENDER, getURLEncode(sasParams.getGender(), format), false);
        }

        LOG.debug("Marimedia url is {}", url);
        return new URI(url.toString());
    }

    // For POST requests.
    @Override
    protected Request getNingRequest() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }
        return new RequestBuilder().setUrl(uri.toString()).setHeader("x-display-metrics", String.format("%sx%s", width, height))
                .setHeader("xplus1-user-agent", sasParams.getUserAgent())
                .setHeader("x-plus1-remote-addr", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us").setHeader(HttpHeaders.Names.REFERER, requestUrl)
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader(HttpHeaders.Names.HOST, uri.getHost())
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp()).build();
    }

    @Override
    public void parseResponse(final String response,
                              final HttpResponseStatus status) {
        LOG.debug("Marimedia response is {}", response);

        if (isValidResponse(response, status)) {
            statusCode = status.code();
            VelocityContext context = new VelocityContext();
            try {
                JSONObject ad = new JSONObject(response);

                // Banner or Interstitial.
                if (ad.getString("adType").equalsIgnoreCase("banner")) {
                    String imageUrl = ad.getString("imageUrl");
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, imageUrl);
                }
                else if( ad.getString("adType").equalsIgnoreCase("html")) {
                    String htmlUrl = ad.getString("htmlUrl");
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, htmlUrl);
                }
                else {
                    // Other format.
                    // adType is "video" or "empty".
                    adStatus = "NO_AD";
                    LOG.info("Error parsing response from Marimedia");
                    LOG.info("Response from Marimedia {}", response);
                    return;
                }

                // Ad URL.
                String adUrl = ad.getString("adUrl");
                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adUrl);

                // Impression URL.
                if(ad.has("impUrl")) {
                    String impressionUrl = ad.getString("impUrl");
                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, impressionUrl);
                }

                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);

                responseContent = Formatter.getResponseFromTemplate(Formatter.TemplateType.IMAGE, context, sasParams, beaconUrl);
                adStatus = "AD";
            } catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from Marimedia");
                LOG.info("Response from Marimedia {}", response);
            }
        }
    }

    @Override
    public String getName() {
        return "marimedia";
    }

    @Override
    public String getId() {
        return (config.getString("marimedia.advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }
}