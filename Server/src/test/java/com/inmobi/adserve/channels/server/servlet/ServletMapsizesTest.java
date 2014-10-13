package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.CreativeCache;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Logging.class, CreativeCache.class})
public class ServletMapsizesTest {

    @Test
    public void testHandleRequestConnectionLimitHandlerIsNotNull() throws Exception {
        mockStatic(Logging.class);
        mockStatic(CreativeCache.class);
        ConnectionLimitHandler mockConnectionLimitHandler = createMock(ConnectionLimitHandler.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);

        expect(Logging.getSampledadvertiserlognos()).andReturn(new ConcurrentHashMap<String, String>()).times(1);
        expect(CreativeCache.getCreativeCache()).andReturn(new ConcurrentHashMap<String, HashSet<String>>()).times(1);
        expect(mockConnectionLimitHandler.getMaxConnectionsLimit()).andReturn(100).times(1);
        expect(mockConnectionLimitHandler.getDroppedConnections()).andReturn(new AtomicInteger(99)).times(1);
        expect(mockConnectionLimitHandler.getActiveConnections()).andReturn(new AtomicInteger(98)).times(1);
        mockResponseSender.sendResponse("{\"IncomingDroppedConnections\":99,\"SampledAdvertiserLog\":0,\"IncomingActiveConnections\":98,\"IncomingMaxConnections\":100,\"creativeCache\":0,\"SampledAdvertiserMap\":{}}", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletMapsizes tested = new ServletMapsizes(mockConnectionLimitHandler);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestConnectionLimitHandlerIsNull() throws Exception {
        mockStatic(Logging.class);
        mockStatic(CreativeCache.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);

        expect(Logging.getSampledadvertiserlognos()).andReturn(new ConcurrentHashMap<String, String>()).times(1);
        expect(CreativeCache.getCreativeCache()).andReturn(new ConcurrentHashMap<String, HashSet<String>>()).times(1);
        mockResponseSender.sendResponse("{\"SampledAdvertiserLog\":0,\"creativeCache\":0,\"SampledAdvertiserMap\":{}}", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletMapsizes tested = new ServletMapsizes(null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletMapsizes tested = new ServletMapsizes(null);
        assertThat(tested.getName(), is(IsEqual.equalTo("mapsizes")));
    }
}