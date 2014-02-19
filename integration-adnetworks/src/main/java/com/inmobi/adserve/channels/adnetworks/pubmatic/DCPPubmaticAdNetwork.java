package com.inmobi.adserve.channels.adnetworks.pubmatic;

import java.awt.Dimension;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
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

    public DCPPubmaticAdNetwork(final Configuration config, final ClientBootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        super(config, clientBootstrap, baseRequestHandler, serverEvent);
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
            request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
            LOG.debug("host name is {}", uri.getHost());
            request.setHeader(HttpHeaders.Names.HOST, uri.getHost());
            LOG.debug("got the host");
            request.setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent());
            request.setHeader(HttpHeaders.Names.REFERER, uri.toString());
            request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES);
            request.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp());
            request.setHeader("RLNClientIpAddr", sasParams.getRemoteHostIp());
            ChannelBuffer buffer = ChannelBuffers.copiedBuffer(getRequestParams(), CharsetUtil.UTF_8);
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buffer.readableBytes()));
            request.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-www-form-urlencoded");
            request.setContent(buffer);
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

        if (null == response || status.getCode() != 200 || response.trim().isEmpty()) {
            statusCode = status.getCode();
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
                statusCode = status.getCode();
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