package com.inmobi.adserve.channels.adnetworks.pubmatic;

import java.awt.Dimension;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.RequestBuilder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

public class DCPPubmaticAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPPubmaticAdNetwork.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String CREATIVE_TAG = "creative_tag";
    private static String TRACKING_URL = "tracking_url";
    private static String PUBMATIC_BID = "PubMatic_Bid";
    private static String ERROR_CODE = "error_code";
    private transient String pubId;
    private String latlong = null;
    private int width;
    private int height;
    private String deviceId;
    private String adId;
    private String nettype;
    private int udidtype;
    private int udidhash;
    private static final String CELLTOWER = "CELLTOWER";

    public DCPPubmaticAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for pubmatic so exiting adapter");
            LOG.info("Configure parameters inside pubmatic returned false");
            return false;
        }

        host = config.getString("pubmatic.host");
        pubId = config.getString("pubmatic.pubId");
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
            try {
                final JSONObject additionalParams = entity.getAdditionalParams();
                // ad id is configured as the additional param in the
                // segment table
                adId = additionalParams.getString(selectedSlotId.toString());

            } catch (final Exception e) {
                LOG.error("AdId is not configured for the segment:{}, exception raised {}",
                        entity.getExternalSiteKey(), e);
                LOG.info("Configure parameters inside pubmatic returned false");
                return false;
            }
        } else {
            LOG.debug("mandate parameters missing for pubmatic, so returning from adapter");
            LOG.info("Configure parameters inside pubmatic returned false");
            return false;
        }

        if (ConnectionType.WIFI == sasParams.getConnectionType()) {
            nettype = "wifi";
        } else {
            nettype = "carrier";
        }

        final String udid = getUid();
        if (!"wap".equalsIgnoreCase(sasParams.getSource()) && StringUtils.isBlank(udid)) { // deviceid mandatory for
                                                                                           // App traffic
            LOG.debug("mandate parameters missing for pubmatic, so returning from adapter");
            LOG.info("Configure parameters inside pubmatic returned false");
            return false;
        }

        if (casInternalRequestParameters.getLatLong() != null) {
            final String[] t = casInternalRequestParameters.getLatLong().split(",");
            if (t.length > 1) {
                latlong = String.format("%s,%s", t[0], t[1]);
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "pubmaticDCP";
    }

    public String getRequestParams() {
        final StringBuilder params =
                new StringBuilder(
                        "timezone=0&frameName=test&inIframe=1&adVisibility=0&adPosition=-1x-1&operId=201&pubId=");
        params.append(pubId);
        params.append("&adId=").append(adId);
        params.append("&siteId=").append(externalSiteId);

        if (sasParams.getGender() != null) {
            params.append("&gender=").append(sasParams.getGender());
        }

        final int yob = getYearofBirth();
        if (yob != -1) {
            params.append("&yob=").append(yob);
        }

        if (!StringUtils.isEmpty(latlong)) {
            params.append("&loc=").append(latlong);

            if (DERIVED_LAT_LON.equalsIgnoreCase(sasParams.getLocSrc())) {
                params.append("&loc_source=2");
            } else if (LATLON.equalsIgnoreCase(sasParams.getLocSrc())
                    || CELLTOWER.equalsIgnoreCase(sasParams.getLocSrc())) {
                params.append("&loc_source=1");
            } else {
                params.append("&loc_source=0");
            }

        }

        if (casInternalRequestParameters.getZipCode() != null) {
            params.append("&zip=").append(casInternalRequestParameters.getZipCode());
        }

        if (sasParams.getCountryCode() != null) {
            params.append("&country=").append(IABCountriesMap.getIabCountry(sasParams.getCountryCode()));
        }
        params.append("&nettype=").append(nettype);
        setDeviceIdandType();
        if (deviceId != null) {
            params.append("&udid=").append(deviceId);
            params.append("&udidtype=").append(udidtype);
            params.append("&udidhash=").append(udidhash);
        }
        params.append("&kadwidth=").append(width).append("&kadheight=").append(height);
        params.append("&pageURL=").append(blindedSiteId);
        params.append("&keywords=").append(getURLEncode(getCategories(','), format));
        final SimpleDateFormat dfm = new SimpleDateFormat(DATE_FORMAT);
        params.append("&kltstamp=").append(getURLEncode(dfm.format(Calendar.getInstance().getTime()), format));
        params.append("&ranreq=").append(Math.random());

        LOG.debug("pubmatic url is {}", params);

        return params.toString();
    }

    @Override
    protected RequestBuilder getNingRequestBuilder() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        final byte[] body = getRequestParams().getBytes(CharsetUtil.UTF_8);

        return new RequestBuilder("POST").setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .setHeader(HttpHeaders.Names.HOST, uri.getHost()).setBody(body)
                .setHeader("RLNClientIpAddr", sasParams.getRemoteHostIp());
    }

    @Override
    public URI getRequestUri() throws Exception {
        return new URI(host);
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
            String htmlCode = DEFAULT_EMPTY_STRING;
            String partnerBeacon = null;
            try {
                statusCode = status.code();
                JSONObject adResponse = new JSONObject(response.replace("\n", " "));
                adResponse = adResponse.getJSONObject(PUBMATIC_BID);
                if (adResponse.has(ERROR_CODE)) {
                    adStatus = NO_AD;
                    LOG.info("Error response from pubmatic: {}", response);
                    return;
                }
                htmlCode = adResponse.getString(CREATIVE_TAG).trim();
                partnerBeacon = adResponse.getString(TRACKING_URL);
            } catch (final JSONException exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from pubmatic: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from pubmatic: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }

            final VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, partnerBeacon);
            context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, htmlCode);
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = AD_STRING;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from pubmatic: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
        LOG.debug("response length is {}", responseContent);
    }

    @Override
    public String getId() {
        return config.getString("pubmatic.advertiserId");
    }

    private void setDeviceIdandType() {
        if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA())) {
                deviceId = casInternalRequestParameters.getUidIFA();
                udidtype = 1;
                udidhash = 1;
                return;
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
                deviceId = casInternalRequestParameters.getUidIDUS1();
                udidtype = 4;
                udidhash = 2;
                return;
            }

        } else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
            final String gpid = getGPID();
            if (StringUtils.isNotBlank(gpid)) {
                deviceId = gpid;
                udidtype = 9;
                udidhash = 1;
                return;
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
                deviceId = casInternalRequestParameters.getUidO1();
                udidtype = 8;
                udidhash = 2;
                return;
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
                deviceId = casInternalRequestParameters.getUid();

                udidtype = 5;
                udidhash = 3;
                return;
            }

        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidSO1())) {
            deviceId = casInternalRequestParameters.getUidSO1();
            udidtype = 8;
            udidhash = 2;
            return;
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
            deviceId = casInternalRequestParameters.getUidO1();
            udidtype = 8;
            udidhash = 2;
            return;
        }

    }

}
