package com.inmobi.template.module;

import org.apache.velocity.tools.generic.MathTool;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.config.DefaultGsonDeserializerConfiguration;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.GsonDeserializerConfiguration;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.interfaces.Tools;
import com.inmobi.template.tool.TemplateTool;
import com.inmobi.template.tool.ToolsImpl;

/**
 *
 * @author ritwik.kumar
 *
 */
public class TemplateModule extends AbstractModule {

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named("ContextCodeFile")).to("/contextCode.vm");
        bind(TemplateParser.class).asEagerSingleton();
        bind(TemplateDecorator.class).asEagerSingleton();
        bind(GsonDeserializerConfiguration.class).to(DefaultGsonDeserializerConfiguration.class);
        bind(TemplateConfiguration.class).to(defaultTemplateInitializer()).asEagerSingleton();
    }

    /**
     *
     * @return
     */
    private Class<? extends TemplateConfiguration> defaultTemplateInitializer() {
        bind(Tools.class).to(ToolsImpl.class).asEagerSingleton();
        bind(MathTool.class).asEagerSingleton();
        bind(TemplateTool.class).asEagerSingleton();
        bind(GsonManager.class).asEagerSingleton();
        return DefaultConfiguration.class;
    }
}
