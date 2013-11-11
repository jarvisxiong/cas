package com.inmobi.adserve.channels.adnetworks.atnt;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.DebugLogger;


public class ATNTAdNetwork extends BaseAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true on
    // success.
    private String              loc;
    private String              platform;
    private final Configuration config;

    public ATNTAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent) {
        super(baseRequestHandler, serverEvent, logger);
        this.logger = logger;
        this.config = config;
        this.clientBootstrap = clientBootstrap;
    }

    // Assign the value to the parameters
    @Override
    public boolean configureParameters() {
        if (sasParams.getUserAgent() == null || sasParams.getRemoteHostIp() == null
                || StringUtils.isBlank(externalSiteId)) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MANDATE_PARAM_MISSING;
            logger.debug("configure parameters in atnt returned false because mandate parameters were missing");
            return false;
        }
        if (casInternalRequestParameters.latLong != null) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            loc = latlong[0] + ":" + latlong[1];
        }
        else {
            loc = null;
        }
        logger.debug("Configure Parameters in atnt retunred true");
        return true;
    }

    // generating random integer for visitorId
    private String getVisitorId() {
        Random rand = new Random();
        return (Integer.toString(rand.nextInt(10000)));
    }

    @Override
    public String getName() {
        return "atnt";
    }

    // forming the url with the available parameter
    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder finalUrl = new StringBuilder(config.getString("atnt.host"));
            finalUrl.append(externalSiteId)
                        .append("&ip=")
                        .append(sasParams.getRemoteHostIp())
                        .append("&useragent=")
                        .append(getURLEncode(sasParams.getUserAgent(), format));
            finalUrl.append("&loc=")
                        .append(loc)
                        .append("&gender=")
                        .append(sasParams.getGender())
                        .append("&age=")
                        .append(sasParams.getAge());
            finalUrl.append("&listingcount=1")
                        .append("&udid=")
                        .append(casInternalRequestParameters.uid)
                        .append("&visitorid=")
                        .append(getVisitorId())
                        .append("&platform=");
            finalUrl.append(platform).append("&clkpxl=").append(getURLEncode(getClickUrl(), format));

            String[] urlParams = finalUrl.toString().split("&");
            finalUrl.delete(0, finalUrl.length());
            finalUrl.append(urlParams[0]);

            // discarding parameters that have null values
            for (int i = 1; i < urlParams.length; i++) {
                String[] paramValue = urlParams[i].split("=");
                if ((paramValue.length == 2) && !(paramValue[1].equals("null"))
                        && !(StringUtils.isEmpty(paramValue[1]))) {
                    finalUrl.append('&').append(paramValue[0]).append('=').append(paramValue[1]);
                }
            }
            logger.debug("url inside atnt is", finalUrl);
            return (new URI(finalUrl.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info("Error Forming Url inside atnt", exception.getMessage());
        }
        return null;
    }

    // Returns the Channel Id for the TPAN as in our database. This will be
    // hardcoded.
    @Override
    public String getId() {
        return config.getString("atnt.advertiserId");
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }
}