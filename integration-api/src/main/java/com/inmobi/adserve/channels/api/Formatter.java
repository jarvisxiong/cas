package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android;
import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.iOS;

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

import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.channels.util.config.GlobalConstant;


public class Formatter {
    private static final Logger LOG = LoggerFactory.getLogger(Formatter.class);

    public enum TemplateType {
        HTML,
        PLAIN,
        RICH,
        IMAGE,
        RTB_HTML,
        INTERSTITIAL_VAST_VIDEO,
        INTERSTITIAL_REWARDED_VAST_VIDEO,
        NEXAGE_JS_AD_TAG,
        WAP_HTML_JS_AD_TAG,
        IX_HTML,
        ADBAY_HTML,
        CAU
    }

    private static VelocityEngine velocityEngine;
    private static Template velocityTemplateHtml;
    private static Template velocityTemplatePlainTxt;
    private static Template velocityTemplateRichTxt;
    private static Template velocityTemplateImg;
    private static Template velocityTemplateRtb;
    private static Template velocityTemplateIx;
    private static Template velocityTemplateInterstitialRewardedVideo;
    private static Template velocityTemplateInterstitialNonRewardedVideo;
    private static Template velocityTemplateJsAdTag;
    private static Template velocityTemplateWapHtmlJsAdTag;
    private static Template velocityTemplateAdbay;
    private static Template velocityTemplateCAU;


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
        velocityTemplateInterstitialRewardedVideo = velocityEngine.getTemplate("ixRewardedVastVideoAdFormat.vm");
        velocityTemplateInterstitialNonRewardedVideo = velocityEngine.getTemplate("ixNonRewardedVastVideoAdFormat.vm");
        velocityTemplateJsAdTag = velocityEngine.getTemplate("nexageJsAdTag.vm");
        velocityTemplateWapHtmlJsAdTag = velocityEngine.getTemplate("wapHtmlAdFormat.vm");
        velocityTemplateAdbay = velocityEngine.getTemplate("adbayHtmlTag.vm");
        velocityTemplateCAU = velocityEngine.getTemplate("cau.vm");
    }

    static void updateVelocityContext(final VelocityContext context, final SASRequestParameters sasParams,
            final String beaconUrl) {
        if (StringUtils.isNotBlank(beaconUrl)) {
            context.put(VelocityTemplateFieldConstants.IM_BEACON_URL, beaconUrl);
        }

        if (isRequestFromSdk(sasParams)) {
            context.put(VelocityTemplateFieldConstants.SDK, true);
            context.put(VelocityTemplateFieldConstants.IS_DEEPLINK_SUPPORTED, sasParams.isDeeplinkingSupported());

            if (Android.getValue() == sasParams.getOsId()) {
                context.put(VelocityTemplateFieldConstants.ANDROID, true);
            } else if (iOS.getValue() == sasParams.getOsId()) {
                context.put(VelocityTemplateFieldConstants.IOS, true);
            }
            context.put(VelocityTemplateFieldConstants.SDK360_ONWARDS, isRequestFromSdkVersionOnwards(sasParams, 360));
            context.put(VelocityTemplateFieldConstants.SDK450_ONWARDS, isRequestFromSdkVersionOnwards(sasParams, 450));
            context.put(VelocityTemplateFieldConstants.SDK500_ONWARDS, isRequestFromSdkVersionOnwards(sasParams, 500));
            if (StringUtils.isNotBlank(sasParams.getImaiBaseUrl())) {
                context.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());
            }
            if (RequestedAdType.INTERSTITIAL == sasParams.getRequestedAdType()) {
                context.put(VelocityTemplateFieldConstants.IS_INTERSTITIAL, true);
            }
        }
    }

    /**
     * The request has to come from inmobi sdk residing in the mobile app
     */
    private static boolean isRequestFromSdk(final SASRequestParameters sasParams) {
        return GlobalConstant.APP.equalsIgnoreCase(sasParams.getSource())
                && StringUtils.isNotBlank(sasParams.getSdkVersion());
    }

    /**
     * Returns true/false depending on whether the request is from sdk version >= version
     *
     * @param sasParams
     * @param version
     * @return true if the request is from sdk version >= version
     */
    public static boolean isRequestFromSdkVersionOnwards(final SASRequestParameters sasParams, final int version) {
        if (StringUtils.isBlank(sasParams.getSdkVersion())) {
            return false;
        }
        try {
            final String os = sasParams.getSdkVersion();
            if ((os.startsWith("i") || os.startsWith("a"))
                    && Integer.parseInt(sasParams.getSdkVersion().substring(1)) >= version) {
                return true;
            }
        } catch (final StringIndexOutOfBoundsException e2) {
            LOG.debug("Invalid sdkversion {}", e2);
        } catch (final NumberFormatException e3) {
            LOG.debug("Invalid sdkversion {}", e3);
        }
        return false;
    }

    /**
     * 
     * @param type
     * @param context
     * @param sasParams
     * @param beaconUrl - Set Null if you have already updated in context
     * @return
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     * @throws IOException
     */
    public static String getResponseFromTemplate(final TemplateType type, final VelocityContext context,
            final SASRequestParameters sasParams, final String beaconUrl) throws ResourceNotFoundException,
            ParseErrorException, MethodInvocationException, IOException {
        LOG.debug("getResponseFromTemplate TemplateType->{} beaconUrl->{}", type, beaconUrl);
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
                break;
            case INTERSTITIAL_VAST_VIDEO:
                velocityTemplateInterstitialNonRewardedVideo.merge(context, writer);
                break;
            case INTERSTITIAL_REWARDED_VAST_VIDEO:
                velocityTemplateInterstitialRewardedVideo.merge(context, writer);
                break;
            case NEXAGE_JS_AD_TAG:
                velocityTemplateJsAdTag.merge(context, writer);
                break;
            case WAP_HTML_JS_AD_TAG:
                velocityTemplateWapHtmlJsAdTag.merge(context, writer);
                break;
            case ADBAY_HTML:
                velocityTemplateAdbay.merge(context, writer);
                break;
            case CAU:
                velocityTemplateCAU.merge(context, writer);
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
        if (GlobalConstant.ONE.equals(slot) || "1.0".equals(slot)) {
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

    public static String getRTBDNamespace() {
        return "im_" + (Math.abs(ThreadLocalRandom.current().nextInt(10000)) + 10000) + "_";
    }

    public static String getIXNamespace() {
        return "im_" + (Math.abs(ThreadLocalRandom.current().nextInt(10000)) + 20000) + "_";
    }

    public static String getDCPNamespace() {
        return "im_" + (Math.abs(ThreadLocalRandom.current().nextInt(20000)) + 30000) + "_";
    }
}
