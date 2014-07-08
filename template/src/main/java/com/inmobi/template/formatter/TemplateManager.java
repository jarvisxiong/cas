package com.inmobi.template.formatter;

import java.io.IOException;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;



public class TemplateManager {
	
	private static StringResourceRepository stringResourceRepository = new StringResourceRepositoryImpl();
	private static String STRING_REPO_NAME = "service-template-test-internal-repo";
	private static VelocityEngine velocityEngine = new VelocityEngine();
	
	static{
		 
	    StringResourceLoader.setRepository(STRING_REPO_NAME, stringResourceRepository);
        velocityEngine.setProperty(Velocity.RESOURCE_LOADER, "string");
        velocityEngine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        velocityEngine.addProperty("string.resource.loader.repository.name", STRING_REPO_NAME);
        velocityEngine.addProperty("string.resource.loader.repository.static", "true");
        velocityEngine.addProperty("string.resource.loader.cache", "false");
		
	}
	
	
	public static void addToTemplateCache(String templateName, String templateContent) throws IOException{
        stringResourceRepository.putStringResource(templateName, templateContent);
	}
	
	public static Template getTemplate(String templateName) throws ResourceNotFoundException, ParseErrorException, Exception{
		return velocityEngine.getTemplate(templateName);
	}

}
