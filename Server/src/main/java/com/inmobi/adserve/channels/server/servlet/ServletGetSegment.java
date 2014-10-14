package com.inmobi.adserve.channels.server.servlet;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.ChannelServerStringLiterals;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
            final Channel serverChannel) throws Exception {

        Map<String, List<String>> params = queryStringDecoder.parameters();
        JSONObject jObject;
        try {
            jObject = requestParser.extractParams(params, "segments");
        } catch (JSONException exception) {
            LOG.debug("Encountered Json Error while creating json object inside servlet, exception raised {}", exception);
            hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
            InspectorStats.incrementStatCount(InspectorStrings.JSON_PARSING_ERROR, InspectorStrings.COUNT);
            hrh.responseSender.sendResponse("Incorrect Json", serverChannel);
            return;
        }

        if (null == jObject) {
            hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
            hrh.responseSender.sendResponse("Incorrect Json", serverChannel);
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

            if (repoName != null) {
                if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryChannelRepository(id);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryChannelAdGroupRepository(id);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryChannelFeedbackRepository(id);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryChannelSegmentFeedbackRepository(id);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.querySiteMetaDetaRepository(id);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.querySiteTaxonomyRepository(id);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryPricingEngineRepository(
                            Integer.parseInt(id.split("_")[0]), Integer.parseInt(id.split("_")[1]));
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_FILTER_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.querySiteFilterRepository(id.split("_")[0],
                            Integer.parseInt(id.split("_")[1]));
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.AEROSPIKE_FEEDBACK)) {
                    if (id.split("_").length > 1) {
                        entity = CasConfigUtil.repositoryHelper.querySiteAerospikeFeedbackRepository(id.split("_")[0], Integer.parseInt(id.split("_")[1]));
                    } else {
                        entity = CasConfigUtil.repositoryHelper.querySiteAerospikeFeedbackRepository(id);
                    }
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_ECPM_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.querySiteEcpmRepository(id.split("_")[0],
                            Integer.parseInt(id.split("_")[1]), Integer.parseInt(id.split("_")[2]));
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryCurrencyConversionRepository(id);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.WAP_SITE_UAC_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryWapSiteUACRepository(id.split("_")[0]);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.IX_ACCOUNT_MAP_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryIXAccountMapRepository(Long.parseLong(id.split("_")[0]));
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CREATIVE_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryCreativeRepository(id.split("_")[0], id.split("_")[1]);
                } else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.NATIVE_AD_TEMPLATE_REPOSITORY)) {
                    entity = CasConfigUtil.repositoryHelper.queryNativeAdTemplateRepository(id);
                }
            }
            segmentInfo.put(key, entity);
        }
        Gson gson = new Gson();
        hrh.responseSender.sendResponse(gson.toJson(segmentInfo), serverChannel);
    }

    @Override
    public String getName() {
        return "getSegment";
    }

}
