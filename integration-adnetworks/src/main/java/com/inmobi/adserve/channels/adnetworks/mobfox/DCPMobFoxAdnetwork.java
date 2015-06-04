package com.inmobi.adserve.channels.adnetworks.mobfox;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;

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
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.config.GlobalConstant;


public class DCPMobFoxAdnetwork extends AbstractDCPAdNetworkImpl {

    protected static final String LAT = "latitude";
    protected static final String LONG = "longitude";
    protected static final String GENDER = "demo.gender";
    protected static final String KEYWORDS = "demo.keywords";
    protected static final String AGE = "demo.age";

    private static final Logger LOG = LoggerFactory.getLogger(DCPMobFoxAdnetwork.class);

    private static final String PUBID = "s";
    private static final String UA = "u";
    private static final String CLIENT_IP = "i";
    private static final String TRAFFICTYPE = "m";
    private static final String MRAIDSUPPORT = "c_mraid";
    private static final String SHA1UDID = "o_mcsha1";
    private static final String MD5UDID = "o_mcmd5";
    private static final String IFA = "o_iosadvid";
    private static final String VERSION = "v";

    private static final String WIDTH = "adspace.width";
    private static final String HEIGHT = "adspace.height";
    private static final String B_SITE_ID = "s_subid";
    private static final String REQUEST_TYPE = "rt";

    private static final String TYPE = "live";
    private static final String MRAID_TYPE = GlobalConstant.ONE;
    private static final String API_VERSION = "2.0";
    private static final String REQUEST_TYPE_VALUE = "api";

    private transient String latitude;
    private transient String longitude;
    private int width;
    private int height;


    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPMobFoxAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);

    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for mobfox so exiting adapter");
            LOG.info("Configure parameters inside Mobfox returned false");
            return false;
        }
        host = config.getString("mobfox.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
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

        return true;
    }

    @Override
    public String getName() {
        return "mobfoxDCP";
    }

    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder url = new StringBuilder(host);
        appendQueryParam(url, REQUEST_TYPE, REQUEST_TYPE_VALUE, true);
        appendQueryParam(url, PUBID, externalSiteId, false);
        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
        appendQueryParam(url, CLIENT_IP, sasParams.getRemoteHostIp(), false);
        // TODO is [p(url for sites) required?]
        appendQueryParam(url, TRAFFICTYPE, TYPE, false);
        appendQueryParam(url, MRAIDSUPPORT, MRAID_TYPE, false);
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA())) {
            appendQueryParam(url, IFA, casInternalRequestParameters.getUidIFA(), false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidSO1())) {
            appendQueryParam(url, SHA1UDID, casInternalRequestParameters.getUidSO1(), false);
        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
            appendQueryParam(url, SHA1UDID, casInternalRequestParameters.getUidO1(), false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
            appendQueryParam(url, MD5UDID, casInternalRequestParameters.getUidMd5(), false);
        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
            appendQueryParam(url, MD5UDID, casInternalRequestParameters.getUid(), false);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
            appendQueryParam(url, SHA1UDID, casInternalRequestParameters.getUidIDUS1(), false);
        } else {
            final String gpid = getGPID();
            if (gpid != null) {
                url.append("&o_andadvid=").append(gpid);
            }
        }
        appendQueryParam(url, VERSION, API_VERSION, false);
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
        if (null != sasParams.getAge()) {
            appendQueryParam(url, AGE, sasParams.getAge().toString(), false);
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
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            statusCode = status.code();
            final VelocityContext context = new VelocityContext();
            try {
                final Request request = jaxbHelper.unmarshal(response, Request.class);
                final String htmlContent = request.getHtmlString();
                if (StringUtils.isBlank(htmlContent)) {
                    adStatus = NO_AD;
                    statusCode = 500;
                    responseContent = DEFAULT_EMPTY_STRING;
                    return;
                }
                buildInmobiAdTracker();
                context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, htmlContent);

                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams,
                        getBeaconUrl());
                adStatus = AD_STRING;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from Mobfox: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }
    }

    @Override
    public String getId() {
        return config.getString("mobfox.advertiserId");
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
