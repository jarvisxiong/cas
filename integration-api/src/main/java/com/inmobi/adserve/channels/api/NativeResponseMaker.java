package com.inmobi.adserve.channels.api;

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
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeConstraints;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.Image;
import com.inmobi.template.context.App;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.exception.TemplateException;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.interfaces.TemplateConfiguration;

import lombok.Data;

public class NativeResponseMaker {
    private static final Logger LOG = LoggerFactory.getLogger(NativeResponseMaker.class);
    private static final String ERROR_STR = "%s can't be null.";

    private final TemplateParser templateParser;
    private final TemplateDecorator templateDecorator;
    private final Gson gson;

    @Inject
    public NativeResponseMaker(final TemplateParser templateParser, final TemplateDecorator templateDecorator,
            TemplateConfiguration tc) throws TemplateException {
        gson = tc.getGsonManager().getGsonInstance();
        this.templateParser = templateParser;
        this.templateDecorator = templateDecorator;
    }

    private void checkPreconditions(final Map<String, String> params, final NativeAdTemplateEntity templateEntity) {
        Preconditions.checkNotNull(params, ERROR_STR, "params");
        Preconditions.checkNotNull(params.containsKey("placementId"), ERROR_STR, "placementId");
        Preconditions.checkNotNull(templateEntity, ERROR_STR, "templateEntity");
    }

    public String makeResponse(final BidResponse response, final Map<String, String> params,
            final NativeAdTemplateEntity templateEntity) throws Exception {
        checkPreconditions(params, templateEntity);
        Preconditions.checkNotNull(response, ERROR_STR, "BidResponse");

        final String placementId = params.get("placementId");
        final App app = createNativeAppObject(response.getSeatbid().get(0).getBid().get(0).getAdm(), params);
        validateResponse(app, templateEntity);
        final VelocityContext vc = getVelocityContext(app, params);
        vc.put("NAMESPACE", Formatter.getRTBDNamespace());

        return createNativeAd(vc, app, placementId);
    }

    public String makeDCPNativeResponse(final App app, final Map<String, String> params,
            final NativeAdTemplateEntity templateEntity) throws Exception {
        validateResponse(app, templateEntity);
        final VelocityContext vc = getVelocityContext(app, params);
        vc.put("NAMESPACE", Formatter.getDCPNamespace());

        return createNativeAd(vc, app, params.get("placementId"));
    }

    public String makeIXResponse(final App app, final Map<String, String> params) throws Exception {
        Preconditions.checkNotNull(params, ERROR_STR, "params");
        Preconditions.checkNotNull(params.containsKey("placementId"), ERROR_STR, "placementId");
        final VelocityContext vc = getVelocityContext(app, params);
        vc.put("NAMESPACE", Formatter.getIXNamespace());

        return createNativeAd(vc, app, params.get("placementId"));
    }

    private String createNativeAd(final VelocityContext vc, final App app, final String placementId) throws Exception {
        final String namespace = (String) vc.get("NAMESPACE");
        final String pubContent = templateParser.format(app, placementId);
        final String contextCode = templateDecorator.getContextCode(vc);
        LOG.debug("Making response for placementId : {} ", placementId);
        LOG.debug("namespace : {}", namespace);
        LOG.debug("pubContent : {}", pubContent);
        LOG.debug("contextCode : {}", contextCode);
        return makeNativeAd(pubContent, contextCode, namespace);
    }

    private App createNativeAppObject(final String adm, final Map<String, String> params) throws Exception {
        final App app = gson.fromJson(adm, App.class);
        app.setAdImpressionId(params.get("impressionId"));
        return app;
    }


    private void validateResponse(final App app, final NativeAdTemplateEntity templateEntity) throws Exception {
        final String mandatoryKey = templateEntity.getMandatoryKey();
        final List<Integer> mandatoryList = NativeConstraints.getRTBDMandatoryList(mandatoryKey);
        for (final Integer integer : mandatoryList) {
            switch (integer) {
                case NativeConstraints.ICON_INDEX:
                    if (app.getIcons() == null || app.getIcons().isEmpty()
                            || StringUtils.isEmpty(app.getIcons().get(0).getUrl())) {
                        throw new Exception(String.format(ERROR_STR, "Icon"));
                    }
                    break;
                case NativeConstraints.SCREEN_SHOT_INDEX:
                    if (app.getScreenshots() == null || app.getScreenshots().isEmpty()) {
                        throw new Exception(String.format(ERROR_STR, "Image"));
                    }
                    break;
                case NativeConstraints.TITLE_INDEX:
                    if (StringUtils.isEmpty(app.getTitle())) {
                        throw new Exception(String.format(ERROR_STR, "Title"));
                    }
                    break;
                case NativeConstraints.DESCRIPTION_INDEX:
                    if (StringUtils.isEmpty(app.getDesc())) {
                        throw new Exception(String.format(ERROR_STR, "Description"));
                    }
                    break;
                default:
                    break;
            }
        }

        final Image image = NativeConstraints.getRTBImage(templateEntity.getImageKey());
        if (image != null) {
            final Screenshot screenShot = app.getScreenshots().get(0);
            if (!(screenShot.getW() >= image.getMinwidth() && screenShot.getW() <= image.getMaxwidth())) {
                throw new Exception(String.format("Expected image constraints are %s. But got image attributes : %s ",
                        image, screenShot));
            }
        }
    }


    private VelocityContext getVelocityContext(final App app, final Map<String, String> params) {
        final VelocityContext context = new VelocityContext();
        context.put("LANDING_PAGE", app.getOpeningLandingUrl());
        context.put("OLD_LANDING_PAGE", app.getOpeningLandingUrl());
        context.put("TRACKING_CODE", getTrackingCode(params, app));
        context.put("BEACON_URL", params.get("beaconUrl"));
        context.put("CLICK_TRACKER", getClickUrl(params, app));
        return context;
    }

    private static String constructBeaconUrl(final String url) {
        return String.format("<img src=\\\"%s\\\" style=\\\"display:none;\\\" />", url);
    }

    protected String getTrackingCode(final Map<String, String> params, final App app) {
        final StringBuilder bcu = new StringBuilder();
        try {
            final String nUrl = params.get("nUrl");
            if (nUrl != null) {
                bcu.append(constructBeaconUrl(nUrl));
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
