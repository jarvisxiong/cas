package com.inmobi.adserve.channels.adnetworks.verve;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

public class DCPVerveAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPVerveAdNetwork.class);

    private transient String latitude;
    private transient String longitude;
    private int width;
    private int height;
    private String portalKeyword;
    private String adUnit;
    private static final String IPHONE_KEYWORD = "iphn";
    private static final String ANDROID_KEYWORD = "anap";
    private static final String WAP_KEYWORD = "ptnr";
    private static final String WAP = "wap";
    private static final String DERIVED_LAT_LONG = "DERIVED_LAT_LON";
    private static final String TRUE_LAT_LONG_ONLY = "trueLatLongOnly";
    private static final String MMA = "mma";
    private static final String BANNER = "banner";
    private static final String INTER = "inter";
    private boolean sendTrueLatLongOnly;

    public DCPVerveAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for verve so exiting adapter");
            LOG.info("Configure parameters inside verve returned false");
            return false;
        }
        host = config.getString("verve.host");

        try {
            // TRUE_LAT_LONG_ONLY is configured as the additional param in the
            // segment table
            sendTrueLatLongOnly = Boolean.parseBoolean(entity.getAdditionalParams().getString(TRUE_LAT_LONG_ONLY));
        } catch (final JSONException e) {
            sendTrueLatLongOnly = false;
            LOG.info("trueLatLong is not configured for the segment:{} {}, exception raised {}",
                    entity.getExternalSiteKey(), getName(), e);
        }

        if (sendTrueLatLongOnly) {
            if (DERIVED_LAT_LONG.equalsIgnoreCase(sasParams.getLocSrc())) {
                LOG.info("Configure parameters inside verve returned false");
                return false;
            } else if (casInternalRequestParameters.getLatLong() != null
                    && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
                final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
                latitude = latlong[0];
                longitude = latlong[1];
            } else {
                LOG.info("Configure parameters inside verve returned false");
                return false;
            }
            if (StringUtils.isBlank(latitude) || StringUtils.isBlank(longitude)) {
                LOG.info("Configure parameters inside verve returned false");
                return false;
            }
        } else if (!DERIVED_LAT_LONG.equalsIgnoreCase(sasParams.getLocSrc())
                && StringUtils.isNotBlank(sasParams.getLocSrc())) { // request
            // has true
            // lat-long
            LOG.info("Configure parameters inside verve returned false");
            return false;
        }
        adUnit = MMA;
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            final Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
            if (sasParams.getSlot() == 11) {
                adUnit = BANNER;
            } else if (sasParams.getSlot() == 10 || sasParams.getSlot() == 14) {
                adUnit = INTER;
            }
        }

        if (WAP.equalsIgnoreCase(sasParams.getSource())) {
            portalKeyword = WAP_KEYWORD;
        } else if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
            portalKeyword = IPHONE_KEYWORD;
        } else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
            if (StringUtils.isBlank(sasParams.getSdkVersion())
                    || sasParams.getSdkVersion().toLowerCase().startsWith("a35")) {
                LOG.info("Configure parameters inside verve returned false as Android Version is 3.5.*");
                return false;
            }
            portalKeyword = ANDROID_KEYWORD;
        } else {
            LOG.info("Configure parameters inside verve returned false as unsupported source: {}", sasParams.getSource());
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "verve";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder();
            url.append(host).append("?ip=").append(sasParams.getRemoteHostIp());
            url.append("&p=").append(portalKeyword);
            url.append("&b=").append(externalSiteId);
            url.append("&site=").append(blindedSiteId);
            if (!StringUtils.isEmpty(sasParams.getGender())) {
                url.append("&ei=gender=").append(sasParams.getGender().toLowerCase());
            }
            if (null != sasParams.getAge()) {
                url.append(";age=").append(sasParams.getAge());
            }
            url.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
            if (sendTrueLatLongOnly) {
                url.append("&lat=").append(latitude);
                url.append("&long=").append(longitude);
            }

            final String casUidMd5 = casInternalRequestParameters.getUidMd5();
            final String casUid = casInternalRequestParameters.getUid();

            if (!"wap".equalsIgnoreCase(sasParams.getSource())) {
                if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
                    if (casInternalRequestParameters.getUidIFA() != null) {
                        url.append("&uis=a&ui=").append(casInternalRequestParameters.getUidIFA());
                    } else if (casInternalRequestParameters.getUidSO1() != null) {
                        url.append("&uis=us&ui=").append(casInternalRequestParameters.getUidSO1());
                    } else if (casInternalRequestParameters.getUidO1() != null) {
                        url.append("&uis=us&ui=").append(casInternalRequestParameters.getUidO1());
                    } else if (casUidMd5 != null) {
                        url.append("&uis=u&ui=").append(casUidMd5);
                    } else if (casInternalRequestParameters.getUidIDUS1() != null) {
                        url.append("&uis=ds&ui=").append(casInternalRequestParameters.getUidIDUS1());
                    } else if (!StringUtils.isBlank(casUid) && !"null".equals(casUid)) {
                        url.append("&uis=v&ui=").append(casUid);
                    }
                } else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
                    if (casUidMd5 != null) {
                        url.append("&uis=dm&ui=").append(casUidMd5);
                    } else if (!StringUtils.isBlank(casUid) && !"null".equals(casUid)) {
                        url.append("&uis=v&ui=").append(casUid);
                    } else {
                        final String gpid = getGPID();
                        if (gpid != null) {
                            url.append("&g=").append(gpid);
                        }
                    }
                }
            }

            if (casInternalRequestParameters.getZipCode() != null) {
                url.append("&z=").append(casInternalRequestParameters.getZipCode());
            }

            url.append("&c=97");// get category map

            if (width != 0 && height != 0) {
                url.append("&size=").append(width).append('x').append(height);
                url.append("&adunit=").append(adUnit);
            }

            LOG.debug("Verve url is {}", url);
            return new URI(url.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
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
        } else {
            statusCode = status.code();
            final VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.IM_BEACON_URL, beaconUrl);
            context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            } catch (final Exception exception) {
                adStatus = "NO_AD";
                LOG.error("Error parsing response {} from verve: {}", response, exception);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return config.getString("verve.advertiserId");
    }
}
