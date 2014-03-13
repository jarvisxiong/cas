package com.inmobi.adserve.channels.adnetworks.drawbridge;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DrawBridgeAdNetwork extends AbstractDCPAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true on
    // success.i
    private static final Logger LOG           = LoggerFactory.getLogger(DrawBridgeAdNetwork.class);

    private String              geoCoordinate = null;
    private Dimension           dim;
    private String              source        = null;
    private int                 filer_iPod    = 0;                                                 // 1 - Only iPod
                                                                                                    // (Filter non
                                                                                                    // iPod), 2 - Filter
                                                                                                    // iPod, 0 &
                                                                                                    // others - Dont
                                                                                                    // filter
    private static final String FILTER_IPOD   = "filter_iPod";

    public DrawBridgeAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    // Configure the request parameters for making the ad call
    @Override
    protected boolean configureParameters() {
        if (StringUtils.isEmpty(sasParams.getUserAgent())) {
            LOG.debug("Drawbridge : user-agent blank/null, mandatory parameter missing");
            return false;
        }
        dim = getAdDimension();
        source = StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource()) ? "0"
                : "1";
        if (casInternalRequestParameters.latLong != null
                && StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
            String[] latlong = casInternalRequestParameters.latLong.split(",");
            geoCoordinate = latlong[0] + "," + latlong[1];
        }
        filer_iPod = entity.getAdditionalParams().optInt(FILTER_IPOD);
        boolean is_iPodRequest = sasParams.getUserAgent().toLowerCase().contains("ipod");
        if (filer_iPod == 1 && !is_iPodRequest) { // 1 - Only iPod (Filter non iPod traffic)
            LOG.debug("Drawbridge : Request is from non iPod device for an iPod segment, so exiting adapter");
            return false;
        }
        if (filer_iPod == 2 && is_iPodRequest) { // 2 - Filter iPod (non iPod traffic only)
            LOG.debug("Drawbridge : Request is from iPod device for a non-iPod segment, so exiting adapter");
            return false;
        }
        LOG.debug("configure parameter successful for drawbridge");
        return true;
    }

    @Override
    public String getName() {
        return "drawbridge";
    }

    @Override
    public String getId() {
        return (config.getString("drawbridge.advertiserId"));
    }

    // get URI
    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder finalUrl = new StringBuilder();
        finalUrl.append(config.getString("drawbridge.host")).append(config.getString("drawbridge.partnerId"))
                .append("&_psign=").append(config.getString("drawbridge.partnerSignature"));
        if (!(StringUtils.isEmpty(sasParams.getRemoteHostIp()) || sasParams.getRemoteHostIp().equals("null"))) {
            finalUrl.append("&_clip=").append(sasParams.getRemoteHostIp());
        }
        if (sasParams.getOsId() == HandSetOS.iPhone_OS.getValue()) {
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidIFA)) {
                finalUrl.append("&_ifa=").append(casInternalRequestParameters.uidIFA);
            }
            if (StringUtils.isNotBlank(casInternalRequestParameters.uidADT)) {
                finalUrl.append("&_optout=").append(casInternalRequestParameters.uidADT);
            }
        }
        // Setting UDID preference IDUS1, UM5, UDID
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidIDUS1)) {
            finalUrl.append("&_did=").append(casInternalRequestParameters.uidIDUS1);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)) {
            finalUrl.append("&_did=").append(casInternalRequestParameters.uidMd5);
        }
        else if (StringUtils.isNotBlank(casInternalRequestParameters.uid)) {
            finalUrl.append("&_did=").append(casInternalRequestParameters.uid);
        }

        if (StringUtils.isNotBlank(casInternalRequestParameters.uidSO1)) {
            finalUrl.append("&_odin1=").append(casInternalRequestParameters.uidSO1);
        }
        if (StringUtils.isNotBlank(casInternalRequestParameters.uidO1)) {
            finalUrl.append("&_macsha1=").append(casInternalRequestParameters.uidO1);
        }

        if (!(StringUtils.isEmpty(sasParams.getUserAgent()) || sasParams.getUserAgent().equals("null"))) {
            finalUrl.append("&_ua=").append(getURLEncode(sasParams.getUserAgent(), format));
        }
        if (!(StringUtils.isEmpty(geoCoordinate) || geoCoordinate.equals("null"))) {
            finalUrl.append("&_geo=").append(geoCoordinate);
        }
        finalUrl.append("&_art=sb");

        if (!(StringUtils.isEmpty(sasParams.getGender()) || sasParams.getGender().equals("null"))) {
            finalUrl.append("&_dgen=").append(sasParams.getGender());
        }
        String temp = getYearofBirth();
        if (!(StringUtils.isEmpty(temp) || temp.equals("null"))) {
            finalUrl.append("&_dyob=").append(temp);
        }
        if (!(StringUtils.isEmpty(casInternalRequestParameters.zipCode) || casInternalRequestParameters.zipCode
                .equals("null"))) {
            finalUrl.append("&_dzip=").append(casInternalRequestParameters.zipCode);
        }
        temp = getCategories(',');
        if (!(StringUtils.isEmpty(temp) || temp.equals("null"))) {
            finalUrl.append("&_pubcat=").append(getURLEncode(temp, format));
        }
        if (null != dim) {
            finalUrl.append("&_adw=").append((int) dim.getWidth()).append("&_adh=").append((int) dim.getHeight());
        }
        if (!(StringUtils.isEmpty(sasParams.getCountry()) || sasParams.getCountry().equals("null"))) {
            finalUrl.append("&_dco=").append(getCountry(sasParams.getCountry()));
        }
        finalUrl.append("&_clickbeacon=").append(getURLEncode(clickUrl, format));
        finalUrl.append("&_aid=").append(blindedSiteId);
        finalUrl.append("&_test=").append(config.getString("drawbridge.test"));
        finalUrl.append("&_impressionbeacon=").append(getURLEncode(beaconUrl, format));
        finalUrl.append("&_app=").append(source);

        LOG.debug("url inside drawbridge: {}", finalUrl);
        try {
            return (new URI(finalUrl.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("Error Forming Url inside drawbridge: {}", exception);
        }
        return null;
    }

    // if we need to send click url
    @Override
    public boolean isClickUrlRequired() {
        return true;
    }

    // get 3 letter country name
    public String getCountry(final String country) {
        try {
            if (country != null) {
                return (new Locale("en", country).getISO3Country());
            }
            return null;
        }
        catch (MissingResourceException exception) {
            LOG.info("3 letter name not found for country");
            return null;
        }
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
            context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, response.trim());
            context.remove(VelocityTemplateFieldConstants.IMBeaconUrl);
            try {
                responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, null);
            }
            catch (Exception exception) {
                adStatus = "NO_AD";
                LOG.info("Error parsing response from drawbridge : {}", exception);
                LOG.info("Response from drawbridge: {}", response);
                try {
                    throw exception;
                }
                catch (Exception e) {
                    LOG.info("Error while rethrowing the exception : {}", e);
                }
            }
            adStatus = "AD";
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    // Generate sha1 hash of uid
    public String getSha1Hash(final String param) {
        if (param != null && param.length() == 32) {
            return param;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(param.getBytes());
            byte[] output = md.digest();
            return (bytesToHex(output));
        }
        catch (NoSuchAlgorithmException exception) {
            LOG.info("error generating hash of uid {}", exception);
            return null;
        }
        catch (NullPointerException exception) {
            LOG.info("error generating hash inside drawbridge {}", exception);
            return null;
        }
    }

    // get ad dimension
    public Dimension getAdDimension() {
        if (sasParams.getSlot() != null && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            return (SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())));
        }
        return (new Dimension(320, 50));
    }

    // convert byte array to hexadecimal value
    public String bytesToHex(final byte[] b) {
        char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        StringBuffer buf = new StringBuffer();
        for (int j = 0; j < b.length; j++) {
            buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
            buf.append(hexDigit[b[j] & 0x0f]);
        }
        return buf.toString();
    }

}