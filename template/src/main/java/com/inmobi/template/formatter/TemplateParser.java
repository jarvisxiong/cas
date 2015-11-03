package com.inmobi.template.formatter;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.MathTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
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

    @Inject
    public TemplateParser(final TemplateConfiguration tc) {
        tools = tc.getTool();
        mTool = tc.getMathTool();
    }

    /**
     *
     * @return
     */
    private VelocityContext getVelocityContext() {
        final VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tool", tools);
        velocityContext.put("math", mTool);
        return velocityContext;
    }

    /**
     *
     * @param context
     * @param templateName
     * @return
     * @throws TemplateException
     */
    public String format(final Context context, final String templateName) throws TemplateException {
        LOG.debug("Formating Template for placement : {}", templateName);
        try {
            final VelocityContext velocityContext = getVelocityContext();
            velocityContext.put("first", context);
            velocityContext.put("same_ar_screenshots", context.get("app.screenshots"));

            final Template template = TemplateManager.getInstance().getTemplate(templateName);
            final StringWriter writer = new StringWriter();
            template.merge(velocityContext, writer);
            return writer.toString();
        } catch (final Exception e) {
            LOG.error(String.format("Error while merging template for %s", templateName));
            throw new TemplateException("Exception occurred for placementId " + templateName, e);
        }

    }



}
