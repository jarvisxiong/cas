package com.inmobi.adserve.channels.server;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.util.DebugLogger;

public class ServletLbStatus implements Servlet {

  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {
    hrh.sendLbStatus(e);
  }

  @Override
  public String getName() {
    return "lbstatus";
  }
}
