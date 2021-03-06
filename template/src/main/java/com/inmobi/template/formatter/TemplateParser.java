package com.inmobi.template.formatter;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.MathTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.inmobi.template.context.KeyConstants;
import com.inmobi.template.exception.TemplateException;
import com.inmobi.template.interfaces.Context;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.interfaces.Tools;

/**
 *
 * @author ritwik.kumar
 *
 */
public class TemplateParser {
    private final static Logger LOG = LoggerFactory.getLogger(TemplateParser.class);
    private final Tools tools;
    private final MathTool mTool;
    private final ListTool listTool;

    @Inject
    public TemplateParser(final TemplateConfiguration tc) {
        tools = tc.getTool();
        mTool = tc.getMathTool();
        listTool = tc.getListTool();
    }

    /**
     *
     * @return
     */
    private VelocityContext getVelocityContext() {
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tool", tools);
        velocityContext.put("math", mTool);
        velocityContext.put("list", listTool);
        return velocityContext;
    }

    /**
     *
     * @param context
     * @param templateId
     * @return
     * @throws TemplateException
     */
    public String format(final Context context, final String templateId) throws TemplateException {
        LOG.debug("Formating Template for placement : {}", templateId);
        try {
            final VelocityContext velocityContext = getVelocityContext();
            // If Ad is a MovieBoard ad, set velocityContext to be consumed by MovieBoard template
            if (null != context.get(KeyConstants.AD_MOVIEBOARD)) {
                LOG.debug("Setting context to be consumed by MovieBoard template");
                velocityContext.put("ad",context);
                velocityContext.put("vastXml", context.get(KeyConstants.AD_VAST_XML));
            } else {
                velocityContext.put("first", context);
                velocityContext.put("same_ar_screenshots", context.get(KeyConstants.APP_SCREENSHOTS));
            }

            final Template template = TemplateManager.getInstance().getTemplate(templateId);
            final StringWriter writer = new StringWriter();
            template.merge(velocityContext, writer);
            return writer.toString();
        } catch (final Exception e) {
            LOG.error(String.format("Error while merging template for %s", templateId));
            throw new TemplateException("Exception occurred for templateId " + templateId, e);
        }

    }

}
