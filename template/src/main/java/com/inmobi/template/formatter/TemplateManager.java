package com.inmobi.template.formatter;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;

import com.google.common.base.Preconditions;

public class TemplateManager {
    private static final Logger LOG = Logger.getLogger("repository");
    private static final String STRING_REPO_NAME = "rtb-service-template-test-internal-repo";
    private static final String ERROR_STR = "%s can't be null.";
    private static final TemplateManager SINGLETON = new TemplateManager();

    private final StringResourceRepository stringResourceRepository = new StringResourceRepositoryImpl();
    private final VelocityEngine velocityEngine = new VelocityEngine();

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


    /**
     * 
     * @return
     */
    public static TemplateManager getInstance() {
        return SINGLETON;
    }


    /**
     * 
     * @param templateId
     * @param templateContent
     */
    public void addToTemplateCache(final String templateId, final String templateContent) {
        Preconditions.checkNotNull(templateId, ERROR_STR, templateId);
        Preconditions.checkNotNull(templateContent, ERROR_STR, templateContent);
        LOG.debug(String.format("Adding to template cache for templateId id %s ", templateId));
        stringResourceRepository.putStringResource(templateId, templateContent);
    }

    /**
     * 
     * @param templateId
     * @param templateContent
     */
    public void addToTemplateCache(final long templateId, final String templateContent) {
        addToTemplateCache(String.valueOf(templateId), templateContent);
    }

    /**
     * 
     * @param templateId
     * @return
     */
    public Template getTemplate(final String templateId) {
        LOG.debug("GET Template for templateId: " + templateId);
        return velocityEngine.getTemplate(templateId);
    }

    /**
     * 
     * @param templateId
     * @return
     */
    public Template getTemplate(final long templateId) {
        return getTemplate(String.valueOf(templateId));
    }

}
