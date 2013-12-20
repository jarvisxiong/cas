package com.inmobi.adserve.channels.adnetworks.lomark;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;


public class DCPLomarkAdNetwork extends BaseAdNetworkImpl {
    private final Configuration          config;
    private transient String             key;
    private transient String             secretKey;
    private transient String             latitude;
    private transient String             longitude;
    private int                          width;
    private int                          height;
    private int                          client;
    private int                          siteType;
    private String                       uuid;

    private static Map<Long, Integer>    categoryMap = new HashMap<Long, Integer>();
    private static Map<Integer, Integer> carrierIdMap;
    static {
        carrierIdMap = new HashMap<Integer, Integer>();
        // carrier ip map
        carrierIdMap.put(786, 3);
        carrierIdMap.put(787, 1);
        carrierIdMap.put(788, 2);
        // category ip map
        categoryMap.put(4l, 1);
        categoryMap.put(23l, 1);
        categoryMap.put(24l, 1);
        categoryMap.put(13l, 2);
        categoryMap.put(3l, 3);
        categoryMap.put(3l, 9);
        categoryMap.put(6l, 4);
        categoryMap.put(10l, 4);
        categoryMap.put(18l, 4);
        categoryMap.put(31l, 4);
        categoryMap.put(30l, 5);
        categoryMap.put(12l, 6);
        categoryMap.put(29l, 7);
        categoryMap.put(11l, 8);
        categoryMap.put(25l, 8);
        categoryMap.put(19l, 9);
        categoryMap.put(32l, 9);
        categoryMap.put(15l, 10);
        categoryMap.put(28l, 11);
    }

    public DCPLomarkAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent) {
        super(baseRequestHandler, serverEvent, logger);
        this.config = config;
        this.logger = logger;
        this.clientBootstrap = clientBootstrap;
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandatory parameters missing for lomark so exiting adapter");
            return false;
        }
        host = config.getString("lomark.host");
        key = config.getString("lomark.key");
        secretKey = config.getString("lomark.secretkey");

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        uuid = getUid();
        client = 3;
        siteType = 2; // wap
        if (sasParams.getOsId() == HandSetOS.Android.getValue()) { // android
            client = 1;
            siteType = 1; // app
        }
        else if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) { // iPhone
            client = 2;
            siteType = 1;// app
        }
        else if (sasParams.getOsId() == HandSetOS.Windows_Mobile_OS.getValue()
                || sasParams.getOsId() == HandSetOS.Windows_CE.getValue()
                || sasParams.getOsId() == HandSetOS.Windows_Phone_OS.getValue()
                || sasParams.getOsId() == HandSetOS.Windows_RT.getValue()) {
            client = 3;
        }
        else {
            logger.info("Lomark: Device OS - Unsupported OS");
            return false;
        }
        // filter non udid app traffic for Lomark
        if (client < 3 && StringUtils.isBlank(uuid)) {
            logger.info("Lomark: Udid - mandatory paramter for app - missing");
            return false;
        }

        logger.info("Configure parameters inside lomark returned true");
        return true;
    }

    @Override
    public String getName() {
        return "lomark";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder();
            HashMap<String, String> requestMap = new HashMap<String, String>();
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
            int adSpaceId = getAdType();
            url.append("&AdSpaceType=").append(adSpaceId); // ("1 Banner 2 interstitial");
            requestMap.put("AdSpaceType", String.valueOf(adSpaceId));
            // map operator
            Integer carrierId = getCarrierId();
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
            Long[] segmentCategories = entity.getTags();
            if (null != segmentCategories && 1 != segmentCategories[0] && null != categoryMap.get(segmentCategories[0])) {
                category = categoryMap.get(segmentCategories[0]);
            }
            else if (null != sasParams.getCategories()) {
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
            String millisec = String.valueOf(Calendar.getInstance().getTimeInMillis()).substring(0, 10);
            url.append("&Timestamp=").append(millisec);
            requestMap.put("Timestamp", String.valueOf(millisec));
            url.append("&Sign=").append(getSignature(requestMap, secretKey));
            logger.debug("lomark url is", url.toString());
            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info(exception.getMessage());
        }
        return null;
    }

    @Override
    public void parseResponse(String response, HttpResponseStatus status) {
        logger.debug("response is", response, "and response length is", response.length());
        if (status.getCode() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.getCode();

            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.getInt("status") != 100) {
                    adStatus = "NO_AD";
                    return;
                }
                JSONObject adResponse = jsonObject.getJSONObject("data").getJSONObject("ad").getJSONObject("creative");
                JSONObject displayInfo = adResponse.getJSONObject("displayinfo");
                JSONObject clickInfo = adResponse.getJSONArray("clkinfos").getJSONObject(0);
                int clickType = clickInfo.getInt("type");
                int creativeType = displayInfo.getInt("type");
                if (creativeType == 3 || clickType > 3) {
                    logger.info("Unsupported Creative type or click type for Lomark");
                    adStatus = "NO_AD";
                    return;
                }
                String imageUrl = displayInfo.getString("img");
                String partnerClickUrl = clickInfo.getString("url");
                JSONObject trackUrlInfo = adResponse.getJSONObject("trackers");

                JSONArray clickBeacons = trackUrlInfo.getJSONArray("clicks").getJSONObject(0).getJSONArray("urls");
                JSONArray impressionBeacon = trackUrlInfo.getJSONObject("display").getJSONArray("urls");

                String partnerClickBeacon = null;
                if (clickBeacons.length() > 0) {
                    partnerClickBeacon = clickBeacons.getString(0);
                }
                String partnerImpressionBeacon = null;
                if (impressionBeacon.length() > 0) {
                    partnerImpressionBeacon = impressionBeacon.getString(0);
                }

                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, partnerClickUrl);

                if (StringUtils.isNotBlank(imageUrl)) {
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, imageUrl);
                }
                if (StringUtils.isNotBlank(partnerClickBeacon)) {
                    context.put(VelocityTemplateFieldConstants.PartnerClickBeacon, partnerClickBeacon);
                }
                if (StringUtils.isNotBlank(partnerImpressionBeacon)) {
                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, partnerImpressionBeacon);
                }

                TemplateType type;
                if (creativeType == 2) {
                    context.put(VelocityTemplateFieldConstants.AdText,
                        displayInfo.getJSONObject("title").getString("text"));
                    context.put(VelocityTemplateFieldConstants.Description, displayInfo.getString("subtitle"));
                    String vmTemplate = Formatter.getRichTextTemplateForSlot(slot);
                    if (StringUtils.isEmpty(vmTemplate)) {
                        logger.info("No template found for the slot");
                        adStatus = "NO_AD";
                        return;
                    }
                    else {
                        context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                        type = TemplateType.RICH;
                    }
                }
                else {
                    type = TemplateType.IMAGE;
                    adStatus = "AD";
                }
                responseContent = Formatter.getResponseFromTemplate(type, context, sasParams, beaconUrl, logger);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from lomark :", exception);
                logger.info("Response from lomark :", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    logger.info("Error while rethrowing the exception :", e);
                }
            }
        }
        logger.debug("response length is", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("lomark.advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    private String getSignature(HashMap<String, String> params, String secret) throws IOException

    {
        // first sort asc as per the paramter names
        Map<String, String> sortedParams = new TreeMap<String, String>(params);
        Set<Entry<String, String>> entrys = sortedParams.entrySet();
        // after sorting，organize all paramters with key=value"format
        StringBuilder basestring = new StringBuilder();
        for (Entry<String, String> param : entrys) {
            basestring.append(param.getKey()).append('=').append(param.getValue());
        }
        basestring.append(secret);
        // MD5 Hashed
        byte[] bytes = DigestUtils.md5(basestring.toString().getBytes());
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append('0');
            }
            sign.append(hex);
        }
        return sign.toString();

    }

    private int getAdType() {
        Integer slot = Integer.parseInt(sasParams.getSlot());
        if (10 == slot // 300X250
                || 14 == slot // 320X480
                || 16 == slot) /* 768X1024 */{
            return 2;
        }
        return 1;
    }

    private Integer getCarrierId() {
        try {
            int carrierId = sasParams.getCarrierId();
            if (carrierIdMap.containsKey(carrierId)) {
                return carrierIdMap.get(carrierId);
            }
            else {
                return 4;
            }
        }
        catch (Exception e) {
            logger.info("Cannot map carrier Id for Lomark");
        }
        return 4;
    }
}
