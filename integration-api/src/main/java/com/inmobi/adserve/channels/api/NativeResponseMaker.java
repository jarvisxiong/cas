package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants.IMAI_BASE_URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.inmobi.adserve.contracts.ump.NativeAd;
import com.inmobi.template.context.App;
import com.inmobi.template.exception.TemplateException;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.interfaces.TemplateConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeResponseMaker {
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
    private static final String URLS = "urls";

    @Inject
    public NativeResponseMaker(final TemplateParser templateParser, final TemplateDecorator templateDecorator,
            final TemplateConfiguration tc) throws TemplateException {
        gson = tc.getGsonManager().getGsonInstance();
        this.templateParser = templateParser;
        this.templateDecorator = templateDecorator;
    }

    public String makeDCPNativeResponse(final App app, final Map<String, String> params, final boolean noJsTracking)
            throws Exception {
        final VelocityContext vcContextCode = getVCForContextCode(app, params);
        vcContextCode.put(NAMESPACE_PARAM, Formatter.getDCPNamespace());
        return createNativeAd(vcContextCode, app, params, noJsTracking);
    }

    public String makeRTBDResponse(final App app, final Map<String, String> params, final boolean noJsTracking)
            throws Exception {
        final VelocityContext vcContextCode = getVCForContextCode(app, params);
        vcContextCode.put(NAMESPACE_PARAM, Formatter.getRTBDNamespace());
        return createNativeAd(vcContextCode, app, params, noJsTracking);
    }

    public String makeIXResponse(final App app, final Map<String, String> params, final boolean noJsTracking)
            throws Exception {
        final VelocityContext vcContextCode = getVCForContextCode(app, params);
        vcContextCode.put(NAMESPACE_PARAM, Formatter.getIXNamespace());
        return createNativeAd(vcContextCode, app, params, noJsTracking);
    }

    private String createNativeAd(final VelocityContext vc, final App app, final Map<String, String> params,
            final boolean noJsTracking) throws Exception {
        final String templateId = params.get(TEMPLATE_ID_PARAM);
        final String pubContent = templateParser.format(app, templateId);
        log.debug("Making response for placementId : {} ", templateId);
        log.debug("pubContent : {}", pubContent);
        if (noJsTracking) {
            final Map<Integer, Map<String, List<String>>> eventTracking = getEventTracking(app, params);
            final String landingPage = app.getOpeningLandingUrl();
            log.debug("landingPage : {}", landingPage);
            log.debug("eventTracking : {}", eventTracking);
            return makeNativeAd(pubContent, null, null, landingPage, eventTracking);
        } else {
            final String contextCode = templateDecorator.getContextCode(vc);
            final String namespace = (String) vc.get(NAMESPACE_PARAM);
            log.debug("namespace : {}", namespace);
            log.debug("contextCode : {}", contextCode);
            return makeNativeAd(pubContent, contextCode, namespace, null, null);
        }
    }

    /**
     * Render and ClientFill are empty in case of IX
     */
    protected Map<Integer, Map<String, List<String>>> getEventTracking(final App app,
            final Map<String, String> params) {
        // click Tracker
        List<String> clickUrls = app.getClickUrls();
        if (clickUrls == null) {
            clickUrls = new ArrayList<>();
        }
        final String inmobiClickUrl = params.get(CLICK_URL_PARAM);
        if (StringUtils.isNotBlank(inmobiClickUrl)) {
            clickUrls.add(inmobiClickUrl);
        }
        final Map<String, List<String>> clickMap = new HashMap<>();
        clickMap.put(URLS, clickUrls);

        // View or Impression Tracker
        final Map<String, List<String>> renderMap = new HashMap<>();
        final List<String> renderTrackers = new ArrayList<>();
        final List<String> pixelUrls = app.getPixelUrls();
        if (CollectionUtils.isNotEmpty(pixelUrls)) {
            renderTrackers.addAll(pixelUrls);
        }
        final String nUrl = params.get(NURL_URL_PARAM);
        if (StringUtils.isNotBlank(nUrl)) {
            renderTrackers.add(nUrl);
        }
        final String winUrl = params.get(WIN_URL_PARAM);
        if (StringUtils.isNotBlank(winUrl)) {
            renderTrackers.add(winUrl);
        }
        renderMap.put(URLS, renderTrackers);

        final Map<Integer, Map<String, List<String>>> eventTracking = new HashMap<>();
        eventTracking.put(TrackerUIInteraction.CLICK.getValue(), clickMap);
        eventTracking.put(TrackerUIInteraction.RENDER.getValue(), renderMap);
        return eventTracking;
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
            log.debug("Exception while parsing response {}", e);
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
        List<String> clickUrls = app.getClickUrls();
        if (null == clickUrls) {
            clickUrls = new ArrayList<>();
        }
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

    protected String makeNativeAd(String pubContent, final String contextCode, final String namespace,
            final String landingPage, final Map<Integer, Map<String, List<String>>> eventTracking)
            throws JSONException {
        pubContent = base64(pubContent);
        final NativeAd nativeAd = new NativeAd(pubContent, contextCode, namespace, landingPage, eventTracking);
        return gson.toJson(nativeAd);
    }

    protected String base64(final String input) {
        // The escaping is not url safe, the input is decoded as base64 utf-8 string
        final Base64 base64 = new Base64();
        return base64.encodeAsString(input.getBytes(Charsets.UTF_8));
    }

}
