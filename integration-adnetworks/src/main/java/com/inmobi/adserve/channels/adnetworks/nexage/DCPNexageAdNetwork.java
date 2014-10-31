package com.inmobi.adserve.channels.adnetworks.nexage;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

public class DCPNexageAdNetwork extends AbstractDCPAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true
    // on
    // success.i
    private static final Logger LOG = LoggerFactory.getLogger(DCPNexageAdNetwork.class);

    private static final String POS = "pos";
    private static final String JS_AD_TAG = "jsAdTag";
    private static final String DCN = "DCN";
    private static final String LAT_LONG = "LatLong";
    private static final String CATEGORY = "CATEGORY";
    private static final String BLINDED_SITE_ID = "BlindedSiteId";
    private static IABCountriesInterface iABCountries;

    protected boolean jsAdTag = false;
    private int height = 0;
    private int width = 0;
    private String pos;
    private boolean isGeo = false;
    private boolean isApp = false;



    static {
        iABCountries = new IABCountriesMap();
    }

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

        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            final Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
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
            LOG.debug("exception raised while retrieving JS_AD_TAG from additional Params {}", e);
        }

        isApp = StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource())
                        ? false
                        : true;

        return true;
    }

    @Override
    public String getName() {
        return "nexage";
    }

    @Override
    public String getId() {
        return config.getString("nexage.advertiserId");
    }

    // get URI
    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder finalUrl = new StringBuilder(config.getString("nexage.host"));
        finalUrl.append("pos=").append(pos);
        if (height > 0) {
            finalUrl.append("&p(size)=").append(width).append('x').append(height);
        }
        if ("test".equals(config.getString("nexage.test"))) {
            finalUrl.append("&mode=test");
        }
        finalUrl.append("&dcn=").append(externalSiteId);
        finalUrl.append("&ip=").append(sasParams.getRemoteHostIp());
        finalUrl.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
        finalUrl.append("&p(site)=");
        if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
            finalUrl.append('p');
        } else {
            finalUrl.append("fs");
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
            finalUrl.append("&d(id2)=").append(casInternalRequestParameters.getUidO1());
        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
            finalUrl.append("&d(id2)=").append(casInternalRequestParameters.getUidIDUS1());
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {
            if (isApp) {
                finalUrl.append("&d(id12)=").append(casInternalRequestParameters.getUidMd5());
            } else {
                finalUrl.append("&u(id)=").append(casInternalRequestParameters.getUidMd5());
            }
        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUid())) {
            if (isApp) {
                finalUrl.append("&d(id12)=").append(casInternalRequestParameters.getUid());
            } else {
                finalUrl.append("&u(id)=").append(casInternalRequestParameters.getUid());
            }
        } else {
            final String gpid = getGPID();
            if (gpid != null) {
                finalUrl.append("&d(id12)=").append(getHashedValue(gpid, "MD5"));
            }
        }
        if (isGeo) {
            finalUrl.append("&req(loc)=").append(getURLEncode(casInternalRequestParameters.getLatLong(), format));
        }

        finalUrl.append("&cn=").append(getCategories(',', true, true).split(",")[0].trim());

        if (null != sasParams.getAge()) {
            finalUrl.append("&u(age)=").append(sasParams.getAge());
        }

        if (StringUtils.isNotBlank(sasParams.getGender())) {
            finalUrl.append("&u(gender)=").append(sasParams.getGender());
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.getZipCode())) {
            finalUrl.append("&req(zip)=").append(casInternalRequestParameters.getZipCode());
        }

        finalUrl.append("&p(blind_id)=").append(blindedSiteId); // send
        // blindedSiteid
        // instead of
        // url

        finalUrl.append("&u(country)=").append(iABCountries.getIabCountry(sasParams.getCountryCode()));

        if (null != sasParams.getState()) {
            finalUrl.append("&u(dma)=").append(sasParams.getState());
        }

        final String[] urlParams = finalUrl.toString().split("&");
        finalUrl.delete(0, finalUrl.length());
        finalUrl.append(urlParams[0]);

        // discarding parameters that have null values
        for (int i = 1; i < urlParams.length; i++) {
            final String[] paramValue = urlParams[i].split("=");
            if (paramValue.length == 2 && !"null".equals(paramValue[1]) && !StringUtils.isEmpty(paramValue[1])) {
                finalUrl.append("&").append(paramValue[0]).append("=").append(paramValue[1]);
            }
        }
        LOG.debug("url inside nexage: {}", finalUrl);
        try {
            return new URI(finalUrl.toString());
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
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
            } catch (final Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response {} from nexage: {}", response, exception);
            }
            adStatus = "AD";
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
            responseContent =
                    Formatter.getResponseFromTemplate(TemplateType.NEXAGE_JS_AD_TAG, context, sasParams, beaconUrl);
            adStatus = "AD";
        } catch (final Exception exception) {
            adStatus = "NO_AD";
            LOG.info("Error generating Static Js adtag for nexage  : {}", exception);
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public boolean useJsAdTag() {
        return jsAdTag;
    }
}
