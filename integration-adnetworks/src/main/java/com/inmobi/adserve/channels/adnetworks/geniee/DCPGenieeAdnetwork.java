package com.inmobi.adserve.channels.adnetworks.geniee;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.RequestBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created by deepak.jha on 6/27/16.
 */
public class DCPGenieeAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final String ZONEID = "zoneid";
    private static final String IP = "ip";
    private static final String UA = "ua";
    private static final String ADTK = "adtk";
    private static final String ACCEPT_LANGUAGE= "accept_lang";
    private static final String IDFA = "idfa";
    private static final String BUYER_ID = "buyer_uid";
    private static final String APID = "apid";
    private static final String CARR = "carr";
    private static final String DVMD = "dvmd";
    private static final String LATI = "lati";
    private static final String LONG = "long";
    private static final String DFM = "dfm";
    private static final String TPAF = "tpaf";
    private transient String latitude;
    private transient String longitude;



    private static final Logger LOG = LoggerFactory.getLogger(DCPGenieeAdnetwork.class);


    public DCPGenieeAdnetwork(Configuration config, Bootstrap clientBootstrap,
                                 HttpRequestHandlerBase baseRequestHandler, Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
            || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for geniee so exiting adapter");
            LOG.info("Configure parameters inside geniee returned false");
            return false;
        }
        host = config.getString("geniee.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
            && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
       return true;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return "geniee";
    }

    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder url = new StringBuilder(host);
        appendQueryParam(url, ZONEID, "1077865", true);
        appendQueryParam(url, IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(),"UTF-8"), false);
        appendQueryParam(url, ADTK, casInternalRequestParameters.isTrackingAllowed() ? 0 : 1, false);
        appendQueryParam(url, ACCEPT_LANGUAGE, "en-us", false);
        String idfa = getUidIFA(false);
        final String gpId = getGPID(false);
        appendQueryParam(url, IDFA, StringUtils.isNotBlank(idfa) ? idfa : StringUtils.isNotBlank(gpId) ? gpId : "", false);
//        appendQueryParam(url, BUYER_ID, "I have to ask", false);
        appendQueryParam(url, APID, blindedSiteId, false);
        appendQueryParam(url, CARR, sasParams.getCarrierId(), false);
        appendQueryParam(url, DVMD, sasParams.getDeviceModel(), false);
        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
            appendQueryParam(url, LATI, latitude, false);
            appendQueryParam(url, LONG, longitude, false);
        }
        appendQueryParam(url, DFM, 1, false);
        appendQueryParam(url, TPAF, 0, false);
        return new URI(url.toString());
    }


    @Override
    protected RequestBuilder getNingRequestBuilder() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        return new RequestBuilder().setUrl(uri.toString())
            .setHeader(HttpHeaders.Names.HOST, uri.getHost())
            .setHeader(HttpHeaders.Names.CONNECTION, "keep-alive")
            .setHeader(HttpHeaders.Names.ACCEPT, "application/json")
            .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
            .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us");
    }


    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("Geniee Response is {}", response);
        if (StringUtils.isEmpty(response) || status.code() != 200) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
        } else {
            buildInmobiAdTracker();
            try {
                statusCode = status.code();
                JSONObject adResponse = new JSONObject(response);
                final VelocityContext context = new VelocityContext();
                Formatter.TemplateType t = Formatter.TemplateType.HTML;
                context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, adResponse.get("adm"));
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, getBeaconUrl());
                adStatus = AD_STRING;
                LOG.debug("Ad returned from Geniee adapter is {}",responseContent);
                LOG.debug("response length is {}", responseContent.length());
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from Geniee: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }
    }


}
