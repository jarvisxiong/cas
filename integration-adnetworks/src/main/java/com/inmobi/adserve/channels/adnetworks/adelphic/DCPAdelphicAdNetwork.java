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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.IABCategoriesInterface;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPAdelphicAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger                 LOG            = LoggerFactory.getLogger(DCPAdelphicAdNetwork.class);

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

    /**
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverEvent
     */
    public DCPAdelphicAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for Adelphic so exiting adapter");
            return false;
        }

        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            LOG.debug("mandate parameters missing for Adelphic, so returning from adapter");
            return false;
        }
        deviceId = getUid();
        if (StringUtils.isEmpty(deviceId)) {
            LOG.debug("mandate parameters missing for Adelphic, so returning from adapter");
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
            LOG.error("Spot Id/Site/pubId is not configured for the segment:{}", entity.getExternalSiteKey());
            return false;
        }
        sourceType = (StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource())) ? 'w'
                : 'a';
        LOG.info("Configure parameters inside Adelphic returned true");
        return true;
    }

    @Override
    public String getName() {
        return "adelphic";
    }

    @Override
    public URI getRequestUri() throws Exception {
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
            LOG.debug("Adelphic url is {}", url);

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
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from Adelphic : {}", exception);
                LOG.info("Response from Adelphic: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("adelphic.advertiserId"));
    }

    private String getAdType() {
        Integer slot = Integer.parseInt(sasParams.getSlot());
        if (10 == slot // 300X250
                || 14 == slot // 320X480
                || 16 == slot) /* 768X1024 */{
            return "interstitial";
        }
        return "banner";
    }
}