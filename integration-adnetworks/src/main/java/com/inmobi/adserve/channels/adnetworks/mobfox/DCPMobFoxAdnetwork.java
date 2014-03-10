package com.inmobi.adserve.channels.adnetworks.mobfox;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.server.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPMobFoxAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger   LOG          = LoggerFactory.getLogger(DCPMobFoxAdnetwork.class);

    private transient String      latitude;
    private transient String      longitude;
    private int                   width;
    private int                   height;

    private static final String   PUBID        = "s";
    private static final String   UA           = "u";
    private static final String   CLIENT_IP    = "i";
    private static final String   TRAFFICTYPE  = "m";
    private static final String   MRAIDSUPPORT = "c_mraid";
    private static final String   SHA1UDID     = "o_mcsha1";
    private static final String   MD5UDID      = "o_mcmd5";
    private static final String   IFA          = "o_iosadvid";
    private static final String   VERSION      = "v";
    protected static final String LAT          = "latitude";
    protected static final String LONG         = "longitude";
    protected static final String GENDER       = "demo.gender";
    protected static final String KEYWORDS     = "demo.keywords";
    protected static final String AGE          = "demo.age";
    private static final String   WIDTH        = "adspace.width";
    private static final String   HEIGHT       = "adspace.height";
    private static final String   B_SITE_ID    = "s_subid";
    private static final String   REQUEST_TYPE = "rt";

    private static final String   type         = "live";
    private static final String   mraidType    = "1";
    private static final String   apiVersion   = "2.0";
    private static final String   requestType  = "api";

    private static JAXBContext    jaxbContext;
    private static Unmarshaller   jaxbUnmarshaller;

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPMobFoxAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        try {
            jaxbContext = JAXBContext.newInstance(Request.class);
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
            LOG.debug("mandatory parameters missing for mobfox so exiting adapter");
            return false;
        }
        host = config.getString("mobfox.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
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

        LOG.info("Configure parameters inside Mobfox returned true");
        return true;
    }

    @Override
    public String getName() {
        return "mobfox";
    }

    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder url = new StringBuilder(host);
        appendQueryParam(url, REQUEST_TYPE, requestType, true);
        appendQueryParam(url, PUBID, externalSiteId, false);
        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
        // TODO is [p(url for sites) required?
        appendQueryParam(url, TRAFFICTYPE, type, false);
        appendQueryParam(url, MRAIDSUPPORT, mraidType, false);
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
            appendQueryParam(url, IFA, casInternalRequestParameters.uidIFA, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
            appendQueryParam(url, SHA1UDID, casInternalRequestParameters.uidSO1, false);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            appendQueryParam(url, SHA1UDID, casInternalRequestParameters.uidO1, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
            appendQueryParam(url, MD5UDID, casInternalRequestParameters.uidMd5, false);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
            appendQueryParam(url, MD5UDID, casInternalRequestParameters.uid, false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
            appendQueryParam(url, SHA1UDID, casInternalRequestParameters.uidIDUS1, false);
        }
        appendQueryParam(url, VERSION, apiVersion, false);
        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
            appendQueryParam(url, LAT, latitude, false);
            appendQueryParam(url, LONG, longitude, false);
        }
        if (StringUtils.isNotBlank(sasParams.getGender())) {
            appendQueryParam(url, GENDER, sasParams.getGender(), false);
        }
        appendQueryParam(url, KEYWORDS, getURLEncode(getCategories(',', true, false), format), false);
        if (width != 0) {
            appendQueryParam(url, WIDTH, width + "", false);
        }
        if (height != 0) {
            appendQueryParam(url, HEIGHT, height + "", false);
        }
        if (StringUtils.isNotBlank(sasParams.getAge())) {
            appendQueryParam(url, AGE, sasParams.getAge(), false);
        }
        appendQueryParam(url, B_SITE_ID, blindedSiteId, false);

        LOG.debug("Mobfox url is {}", url);
        return new URI(url.toString());
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);

        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.code();
            VelocityContext context = new VelocityContext();
            try {
                Request request = (Request) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(response.getBytes()));
                String htmlContent = request.getHtmlString();
                if (StringUtils.isBlank(htmlContent)) {
                    adStatus = "NO_AD";
                    statusCode = 500;
                    responseContent = "";
                    return;
                }
                context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, htmlContent);

                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from Mobfox");
                LOG.info("Response from Mobfox {}", response);
            }
        }
    }

    @Override
    public String getId() {
        return (config.getString("mobfox.advertiserId"));
    }

    @XmlRootElement
    public static class Request {
        String htmlString;
        String type;

        public String getHtmlString() {
            return htmlString;
        }

        @XmlElement
        public void setHtmlString(final String htmlString) {
            this.htmlString = htmlString;
        }

        public String getType() {
            return type;
        }

        @XmlAttribute
        public void setType(final String type) {
            this.type = type;
        }
    }
}
