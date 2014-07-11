package com.inmobi.template.formatter;

import java.io.IOException;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;



public class TemplateManager {
	
	private final  Logger            LOG                          = LoggerFactory.getLogger(TemplateManager.class);
	
	private  final StringResourceRepository stringResourceRepository = new StringResourceRepositoryImpl();
	private  final String STRING_REPO_NAME = "rtb-service-template-test-internal-repo";
	private  final VelocityEngine velocityEngine = new VelocityEngine();
	
	private  final String errorStr = "%s can't be null."; 
	private  final String logMsg = "Adding to template cache for site id %s ";
	
	private static TemplateManager SINGLETON = new TemplateManager();
	
	private TemplateManager(){
		init();
	}
	
	private void init(){
		 
	    StringResourceLoader.setRepository(STRING_REPO_NAME, stringResourceRepository);
        velocityEngine.setProperty(Velocity.RESOURCE_LOADER, "string");
        velocityEngine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        velocityEngine.addProperty("string.resource.loader.repository.name", STRING_REPO_NAME);
        velocityEngine.addProperty("string.resource.loader.repository.static", "true");
        velocityEngine.addProperty("string.resource.loader.cache", "false");
		
	}
	
	
	public static TemplateManager getInstance(){
		return SINGLETON;
	}
	
	
	public  void addToTemplateCache(String templateName, String templateContent) {
		Preconditions.checkNotNull(templateName, errorStr,templateName);
		Preconditions.checkNotNull(templateName, errorStr,templateContent);
		
		LOG.debug(String.format(logMsg, templateName));
		
        stringResourceRepository.putStringResource(templateName, templateContent);
	}
	
	public  Template getTemplate(String templateName) {
		return velocityEngine.getTemplate(templateName);
	}

}
