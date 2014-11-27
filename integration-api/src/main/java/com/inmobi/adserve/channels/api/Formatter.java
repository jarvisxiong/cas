package com.inmobi.adserve.channels.api;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class Formatter {

    private static final Logger LOG = LoggerFactory.getLogger(Formatter.class);

    public enum TemplateType {
        HTML, PLAIN, RICH, IMAGE, RTB_HTML, RTB_BANNER_VIDEO, NEXAGE_JS_AD_TAG, WAP_HTML_JS_AD_TAG, IX_HTML
    }

    private static final String APP = "APP";
    private static VelocityEngine velocityEngine;
    private static Template velocityTemplateHtml;
    private static Template velocityTemplatePlainTxt;
    private static Template velocityTemplateRichTxt;
    private static Template velocityTemplateImg;
    private static Template velocityTemplateRtb;
    private static Template velocityTemplateIx;
    private static Template velocityTemplateRtbBannerVideo;
    private static Template velocityTemplateJsAdTag;
    private static Template velocityTemplateWapHtmlJsAdTag;

    public static void init() throws Exception {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        velocityTemplateHtml = velocityEngine.getTemplate("htmlAdFormat.vm");
        velocityTemplatePlainTxt = velocityEngine.getTemplate("plainTextFormat.vm");
        velocityTemplateRichTxt = velocityEngine.getTemplate("richTxtFormat.vm");
        velocityTemplateImg = velocityEngine.getTemplate("ImageAdFormat.vm");
        velocityTemplateRtb = velocityEngine.getTemplate("rtbHtmlAdFormat.vm");
        velocityTemplateIx = velocityEngine.getTemplate("ixHtmlAdFormat.vm");
        velocityTemplateRtbBannerVideo = velocityEngine.getTemplate("rtbBannerVideoAdFormat.vm");
        velocityTemplateJsAdTag = velocityEngine.getTemplate("nexageJsAdTag.vm");
        velocityTemplateWapHtmlJsAdTag = velocityEngine.getTemplate("wapHtmlAdFormat.vm");
    }

    static void updateVelocityContext(final VelocityContext context, final SASRequestParameters sasParams,
            final String beaconUrl) {
        if (StringUtils.isNotBlank(beaconUrl)) {
            context.put(VelocityTemplateFieldConstants.IM_BEACON_URL, beaconUrl);
        }

        if (isRequestFromSdk(sasParams)) {
            context.put(VelocityTemplateFieldConstants.SDK, true);
            context.put(VelocityTemplateFieldConstants.SDK360_ONWARDS, requestFromSDK360Onwards(sasParams));
            if (StringUtils.isNotBlank(sasParams.getImaiBaseUrl())) {
                context.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());
            }
        }

    }

    /**
     * The request has to come from inmobi sdk residing in the mobile app
     */
    private static boolean isRequestFromSdk(final SASRequestParameters sasParams) {
        return APP.equalsIgnoreCase(sasParams.getSource()) && StringUtils.isNotBlank(sasParams.getSdkVersion());
    }

    static boolean requestFromSDK360Onwards(final SASRequestParameters sasParams) {
        if (StringUtils.isBlank(sasParams.getSdkVersion())) {
            return false;
        }
        try {
            final String os = sasParams.getSdkVersion();
            if ((os.startsWith("i") || os.startsWith("a"))
                    && Integer.parseInt(sasParams.getSdkVersion().substring(1)) >= 360) {
                return true;
            }
        } catch (final StringIndexOutOfBoundsException e2) {
            LOG.debug("Invalid sdkversion {}", e2);
        } catch (final NumberFormatException e3) {
            LOG.debug("Invalid sdkversion {}", e3);
        }
        return false;
    }

    public static String getResponseFromTemplate(final TemplateType type, final VelocityContext context,
            final SASRequestParameters sasParams, final String beaconUrl) throws ResourceNotFoundException,
            ParseErrorException, MethodInvocationException, IOException {
        updateVelocityContext(context, sasParams, beaconUrl);
        final StringWriter writer = new StringWriter();
        switch (type) {
            case HTML:
                velocityTemplateHtml.merge(context, writer);
                break;
            case PLAIN:
                velocityTemplatePlainTxt.merge(context, writer);
                break;
            case RICH:
                velocityTemplateRichTxt.merge(context, writer);
                break;
            case IMAGE:
                velocityTemplateImg.merge(context, writer);
                break;
            case RTB_HTML:
                velocityTemplateRtb.merge(context, writer);
                break;
            case IX_HTML:
                velocityTemplateIx.merge(context, writer);
            case RTB_BANNER_VIDEO:
                velocityTemplateRtbBannerVideo.merge(context, writer);
                break;
            case NEXAGE_JS_AD_TAG:
                velocityTemplateJsAdTag.merge(context, writer);
                break;
            case WAP_HTML_JS_AD_TAG:
                velocityTemplateWapHtmlJsAdTag.merge(context, writer);
                break;
            default:
                break;
        }
        return writer.toString();
    }

    public static String getRichTextTemplateForSlot(final String slot) {
        if (slot == null) {
            return null;
        }
        if ("1".equals(slot) || "1.0".equals(slot)) {
            return "template_120_20";
        }
        if ("2".equals(slot) || "2.0".equals(slot)) {
            return "template_168_28";
        }
        if ("3".equals(slot) || "3.0".equals(slot)) {
            return "template_216_36";
        }
        if ("4".equals(slot) || "4.0".equals(slot)) {
            return "template_300_50";
        }
        if ("9".equals(slot) || "9.0".equals(slot)) {
            return "template_320_48";
        }
        if ("11".equals(slot) || "11.0".equals(slot)) {
            return "template_728_90";
        }
        if ("12".equals(slot) || "12.0".equals(slot)) {
            return "template_468_60";
        }
        if ("15".equals(slot) || "15.0".equals(slot)) {
            return "template_320_50";
        }
        return null;
    }

    public static String getNamespace() {
        return "im_" + (Math.abs(ThreadLocalRandom.current().nextInt(10000)) + 10000) + "_";
    }
}
