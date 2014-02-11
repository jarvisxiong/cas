package com.inmobi.adserve.channels.adnetworks.nexage;

import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;


public class DCPNexageAdNetwork extends AbstractDCPAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true on
    // success.i
    private static final Logger          LOG             = LoggerFactory.getLogger(DCPNexageAdNetwork.class);

    private int                          height          = 0;
    private int                          width           = 0;
    private static IABCountriesInterface iABCountries;
    private Request                      ningRequest;
    private String                       pos;
    protected boolean                    jsAdTag         = false;
    private boolean                      isGeo           = false;

    private static final String          POS             = "pos";
    private static final String          JS_AD_TAG       = "jsAdTag";
    private static final String          DCN             = "DCN";
    private static final String          LAT_LONG        = "LatLong";
    private static final String          CATEGORY        = "CATEGORY";
    private static final String          BLINDED_SITE_ID = "BlindedSiteId";

    static {
        iABCountries = new IABCountriesMap();
    }

    public DCPNexageAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    // Configure the request parameters for making the ad call
    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandate parameters missing for nexage, so returning from adapter");
            return false;
        }

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            isGeo = true;
        }

        if (null != sasParams.getSlot()
                && SlotSizeMapping.getDimension((long)sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long)sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        try {
            // pos is configured as the additional param in the
            // segment table
            pos = entity.getAdditionalParams().getString(POS);
        }
        catch (JSONException e) {
            LOG.info("pos is not configured for the segment:{} {}", entity.getExternalSiteKey(), this.getName());
            return false;
        }

        try {
            jsAdTag = Boolean.parseBoolean(entity.getAdditionalParams().getString(JS_AD_TAG));
        }
        catch (JSONException e) {
            jsAdTag = false;
        }

        LOG.debug("Configure parameters inside nexage returned true");
        return true;
    }

    @Override
    public String getName() {
        return "nexage";
    }

    @Override
    public String getId() {
        return (config.getString("nexage.advertiserId"));
    }

    // get URI
    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder finalUrl = new StringBuilder(config.getString("nexage.host"));
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
        }
        else {
            finalUrl.append("fs");
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            finalUrl.append("&d(id2)=").append(casInternalRequestParameters.uidO1);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
            finalUrl.append("&d(id2)=").append(casInternalRequestParameters.uidMd5);
        }

        if (isGeo) {
            finalUrl.append("&req(loc)=").append(getURLEncode(casInternalRequestParameters.latLong, format));
        }

        finalUrl.append("&cn=").append(getCategories(',', true, true).split(",")[0].trim());

        if (null != sasParams.getAge()) {
            finalUrl.append("&u(age)=").append(sasParams.getAge());
        }

        if (StringUtils.isNotBlank(sasParams.getGender())) {
            finalUrl.append("&u(gender)=").append(sasParams.getGender());
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.zipCode)) {
            finalUrl.append("&req(zip)=").append(casInternalRequestParameters.zipCode);
        }

        finalUrl.append("&p(blind_id)=").append(blindedSiteId); // send blindedSiteid instead of url

        finalUrl.append("&u(country)=").append(iABCountries.getIabCountry(sasParams.getCountryCode()));

        if (null != sasParams.getState()) {
            finalUrl.append("&u(dma)=").append(sasParams.getState());
        }

        String[] urlParams = finalUrl.toString().split("&");
        finalUrl.delete(0, finalUrl.length());
        finalUrl.append(urlParams[0]);

        // discarding parameters that have null values
        for (int i = 1; i < urlParams.length; i++) {
            String[] paramValue = urlParams[i].split("=");
            if ((paramValue.length == 2) && !(paramValue[1].equals("null")) && !(StringUtils.isEmpty(paramValue[1]))) {
                finalUrl.append("&").append(paramValue[0]).append("=").append(paramValue[1]);
            }
        }
        LOG.debug("url inside nexage: {}", finalUrl);
        try {
            return (new URI(finalUrl.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("Error Forming Url inside nexage {}", exception);
        }
        return null;
    }

    // parse the response received from nexage
    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {} and response length is ", response, response.length());
        if (status.getCode() != 200 || response.trim().isEmpty()) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            return;
        }
        else {
            statusCode = status.getCode();
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from nexage : {}", exception);
                LOG.info("Response from nexage: {}", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    LOG.info("Error while rethrowing the exception : {}", e);
                }
            }
            adStatus = "AD";
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean makeAsyncRequest() {
        LOG.debug("In nexage async");

        if (useJsAdTag()) {
            generateJsAdResponse();
            processResponse();
            LOG.debug("sent jsadcode ... returning from make NingRequest");
            return true;
        }
        try {
            String uri = getRequestUri().toString();
            requestUrl = uri;
            setNingRequest(requestUrl);
            LOG.debug("Nexage uri : {}", uri);
            startTime = System.currentTimeMillis();
            baseRequestHandler.getAsyncClient().executeRequest(ningRequest, new AsyncCompletionHandler() {
                @Override
                public Response onCompleted(final Response response) throws Exception {
                    MDC.put("requestId", serverEvent.getChannel().getId().toString());
                    if (!isRequestCompleted()) {
                        LOG.debug("Operation complete for channel partner: {}", getName());
                        latency = System.currentTimeMillis() - startTime;
                        LOG.debug("Nexage operation complete latency {}", latency);
                        String responseStr = response.getResponseBody();
                        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(response.getStatusCode());
                        parseResponse(responseStr, httpResponseStatus);
                        processResponse();
                    }
                    return response;
                }

                @Override
                public void onThrowable(final Throwable t) {
                    MDC.put("requestId", serverEvent.getChannel().getId().toString());
                    if (isRequestComplete) {
                        return;
                    }

                    if (t instanceof java.util.concurrent.TimeoutException) {
                        latency = System.currentTimeMillis() - startTime;
                        LOG.debug("Nexage timeout latency {}", latency);
                        adStatus = "TIME_OUT";
                        processResponse();
                        return;
                    }

                    LOG.debug("Nexage error latency {}", latency);
                    adStatus = "TERM";
                    LOG.info("error while fetching response from: {} {}", getName(), t);
                    processResponse();
                    return;
                }
            });
        }
        catch (Exception e) {
            LOG.debug("Exception in Nexage makeAsyncRequest : {}", e);
        }
        LOG.debug("returning from make NingRequest");
        return true;
    }

    @Override
    public void generateJsAdResponse() {
        statusCode = HttpResponseStatus.OK.getCode();
        VelocityContext context = new VelocityContext();
        context.put(POS, pos);
        context.put(DCN, externalSiteId);
        context.put(CATEGORY, getCategories(',', true, true).split(",")[0].trim());
        context.put(BLINDED_SITE_ID, blindedSiteId);
        if (isGeo) {
            context.put(LAT_LONG, casInternalRequestParameters.latLong);
        }
        try {
            responseContent = Formatter.getResponseFromTemplate(TemplateType.NEXAGE_JS_AD_TAG, context, sasParams,
                beaconUrl);
            adStatus = "AD";
        }
        catch (Exception exception) {
            adStatus = "NO_AD";
            LOG.info("Error generating Static Js adtag for nexage  : {}", exception);
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    private void setNingRequest(final String requestUrl) {
        ningRequest = new RequestBuilder()
                .setUrl(requestUrl)
                    .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                    .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                    .setHeader(HttpHeaders.Names.REFERER, requestUrl)
                    .setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
                    .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                    .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                    .build();
    }

    @Override
    public boolean useJsAdTag() {
        return jsAdTag;
    }
}
