package com.inmobi.adserve.channels.adnetworks.placeiq;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class DCPPlaceIQAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger           LOG           = LoggerFactory.getLogger(DCPPlaceIQAdnetwork.class);

    private transient String              latitude;
    private transient String              longitude;
    private int                           width;
    private int                           height;
    private String                        os            = null;
    private static final String           sizeFormat    = "%dx%d";

    private static final String           LAT           = "LT";
    private static final String           LONG          = "LG";
    private static final String           RESPONSE_TYPE = "ST";
    private static final String           REQUEST_TYPE  = "RT";
    private static final String           SIZE          = "SZ";
    private static final String           ANDROIDMD5    = "AM";
    private static final String           ANDROIDIDSHA1 = "AH";
    private static final String           IDFA          = "IA";
    private static final String           UA            = "UA";
    private static final String           CLIENT_IP     = "IP";
    private static final String           PT            = "PT";
    private static final String           ADUNIT        = "AU";
    private static final String           OS            = "DO";
    private static final String           APPID         = "AP";
    private static final String           SITEID        = "SI";
    //private static final String           ZIP           = "ZP";
    private static final String           COUNTRY       = "CO";
    //private static final String           SECRET        = "SK";
    private static final String           ADTYPE        = "AT";
    private static final String           APPTYPE       = "STG,RMG,MRD";
    private static final String           WAPTYPE       = "STG,STW,RMG,MRD";
    private static final String           ANDROID       = "Android";
    private static final String           IOS           = "iOS";
    private static final String           auIdFormat    = "%s/%s/%s/%s";
    private static final String           XMLFORMAT       = "xml";
    private final SimpleDateFormat        dateFormat    = new SimpleDateFormat("yyyy-MM-dd");

    private final String                  partnerId;
    private final String                  seed;
    private final String                  requestFormat;
    private final String                  responseFormat;
    private static Map<Integer, String>   categoryList  = new HashMap<Integer, String>();

    private static DocumentBuilderFactory factory;
    private static DocumentBuilder        builder;

    private boolean                       isApp;
    private boolean                       isGeoOrDeviceIdPresent;

    static {

        categoryList.put(2, "bk");
        categoryList.put(3, "bz");
        categoryList.put(7, "ed");
        categoryList.put(8, "en");
        categoryList.put(9, "fn");
        categoryList.put(10, "fd");
        categoryList.put(11, "gm");
        categoryList.put(27, "sp");
        categoryList.put(31, "ht");
        categoryList.put(32, "lf");
        categoryList.put(34, "md");
        categoryList.put(35, "mu");
        categoryList.put(36, "nw");
        categoryList.put(37, "ph");
        categoryList.put(38, "at");
        categoryList.put(54, "nw");
        categoryList.put(53, "mu");
        categoryList.put(56, "fm");
        categoryList.put(59, "nw");
        categoryList.put(66, "pd");
        categoryList.put(67, "sh");
        categoryList.put(68, "sc");
        categoryList.put(72, "tr");
        categoryList.put(71, "tl");
        categoryList.put(74, "wt");
    }

    public DCPPlaceIQAdnetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
        partnerId = config.getString("placeiq.partnerId");
        host = config.getString("placeiq.host");
        seed = config.getString("placeiq.seed");
        requestFormat = config.getString("placeiq.requestFormat");
        responseFormat = config.getString("placeiq.responseFormat");
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            LOG.error("XML Parser Builder initialization failed");
        }
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for placeiq so exiting adapter");
            return false;
        }
        isGeoOrDeviceIdPresent = false;
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
            isGeoOrDeviceIdPresent = true;
        }


        
        if (SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
        	Long slot = Long.parseLong(sasParams.getSlot());
        	if(slot == 9l){
        		slot = 15l;
        	}
            Dimension dim = SlotSizeMapping.getDimension(slot);

            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            LOG.debug("mandatory parameters missing for placeiq so exiting adapter");
            return false;
        }

        if (sasParams.getOsId() == HandSetOS.Android.getValue()) { // android
            os = ANDROID;
            isApp = true;
            if (!(StringUtils.isEmpty(casInternalRequestParameters.uidMd5)
                    && StringUtils.isEmpty(casInternalRequestParameters.uid) && StringUtils
                        .isEmpty(casInternalRequestParameters.uidIDUS1))) {
                isGeoOrDeviceIdPresent = true;
            }
        }
        else if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) { // iPhone
            os = IOS;
            isApp = true;
            if (StringUtils.isNotEmpty(casInternalRequestParameters.uidIFA)) {
                isGeoOrDeviceIdPresent = true;
            }
        }
        else {
            isApp = false;
            os = "Windows";
        }
        if (!isGeoOrDeviceIdPresent) {
            LOG.debug("mandatory parameters missing for placeiq so exiting adapter");
            return false;
        }

        LOG.info("Configure parameters inside PlaceIQ returned true");
        return true;
    }

    @Override
    public String getName() {
        return "placeiq";
    }

    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder url = new StringBuilder(host);
        appendQueryParam(url, REQUEST_TYPE, requestFormat, true);
        appendQueryParam(url,RESPONSE_TYPE, responseFormat,false);
        Calendar now = Calendar.getInstance();
        //appendQueryParam(url, SECRET, getHashedValue(dateFormat.format(now.getTime()) + seed, "MD5"), false);
        appendQueryParam(url, PT, partnerId, false);
        String category = getCategory();
        String auId = String.format(auIdFormat, externalSiteId, category, Long.toHexString(sasParams.getSiteIncId()),
            Long.toHexString(this.entity.getAdgroupIncId()));
        appendQueryParam(url, ADUNIT, getURLEncode(auId, format), false);
        appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);

        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        if (StringUtils.isNotEmpty(os)) {
            appendQueryParam(url, OS, os, false);
        }
        if (StringUtils.isNotEmpty(latitude) && StringUtils.isNotEmpty(longitude)) {
            appendQueryParam(url, LAT, latitude, false);
            appendQueryParam(url, LONG, longitude, false);
        }
        /*if (StringUtils.isNotEmpty(casInternalRequestParameters.zipCode)) {
            appendQueryParam(url, ZIP, casInternalRequestParameters.zipCode, false);
        }*/
        if (StringUtils.isNotEmpty(sasParams.getCountryCode())) {
            appendQueryParam(url, COUNTRY, sasParams.getCountryCode().toUpperCase(), false);
        }

        appendQueryParam(url, SIZE, String.format(sizeFormat, width, height), false);
        if (os.equalsIgnoreCase(IOS)) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                appendQueryParam(url, IDFA, casInternalRequestParameters.uidIFA, false);

            }
        }
        if (os.equalsIgnoreCase(ANDROID)) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                appendQueryParam(url, ANDROIDMD5, casInternalRequestParameters.uidMd5, false);
            }
            else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
                appendQueryParam(url, ANDROIDMD5, casInternalRequestParameters.uid, false);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
                appendQueryParam(url, ANDROIDIDSHA1, casInternalRequestParameters.uidIDUS1, false);
            }
        }

        if (isApp) {
            appendQueryParam(url, APPID, sasParams.getSiteIncId() + "", false);
            appendQueryParam(url, ADTYPE, getURLEncode(APPTYPE, format), false);
            
        }
        else {
            appendQueryParam(url, SITEID, sasParams.getSiteIncId() + "", false);
            appendQueryParam(url, ADTYPE, getURLEncode(WAPTYPE, format), false);
        }
        LOG.debug("PlaceIQ url is {}", url);

        return new URI(url.toString());
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        statusCode = status.getCode();
        if (StringUtils.isBlank(response) || status.getCode() != 200
                || (XMLFORMAT.equalsIgnoreCase(responseFormat) && response.contains("<NOAD>"))) {
            if (200 == statusCode) {
                statusCode = 500;
            }
            adStatus = "NO_AD";
            responseContent = "";
            return;
        }
        else {
            try {
                VelocityContext context = new VelocityContext();
                if (XMLFORMAT.equalsIgnoreCase(responseFormat)) {
                    if (response.contains("<NOAD>")) {
                        adStatus = "NO_AD";
                        responseContent = "";
                        return;
                    }
                    Document doc = builder.parse(new InputSource(new java.io.StringReader(response)));
                    doc.getDocumentElement().normalize();
                    NodeList reportNodes = doc.getElementsByTagName("PLACEIQ");
                    Node rootNode = reportNodes.item(0);
                    if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element rootElement = (Element) rootNode;

                        Element htmlContent = (Element) rootElement.getElementsByTagName("CONTENT").item(0);
                        context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, htmlContent.getTextContent());
                    }
                }
                else {
                    context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response);
                }

                statusCode = status.getCode();
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from PlaceIQ : {}", exception);
                LOG.info("Response from PlaceIQ: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());

    }

    @Override
    public String getId() {
        return (config.getString("placeiq.advertiserId"));
    }

    private String getCategory() {
        Long[] segmentCategories = null;
        boolean allTags = false;
        if (entity != null) {
            segmentCategories = entity.getCategoryTaxonomy();
            allTags = entity.isAllTags();
        }
        String category = null;
        if (allTags) {

            for (int index = 0; index < sasParams.getCategories().size(); index++) {
                if (categoryList.get(sasParams.getCategories().get(index).intValue()) == null) {
                    continue;
                }
                category = categoryList.get(sasParams.getCategories().get(index).intValue());
                break;
            }

        }
        else {
            for (int index = 0; index < sasParams.getCategories().size(); index++) {

                int cat = sasParams.getCategories().get(index).intValue();
                for (int i = 0; i < segmentCategories.length; i++) {
                    if (cat == segmentCategories[i]) {
                        category = categoryList.get(cat);
                        break;
                    }
                }
                if (null != category) {
                    break;
                }
            }
        }

        return StringUtils.isBlank(category) ? "uc" : category;
    }

}
