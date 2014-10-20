package com.inmobi.adserve.channels.adnetworks.googleadx;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;

public class GoogleAdXAdNetwork extends AbstractDCPAdNetworkImpl {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAdXAdNetwork.class);

    private static final String SCRIPT_END_PART = "<script type=\"text/javascript\" "
            + "src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script>";

    private String googleInMobiPubID = null;
    private int width, height;

    public GoogleAdXAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {

        if (sasParams.getUserAgent() != null && sasParams.getUserAgent().toLowerCase().contains("opera mini")
                || sasParams.getDeviceType() != null && "FEATURE_PHONE".equals(sasParams.getDeviceType())) {
            return false;
        }

        googleInMobiPubID = config.getString("googleadx.googleAdXPublisherID");

        Short slot = sasParams.getSlot();
        if (slot == null || slot == 9 || SlotSizeMapping.getDimension((long) slot) == null) {
            slot = 15;
        }
        final Dimension dim = SlotSizeMapping.getDimension((long) slot);
        width = (int) Math.ceil(dim.getWidth());
        height = (int) Math.ceil(dim.getHeight());
        LOG.debug("Configure parameters inside GoogleAdX returned true");
        return true;
    }

    @Override
    public String getName() {
        return "googleadx";
    }

    @Override
    public String getId() {
        return config.getString("googleadx.advertiserId");
    }

    @Override
    public void generateJsAdResponse() {
        statusCode = HttpResponseStatus.OK.code();
        final VelocityContext context = new VelocityContext();

        final StringBuffer sb = new StringBuffer("<script type=\"text/javascript\">");
        sb.append("google_ad_client = \"").append(googleInMobiPubID).append("\";");
        sb.append("google_ad_slot = \"").append(externalSiteId).append("\";");
        sb.append("google_ad_width = ").append(width).append(";");
        sb.append("google_ad_height = ").append(height).append(";");
        if (!isApp()) {
            sb.append("google_page_url = \"")
                    .append(sasParams.getReferralUrl() != null ? sasParams.getReferralUrl() : sasParams.getAppUrl())
                    .append("\";");
        }
        sb.append("</script>");
        sb.append(SCRIPT_END_PART);

        context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, sb.toString());
        try {
            TemplateType templateType = TemplateType.HTML;
            if (StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource())) {
                templateType = TemplateType.WAP_HTML_JS_AD_TAG;
            }

            responseContent = Formatter.getResponseFromTemplate(templateType, context, sasParams, beaconUrl);
            adStatus = "AD";
        } catch (final Exception exception) {
            adStatus = "NO_AD";
            LOG.info("Error generating Static Js adtag for GoogleAdX  : {}", exception);
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public boolean useJsAdTag() {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
        return null;
    }
}
