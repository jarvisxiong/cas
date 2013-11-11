package com.inmobi.adserve.channels.api;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class Formatter {

    public enum TemplateType {
        HTML,
        PLAIN,
        RICH,
        IMAGE,
        RTB_HTML,
        NEXAGE_JS_AD_TAG
    }

    private static final String   WAP = "WAP";
    private static VelocityEngine velocityEngine;
    private static Template       velocityTemplateHtml;
    private static Template       velocityTemplatePlainTxt;
    private static Template       velocityTemplateRichTxt;
    private static Template       velocityTemplateImg;
    private static Template       velocityTemplateRtb;
    private static Template       velocityTemplateJsAdTag;

    public static void init() throws Exception {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty("file.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        velocityTemplateHtml = velocityEngine.getTemplate("htmlAdFormat.vm");
        velocityTemplatePlainTxt = velocityEngine.getTemplate("plainTextFormat.vm");
        velocityTemplateRichTxt = velocityEngine.getTemplate("richTxtFormat.vm");
        velocityTemplateImg = velocityEngine.getTemplate("ImageAdFormat.vm");
        velocityTemplateRtb = velocityEngine.getTemplate("rtbHtmlAdFormat.vm");
        velocityTemplateJsAdTag = velocityEngine.getTemplate("nexageJsAdTag.vm");
    }

    static void updateVelocityContext(VelocityContext context, SASRequestParameters sasParams, String beaconUrl,
            DebugLogger logger) {
        if (StringUtils.isNotBlank(beaconUrl)) {
            context.put(VelocityTemplateFieldConstants.IMBeaconUrl, beaconUrl);
        }
        if (!WAP.equalsIgnoreCase(sasParams.getSource())) {
            context.put(VelocityTemplateFieldConstants.APP, true);
            context.put(VelocityTemplateFieldConstants.SDK360Onwards, requestFromSDK360Onwards(sasParams, logger));
            if (StringUtils.isNotBlank(sasParams.getImaiBaseUrl())) {
                context.put(VelocityTemplateFieldConstants.IMAIBaseUrl, sasParams.getImaiBaseUrl());
            }
        }
    }

    static boolean requestFromSDK360Onwards(SASRequestParameters sasParams, DebugLogger logger) {
        if (StringUtils.isBlank(sasParams.getSdkVersion())) {
            return false;
        }
        try {
            String os = sasParams.getSdkVersion();
            if ((os.startsWith("i") || os.startsWith("a"))
                    && Integer.parseInt(sasParams.getSdkVersion().substring(1, 3)) > 35) {
                return true;
            }
        }
        catch (StringIndexOutOfBoundsException e2) {
            logger.debug("Invalid sdkversion ", e2.getMessage());
        }
        catch (NumberFormatException e3) {
            logger.debug("Invalid sdkversion ", e3.getMessage());
        }
        return false;
    }

    public static String getResponseFromTemplate(TemplateType type, VelocityContext context,
            SASRequestParameters sasParams, String beaconUrl, DebugLogger logger) throws ResourceNotFoundException,
            ParseErrorException, MethodInvocationException, IOException {
        updateVelocityContext(context, sasParams, beaconUrl, logger);
        StringWriter writer = new StringWriter();
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
            case NEXAGE_JS_AD_TAG:
                velocityTemplateJsAdTag.merge(context, writer);
                break;
        }
        return writer.toString();
    }

    public static String getRichTextTemplateForSlot(String slot) {
        if (slot == null) {
            return null;
        }
        if (slot.equals("1") || slot.equals("1.0")) {
            return "template_120_20";
        }
        if (slot.equals("2") || slot.equals("2.0")) {
            return "template_168_28";
        }
        if (slot.equals("3") || slot.equals("3.0")) {
            return "template_216_36";
        }
        if (slot.equals("4") || slot.equals("4.0")) {
            return "template_300_50";
        }
        if (slot.equals("9") || slot.equals("9.0")) {
            return "template_320_48";
        }
        if (slot.equals("11") || slot.equals("11.0")) {
            return "template_728_90";
        }
        if (slot.equals("12") || slot.equals("12.0")) {
            return "template_468_60";
        }
        if (slot.equals("15") || slot.equals("15.0")) {
            return "template_320_50";
        }
        return null;
    }
}
