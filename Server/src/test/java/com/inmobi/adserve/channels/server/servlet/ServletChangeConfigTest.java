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

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.core.IsEqual;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CasConfigUtil.class, InspectorStats.class})
public class ServletChangeConfigTest {

	@Test
	public void testHandleRequestJsonException() throws Exception {
		mockStatic(InspectorStats.class);
		final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
		final ResponseSender mockResponseSender = createMock(ResponseSender.class);
		final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
		final RequestParser mockRequestParser = createMock(RequestParser.class);

		expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
		expect(mockRequestParser.extractParams(null, "update")).andThrow(new JSONException("Json Exception"));
		mockHttpRequestHandler.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
		expectLastCall().times(1);
		InspectorStats.incrementStatCount(InspectorStrings.JSON_PARSING_ERROR, InspectorStrings.COUNT);
		expectLastCall().times(1);
		mockResponseSender.sendResponse("Incorrect Json", null);
		expectLastCall().times(1);

		replayAll();
		mockHttpRequestHandler.responseSender = mockResponseSender;

		final ServletChangeConfig tested = new ServletChangeConfig(mockRequestParser);
		tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

		verifyAll();
	}

	@Test
	public void testHandleRequestJsonObjectIsNull() throws Exception {
		final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
		final ResponseSender mockResponseSender = createMock(ResponseSender.class);
		final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
		final RequestParser mockRequestParser = createMock(RequestParser.class);

		expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
		expect(mockRequestParser.extractParams(null, "update")).andReturn(null).times(1);
		mockHttpRequestHandler.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
		expectLastCall().times(1);
		mockResponseSender.sendResponse("Incorrect Json", null);
		expectLastCall().times(1);

		replayAll();
		mockHttpRequestHandler.responseSender = mockResponseSender;

		final ServletChangeConfig tested = new ServletChangeConfig(mockRequestParser);
		tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

		verifyAll();
	}

	@Test
	public void testHandleRequestJsonSuccessfull() throws Exception {
		final String configKey1 = "adapter.key";
		final String configKey2 = "server.key";
		final String configKey3 = "reserTimers";
		final String value = "value";
		final String response =
				"Successfully changed Config!!!!!!!!!!!!!!!!!\nThe changes are\nadapter.key=key\nserver.key=key\n";

		mockStatic(CasConfigUtil.class);
		final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
		final ResponseSender mockResponseSender = createMock(ResponseSender.class);
		final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
		final RequestParser mockRequestParser = createMock(RequestParser.class);
		final JSONObject mockJsonObject = createMock(JSONObject.class);
		final Iterator mockIterator = createMock(Iterator.class);
		final Configuration mockConfig = createMock(Configuration.class);

		expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
		expect(mockRequestParser.extractParams(null, "update")).andReturn(mockJsonObject).times(1);
		expect(mockJsonObject.keys()).andReturn(mockIterator).times(1);
		expect(mockJsonObject.getString(configKey1)).andReturn(value).times(1);
		expect(mockJsonObject.getString(configKey2)).andReturn(value).times(1);
		expect(mockIterator.hasNext()).andReturn(true).times(3).andReturn(false).times(1);
		expect(mockIterator.next()).andReturn(configKey1).times(1).andReturn(configKey2).times(1).andReturn(configKey3)
				.times(1);
		expect(CasConfigUtil.getAdapterConfig()).andReturn(mockConfig).times(3);
		expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).times(3);
		expect(mockConfig.containsKey("key")).andReturn(true).times(2);
		expect(mockConfig.getString("key")).andReturn("key").times(2);
		mockConfig.setProperty("key", "value");
		expectLastCall().times(1);
		InspectorStats.resetTimers();
		expectLastCall().times(1);

		mockResponseSender.sendResponse(response, null);
		expectLastCall().times(1);

		replayAll();
		mockHttpRequestHandler.responseSender = mockResponseSender;

		final ServletChangeConfig tested = new ServletChangeConfig(mockRequestParser);
		tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

		verifyAll();
	}

	@Test
	public void testGetName() throws Exception {
		final ServletChangeConfig tested = new ServletChangeConfig(null);
		assertThat(tested.getName(), is(IsEqual.equalTo("configchange")));
	}
}
