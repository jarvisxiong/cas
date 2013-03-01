package com.inmobi.adserve.channels.server;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.util.DebugLogger;

public class ServletInvalid implements Servlet {

  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {
    // invalid request
    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
    response.setContent(ChannelBuffers.copiedBuffer("Page not Found", Charset.forName("UTF-8").name()));
    if(e != null) {
      Channel channel = e.getChannel();
      if(channel != null && channel.isWritable()) {
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
      }
    }
  }

  @Override
  public String getName() {
    return "Invalid Servlet";
  }

}
