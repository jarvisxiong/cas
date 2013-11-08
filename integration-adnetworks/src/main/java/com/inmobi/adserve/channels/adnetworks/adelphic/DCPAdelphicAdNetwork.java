package com.inmobi.adserve.channels.adnetworks.adelphic;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.IABCategoriesInterface;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPAdelphicAdNetwork extends BaseAdNetworkImpl
{
    private final Configuration                 config;
    private int                                 width;
    private int                                 height;
    private String                              deviceId;
    private String                              latitude;
    private String                              longitude;
    private String                              spotId;
    private String                              publisherId;
    private String                              siteId;
    private Character                           sourceType;

    private static final IABCategoriesInterface iabCategoryMap = new IABCategoriesMap();

    public DCPAdelphicAdNetwork(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent)
    {
        super(baseRequestHandler, serverEvent, logger);
        this.config = config;
        this.logger = logger;
        this.clientBootstrap = clientBootstrap;
    }

    @Override
    public boolean configureParameters()
    {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.debug("mandatory parameters missing for Adelphic so exiting adapter");
            return false;
        }

        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            logger.debug("mandate parameters missing for Adelphic, so returning from adapter");
            return false;
        }
        deviceId = getUid();
        if (StringUtils.isEmpty(deviceId)) {
            logger.debug("mandate parameters missing for Adelphic, so returning from adapter");
            return false;
        }
        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];
        }
        try {
            JSONObject additionalParams = entity.getAdditionalParams();
            // Spot is configured as the additional param in the
            // segment table
            spotId = additionalParams.getString("spot");
            publisherId = additionalParams.getString("pubId");
            siteId = additionalParams.getString("site");
        }
        catch (Exception e) {
            logger.error("Spot Id/Site/pubId is not configured for the segment:{}", entity.getExternalSiteKey());
            return false;
        }
        sourceType = (StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource())) ? 'w'
                : 'a';
        logger.info("Configure parameters inside Adelphic returned true");
        return true;
    }

    @Override
    public String getName()
    {
        return "adelphic";
    }

    @Override
    public URI getRequestUri() throws Exception
    {
        try {
            String host = config.getString("adelphic.host");
            StringBuilder url = new StringBuilder(host);
            url.append("?pub=")
                        .append(publisherId)
                        .append("&site=")
                        .append(siteId)
                        .append("&spot=")
                        .append(spotId)
                        .append("&msi.name=")
                        .append(blindedSiteId)
                        .append("&msi.id=")
                        .append(blindedSiteId)
                        .append("&msi.type=")
                        .append(sourceType)
                        .append("&version=1.0")
                        .append("&ua=")
                        .append(getURLEncode(sasParams.getUserAgent(), format))
                        .append("&cliend_ip=")
                        .append(sasParams.getRemoteHostIp())
                        .append("&ctype=")
                        .append(getAdType())
                        .append("&csize=")
                        .append(width)
                        .append("x")
                        .append(height);
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                url.append("&ifa_sha1=").append(casInternalRequestParameters.uidIFA);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
                url.append("&odin_1=").append(casInternalRequestParameters.uidSO1);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
                url.append("&did_md5=").append(casInternalRequestParameters.uidMd5);
            }

            if (!StringUtils.isEmpty(latitude)) {
                url.append("&lat=").append(latitude);
            }
            if (!StringUtils.isEmpty(longitude)) {
                url.append("&lon=").append(longitude);
            }
            if (sasParams.getGender() != null) {
                url.append("&gender=").append(sasParams.getGender());
            }
            if (sasParams.getAge() != null) {
                url.append("&age=").append(sasParams.getAge());
            }
            ArrayList<Long> bCat = new ArrayList<Long>();

            if (casInternalRequestParameters.blockedCategories != null) {
                bCat.addAll(casInternalRequestParameters.blockedCategories);
            }

            if (SITE_RATING_PERFORMANCE.equalsIgnoreCase(sasParams.getSiteType())) {
                bCat.add(IABCategoriesMap.PERFORMANCE_BLOCK_CATEGORIES);
            }
            else {
                bCat.add(IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES);
            }
            url.append("&bcat=").append(
                getURLEncode(getValueFromListAsString(iabCategoryMap.getIABCategories(bCat)), format));
            url.append("&scat=").append(getURLEncode(getCategories(',', true, true), format));
            logger.debug("Adelphic url is", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info(exception.getMessage());
        }
        return null;
    }

    @Override
    public void parseResponse(String response, HttpResponseStatus status)
    {
        logger.debug("response is", response);
        statusCode = status.getCode();
        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl,
                    logger);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from Adelphic :", exception);
                logger.info("Response from Adelphic:", response);
            }
        }
        logger.debug("response length is", responseContent.length());
    }

    @Override
    public String getId()
    {
        return (config.getString("adelphic.advertiserId"));
    }

    private String getAdType()
    {
        Integer slot = Integer.parseInt(sasParams.getSlot());
        if (10 == slot // 300X250
                || 14 == slot // 320X480
                || 16 == slot) /* 768X1024 */{
            return "interstitial";
        }
        return "banner";
    }
}