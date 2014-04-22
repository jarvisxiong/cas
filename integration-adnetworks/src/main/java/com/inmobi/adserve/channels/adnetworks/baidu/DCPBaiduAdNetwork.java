package com.inmobi.adserve.channels.adnetworks.baidu;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.util.Calendar;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPBaiduAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG          = LoggerFactory.getLogger(DCPBaiduAdNetwork.class);

    private transient String    latitude;
    private transient String    longitude;
    private int                 width;
    private int                 height;
    private String              uid;
    private String              os;

    private static final String APP_ID       = "appid";
    private static final String WIDTH        = "w";
    private static final String HEIGHT       = "h";
    private static final String IP           = "ip";
    private static final String IMP_T_URL    = "impt";
    private static final String CLK_T_URL    = "clkt";
    private static final String GPS_LOCATION = "g";
    private static final String SN           = "sn";
    private static final String LP_ACT_VALUE = "LP,PH,DL,MAP,SMS,MAI,VD,RM";
    private static final String LP_ACT_TYPE  = "act";
    private static final String GEO_TEMPLATE = "%s_%s_%s";
    private static final String Q_FORMAT     = "%s_cpr";
    private static final String Q_APPID      = "q";
    private static final String OS           = "os";
    private static final String ANDROID      = "android";
    private static final String IOS          = "iOS";
    private static final String SYMBIAN      = "symbian";
    private static final String WEB          = "web";

    public DCPBaiduAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        host = config.getString("baidu.host");
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for baidu so exiting adapter");
            return false;
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            // Baidu wanted in that format
            height = (int) Math.ceil(dim.getWidth());
            width = (int) Math.ceil(dim.getHeight());
        }
        else {
            LOG.debug("mandate parameters missing for Baidu, so returning from adapter");
            return false;
        }
        uid = getUid();

        if (StringUtils.isBlank(uid)) {
            LOG.debug("mandatory parameters missing for baidu so exiting adapter");
            return false;

        }

        if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
            os = IOS;
        }
        else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
            os = ANDROID;
        }
        else if (sasParams.getOsId() == HandSetOS.Symbian_OS.getValue()) {
            os = SYMBIAN;
        }
        else {
            os = WEB;
        }
        LOG.info("Configure parameters inside baidu returned true");
        return true;
    }

    @Override
    public String getName() {
        return "baidu";
    }

    @Override
    public URI getRequestUri() throws Exception {

        StringBuilder url = new StringBuilder(host);

        appendQueryParam(url, APP_ID, externalSiteId, false);
        appendQueryParam(url, OS, os, false);
        appendQueryParam(url, WIDTH, width + "", false);
        appendQueryParam(url, HEIGHT, height + "", false);
        appendQueryParam(url, IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(url, IMP_T_URL, getURLEncode(beaconUrl, format), false);
        appendQueryParam(url, CLK_T_URL, getURLEncode(getClickUrl(), format), false);
        appendQueryParam(url, SN, uid, false);
        appendQueryParam(url, Q_APPID, String.format(Q_FORMAT, externalSiteId), false);
        appendQueryParam(url, LP_ACT_TYPE, getURLEncode(LP_ACT_VALUE, format), false);

        if (StringUtils.isNotEmpty(latitude) && StringUtils.isNotEmpty(longitude)) {

            String geo = String.format(GEO_TEMPLATE, String.valueOf(Calendar.getInstance().getTimeInMillis()),
                    longitude, latitude);
            appendQueryParam(url, GPS_LOCATION, geo, false);
        }

        LOG.debug("baidu url is {}", url);

        return new URI(url.toString());
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {} and response length is {}", response, response.length());
        if (status.code() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.code();
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());

            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, null);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from baidu : {}", exception);
                LOG.info("Response from baidu: {}", response);
                return;
            }
            adStatus = "AD";
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("baidu.advertiserId"));
    }
}