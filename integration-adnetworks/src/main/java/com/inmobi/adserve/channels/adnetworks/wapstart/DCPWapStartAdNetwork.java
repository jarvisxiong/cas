package com.inmobi.adserve.channels.adnetworks.wapstart;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;


public class DCPWapStartAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPWapStartAdNetwork.class);

    private static final String LOGIN = "login";
    private static final String PROVIDER = "wapstart";
    private String latitude = null;
    private String longitude = null;
    private int width;
    private int height;
    private String adid = null;
    private String udid;



    public DCPWapStartAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);

    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for wapstart so exiting adapter");
            LOG.info("Configure parameters inside wapstart returned false");
            return false;
        }
        host = config.getString("wapstart.host");

        if (repositoryHelper.querySlotSizeMapRepository(selectedSlotId) != null) {
            if(selectedSlotId == 9 || selectedSlotId == 15 || selectedSlotId == 24) {
                selectedSlotId = 4;
            }
            final Dimension dim = repositoryHelper.querySlotSizeMapRepository(selectedSlotId).getDimension();
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        } else {
            LOG.debug("mandate parameters missing for WapStart, so returning from adapter");
            LOG.info("Configure parameters inside wapstart returned false");
            return false;
        }

        if (casInternalRequestParameters.getLatLong() != null
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];

        }

        udid = getUid();
        if (StringUtils.isBlank(udid)) {
            LOG.debug("Udid mandatory for Wapstart");
            LOG.info("Configure parameters inside wapstart returned false");
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "wapstart";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder(String.format(host, externalSiteId));
            return new URI(url.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    private String getRequestParams() {
        final User user = new User();
        final String gender = sasParams.getGender();
        if (StringUtils.isNotBlank(gender)) {
            final int gen = "F".equalsIgnoreCase(gender) ? 2 : 1;
            user.setGender(gen);
        }
        if (sasParams.getAge() != null) {
            final int age = sasParams.getAge();
            final int year = Calendar.getInstance().get(Calendar.YEAR);
            final int yob = year - age;
            user.setYob(yob);
        }

        final WapstartData data = new WapstartData();
        final Segment segment = new Segment();
        segment.setName(LOGIN);
        segment.setValue(udid);
        data.setSegment(segment);
        data.setName(PROVIDER);
        user.setData(data);


        final Geo geo = new Geo();
        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
            geo.setLat(latitude);
            geo.setLon(longitude);
        }
        if (StringUtils.isNotBlank(sasParams.getCountryCode())) {
            geo.setCountry(sasParams.getCountryCode());
        }
        final Device device = new Device();
        device.setIp(sasParams.getRemoteHostIp());
        device.setUa(sasParams.getUserAgent());
        final String gpid = getGPID();
        if (gpid != null) {
            adid = gpid;
            device.setAdid(adid);

        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
            device.setAndroid_id(casInternalRequestParameters.getUidMd5());
        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
            device.setAndroid_id(casInternalRequestParameters.getUidO1());
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidIFA())
                && "1".equals(casInternalRequestParameters.getUidADT())) {
            device.setIfa(casInternalRequestParameters.getUidIFA());
        }
        device.setGeo(geo);

        final Publisher publisher = new Publisher();
        publisher.setName(blindedSiteId);
        publisher.setId(sasParams.getSiteIncId());

        final Site site = new Site();
        site.setId(Integer.parseInt(externalSiteId));
        site.setPublisher(publisher);
        site.setCtype(1);

        final Banner banner = new Banner();
        banner.setH(height);
        banner.setW(width);
        // 5:MRAID 2
        banner.setApi(5);
        // Banner type 1: Text and Graphic
        banner.setBtype(1);
        final Impression impression = new Impression();
        final Banner[] banners = new Banner[1];
        banners[0] = banner;
        impression.setBanner(banners);

        final WapStartAdrequest adRequest = new WapStartAdrequest();
        adRequest.setDevice(device);
        adRequest.setImpression(impression);
        adRequest.setSite(site);
        adRequest.setUser(user);

        final ObjectMapper mapper = new ObjectMapper();

        try {

            final String requestBody = mapper.writeValueAsString(adRequest);
            LOG.debug(requestBody);
            return requestBody;
        } catch (final JsonProcessingException e) {
            LOG.info("{}", e);
        }
        return null;

    }

    @Override
    public Request getNingRequest() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        final String requestParams = getRequestParams();
        final Request ningRequest =
                new RequestBuilder("POST").setUrl(uri.toString())
                        .setHeader("x-display-metrics", String.format("%sx%s", width, height))
                        .setHeader("xplus1-user-agent", sasParams.getUserAgent())
                        .setHeader("x-plus1-remote-addr", sasParams.getRemoteHostIp())
                        .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                        .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                        .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                        .setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
                        .setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(requestParams.length()))
                        .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                        .setHeader(HttpHeaders.Names.HOST, uri.getHost()).setBody(requestParams).build();
        LOG.debug("WapStart request: {}", ningRequest);
        LOG.debug("WapStart request Body: {}", requestParams);
        return ningRequest;
    }



    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("Wapstart response is {} and response length is {} and status code is {}", response,
                response.length(), status);
        if (status.code() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        try {
            final JSONObject responseJson = new JSONObject(response).getJSONArray("seat").getJSONObject(0);
            TemplateType t;
            final VelocityContext context = new VelocityContext();
            String partnerClickUrl = null;
            if (responseJson.has("clink")) {
                partnerClickUrl = responseJson.getString("clink");
            } else {
                adStatus = "NO_AD";
                statusCode = 500;
                return;
            }
            context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, partnerClickUrl);
            context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);
            context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, responseJson.getString("vlink"));
            if (responseJson.has("graphic")) {
                final JSONObject textGraphic = responseJson.getJSONObject("graphic").getJSONObject("picture");
                final String imageUrl = textGraphic.getString("name");
                context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, imageUrl);
                t = TemplateType.IMAGE;
            } else if (responseJson.has("textgraphic")) {
                final JSONObject textGraphic = responseJson.getJSONObject("textgraphic").getJSONObject("picture");
                final String imageUrl = textGraphic.getString("name");
                context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, imageUrl);
                t = TemplateType.IMAGE;
            }else if (responseJson.has("text")) {
                final JSONObject text = responseJson.getJSONObject("text");
                context.put(VelocityTemplateFieldConstants.AD_TEXT, text.getString("title"));
                if (text.has("content")) {
                    context.put(VelocityTemplateFieldConstants.DESCRIPTION, text.getString("content"));
                }
                final String vmTemplate = Formatter.getRichTextTemplateForSlot(selectedSlotId.toString());
                if (StringUtils.isEmpty(vmTemplate)) {
                    t = TemplateType.PLAIN;
                } else {
                    context.put(VelocityTemplateFieldConstants.TEMPLATE, vmTemplate);
                    t = TemplateType.RICH;
                }

            } else {
                adStatus = "NO_AD";
                statusCode = 500;
                return;
            }
            responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
            adStatus = "AD";
            statusCode = 200;
        } catch (final Exception exception) {
            adStatus = "NO_AD";
            LOG.info("Error parsing response {} from Wapstart: {}", response, exception);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            return;
        }
        LOG.debug("response length is {}", responseContent.length());
    }


    @Override
    public String getId() {
        return config.getString("wapstart.advertiserId");
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }
}
