package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.Charset;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServletInvalid.class)
public class ServletInvalidTest {

    @Test
    public void testHandleRequest() throws Exception {
        final Channel mockChannel = createMock(Channel.class);
        final ChannelFuture mockFuture = createMock(ChannelFuture.class);

        final HttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer(
                        "Page not Found", Charset.defaultCharset()), true);

        expectNew(DefaultFullHttpResponse.class, HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer("Page not Found", Charset.defaultCharset()), true).andReturn(
                (DefaultFullHttpResponse) response).times(1);
        expect(mockChannel.writeAndFlush(response)).andReturn(mockFuture).times(1);
        expect(mockFuture.addListener(ChannelFutureListener.CLOSE)).andReturn(null).times(1);

        replayAll();

        final ServletInvalid tested = new ServletInvalid();
        tested.handleRequest(null, null, mockChannel);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletInvalid tested = new ServletInvalid();
        assertThat(tested.getName(), is(IsEqual.equalTo("Invalid Servlet")));
    }
}
