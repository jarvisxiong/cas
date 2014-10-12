package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.Charset;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, ServletLbStatus.class})
public class ServletLbStatusTest {

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

        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(null, null, mockResponseSender);

        replayAll();

        ServerStatusInfo.statusCode = 200;

        ServletLbStatus tested = new ServletLbStatus();
        tested.handleRequest(httpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestStatusCodeIs404() throws Exception {
        mockStatic(InspectorStats.class);

        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        Channel mockChannel = createMock(Channel.class);
        ChannelFuture mockFuture = createMock(ChannelFuture.class);

        ServerStatusInfo.statusCode = 404;
        ServerStatusInfo.statusString = "test";

        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer(ServerStatusInfo.statusString, Charset.defaultCharset()));
        HttpRequestHandler httpRequestHandler = new HttpRequestHandler(null, null, mockResponseSender);

        expectNew(DefaultFullHttpResponse.class, HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer(ServerStatusInfo.statusString, Charset.defaultCharset()))
                .andReturn((DefaultFullHttpResponse) response).times(1);
        expect(mockChannel.writeAndFlush(response)).andReturn(mockFuture).times(1);
        expect(mockFuture.addListener(ChannelFutureListener.CLOSE)).andReturn(null).times(1);
        InspectorStats.incrementStatCount("LbStatus", InspectorStrings.totalRequests);
        expectLastCall().times(1);

        replayAll();

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