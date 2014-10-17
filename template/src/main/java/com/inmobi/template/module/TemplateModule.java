package com.inmobi.template.module;

import org.apache.velocity.tools.generic.MathTool;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.config.DefaultDeserializerConfiguration;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.DeserializerConfiguration;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.interfaces.Tools;
import com.inmobi.template.tool.ToolsImpl;


public class TemplateModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TemplateParser.class).asEagerSingleton();
    bindConstant().annotatedWith(Names.named("ContextCodeFile")).to("/contextCode.vm");
    bind(TemplateConfiguration.class).to(defaultTemplateInitializer()).asEagerSingleton();
    bind(DeserializerConfiguration.class).to(DefaultDeserializerConfiguration.class);
  }



  /**
   * Just used to created default configuration.
   * 
   * @return
   */
  private Class<? extends TemplateConfiguration> defaultTemplateInitializer() {

    /**
     * Caution : never directly use Tools,MathTool,GsonManager to Inject rather use TemplateConfiguration
     * Implementation. I did it just because I am lazy.
     */
    bind(Tools.class).to(ToolsImpl.class);
    bind(MathTool.class).asEagerSingleton();
    bind(GsonManager.class).asEagerSingleton();
    bind(TemplateDecorator.class).asEagerSingleton();

    return DefaultConfiguration.class;

  }


}
