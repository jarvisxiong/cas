package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.CategoryList;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public abstract class BaseMoolahMediaNetworkImpl extends BaseAdNetworkImpl
{

    protected String                     advertiserId;
    protected String                     publisherId;
    protected String                     advertiserName;

    // Parameters from NAS
    private static Map<Integer, Integer> carrierIdMap;
    private String                       hashScheme = null;
    private static final String          md5Hash    = "md5";
    private static final String          sha1Hash   = "sha1";

    static {
        carrierIdMap = new HashMap<Integer, Integer>();
        carrierIdMap.put(516, 537);
        carrierIdMap.put(394, 534);
        carrierIdMap.put(383, 525);
        carrierIdMap.put(374, 516);
        carrierIdMap.put(515, 510);
        carrierIdMap.put(511, 389);
        carrierIdMap.put(393, 386);
        carrierIdMap.put(402, 383);
        carrierIdMap.put(382, 383);
        carrierIdMap.put(514, 102);
        carrierIdMap.put(395, 87);
        carrierIdMap.put(512, 80);
        carrierIdMap.put(379, 79);
        carrierIdMap.put(378, 77);
        carrierIdMap.put(403, 77);
        carrierIdMap.put(513, 70);
        carrierIdMap.put(392, 40);
        carrierIdMap.put(566, 10);
    }

    public BaseMoolahMediaNetworkImpl(DebugLogger logger, Configuration config, ClientBootstrap clientBootstrap,
            HttpRequestHandlerBase baseRequestHandler, MessageEvent serverEvent)
    {
        super(baseRequestHandler, serverEvent, logger);
        this.clientBootstrap = clientBootstrap;
        this.logger = logger;
    }

    @Override
    protected boolean configureParameters()
    {
        if (sasParams.getRemoteHostIp() == null || StringUtils.isEmpty(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            logger.info("mandate parameters missing for mullah media so exiting adapter");
            return false;
        }
        logger.debug("beacon url inside mullah media is ", beaconUrl);

        source = StringUtils.isBlank(sasParams.getSource()) || "wap".equalsIgnoreCase(sasParams.getSource()) ? "web"
                : "app";
        logger.debug("Configure parameters inside mullah media returned true");
        return true;
    }

    @Override
    public String getName()
    {
        return advertiserName;
    }

    @Override
    public boolean isClickUrlRequired()
    {
        return true;
    }

    @Override
    public String getId()
    {
        return advertiserId;
    }

    @Override
    public void parseResponse(String response, HttpResponseStatus status)
    {
        if (StringUtils.isBlank(response) || status.getCode() != 200 || !response.startsWith("{")
                || response.startsWith("{\"error")) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            logger.debug("beacon url inside mullah media is ", beaconUrl);
            try {
                statusCode = status.getCode();
                JSONObject adResponse = new JSONObject(response);
                VelocityContext context = new VelocityContext();
                context.put(VelocityTemplateFieldConstants.PartnerClickUrl, adResponse
                        .getJSONArray("landing")
                            .getString(0));
                context.put(VelocityTemplateFieldConstants.PartnerImgUrl, adResponse.getJSONArray("img").getString(0));
                context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
                responseContent = Formatter.getResponseFromTemplate(TemplateType.IMAGE, context, sasParams, beaconUrl,
                    logger);
                adStatus = "AD";
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from mullah : ", exception);
                logger.info("Response from mullah:", response);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                logger.info("Error parsing response from mullah : ", exception);
                logger.info("Response from mullah:", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    logger.info("Error while rethrowing the exception : ", e);
                }
            }

        }
        logger.debug("response length is ", responseContent.length());
    }

    public String getRequestParameters() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        sb.append("user_agent=").append(getURLEncode(sasParams.getUserAgent(), format)).append("&user_ip=");
        sb.append(sasParams.getRemoteHostIp())
                    .append("&request_from=indirect&response_format=json&type=")
                    .append(source);
        String uid = getUid();
        if (uid != null) {
            sb.append("&unique_key=").append(uid);
            if (hashScheme != null) {
                sb.append("&hash_scheme=").append(hashScheme);
            }
        }
        if (sasParams.getAge() != null) {
            sb.append("&age=").append(sasParams.getAge());
        }
        if (sasParams.getGender() != null) {
            sb.append("&gender=").append(sasParams.getGender().toUpperCase());
        }
        if (casInternalRequestParameters.zipCode != null) {
            sb.append("&zip=").append(casInternalRequestParameters.zipCode);
        }
        if (sasParams.getCountry() != null) {
            sb.append("&country=").append(sasParams.getCountry());
        }
        if (casInternalRequestParameters.latLong != null && casInternalRequestParameters.latLong.contains(",")) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            sb.append("&lat=").append(latlong[0]).append("&long=").append(latlong[1]);
        }
        Integer carrierId = getCarrierId();
        if (carrierId != null) {
            sb.append("&carrier_id=").append(carrierId);
        }
        if (!StringUtils.isEmpty(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            sb.append("&w=").append(dim.getWidth()).append("&h=").append(dim.getHeight());
            if (sasParams.getSlot().equals("10")) // 300x250
            {
                blindedSiteId += "-300x250";
            }
        }
        sb.append("&sid=")
                    .append(blindedSiteId)
                    .append("&pid=")
                    .append(publisherId)
                    .append("&site_name=")
                    .append(externalSiteId);

        sb.append("&cat=").append(getURLEncode(getCategory(), format));
        logger.debug("post body inside mullah media is ", sb.toString());
        return (sb.toString());
    }

    @Override
    public URI getRequestUri() throws Exception
    {
        try {
            return (new URI(host + "?" + getRequestParameters()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            logger.info(exception.getMessage());
        }
        return null;
    }

    public String getCategory()
    {
        Long[] segmentCategories = entity.getTags();
        if (segmentCategories != null && segmentCategories[0] != 1) {
            for (int index = 0; index < segmentCategories.length; index++) {
                String category = CategoryList.getCategory(segmentCategories[index].intValue());
                logger.debug("segment category is ", category);
                if (category != null) {
                    return category;
                }
            }
        }
        else if (sasParams.getCategories() != null) {
            for (int index = 0; index < sasParams.getCategories().size(); index++) {
                String category = CategoryList.getCategory(sasParams.getCategories().get(index).intValue());
                logger.debug("category is ", category);
                if (category != null) {
                    return category;
                }
            }
        }
        return "miscellenous";
    }

    public Integer getCarrierId()
    {
        try {
            return carrierIdMap.get(sasParams.getCarrier().getInt(0));
        }
        catch (JSONException e) {
            logger.info("Cannot map carrier Id for MM");
            return null;
        }
    }

    /**
     * function returns the unique device id
     * 
     * @return
     */
    @Override
    protected String getUid()
    {
        if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()
                && StringUtils.isNotEmpty(casInternalRequestParameters.uidIFA)) {
            return casInternalRequestParameters.uidIFA;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidMd5)) {
            hashScheme = md5Hash;
            return casInternalRequestParameters.uidMd5;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uid)) {
            hashScheme = md5Hash;
            return casInternalRequestParameters.uid;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidO1)) {
            hashScheme = sha1Hash;
            return casInternalRequestParameters.uidO1;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidSO1)) {
            hashScheme = sha1Hash;
            return casInternalRequestParameters.uidSO1;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidIDUS1)) {
            hashScheme = sha1Hash;
            return casInternalRequestParameters.uidIDUS1;
        }
        return null;
    }
}