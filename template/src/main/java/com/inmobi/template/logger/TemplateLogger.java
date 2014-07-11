package com.inmobi.template.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class TemplateLogger {
	
	private final static Logger            LOG                          = LoggerFactory.getLogger(TemplateLogger.class);
	private static TemplateLogger SINGLETON = new TemplateLogger(); 
	private TemplateLogger(){}
	
	
	public static TemplateLogger getInstance(){
		return SINGLETON;
	}
	
	  public void logParameters(Integer asyncType, Integer asyncMethod)
	  {
	      LOG.debug("AsyncType : "+asyncType+" | asyncMethod : "+asyncMethod);
	  }

}
