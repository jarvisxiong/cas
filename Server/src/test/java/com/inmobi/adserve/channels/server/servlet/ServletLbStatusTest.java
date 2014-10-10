package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, ServletLbStatus.class})
public class ServletLbStatusTest {
    private static HttpRequestHandler  httpRequestHandler;
    private static QueryStringDecoder  mockQueryStringDecoder;
    private static Channel             mockChannel;

    @Test
    public void testHandleRequestStatusCodeIs200() throws Exception {
        mockStatic(InspectorStats.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);

        InspectorStats.incrementStatCount("LbStatus", InspectorStrings.totalRequests);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount("LbStatus", InspectorStrings.successfulRequests);
        expectLastCall().times(1);
        mockResponseSender.sendResponse("OK", null);
        expectLastCall().times(1);

        httpRequestHandler = new HttpRequestHandler(null, null, mockResponseSender);

        replayAll();

        ServerStatusInfo.statusCode = 200;

        ServletLbStatus tested = new ServletLbStatus();
        tested.handleRequest(httpRequestHandler, null, mockChannel);

        verifyAll();
    }

    @Test
    public void testHandleRequestStatusCodeIs404() throws Exception {
        mockStatic(InspectorStats.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);

        InspectorStats.incrementStatCount("LbStatus", InspectorStrings.totalRequests);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount("LbStatus", InspectorStrings.successfulRequests);
        expectLastCall().times(1);
        mockResponseSender.sendResponse("OK", null);
        expectLastCall().times(1);

        httpRequestHandler = new HttpRequestHandler(null, null, mockResponseSender);

        replayAll();

        ServerStatusInfo.statusCode = 200;

        ServletLbStatus tested = new ServletLbStatus();
        tested.handleRequest(httpRequestHandler, null, mockChannel);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletLbStatus tested = new ServletLbStatus();
        assertThat(tested.getName(), is(equalTo("lbstatus")));
    }
}