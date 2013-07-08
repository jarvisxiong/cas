package com.inmobi.adserve.channels.server.servlet;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.client.BootstrapCreation;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;

public class ServletStat implements Servlet {

  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {
    logger.debug("Inside stat servlet");
    hrh.responseSender.sendResponse(
        InspectorStats.getStats(BootstrapCreation.getMaxConnections(), BootstrapCreation.getDroppedConnections()), e);
  }

  @Override
  public String getName() {
    return "stat";
  }
}
