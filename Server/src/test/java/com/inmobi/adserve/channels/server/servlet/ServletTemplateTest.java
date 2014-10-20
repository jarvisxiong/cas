package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeAdTemplateRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.Utils.TestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NativeAdTemplateRepository.class, NativeAdTemplateEntity.class})
public class ServletTemplateTest {

	private Map<String, List<String>> createMapFromString(final String siteId) {
		final Map<String, List<String>> params = new HashMap<>();
		if (null != siteId) {
			params.put("siteId", Arrays.asList(siteId));
		}
		return params;
	}

	@Test
	public void testHandleRequestSiteIdFoundEntityNotNull() throws Exception {
		final String siteId = TestUtils.SampleStrings.siteId;

		mockStatic(CasConfigUtil.class);
		final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
		final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
		final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
		final ResponseSender mockResponseSender = createMock(ResponseSender.class);
		final NativeAdTemplateRepository mockTemplateRepository = createMock(NativeAdTemplateRepository.class);
		final NativeAdTemplateEntity mockEntity = createMock(NativeAdTemplateEntity.class);

		expect(mockQueryStringDecoder.parameters()).andReturn(createMapFromString(siteId)).times(1);
		expect(mockRepositoryHelper.getNativeAdTemplateRepository()).andReturn(mockTemplateRepository).times(1);
		expect(mockTemplateRepository.query(siteId)).andReturn(mockEntity).times(1);
		expect(mockEntity.getJSON()).andReturn("{dummy}").times(1);
		mockResponseSender.sendResponse("{dummy}", null);
		expectLastCall().times(1);

		replayAll();
		mockHttpRequestHandler.responseSender = mockResponseSender;
		CasConfigUtil.repositoryHelper = mockRepositoryHelper;

		final ServletTemplate tested = new ServletTemplate();
		tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

		verifyAll();
	}

	@Test
	public void testHandleRequestSiteIdFoundEntityNull() throws Exception {
		final String siteId = TestUtils.SampleStrings.siteId;

		mockStatic(CasConfigUtil.class);
		final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
		final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
		final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
		final ResponseSender mockResponseSender = createMock(ResponseSender.class);
		final NativeAdTemplateRepository mockTemplateRepository = createMock(NativeAdTemplateRepository.class);

		expect(mockQueryStringDecoder.parameters()).andReturn(createMapFromString(siteId)).times(1);
		expect(mockRepositoryHelper.getNativeAdTemplateRepository()).andReturn(mockTemplateRepository).times(1);
		expect(mockTemplateRepository.query(siteId)).andReturn(null).times(1);
		mockResponseSender.sendResponse("No template found for site Id " + siteId, null);
		expectLastCall().times(1);

		replayAll();
		mockHttpRequestHandler.responseSender = mockResponseSender;
		CasConfigUtil.repositoryHelper = mockRepositoryHelper;

		final ServletTemplate tested = new ServletTemplate();
		tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

		verifyAll();
	}

	@Test
	public void testHandleRequestSiteIdNotFound() throws Exception {
		final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
		final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
		final ResponseSender mockResponseSender = createMock(ResponseSender.class);

		expect(mockQueryStringDecoder.parameters()).andReturn(createMapFromString(null)).times(1);
		mockResponseSender.sendResponse("Invalid siteId", null);
		expectLastCall().times(1);

		replayAll();
		mockHttpRequestHandler.responseSender = mockResponseSender;

		final ServletTemplate tested = new ServletTemplate();
		tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

		verifyAll();
	}

	@Test
	public void testGetName() throws Exception {
		final ServletTemplate tested = new ServletTemplate();
		assertThat(tested.getName(), is(IsEqual.equalTo("template")));
	}
}
