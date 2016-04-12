package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.IMAI_BASE_URL;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.inmobi.template.context.App;
import com.inmobi.template.exception.TemplateException;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.interfaces.TemplateConfiguration;

import lombok.Data;

public class NativeResponseMaker {
    private static final Logger LOG = LoggerFactory.getLogger(NativeResponseMaker.class);
    private static final String ERROR_STR = "%s can't be null.";
    public static final String NAMESPACE_PARAM = "NAMESPACE";
    public static final String TEMPLATE_ID_PARAM = "templateId";
    public static final String BEACON_URL_PARAM = "beaconUrl";
    public static final String CLICK_URL_PARAM = "clickUrl";
    public static final String WIN_URL_PARAM = "winUrl";
    public static final String NURL_URL_PARAM = "nUrl";
    private final TemplateParser templateParser;
    private final TemplateDecorator templateDecorator;
    private final Gson gson;

    @Inject
    public NativeResponseMaker(final TemplateParser templateParser, final TemplateDecorator templateDecorator,
            final TemplateConfiguration tc) throws TemplateException {
        gson = tc.getGsonManager().getGsonInstance();
        this.templateParser = templateParser;
        this.templateDecorator = templateDecorator;
    }

    public String makeDCPNativeResponse(final App app, final Map<String, String> params) throws Exception {
        final VelocityContext vcContextCode = getVCForContextCode(app, params);
        vcContextCode.put(NAMESPACE_PARAM, Formatter.getDCPNamespace());
        return createNativeAd(vcContextCode, app, params.get(TEMPLATE_ID_PARAM));
    }

    public String makeRTBDResponse(final App app, final Map<String, String> params) throws Exception {
        final VelocityContext vcContextCode = getVCForContextCode(app, params);
        vcContextCode.put(NAMESPACE_PARAM, Formatter.getRTBDNamespace());
        return createNativeAd(vcContextCode, app, params.get(TEMPLATE_ID_PARAM));
    }

    public String makeIXResponse(final App app, final Map<String, String> params) throws Exception {
        final VelocityContext vcContextCode = getVCForContextCode(app, params);
        vcContextCode.put(NAMESPACE_PARAM, Formatter.getIXNamespace());
        return createNativeAd(vcContextCode, app, params.get(TEMPLATE_ID_PARAM));
    }

    private String createNativeAd(final VelocityContext vc, final App app, final String templateId) throws Exception {
        final String namespace = (String) vc.get(NAMESPACE_PARAM);
        final String pubContent = templateParser.format(app, templateId);
        final String contextCode = templateDecorator.getContextCode(vc);
        LOG.debug("Making response for templateId : {} ", templateId);
        LOG.debug("namespace : {}", namespace);
        LOG.debug("pubContent : {}", pubContent);
        LOG.debug("contextCode : {}", contextCode);
        return makeNativeAd(pubContent, contextCode, namespace);
    }

    private VelocityContext getVCForContextCode(final App app, final Map<String, String> params) {
        Preconditions.checkNotNull(params, ERROR_STR, "params");
        Preconditions.checkNotNull(params.containsKey(TEMPLATE_ID_PARAM), ERROR_STR, TEMPLATE_ID_PARAM);

        final VelocityContext context = new VelocityContext();
        context.put("LANDING_PAGE", app.getOpeningLandingUrl());
        context.put("OLD_LANDING_PAGE", app.getOpeningLandingUrl());
        context.put("TRACKING_CODE", getTrackingCode(params, app));
        context.put("BEACON_URL", params.get(BEACON_URL_PARAM));
        context.put("CLICK_TRACKER", getClickUrl(app, params.get(CLICK_URL_PARAM)));
        context.put(IMAI_BASE_URL, params.get(IMAI_BASE_URL));
        return context;
    }

    private static String constructBeaconUrl(final String url) {
        return String.format("<img src=\\\"%s\\\" style=\\\"display:none;\\\" />", url);
    }

    protected String getTrackingCode(final Map<String, String> params, final App app) {
        final StringBuilder bcu = new StringBuilder();
        try {
            final String nUrl = params.get(NURL_URL_PARAM);
            if (nUrl != null) {
                bcu.append(constructBeaconUrl(nUrl));
            }

        } catch (final Exception e) {
            LOG.debug("Exception while parsing response {}", e);
        }

        final String winUrl = params.get(WIN_URL_PARAM);
        if (!StringUtils.isEmpty(winUrl)) {
            bcu.append(constructBeaconUrl(winUrl));
        }

        final List<String> pixelurls = app.getPixelUrls();
        if (pixelurls != null) {
            for (final String purl : pixelurls) {
                bcu.append(constructBeaconUrl(purl));
            }
        }
        return bcu.toString();
    }

    private String getClickUrl(final App app, final String inmobiClickUrl) {
        final StringBuilder ct = new StringBuilder();
        final List<String> clickUrls = app.getClickUrls();
        if (StringUtils.isNotBlank(inmobiClickUrl)) {
            clickUrls.add(inmobiClickUrl);
        }
        if (clickUrls != null) {
            int i = 0;
            for (; i < clickUrls.size() - 1; i++) {
                ct.append("\"").append(clickUrls.get(i)).append("\"").append(",");
            }

            if (!clickUrls.isEmpty()) {
                ct.append("\"").append(clickUrls.get(i)).append("\"");
            }
        }
        return ct.toString();
    }

    public String makeNativeAd(String pubContent, final String contextCode, final String namespace)
            throws JSONException {
        pubContent = base64(pubContent);
        final NativeAd nativeAd = new NativeAd(pubContent, contextCode, namespace);
        return gson.toJson(nativeAd);
    }

    public String base64(final String input) {
        // The escaping is not url safe, the input is decoded as base64 utf-8 string
        final Base64 base64 = new Base64();
        return base64.encodeAsString(input.getBytes(Charsets.UTF_8));
    }

    @Data
    private static class NativeAd {
        private final String pubContent;
        private final String contextCode;
        private final String namespace;
    }

}
