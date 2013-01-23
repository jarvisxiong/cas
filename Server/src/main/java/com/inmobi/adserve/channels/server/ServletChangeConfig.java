package com.inmobi.adserve.channels.server;

import java.util.Iterator;
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
      hrh.setTerminationReason(ServletHandler.jsonParsingError);
      InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
    }
    if(null == jObject) {
      hrh.responseSender.sendResponse("Incorrect Json", e);
      return;
    }
    logger.debug("Successfully got json for config change");
    try {
      StringBuilder updates = new StringBuilder();
      updates.append("Successfully changed Config!!!!!!!!!!!!!!!!!\n").append("The changes are\n");
      Iterator<String> itr = jObject.keys();
      while (itr.hasNext()) {
        String configKey = itr.next().toString();
        if(configKey.startsWith("adapter")
            && ServletHandler.adapterConfig.containsKey(configKey.replace("adapter.", ""))) {
          ServletHandler.adapterConfig.setProperty(configKey.replace("adapter.", ""), jObject.getString(configKey));
          updates.append(configKey).append("=")
              .append(ServletHandler.adapterConfig.getString(configKey.replace("adapter.", ""))).append("\n");
        }
        if(configKey.startsWith("server") && ServletHandler.config.containsKey(configKey.replace("server.", ""))) {
          ServletHandler.config.setProperty(configKey.replace("server.", ""), jObject.getString(configKey));
          if(configKey.replace("server.", "").equals("maxconnections")) {
            BootstrapCreation.setMaxConnectionLimit(ServletHandler.config.getInt(configKey.replace("server.", "")));
          }
          updates.append(configKey).append("=")
              .append(ServletHandler.config.getString(configKey.replace("server.", ""))).append("\n");
        }
      }
      hrh.responseSender.sendResponse(updates.toString(), e);
    } catch (JSONException ex) {
      logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler for config change");
      hrh.terminationReason = ServletHandler.jsonParsingError;
    }            
  }
  @Override
  public String getName() {
    return "configchange";
  }

}
