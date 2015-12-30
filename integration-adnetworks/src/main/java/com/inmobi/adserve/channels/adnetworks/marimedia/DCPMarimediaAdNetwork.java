package com.inmobi.adserve.channels.adnetworks.marimedia;

// Created by Dhanasekaran K P on 23/9/14.

import java.awt.Dimension;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.RequestBuilder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DCPMarimediaAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPMarimediaAdNetwork.class);
    private static final String UA = "u";
    private static final String APP_ID = "a";
    private static final String IP_ADDRESS = "i";
    private static final String RESOLUTION = "r";
    private static final String AD_TYPE = "t";

    private static final String ANDROID_ID = "tt_android_id";
    private static final String ANDROID_ID_SHA1 = "tt_android_id_sha1";
    private static final String ANDROID_ID_MD5 = "tt_android_id_md5";
    private static final String ANDROID_ADVERTISING_ID = "tt_advertising_id";
    private static final String BLINDED_SITE_ID = "tt_sub_aff";
    private static final String LONGITUDE = "lon";

    private static final String IDFA = "tt_idfa";
    private static final String UDID = "tt_udid";
    private static final String UDID_MD5 = "tt_udid_md5";

    private static final String NETWORK_TYPE = "nt";

    private static final String AGE = "age";

    private int width;
    private int height;
    private String resolution;
    private String networkType;

    public DCPMarimediaAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
                                 final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        // Get the host.
        host = config.getString("marimedia.host");

        // Check User Agent.
        if (StringUtils.isBlank(sasParams.getUserAgent())) {
            LOG.debug("Mandatory parameters missing for Marimedia so exiting adapter");
            LOG.info("Configure parameters inside Marimedia returned false");
            return false;
        }

        // Check App ID.
        // i.e. Check externalSiteId.
        if (StringUtils.isBlank(externalSiteId)) {
            LOG.debug("Mandatory parameters missing for Marimedia so exiting adapter");
            LOG.info("Configure parameters inside Marimedia returned false");
            return false;
        }

        // Check IP Address.
        if (StringUtils.isBlank(sasParams.getRemoteHostIp())) {
            LOG.debug("Mandatory parameters missing for Marimedia so exiting adapter");
            LOG.info("Configure parameters inside Marimedia returned false");
            return false;
        }

        // Check Resolution.
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null == slotSizeMapEntity) {
            LOG.debug("Mandatory parameters missing for Marimedia so exiting adapter");
            LOG.info("Configure parameters inside Marimedia returned false");
            return false;
        } else {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());

            resolution = width + "x" + height;
        }

        // Check Ad type.
        // Will be set in getRequestUri().
        // No video format supported, either "Interstitial" or "Banner".

        // Find the connection type.
        if (ConnectionType.WIFI == sasParams.getConnectionType()) {
            networkType = "wifi";
        } else {
            networkType = "carrier";
        }

        if (sasParams.getOsId() == HandSetOS.Android.getValue() && StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            //version value can  be 3.2.4 or 3.2
            String version = sasParams.getOsMajorVersion();
            int index = version.indexOf(".");
            String versionPart = version.substring(index+1);
            versionPart = versionPart.replace(".", "");

            if(Float.valueOf(version.substring(0,index+1)+ versionPart) < 2.1){
                LOG.info("Blocking traffic for Android version less than 2.1");
                return false;
            }
        }

        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder url = new StringBuilder(host);

        // Set User Agent.
        if (StringUtils.isNotBlank(sasParams.getUserAgent())) {
            appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        }

        // Set App ID.
        if (StringUtils.isNotBlank(externalSiteId)) {
            appendQueryParam(url, APP_ID, getURLEncode(externalSiteId, format), false);
        }

        // Set IP.
        if (StringUtils.isNotBlank(sasParams.getRemoteHostIp())) {
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
        if (StringUtils.isNotBlank(resolution)) {
            appendQueryParam(url, RESOLUTION, getURLEncode(resolution, format), false);
        }

        // Set Latitude & Longitude.
        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");

            appendQueryParam(url, LAT, getURLEncode(latlong[0], format), false);
            appendQueryParam(url, LONGITUDE, getURLEncode(latlong[1], format), false);
        }

        // Set Android ID.
        if (isAndroid()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.getUidMd5(), false);
                appendQueryParam(url, ANDROID_ID_MD5, casInternalRequestParameters.getUidMd5(), false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.getUid(), false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.getUidO1(), false);
                appendQueryParam(url, ANDROID_ID_SHA1, casInternalRequestParameters.getUidO1(), false);
            }

            final String gpId = getGPID(false);
            if (StringUtils.isNotBlank(gpId)) {
                appendQueryParam(url, ANDROID_ADVERTISING_ID, gpId, false);
            }
        }

        // Set IDFA and UDID.
        if (isIOS()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
                appendQueryParam(url, UDID, casInternalRequestParameters.getUidIDUS1(), false);
            }
            final String ifa = getUidIFA(false);
            if (StringUtils.isNotBlank(ifa)) {
                appendQueryParam(url, IDFA, ifa, false);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                appendQueryParam(url, UDID_MD5, casInternalRequestParameters.getUidMd5(), false);
            }
        }

        // Set Network Type.
        appendQueryParam(url, NETWORK_TYPE, networkType, false);

        // Set Age.
        if (null != sasParams.getAge()) {
            appendQueryParam(url, AGE, getURLEncode(sasParams.getAge().toString(), format), false);
        }

        // Set Gender.
        if (null != sasParams.getGender()) {
            appendQueryParam(url, GENDER, getURLEncode(sasParams.getGender(), format), false);
        }
        appendQueryParam(url, BLINDED_SITE_ID, blindedSiteId, false);

        LOG.debug("Marimedia url is {}", url);
        return new URI(url.toString());
    }

    // For POST requests.
    @Override
    protected RequestBuilder getNingRequestBuilder() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }
        return new RequestBuilder().setUrl(uri.toString())
                .setHeader("x-display-metrics", String.format("%sx%s", width, height))
                .setHeader("xplus1-user-agent", sasParams.getUserAgent())
                .setHeader("x-plus1-remote-addr", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us").setHeader(HttpHeaders.Names.REFERER, requestUrl)
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader(HttpHeaders.Names.HOST, uri.getHost())
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp());
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("Marimedia response is {}", response);

        if (isValidResponse(response, status)) {
            statusCode = status.code();
            final VelocityContext context = new VelocityContext();
            try {
                final JSONObject ad = new JSONObject(response);
                buildInmobiAdTracker();

                // Banner or Interstitial.
                if ("banner".equalsIgnoreCase(ad.getString("adType"))) {
                    final String imageUrl = ad.getString("imageUrl");
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, imageUrl);
                } else if ("html".equalsIgnoreCase(ad.getString("adType"))) {
                    final String htmlUrl = ad.getString("htmlUrl");
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, htmlUrl);
                } else {
                    // Other format.
                    // adType is "video" or "empty".
                    adStatus = NO_AD;
                    responseContent = null;
                    LOG.info("Error parsing response {} from Marimedia", response);
                    return;
                }

                // Ad URL.
                final String adUrl = ad.getString("adUrl");
                context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, adUrl);

                // Impression URL.
                if (ad.has("impUrl")) {
                    final String impressionUrl = ad.getString("impUrl");
                    context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, impressionUrl);
                }

                List<String> partnerBeacons = new ArrayList<>();
                if(ad.has("pixels")){
                    JSONArray pixelArray = ad.getJSONArray("pixels");
                    for(int i=0;i<pixelArray.length();i++){
                        partnerBeacons.add(pixelArray.getString(i));
                    }
                    context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_LIST, partnerBeacons);
                }
                context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());

                responseContent =
                        Formatter.getResponseFromTemplate(Formatter.TemplateType.IMAGE, context, sasParams,
                                getBeaconUrl());
                adStatus = AD_STRING;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from Marimedia: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }
    }

    @Override
    public String getName() {
        return "marimediaDCP";
    }

    @Override
    public String getId() {
        return config.getString("marimedia.advertiserId");
    }

}
