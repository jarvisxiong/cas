package com.inmobi.adserve.channels.adnetworks.openx;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;


public class OpenxAdNetwork extends BaseAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true on
    // success.i

    private String              latitude  = null;
    private String              longitude = null;
    private final Configuration config;

    public OpenxAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent) {
        super(baseRequestHandler, serverEvent, logger);
        this.logger = logger;
        this.config = config;
        this.clientBootstrap = clientBootstrap;
    }

    // Configure the request parameters for making the ad call
    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandate parameters missing for openx, so returning from adapter");
            return false;
        }

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        logger.debug("Configure parameters inside openx returned true");
        return true;
    }

    @Override
    public String getName() {
        return "openx";
    }

    @Override
    public String getId() {
        return (config.getString("openx.advertiserId"));
    }

    // get URI
    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder finalUrl = new StringBuilder(config.getString("openx.host"));
        finalUrl.append(externalSiteId)
                    .append("&cnt=")
                    .append(sasParams.getCountryCode().toLowerCase())
                    .append("&dma=")
                    .append(sasParams.getState());
        finalUrl.append("&net=").append(sasParams.getLocSrc()).append("&age=").append(sasParams.getAge());
        if (sasParams.getGender() != null) {
            finalUrl.append("&gen=").append(sasParams.getGender().toUpperCase());
        }
        finalUrl.append("&ip=")
                    .append(sasParams.getRemoteHostIp())
                    .append("&lat=")
                    .append(latitude)
                    .append("&lon=")
                    .append(longitude);
        if (StringUtils.isNotEmpty(latitude)) {
            finalUrl.append("&lt=3");
        }
        finalUrl.append("&zip=")
                    .append(casInternalRequestParameters.zipCode)
                    .append("&c.siteId=")
                    .append(blindedSiteId);

        if (HandSetOS.iPhone_OS.getValue() == sasParams.getOsId()) {
            finalUrl.append("&did.ia=").append(casInternalRequestParameters.uidIFA);
            finalUrl.append("&did.iat=").append(casInternalRequestParameters.uidADT);
            finalUrl.append("&did.o1=").append(casInternalRequestParameters.uidO1);
            finalUrl.append("&did.ma.md5=").append(casInternalRequestParameters.uidMd5);
            finalUrl.append("&did.ma.sha1=").append(casInternalRequestParameters.uidSO1);
        }
        else if (HandSetOS.Android.getValue() == sasParams.getOsId()) {
            finalUrl.append("&did.ai.md5=").append(casInternalRequestParameters.uidMd5);
            finalUrl.append("&did.ai.sha1=").append(casInternalRequestParameters.uidO1);
        }

        finalUrl.append("&did=").append(casInternalRequestParameters.uid);

        String[] urlParams = finalUrl.toString().split("&");
        finalUrl.delete(0, finalUrl.length());
        finalUrl.append(urlParams[0]);

        // discarding parameters that have null values
        for (int i = 1; i < urlParams.length; i++) {
            String[] paramValue = urlParams[i].split("=");
            if ((paramValue.length == 2) && !(paramValue[1].equals("null")) && !(StringUtils.isEmpty(paramValue[1]))) {
                finalUrl.append('&').append(paramValue[0]).append('=').append(paramValue[1]);
            }
        }
        logger.debug("url inside openx: ", finalUrl.toString());
        try {
            return (new URI(finalUrl.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info("Error Forming Url inside openx", exception.getMessage());
        }
        return null;
    }

    // parse the response received from openx
    @Override
    public void parseResponse(String response, HttpResponseStatus status) {
        logger.debug("response is", response, "and response length is", response.length());
        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.getCode();
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl,
                    logger);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from openx :", exception);
                logger.info("Response from openx:", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    logger.info("Error while rethrowing the exception :", e);
                }
            }
            adStatus = "AD";
        }
        logger.debug("response length is ", responseContent.length());
    }
}
