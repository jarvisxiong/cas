package com.inmobi.adserve.channels.server;

import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class ServletChangeConfig implements Servlet{
  
  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception{
    Map<String, List<String>> params = queryStringDecoder.getParameters();
    JSONObject jObject;
    try {
      jObject = RequestParser.extractParams(params, "update", logger);
    } catch (JSONException exeption) {
      jObject = new JSONObject();
      logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
      hrh.setTerminationReason(HttpRequestHandler.jsonParsingError);
      InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
    }
    hrh.changeConfig(e, jObject);            
  }
  @Override
  public String getName() {
    return "configchange";
  }

}
