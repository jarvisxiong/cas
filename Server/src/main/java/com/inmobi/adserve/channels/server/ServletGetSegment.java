package com.inmobi.adserve.channels.server;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class ServletGetSegment implements Servlet {
  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {
    Map<String, List<String>> params = queryStringDecoder.getParameters();
    JSONObject jObject;
    try {
      jObject = RequestParser.extractParams(params, "segments", logger);
    } catch (JSONException exeption) {
      logger.debug("Encountered Json Error while creating json object inside servlet");
      hrh.setTerminationReason(ServletHandler.jsonParsingError);
      InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
      hrh.responseSender.sendResponse("Incorrect Json", e);
      return;
    }

    if(null == jObject) {
      hrh.responseSender.sendResponse("Incorrect Json", e);
      return;
    }

    Map<String, HashMap<String, String>> segmentInfo = new HashMap<String, HashMap<String, String>>();
    JSONArray segmentList = jObject.getJSONArray("segment-list");

    for (int i = 0; i < segmentList.length(); i++) {
      JSONObject segment = segmentList.getJSONObject(i);
      String id = segment.getString("id");
      String repoName = segment.getString("repo-name");
      String key = id + "_" + repoName;
      segmentInfo.put(key, new HashMap<String, String>());

      if(repoName != null && repoName.equalsIgnoreCase("channel")) {
        ChannelEntity entity = ServletHandler.repositoryHelper.queryChannelRepository(id);
        if(entity == null)
          continue;
        for (Method method : entity.getClass().getMethods()) {
          if(method.getName().startsWith("get")) {
            segmentInfo.get(key).put(
                method.getName(),
                null == method.invoke(entity, (Object[]) null) ? "null" : method.invoke(entity, (Object[]) null)
                    .toString());
          }
        }
      }

      if(repoName != null && repoName.equalsIgnoreCase("channelsegment")) {
        ChannelSegmentEntity entity = ServletHandler.repositoryHelper.queryChannelAdGroupRepository(id);
        if(entity == null)
          continue;
        for (Method method : entity.getClass().getMethods()) {
          if(method.getName().startsWith("get")) {
            segmentInfo.get(key).put(
                method.getName(),
                null == method.invoke(entity, (Object[]) null) ? "null" : method.invoke(entity, (Object[]) null)
                    .toString());
          }
        }
      }

      if(repoName != null && repoName.equalsIgnoreCase("channelfeedback")) {
        ChannelFeedbackEntity entity = ServletHandler.repositoryHelper.queryChannelFeedbackRepository(id);
        if(entity == null)
          continue;
        for (Method method : entity.getClass().getMethods()) {
          if(method.getName().startsWith("get")) {
            segmentInfo.get(key).put(
                method.getName(),
                null == method.invoke(entity, (Object[]) null) ? "null" : method.invoke(entity, (Object[]) null)
                    .toString());
          }
        }
      }

      if(repoName != null && repoName.equalsIgnoreCase("channelsegmentfeedback")) {
        ChannelSegmentFeedbackEntity entity = ServletHandler.repositoryHelper.queryChannelSegmentFeedbackRepository(id);
        if(entity == null)
          continue;
        for (Method method : entity.getClass().getMethods()) {
          if(method.getName().startsWith("get")) {
            segmentInfo.get(key).put(
                method.getName(),
                null == method.invoke(entity, (Object[]) null) ? "null" : method.invoke(entity, (Object[]) null)
                    .toString());
          }
        }
      }
    }

    hrh.responseSender.sendResponse(segmentInfo.toString(), e);
    return;
  }

  @Override
  public String getName() {
    return "getSegment";
  }

}
