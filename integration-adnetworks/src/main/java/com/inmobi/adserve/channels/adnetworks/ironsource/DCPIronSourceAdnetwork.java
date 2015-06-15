package com.inmobi.adserve.channels.adnetworks.ironsource;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by deepak on 24/3/15.
 */
public class DCPIronSourceAdnetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPIronSourceAdnetwork.class);
    private static final String SITEID = "siteid";
    private static final String TOKEN = "token";
    private static final String IP= "ip";
    private static final String PACKAGENAME = "packageName";
    private static final String OSVERSION = "osVersion";
    private static final String IDFA = "idfa";
    private static final String GAID = "gaid";
    private static final String UA = "ua";
    private boolean isApp;
    private static String UID = null;
    private int width;
    private int height;
    private transient String latitude;
    private transient String longitude;
    private static Short slotid;

    public DCPIronSourceAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
                                  final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId) || (StringUtils.isEmpty(casInternalRequestParameters.getUidIFA()) && StringUtils.isEmpty(getGPID()))) {
            LOG.debug("mandatory parameters missing for ironsource so exiting adapter");
            LOG.info("Configure parameters inside ironsource returned false");
            return false;
        }
        host = config.getString("ironsource.host");
        isApp = StringUtils.isBlank(sasParams.getSource()) || WAP.equalsIgnoreCase(sasParams.getSource())
                ? false
                : true;
        if (isApp && (StringUtils.isEmpty(getGPID())) && (StringUtils.isEmpty(casInternalRequestParameters.getUidIFA())) ) {
            LOG.debug("mandatory parameter GAID/IDFA is missing for APP traffic in IronSource so exiting adapter");
            return false;
        }
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
        return "ironsourceDCP";
    }

    @Override
    public URI getRequestUri() throws Exception{
        try {
        final StringBuilder url = new StringBuilder(host);
            appendQueryParam(url, SITEID, "66", true);
            appendQueryParam(url,TOKEN, getToken(), false);
            appendQueryParam(url, PACKAGENAME, blindedSiteId, false);
            appendQueryParam(url,IP, sasParams.getRemoteHostIp(), false);
            appendQueryParam(url, OSVERSION, sasParams.getOsMajorVersion(), false);
            if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
                UID = StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA()) && GlobalConstant.ONE.equals(casInternalRequestParameters.getUidADT()) ? casInternalRequestParameters.getUidIFA() : null;
                    appendQueryParam(url, IDFA, UID, false);
                }
            else if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
                    appendQueryParam(url, GAID, getGPID(), false);
            }
            appendQueryParam(url, UA,getURLEncode(sasParams.getUserAgent(), format), false);
        LOG.debug("IronSource url is {}", url);
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
        if (StringUtils.isBlank(response) || status.code() != 200||response.contains("\"error\":true")) {
            statusCode = 500;
            responseContent = "";
            return;
        }
        else {
            try {
                String creativetype = null;
                final JSONObject adResponse = new JSONObject(response);
                    statusCode = status.code();
                    final VelocityContext context = new VelocityContext();
                    TemplateType t = TemplateType.IMAGE;
                    final JSONArray responseAd =adResponse.getJSONArray("ads");
                        //(JSONArray) adResponse.get("ads");
                    if(responseAd.length() > 0){
                        final JSONObject responseAdObj = responseAd.getJSONObject(0);
                        final JSONObject responseCreative = responseAdObj.getJSONObject("creatives");
                        creativetype = "banner" + width + "x" + height;
                        if (width == 800 && height == 1280) {
                            creativetype = "banner768x1024";
                        } else if (width == 1280 && height == 800) {
                            creativetype = "banner1024x768";
                        }
                        context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, responseCreative.getString(creativetype));
                        buildInmobiAdTracker();
                        context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, responseAdObj.getString("clickURL"));
                        adStatus = AD_STRING;
                        responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, getBeaconUrl());
                        LOG.debug("response content length is {} and the response is {}", responseContent.length(), responseContent);
                    }
                } catch (final JSONException exception) {
                    adStatus = NO_AD;
                    LOG.info("Error parsing response {} from ironsource: {}", response, exception);
                    InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                }catch (final Exception exception) {
                    adStatus = NO_AD;
                    LOG.info("Error parsing response {} from ironsource: {}", response, exception);
                    InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                }
            }
    }

    public String getToken() {
        return config.getString("ironsource.tokenid");
    }
}
