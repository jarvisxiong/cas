package com.inmobi.adserve.channels.adnetworks.xad;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
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


public class DCPxAdAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPxAdAdNetwork.class);

    private String              latitude         = null;
    private String              longitude        = null;
    private int                 width;
    private int                 height;
    private String              deviceId;
    private String              deviceIdType;
    private static final String DERIVED_LAT_LONG = "derived-lat-lon";
    private boolean             isLocSourceDerived;
    private static final String APP_ID_FORMAT    = "%s_%s";                                       // <blinded_id>_<category>
    private static final String UUID_MD5         = "uuid|md5";
    private static final String UUID_SHA1        = "uuid|sha1";
    private static final String ANDROID_ID_MD5   = "android_id|md5";
    private static final String ANDROID_ID_SHA1  = "android_id|sha1";
    private static final String IDFA_PLAIN       = "idfa|plain";

    public DCPxAdAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for xad so exiting adapter");
            return false;
        }
        host = config.getString("xad.host");

        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            LOG.debug("mandate parameters missing for xAd, so returning from adapter");
            return false;
        }
        setDeviceIdandType();

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            if (DERIVED_LAT_LONG.equalsIgnoreCase(sasParams.getLocSrc())) {
                isLocSourceDerived = true;
            }
            else {
                String[] latlong = casInternalRequestParameters.latLong.split(",");
                latitude = latlong[0];
                longitude = latlong[1];
            }
        }

        LOG.info("Configure parameters inside xad returned true");
        return true;
    }

    @Override
    public String getName() {
        return "xad";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder(host);
            url.append("?v=1.1&o_fmt=html5&ip=").append(sasParams.getRemoteHostIp());
            url.append("&k=").append(externalSiteId);
            String categories = getCategories(':');
            String appId = String.format(APP_ID_FORMAT, blindedSiteId, categories.split(":")[0].replace(' ', '_'));
            url.append("&appid=").append(appId);

            if (sasParams.getGender() != null) {
                url.append("&gender=").append(sasParams.getGender());
            }
            if (sasParams.getAge() != null) {
                url.append("&age=").append(sasParams.getAge());
            }
            url.append("&devid=").append(getURLEncode(sasParams.getUserAgent(), format));
            // if (!isLocSourceDerived) {
            if (!StringUtils.isEmpty(latitude)) {
                url.append("&lat=").append(latitude);
            }
            if (!StringUtils.isEmpty(longitude)) {
                url.append("&long=").append(longitude);
            }
            // }
            if (casInternalRequestParameters.zipCode != null) {
                url.append("&loc=").append(casInternalRequestParameters.zipCode);
            }
            url.append("&uid=").append(deviceId);
            url.append("&uid_type=").append(getURLEncode(deviceIdType, format));
            if (IDFA_PLAIN.equals(deviceIdType) && StringUtils.isNotBlank(casInternalRequestParameters.uidADT)) {
                url.append("&uid_tr=").append(casInternalRequestParameters.uidADT);
            }
            url.append("&size=").append(width).append("x").append(height);
            if (sasParams.getCountry() != null) {
                url.append("&co=").append(sasParams.getCountry().toUpperCase());
            }
            url.append("&category=").append(getURLEncode(categories, format));
            int osId = sasParams.getOsId() - 1;
            if (osId < 0) {
                osId = 0;
            }
            url.append("&os=").append(HandSetOS.values()[osId].toString());

            LOG.debug("xAd url is {}", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);

        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.getCode();
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from XAd : {}", exception);
                LOG.info("Response from XAd: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("xad.advertiserId"));
    }

    /**
     * function returns the unique device id
     * 
     * @return
     */
    private void setDeviceIdandType() {
        if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                deviceId = casInternalRequestParameters.uidIFA;
                deviceIdType = IDFA_PLAIN;
                return;
            }
        }
        else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                deviceId = casInternalRequestParameters.uidMd5;
                deviceIdType = ANDROID_ID_MD5;
                return;
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
                deviceId = casInternalRequestParameters.uidSO1;
                deviceIdType = ANDROID_ID_SHA1;
                return;
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
                deviceId = casInternalRequestParameters.uidO1;
                deviceIdType = ANDROID_ID_SHA1;
                return;
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
                deviceId = casInternalRequestParameters.uid;
                deviceIdType = ANDROID_ID_MD5;
                return;
            }

        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
            deviceId = casInternalRequestParameters.uidSO1;
            deviceIdType = UUID_SHA1;
            return;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            deviceId = casInternalRequestParameters.uidO1;
            deviceIdType = UUID_SHA1;
            return;
        }
        deviceIdType = UUID_MD5;
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
            deviceId = casInternalRequestParameters.uidMd5;
            return;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
            deviceId = casInternalRequestParameters.uid;
            return;
        }
        try {
            JSONObject userParams = new JSONObject(sasParams.getUidParams());
            deviceId = userParams.getString("imuc__5");
            if (StringUtils.isBlank(deviceId)) {
                deviceId = userParams.getString("WC");
            }
            if (StringUtils.isEmpty(deviceId)) {
                LOG.debug("setting deviceid to null for xAd");
            }

        }
        catch (Exception e) {
            LOG.debug("setting deviceid to null for xAd");
        }

        if (StringUtils.isBlank(deviceId)) {
            deviceId = DigestUtils.md5Hex(sasParams.getUserAgent() + sasParams.getRemoteHostIp());
        }
        else {
            deviceId = DigestUtils.md5Hex(deviceId);
        }
    }
}