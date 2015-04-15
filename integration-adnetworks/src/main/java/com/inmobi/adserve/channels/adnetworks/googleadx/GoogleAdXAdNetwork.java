package com.inmobi.adserve.channels.adnetworks.googleadx;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.io.StringWriter;
import java.net.URI;

public class GoogleAdXAdNetwork extends AbstractDCPAdNetworkImpl {

    private static VelocityEngine velocityEngine = new VelocityEngine();
    private static Template velocityAdxTemplate;
    private static Template velocityDFPTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAdXAdNetwork.class);

    private String googleInMobiPubID = null;
    private int width, height;
    private boolean useDFPTag;

    static {
        velocityEngine.setProperty("file.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        velocityAdxTemplate = velocityEngine.getTemplate("google-adx-tag-template.vm");
        velocityDFPTemplate = velocityEngine.getTemplate("google-dfp-tag-template.vm");
    }

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

        Short adapterSlot = selectedSlotId;
        if (adapterSlot == null || adapterSlot == 9 || repositoryHelper.querySlotSizeMapRepository(adapterSlot) == null) {
            adapterSlot = 15;
        }
        final Dimension dim = repositoryHelper.querySlotSizeMapRepository(adapterSlot).getDimension();
        width = (int) Math.ceil(dim.getWidth());
        height = (int) Math.ceil(dim.getHeight());

        if (entity.getAdditionalParams() != null && entity.getAdditionalParams().has("useDFP")) {
            try {
                useDFPTag = entity.getAdditionalParams().getBoolean("useDFP");
            } catch (JSONException e) {
                LOG.debug("GoogleAdX Failed to get useDfp boolean from additional params:{} {}, exception raised {}", entity.getExternalSiteKey(),
                        getName(), e);
            }
        }

        LOG.debug("Configure parameters inside GoogleAdX returned true");
        return true;
    }

    @Override
    public String getName() {
        return "googleadxDCP";
    }

    @Override
    public String getId() {
        return config.getString("googleadx.advertiserId");
    }

    @Override
    public void generateJsAdResponse() {
        statusCode = HttpResponseStatus.OK.code();
        final VelocityContext context = new VelocityContext();

        String referalUrl = sasParams.getReferralUrl();
        if (!isApp()) {
            int index = -1;
            String siteUrl = referalUrl != null ?
                    referalUrl.substring(0, (index = referalUrl.indexOf('?')) == -1 ? referalUrl.length() : index) :
                    sasParams.getAppUrl();
            context.put("googlePageURL", siteUrl);
            context.put("IMBeaconUrl", beaconUrl);
        }

        final StringWriter writer = new StringWriter();

        context.put("googleInMobiPubId", googleInMobiPubID);
        context.put("adTagId", externalSiteId);
        context.put("width", width);
        context.put("height", height);

        try {
            TemplateType templateType = TemplateType.HTML;
            if (StringUtils.isBlank(sasParams.getSource()) || "WAP".equalsIgnoreCase(sasParams.getSource())) {
                templateType = TemplateType.WAP_HTML_JS_AD_TAG;
            }

            if (useDFPTag) {
                velocityDFPTemplate.merge(context, writer);
                responseContent = writer.toString();
            }else {
                velocityAdxTemplate.merge(context, writer);
                context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, writer.toString().trim());
                responseContent = Formatter.getResponseFromTemplate(templateType, context, sasParams, beaconUrl);
            }

            adStatus = "AD";
        } catch (final Exception exception) {
            adStatus = "NO_AD";
            LOG.info("Error generating Static Js adtag for GoogleAdX  : {}", exception);
        }
        LOG.debug("response from Zero {} length is {}",responseContent, responseContent.length());
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
