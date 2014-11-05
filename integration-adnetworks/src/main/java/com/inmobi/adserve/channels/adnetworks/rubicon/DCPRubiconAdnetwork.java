package com.inmobi.adserve.channels.adnetworks.rubicon;

import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

public class DCPRubiconAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPRubiconAdnetwork.class);


    private static final String APP_BUNDLE = "app.bundle";
    private static final String SITE_ID = "site_id";
    private static final String ZONE_ID = "zone_id";
    private static final String SIZE_ID = "size_id";
    private static final String UA = "ua";
    private static final String CLIENT_IP = "ip";
    private static final String DEVICE_OS = "device.os";
    private static final String OS_VERSION = "device.osv";
    private static final String DEVICE_ID = "device.dpid";
    private static final String SHA1_DEVICE_ID = "device.dpidsha1";
    private static final String MD5_DEVICE_ID = "device.dpidmd5";
    private static final String DEVICE_ID_TYPE = "device.dpid_type";
    private static final String CONNECTION_TYPE = "device.connectiontype";
    private static final String LAT = "geo.latitude";
    private static final String LONG = "geo.longitude";
    private static final String AD_SENSITIVE = "i.aq_sensitivity";
    private static final String KEYWORDS = "kw";
    private static final String FLOOR_PRICE = "rp_floor";
    private static final String APP_RATING = "app.rating";
    private static final String DISPLAY_TYPE = "display";
    private static final String IAB_CATEGORY = "i.iab";
    private static final String INMOBI_CATEGORY = "i.category";
    private static final String RESPONSE_TEMPLATE = "<script>%s</script>";
    private static final String BUNDLE_ID_TEMPLATE = "com.inmobi-exchange.%s";
    private static final String DOMAIN_NAME = "app.domain";
    private static final String APPSTORE_CATEGORY = "app.category";
    private static final String BLOCKLIST_PARAM = "p_block_keys";

    private static final String DEFAULT_ZONE = "default";
    private static final String SENSITIVITY_LOW = "low";
    private static final String SENSITIVITY_HIGH = "high";
    private static final String SITE_KEY_ADDL_PARAM = "site";
    private static final String FS_RATING = "4+";
    private static final String PERFORMANCE_RATING = "9+";
    private static final String DOMAIN = "com.inmobi-exchange";
    private static final String IDFA = "idfa";
    private static final String GPID = "gaid";
    private static final String OPEN_UDID = "open-udid";
    private static final String UDID = "udid";
    private static final String ACCEPT_APIS = "accept.apis";

    // The following BLOCKLIST_IDs have been registered with Rubicon.
    private static final String RUBICON_FS_BLOCKLIST_ID = "InMobiFS";
    private static final String RUBICON_PERF_BLOCKLIST_ID = "InMobiPERF";

    private static final double MIN_ECPM = 0.1;

    private static final String SITE_BLOCKLIST_FORMAT = "blk%s";
    private static final String SITE_FLOOR_KEF_FORMAT = "%s_%d";

    private static Map<String, Double> siteFloorMap;
    private static Map<Short, Integer> slotIdMap;

    private String latitude;
    private String longitude;
    private String zoneId;
    private String siteId;

    private final String userName;
    private final String password;
    private final double ECPM_PERCENTAGE;

    private boolean isApp;

    static {
        slotIdMap = new HashMap<Short, Integer>();
        slotIdMap.put((short) 4, 44);
        // Mapping 320x48 to 320x50
        slotIdMap.put((short) 9, 43);
        slotIdMap.put((short) 10, 15);
        slotIdMap.put((short) 11, 2);
        slotIdMap.put((short) 12, 1);
        slotIdMap.put((short) 13, 8);
        slotIdMap.put((short) 14, 67);
        slotIdMap.put((short) 15, 43);
        slotIdMap.put((short) 16, 102);
        slotIdMap.put((short) 18, 9);
        slotIdMap.put((short) 19, 50);
        slotIdMap.put((short) 21, 45);
        slotIdMap.put((short) 23, 46);
        slotIdMap.put((short) 29, 14);
        slotIdMap.put((short) 32, 101);

        siteFloorMap = new HashMap<String, Double>();
        siteFloorMap.put("1387380247996547_94", 1.1d);
        siteFloorMap.put("1387380247996547_0", 0.27d);
        siteFloorMap.put("1399614102253772_44", 19d);
        siteFloorMap.put("1399614102253772_0", 13d);
        siteFloorMap.put("1397202244813823_0", 1d);
        siteFloorMap.put("1399614063068988_44", 13d);
        siteFloorMap.put("1399614063068988_0", 11.5d);
        siteFloorMap.put("1397121033459365_0", 0.4d);
    }

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPRubiconAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        userName = config.getString("rubicon.username");
        password = config.getString("rubicon.password");
        ECPM_PERCENTAGE = config.getDouble("rubicon.eCPMPercentage");
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for rubicon so exiting adapter");
            LOG.info("Configure parameters inside Rubicon returned false");
            return false;
        }
        host = config.getString("rubicon.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            if (!slotIdMap.containsKey(sasParams.getSlot())) {
                LOG.debug("Size not allowed for rubicon so exiting adapter");
                LOG.info("Configure parameters inside Rubicon returned false");
                return false;
            }
        } else {
            LOG.debug("mandatory parameter size missing for rubicon so exiting adapter");
            LOG.info("Configure parameters inside Rubicon returned false");
            return false;
        }

        isApp = StringUtils.isBlank(sasParams.getSource()) || WAP.equalsIgnoreCase(sasParams.getSource())
                        ? false
                        : true;

        final JSONObject additionalParams = entity.getAdditionalParams();

        try {
            siteId = additionalParams.getString(SITE_KEY_ADDL_PARAM);
        } catch (final JSONException e) {
            LOG.debug("Site Id is not configured in rubicon so exiting adapter, raised exception {}", e);
            LOG.info("Configure parameters inside Rubicon returned false");
            return false;
        }
        zoneId = getZoneId(additionalParams);
        if (null == zoneId) {
            LOG.debug("Zone Id is not configured in rubicon so exiting adapter");
            LOG.info("Configure parameters inside Rubicon returned false");
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "rubicon";
    }

    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder url = new StringBuilder(host);
        appendQueryParam(url, ZONE_ID, zoneId, false);
        if (isApp) {
            appendQueryParam(url, APP_BUNDLE, String.format(BUNDLE_ID_TEMPLATE, sasParams.getSiteIncId()), false);
            appendQueryParam(url, DOMAIN_NAME, DOMAIN, false);
        }
        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(url, SITE_ID, siteId, false);
        final Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            appendQueryParam(url, DEVICE_OS, HandSetOS.values()[sasParamsOsId - 1].toString(), false);
        }
        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            appendQueryParam(url, OS_VERSION, sasParams.getOsMajorVersion(), false);
        }
        appendQueryParam(url, SIZE_ID, slotIdMap.get(sasParams.getSlot()), false);
        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
            appendQueryParam(url, LAT, latitude, false);
            appendQueryParam(url, LONG, longitude, false);
        }
        if (NetworkType.WIFI == sasParams.getNetworkType()) {
            appendQueryParam(url, CONNECTION_TYPE, 2, false);
        } else {
            appendQueryParam(url, CONNECTION_TYPE, 0, false);
        }
        final String uacContentRating = getUACContentRating(url);
        final List<String> blockedList = Lists.newArrayList();
        blockedList.add(String.format(SITE_BLOCKLIST_FORMAT, sasParams.getSiteIncId()));
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            appendQueryParam(url, AD_SENSITIVE, SENSITIVITY_LOW, false);
            blockedList.add(RUBICON_PERF_BLOCKLIST_ID);
            if (isApp) {
                final String rating = uacContentRating == null ? PERFORMANCE_RATING : uacContentRating;
                appendQueryParam(url, APP_RATING, rating, false);
            }
        } else {
            appendQueryParam(url, AD_SENSITIVE, SENSITIVITY_HIGH, false);
            blockedList.add(RUBICON_FS_BLOCKLIST_ID);
            if (isApp) {
                final String rating = uacContentRating == null ? FS_RATING : uacContentRating;
                appendQueryParam(url, APP_RATING, rating, false);
            }
        }

        if (StringUtils.isNotBlank(sasParams.getSdkVersion())) {
            final int version = Integer.parseInt(sasParams.getSdkVersion().substring(1));
            // 5 for MRAID-2 and 3 for MRAID-1
            if (version >= 400) {
                appendQueryParam(url, ACCEPT_APIS, 5, false);
            } else {
                appendQueryParam(url, ACCEPT_APIS, 3, false);
            }

        }
        appendQueryParam(url, BLOCKLIST_PARAM, getURLEncode(StringUtils.join(blockedList, ','), format), false);

        final Double siteSpecificFloorDefault =
                siteFloorMap.get(String.format(SITE_FLOOR_KEF_FORMAT, sasParams.getSiteIncId(), 0));


        if (siteSpecificFloorDefault == null) {
            if (sasParams.getSiteEcpmEntity() != null && sasParams.getSiteEcpmEntity().getEcpm() > 0) {
                appendQueryParam(url, FLOOR_PRICE, ECPM_PERCENTAGE * sasParams.getSiteEcpmEntity().getEcpm(), false);
            } else if (sasParams.getSiteEcpmEntity() != null && sasParams.getSiteEcpmEntity().getNetworkEcpm() > 0) {
                appendQueryParam(url, FLOOR_PRICE, ECPM_PERCENTAGE * sasParams.getSiteEcpmEntity().getNetworkEcpm(),
                        false);
            } else if (casInternalRequestParameters.getAuctionBidFloor() > 0) {
                appendQueryParam(url, FLOOR_PRICE, casInternalRequestParameters.getAuctionBidFloor(), false);
            } else {
                appendQueryParam(url, FLOOR_PRICE, MIN_ECPM, false);
            }
        } else {
            final Double siteSpecificFloorPerCountry =
                    siteFloorMap.get(String.format(SITE_FLOOR_KEF_FORMAT, sasParams.getSiteIncId(),
                            sasParams.getCountryId()));
            final double floor =
                    siteSpecificFloorPerCountry == null ? siteSpecificFloorDefault : siteSpecificFloorPerCountry;
            appendQueryParam(url, FLOOR_PRICE, floor, false);
        }
        if (isInterstitial()) {
            // display type 1 for interstitial
            appendQueryParam(url, DISPLAY_TYPE, 1, false);
        }
        appendQueryParam(url, INMOBI_CATEGORY, getURLEncode(getCategories(',', false, false), format), false);
        appendQueryParam(url, IAB_CATEGORY, getURLEncode(getCategories(',', true, true), format), false);
        appendDeviceIds(url);

        appendQueryParam(url, KEYWORDS, externalSiteId, false);
        LOG.debug("Rubicon url is {}", url);
        return new URI(url.toString());
    }


    @Override
    public Request getNingRequest() throws Exception {

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }
        final String authStr = userName + ":" + password;
        final String authEncoded = new String(Base64.encodeBase64(authStr.getBytes()));
        return new RequestBuilder().setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                .setHeader("Authorization", "Basic " + authEncoded).setHeader(HttpHeaders.Names.HOST, uri.getHost())
                .build();

    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug(" Rubicon response is {}", response);

        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        } else {
            statusCode = status.code();
            final VelocityContext context = new VelocityContext();
            try {
                final JSONObject adResponse = new JSONObject(response);
                if ("ok".equalsIgnoreCase(adResponse.getString("status"))) {
                    final JSONObject ad = adResponse.getJSONArray("ads").getJSONObject(0);

                    if (ad.has("impression_url")) {
                        final String partnerBeacon = ad.getString("impression_url");
                        context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, partnerBeacon);
                    }
                    final String htmlContent = ad.has("script") ? ad.getString("script") : null;
                    if (StringUtils.isBlank(htmlContent)) {
                        adStatus = "NO_AD";
                        statusCode = 204;
                        responseContent = "";
                        return;
                    }
                    context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE,
                            String.format(RESPONSE_TEMPLATE, htmlContent));
                    TemplateType templateType = TemplateType.HTML;
                    if (!isApp) {
                        templateType = TemplateType.WAP_HTML_JS_AD_TAG;
                    }

                    responseContent = Formatter.getResponseFromTemplate(templateType, context, sasParams, beaconUrl);
                    adStatus = "AD";
                } else {
                    adStatus = "NO_AD";
                    return;
                }
            } catch (final Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response {} from Rubicon: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }
    }

    @Override
    public String getId() {
        return config.getString("rubicon.advertiserId");
    }

    public String getZoneId(final JSONObject additionalParams) {
        String categoryZoneId = null;
        try {
            if (sasParams.getCategories() != null) {
                for (int index = 0; index < sasParams.getCategories().size(); index++) {
                    final String categoryIdKey = sasParams.getCategories().get(index).toString();
                    if (additionalParams.has(categoryIdKey)) {
                        categoryZoneId = additionalParams.getString(categoryIdKey);
                        LOG.debug("category Id is {}", categoryZoneId);
                    }
                    if (categoryZoneId != null) {
                        return categoryZoneId;
                    }
                }
            }
            if (additionalParams.has(DEFAULT_ZONE)) {
                categoryZoneId = additionalParams.getString(DEFAULT_ZONE);
            }

        } catch (final JSONException exception) {
            LOG.info("Unable to get zone_id for Rubicon, raised exception {}", exception);
        }
        return categoryZoneId;
    }

    private String getUACContentRating(final StringBuilder url) {
        String contentRating = null;
        if (sasParams.getWapSiteUACEntity() != null) {
            final List<String> appstoreCategories = sasParams.getWapSiteUACEntity().getCategories();
            if (appstoreCategories != null && !appstoreCategories.isEmpty()) {
                appendQueryParam(url, APPSTORE_CATEGORY,
                        getURLEncode(StringUtils.join(appstoreCategories, ','), format), false);
            }
            contentRating = sasParams.getWapSiteUACEntity().getContentRating();
        }
        return contentRating;
    }

    private void appendDeviceIds(final StringBuilder url) {
        // Device id type 1 (IDFA), 2 (OpenUDID), 3 (Apple UDID), 4 (Android
        // device ID)

        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA())
                && "1".equals(casInternalRequestParameters.getUidADT())) {
            appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.getUidIFA(), false);
            appendQueryParam(url, DEVICE_ID_TYPE, IDFA, false);
        } else {
            final String gpid = getGPID();
            if (null != gpid) {
                appendQueryParam(url, DEVICE_ID, gpid, false);
                appendQueryParam(url, DEVICE_ID_TYPE, GPID, false);

            } else {
                boolean isUdid = false;
                if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
                    appendQueryParam(url, MD5_DEVICE_ID, casInternalRequestParameters.getUidMd5(), false);
                    isUdid = true;
                }

                if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
                    appendQueryParam(url, SHA1_DEVICE_ID, casInternalRequestParameters.getUidIDUS1(), false);
                    isUdid = true;
                } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
                    appendQueryParam(url, SHA1_DEVICE_ID, casInternalRequestParameters.getUidO1(), false);
                    isUdid = true;
                }

                if (isUdid) {
                    appendQueryParam(url, DEVICE_ID_TYPE, UDID, false);
                } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
                    appendQueryParam(url, MD5_DEVICE_ID, casInternalRequestParameters.getUid(), false);
                    appendQueryParam(url, DEVICE_ID_TYPE, OPEN_UDID, false);
                }
            }

        }
    }


}
