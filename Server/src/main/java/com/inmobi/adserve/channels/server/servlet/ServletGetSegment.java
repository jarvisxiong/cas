package com.inmobi.adserve.channels.server.servlet;

import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.MOVIEBOARD;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.STATIC;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.VAST;
import static com.inmobi.adserve.channels.server.CasConfigUtil.JSON_PARSING_ERROR;
import static com.inmobi.adserve.channels.server.CasConfigUtil.repositoryHelper;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.AEROSPIKE_FEEDBACK;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CAU_METADATA_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CREATIVE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.GEO_CITY_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.GEO_ZIP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IMEI_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IX_ACCOUNT_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IX_PACKAGE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.NATIVE_AD_TEMPLATE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_ECPM_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_FILTER_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_METADATA_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SLOT_SIZE_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.WAP_SITE_UAC_REPOSITORY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.utils.CasUtils;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


/**
 * @author devashish To see the state of currently loaded entries in all repository
 * @author ritwik.kumar
 */

@Singleton
@Path("/getsegments")
public class ServletGetSegment implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletGetSegment.class);
    @Inject
    private IPRepository ipRepository;

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {

        final Map<String, List<String>> params = queryStringDecoder.parameters();
        JSONObject jObject;
        try {
            jObject = CasUtils.extractParams(params, "segments"); // requestParser.extractParams(params, "segments");
        } catch (final JSONException exception) {
            LOG.debug("Encountered Json Error while creating json object inside servlet, exception raised {}",
                    exception);
            hrh.setTerminationReason(JSON_PARSING_ERROR);
            hrh.responseSender.sendResponse("Incorrect Json", serverChannel);
            return;
        }

        if (null == jObject) {
            hrh.setTerminationReason(JSON_PARSING_ERROR);
            hrh.responseSender.sendResponse("Incorrect Json", serverChannel);
            return;
        }

        final Map<String, Object> segmentInfo = new HashMap<String, Object>();
        final JSONArray segmentList = jObject.getJSONArray("segment-list");

        for (int i = 0; i < segmentList.length(); i++) {

            final JSONObject segment = segmentList.getJSONObject(i);
            final String id = segment.getString("id");
            final String repoName = segment.getString("repo-name");
            final String key = id + "_" + repoName;
            Object entity = null;

            if (repoName != null) {
                if (repoName.equalsIgnoreCase(CHANNEL_REPOSITORY)) {
                    entity = repositoryHelper.queryChannelRepository(id);
                } else if (repoName.equalsIgnoreCase(CHANNEL_ADGROUP_REPOSITORY)) {
                    entity = repositoryHelper.queryChannelAdGroupRepository(id);
                } else if (repoName.equalsIgnoreCase(CHANNEL_FEEDBACK_REPOSITORY)) {
                    entity = repositoryHelper.queryChannelFeedbackRepository(id);
                } else if (repoName.equalsIgnoreCase(CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)) {
                    entity = repositoryHelper.queryChannelSegmentFeedbackRepository(id);
                } else if (repoName.equalsIgnoreCase(SITE_METADATA_REPOSITORY)) {
                    entity = repositoryHelper.querySiteMetaDetaRepository(id);
                } else if (repoName.equalsIgnoreCase(SITE_TAXONOMY_REPOSITORY)) {
                    entity = repositoryHelper.querySiteTaxonomyRepository(id);
                } else if (repoName.equalsIgnoreCase(PRICING_ENGINE_REPOSITORY)) {
                    entity = repositoryHelper.queryPricingEngineRepository(Integer.parseInt(id.split("_")[0]),
                            Integer.parseInt(id.split("_")[1]));
                } else if (repoName.equalsIgnoreCase(SITE_FILTER_REPOSITORY)) {
                    entity = repositoryHelper.querySiteFilterRepository(id.split("_")[0],
                            Integer.parseInt(id.split("_")[1]));
                } else if (repoName.equalsIgnoreCase(AEROSPIKE_FEEDBACK)) {
                    if (id.split("_").length > 1) {
                        entity = repositoryHelper.querySiteAerospikeFeedbackRepository(id.split("_")[0],
                                Integer.parseInt(id.split("_")[1]));
                    } else {
                        entity = repositoryHelper.querySiteAerospikeFeedbackRepository(id);
                    }
                } else if (repoName.equalsIgnoreCase(IMEI_REPOSITORY)) {
                    entity = repositoryHelper.queryIMEIRepository(id);
                } else if (repoName.equalsIgnoreCase(SITE_ECPM_REPOSITORY)) {
                    entity = repositoryHelper.querySiteEcpmRepository(id.split("_")[0],
                            Integer.parseInt(id.split("_")[1]), Integer.parseInt(id.split("_")[2]));
                } else if (repoName.equalsIgnoreCase(CURRENCY_CONVERSION_REPOSITORY)) {
                    entity = repositoryHelper.queryCurrencyConversionRepository(id);
                } else if (repoName.equalsIgnoreCase(WAP_SITE_UAC_REPOSITORY)) {
                    entity = repositoryHelper.queryWapSiteUACRepository(id.split("_")[0]);
                } else if (repoName.equalsIgnoreCase(IX_ACCOUNT_MAP_REPOSITORY)) {
                    entity = repositoryHelper.queryIXAccountMapRepository(Long.parseLong(id.split("_")[0]));
                } else if (repoName.equalsIgnoreCase(CREATIVE_REPOSITORY)) {
                    entity = repositoryHelper.queryCreativeRepository(id.split("_")[0], id.split("_")[1]);
                } else if (repoName.equalsIgnoreCase(NATIVE_AD_TEMPLATE_REPOSITORY)) {
                    final long placementId = Long.parseLong(id);
                    segmentInfo.put("VAST", repositoryHelper.queryNativeAdTemplateRepository(placementId, VAST));
                    segmentInfo.put("STATIC", repositoryHelper.queryNativeAdTemplateRepository(placementId, STATIC));
                    segmentInfo.put("MOVIEBOARD",
                            repositoryHelper.queryNativeAdTemplateRepository(placementId, MOVIEBOARD));
                } else if (repoName.equalsIgnoreCase(GEO_ZIP_REPOSITORY)) {
                    entity = repositoryHelper.queryGeoZipRepository(Integer.parseInt(id));
                } else if (repoName.equalsIgnoreCase(GEO_CITY_REPOSITORY)) {
                    entity = repositoryHelper.queryGeoCityRepository(Integer.parseInt(id));
                } else if (repoName.equalsIgnoreCase(CAU_METADATA_REPOSITORY)) {
                    entity = repositoryHelper.queryCauMetaDataRepository(Long.parseLong(id));
                } else if (repoName.equalsIgnoreCase(SLOT_SIZE_MAP_REPOSITORY)) {
                    entity = repositoryHelper.querySlotSizeMapRepository(Short.parseShort(id));
                } else if (repoName.equalsIgnoreCase(IP_REPOSITORY)) {
                    entity = ipRepository.replaceHostWitIp(id);
                } else if (repoName.equalsIgnoreCase(IX_PACKAGE_REPOSITORY)) {
                    final ResultSet<IXPackageEntity> resultSet = repositoryHelper.queryIXPackageRepository(
                            Integer.parseInt(id.split("_")[0]), id.split("_")[1], Integer.parseInt(id.split("_")[2]),
                            Integer.parseInt(id.split("_")[3]));
                    final List<Integer> result = new ArrayList<>();
                    for (final IXPackageEntity packageEntity : resultSet) {
                        result.add(packageEntity.getId());
                    }
                    entity = result;
                }
            }
            segmentInfo.put(key, entity);
        }
        final Gson gson = new Gson();
        hrh.responseSender.sendResponse(gson.toJson(segmentInfo), serverChannel);
    }

    @Override
    public String getName() {
        return "getSegment";
    }

}
