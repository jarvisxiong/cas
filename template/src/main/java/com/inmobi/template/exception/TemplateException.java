package com.inmobi.template.exception;

public class TemplateException extends Exception{
	
	 public TemplateException(String message) {
		 super(message);
    	}
	 
	 public TemplateException(String message,Exception e) {
		 super(message, e);
		}
	

}
