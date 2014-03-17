package com.inmobi.adserve.channels.adnetworks.appier;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;


public class DCPAppierAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG          = LoggerFactory.getLogger(DCPAppierAdNetwork.class);

    private transient String    latitude;
    private transient String    longitude;
    private int                 width;
    private int                 height;
    private String              os           = null;
    private static final String sizeFormat   = "%dx%d";
    private String              sourceType;

    private static final String VERSION      = "version";
    private static final String REQID        = "reqid";
    private static final String SEGKEY       = "segkey";
    private static final String CLIENT_IP    = "clientip";
    private static final String B_SITE_ID    = "bsiteid";
    private static final String SITE_TYPE    = "sitetype";
    private static final String CATEGRORIES  = "categories";
    private static final String OS           = "os";
    private static final String IDFA_MD5     = "idfamd5";
    private static final String STD_ODIN1    = "stdodin1";
    private static final String INMOBI_ODIN1 = "inmobiodin1";
    private static final String UM5          = "um5";
    private static final String SITE_RATING  = "siterating";
    private static final String PERFORMANCE  = "Performance";
    private static final String FAMILY_SAFE  = "FAMILY_SAFE";

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPAppierAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for appier so exiting adapter");
            return false;
        }
        host = config.getString("appier.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long)sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        if (sasParams.getOsId() == HandSetOS.Android.getValue()) { // android
            os = "android";
        }
        else if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) { // iPhone
            os = "ios";
        }
        sourceType = (StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource())) ? "1"
                : "0";
        LOG.info("Configure parameters inside Appier returned true");
        return true;
    }

    @Override
    public String getName() {
        return "appier";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    protected Request getNingRequest() throws Exception {

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        byte[] body = getRequestParams().getBytes(CharsetUtil.UTF_8);
        return new RequestBuilder("POST").setURI(uri).setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .setHeader(HttpHeaders.Names.HOST, uri.getHost()).setBody(body).build();
    }

    @Override
    public URI getRequestUri() throws Exception {
        return new URI(host);
    }

    private String getRequestParams() throws Exception {

        StringBuilder url = new StringBuilder(VERSION).append("=1");
        appendQueryParam(url, REQID, impressionId, false);
        appendQueryParam(url, SEGKEY, externalSiteId, false);
        appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(url, B_SITE_ID, blindedSiteId, false);
        appendQueryParam(url, SITE_TYPE, sourceType, false);
        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        appendQueryParam(url, CATEGRORIES, getURLEncode(getCategories(','), format), false);
        if (os != null) {
            appendQueryParam(url, OS, os, false);
        }
        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
            appendQueryParam(url, LAT, latitude, false);
            appendQueryParam(url, LONG, longitude, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.zipCode)) {
            appendQueryParam(url, ZIP, casInternalRequestParameters.zipCode, false);
        }
        if (null != sasParams.getCountryCode()) {
            appendQueryParam(url, COUNTRY, sasParams.getCountryCode().toUpperCase(), false);
        }
        if (StringUtils.isNotBlank(sasParams.getGender())) {
            appendQueryParam(url, GENDER, sasParams.getGender(), false);
        }
        if (width != 0 && height != 0) {
            appendQueryParam(url, SIZE, String.format(sizeFormat, width, height), false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
            appendQueryParam(url, IDFA_MD5, getHashedValue(casInternalRequestParameters.uidIFA, "MD5"), false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
            appendQueryParam(url, STD_ODIN1, casInternalRequestParameters.uidSO1, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            appendQueryParam(url, INMOBI_ODIN1, casInternalRequestParameters.uidO1, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
            appendQueryParam(url, UM5, casInternalRequestParameters.uidMd5, false);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
            appendQueryParam(url, UM5, casInternalRequestParameters.uid, false);
        }
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            appendQueryParam(url, SITE_RATING, PERFORMANCE, false);
        }
        else {
            appendQueryParam(url, SITE_RATING, FAMILY_SAFE, false);
        }

        LOG.debug("Appier url is {}", url);

        return url.toString();
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        statusCode = status.getCode();
        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
            if (200 == statusCode || 204 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else if (statusCode == 204) {
            statusCode = 500;
            adStatus = "NO_AD";
            return;
        }
        else {
            try {
                JSONObject adResponse = new JSONObject(response);
                statusCode = status.getCode();
                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("click_landing"));
                String partnerBeacon = adResponse.getString("imp_beacon");
                if (StringUtils.isNotBlank(partnerBeacon) && !"null".equalsIgnoreCase(partnerBeacon)) {
                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, partnerBeacon);
                }
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                TemplateType t;
                String adType = adResponse.getString("type");
                if ("txt".equalsIgnoreCase(adType)) {
                    context.put(VelocityTemplateFieldConstants.AdText, adResponse.getString("text"));
                    String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
                    if (!StringUtils.isEmpty(vmTemplate)) {
                        context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                        t = TemplateType.RICH;
                    }
                    else {
                        t = TemplateType.PLAIN;
                    }
                }
                else {
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("banner_url"));
                    t = TemplateType.IMAGE;
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.error("Error parsing response from Appier : {}", exception);
                LOG.error("Response from Appier: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("appier.advertiserId"));
    }

}
