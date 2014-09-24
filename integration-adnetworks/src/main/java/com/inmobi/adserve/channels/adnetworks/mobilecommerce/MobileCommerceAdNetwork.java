package com.inmobi.adserve.channels.adnetworks.mobilecommerce;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class MobileCommerceAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(MobileCommerceAdNetwork.class);

    // Parameters from NAS
    private String              uid;
    private final String        responseFormat;

    public MobileCommerceAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        responseFormat = config.getString("mobilecommerce.responseFormat");
    }

    @Override
    public boolean configureParameters() {
        if (sasParams.getRemoteHostIp() == null || sasParams.getUserAgent() == null
                || StringUtils.isBlank(externalSiteId)) {
            LOG.info("mandate parameters missing for mobile commerce so exiting adapter");
            return false;
        }
        host = config.getString("mobilecommerce.host");
        uid = externalSiteId;
        LOG.debug("Configure parameters inside mobile commerce returned true");
        return true;
    }

    @Override
    public String getName() {
        return "mobilecommerce";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public String getId() {
        return (config.getString("mobilecommerce.advertiserId"));
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder();
            url.append(host).append(responseFormat).append("/?uid=").append(uid).append("&sid=").append(blindedSiteId)
                    .append("&gip=");
            url.append(sasParams.getRemoteHostIp()).append("&ua=")
                    .append(getURLEncode(sasParams.getUserAgent(), format));
            url.append("&filter=").append(config.getString("mobilecommerce.filter"));
            url.append("&test=").append(config.getString("mobilecommerce.isTest"));

            if (null != clickUrl) {
                url.append("&inm_clk=").append(getURLEncode(clickUrl, format));
            }
            if (null != beaconUrl) {
                url.append("&inm_img=").append(getURLEncode(beaconUrl, format));
            }

            if (sasParams.getCountryCode() != null) {
                url.append("&region=").append(sasParams.getCountryCode());
            }
            String categoryId = null;
            if ((categoryId = getCategoryId()) != null) {
                url.append("&catId=").append(categoryId);
            }
            LOG.debug("mobile commerce url is {}", url);
            return (new URI(url.toString()));
        } catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
    }

    // get category id specific to inmobi
    public String getCategoryId() {
        if (uid.length() == 6 && uid.matches("\\d+")) {
            return (Integer.toString((Integer.parseInt(uid.substring(4, 6)) + 2) / 3));
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
        } else {
            try {
                statusCode = status.code();
                VelocityContext context = new VelocityContext();
                TemplateType responseTemplate;
                if ("html".equalsIgnoreCase(responseFormat)) {
                    context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
                    responseTemplate = TemplateType.HTML;
                } else if (response.startsWith("{")) {
                    responseTemplate = getAdTemplateFromJson(response, context);
                } else {
                    adStatus = "NO_AD";
                    responseContent = "";
                    return;
                }
                responseContent = Formatter.getResponseFromTemplate(responseTemplate, context, sasParams, null);
                adStatus = "AD";

            } catch (JSONException exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from MobileCommerce : {}", exception);
                LOG.info("Response from siquis: {}", response);
            } catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from MobileCommerce : {}", exception);
                LOG.info("Response from MobileCommerce: {}", response);
                try {
                    throw exception;
                } catch (Exception e) {
                    LOG.info("Error while rethrowing the exception : {}", e);
                }
            }
        }
        LOG.debug("response length is {} responseContent is {}", responseContent.length(), responseContent);
    }

    /**
     * @param response
     * @param context
     * @return
     * @throws JSONException
     */
    private TemplateType getAdTemplateFromJson(final String response, final VelocityContext context)
            throws JSONException {
        TemplateType responseTemplate;
        JSONObject responseJson = new JSONObject(response);
        JSONObject adResponse = responseJson.getJSONObject("ad");
        context.put(VelocityTemplateFieldConstants.AdText, adResponse.get("title"));
        context.put(VelocityTemplateFieldConstants.Description, adResponse.get("desc"));
        context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.get("clickUrl"));
        context.put(VelocityTemplateFieldConstants.SmallFont, "1");
        context.put(VelocityTemplateFieldConstants.IMClickUrl, getURLEncode(clickUrl, format));
        String adUrl = adResponse.getString("displayUrl");
        if (StringUtils.isNotBlank(adUrl)) {
            context.put(VelocityTemplateFieldConstants.AdUrl, adResponse.get("displayUrl"));
        }
        context.put(VelocityTemplateFieldConstants.AdTag, true);
        addPartnerBeaconUrls(context, responseJson);
        String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
        if (StringUtils.isEmpty(vmTemplate)) {
            responseTemplate = TemplateType.PLAIN;
        } else {
            context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
            responseTemplate = TemplateType.RICH;
        }
        return responseTemplate;
    }

    /**
     * @param context
     * @param adResponse
     * @throws JSONException
     */
    private void addPartnerBeaconUrls(final VelocityContext context, final JSONObject adResponse) throws JSONException {
        JSONObject partnerBeaconUrls = adResponse.getJSONObject("trackingUrls");
        if (!StringUtils.isEmpty(partnerBeaconUrls.getString("mc"))) {
            context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl1, partnerBeaconUrls.getString("mc"));
        }
        if (!StringUtils.isEmpty(partnerBeaconUrls.getString("adProvider"))) {
            context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl2, partnerBeaconUrls.getString("adProvider"));
        }
    }
}