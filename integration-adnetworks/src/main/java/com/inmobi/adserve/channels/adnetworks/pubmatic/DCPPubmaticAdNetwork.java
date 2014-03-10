package com.inmobi.adserve.channels.adnetworks.pubmatic;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.awt.Dimension;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.server.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPPubmaticAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger          LOG         = LoggerFactory.getLogger(DCPPubmaticAdNetwork.class);

    private transient String             pubId;
    private String                       latlong     = null;
    private int                          width;
    private int                          height;
    private String                       deviceId;
    private String                       adId;
    private final String                 dateFormat  = "yyyy-MM-dd HH:mm:ss";
    private static String                creativeTag = "creative_tag";
    private static String                trackingUrl = "tracking_url";
    private static String                pubMaticBid = "PubMatic_Bid";
    private static IABCountriesInterface iABCountries;

    static {
        iABCountries = new IABCountriesMap();
    }

    public DCPPubmaticAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for pubmatic so exiting adapter");
            return false;
        }

        host = config.getString("pubmatic.host");
        pubId = config.getString("pubmatic.pubId");

        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
            try {
                JSONObject additionalParams = entity.getAdditionalParams();
                // ad id is configured as the additional param in the
                // segment table
                adId = additionalParams.getString(sasParams.getSlot());

            }
            catch (Exception e) {
                LOG.error("AdId is not configured for the segment:{}", entity.getExternalSiteKey());
                return false;
            }
        }
        else {
            LOG.debug("mandate parameters missing for pubmatic, so returning from adapter");
            return false;
        }

        deviceId = getUid();
        if (!"wap".equalsIgnoreCase(sasParams.getSource()) && StringUtils.isBlank(deviceId)) { // deviceid mandatory for
                                                                                               // App traffic
            LOG.debug("mandate parameters missing for pubmatic, so returning from adapter");
            return false;
        }
        if (casInternalRequestParameters.latLong != null) {
            String[] t = casInternalRequestParameters.latLong.split(",");
            if (t.length > 1) {
                latlong = String.format("%s,%s", t[0], t[1]);
            }
        }

        LOG.info("Configure parameters inside pubmatic returned true");
        return true;
    }

    @Override
    public String getName() {
        return "pubmatic";
    }

    public String getRequestParams() {
        StringBuilder params = new StringBuilder(
                "timezone=0&frameName=test&inIframe=1&adVisibility=0&adPosition=-1x-1&operId=201&pubId=");
        params.append(pubId);
        params.append("&adId=").append(adId);
        params.append("&siteId=").append(externalSiteId);

        if (sasParams.getGender() != null) {
            params.append("&gender=").append(sasParams.getGender());
        }
        if (sasParams.getAge() != null) {
            params.append("&yob=").append(getYearofBirth());
        }

        if (!StringUtils.isEmpty(latlong)) {
            params.append("&loc=").append(latlong);
        }

        if (casInternalRequestParameters.zipCode != null) {
            params.append("&zip=").append(casInternalRequestParameters.zipCode);
        }

        if (sasParams.getCountry() != null) {
            params.append("&country=").append(iABCountries.getIabCountry(sasParams.getCountry()));
        }

        params.append("&udid=").append(deviceId);
        params.append("&kadwidth=").append(width).append("&kadheight=").append(height);
        params.append("&pageURL=").append(blindedSiteId);
        params.append("&keywords=").append(getURLEncode(getCategories(','), format));
        SimpleDateFormat dfm = new SimpleDateFormat(dateFormat);
        params.append("&kltstamp=").append(getURLEncode(dfm.format(Calendar.getInstance().getTime()), format));
        params.append("&ranreq=").append(Math.random());

        LOG.debug("pubmatic url is {}", params);

        return params.toString();
    }

    // form httprequest
    @Override
    public HttpRequest getHttpRequest() throws Exception {
        try {
            URI uri = getRequestUri();
            requestUrl = uri.toString();
            ByteBuf buffer = Unpooled.copiedBuffer(getRequestParams(), CharsetUtil.UTF_8);
            // TODO: remove header validation
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString(), buffer,
                    true);
            LOG.debug("host name is {}", uri.getHost());
            request.headers().set(HttpHeaders.Names.HOST, uri.getHost());
            LOG.debug("got the host");
            request.headers().set(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent());
            request.headers().set(HttpHeaders.Names.REFERER, uri.toString());
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES);
            request.headers().set("X-Forwarded-For", sasParams.getRemoteHostIp());
            request.headers().set("RLNClientIpAddr", sasParams.getRemoteHostIp());
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buffer.readableBytes()));
            request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/x-www-form-urlencoded");
        }
        catch (Exception ex) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.HTTPREQUEST_ERROR;
            LOG.info("Error in making http request {}", ex);
        }
        return request;
    }

    @Override
    public URI getRequestUri() throws Exception {
        return new URI(host);
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);

        if (null == response || status.code() != 200 || response.trim().isEmpty()) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            String htmlCode = "";
            String partnerBeacon = null;
            try {
                statusCode = status.code();
                JSONObject adResponse = new JSONObject(response.replace("\n", " "));
                adResponse = adResponse.getJSONObject(pubMaticBid);
                htmlCode = adResponse.getString(creativeTag).trim();
                partnerBeacon = adResponse.getString(trackingUrl);
            }
            catch (JSONException exception) {
                adStatus = "NO_AD";
                LOG.debug("Error parsing response from pubmatic : {}", exception);
                LOG.info("Response from pubmatic NO_AD: {}", response);
                return;
            }
            catch (Exception ex) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from pubmatic : {}", ex);
                LOG.info("Response from pubmatic: {}", response);
                try {
                    throw ex;
                }
                catch (Exception e) {
                    LOG.info("Error while rethrowing the exception : {}", e);
                    return;
                }
            }

            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, partnerBeacon);
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, htmlCode);
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from pubmatic : {}", exception);
                LOG.info("Response from pubmatic: {}", response);
            }
        }
        LOG.debug("response length is {}", responseContent);
    }

    @Override
    public String getId() {
        return (config.getString("pubmatic.advertiserId"));
    }
}