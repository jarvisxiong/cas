package com.inmobi.adserve.channels.adnetworks.smaato;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.smaato.soma.oapi.Response;
import com.smaato.soma.oapi.Response.Ads.Ad;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class DCPSmaatoAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger         LOG              = LoggerFactory.getLogger(DCPSmaatoAdnetwork.class);

    private transient String            latitude;
    private transient String            longitude;
    private int                         width;
    private int                         height;
    private String                      dimension;

    private static final String         PUBID            = "pub";
    private static final String         UA               = "device";
    private static final String         CLIENT_IP        = "devip";
    private static final String         ADSPACEID        = "adspace";

    private static final String         IFA              = "iosadid";
    private static final String         IFA_TRACKING     = "iosadtracking";
    private static final String         OPEN_UDID        = "openudid";
    private static final String         ANDROID_ID       = "androidid";
    private static final String         ODIN1            = "odin";
    // private static final String VERSION = "apiver";
    protected static final String       LATLONG          = "gps";
    protected static final String       GENDER           = "gender";
    protected static final String       KEYWORDS         = "kws";
    protected static final String       AGE              = "age";
    private static final String         WIDTH            = "width";
    private static final String         HEIGHT           = "height";
    private static final String         FORMAT           = "format";
    // private static final String FORMAT_STRICT = "formatstrict";
    private static final String         DIMENSION        = "dimension";
    private static final String         DIMENSION_STRICT = "dimensionstrict";
    private static final String         SUCCESS          = "success";
    private static final String         IMAGE_TYPE       = "IMG";
    private static final String         TEXT_TYPE        = "TXT";

    private static final String         responseFormat   = "all";
    private static final String         strictField      = "true";
    private static final String         latLongFormat    = "%s,%s";
    private final String                publisherId;

    private static JAXBContext          jaxbContext;
    private static Unmarshaller         jaxbUnmarshaller;
    private static Map<Integer, String> slotIdMap;

    static {
        slotIdMap = new HashMap<Integer, String>();
        slotIdMap.put(1, "mma");
        slotIdMap.put(2, "mma");
        slotIdMap.put(3, "mma");
        slotIdMap.put(4, "mma");
        slotIdMap.put(10, "medrect");
        slotIdMap.put(11, "leader");
        slotIdMap.put(13, "sky");
        slotIdMap.put(14, "full_320x480");
        slotIdMap.put(15, "mma");
        slotIdMap.put(16,"full_768x1024");
        slotIdMap.put(17,"full_800x1280");

    }

    public DCPSmaatoAdnetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);

        publisherId = config.getString("smaato.pubId");
        try {
            jaxbContext = JAXBContext.newInstance(Response.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        }
        catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for smaato so exiting adapter");
            return false;
        }
        host = config.getString("smaato.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            dimension = slotIdMap.get((sasParams.getSlot()).intValue());
            if (StringUtils.isBlank(dimension)) {
                LOG.debug("mandatory parameters missing for smaato so exiting adapter");
                return false;
            }
            Dimension dim = SlotSizeMapping.getDimension((sasParams.getSlot()).longValue());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());

        }
        if (StringUtils.isBlank(getUid())) {
            LOG.debug("mandatory parameters missing for smaato so exiting adapter");
            return false;
        }

        LOG.info("Configure parameters inside Smaato returned true");
        return true;
    }

    @Override
    public String getName() {
        return "smaato";
    }

    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder url = new StringBuilder(host);
        // appendQueryParam(url, VERSION, apiVersion, true);

        appendQueryParam(url, ADSPACEID, externalSiteId, true);
        appendQueryParam(url, PUBID, publisherId, false);
        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(url, FORMAT, responseFormat, false);
        // appendQueryParam(url, FORMAT_STRICT, strictField, false);
        appendQueryParam(url, DIMENSION, dimension, false);
        appendQueryParam(url, DIMENSION_STRICT, strictField, false);

        // TODO map the udids
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
            appendQueryParam(url, IFA, casInternalRequestParameters.uidIFA, false);
            appendQueryParam(url, IFA_TRACKING, casInternalRequestParameters.uidADT, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
            appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.uidMd5, false);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
            appendQueryParam(url, ANDROID_ID, casInternalRequestParameters.uidIDUS1, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
            appendQueryParam(url, OPEN_UDID, casInternalRequestParameters.uid, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
            appendQueryParam(url, ODIN1, casInternalRequestParameters.uidSO1, false);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            appendQueryParam(url, ODIN1, casInternalRequestParameters.uidO1, false);
        }

        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
            appendQueryParam(url, LATLONG, getURLEncode(String.format(latLongFormat, latitude, longitude), format),
                false);
        }
        if (StringUtils.isNotBlank(sasParams.getGender())) {
            appendQueryParam(url, GENDER, sasParams.getGender(), false);
        }
        if (null != sasParams.getPostalCode()) {
            appendQueryParam(url, ZIP, sasParams.getPostalCode().toString(), false);
        }
        appendQueryParam(url, KEYWORDS, getURLEncode(getCategories(',', true, false), format), false);
        if (width != 0) {
            appendQueryParam(url, WIDTH, width + "", false);
        }
        if (height != 0) {
            appendQueryParam(url, HEIGHT, height + "", false);
        }
        if (null != sasParams.getAge()) {
            appendQueryParam(url, AGE, sasParams.getAge().toString(), false);
        }

        LOG.debug("Smaato url is {}", url);
        return new URI(url.toString());
    }

    @Override
    public HttpRequest getHttpRequest() throws Exception {
        try {
            URI uri = getRequestUri();
            requestUrl = uri.toString();
            request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString());
            request.setHeader(HttpHeaders.Names.HOST, uri.getHost());
            request.setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent());
            request.setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us");
            request.setHeader(HttpHeaders.Names.REFERER, uri.toString());
            request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES);
            request.setHeader("x-mh-User-Agent", sasParams.getUserAgent());
            request.setHeader("x-mh-X-Forwarded-For", sasParams.getRemoteHostIp());
            request.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp());
        }
        catch (Exception ex) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.HTTPREQUEST_ERROR;
            LOG.info("Error in making http request {}  for partner : {}", ex.getMessage(), getName());
        }
        return request;
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
            try {
                Response smaatoResponse = (Response) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(response
                        .getBytes()));

                if (!SUCCESS.equalsIgnoreCase(smaatoResponse.getStatus()) || smaatoResponse.getAds().getAd() == null) {
                    adStatus = "NO_AD";
                    statusCode = 500;
                    responseContent = "";
                    return;
                }

                Ad ad = smaatoResponse.getAds().getAd();

                TemplateType t = null;
                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, ad.getAction().getTarget());
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);

                context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, ad.getBeacons().getBeacon());
                if (IMAGE_TYPE.equalsIgnoreCase(ad.getType()) && StringUtils.isNotBlank(ad.getLink())) {
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, ad.getLink());
                    t = TemplateType.IMAGE;
                }
                else if (TEXT_TYPE.equalsIgnoreCase(ad.getType()) && StringUtils.isNotBlank(ad.getAdtext())) {
                    context.put(VelocityTemplateFieldConstants.AdText, ad.getAdtext());
                    String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
                    if (StringUtils.isEmpty(vmTemplate)) {
                        t = TemplateType.PLAIN;
                    }
                    else {
                        context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                        t = TemplateType.RICH;
                    }
                }
                else {
                    adStatus = "NO_   AD";
                    return;
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";

            }

            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from Smaato");
                LOG.info("Response from Smaato {}", response);
            }
        }
    }

    @Override
    public String getId() {
        return (config.getString("smaato.advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

}
