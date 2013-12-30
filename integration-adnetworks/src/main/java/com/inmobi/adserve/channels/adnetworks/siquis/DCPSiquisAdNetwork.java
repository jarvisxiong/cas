package com.inmobi.adserve.channels.adnetworks.siquis;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Calendar;


public class DCPSiquisAdNetwork extends BaseAdNetworkImpl {

    private final Configuration config;
    private boolean             isAndroid;

    public DCPSiquisAdNetwork(final DebugLogger logger, final Configuration config,
            final ClientBootstrap clientBootstrap, final HttpRequestHandlerBase baseRequestHandler,
            final MessageEvent serverEvent) {
        super(baseRequestHandler, serverEvent, logger);
        this.logger = logger;
        this.config = config;
        this.clientBootstrap = clientBootstrap;
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandatory parameters missing for siquis so exiting adapter");
            return false;
        }

        isAndroid = "app".equalsIgnoreCase(sasParams.getSource())
                && sasParams.getOsId() == HandSetOS.Android.getValue();
        host = config.getString("siquis.host");
        logger.info("Configure parameters inside siquis returned true");
        return true;
    }

    @Override
    public String getName() {
        return "siquis";
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder();
            url.append(host).append("&app_id=").append(externalSiteId);
            String uniqueDeviceId = getUid();
            if (StringUtils.isEmpty(uniqueDeviceId) || "null".equals(uniqueDeviceId)) {
                uniqueDeviceId = DigestUtils.md5Hex(sasParams.getUserAgent() + sasParams.getRemoteHostIp()
                        + Calendar.getInstance().getTimeInMillis());
            }
            url.append("&device_id=").append(uniqueDeviceId);
            url.append("&partner_id=").append(config.getString("siquis.partnerId"));

            logger.debug("Siquis url is ", url.toString());
            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info(exception.getMessage());
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        logger.debug("response is ", response, "and response length is ", response.length());
        if (status.getCode() != 200 || StringUtils.isBlank(response) || !response.startsWith("[{\"")) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            try {
                statusCode = status.getCode();
                JSONArray adResponseArray = new JSONArray(response);
                JSONObject adResponse = adResponseArray.getJSONObject(0);
                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.AdText, adResponse.get("title"));
                context.put(VelocityTemplateFieldConstants.Description,
                    adResponse.get(VelocityTemplateFieldConstants.Description));
                if (isAndroid) {
                    String url = reformatClickUr(adResponse.get("clickurl").toString());
                    if (url != null) {
                        context.put(VelocityTemplateFieldConstants.PartnerClickUrl, url);
                    }
                    else {
                        return;
                    }
                }
                else {
                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse.get("clickurl"));
                }
                context.put(VelocityTemplateFieldConstants.SmallFont, "1");
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
                if (StringUtils.isEmpty(vmTemplate)) {
                    responseContent = Formatter.getResponseFromTemplate(TemplateType.PLAIN, context, sasParams,
                        beaconUrl, logger);
                }
                else {
                    context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                    responseContent = Formatter.getResponseFromTemplate(TemplateType.RICH, context, sasParams,
                        beaconUrl, logger);
                }
                adStatus = "AD";
                logger.debug("response length is ", responseContent.length(), "responseContent is", responseContent);
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from siquis : ", exception);
                logger.info("Response from siquis:", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from siquis : ", exception);
                logger.info("Response from siquis:", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    logger.info("Error while rethrowing the exception : ", e);
                }
            }
        }
    }

    /**
     * @param clickUrl
     * @return formatedClickUrl
     */
    private String reformatClickUr(final String clickUrl) {
        StringBuilder formattedUrl = new StringBuilder();

        try {
            String utf8 = "UTF-8";
            String[] url = clickUrl.split("[?]");
            formattedUrl.append(url[0]).append('?');
            String[] urlParams = url[1].split("[&,=]");

            // URLDecoder.decode(clickUrl, "UTF-8")
            for (int i = 0; i < urlParams.length; i += 2) {
                formattedUrl
                        .append(URLDecoder.decode(urlParams[i], utf8))
                            .append('=')
                            .append(URLDecoder.decode(urlParams[i + 1], utf8))
                            .append('&');
            }
        }
        catch (Exception ex) {
            logger.info("Faild to reformat Siquis clickurl ", clickUrl);
            return null;
        }

        return formattedUrl.toString();
    }

    @Override
    public String getId() {
        return (config.getString("siquis.advertiserId"));
    }
}
