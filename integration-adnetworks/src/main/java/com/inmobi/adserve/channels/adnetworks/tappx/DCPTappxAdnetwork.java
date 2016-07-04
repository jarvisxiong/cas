package com.inmobi.adserve.channels.adnetworks.tappx;

import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
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

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by deepak.jha on 5/10/16.
 */
public class DCPTappxAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final String KEY = "key";
    private static final String SIZE = "sz";
    private static final String OS = "os";
    private static final String CB = "cb";
    private static final String AID = "aid";
    private static final String AIDL = "aidl";
    private static final String IP = "ip";
    private static final String UA = "ua";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String OV = "ov";
    private static final String MN = "mn";
    private static final String MO = "mo";
    private static final String CT = "ct";
    private static final String WIFI = "wifi";
    private static final String CARRIER = "carrier";
    private static final String E = "e";
    private static final String TEST = "test";

    private static final Logger LOG = LoggerFactory.getLogger(DCPTappxAdnetwork.class);

    private String latitude;
    private String longitude;
    private int width = 0;
    private int height = 0;
    private String isTest;

    public DCPTappxAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
                             final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandate parameters missing for tappx so exiting adapter");
            return false;
        }
        host = config.getString("tappx.host");
        if (casInternalRequestParameters.getLatLong() != null
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) dim.getWidth();
            height = (int) dim.getHeight();

        }else{
            LOG.debug("Cant find height and width for slot {} in Tappx Adapter. Hence exiting.", selectedSlotId);
            return false;
        }
        LOG.debug("Configure parameters inside tappx returned true");
        return true;
    }

    @Override
    public String getName() {
        return "tappxDCP";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            URIBuilder builder = new URIBuilder(host);
            String slotSize = width+"x"+height;
            String operatingSystem = "";
            if(sasParams.getOsId() == SASRequestParameters.HandSetOS.Android.getValue()){
                operatingSystem = "android";
            }else if(sasParams.getOsId() == SASRequestParameters.HandSetOS.iOS.getValue()) {
                operatingSystem = "ios";
            }
            builder.addParameter(KEY, externalSiteId);
            builder.addParameter(SIZE, slotSize);
            builder.addParameter(OS, operatingSystem);
            builder.addParameter(CB, casInternalRequestParameters.getImpressionId());

            final String ifa = getUidIFA(false);
            final String gpid = getGPID(false);
            if(StringUtils.isNotEmpty(ifa)){
                builder.addParameter(AID,ifa);
            }else if(StringUtils.isNotEmpty(gpid)){
                builder.addParameter(AID,gpid);
            }

            final String aidl = casInternalRequestParameters.isTrackingAllowed() ? "0" : "1";
            builder.addParameter(AIDL, aidl);
            builder.addParameter(IP, sasParams.getRemoteHostIp());
            builder.addParameter(UA, getURLEncode(sasParams.getUserAgent(), format));
            if(StringUtils.isNotEmpty(latitude)) {
                builder.addParameter(LAT, latitude);
            }
            if(StringUtils.isNotEmpty(longitude)) {
                builder.addParameter(LON, longitude);
            }
            if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
                builder.addParameter(OV, sasParams.getOsMajorVersion());
            }
            if(StringUtils.isNotEmpty(sasParams.getDeviceMake())) {
                builder.addParameter(MN, sasParams.getDeviceMake());
            }
            if(StringUtils.isNotEmpty(sasParams.getDeviceModel())) {
                builder.addParameter(MO, sasParams.getDeviceModel());
            }
            if (ConnectionType.WIFI == sasParams.getConnectionType()) {
                builder.addParameter(CT, WIFI);
            } else {
                builder.addParameter(CT, CARRIER);
            }
            isTest = config.getString("tappx.istest");
            builder.addParameter(E, "1");
            builder.addParameter(TEST, isTest);

            URI uri = builder.build();
            LOG.debug("Tappx url is {}", uri);
            return uri;
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("tappx response is {}", response);
        if (StringUtils.isEmpty(response) || status.code() != 200 || response.contains("ERROR")) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            buildInmobiAdTracker();
            try {
                statusCode = status.code();
                final VelocityContext context = new VelocityContext();
                Formatter.TemplateType t = Formatter.TemplateType.HTML;
                context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response);
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, getBeaconUrl());
                adStatus = AD_STRING;
                LOG.debug("Ad returned from Tappx adapter is {}",responseContent);
                LOG.debug("response length is {}", responseContent.length());
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from tappx: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
        }
    }

    @Override
    public String getId() {
        return config.getString("tappx.advertiserId");
    }
}