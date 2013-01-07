package com.inmobi.adserve.channels.server;

import java.util.List;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class ServletChangeRollout implements Servlet{

  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e, DebugLogger logger) throws Exception {
    try {
      List<String> rollout = (queryStringDecoder.getParameters().get("percentRollout"));
      HttpRequestHandler.setPercentRollout(Integer.parseInt(rollout.get(0)));
    } catch (NumberFormatException ex) {
      logger.error("invalid attempt to change rollout percentage " + ex);
      hrh.responseSender.sendResponse("INVALIDPERCENT", e);
    }
    InspectorStats.setWorkflowStats(InspectorStrings.percentRollout, Long.valueOf(HttpRequestHandler.getPercentRollout()));
    logger.debug("new roll out percentage is " + HttpRequestHandler.getPercentRollout());
    hrh.responseSender.sendResponse("OK", e);
  }
  @Override
  public String getName() {
    return "changerollout";
  }
}
