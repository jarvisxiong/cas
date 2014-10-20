package com.inmobi.template.formatter;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.MathTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.inmobi.template.context.App;
import com.inmobi.template.exception.TemplateException;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.Context;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.interfaces.Tools;


public class TemplateParser {
	private final static Logger LOG = LoggerFactory.getLogger(TemplateParser.class);

	private final Tools tools;
	private final MathTool mTool;
	private final GsonManager gsonManager;

	@Inject
	public TemplateParser(final TemplateConfiguration config) {
		tools = config.getTool();
		mTool = config.getMathTool();
		gsonManager = config.getGsonManager();
	}

	private VelocityContext getVelocityContext() {
		final VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("tool", tools);
		velocityContext.put("math", mTool);
		return velocityContext;
	}

	public String format(final String adm, final String templateName) throws ResourceNotFoundException,
			ParseErrorException, Exception {
		// TODO: Redundant exceptions?
		final App app = gsonManager.createGson().fromJson(adm, App.class);
		return format(app, templateName);
	}

	public String format(final Context context, final String templateName) throws TemplateException {
		LOG.debug("Formating Template for site : {}", templateName);
		try {
			final VelocityContext velocityContext = getVelocityContext();
			velocityContext.put("first", context);
			velocityContext.put("same_ar_screenshots", context.get("app.screenshots"));

			final Template template = TemplateManager.getInstance().getTemplate(templateName);
			final StringWriter writer = new StringWriter();
			template.merge(velocityContext, writer);
			/* show the World */
			return writer.toString();
		} catch (final Exception e) {
			LOG.error(String.format("Error while fetching template for %s", templateName));
			throw new TemplateException("Exception occurred for siteId " + templateName, e);
		}

	}



}
