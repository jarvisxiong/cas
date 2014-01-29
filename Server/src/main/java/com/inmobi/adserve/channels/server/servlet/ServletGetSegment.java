package com.inmobi.adserve.channels.server.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.ChannelServerStringLiterals;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author devashish To see the state of currently loaded entries in all repository
 */

@Singleton
@Path("/getsegments")
public class ServletGetSegment implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletGetSegment.class);
    private final RequestParser requestParser;

    @Inject
    ServletGetSegment(final RequestParser requestParser) {
        this.requestParser = requestParser;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {

        Map<String, List<String>> params = queryStringDecoder.getParameters();
        JSONObject jObject;
        try {
            jObject = requestParser.extractParams(params, "segments");
        }
        catch (JSONException exeption) {
            LOG.debug("Encountered Json Error while creating json object inside servlet");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
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

            if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryChannelRepository(id);
            }
            else if (repoName != null
                    && repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryChannelAdGroupRepository(id);
            }
            else if (repoName != null
                    && repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryChannelFeedbackRepository(id);
            }
            else if (repoName != null
                    && repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryChannelSegmentFeedbackRepository(id);
            }
            else if (repoName != null
                    && repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.querySiteMetaDetaRepository(id);
            }
            else if (repoName != null
                    && repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.querySiteTaxonomyRepository(id);
            }
            else if (repoName != null
                    && repoName.equalsIgnoreCase(ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryPricingEngineRepository(
                    Integer.parseInt(id.split("_")[0]), Integer.parseInt(id.split("_")[1]));
            }
            else if (repoName != null
                    && repoName.equalsIgnoreCase(ChannelServerStringLiterals.PUBLISHER_FILTER_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryPublisherFilterRepository(id.split("_")[0],
                    Integer.parseInt(id.split("_")[1]));
            }
            else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals.CITRUS_LEAF_FEEDBACK)) {
                entity = ServletHandler.repositoryHelper.querySiteCitrusLeafFeedbackRepository(id);
            }
            else if (repoName != null && repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_ECPM_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.querySiteEcpmRepository(id.split("_")[0],
                    Integer.parseInt(id.split("_")[1]), Integer.parseInt(id.split("_")[2]));
            }
            else if (repoName != null
                    && repoName.equalsIgnoreCase(ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY)) {
                entity = ServletHandler.repositoryHelper.queryCurrencyConversionRepository(id.split("_")[0]);
            }
            segmentInfo.put(key, entity);
        }
        Gson gson = new Gson();
        hrh.responseSender.sendResponse(gson.toJson(segmentInfo), e);
    }

    @Override
    public String getName() {
        return "getSegment";
    }

}
