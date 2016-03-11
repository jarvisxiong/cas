package com.inmobi.adserve.channels.adnetworks.tencentdcp;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.template.interfaces.TemplateConfiguration;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;

/**
 * Created by thushara.v on 3/9/16.
 */
public class TencentAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final String CHANNEL = "channel";
    private static final String LOC_ID = "loc_id";
    private static final String IFA = "ifa";
    private static final String IMEI = "imei";
    private static final String HASHED_SITE_ID = "inmobi_site_id";
    private static final String SLOT = "slot";

    @Inject
    protected static TemplateConfiguration templateConfiguration;
    private final String advertiserName;
    private final String advertiserId;
    private final Gson gson;

    private String slotId;

    private static final Logger LOG = LoggerFactory.getLogger(TencentAdnetwork.class);

    public TencentAdnetwork(final Configuration config, final Bootstrap clientBootstrap, final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        this.advertiserName = "tencento2";
        advertiserId = config.getString(advertiserName + ".advertiserId");
        gson = templateConfiguration.getGsonManager().getGsonInstance();

    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for TencentDCP so exiting adapter");
            LOG.info("Configure parameters inside TencentDCP returned false");
            return false;
        }
        try {
            final JSONObject additionalParams = entity.getAdditionalParams();
            if (null != additionalParams) {
                slotId = additionalParams.getString(SLOT);
            }
        } catch (Exception exception) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.MISSING_ADDITIONAL_PARAMS);
            LOG.error("Slot Id is not configured for Tencent externalSiteId {}", externalSiteId);
        }
        host = config.getString(advertiserName + ".host");
        return true;
    }

    @Override
    public String getId() {
        return advertiserId;
    }

    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder requestBuilder = new StringBuilder(host);
        appendQueryParam(requestBuilder, CHANNEL, externalSiteId, true);
        appendQueryParam(requestBuilder, LOC_ID, slotId, false);
        if (StringUtils.isNotBlank(casInternalRequestParameters.getIem())) {
            appendQueryParam(requestBuilder, IMEI, casInternalRequestParameters.getIem(), false);
        } else {
            final String imei = getIMEI();
            if (StringUtils.isNotBlank(imei)) {
                appendQueryParam(requestBuilder, IMEI, imei, false);
            }
        }
        final String idfa = getUidIFA(true);
        if (null != idfa) {
            appendQueryParam(requestBuilder, IFA, idfa, false);
        }
        appendQueryParam(requestBuilder, HASHED_SITE_ID, blindedSiteId, false);
        return new URI(requestBuilder.toString());
    }

    @Override
    public void parseResponse(String response, HttpResponseStatus status) {
        adStatus = NO_AD;
        LOG.debug(traceMarker, "response is {}", response);
        if (status.code() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            statusCode = status.code();
            final TencentResponse tencentResponse = gson.fromJson(response, TencentResponse.class);
            if (tencentResponse.getRes_url() != null) {
                buildInmobiAdTracker();
                try {
                    final VelocityContext context = new VelocityContext();
                    context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, getClickUrl());
                    context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, tencentResponse.getPv_url());
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, tencentResponse.getDownload_url());
                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, tencentResponse.getRes_url());
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_BEACON, tencentResponse.getCv_url());
                    responseContent =
                            Formatter.getResponseFromTemplate(Formatter.TemplateType.IMAGE, context, sasParams, getBeaconUrl());
                    adStatus = AD_STRING;
                } catch (Exception exception) {
                    adStatus = NO_AD;
                    LOG.info("Error parsing response {} from Tenent: {}", response, exception);
                    InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                }
            } else {
                adStatus = NO_AD;
                responseContent = DEFAULT_EMPTY_STRING;
                statusCode = 500;
                LOG.info(traceMarker, "No Ad from Tencent");
                return;
            }
        }
    }

    @Override
    public String getName() {
        return advertiserName+DCP_KEY;
    }

}
