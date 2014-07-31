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

    // Category mapping.
    private static Map<Integer, String> categoryMap;
    static {
        categoryMap = new HashMap<Integer, String>();
        categoryMap.put(1, "5");
        categoryMap.put(2, "18");
        categoryMap.put(3, "11");
        categoryMap.put(4, "12");
        categoryMap.put(5, "18");
        categoryMap.put(6, "13");
        categoryMap.put(7, "7");
        categoryMap.put(8, "10");
        categoryMap.put(9, "2");
        categoryMap.put(10, "9");
        categoryMap.put(11, "16");
        categoryMap.put(12, "24");
        categoryMap.put(13, "16");
        categoryMap.put(14, "16");
        categoryMap.put(15, "16");
        categoryMap.put(16, "16");
        categoryMap.put(17, "16");
        categoryMap.put(18, "16");
        categoryMap.put(19, "7");
        categoryMap.put(20, "15");
        categoryMap.put(21, "15");
        categoryMap.put(22, "22");
        categoryMap.put(23, "16");
        categoryMap.put(24, "26");
        categoryMap.put(25, "10");
        categoryMap.put(26, "5");
        categoryMap.put(27, "16");
        categoryMap.put(28, "27");
        categoryMap.put(29, "29");
        categoryMap.put(30, "13");
        categoryMap.put(31, "6");
        categoryMap.put(32, "15");
        categoryMap.put(33, "14");
        categoryMap.put(34, "21");
        categoryMap.put(35, "22");
        categoryMap.put(36, "1");
        categoryMap.put(37, "14");
        categoryMap.put(38, "8");
        categoryMap.put(39, "15");
        categoryMap.put(40, "11");
        categoryMap.put(41, "1");
        categoryMap.put(42, "5");
        categoryMap.put(43, "9");
        categoryMap.put(44, "15");
        categoryMap.put(45, "10");
        categoryMap.put(46, "10");
        categoryMap.put(47, "15");
        categoryMap.put(48, "6");
        categoryMap.put(49, "3");
        categoryMap.put(50, "15");
        categoryMap.put(51, "1");
        categoryMap.put(52, "15");
        categoryMap.put(53, "10");
        categoryMap.put(54, "20");
        categoryMap.put(55, "25");
        categoryMap.put(56, "15");
        categoryMap.put(57, "15");
        categoryMap.put(58, "11");
        categoryMap.put(59, "20");
        categoryMap.put(60, "7");
        categoryMap.put(61, "16");
        categoryMap.put(62, "15");
        categoryMap.put(63, "8");
        categoryMap.put(64, "15");
        categoryMap.put(65, "15");
        categoryMap.put(66, "19");
        categoryMap.put(67, "15");
        categoryMap.put(68, "13");
        categoryMap.put(69, "5");
        categoryMap.put(70, "16");
        categoryMap.put(71, "5");
        categoryMap.put(72, "8");
        categoryMap.put(73, "5");
        categoryMap.put(74, "17");
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
        }
        else {
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
        }
        else {
            builder.setParameter(AD_TYPE, "2"); // Banner.
        }

        // OS.
        Integer sasParamsOsId = sasParams.getOsId();
        if (isValidOS) {
            builder.setParameter(DEVICE_OS, HandSetOS.values()[sasParamsOsId - 1].toString());
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

                // Other responses.
                Node adSpaceID = document.getElementsByTagName("adspaceid").item(0);
                Node returnCode = document.getElementsByTagName("returncode").item(0);
                Node adType = document.getElementsByTagName("adtype").item(0);
                //

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
