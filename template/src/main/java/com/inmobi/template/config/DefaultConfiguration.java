package com.inmobi.template.config;

import org.apache.velocity.tools.generic.MathTool;

import com.google.inject.Inject;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.interfaces.Tools;
import com.inmobi.template.tool.TemplateTool;

import lombok.Data;

@Data
public class DefaultConfiguration implements TemplateConfiguration {
    @Inject
    private GsonManager gsonManager;

    @Inject
    private Tools tool;

    @Inject
    private MathTool mathTool;

    @Inject
    private TemplateTool templateTool;
}
