package com.inmobi.adserve.channels.adnetworks.wapstart;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.xml.sax.InputSource;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.IABCountriesInterface;
import com.inmobi.adserve.channels.util.IABCountriesMap;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;


public class DCPWapStartAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger           LOG           = LoggerFactory.getLogger(DCPWapStartAdNetwork.class);

    private String                        latitude      = null;
    private String                        longitude     = null;
    private int                           width;
    private int                           height;
    private static IABCountriesInterface  iABCountries;
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder        builder;
    private static final String           latlongFormat = "%s,%s";

    static {
        iABCountries = new IABCountriesMap();
    }

    public DCPWapStartAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            LOG.error("XML Parser Builder initialization failed");
        }
    }

    @Override
    public boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for wapstart so exiting adapter");
            return false;
        }
        host = config.getString("wapstart.host");

        if (!StringUtils.isBlank(sasParams.getSlot())
                && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
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
            url.append("?version=2&encoding=1&area=viewBannerXml&ip=").append(sasParams.getRemoteHostIp());
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
            if (sasParams.getCountry() != null) {
                url.append("&countryCode=").append(iABCountries.getIabCountry(sasParams.getCountry()));
            }
            if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
                url.append("&location=")
                        .append(getURLEncode(String.format(latlongFormat, latitude, longitude), format));
            }

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
            try {
                VelocityContext context = new VelocityContext();
                TemplateType t = null;
                Document doc = builder.parse(new InputSource(new java.io.StringReader(response)));
                doc.getDocumentElement().normalize();
                NodeList reportNodes = doc.getElementsByTagName("banner");

                Node rootNode = reportNodes.item(0);
                if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element rootElement = (Element) rootNode;

                    Element partnerClickUrl = (Element) rootElement.getElementsByTagName("link").item(0);
                    Element partnerBeaconElement = (Element) rootElement.getElementsByTagName("cookieSetterUrl")
                            .item(0);

                    String partnerBeacon = partnerBeaconElement.getTextContent();
                    if (StringUtils.isNotEmpty(partnerBeacon)) {
                        context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, partnerBeacon);
                    }

                    context.put(VelocityTemplateFieldConstants.PartnerClickUrl, partnerClickUrl.getTextContent());
                    context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);

                    Element pictureUrl = (Element) rootElement.getElementsByTagName("pictureUrl").item(0);
                    String pictureUrlTxt = pictureUrl.getTextContent();
                    if (StringUtils.isNotEmpty(pictureUrlTxt)) {
                        context.put(VelocityTemplateFieldConstants.PartnerImgUrl, pictureUrlTxt);
                        t = TemplateType.IMAGE;
                    }
                    else {
                        Element title = (Element) rootElement.getElementsByTagName("title").item(0);
                        Element description = (Element) rootElement.getElementsByTagName("content").item(0);
                        context.put(VelocityTemplateFieldConstants.AdText, title.getTextContent());
                        context.put(VelocityTemplateFieldConstants.Description, description.getTextContent());
                        String vmTemplate = Formatter.getRichTextTemplateForSlot(slot);
                        if (!StringUtils.isEmpty(vmTemplate)) {
                            context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
                            t = TemplateType.RICH;
                        }
                        else {
                            t = TemplateType.PLAIN;
                        }
                    }
                }

                statusCode = status.code();
                responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
                adStatus = "AD";
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from WapStart : {}", exception);
                LOG.info("Response from WapStart: {}", response);
            }
        }
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