package com.inmobi.adserve.channels.adnetworks.onedigitalad;

import com.google.gson.Gson;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.BaseAdNetworkHelper;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.contracts.oneDigitalAd.request.App;
import com.inmobi.adserve.contracts.oneDigitalAd.request.Imp;
import com.inmobi.adserve.contracts.oneDigitalAd.request.Device;
import com.inmobi.adserve.contracts.oneDigitalAd.request.Deviceids;
import com.inmobi.adserve.contracts.oneDigitalAd.request.Geo;
import com.inmobi.adserve.contracts.oneDigitalAd.request.BidRequest;
import com.inmobi.types.DeviceType;
import com.ning.http.client.RequestBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.MD5;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.SHA1;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.UTF_8;

/**
 * Created by deepak.jha on 28/6/16.
 */
public class DCPOneDigitalAdNetwork extends AbstractDCPAdNetworkImpl{

    private static Gson gson;
    private static java.util.List<Integer> fsBlockedAttributes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13,
            14, 15, 16);
    private static java.util.List<Integer> performanceBlockedAttributes =
            Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
    private int height;
    private int width;
    private String bidRequestJson = DEFAULT_EMPTY_STRING;
    private static final int DEVICE_TYPE_PHONE = 4;
    private static final int DEVICE_TYPE_TABLET = 5;
    private static final String BLIND_BUNDLE_APP_FORMAT = "com.ix.%s";
    private static final String BLIND_STORE_URL_FORMAT = "http://www.ix.com/%s";
    private static final String X_OPENRTB_VERSION = "x-openrtb-version";
    private static final String rtbVer = "2.0";
    private Double floor;
    private static JSONObject additionalParams;

    @Getter
    @Setter
    BidRequest bidRequest;
    protected boolean isCoppaSet = false;
    private static final Logger LOG = LoggerFactory.getLogger(DCPOneDigitalAdNetwork.class);


    public DCPOneDigitalAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
                                  final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        gson = new Gson();

    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for DCPOneDigitalAd so exiting adapter");
            LOG.info("Configure parameters inside DCPOneDigitalAd returned false");
            return false;
        }
        if(StringUtils.isEmpty(getUidIFA(false))&&StringUtils.isEmpty(getGPID(false))){
            LOG.debug("Device id is not found for the request. DCPOneDigitalAd adapter is exiting.");
            return false;
        }
        host = config.getString("onedigitalad.host");
        if (sasParams.getWapSiteUACEntity() != null) {
            wapSiteUACEntity = sasParams.getWapSiteUACEntity();
            isWapSiteUACEntity = true;
        }

        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) dim.getWidth();
            height = (int)dim.getHeight();
        }
        additionalParams = getEntity().getAdditionalParams();
        if (additionalParams.length()!=0 ) {
            try {
                floor = Double.valueOf(additionalParams.getString("floor"));
            } catch (JSONException e) {
                LOG.debug("Floor price is not found for the segment with external site key : {}", externalSiteId);
            }
        }
        Imp imp = createImpObject();
        App app = createAppObject();
        Device device = createDeviceObject();
        Geo geo = createGeoObject();
        Deviceids deviceIds = createDeviceIdsObject();

        final boolean flag = createBidRequestObject(imp, app, device, geo, deviceIds);
        if (!flag) {
            return false;
        }
        return serializeBidRequest(); // Serializing the bidRequest Object
    }

    private Imp createImpObject(){
        Imp impObject = new Imp();
        if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
            impObject.setBattr(performanceBlockedAttributes);
        } else {
            impObject.setBattr(fsBlockedAttributes);
        }
        impObject.setW(width);
        impObject.setH(height);
        return impObject;
    }

    private App createAppObject() {
        final App appObject = new App();
        appObject.setId(blindedSiteId);
        if (null != sasParams.getCategories()) {
            appObject.setCat(IABCategoriesMap.getIABCategories(sasParams.getCategories()));
        }
        final String blindBundle = String.format(BLIND_BUNDLE_APP_FORMAT, blindedSiteId);
        appObject.setApp(blindBundle);
        final String storeUrl = String.format(BLIND_STORE_URL_FORMAT, blindedSiteId);
        appObject.setBundleurl(storeUrl);
        if(floor != null){
            appObject.setPrice(floor);
        }else {
            floor=0.1;
            appObject.setPrice(floor);
        }
        return appObject;
    }

    private Device createDeviceObject(){
        Device deviceObject = new Device();
        final ConnectionType sasParamConnectionType = sasParams.getConnectionType();
        if (sasParamConnectionType != null) {
            switch (sasParamConnectionType.getValue()){
                case 0:
                    deviceObject.setConnectiontype(0);
                    break;
                case 1:
                    deviceObject.setConnectiontype(0);
                    break;
                case 2:
                    deviceObject.setConnectiontype(3);
                    break;
                case 3:
                    deviceObject.setConnectiontype(0);
                    break;
                case 4:
                    deviceObject.setConnectiontype(1);
                    break;
                case 5:
                    deviceObject.setConnectiontype(2);
                    break;
                case 6:
                    deviceObject.setConnectiontype(4);
                    break;
                default:
                    deviceObject.setConnectiontype(0);
                    break;
            }
        }else{
            deviceObject.setConnectiontype(0);
        }

        final Integer sasParamsOsId = sasParams.getOsId();
        if (sasParamsOsId > 0 && sasParamsOsId < 21) {
            deviceObject.setOs(SASRequestParameters.HandSetOS.values()[sasParamsOsId - 1].toString());
        }

        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
            deviceObject.setOsv(sasParams.getOsMajorVersion());
        }

        if (DeviceType.TABLET == sasParams.getDeviceType()) {
            deviceObject.setDevicetype(DEVICE_TYPE_TABLET);
        } else {
            deviceObject.setDevicetype(DEVICE_TYPE_PHONE); // SmartPhones and FeaturePhones
        }

        deviceObject.setModel(sasParams.getDeviceModel());
        deviceObject.setMake(sasParams.getDeviceMake());
        deviceObject.setIp(sasParams.getRemoteHostIp());
        deviceObject.setUa(sasParams.getUserAgent());
        return deviceObject;
    }

    private Geo createGeoObject(){
        Geo geoObject = new Geo();
        if (null != sasParams.getCountryCode()) {
            geoObject.setCountry(IABCountriesMap.getIabCountry(sasParams.getCountryCode()));
        }
        geoObject.setRegion(DEFAULT_EMPTY_STRING);
        geoObject.setCity(DEFAULT_EMPTY_STRING);
        geoObject.setZip(casInternalRequestParameters.getZipCode());
        if (!isCoppaSet && StringUtils.isNotBlank(casInternalRequestParameters.getLatLong())
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            geoObject.setLat(String.format("%.4f", Double.parseDouble(latlong[0])));
            geoObject.setLon(String.format("%.4f", Double.parseDouble(latlong[1])));
        }
        geoObject.setGps(true);
        geoObject.setJs(true);
        geoObject.setIp(sasParams.getRemoteHostIp());
        return geoObject;
    }

    private Deviceids createDeviceIdsObject(){
        Deviceids deviceIdsObject = new Deviceids();
        String id;
        if (StringUtils.isNotEmpty(id = getUidIFA(false))) {
            deviceIdsObject.setIdfa(id);
            deviceIdsObject.setIdfasha1(BaseAdNetworkHelper.getHashedValue(id, SHA1));
            deviceIdsObject.setIdafamd5(BaseAdNetworkHelper.getHashedValue(id, MD5));

        } else if (StringUtils.isNotEmpty(id = getGPID(false))) {
            deviceIdsObject.setAifa(id);
            deviceIdsObject.setAifash1(BaseAdNetworkHelper.getHashedValue(id, SHA1));
            deviceIdsObject.setAifamd5(BaseAdNetworkHelper.getHashedValue(id, MD5));
        }
        return deviceIdsObject;
    }

    private boolean createBidRequestObject(final Imp imp, final App app, final Device device,
                                           final Geo geo, final Deviceids deviceIds) {
        bidRequest = new BidRequest();
        if (null != casInternalRequestParameters.getImpressionId()) {
            bidRequest.setReqId(casInternalRequestParameters.getImpressionId());
        } else {
            LOG.info(traceMarker, "Impression id can not be null in casInternal Request Params");
            return false;
        }
        bidRequest.setStsid(blindedSiteId);
        bidRequest.setImp(imp);
        bidRequest.setApp(app);
        bidRequest.setDevice(device);
        bidRequest.setGeo(geo);
        bidRequest.setDeviceids(deviceIds);
        return true;
    }

    private boolean serializeBidRequest() {
        try {
            bidRequestJson = gson.toJson(bidRequest);
            LOG.info(traceMarker, "OneDigitalAd request json is: {}", bidRequestJson);
            return true;
        } catch (final Exception e) {
            LOG.debug(traceMarker, "Could not create json from bidRequest for partner OneDigitalAd");
            LOG.info(traceMarker, "Configure parameters inside OneDigitalAd returned false , exception thrown {}", e);
            return false;
        }
    }

    @Override
    protected RequestBuilder getNingRequestBuilder() throws Exception {
        final byte[] body = bidRequestJson.getBytes(CharsetUtil.UTF_8);

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }
        final String httpRequestMethod = POST;
        return new RequestBuilder(httpRequestMethod).setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(body).setBodyEncoding(UTF_8)
                .setHeader(X_OPENRTB_VERSION, rtbVer).setHeader(HttpHeaders.Names.HOST, uri.getHost());
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            LOG.debug("DCPOneDigitalAdD url is {}", host);
            return new URI(host);
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    public String getId() {
        return config.getString("onedigitalad.advertiserId");
    }

    @Override
    public String getName() {
        return "onedigitaladDCP";
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("OneDigitalAd Response is {}", response);
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

                String priceOda = adResponse.getString("price");
                if (Double.valueOf(priceOda) < floor) {
                    InspectorStats.incrementStatCount(getName(), InspectorStrings.BID_FLOOR_TOO_LOW);
                    LOG.debug("Response shared by OneDigital is having less price than floor for request id: {}", casInternalRequestParameters.getImpressionId());
                }

                context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, adResponse.get("adm"));
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, getBeaconUrl());
                adStatus = AD_STRING;
                LOG.debug("Ad returned from OneDigitalAd adapter is {}",responseContent);
                LOG.debug("response length is {}", responseContent.length());
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from OneDigitalAd: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }
    }

}


