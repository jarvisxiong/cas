package com.inmobi.adserve.channels.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class ServletWhiteListSites implements Servlet {
  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {
    Map<String, List<String>> params = queryStringDecoder.getParameters();
    JSONObject jObject;

    try {
      jObject = RequestParser.extractParams(params, "update", logger);
    } catch (JSONException exeption) {
      logger.debug("Encountered Json Error while creating json object inside servlet");
      hrh.setTerminationReason(ServletHandler.jsonParsingError);
      InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
      hrh.responseSender.sendResponse("Incorrect Json", e);
      return;
    }

    if(null == jObject) {
      hrh.responseSender.sendResponse(Filters.whiteListedSites.toString(), e);
      return;
    }

    String operation = jObject.getString("operation");

    String advertiserId = jObject.getString("advertiserId");
    String[] siteList = null;
    try {
      String sites = jObject.getString("sites");

      if(StringUtils.isEmpty(sites)) {
        throw new JSONException("Empty sites");
      }

      siteList = sites.split(",");
    } catch (JSONException exception) {
      logger.debug("No sites");
    }

    if(operation.equals("nowhitelisting")) {
      Filters.whiteListedSites.remove(advertiserId);
    } else if(siteList != null) {
      
      if(operation.equals("add")) {

        if(Filters.whiteListedSites.containsKey(advertiserId)) {
          Filters.whiteListedSites.get(advertiserId).addAll(Arrays.asList(siteList));
        } else {
          HashSet<String> siteSet = new HashSet<String>();
          siteSet.addAll(Arrays.asList(siteList));
          Filters.whiteListedSites.put(advertiserId, siteSet);
        }

      } else if(operation.equals("remove")) {

        if(Filters.whiteListedSites.containsKey(advertiserId)) {
          Filters.whiteListedSites.get(advertiserId).removeAll(Arrays.asList(siteList));
        }

      }
    }
    hrh.responseSender.sendResponse(Filters.whiteListedSites.toString(), e);
    return;

  }

  @Override
  public String getName() {
    return "whiteListSites Servlet";
  }

}
