package com.inmobi.adserve.channels.adnetworks.nexage;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DCPNexageAdNetwork extends AbstractDCPAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true on success
    private static final Logger LOG = LoggerFactory.getLogger(DCPNexageAdNetwork.class);

    private static final String POS = "pos";
    private static final String JS_AD_TAG = "jsAdTag";
    private static final String DCN = "DCN";
    private static final String LAT_LONG = "LatLong";
    private static final String CATEGORY = "CATEGORY";
    private static final String BLINDED_SITE_ID = "BlindedSiteId";

    protected boolean jsAdTag = false;
    private int height = 0;
    private int width = 0;
    private String pos;
    private boolean isGeo = false;
    private boolean isApp = false;

    public DCPNexageAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    // Configure the request parameters for making the ad call
    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandate parameters missing for nexage, so returning from adapter");
            LOG.info("Configure parameters inside nexage returned false");
            return false;
        }

        if (casInternalRequestParameters.getLatLong() != null
                && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
            isGeo = true;
        }
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (null != slotSizeMapEntity) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        try {
            // pos is configured as the additional param in the
            // segment table
            pos = entity.getAdditionalParams().getString(POS);
        } catch (final JSONException e) {
            LOG.debug("POS is not configured for the segment:{} {}, exception raised {}", entity.getExternalSiteKey(),
                    getName(), e);
            LOG.info("Configure parameters inside nexage returned false");
            return false;
        }

        try {
            jsAdTag = Boolean.parseBoolean(entity.getAdditionalParams().getString(JS_AD_TAG));
        } catch (final JSONException e) {
            jsAdTag = false;
            LOG.debug(
                    "exception raised while retrieving JS_AD_TAG from additional Params for the segment:{} {}, exception raised {}",
                    entity.getExternalSiteKey(), getName(), e);
        }

        isApp = StringUtils.isBlank(sasParams.getSource()) || WAP.equalsIgnoreCase(sasParams.getSource())
                ? false
                : true;
        constructURL();
        return true;
    }

    @Override
    public String getName() {
        return "nexageDCP";
    }

    @Override
    public String getId() {
        return config.getString("nexage.advertiserId");
    }

    private void constructURL() {
        final StringBuilder finalUrlBuilder = new StringBuilder(config.getString("nexage.host"));
        finalUrlBuilder.append("pos=").append(pos);

        if (height > 0) {
            finalUrlBuilder.append("&p(size)=").append(width).append('x').append(height);
        }
        if ("test".equals(config.getString("nexage.test"))) {
            finalUrlBuilder.append("&mode=test");
        }
        finalUrlBuilder.append("&dcn=").append(externalSiteId);
        finalUrlBuilder.append("&ip=").append(sasParams.getRemoteHostIp());
        finalUrlBuilder.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
        finalUrlBuilder.append("&p(site)=");
        if (ContentType.PERFORMANCE == sasParams.getSiteContentType()) {
            finalUrlBuilder.append('p');
        } else {
            finalUrlBuilder.append("fs");
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidSO1())) {
            finalUrlBuilder.append("&d(id2)=").append(casInternalRequestParameters.getUidSO1());
        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
            finalUrlBuilder.append("&d(id2)=").append(casInternalRequestParameters.getUidIDUS1());
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
            if (isApp) {
                finalUrlBuilder.append("&d(id12)=").append(casInternalRequestParameters.getUidMd5());
            } else {
                finalUrlBuilder.append("&u(id)=").append(casInternalRequestParameters.getUidMd5());
            }
        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
            if (isApp) {
                finalUrlBuilder.append("&d(id12)=").append(casInternalRequestParameters.getUid());
            } else {
                finalUrlBuilder.append("&u(id)=").append(casInternalRequestParameters.getUid());
            }
        }
        String gpid = getGPID(true);
        if (gpid != null) {
            finalUrlBuilder.append("&d(id24)=").append(gpid);
        }

        final String ifa = getUidIFA(false);
        if (StringUtils.isNotEmpty(ifa) && casInternalRequestParameters.isTrackingAllowed()) {
            finalUrlBuilder.append("&d(id24)=").append(ifa);
        }

        if (isGeo) {
            finalUrlBuilder.append("&req(loc)=")
                    .append(getURLEncode(casInternalRequestParameters.getLatLong(), format));
        }

        finalUrlBuilder.append("&cn=").append(getCategories(',', true, true).split(",")[0].trim());

        if (null != sasParams.getAge()) {
            finalUrlBuilder.append("&u(age)=").append(sasParams.getAge());
        }

        if (StringUtils.isNotBlank(sasParams.getGender())) {
            finalUrlBuilder.append("&u(gender)=").append(sasParams.getGender());
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.getZipCode())) {
            finalUrlBuilder.append("&req(zip)=").append(casInternalRequestParameters.getZipCode());
        }

        finalUrlBuilder.append("&p(blind_id)=").append(blindedSiteId); // send
        // blindedSiteid instead of url

        finalUrlBuilder.append("&u(country)=").append(IABCountriesMap.getIabCountry(sasParams.getCountryCode()));

        if (null != sasParams.getState()) {
            finalUrlBuilder.append("&u(dma)=").append(sasParams.getState());
        }

        final String[] urlParams = finalUrlBuilder.toString().split("&");
        finalUrlBuilder.delete(0, finalUrlBuilder.length());
        finalUrlBuilder.append(urlParams[0]);

        // discarding parameters that have null values
        for (int i = 1; i < urlParams.length; i++) {
            final String[] paramValue = urlParams[i].split("=");
            if (paramValue.length == 2 && !"null".equals(paramValue[1]) && !StringUtils.isEmpty(paramValue[1])) {
                finalUrlBuilder.append("&").append(paramValue[0]).append("=").append(paramValue[1]);
            }
        }
        LOG.debug("url inside nexage: {}", finalUrlBuilder);

        host = finalUrlBuilder.toString();
    }

    // get URI
    @Override
    public URI getRequestUri() throws Exception {
        try {
            return new URI(host);
        } catch (final URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("Error Forming Url inside nexage {}", exception);
        }
        return null;
    }

    // parse the response received from nexage
    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {} and response length is {}", response, response.length());
        if (status.code() != 200 || response.trim().isEmpty()) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            return;
        } else {
            statusCode = status.code();
            final VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response.trim());
            buildInmobiAdTracker();

            try {
                responseContent =
                        Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, getBeaconUrl());
            } catch (final Exception exception) {
                adStatus = NO_AD;
                LOG.info("Error parsing response {} from nexage: {}", response, exception);
                InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
                return;
            }
            adStatus = AD_STRING;
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public void generateJsAdResponse() {
        statusCode = HttpResponseStatus.OK.code();
        final VelocityContext context = new VelocityContext();
        context.put(POS, pos);
        context.put(DCN, externalSiteId);
        context.put(CATEGORY, getCategories(',', true, true).split(",")[0].trim());
        context.put(BLINDED_SITE_ID, blindedSiteId);
        if (isGeo) {
            context.put(LAT_LONG, casInternalRequestParameters.getLatLong());
        }
        try {
            responseContent = Formatter.getResponseFromTemplate(TemplateType.NEXAGE_JS_AD_TAG, context, sasParams,
                    getBeaconUrl());
            LOG.debug("response length is {}", responseContent.length());
            adStatus = AD_STRING;
        } catch (final Exception exception) {
            adStatus = NO_AD;
            LOG.info("Error generating Static Js adtag for nexage  : {}", exception);
        }

    }

    @Override
    public boolean useJsAdTag() {
        return jsAdTag;
    }
}
