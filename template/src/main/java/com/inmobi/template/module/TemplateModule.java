package com.inmobi.template.module;
import org.apache.velocity.tools.generic.MathTool;

import com.google.inject.AbstractModule;
import com.inmobi.template.formatter.TemplateManager;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.Tools;
import com.inmobi.template.tool.ToolsImpl;


public class TemplateModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Tools.class).to(ToolsImpl.class).asEagerSingleton();
		bind(TemplateParser.class).asEagerSingleton();
		bind(MathTool.class).asEagerSingleton();
		//bind(TemplateManager.class).asEagerSingleton();
		bind(GsonManager.class).asEagerSingleton();
		
	}

}
