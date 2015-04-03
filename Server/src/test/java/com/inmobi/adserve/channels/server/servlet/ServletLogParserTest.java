package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

// TODO: Optimise
@RunWith(PowerMockRunner.class)
@PrepareForTest({CasConfigUtil.class, ServletLogParser.class})
public class ServletLogParserTest {

    private Map<String, List<String>> createMapFromStringPair(final String key, final String value) {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(key, Arrays.asList(value));
        return params;
    }

    @Test
    public void testHandleRequestHttpMethodIsPost() throws Exception {
        final String targetStrings = "targetStrings";
        final String logFilePath = "logFilePath";
        // TODO: StringBuilder
        final String jObject = "a=" + targetStrings + "&b=" + logFilePath + "&c&d";

        mockStatic(CasConfigUtil.class);
        final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final FullHttpRequest mockHttpRequest = createMock(FullHttpRequest.class);
        final ByteBuf mockByteBuf = createMock(ByteBuf.class);
        final InputStream mockInputStream = createMock(InputStream.class);
        final InputStreamReader mockInputStreamReader = createMock(InputStreamReader.class);
        final BufferedReader mockBufferedReader = createMock(BufferedReader.class);

        final Configuration mockConfig = createMock(Configuration.class);
        final Process mockProcess = createMock(Process.class);

        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).times(1);
        expect(mockHttpRequest.getMethod()).andReturn(HttpMethod.POST).times(1);
        expect(mockHttpRequest.content()).andReturn(mockByteBuf).times(1);
        expect(mockByteBuf.toString(CharsetUtil.UTF_8)).andReturn(jObject).times(1);
        expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).times(2);
        expect(mockConfig.getString("logParserScript")).andReturn("/opt/bin/mkhoj/parser.sh").times(2);
        expect(mockProcess.waitFor()).andReturn(0).times(1);
        expect(mockProcess.getInputStream()).andReturn(mockInputStream).times(1);
        expect(mockBufferedReader.readLine())
                .andReturn("test").times(1)
                .andReturn(null).times(1);
        expectNew(BufferedReader.class, new Class[] {Reader.class}, mockInputStreamReader)
                .andReturn(mockBufferedReader).anyTimes();
        expectNew(InputStreamReader.class, new Class[]{InputStream.class}, mockInputStream)
                .andReturn(mockInputStreamReader).anyTimes();
        mockBufferedReader.close();
        expectLastCall().times(1);
        mockResponseSender.sendResponse("test\n", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        final ProcessBuilder mockProcessBuilder = createMock(ProcessBuilder.class);
        expect(mockProcessBuilder.start()).andReturn(mockProcess).times(1);
        expectNew(ProcessBuilder.class, CasConfigUtil.getServerConfig().getString("logParserScript"), "-t",
                targetStrings, "-l", logFilePath).andReturn(mockProcessBuilder).times(1);
        replayAll();

        final ServletLogParser tested = new ServletLogParser();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestHttpMethodIsGetKeyIsSearch() throws Exception {
        final String targetStrings = "targetStrings";
        final String logFilePath = "logFilePath";
        // TODO: StringBuilder
        final String jObject = "a=" + targetStrings + "&b=" + logFilePath + "&c&d";

        mockStatic(CasConfigUtil.class);
        final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final FullHttpRequest mockHttpRequest = createMock(FullHttpRequest.class);
        final ByteBuf mockByteBuf = createMock(ByteBuf.class);
        final InputStream mockInputStream = createMock(InputStream.class);
        final InputStreamReader mockInputStreamReader = createMock(InputStreamReader.class);
        final BufferedReader mockBufferedReader = createMock(BufferedReader.class);

        final Configuration mockConfig = createMock(Configuration.class);
        final Process mockProcess = createMock(Process.class);

        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).times(1);
        expect(mockHttpRequest.getMethod()).andReturn(HttpMethod.POST).times(1);
        expect(mockHttpRequest.content()).andReturn(mockByteBuf).times(1);
        expect(mockByteBuf.toString(CharsetUtil.UTF_8)).andReturn(jObject).times(1);
        expect(mockQueryStringDecoder.parameters()).andReturn(createMapFromStringPair("searcH", targetStrings))
                .times(1);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).times(2);
        expect(mockConfig.getString("logParserScript")).andReturn("/opt/bin/mkhoj/parser.sh").times(2);
        expect(mockProcess.waitFor()).andReturn(-1).times(1);

        expect(mockProcess.getInputStream()).andReturn(mockInputStream).times(1);
        expect(mockBufferedReader.readLine())
                .andReturn("test").times(1)
                .andReturn(null).times(1);
        expectNew(BufferedReader.class, new Class[] {Reader.class}, mockInputStreamReader)
                .andReturn(mockBufferedReader).anyTimes();
        expectNew(InputStreamReader.class, new Class[]{InputStream.class}, mockInputStream)
                .andReturn(mockInputStreamReader).anyTimes();
        mockBufferedReader.close();
        expectLastCall().times(1);
        mockResponseSender.sendResponse("test\n", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        final ProcessBuilder mockProcessBuilder = createMock(ProcessBuilder.class);
        expect(mockProcessBuilder.start()).andReturn(mockProcess).times(1);
        expectNew(ProcessBuilder.class, CasConfigUtil.getServerConfig().getString("logParserScript"), "-t",
                targetStrings, "-l", logFilePath).andReturn(mockProcessBuilder).times(1);
        replayAll();

        final ServletLogParser tested = new ServletLogParser();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestHttpMethodIsGetKeyIsLogFilePath() throws Exception {
        final String targetStrings = "targetStrings";
        final String logFilePath = "/opt/mkhoj/logs/cas/debug/";
        // TODO: StringBuilder
        final String jObject = "a=" + targetStrings + "&b=" + logFilePath + "&c&d";

        mockStatic(CasConfigUtil.class);
        final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final FullHttpRequest mockHttpRequest = createMock(FullHttpRequest.class);
        final ByteBuf mockByteBuf = createMock(ByteBuf.class);
        final InputStream mockInputStream = createMock(InputStream.class);
        final InputStreamReader mockInputStreamReader = createMock(InputStreamReader.class);
        final BufferedReader mockBufferedReader = createMock(BufferedReader.class);

        final Configuration mockConfig = createMock(Configuration.class);
        final Process mockProcess = createMock(Process.class);

        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).times(1);
        expect(mockHttpRequest.getMethod()).andReturn(HttpMethod.POST).times(1);
        expect(mockHttpRequest.content()).andReturn(mockByteBuf).times(1);
        expect(mockByteBuf.toString(CharsetUtil.UTF_8)).andReturn(jObject).times(1);
        expect(mockQueryStringDecoder.parameters()).andReturn(createMapFromStringPair("logFilePath", null)).times(1);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).times(2);
        expect(mockConfig.getString("logParserScript")).andReturn("/opt/bin/mkhoj/parser.sh").times(2);
        expect(mockProcess.waitFor()).andReturn(-1).times(1);

        expect(mockProcess.getInputStream()).andReturn(mockInputStream).times(1);
        expect(mockBufferedReader.readLine())
                .andReturn("test").times(1)
                .andReturn(null).times(1);
        expectNew(BufferedReader.class, new Class[] {Reader.class}, mockInputStreamReader)
                .andReturn(mockBufferedReader).anyTimes();
        expectNew(InputStreamReader.class, new Class[]{InputStream.class}, mockInputStream)
                .andReturn(mockInputStreamReader).anyTimes();
        mockBufferedReader.close();
        expectLastCall().times(1);
        mockResponseSender.sendResponse("test\n", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        final ProcessBuilder mockProcessBuilder = createMock(ProcessBuilder.class);
        expect(mockProcessBuilder.start()).andReturn(mockProcess).times(1);
        expectNew(ProcessBuilder.class, CasConfigUtil.getServerConfig().getString("logParserScript"), "-t",
                targetStrings, "-l", logFilePath).andReturn(mockProcessBuilder).times(1);
        replayAll();

        final ServletLogParser tested = new ServletLogParser();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletLogParser tested = new ServletLogParser();
        assertThat(tested.getName(), is(IsEqual.equalTo("LogParser")));
    }
}
