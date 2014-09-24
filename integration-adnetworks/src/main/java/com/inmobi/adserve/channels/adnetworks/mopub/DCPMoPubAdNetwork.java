package com.inmobi.adserve.channels.adnetworks.mopub;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;


public class DCPMoPubAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPMoPubAdNetwork.class);

    private String              deviceId;
    private static final String name             = "mopub";
    private static final String responseTemplate = "%s <img src='%s' height=1 width=1 border=0 style=\"display:none;\"/>";

    public DCPMoPubAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for MoPub so exiting adapter");
            return false;
        }

        if (HandSetOS.iOS.getValue() == sasParams.getOsId()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                deviceId = "ifa:" + casInternalRequestParameters.uidIFA + "&dnt=" + casInternalRequestParameters.uidADT;
            }
        }

        if (StringUtils.isBlank(deviceId) && StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            deviceId = "sha:" + casInternalRequestParameters.uidO1;
        }
        if (StringUtils.isBlank(deviceId)) {
            LOG.debug("Device id mandate parameters missing for MoPub, so returning from adapter");
            return false;
        }

        LOG.info("Configure parameters inside MoPub returned true");
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            String host = config.getString("mopub.host");
            StringBuilder url = new StringBuilder(host);
            url.append("?v=1&id=").append(externalSiteId).append("&ip=").append(sasParams.getRemoteHostIp())
                    .append("&udid=").append(deviceId).append("&q=")
                    .append(getURLEncode(getCategories(',', true, false), format));

            if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)
                    && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
                url.append("&ll=").append(getURLEncode(casInternalRequestParameters.latLong, format));
            }

            LOG.debug("MoPub url is {}", url);

            return (new URI(url.toString()));
        } catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        statusCode = status.code();
        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        } else {
            responseContent = String.format(responseTemplate, response, beaconUrl);
            adStatus = "AD";
        }
        LOG.debug("response length is {}", response.length());
    }

    @Override
    public String getId() {
        return (config.getString("mopub.advertiserId"));
    }

}
