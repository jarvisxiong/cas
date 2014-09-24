package com.inmobi.adserve.channels.adnetworks.madhouse;

// Created By : Dhanasekaran K P

import com.inmobi.adserve.channels.adnetworks.rubicon.DCPRubiconAdnetwork;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DCPMadHouseAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPRubiconAdnetwork.class);
    private static final String AD_TYPE = "adtype"; // Banner or Interstitial.
    private static final String DEVICE_OS = "os";
    private static final String OS_VERSION = "osv";
    private static final String UA = "ua";
    private static final String IP = "ip";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String PUBLISHER_ID = "pid"; // Advertiser ID.
    private static final String PCAT = "pcat"; // Category Mapping.

    private static final String ANDROID_ID = "aid"; // Android OS.
    private static final String IDFA = "idfa"; // iOS OS.
    private static final String OPEN_UDID = "oid";
    private static final String UID = "uid"; // Other OS.
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";

    private static final String BUNDLE_ID_TEMPLATE = "com.inmobi-exchange.%s";
    private String latitude;
    private String longitude;
    private String uniqueID;
    private String publisherID;
    private String category;
    private int width;
    private int height;
    private boolean isValidOS;

    // OS mapping.
    private static Map<Integer, Integer> osMap;
    static {
        osMap = new HashMap<Integer, Integer>();
        osMap.put(3, 0);
        osMap.put(5, 1);
        osMap.put(9, 2);
        osMap.put(13, 3);
        osMap.put(8, 5);
        osMap.put(1, 4);
    }

    // Category mapping.
    private static Map<Long, String> categoryMap;
    static {
        categoryMap = new HashMap<Long, String>();
        categoryMap.put(1L, "5");
        categoryMap.put(2L, "18");
        categoryMap.put(3L, "11");
        categoryMap.put(4L, "12");
        categoryMap.put(5L, "18");
        categoryMap.put(6L, "13");
        categoryMap.put(7L, "7");
        categoryMap.put(8L, "10");
        categoryMap.put(9L, "2");
        categoryMap.put(10L, "9");
        categoryMap.put(11L, "16");
        categoryMap.put(12L, "24");
        categoryMap.put(13L, "16");
        categoryMap.put(14L, "16");
        categoryMap.put(15L, "16");
        categoryMap.put(16L, "16");
        categoryMap.put(17L, "16");
        categoryMap.put(18L, "16");
        categoryMap.put(19L, "7");
        categoryMap.put(20L, "15");
        categoryMap.put(21L, "15");
        categoryMap.put(22L, "22");
        categoryMap.put(23L, "16");
        categoryMap.put(24L, "26");
        categoryMap.put(25L, "10");
        categoryMap.put(26L, "5");
        categoryMap.put(27L, "16");
        categoryMap.put(28L, "27");
        categoryMap.put(29L, "29");
        categoryMap.put(30L, "13");
        categoryMap.put(31L, "6");
        categoryMap.put(32L, "15");
        categoryMap.put(33L, "14");
        categoryMap.put(34L, "21");
        categoryMap.put(35L, "22");
        categoryMap.put(36L, "1");
        categoryMap.put(37L, "14");
        categoryMap.put(38L, "8");
        categoryMap.put(39L, "15");
        categoryMap.put(40L, "11");
        categoryMap.put(41L, "1");
        categoryMap.put(42L, "5");
        categoryMap.put(43L, "9");
        categoryMap.put(44L, "15");
        categoryMap.put(45L, "10");
        categoryMap.put(46L, "10");
        categoryMap.put(47L, "15");
        categoryMap.put(48L, "6");
        categoryMap.put(49L, "3");
        categoryMap.put(50L, "15");
        categoryMap.put(51L, "1");
        categoryMap.put(52L, "15");
        categoryMap.put(53L, "10");
        categoryMap.put(54L, "20");
        categoryMap.put(55L, "25");
        categoryMap.put(56L, "15");
        categoryMap.put(57L, "15");
        categoryMap.put(58L, "11");
        categoryMap.put(59L, "20");
        categoryMap.put(60L, "7");
        categoryMap.put(61L, "16");
        categoryMap.put(62L, "15");
        categoryMap.put(63L, "8");
        categoryMap.put(64L, "15");
        categoryMap.put(65L, "15");
        categoryMap.put(66L, "19");
        categoryMap.put(67L, "15");
        categoryMap.put(68L, "13");
        categoryMap.put(69L, "5");
        categoryMap.put(70L, "16");
        categoryMap.put(71L, "5");
        categoryMap.put(72L, "8");
        categoryMap.put(73L, "5");
        categoryMap.put(74L, "17");
    }
    //

    public DCPMadHouseAdNetwork(final Configuration config,
                                final Bootstrap clientBootstrap,
                                final HttpRequestHandlerBase baseRequestHandler,
                                final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        // Get the host.
        host = config.getString("madhouse.host");

        // If the OS is iOS, then Open UDID or IDFA is required.
        if (isIOS()) {
            if (StringUtils.isBlank(casInternalRequestParameters.uidIDUS1)
                && StringUtils.isBlank(casInternalRequestParameters.uidIFA)) {
                LOG.debug("mandatory parameters missing for madhouse so exiting adapter");
                return false;
            }
        }

        // If the OS is Android, then Android ID is required.
        if (isAndroid()) {
            if (StringUtils.isBlank(casInternalRequestParameters.uidMd5)
                && StringUtils.isBlank(casInternalRequestParameters.uid)
                && StringUtils.isBlank(casInternalRequestParameters.uidO1)) {
                LOG.debug("mandatory parameters missing for madhouse so exiting adapter");
                return false;
            }
        }

        // Operating system check.
        int sasParamsOsId = sasParams.getOsId();
        isValidOS = (sasParamsOsId >= HandSetOS.Others.getValue()
            && sasParamsOsId <= HandSetOS.Windows_RT.getValue()); // Check if the OS ID is valid.
        if (!isValidOS) {
            LOG.debug("mandatory parameters missing for madhouse so exiting adapter");
            return false;
        }

        // Unique ID for other OS's.
        if(!isAndroid() && !isIOS()) {
            uniqueID = String.format(BUNDLE_ID_TEMPLATE, blindedSiteId);
        }

        // Publisher ID.
        publisherID = config.getString("madhouse.advertiserId");

        // IP check.
        if(StringUtils.isBlank(sasParams.getRemoteHostIp())) {
            LOG.debug("mandatory parameters missing for madhouse so exiting adapter");
            return false;
        }

        // Get latitude & longitude.
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
            && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }

        // Get width & height.
        if (null != sasParams.getSlot()
            && null != SlotSizeMapping.getDimension((long) sasParams.getSlot())) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        } else {
            LOG.debug("mandatory parameters missing for madhouse so exiting adapter");
            return false;
        }

        // Get the category.
        category = categoryMap.get(sasParams.getCategories().get(0));

        // Configuration successful.
        LOG.info("Configure parameters inside MadHouse returned true");
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
        URIBuilder builder = new URIBuilder();
        builder.setHost(host);
        builder.setScheme("http");

        // Add the parameters.
        // Ad Type.
        if(isInterstitial()) {
            builder.setParameter(AD_TYPE, "5"); // Interstitial.
        } else {
            builder.setParameter(AD_TYPE, "2"); // Banner.
        }

        // OS.
        Integer sasParamsOsId = sasParams.getOsId();
        if (isValidOS) {
            builder.setParameter(DEVICE_OS, osMap.get(sasParamsOsId).toString());
        }

        // OS Version.
        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            builder.setParameter(OS_VERSION, sasParams.getOsMajorVersion());
        }

        // Android ID for Android.
        if (isAndroid()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                builder.setParameter(ANDROID_ID, casInternalRequestParameters.uidMd5);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
                builder.setParameter(ANDROID_ID, casInternalRequestParameters.uid);
            } else if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
                builder.setParameter(ANDROID_ID, casInternalRequestParameters.uidO1);
            }
        }

        // IDFA for iOS.
        if (isIOS()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
                builder.setParameter(OPEN_UDID, casInternalRequestParameters.uidIDUS1);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                builder.setParameter(IDFA, casInternalRequestParameters.uidIFA);
            }
        }

        // UID for other OS's.
        if (!isIOS() && !isAndroid()) {
            builder.setParameter(UID, uniqueID);
        }

        // Width & Height.
        if (null != sasParams.getSlot()
            && null != SlotSizeMapping.getDimension((long) sasParams.getSlot())) {
            builder.setParameter(WIDTH, String.valueOf(width));
            builder.setParameter(HEIGHT, String.valueOf(height));
        }

        // Latitude & Longitude.
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
            && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            builder.setParameter(LATITUDE, latitude);
            builder.setParameter(LONGITUDE, longitude);
        }

        // IP.
        if(StringUtils.isNotBlank(sasParams.getRemoteHostIp())) {
            builder.setParameter(IP, sasParams.getRemoteHostIp());
        }

        // UA, Category and Publisher ID.
        builder.setParameter(UA, getURLEncode(sasParams.getUserAgent(), format));
        builder.setParameter(PCAT, category);
        builder.setParameter(PUBLISHER_ID, publisherID);

        URI uri = builder.build();
        return uri;
    }

    @Override
    public void parseResponse(final String response,
                              final HttpResponseStatus status) {
        LOG.debug("Madhouse response is {}", response);

        if (isValidResponse(response, status)){
            statusCode = status.code();
            VelocityContext context = new VelocityContext();
            try {
                // DOM Parser.
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource inputSource = new InputSource(new StringReader(response));
                Document document = builder.parse(inputSource);

                // Get the HTML Ad.
                Element htmlContent = (Element) document.getElementsByTagName("adhtml").item(0);
                context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, htmlContent.getTextContent());
                //

                /* Other responses.
                Node adSpaceID = document.getElementsByTagName("adspaceid").item(0);
                Node returnCode = document.getElementsByTagName("returncode").item(0);
                Node adType = document.getElementsByTagName("adtype").item(0);
                */

                statusCode = status.code();
                responseContent = Formatter.getResponseFromTemplate(Formatter.TemplateType.HTML, context, sasParams, beaconUrl);

                adStatus = "AD";
            } catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from Madhouse");
                LOG.info("Response from Madhouse {}", response);
            }
        }

        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getName() {
        return "madhouse";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public String getId() {
        return (config.getString("madhouse.advertiserId"));
    }
}
