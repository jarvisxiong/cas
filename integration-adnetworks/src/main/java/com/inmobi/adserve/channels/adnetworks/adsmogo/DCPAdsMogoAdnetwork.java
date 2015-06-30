package com.inmobi.adserve.channels.adnetworks.adsmogo;

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
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.ning.http.client.RequestBuilder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DCPAdsMogoAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPAdsMogoAdnetwork.class);
    private static final String SIGNATURE_HEADER = "MOGO_API_SIGNATURE";
    private static final String AUTHKEY_HEADER = "MOGO_API_AUTHKEY";
    private static final String APPID = "aid";
    private static final String ADSPACE_TYPE = "ast";
    private static final String DEVICE_TYPE = "p";
    private static final String ADSPACE_WIDTH = "w";
    private static final String ADSPACE_HEIGHT = "h";
    private static final String IOS_OPEN_UDID = "ouid";
    private static final String ANDROID_ID = "anid";
    private static final String IDFA = "ida";
    private static final String IOS_ID = "ouid";
    private static final String DEVICE_OS = "os";
    private static final String LON = "lon";
    private static final String COUNTRY = "co";
    private static final String USER_GENDER = "GENDER";
    private static final String USER_AGE = "AGE";
    private static final String IPAD_UA = "ipad";
    private static final int INTERSTITIAL = 2;
    private static final int BANNER = 1;
    private static final int IPHONE = 1;
    private static final int ANDROID = 2;
    private static final int IPAD = 3;

    private transient String latitude;
    private transient String longitude;
    private int width;
    private int height;

    private String os = null;
    private String authSignature = null;
    private final String authKey;
    private final String authSecret;


    private boolean isApp;

    public DCPAdsMogoAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        host = config.getString("adsmogo.host");
        authKey = config.getString("adsmogo.authkey");
        authSecret = config.getString("adsmogo.authsecret");
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for AdsMogo so exiting adapter");
            return false;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        } else {
            LOG.debug("mandatory parameters missing for AdsMogo so exiting adapter");
            return false;
        }
        isApp = StringUtils.isBlank(sasParams.getSource()) || WAP.equalsIgnoreCase(sasParams.getSource())
                        ? false
                        : true;

        final Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            os = HandSetOS.values()[sasParamsOsId - 1].toString();
        }

        if (isApp && StringUtils.isEmpty(getUid(false))) {
            LOG.debug("mandatory parameter udid is missing for APP traffic in AdsMogo so exiting adapter");
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "adsmogoDCP";
    }

    @SuppressWarnings("deprecation")
    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder url = new StringBuilder(host);
        appendQueryParam(url, APPID, blindedSiteId, false);
        appendQueryParam(url, IP, sasParams.getRemoteHostIp(), false);
        if (isInterstitial()) {
            appendQueryParam(url, ADSPACE_TYPE, INTERSTITIAL, false);
        } else {
            appendQueryParam(url, ADSPACE_TYPE, BANNER, false);
        }
        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        if (StringUtils.isNotEmpty(os)) {
            appendQueryParam(url, DEVICE_OS, os, false);
        }

        if (null != sasParams.getAge()) {
            appendQueryParam(url, USER_AGE, sasParams.getAge().toString(), false);
        }

        if (StringUtils.isNotEmpty(latitude) && StringUtils.isNotEmpty(longitude)) {
            appendQueryParam(url, LAT, latitude, false);
            appendQueryParam(url, LON, longitude, false);
        }

        if (StringUtils.isNotEmpty(sasParams.getCountryCode())) {
            appendQueryParam(url, COUNTRY, sasParams.getCountryCode().toUpperCase(), false);
        }
        appendQueryParam(url, ADSPACE_WIDTH, width, false);
        appendQueryParam(url, ADSPACE_HEIGHT, height, false);

        if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
            final String ifa = getUidIFA(false);
            if (StringUtils.isNotBlank(ifa)) {
                appendQueryParam(url, IDFA, ifa, false);

            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
                appendQueryParam(url, IOS_OPEN_UDID, casInternalRequestParameters.getUid(), false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
                appendQueryParam(url, IOS_ID, casInternalRequestParameters.getUidIDUS1(), false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                appendQueryParam(url, IOS_ID, casInternalRequestParameters.getUidMd5(), false);
            }
            if(sasParams.getUserAgent().toLowerCase().contains(IPAD_UA)){
                appendQueryParam(url,DEVICE_TYPE,IPAD,false);
            }else{
                appendQueryParam(url,DEVICE_TYPE,IPHONE,false);
            }
        }

        if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.getUidMd5(), false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
                appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.getUid(), false);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
                appendQueryParam(url, ANDROID_ID, getURLEncode(casInternalRequestParameters.getUidO1(), format), false);
            }
            appendQueryParam(url,DEVICE_TYPE, ANDROID,false);
        }
        final String gen = sasParams.getGender();
        if (StringUtils.isNotEmpty(gen)) {
            if (gen.toLowerCase().startsWith("f")) {

                appendQueryParam(url, USER_GENDER, 1, false);

            } else if (gen.toLowerCase().startsWith("m")) {

                appendQueryParam(url, USER_GENDER, 0, false);

            }
        }
        LOG.debug("AdsMogo url is {}", url);
        final URI requestUrl = new URI(url.toString());
        final StringBuilder query = new StringBuilder(URLDecoder.decode(requestUrl.getQuery())).append(authSecret);
        authSignature = getHashedValue(query.toString(), GlobalConstant.MD5);

        return requestUrl;

    }


    @Override
    public RequestBuilder getNingRequestBuilder() throws Exception {

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        return new RequestBuilder().setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp()).setHeader(SIGNATURE_HEADER, authSignature)
                .setHeader(AUTHKEY_HEADER, authKey).setHeader(HttpHeaders.Names.HOST, uri.getHost());
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        if (StringUtils.isBlank(response) || status.code() != 200) {
            statusCode = 500;
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            final VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response.trim());
            buildInmobiAdTracker();

            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams,
                        getBeaconUrl());
                adStatus = AD_STRING;
                statusCode = 200;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from AdsMogo: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return config.getString("adsmogo.advertiserId");
    }
    
}
