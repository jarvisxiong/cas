package com.inmobi.adserve.channels.api;

import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;

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

import java.io.IOException;
import java.io.StringWriter;


public class Formatter {

    private static final Logger LOG = LoggerFactory.getLogger(Formatter.class);

    public enum TemplateType {
        HTML,
        PLAIN,
        RICH,
        IMAGE,
        RTB_HTML,
        NEXAGE_JS_AD_TAG
    }

    private static final String   APP = "APP";
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

    static void updateVelocityContext(final VelocityContext context, final SASRequestParameters sasParams,
 final String beaconUrl) {
		if (StringUtils.isNotBlank(beaconUrl)) {
			context.put(VelocityTemplateFieldConstants.IMBeaconUrl, beaconUrl);
		}
		
		if (isRequestFromSdk(sasParams)) {
				context.put(VelocityTemplateFieldConstants.APP, true);
				context.put(VelocityTemplateFieldConstants.SDK360Onwards,
						requestFromSDK360Onwards(sasParams));
			if (StringUtils.isNotBlank(sasParams.getImaiBaseUrl())) {
				context.put(VelocityTemplateFieldConstants.IMAIBaseUrl,
						sasParams.getImaiBaseUrl());
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
            String os = sasParams.getSdkVersion();
            if ((os.startsWith("i") || os.startsWith("a"))
                    && Integer.parseInt(sasParams.getSdkVersion().substring(1, 3)) > 35) {
                return true;
            }
        }
        catch (StringIndexOutOfBoundsException e2) {
            LOG.debug("Invalid sdkversion {}", e2);
        }
        catch (NumberFormatException e3) {
            LOG.debug("Invalid sdkversion {}", e3);
        }
        return false;
    }

    public static String getResponseFromTemplate(final TemplateType type, final VelocityContext context,
            final SASRequestParameters sasParams, final String beaconUrl) throws ResourceNotFoundException,
            ParseErrorException, MethodInvocationException, IOException {
        updateVelocityContext(context, sasParams, beaconUrl);
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

    public static String getRichTextTemplateForSlot(final String slot) {
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
