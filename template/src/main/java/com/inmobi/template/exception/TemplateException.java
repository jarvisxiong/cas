package com.inmobi.template.exception;

public class TemplateException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public TemplateException(final String message) {
    super(message);
  }

  public TemplateException(final String message, final Exception e) {
    super(message, e);
  }


}
