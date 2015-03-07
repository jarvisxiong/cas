package com.inmobi.template.interfaces;

import org.apache.velocity.tools.generic.MathTool;

import com.inmobi.template.gson.GsonManager;

public interface TemplateConfiguration {
    public MathTool getMathTool();
    public Tools getTool();
    public GsonManager getGsonManager();
}
