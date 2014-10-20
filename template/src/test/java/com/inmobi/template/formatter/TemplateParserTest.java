package com.inmobi.template.formatter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.velocity.tools.generic.MathTool;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.tool.ToolsImpl;

public class TemplateParserTest {
	TemplateConfiguration mockTemplateConfig;

	public void prepareMockTemplateConfiguration() {
		mockTemplateConfig = EasyMock.createMock(TemplateConfiguration.class);
		org.easymock.EasyMock.expect(mockTemplateConfig.getTool()).andReturn(new ToolsImpl()).times(1);
		org.easymock.EasyMock.expect(mockTemplateConfig.getMathTool()).andReturn(new MathTool()).times(1);
		org.easymock.EasyMock.expect(mockTemplateConfig.getGsonManager()).andReturn(new GsonManager()).times(1);
		EasyMock.replay(mockTemplateConfig);
	}

	@Before
	public void setUp() throws Exception {
		prepareMockTemplateConfiguration();

	}

	@Ignore
	@Test
	public void testFormat() throws Exception {
		final TemplateParser templateParser = new TemplateParser(mockTemplateConfig);

		final String adm = "";
		final String templateName = "";

		final String expectedPubContent = "";
		final String actualPubContent = templateParser.format(adm, templateName);

		assertThat(actualPubContent, is(equalTo(expectedPubContent)));
	}
}
