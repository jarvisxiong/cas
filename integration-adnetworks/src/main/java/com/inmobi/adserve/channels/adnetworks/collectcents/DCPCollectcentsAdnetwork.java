package com.inmobi.adserve.channels.adnetworks.collectcents;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.RequestBuilder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DCPCollectcentsAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPCollectcentsAdnetwork.class);

    private short adSize;
    private String latitude = null;
    private String longitude = null;
    private String pubId;

    private static final String BANNER = "banner";
    private static final String HTML = "HTML";
    private static final String LOW_MATURITY = "UA";
    private static final String MEDIUM_MATURITY = "A";
    private static final String APPTYPE = "app";
    private static final String WAPTYPE = "wap";
    private static final String NOAD = "<!-- Collectcent:";


    private boolean isApp;

    public DCPCollectcentsAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);

    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for collectcents so exiting adapter");
            return false;
        }
        host = config.getString("collectcents.host");
        pubId = config.getString("collectcents.pubid");
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity slotSizeMapEntity =
                repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            adSize = slotSizeMapEntity.getSlotId();
        } else {
            LOG.debug("mandate parameters missing for Collectcents, so returning from adapter");
            LOG.info("Configure parameters inside Collectcents returned false");
            return false;
        }

        if (null != casInternalRequestParameters.getLatLong()
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {

            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];

        }

        final String udid = getUid(true);
        if (null == udid) {
            LOG.debug("mandate parameters missing for Collectcents, so returning from adapter");
            LOG.info("Configure parameters inside Collectcents returned false");
            return false;
        }
        isApp =
                StringUtils.isBlank(sasParams.getSource()) || WAP.equalsIgnoreCase(sasParams.getSource())
                        ? false
                        : true;

        LOG.info("Configure parameters inside collectcents returned true");
        return true;
    }

    @Override
    public String getName() {
        return "collectcentsDCP";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            return new URI(host);
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    private String getRequestParams() {

        final MainRequest mainReq = new MainRequest();
        mainReq.setPubid(pubId);
        mainReq.setAds(1);
        mainReq.setAdtype(BANNER);
        mainReq.setResponse(HTML);

        final Banner banner = new Banner();
        banner.setAdsize(adSize);
        mainReq.setBanner(banner);

        final List<MainRequest> reqArray = new ArrayList<MainRequest>();
        reqArray.add(mainReq);

        final CollectcentsRequest request = new CollectcentsRequest();
        request.setMain(reqArray);
        request.setResponseformat(HTML);
        final Site site = new Site();
        site.setId(blindedSiteId);
        if (sasParams.getSiteContentType() == ContentType.FAMILY_SAFE) {
            site.setRated(LOW_MATURITY);
        } else {
            site.setRated(MEDIUM_MATURITY);
        }
        final String category = getCategories(',', false);
        if (category != null) {
            site.setCategory(category.split(",")[0]);
        }
        request.setSite(site);

        final Device device = new Device();

        final int sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            device.setOs(HandSetOS.values()[sasParamsOsId - 1].toString());
        }
        device.setIp(sasParams.getRemoteHostIp());
        if (ConnectionType.WIFI == sasParams.getConnectionType()) {
            device.setConntype("wifi");
        }
        device.setUa(sasParams.getUserAgent());
        final String uid = getUid(true);
        if (uid != null) {
            device.setDeviceid(uid);
        }
        if (isApp) {
            device.setType(APPTYPE);
        } else {
            device.setType(WAPTYPE);
        }

        final Geo geo = new Geo();
        geo.setGeolat(latitude);
        geo.setGeolong(longitude);
        device.setGeo(geo);

        final User user = new User();

        if (sasParams.getGender() != null) {
            user.setGender(sasParams.getGender());
        }

        final int yob = getYearofBirth();
        if (yob != -1) {
            user.setYob(yob);
        }

        request.setUser(user);

        request.setDevice(device);

        final ObjectMapper mapper = new ObjectMapper();

        try {

            final String requestBody = mapper.writeValueAsString(request);
            LOG.debug(requestBody);
            return requestBody;
        } catch (final JsonProcessingException e) {
            LOG.error("{}", e);
        }
        return null;

    }

    @Override
    public RequestBuilder getNingRequestBuilder() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        final String requestParams = getRequestParams();
        final RequestBuilder ningRequestBuilder =
                new RequestBuilder(POST).setUrl(uri.toString())
                        .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                        .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                        .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                        .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
                        .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                        .setHeader(HttpHeaders.Names.HOST, uri.getHost()).setBody(requestParams);
        LOG.debug("Collectcents request: {}", ningRequestBuilder);
        LOG.debug("Collectcents request Body: {}", requestParams);
        return ningRequestBuilder;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);

        if (null == response || status.code() != 200 || response.trim().isEmpty() || response.startsWith(NOAD)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            statusCode = status.code();
            final VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response.trim());
            buildInmobiAdTracker();

            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams,
                        getBeaconUrl());
                adStatus = AD_STRING;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from Collectcent: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
    }

    @Override
    public String getId() {
        return config.getString("collectcents.advertiserId");
    }

}
