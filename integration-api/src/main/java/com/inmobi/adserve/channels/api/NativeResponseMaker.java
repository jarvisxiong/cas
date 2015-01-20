package com.inmobi.adserve.channels.api;

import java.util.List;
import java.util.Map;

import lombok.Data;

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
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeConstrains;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.Image;
import com.inmobi.template.context.App;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.exception.TemplateException;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.interfaces.TemplateConfiguration;

public class NativeResponseMaker {

    private static final Logger LOG = LoggerFactory.getLogger(NativeResponseMaker.class);
    private static final String ERROR_STR = "%s can't be null.";

    private final TemplateParser templateParser;
    private final TemplateDecorator templateDecorator;
    private Gson gson = null;

    @Inject
    public NativeResponseMaker(final TemplateParser parser, final TemplateConfiguration tc) throws TemplateException {
        gson = tc.getGsonManager().createGson();
        templateParser = parser;
        templateDecorator = tc.getTemplateDecorator();
    }

    private void checkPreconditions (final Map<String, String> params,
                                     final NativeAdTemplateEntity templateEntity) {
        Preconditions.checkNotNull(params, ERROR_STR, "params");
        Preconditions.checkNotNull(params.containsKey("siteId"), ERROR_STR, "siteId");
        Preconditions.checkNotNull(templateEntity, ERROR_STR, "templateEntity");
    }

    public String makeResponse(final BidResponse response, final Map<String, String> params,
            final NativeAdTemplateEntity templateEntity) throws Exception {
        checkPreconditions(params, templateEntity);
        Preconditions.checkNotNull(response, ERROR_STR, "BidResponse");

        final String siteId = params.get("siteId");
        final App app = createNativeAppObject(response.getSeatbid().get(0).getBid().get(0).getAdm(), params);
        validateResponse(app, templateEntity);
        final VelocityContext vc = getVelocityContext(app, response, params);

        return createNativeAd(vc, app, siteId);
    }

    public String makeHostedResponse(final String adm, final Map<String, String> params,
                                     final NativeAdTemplateEntity templateEntity) throws Exception {
        checkPreconditions(params, templateEntity);
        Preconditions.checkNotNull(adm, ERROR_STR, "AdMarkup");

        final String siteId = params.get("siteId");
        final App app = createNativeAppObject(adm, params);
        validateResponse(app, templateEntity);
        final VelocityContext vc = getVelocityContext(app, null, params);

        return createNativeAd(vc, app, siteId);
    }

    private String createNativeAd(final VelocityContext vc, final App app, final String siteId) throws Exception {
        final String namespace = Formatter.getNamespace();
        vc.put("NAMESPACE", namespace);

        final String pubContent = templateParser.format(app, siteId);
        final String contextCode = templateDecorator.getContextCode(vc);

        LOG.debug("Making response for siteId : {} ", siteId);
        return nativeAd(pubContent, contextCode, namespace);
    }

    private App createNativeAppObject(final String adm, final Map<String, String> params) throws Exception {
        final App app = gson.fromJson(adm, App.class);
        app.setAdImpressionId(params.get("impressionId"));
        return app;
    }

    private void validateResponse(final App app, final NativeAdTemplateEntity templateEntity) throws Exception {
        final String mandatoryKey = templateEntity.getMandatoryKey();
        final List<Integer> mandatoryList = NativeConstrains.getMandatoryList(mandatoryKey);
        for (final Integer integer: mandatoryList) {
            switch (integer) {
                case NativeConstrains.ICON:
                    if (app.getIcons() == null || app.getIcons().isEmpty()
                            || StringUtils.isEmpty(app.getIcons().get(0).getUrl())) {
                        throwException(String.format(ERROR_STR, "Icon"));
                    }
                    break;
                case NativeConstrains.MEDIA:
                    if (app.getScreenshots() == null || app.getScreenshots().isEmpty()) {
                        throwException(String.format(ERROR_STR, "Image"));
                    }
                    break;
                case NativeConstrains.HEADLINE:
                    if (StringUtils.isEmpty(app.getTitle())) {
                        throwException(String.format(ERROR_STR, "Title"));
                    }
                    break;
                case NativeConstrains.DESCRIPTION:
                    if (StringUtils.isEmpty(app.getDesc())) {
                        throwException(String.format(ERROR_STR, "Description"));
                    }
                    break;
                default:
                    break;
            }

        }

        final Image image = NativeConstrains.getImage(templateEntity.getImageKey());
        if (image != null) {
            final Screenshot screenShot = app.getScreenshots().get(0);
            if (!(screenShot.getW() >= image.getMinwidth() && screenShot.getW() <= image.getMaxwidth())) {
                throwException(String.format("Expected image constraints are %s. But got image attributes : %s ", image,
                        screenShot));
            }
        }


    }

    private VelocityContext getVelocityContext(final App app, final BidResponse response,
            final Map<String, String> params) {
        final VelocityContext context = new VelocityContext();
        final String impId = app.getAdImpressionId();

        context.put("IMP_ID", impId);
        context.put("LANDING_PAGE", app.getOpeningLandingUrl());
        context.put("OLD_LANDING_PAGE", app.getOpeningLandingUrl());
        context.put("TRACKING_CODE", getTrackingCode(response, params, app));
        context.put("BEACON_URL", params.get("beaconUrl"));
        context.put("CLICK_TRACKER", getClickUrl(params, app));
        return context;
    }

    private static String constructBeaconUrl(final String url) {
        return String.format("<img src=\\\"%s\\\" style=\\\"display:none;\\\" />", url);
    }

    protected String getTrackingCode(final BidResponse response, final Map<String, String> params, final App app) {
        final StringBuilder bcu = new StringBuilder();
        String nUrl = null;
        try {
            if (null != response) {
                nUrl = response.getSeatbid().get(0).getBid().get(0).getNurl();
                if (nUrl != null) {
                    bcu.append(constructBeaconUrl(nUrl));
                }
            }
        } catch (final Exception e) {
            LOG.debug("Exception while parsing response {}", e);
        }

        final String winUrl = params.get("winUrl");
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

    private void throwException(final String message) throws Exception {
        throw new Exception(message);
    }

    private String getClickUrl(final Map<String, String> params, final App app) {
        final StringBuilder ct = new StringBuilder();
        final List<String> clickUrls = app.getClickUrls();
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


    @Data
    private static class NativeAd {
        private final String pubContent;
        private final String contextCode;
        private final String namespace;
    }

    public String nativeAd(String pubContent, final String contextCode, final String namespace) throws JSONException {
        pubContent = base64(pubContent);
        final NativeAd nativeAd = new NativeAd(pubContent, contextCode, namespace);
        return gson.toJson(nativeAd);
    }

    public String base64(final String input) {
        // The escaping is not url safe, the input is decoded as base64 utf-8 string
        final Base64 base64 = new Base64();
        return base64.encodeAsString(input.getBytes(Charsets.UTF_8));
    }

}
