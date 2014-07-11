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
	
	private final static Logger            LOG                          = LoggerFactory.getLogger(TemplateParser.class);
	
	private Tools tools;
	
	private  MathTool mTool;
	
	private GsonManager gsonManager;
	
	@Inject
	public TemplateParser(TemplateConfiguration config){
		this.tools = config.getTool();
		this.mTool = config.getMathTool();
		this.gsonManager = config.getGsonManager();
	}
	
	private VelocityContext getVelocityContext(){
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("tool", tools);
	    velocityContext.put("math", mTool);
	    return velocityContext;
	}
	
	public String format(String adm,String templateName) throws ResourceNotFoundException, ParseErrorException, Exception{
		App app = gsonManager.createGson().fromJson(adm, App.class);
		return format(app,templateName);
	}
	
	public String format(Context context,String templateName) throws TemplateException{
		
	    //velocityContext.put("config", UnifiedFormatterConfiguration.getInstance());
	    //velocityContext.put("ad", adContext);
	   // List<CreativeContext> creativeContextList = adContext.getCreativeList();
	    //if (!creativeContextList.isEmpty()) {
	      //velocityContext.put("first", new ContextImpl());
	    //ContextImpl c = new ContextImpl();
	    //c.setApp(getSampleApp());
		try{
			VelocityContext velocityContext = getVelocityContext();
		    velocityContext.put("first", context);
		      
	         Template template = TemplateManager.getInstance().getTemplate(templateName);
		     StringWriter writer = new StringWriter();
		     template.merge( velocityContext, writer );
		        /* show the World */
		      return writer.toString();
		}catch(Exception e){
			LOG.error(String.format("Error while fetching template for %s", templateName));
			throw new TemplateException("Exception occured for siteId "+templateName, e);
		}
		
	}
	
	
		    
	
	

}
