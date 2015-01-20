package com.inmobi.adserve.channels.adnetworks.lomark;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPLomarkAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPLomarkAdNetwork.class);

    private static final String NO_AD = "NO_AD";
    private static Map<Long, Integer> categoryMap = new HashMap<Long, Integer>();
    private static Map<Integer, Integer> carrierIdMap;
    private transient String key;
    private transient String secretKey;
    private transient String latitude;
    private transient String longitude;
    private int width;
    private int height;
    private int client;
    private int siteType;
    private String uuid;


    static {
        carrierIdMap = new HashMap<Integer, Integer>();
        // carrier ip map
        carrierIdMap.put(786, 3);
        carrierIdMap.put(787, 1);
        carrierIdMap.put(788, 2);
        // category ip map
        categoryMap.put(4L, 1);
        categoryMap.put(23L, 1);
        categoryMap.put(24L, 1);
        categoryMap.put(13L, 2);
        categoryMap.put(3L, 3);
        categoryMap.put(3L, 9);
        categoryMap.put(6L, 4);
        categoryMap.put(10L, 4);
        categoryMap.put(18L, 4);
        categoryMap.put(31L, 4);
        categoryMap.put(30L, 5);
        categoryMap.put(12L, 6);
        categoryMap.put(29L, 7);
        categoryMap.put(11L, 8);
        categoryMap.put(25L, 8);
        categoryMap.put(19L, 9);
        categoryMap.put(32L, 9);
        categoryMap.put(15L, 10);
        categoryMap.put(28L, 11);
    }

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPLomarkAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
                              final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for lomark so exiting adapter");
            LOG.info("Configure parameters inside lomark returned false");
            return false;
        }
        host = config.getString("lomark.host");
        key = config.getString("lomark.key");
        secretKey = config.getString("lomark.secretkey");

        if (casInternalRequestParameters.getLatLong() != null
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
        }

        uuid = getUid();
        client = 3;
        siteType = 2; // wap
        if (sasParams.getOsId() == HandSetOS.Android.getValue()) { // android
            client = 1;
            siteType = 1; // app
        } else if (sasParams.getOsId() == HandSetOS.iOS.getValue()) { // iPhone
            client = 2;
            siteType = 1;// app
        } else if (sasParams.getOsId() == HandSetOS.Windows_Mobile_OS.getValue()
                || sasParams.getOsId() == HandSetOS.Windows_CE.getValue()
                || sasParams.getOsId() == HandSetOS.Windows_Phone_OS.getValue()
                || sasParams.getOsId() == HandSetOS.Windows_RT.getValue()) {
            client = 3;
        } else {
            LOG.debug("Lomark: Device OS - Unsupported OS");
            LOG.info("Configure parameters inside lomark returned false");
            return false;
        }
        // filter non udid app traffic for Lomark
        if (client < 3 && StringUtils.isBlank(uuid)) {
            LOG.debug("Lomark: Udid - mandatory paramter for app - missing");
            LOG.info("Configure parameters inside lomark returned false");
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "lomark";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder();
            final HashMap<String, String> requestMap = new HashMap<String, String>();
            requestMap.put("Format", "json");
            requestMap.put("Ip", sasParams.getRemoteHostIp());
            requestMap.put("Client", String.valueOf(client));
            requestMap.put("SiteType", String.valueOf(siteType));
            requestMap.put("Key", key);

            url.append(host).append("?Format=json&Ip=").append(sasParams.getRemoteHostIp());
            url.append("&Client=").append(client);
            url.append("&SiteType=").append(siteType);
            url.append("&Key=").append(key);

            url.append("&AppId=").append(externalSiteId);
            requestMap.put("AppId", externalSiteId);

            // ("1 Banner 2 interstitial");
            if (isInterstitial()) {
                url.append("&AdSpaceType=2");
                requestMap.put("AdSpaceType", "2");
            } else {
                url.append("&AdSpaceType=1");
                requestMap.put("AdSpaceType", "1");
            }
            // map operator
            final Integer carrierId = getCarrierId();
            url.append("&Operator=").append(carrierId);// .append("1:China Mobile,2:China Unicom,3:China Telecom,4:other");
            requestMap.put("Operator", carrierId.toString());
            if (!StringUtils.isEmpty(latitude) && !StringUtils.isEmpty(longitude)) {
                requestMap.put("Lat", latitude);
                requestMap.put("Long", longitude);
                url.append("&Lat=").append(latitude);
                url.append("&Long=").append(longitude);
            }
            url.append("&Uuid=").append(uuid);
            requestMap.put("Uuid", uuid);
            url.append("&DeviceType=1");
            requestMap.put("DeviceType", "1");
            url.append("&AppName=").append(blindedSiteId);
            requestMap.put("AppName", blindedSiteId);
            // map Category
            int category = 12;
            final Long[] segmentCategories = entity.getTags();
            if (null != segmentCategories && segmentCategories.length > 0 && 1 != segmentCategories[0]
                    && null != categoryMap.get(segmentCategories[0])) {
                category = categoryMap.get(segmentCategories[0]);
            } else if (null != sasParams.getCategories()) {
                for (int i = 0; i < sasParams.getCategories().size(); i++) {
                    if (null != categoryMap.get(sasParams.getCategories().get(i))) {
                        category = categoryMap.get(sasParams.getCategories().get(i));
                        break;
                    }
                }
            }
            url.append("&Category=").append(category);// get category map
            requestMap.put("Category", String.valueOf(category));
            if (width != 0 && height != 0) {
                requestMap.put("Aw", String.valueOf(width));
                requestMap.put("Ah", String.valueOf(height));
                url.append("&Aw=").append(width).append("&Ah=").append(height);
            }
            if (sasParams.getSdkVersion() != null) {
                requestMap.put("SdkVersion", sasParams.getSdkVersion());
                url.append("&SdkVersion=").append(sasParams.getSdkVersion());
            }
            final String millisec = String.valueOf(Calendar.getInstance().getTimeInMillis()).substring(0, 10);
            url.append("&Timestamp=").append(millisec);
            requestMap.put("Timestamp", String.valueOf(millisec));
            url.append("&Sign=").append(getSignature(requestMap, secretKey));
            LOG.debug("lomark url is {}", url);
            return new URI(url.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
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

            try {
                final JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.getInt("status") != 100) {
                    adStatus = NO_AD;
                    return;
                }
                final JSONObject adResponse =
                        jsonObject.getJSONObject("data").getJSONObject("ad").getJSONObject("creative");
                final JSONObject displayInfo = adResponse.getJSONObject("displayinfo");
                final JSONObject clickInfo = adResponse.getJSONArray("clkinfos").getJSONObject(0);
                final int clickType = clickInfo.getInt("type");
                final int creativeType = displayInfo.getInt("type");
                if (creativeType == 3 || clickType > 3) {
                    LOG.info("Unsupported Creative type or click type for Lomark");
                    adStatus = NO_AD;
                    return;
                }
                final String imageUrl = displayInfo.getString("img");
                final String partnerClickUrl = clickInfo.getString("url");
                final JSONObject trackUrlInfo = adResponse.getJSONObject("trackers");

                final JSONArray clickBeacons =
                        trackUrlInfo.getJSONArray("clicks").getJSONObject(0).getJSONArray("urls");
                final JSONArray impressionBeacon = trackUrlInfo.getJSONObject("display").getJSONArray("urls");

                String partnerClickBeacon = null;
                if (clickBeacons.length() > 0) {
                    partnerClickBeacon = clickBeacons.getString(0);
                }
                String partnerImpressionBeacon = null;
                if (impressionBeacon.length() > 0) {
                    partnerImpressionBeacon = impressionBeacon.getString(0);
                }

                final VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);
                context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, partnerClickUrl);

                if (StringUtils.isNotBlank(imageUrl)) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, imageUrl);
                }
                if (StringUtils.isNotBlank(partnerClickBeacon)) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_BEACON, partnerClickBeacon);
                }
                if (StringUtils.isNotBlank(partnerImpressionBeacon)) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, partnerImpressionBeacon);
                }

                TemplateType type;
                if (creativeType == 2) {
                    context.put(VelocityTemplateFieldConstants.AD_TEXT,
                            displayInfo.getJSONObject("title").getString("text"));
                    context.put(VelocityTemplateFieldConstants.DESCRIPTION, displayInfo.getString("subtitle"));
                    final String vmTemplate = Formatter.getRichTextTemplateForSlot(selectedSlotId.toString());
                    if (StringUtils.isEmpty(vmTemplate)) {
                        LOG.info("No template found for the slot");
                        adStatus = NO_AD;
                        return;
                    } else {
                        context.put(VelocityTemplateFieldConstants.TEMPLATE, vmTemplate);
                        type = TemplateType.RICH;
                    }
                } else {
                    type = TemplateType.IMAGE;
                    adStatus = "AD";
                }
                responseContent = Formatter.getResponseFromTemplate(type, context, sasParams, beaconUrl);
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from lomark: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return config.getString("lomark.advertiserId");
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    private String getSignature(final HashMap<String, String> params, final String secret) throws IOException {
        // first sort asc as per the paramter names
        final Map<String, String> sortedParams = new TreeMap<String, String>(params);
        final Set<Entry<String, String>> entrys = sortedParams.entrySet();
        // after sorting organize all paramters with key=value"format
        final StringBuilder basestring = new StringBuilder();
        for (final Entry<String, String> param : entrys) {
            basestring.append(param.getKey()).append('=').append(param.getValue());
        }
        basestring.append(secret);
        // MD5 Hashed
        final byte[] bytes = DigestUtils.md5(basestring.toString().getBytes());
        final StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            final String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append('0');
            }
            sign.append(hex);
        }
        return sign.toString();

    }

    private Integer getCarrierId() {
        try {
            final int carrierId = sasParams.getCarrierId();
            if (carrierIdMap.containsKey(carrierId)) {
                return carrierIdMap.get(carrierId);
            } else {
                return 4;
            }
        } catch (final Exception e) {
            LOG.info("Cannot map carrier Id for Lomark, exception raised {}", e);
        }
        return 4;
    }
}
