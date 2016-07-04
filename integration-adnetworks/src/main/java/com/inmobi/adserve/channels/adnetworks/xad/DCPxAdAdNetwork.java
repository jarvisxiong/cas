package com.inmobi.adserve.channels.adnetworks.xad;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.types.LocationSource;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DCPxAdAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPxAdAdNetwork.class);

    private static final String APP_ID_FORMAT = "%s_%s"; // <blinded_id>_<category>
    private static final String UUID_MD5 = "UUID|MD5";
    private static final String UUID_SHA1 = "UUID|SHA1";
    private static final String ANDROID_ID_MD5 = "Android_Id|MD5";
    private static final String ANDROID_ID_SHA1 = "Android_Id|SHA1";
    private static final String IDFA_PLAIN = "IDFA|RAW";
    private static final String GPID_PLAIN = "GIDFA|RAW";
    private static final String APP = "app";
    private static final String WEB = "web";


    private String latitude = null;
    private String longitude = null;
    private int width;
    private int height;
    private String deviceId;
    private String deviceIdType;
    private String sourceType;
    // private boolean isLocSourceDerived;

    public DCPxAdAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for xad so exiting adapter");
            LOG.info("Configure parameters inside xad returned false");
            return false;
        }
        host = config.getString("xad.host");

        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        } else {
            LOG.debug("mandate parameters missing for xAd, so returning from adapter");
            LOG.info("Configure parameters inside xad returned false");
            return false;
        }
        setDeviceIdandType();

        if (null != casInternalRequestParameters.getLatLong()
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            if (LocationSource.DERIVED_LAT_LON != sasParams.getLocationSource()) {
                final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
                latitude = latlong[0];
                longitude = latlong[1];
            }
        }
        sourceType =
                StringUtils.isBlank(sasParams.getSource()) || WAP.equalsIgnoreCase(sasParams.getSource()) ? WEB : APP;

        return true;
    }

    @Override
    public String getName() {
        return "xadDCP";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder(host);
            url.append("?v=1.2&o_fmt=html5&ip=").append(sasParams.getRemoteHostIp());
            url.append("&k=").append(externalSiteId);
            final String categories = getCategories(',', true, true);
            final String appId =
                    String.format(APP_ID_FORMAT, blindedSiteId, categories.split(",")[0].replace(' ', '_'));
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
            if (null != casInternalRequestParameters.getZipCode()) {
                url.append("&loc=").append(casInternalRequestParameters.getZipCode());
            }
            url.append("&uid=").append(deviceId);
            url.append("&uid_type=").append(getURLEncode(deviceIdType, format));
            if (IDFA_PLAIN.equals(deviceIdType)) {
                url.append("&uid_tr=").append(casInternalRequestParameters.isTrackingAllowed() ? 1 : 0);
            }
            url.append("&size=").append(width).append("x").append(height);
            if (sasParams.getCountryCode() != null) {
                url.append("&co=").append(sasParams.getCountryCode().toUpperCase());
            }
            for (final String cat : categories.split(",")) {
                url.append("&cat=").append(cat);
            }
            int osId = sasParams.getOsId() - 1;
            if (osId < 0) {
                osId = 0;
            }
            url.append("&os=").append(HandSetOS.values()[osId].toString());
            if (isInterstitial()) {
                url.append("&instl=1");
            } else {
                url.append("&instl=0");
            }
            url.append("&pt=").append(sourceType);


            final HashSet<String> bCatSet = new HashSet<String>();

            if (casInternalRequestParameters.getBlockedIabCategories() != null) {
                bCatSet.addAll(casInternalRequestParameters.getBlockedIabCategories());
            }

            if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
                bCatSet.addAll(IABCategoriesMap.getIABCategories(IABCategoriesMap.PERFORMANCE_BLOCK_CATEGORIES));
            } else {
                bCatSet.addAll(IABCategoriesMap.getIABCategories(IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES));
            }

            for (final String bCategory : bCatSet) {
                url.append("&bcat=").append(bCategory);
            }
            LOG.debug("xAd url is {}", url);

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
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            statusCode = status.code();
            final VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response.trim());
            buildInmobiAdTracker();

            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, getBeaconUrl());
                adStatus = AD_STRING;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from XAd: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return config.getString("xad.advertiserId");
    }

    /**
     * function returns the unique device id
     * 
     * @return
     */
    private void setDeviceIdandType() {
        if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
            final String ifa = getUidIFA(false);
            if (StringUtils.isNotBlank(ifa)) {
                deviceId = ifa;
                deviceIdType = IDFA_PLAIN;
                return;
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
                deviceId = casInternalRequestParameters.getUidIDUS1();
                deviceIdType = UUID_SHA1;
                return;
            }


        } else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
            String gpid = getGPID(true);
            if (StringUtils.isNotBlank(gpid)) {
                deviceId = gpid;
                deviceIdType = GPID_PLAIN;
                return;
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                deviceId = casInternalRequestParameters.getUidMd5();
                deviceIdType = ANDROID_ID_MD5;
                return;
            }

            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
                deviceId = casInternalRequestParameters.getUidO1();
                deviceIdType = ANDROID_ID_SHA1;
                return;
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
                deviceId = casInternalRequestParameters.getUid();
                deviceIdType = ANDROID_ID_MD5;
                return;
            }

        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidSO1())) {
            deviceId = casInternalRequestParameters.getUidSO1();
            deviceIdType = UUID_SHA1;
            return;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
            deviceId = casInternalRequestParameters.getUidO1();
            deviceIdType = UUID_SHA1;
            return;
        }
        deviceIdType = UUID_MD5;
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
            deviceId = casInternalRequestParameters.getUidMd5();
            return;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
            deviceId = casInternalRequestParameters.getUid();
            return;
        }
        deviceId = casInternalRequestParameters.getUuidFromUidCookie();
        if (StringUtils.isBlank(deviceId)) {
            deviceId = casInternalRequestParameters.getUidWC();
        }
        if (StringUtils.isEmpty(deviceId)) {
            LOG.debug("setting deviceid to null for xAd");
        }

        if (StringUtils.isBlank(deviceId)) {
            deviceId = DigestUtils.md5Hex(sasParams.getUserAgent() + sasParams.getRemoteHostIp());
        } else {
            deviceId = DigestUtils.md5Hex(deviceId);
        }
    }


}
