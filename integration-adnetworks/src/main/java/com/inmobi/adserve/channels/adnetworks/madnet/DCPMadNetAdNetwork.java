package com.inmobi.adserve.channels.adnetworks.madnet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


/**
 * @author deepak
 * 
 */
public class DCPMadNetAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DCPMadNetAdNetwork.class);

    private int                 width;
    private int                 height;
    private String              clientId;
    private static final String WAP = "wap";

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPMadNetAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())) {
            LOG.debug("mandatory parameters missing for madnet so exiting adapter");
            return false;
        }
        host = config.getString("madnet.host");
        clientId = config.getString("madnet.clientId");
        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }

        LOG.info("Configure parameters inside madnet returned true");
        return true;
    }

    @Override
    public String getName() {
        return "madnet";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder();
            url.append(host).append("?uuid=0&t=json&html=0&type=text%2Cimage&ip=").append(sasParams.getRemoteHostIp());
            url.append("&nid=").append(clientId);
            url.append("&pid=").append(blindedSiteId);
            url.append("&w=").append(width);
            url.append("&h=").append(height);
            url.append("&ua=").append(getURLEncode(sasParams.getUserAgent(), format));
            url.append("&cat=").append(getURLEncode(getCategories(',', true, true), format));

            if (WAP.equalsIgnoreCase(sasParams.getSource())) {
                url.append("&sid=");
            }
            else {
                url.append("&apid=");
            }
            url.append(blindedSiteId);

            if (!StringUtils.isEmpty(sasParams.getAge())) {
                url.append("&age=").append(sasParams.getAge());
            }
            if (!StringUtils.isEmpty(sasParams.getGender())) {
                url.append("gen=").append(sasParams.getGender().toLowerCase());
            }
            if (casInternalRequestParameters.latLong != null
                    && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
                url.append("&gps=").append(getURLEncode(casInternalRequestParameters.latLong, format));
            }

            if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
                if (casInternalRequestParameters.uidIFA != null) {
                    url.append("&idfa=").append(casInternalRequestParameters.uidIFA);
                    url.append("&idfatracking=").append(casInternalRequestParameters.uidADT);
                }
            }

            if (casInternalRequestParameters.uidIDUS1 != null) {
                url.append("&dpidsha1=").append(casInternalRequestParameters.uidIDUS1);
            }
            if (casInternalRequestParameters.uidMd5 != null) {
                url.append("&dpidmd5=").append(casInternalRequestParameters.uidMd5);
            }

            if (casInternalRequestParameters.uidO1 != null) {
                url.append("&macsha1=").append(casInternalRequestParameters.uidO1);
            }

            if (casInternalRequestParameters.zipCode != null) {
                url.append("&z=").append(casInternalRequestParameters.zipCode);
            }

            LOG.debug("MadNet url is {}", url);
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
        LOG.debug("response is {} and response length is {}", response, response.length());
        if (status.code() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            statusCode = status.code();
            VelocityContext context = new VelocityContext();
            TemplateType t = TemplateType.IMAGE;
            try {
                JSONObject adResponse = (new JSONObject(response)).getJSONObject("response").getJSONArray("ads")
                        .getJSONObject(0);

                if ("text".equalsIgnoreCase(adResponse.getString("type"))) {
                    adResponse = adResponse.getJSONObject("components");
                    context.put(VelocityTemplateFieldConstants.AdText, adResponse.getString("text_title"));
                    String vmTemplate = Formatter.getRichTextTemplateForSlot(slot);
                    if (!StringUtils.isEmpty(vmTemplate)) {
                        context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                        t = TemplateType.RICH;
                    }
                    else {
                        t = TemplateType.PLAIN;
                    }
                }
                else {
                    adResponse = adResponse.getJSONObject("components");
                    context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getString("image_url"));
                }

                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.getString("click_url"));
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                JSONArray beaconArray = adResponse.getJSONArray("beacons");
                int beaconArrayLength = beaconArray.length();
                context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, beaconArray.getString(0));
                if (beaconArrayLength > 1) {
                    context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl1, beaconArray.getString(1));
                    if (beaconArrayLength > 2) {
                        context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl2, beaconArray.getString(2));
                    }
                }
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from MadNet : {}", exception);
                LOG.info("Response from MadNet: {}", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from MadNet : {}", exception);
                LOG.info("Response from MadNet: {}", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    LOG.info("Error while rethrowing the exception : {}", e);
                }
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("madnet.advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }
}