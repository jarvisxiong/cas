package com.inmobi.template.formatter;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.IOUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inmobi.template.context.CreativeContext;
import com.inmobi.template.exception.TemplateException;

public class TemplateDecorator {
	
	private final static Logger            LOG                          = LoggerFactory.getLogger(TemplateDecorator.class);
	
	private String contextCodeFile;
	
	@Inject
	public TemplateDecorator() {}
	
	@Inject
	public void addContextFile(@Named("ContextCodeFile") String contextCodeVm) throws TemplateException{
		this.contextCodeFile = contextCodeVm;
		String cc = getFileContent(contextCodeFile);
		TemplateManager.getInstance().addToTemplateCache(contextCodeFile, cc);
	}
	
	private String getFileContent(String fileName) throws TemplateException{
		try{
			InputStream is = TemplateDecorator.class.getResourceAsStream(fileName);
			byte[] b = IOUtils.readFully(is, -1, true);
			return new String(b);
		}catch(Exception e){
			throw new TemplateException("Error while reading resource", e);
		}
		
	}
	
	public String getContextCode(VelocityContext velocityContext) throws TemplateException{
		return getTemplateContent(velocityContext, contextCodeFile);
	}
	
	
	private String getTemplateContent(VelocityContext velocityContext,String templateName) throws TemplateException{
		try{
	         Template template = TemplateManager.getInstance().getTemplate(templateName);
		     StringWriter writer = new StringWriter();
		     template.merge( velocityContext, writer );
		      return writer.toString();
		}catch(Exception e){
			LOG.error(String.format("Error while fetching template for %s", templateName));
			throw new TemplateException("Exception occured for siteId "+templateName, e);
		}
	}
	
	
	
	public static void main(String args[]) throws TemplateException{
		
		CreativeContext cc = new CreativeContext();
		TemplateDecorator td = new TemplateDecorator();
		
	}

}
