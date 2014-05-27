package com.inmobi.adserve.channels.adnetworks.wapstart;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;


public class DCPWapStartAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger          LOG           = LoggerFactory.getLogger(DCPWapStartAdNetwork.class);

    private String                       latitude      = null;
    private String                       longitude     = null;
    private int                          width;
    private int                          height;
    private static IABCountriesInterface iABCountries;
    private static final String          latlongFormat = "%s,%s";

    static {
        iABCountries = new IABCountriesMap();
    }

    public DCPWapStartAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);

    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for wapstart so exiting adapter");
            return false;
        }
        host = config.getString("wapstart.host");

        if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
            Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
            width = (int) Math.ceil(dim.getWidth());
            height = (int) Math.ceil(dim.getHeight());
        }
        else {
            LOG.debug("mandate parameters missing for WapStart, so returning from adapter");
            return false;
        }

        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            latitude = latlong[0];
            longitude = latlong[1];

        }

        LOG.info("Configure parameters inside wapstart returned true");
        return true;
    }

    @Override
    public String getName() {
        return "wapstart";
    }

    @Override
    public URI getRequestUri() throws Exception {
        try {
            StringBuilder url = new StringBuilder(host);
            url.append("?version=2&encoding=1&area=viewBanner&ip=").append(sasParams.getRemoteHostIp());
            url.append("&id=").append(externalSiteId);
            String bsiteId = StringUtils.replace(blindedSiteId, "-", "");
            url.append("&pageId=00000000").append(bsiteId);
            url.append("&kws=").append(getURLEncode(getCategories(';'), format));

            // if (sasParams.getGender() != null) {
            // url.append("&sex=").append(sasParams.getGender());
            // }
            if (sasParams.getAge() != null) {
                url.append("&age=").append(sasParams.getAge());
            }
            if (sasParams.getCountryCode() != null) {
                url.append("&countryCode=").append(iABCountries.getIabCountry(sasParams.getCountryCode()));
            }
            if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
                url.append("&location=")
                        .append(getURLEncode(String.format(latlongFormat, latitude, longitude), format));
            }
            url.append("&callbackurl=").append(getURLEncode(clickUrl, format));

            LOG.debug("WapStart url is {}", url);

            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.info("{}", exception);
        }
        return null;
    }

    @Override
    protected Request getNingRequest() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }
        return new RequestBuilder().setURI(uri).setHeader("x-display-metrics", String.format("%sx%s", width, height))
                .setHeader("xplus1-user-agent", sasParams.getUserAgent())
                .setHeader("x-plus1-remote-addr", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us").setHeader(HttpHeaders.Names.REFERER, requestUrl)
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader(HttpHeaders.Names.HOST, uri.getHost())
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp()).build();
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
        	//TODO fix me
        	if(response.trim().length() < 50){
        		adStatus = "NO_AD";
        		statusCode = 500;
        		return;
        	}
            statusCode = status.code();
            VelocityContext context = new VelocityContext();
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());

            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from Wapstart : {}", exception);
                LOG.info("Response from WapStart: {}", response);
                return;
            }
            adStatus = "AD";
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public String getId() {
        return (config.getString("wapstart.advertiserId"));
    }

    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

}
