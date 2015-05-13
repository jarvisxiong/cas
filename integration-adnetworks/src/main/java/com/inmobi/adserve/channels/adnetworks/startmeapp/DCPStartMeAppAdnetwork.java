package com.inmobi.adserve.channels.adnetworks.startmeapp;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.config.GlobalConstant;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by deepak on 27/3/15.
 */
public class DCPStartMeAppAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPStartMeAppAdnetwork.class);
    private static final String APIVER = "apiver";
    private static final String PUBID = "pubid";
    private static final String ADSPACE = "adspace";
    private static final String RESPONSE = "response";
    private static final String SOURCE = "source";
    private static final String OS = "os";
    private static final String OSV = "osv";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String PDID = "pdid";
    private static final String KEYWORDS = "keywords";
    private static String os = null;
    private static String pubid;
    public static Integer height;
    public static Integer width;

    public DCPStartMeAppAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for startmeappdcp so exiting adapter");
            LOG.info("Configure parameters inside startmeappdcp returned false");
            return false;
        }
        host = config.getString("startmeappdcp.host");
        pubid = config.getString("startmeappdcp.pubid");
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
        return "startmeappdcp";
    }

    @Override
    public String getId() {
        return config.getString("startmeappdcp.advertiserId");
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            final StringBuilder url = new StringBuilder(host);
            appendQueryParam(url, APIVER, GlobalConstant.ONE, true);
            appendQueryParam(url, ADSPACE, externalSiteId, false);
            appendQueryParam(url, PUBID, pubid, false);
            appendQueryParam(url, RESPONSE, "JSON", false);
            appendQueryParam(url, SOURCE, sasParams.getSource(), false);
            appendQueryParam(url, IP, sasParams.getRemoteHostIp(), false);
            appendQueryParam(url, UA, getURLEncode(sasParams.getUserAgent(), format), false);
            if (sasParams.getOsId() == SASRequestParameters.HandSetOS.iOS.getValue()) {
                appendQueryParam(url, PDID, casInternalRequestParameters.getUidIFA(), false);
            } else {
                appendQueryParam(url, PDID, casInternalRequestParameters.getGpid(), false);
            }
            appendQueryParam(url, HEIGHT, height, false);
            appendQueryParam(url, WIDTH, width, false);
            appendQueryParam(url, KEYWORDS, getURLEncode(getCategories(','), format), false);
            if (sasParams.getOsId() == SASRequestParameters.HandSetOS.iOS.getValue()) {
                os = "IOS";
            } else if (sasParams.getOsId() == SASRequestParameters.HandSetOS.Android.getValue()) {
                os = "Android";
            }
            appendQueryParam(url, OS, os, false);
            appendQueryParam(url, OSV, sasParams.getOsMajorVersion(), false);
            LOG.debug("StartMeAppDCP url is {}", url);
            return new URI(url.toString());
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        if (StringUtils.isBlank(response) || status.code() != 200) {
            statusCode = 500;
            responseContent = "";
            return;
        } else {
            try {
                final JSONObject adResponse = new JSONObject(response);
                statusCode = status.code();
                final VelocityContext context = new VelocityContext();
                Formatter.TemplateType t = Formatter.TemplateType.HTML;
                context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, adResponse.getString("adm"));
                adStatus = AD_STRING;
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                LOG.debug("response content length is {} and the response is {}", responseContent.length(), responseContent);
            } catch (final JSONException exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from Startmeappdcp: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from Startmeappdcp: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
            }
        }

    }
}
