package com.inmobi.adserve.channels.adnetworks.adsmogo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URLDecoder;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

public class DCPAdsMogoAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory
            .getLogger(DCPAdsMogoAdnetwork.class);

    private transient String latitude;
    private transient String longitude;
    private int width;
    private int height;

    private final String os = null;
    private String authSignature = null;
    private final String authKey;
    private final String authSecret;
    private static final String SIGNATURE_HEADER = "MOGO_API_SIGNATURE";
    private static final String AUTHKEY_HEADER = "MOGO_API_AUTHKEY";
    private static final String APPID = "aid";
    private static final String ADSPACE_TYPE = "ast";
    private static final String ADSPACE_WIDTH = "w";
    private static final String ADSPACE_HEIGHT = "h";
    private static final String IPADDRESS = "ip";
    private static final String USER_AGENT = "ua";
    private static final String IOS_ID = "ouid";
    private static final String ANDROID_ID = "anid";
    private static final String DEVICE_OS = "os";
    private static final String LAT = "lat";
    private static final String LONG = "lon";
    private static final String COUNTRY = "co";
    private static final String USER_GENDER = "GENDER";
    private static final String USER_AGE = "AGE";

    public DCPAdsMogoAdnetwork(final Configuration config,
            final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler,
            final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        host = config.getString("adsmogo.host");
        authKey = config.getString("adsmogo.authkey");
        authSecret = config.getString("adsmogo.authsecret");
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp())
                || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for AdsMogo so exiting adapter");
            return false;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(
                        casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }

        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams
                    .getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        } else {
            LOG.debug("mandatory parameters missing for AdsMogo so exiting adapter");
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "adsmogo";
    }

    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder url = new StringBuilder(host);
        appendQueryParam(url, APPID, externalSiteId, false);
        appendQueryParam(url, IPADDRESS, sasParams.getRemoteHostIp(), false);
        appendQueryParam(url, ADSPACE_TYPE, getAdType(), false);

        appendQueryParam(url, USER_AGENT,
                getURLEncode(sasParams.getUserAgent(), format), false);
        if (StringUtils.isNotEmpty(os)) {
            appendQueryParam(url, DEVICE_OS, os, false);
        }

        if (null != sasParams.getAge()) {
            appendQueryParam(url, USER_AGE, sasParams.getAge().toString(),
                    false);
        }

        if (StringUtils.isNotEmpty(latitude)
                && StringUtils.isNotEmpty(longitude)) {
            appendQueryParam(url, LAT, latitude, false);
            appendQueryParam(url, LONG, longitude, false);
        }

        if (StringUtils.isNotEmpty(sasParams.getCountryCode())) {
            appendQueryParam(url, COUNTRY, sasParams.getCountryCode()
                    .toUpperCase(), false);
        }
        appendQueryParam(url, ADSPACE_WIDTH, width, false);
        appendQueryParam(url, ADSPACE_HEIGHT, height, false);

        if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                appendQueryParam(url, IOS_ID,
                        casInternalRequestParameters.uidIFA, false);

            } else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
                appendQueryParam(url, IOS_ID, casInternalRequestParameters.uid,
                        false);
            }
        }

        if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                appendQueryParam(url, ANDROID_ID,
                        casInternalRequestParameters.uidMd5, false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
                appendQueryParam(url, ANDROID_ID,
                        casInternalRequestParameters.uid, false);
            } else if (StringUtils
                    .isNotBlank(casInternalRequestParameters.uidIDUS1)) {
                appendQueryParam(url, ANDROID_ID,
                        casInternalRequestParameters.uidIDUS1, false);
            }
        }
        String gen = sasParams.getGender();
        if (StringUtils.isNotEmpty(gen)) {
            if (gen.toLowerCase().startsWith("f")) {

                appendQueryParam(url, USER_GENDER, 1, false);

            } else if (gen.toLowerCase().startsWith("m")) {

                appendQueryParam(url, USER_GENDER, 0, false);

            }
        }
        LOG.debug("AdsMogo url is {}", url);
        URI requestUrl = new URI(url.toString());
        StringBuilder query = new StringBuilder(URLDecoder.decode(requestUrl.getQuery()))
                .append(authSecret);
        authSignature = getHashedValue(query.toString(), "MD5");

        return requestUrl;
    }

    private String getAdType() {
        Short slot = sasParams.getSlot();
        if (10 == slot // 300X250
                || 14 == slot // 320X480
                || 16 == slot) /* 768X1024 */{
            return "interstitial";
        }
        return "banner";
    }

    @Override
    public Request getNingRequest() throws Exception {

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        return new RequestBuilder()
                .setURI(uri)
                .setHeader(HttpHeaders.Names.USER_AGENT,
                        sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING,
                        HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                .setHeader(SIGNATURE_HEADER, authSignature)
                .setHeader(AUTHKEY_HEADER, authKey)
                .setHeader(HttpHeaders.Names.HOST, uri.getHost()).build();
    }

    @Override
    public void parseResponse(final String response,
            final HttpResponseStatus status) {
        if (StringUtils.isBlank(response) || status.code() != 200
                ) {
            statusCode = 500;
            responseContent = "";
            return;
        } else {
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode,
                    response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(
                        TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
                statusCode = 200;
            } catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from AdsMogo : {}", exception);
                LOG.info("Response from AdsMogo: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("adsmogo.advertiserId"));
    }
}
