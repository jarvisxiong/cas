package com.inmobi.template.formatter;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;



public class TemplateManager {

	private static final Logger LOG = LoggerFactory.getLogger(TemplateManager.class);

	private final StringResourceRepository stringResourceRepository = new StringResourceRepositoryImpl();
	private static final String STRING_REPO_NAME = "rtb-service-template-test-internal-repo";
	private final VelocityEngine velocityEngine = new VelocityEngine();

	private static final String ERROR_STR = "%s can't be null.";
	private static final String LOG_MSG = "Adding to template cache for site id %s ";

	private static TemplateManager SINGLETON = new TemplateManager();

	private TemplateManager() {
		init();
	}

	private void init() {

		StringResourceLoader.setRepository(STRING_REPO_NAME, stringResourceRepository);
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "string");
		velocityEngine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
		velocityEngine.addProperty("string.resource.loader.repository.name", STRING_REPO_NAME);
		velocityEngine.addProperty("string.resource.loader.repository.static", "true");
		velocityEngine.addProperty("string.resource.loader.cache", "false");

	}


	public static TemplateManager getInstance() {
		return SINGLETON;
	}


	public void addToTemplateCache(final String templateName, final String templateContent) {
		Preconditions.checkNotNull(templateName, ERROR_STR, templateName);
		Preconditions.checkNotNull(templateName, ERROR_STR, templateContent);

		LOG.debug(String.format(LOG_MSG, templateName));

		stringResourceRepository.putStringResource(templateName, templateContent);
	}

	public Template getTemplate(final String templateName) {

		LOG.debug(String.format("GET Template for site : %s", templateName));
		return velocityEngine.getTemplate(templateName);
	}

}
