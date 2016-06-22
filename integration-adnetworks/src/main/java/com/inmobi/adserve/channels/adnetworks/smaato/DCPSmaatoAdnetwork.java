package com.inmobi.adserve.channels.adnetworks.smaato;

import java.awt.Dimension;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
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
import com.ning.http.client.RequestBuilder;
import com.smaato.soma.oapi.Response;
import com.smaato.soma.oapi.Response.Ads.Ad;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;


public class DCPSmaatoAdnetwork extends AbstractDCPAdNetworkImpl {

    protected static final String GPS = "gps";
    protected static final String KWS = "kws";
    protected static final String AGE = "age";

    private static final Logger LOG = LoggerFactory.getLogger(DCPSmaatoAdnetwork.class);

    private static final String PUB = "pub";
    private static final String DEVICE = "device";
    private static final String MRAIDVER = "mraidver";
    private static final String MRAIDVERSION = "2";
    private static final String DEVIP = "devip";
    private static final String DIVID = "divid";
    private static final String ADSPACE = "adspace";
    private static final String DEVICEMODEL = "devicemodel";
    private static final String DEVICEMAKE = "devicemake";

    private static final String IOSADID = "iosadid";
    private static final String IOSADTRACKING = "iosadtracking";
    private static final String GOOGLEADID = "googleadid";
    private static final String GOOGLEDNT = "googlednt";
    private static final String ANDROIDID = "androidid";
    private static final String APIVER = "apiver";
    private static final String APIVERSION = "501";
    private static final String RESPONSE = "response";
    private static final String RESPONSE_TYPE = "XML";
    private static final String COPPA = "coppa";
    private static final short AGE_LIMIT_FOR_COPPA = 8;

    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String FORMAT = "format";
    private static final String FORMAT_STRICT = "formatstrict";
    private static final String DIMENSION = "dimension";
    private static final String DIMENSION_STRICT = "dimensionstrict";
    private static final String SUCCESS = "success";
    private static final String IMAGE_TYPE = "IMG";
    private static final String TEXT_TYPE = "TXT";
    private static final String RICHMEDIA_TYPE = "RICHMEDIA";

    private static final String RESPONSE_FORMAT = "all";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String LAT_LONG_FORMAT = "%s,%s";
    private static Map<Integer, String> slotIdMap;

    private final String publisherId;

    private transient String latitude;
    private transient String longitude;
    private int width;
    private int height;
    private String dimension;
    private boolean isApp;

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
        slotIdMap.put(16, "full_768x1024");
        slotIdMap.put(17, "full_800x1280");
    }

    public DCPSmaatoAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        publisherId = config.getString("smaato.pubId");
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for smaato so exiting adapter");
            LOG.info("Configure parameters inside Smaato returned false");
            return false;
        }
        host = config.getString("smaato.host");
        if (StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            dimension = slotIdMap.get(selectedSlotId.intValue());
            if (StringUtils.isBlank(dimension)) {
                LOG.debug("mandatory parameters missing for smaato so exiting adapter");
                LOG.info("Configure parameters inside Smaato returned false");
                return false;
            }
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());

        }
        if (StringUtils.isBlank(getUid(true))) {
            LOG.debug("mandatory parameters missing for smaato so exiting adapter");
            LOG.info("Configure parameters inside Smaato returned false");
            return false;
        }
        isApp =
            StringUtils.isBlank(sasParams.getSource()) || WAP.equalsIgnoreCase(sasParams.getSource())
                ? false
                : true;

        return true;
    }

    @Override
    public String getName() {
        return "smaatoDCP";
    }

    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder url = new StringBuilder(host);
        appendQueryParam(url, APIVER, APIVERSION, true);
        appendQueryParam(url, ADSPACE, externalSiteId, false);
        appendQueryParam(url, PUB, publisherId, false);
        appendQueryParam(url, DEVIP, sasParams.getRemoteHostIp(), false);
        if (!isApp) {
            appendQueryParam(url, DIVID, "smt-"+externalSiteId, false);
        }
        appendQueryParam(url, DEVICE, getURLEncode(sasParams.getUserAgent(), format), false);
        appendQueryParam(url, MRAIDVER, MRAIDVERSION, false);
        appendQueryParam(url, FORMAT, RESPONSE_FORMAT, false);
        appendQueryParam(url, FORMAT_STRICT, FALSE, false);
        appendQueryParam(url, DIMENSION, dimension, false);
        appendQueryParam(url, DIMENSION_STRICT, TRUE, false);
        final String ifa = getUidIFA(false);
        if (StringUtils.isNotBlank(ifa)) {
            appendQueryParam(url, IOSADID, ifa, false);
            appendQueryParam(url, IOSADTRACKING, casInternalRequestParameters.isTrackingAllowed() ? TRUE : FALSE, false);
        }

        final String gpId = getGPID(false);
        if (StringUtils.isNotBlank(gpId)) {
            appendQueryParam(url, GOOGLEADID, gpId, false);
            if (casInternalRequestParameters.isTrackingAllowed()) {
                appendQueryParam(url, GOOGLEDNT, FALSE, false);
            } else {
                appendQueryParam(url, GOOGLEDNT, TRUE, false);
            }
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
            appendQueryParam(url, ANDROIDID, casInternalRequestParameters.getUidMd5(), false);
        }

        appendQueryParam(url, RESPONSE, RESPONSE_TYPE, false);

        boolean isCoppaSet = isWapSiteUACEntity && wapSiteUACEntity.isCoppaEnabled() || sasParams.getAge() != null
            && sasParams.getAge() <= AGE_LIMIT_FOR_COPPA;
        int coppaValue = isCoppaSet?1:0;
        appendQueryParam(url, COPPA, coppaValue, false);

        if (height != 0) {
            appendQueryParam(url, HEIGHT, height + "", false);
        }
        if (width != 0) {
            appendQueryParam(url, WIDTH, width + "", false);
        }

        appendQueryParam(url, KWS, getURLEncode(getCategories(',', true, false), format), false);
        if (null != sasParams.getAge()) {
            appendQueryParam(url, AGE, sasParams.getAge().toString(), false);
        }

        if (StringUtils.isNotBlank(sasParams.getGender())) {
            appendQueryParam(url, GENDER, sasParams.getGender(), false);
        }

        if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
            appendQueryParam(url, GPS, getURLEncode(String.format(LAT_LONG_FORMAT, latitude, longitude), format),
                    false);
        }
        if (null != sasParams.getPostalCode()) {
            appendQueryParam(url, ZIP, sasParams.getPostalCode(), false);
        }

        if(sasParams.getDeviceModel()!=null){
            appendQueryParam(url, DEVICEMODEL, getURLEncode(sasParams.getDeviceModel(),"UTF-8"), false);
        }

        if(sasParams.getDeviceMake()!=null){
            appendQueryParam(url, DEVICEMAKE, getURLEncode(sasParams.getDeviceMake(),"UTF-8"), false);
        }
        LOG.debug("Smaato url is {}", url);
        return new URI(url.toString());
    }

    @Override
    protected RequestBuilder getNingRequestBuilder() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        return new RequestBuilder().setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("x-mh-User-Agent", sasParams.getUserAgent())
                .setHeader("x-mh-X-Forwarded-For", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.HOST, uri.getHost())
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp());
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
                final Response smaatoResponse = jaxbHelper.unmarshal(response, com.smaato.soma.oapi.Response.class);

                if (!SUCCESS.equalsIgnoreCase(smaatoResponse.getStatus()) || smaatoResponse.getAds().getAd() == null) {
                    adStatus = NO_AD;
                    statusCode = 500;
                    responseContent = DEFAULT_EMPTY_STRING;
                    return;
                }


                final Ad ad = smaatoResponse.getAds().getAd().get(0);



                TemplateType t = null;
                buildInmobiAdTracker();

                context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, ad.getAction().getTarget());
                context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());

                List<String> partnerBeacons = new ArrayList<>();
                for (int count = 0; count < ad.getBeacons().getBeacon().size(); count++) {
                    partnerBeacons.add(ad.getBeacons().getBeacon().get(count));
                }
                context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_LIST, partnerBeacons);
                // context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, ad.getBeacons().getBeacon());
                if (IMAGE_TYPE.equalsIgnoreCase(ad.getType()) && StringUtils.isNotBlank(ad.getLink())) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, ad.getLink());
                    t = TemplateType.IMAGE;
                } else if (TEXT_TYPE.equalsIgnoreCase(ad.getType()) && StringUtils.isNotBlank(ad.getAdtext())) {
                    context.put(VelocityTemplateFieldConstants.AD_TEXT, ad.getAdtext());
                    final String vmTemplate = Formatter.getRichTextTemplateForSlot(selectedSlotId.toString());
                    if (StringUtils.isEmpty(vmTemplate)) {
                        t = TemplateType.PLAIN;
                    } else {
                        context.put(VelocityTemplateFieldConstants.TEMPLATE, vmTemplate);
                        t = TemplateType.RICH;
                    }
                } else if (RICHMEDIA_TYPE.equalsIgnoreCase(ad.getType()) && null != ad.getMediadata()) {
                    context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, ad.getMediadata().toString());
                    t = TemplateType.HTML;
                } else {
                    adStatus = NO_AD;
                    return;
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, getBeaconUrl());
                adStatus = AD_STRING;
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from Smaato: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }
    }

    @Override
    public String getId() {
        return config.getString("smaato.advertiserId");
    }

}
