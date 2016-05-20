package com.inmobi.template.interfaces;

import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.MathTool;

import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.tool.TemplateTool;

/**
 * 
 * @author ritwik.kumar
 *
 */
public interface TemplateConfiguration {
    /**
     * 
     * @return
     */
    MathTool getMathTool();

    ListTool getListTool();

    /**
     * 
     * @return
     */
    Tools getTool();

    /**
     * 
     * @return
     */
    TemplateTool getTemplateTool();

    /**
     * 
     * @return
     */
    GsonManager getGsonManager();
}
