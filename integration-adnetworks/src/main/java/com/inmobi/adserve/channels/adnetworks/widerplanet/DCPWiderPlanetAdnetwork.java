package com.inmobi.adserve.channels.adnetworks.widerplanet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.adnetworks.adelphic.DCPAdelphicAdNetwork;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPWiderPlanetAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPAdelphicAdNetwork.class);

    private String              inmobiCookieId;

    public DCPWiderPlanetAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for widerplanet so exiting adapter");
            return false;
        }
        if (!"WAP".equalsIgnoreCase(sasParams.getSource())) {
            LOG.debug("Only WAP traffic allowed. So exiting the adapter");
        }
        try {
            JSONObject userParams = new JSONObject(sasParams.getUidParams());
            inmobiCookieId = userParams.getString("imuc__5");
            if (StringUtils.isBlank(inmobiCookieId)) {
                inmobiCookieId = userParams.getString("WC");
            }
            if (StringUtils.isEmpty(inmobiCookieId)) {
                LOG.debug("imucId is not present. So exiting the adapter");
                return false;
            }

        }
        catch (Exception e) {
            LOG.debug("imucId is not present. So exiting the adapter");
            return false;
        }

        LOG.info("Configure parameters inside Wider Planet returned true");
        return true;
    }

    @Override
    public String getName() {
        return "widerplanet";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            String host = config.getString("widerplanet.host");
            StringBuilder url = new StringBuilder(host);
            url.append("?zoneid=").append(externalSiteId).append("&useragent=")
                    .append(getURLEncode(sasParams.getUserAgent(), format)).append("&uip=")
                    .append(sasParams.getRemoteHostIp()).append("&wuid=").append(getHashedValue(inmobiCookieId, "MD5"));
            if (StringUtils.isNotBlank(casInternalRequestParameters.latLong)) {
                url.append("&location=").append(getURLEncode(casInternalRequestParameters.latLong, format));
            }
            String uid = getUid();
            if (null != uid) {
                url.append("&duid=").append(uid);
            }
            LOG.debug("WiderPlanet url is {}", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
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
        }
        else {
            try {

                JSONObject adResponse = new JSONObject(response);
                if (StringUtils.isNotBlank(adResponse.getString("response"))) {
                    VelocityContext context = new VelocityContext();
                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("landingUrl"));
                    context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                    TemplateType t = TemplateType.IMAGE;
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("imageUrl"));

                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, adResponse.getString("beacon"));

                    if (response.contains("beacon_ext1")) {
                        context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl1,
                                adResponse.getString("beacon_ext1"));
                    }
                    if (response.contains("beacon_ext2")) {
                        context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl2,
                                adResponse.getString("beacon_ext2"));
                    }
                    responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                    adStatus = "AD";
                }
                else {
                    statusCode = 500;
                    adStatus = "NO_AD";
                    responseContent = "";
                    return;
                }
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from widerplanet : {}", exception);
                LOG.info("Response from wider planet: {}", response);

            }

        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("widerplanet.advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

}
