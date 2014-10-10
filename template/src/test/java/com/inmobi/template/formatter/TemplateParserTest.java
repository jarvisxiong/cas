package com.inmobi.template.formatter;

import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.tool.ToolsImpl;
import org.apache.velocity.tools.generic.MathTool;
import org.easymock.classextension.EasyMock;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TemplateParserTest {
    TemplateConfiguration mockTemplateConfig;

    public void prepareMockTemplateConfiguration() {
        mockTemplateConfig = EasyMock.createMock(TemplateConfiguration.class);
        EasyMock.expect(mockTemplateConfig.getTool()).andReturn(new ToolsImpl()).times(1);
        EasyMock.expect(mockTemplateConfig.getMathTool()).andReturn(new MathTool()).times(1);
        EasyMock.expect(mockTemplateConfig.getGsonManager()).andReturn(new GsonManager()).times(1);
        EasyMock.replay(mockTemplateConfig);
    }

    @Before
    public void setUp() throws Exception {
        prepareMockTemplateConfiguration();

    }

    //@Test
    public void testFormat() throws Exception {
        TemplateParser templateParser = new TemplateParser(mockTemplateConfig);

        String adm          = "";
        String templateName = "";

        String expectedPubContent = "";
        String actualPubContent = templateParser.format(adm, templateName);

        assertThat(actualPubContent, is(equalTo(expectedPubContent)));
    }
}