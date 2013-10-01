package com.inmobi.adserve.channels.server.servlet;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.server.ChannelServerStringLiterals;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author devashish To see the state of currently loaded entries in all
 *         repository
 */

public class ServletGetSegment implements Servlet {
    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder,
                              MessageEvent e,
                              DebugLogger logger) throws Exception {

        Map<String, List<String>> params = queryStringDecoder.getParameters();
        JSONObject jObject;
        try {
            jObject = RequestParser.extractParams(params, "segments");
        } catch (JSONException exeption) {
            logger.debug("Encountered Json Error while creating json object inside servlet");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError,
                    InspectorStrings.count);
            hrh.responseSender.sendResponse("Incorrect Json", e);
            return;
        }

        if (null == jObject) {
            hrh.responseSender.sendResponse("Incorrect Json", e);
            return;
        }

        Map<String, Object> segmentInfo = new HashMap<String, Object>();
        JSONArray segmentList = jObject.getJSONArray("segment-list");

        for (int i = 0; i < segmentList.length(); i++) {

            JSONObject segment = segmentList.getJSONObject(i);
            String id = segment.getString("id");
            String repoName = segment.getString("repo-name");
            String key = id + "_" + repoName;
            Object entity = null;

            if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals
                    .CHANNEL_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryChannelRepository(id);
            } else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals
                    .CHANNEL_ADGROUP_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryChannelAdGroupRepository(id);
            } else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals
                    .CHANNEL_FEEDBACK_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryChannelFeedbackRepository(id);
            } else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals
                    .CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryChannelSegmentFeedbackRepository(id);
            } else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals
                    .SITE_METADATA_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.querySiteMetaDetaRepository(id);
            } else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals
                    .SITE_TAXONOMY_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.querySiteTaxonomyRepository(id);
            } else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals
                    .PUBLISHER_FILTER_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryPublisherFilterRepository(id.split
                        ("_")[0], Integer.parseInt(id.split("_")[1]), null);
            } else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals
                    .CITRUS_LEAF_FEEDBACK)) {
                entity = ServletHandler.repositoryHelper.querySiteCitrusLeafFeedbackRepository(id);
            }
            segmentInfo.put(key, entity);
        }
        Gson gson = new Gson();
        hrh.responseSender.sendResponse(gson.toJson(segmentInfo), e);
        return;
    }

    @Override
    public String getName() {
        return "getSegment";
    }

    public Map<String, String> getSegments(Object entity)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Map<String, String> entityInfo = new HashMap<String, String>();
        if (entity != null) {
            for (Method method : entity.getClass().getMethods()) {
                if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                    String key = method.getName().replaceFirst("get", "");
                    Object value = null == method.invoke(entity, (Object[]) null) ?
                            "null" : method.invoke(entity, (Object[]) null);
                    String valueString = value instanceof Object[] ?
                            Arrays.asList(value).toString() : value.toString();
                    entityInfo.put(key, valueString);
                }
            }
        }
        return entityInfo;
    }

}
