package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest(InspectorStats.class)
public class ServletChangeRolloutTest {
    private static HttpRequestHandler  mockHttpRequestHandler;
    private static QueryStringDecoder  mockQueryStringDecoder;

    private Map<String, List<String>> createMapFromString(String rollout) {
        Map<String, List<String>> params = new HashMap<>();
        params.put("percentRollout", Arrays.asList(rollout));
        return params;
    }

    @Test
    public void testHandleRequestThrowsNumberFormatException() throws Exception {
        mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);

        expect(mockQueryStringDecoder.parameters())
                .andReturn(createMapFromString("dummy")).times(1);
        mockResponseSender.sendResponse("INVALIDPERCENT", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletChangeRollout tested = new ServletChangeRollout();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestSuccessful() throws Exception {
        mockStatic(InspectorStats.class);
        mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);

        expect(mockQueryStringDecoder.parameters())
                .andReturn(createMapFromString("50")).times(1);
        InspectorStats.incrementStatCount(InspectorStrings.PERCENT_ROLL_OUT, 50L);
        expectLastCall().times(1);
        mockResponseSender.sendResponse("OK", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletChangeRollout tested = new ServletChangeRollout();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletChangeRollout tested = new ServletChangeRollout();
        assertThat(tested.getName(), is(IsEqual.equalTo("changerollout")));
    }
}