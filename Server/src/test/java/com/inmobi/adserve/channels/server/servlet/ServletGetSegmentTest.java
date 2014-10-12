package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SiteFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteFilterEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.ChannelServerStringLiterals;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.hamcrest.core.IsEqual;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CasConfigUtil.class, InspectorStats.class})
public class ServletGetSegmentTest {

    private Map<String, List<String>> createMapFromStringPair(String key, String value) {
        Map<String, List<String>> params = new HashMap<>();
        params.put(key, Arrays.asList(value));
        return params;
    }

    @Test
    public void testHandleRequestJsonException() throws Exception {
        mockStatic(InspectorStats.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        RequestParser mockRequestParser = createMock(RequestParser.class);

        expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
        expect(mockRequestParser.extractParams(null, "segments")).andThrow(new JSONException("Json Exception"));
        mockHttpRequestHandler.setTerminationReason(CasConfigUtil.jsonParsingError);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
        expectLastCall().times(1);
        mockResponseSender.sendResponse("Incorrect Json", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletGetSegment tested = new ServletGetSegment(mockRequestParser);
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestJsonObjectIsNull() throws Exception {
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        RequestParser mockRequestParser = createMock(RequestParser.class);

        expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
        expect(mockRequestParser.extractParams(null, "segments")).andReturn(null).times(1);
        mockHttpRequestHandler.setTerminationReason(CasConfigUtil.jsonParsingError);
        expectLastCall().times(1);
        mockResponseSender.sendResponse("Incorrect Json", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletGetSegment tested = new ServletGetSegment(mockRequestParser);
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestJsonObjectIsNotNull() throws Exception {
        String id = "0_1_2";
        String id2 = "0";
        int mockJSONArraySize = 15;

        mockStatic(CasConfigUtil.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        RequestParser mockRequestParser = createMock(RequestParser.class);
        JSONObject mockJSONObject = createMock(JSONObject.class);
        JSONArray mockJSONArray = createMock(JSONArray.class);
        RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);

        ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);
        ChannelSegmentFeedbackEntity mockChannelSegmentFeedbackEntity = createMock(ChannelSegmentFeedbackEntity.class);
        SiteMetaDataEntity mockSiteMetaDataEntity = createMock(SiteMetaDataEntity.class);
        SiteTaxonomyEntity mockSiteTaxonomyEntity = createMock(SiteTaxonomyEntity.class);
        PricingEngineEntity mockPricingEngineEntity = createMock(PricingEngineEntity.class);
        SiteFilterEntity mockSiteFilterEntity = createMock(SiteFilterEntity.class);
        SegmentAdGroupFeedbackEntity mockSegmentAdGroupFeedbackEntity = createMock(SegmentAdGroupFeedbackEntity.class);
        SiteFeedbackEntity mockSiteFeedbackEntity = createMock(SiteFeedbackEntity.class);
        SiteEcpmEntity mockSiteEcpmEntity = createMock(SiteEcpmEntity.class);
        CurrencyConversionEntity mockCurrencyConversionEntity = createMock(CurrencyConversionEntity.class);
        WapSiteUACEntity mockWapSiteUACEntity = createMock(WapSiteUACEntity.class);
        IXAccountMapEntity mockIXAccountMapEntity = createMock(IXAccountMapEntity.class);
        CreativeEntity mockCreativeEntity = createMock(CreativeEntity.class);

        expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
        expect(mockRequestParser.extractParams(null, "segments")).andReturn(mockJSONObject).times(1);
        expect(mockJSONObject.getJSONArray("segment-list")).andReturn(mockJSONArray).times(1);
        expect(mockJSONArray.length()).andReturn(mockJSONArraySize).times(mockJSONArraySize+1);
        for(int i = 0; i < mockJSONArraySize; ++i) {
            expect(mockJSONArray.getJSONObject(i)).andReturn(mockJSONObject).times(1);
        }
        expect(mockJSONObject.getString("id"))
                .andReturn(id).times(9)
                .andReturn(id2).times(1)
                .andReturn(id).times(mockJSONArraySize-10);

        // Sum of all times should be equal to mockJSONArraySize
        expect(mockJSONObject.getString("repo-name"))
                .andReturn(ChannelServerStringLiterals.CHANNEL_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.SITE_FILTER_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.AEROSPIKE_FEEDBACK).times(2)
                .andReturn(ChannelServerStringLiterals.SITE_ECPM_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.WAP_SITE_UAC_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.IX_ACCOUNT_MAP_REPOSITORY).times(1)
                .andReturn(ChannelServerStringLiterals.CREATIVE_REPOSITORY).times(1);

        expect(mockRepositoryHelper.queryChannelRepository(id))
                .andReturn((ChannelEntity) mockChannelEntity).times(1);
        expect(mockRepositoryHelper.queryChannelAdGroupRepository(id))
                .andReturn((ChannelSegmentEntity) mockChannelSegmentEntity).times(1);
        expect(mockRepositoryHelper.queryChannelFeedbackRepository(id))
                .andReturn((ChannelFeedbackEntity) mockChannelFeedbackEntity).times(1);
        expect(mockRepositoryHelper.queryChannelSegmentFeedbackRepository(id))
                .andReturn((ChannelSegmentFeedbackEntity) mockChannelSegmentFeedbackEntity).times(1);
        expect(mockRepositoryHelper.querySiteMetaDetaRepository(id))
                .andReturn((SiteMetaDataEntity) mockSiteMetaDataEntity).times(1);
        expect(mockRepositoryHelper.querySiteTaxonomyRepository(id))
                .andReturn((SiteTaxonomyEntity) mockSiteTaxonomyEntity).times(1);
        expect(mockRepositoryHelper.queryPricingEngineRepository(Integer.parseInt(id.split("_")[0]), Integer.parseInt(id.split("_")[1])))
                .andReturn((PricingEngineEntity) mockPricingEngineEntity).times(1);
        expect(mockRepositoryHelper.querySiteFilterRepository(id.split("_")[0], Integer.parseInt(id.split("_")[1])))
                .andReturn((SiteFilterEntity) mockSiteFilterEntity).times(1);
        expect(mockRepositoryHelper.querySiteAerospikeFeedbackRepository(id.split("_")[0], Integer.parseInt(id.split("_")[1])))
                .andReturn((SegmentAdGroupFeedbackEntity) mockSegmentAdGroupFeedbackEntity).times(1);
        expect(mockRepositoryHelper.querySiteAerospikeFeedbackRepository(id2))
                .andReturn((SiteFeedbackEntity) mockSiteFeedbackEntity).times(1);
        expect(mockRepositoryHelper.querySiteEcpmRepository(id.split("_")[0], Integer.parseInt(id.split("_")[1]), Integer.parseInt(id.split("_")[2])))
                .andReturn((SiteEcpmEntity) mockSiteEcpmEntity).times(1);
        expect(mockRepositoryHelper.queryCurrencyConversionRepository(id))
                .andReturn((CurrencyConversionEntity) mockCurrencyConversionEntity).times(1);
        expect(mockRepositoryHelper.queryWapSiteUACRepository(id.split("_")[0]))
                .andReturn((WapSiteUACEntity) mockWapSiteUACEntity).times(1);
        expect(mockRepositoryHelper.queryIXAccountMapRepository(Long.parseLong(id.split("_")[0])))
                .andReturn((IXAccountMapEntity) mockIXAccountMapEntity).times(1);
        expect(mockRepositoryHelper.queryCreativeRepository(id.split("_")[0], id.split("_")[1]))
                .andReturn((CreativeEntity) mockCreativeEntity).times(1);

        mockResponseSender.sendResponse("{\"0_1_2_IXAccountMapRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},\"0_1_2_WapSiteUACRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"siteTypeId\":0,\"isCoppaEnabled\":false,\"isTransparencyEnabled\":false},\"0_1_2_ChannelRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"isActive\":false,\"isTestMode\":false,\"burstQps\":0,\"impressionCeil\":0,\"impressionFloor\":0,\"requestCap\":0,\"priority\":0,\"demandSourceTypeId\":0,\"isRtb\":false,\"wnRequied\":false,\"wnFromClient\":false,\"isSiteInclusion\":false,\"accountSegment\":0},\"0_aerospike\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"lastUpdated\":0},\"0_1_2_SiteEcpmRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"ecpm\":0.0,\"networkEcpm\":0.0},\"0_1_2_SiteTaxonomyRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},\"0_1_2_SiteFilterRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"isExpired\":false},\"0_1_2_SiteMetaDataRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},\"0_1_2_CurrencyConversionRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},\"0_1_2_ChannelAdGroupRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"platformTargeting\":0,\"status\":false,\"isTestMode\":false,\"adgroupIncId\":0,\"allTags\":false,\"udIdRequired\":false,\"zipCodeRequired\":false,\"latlongRequired\":false,\"restrictedToRichMediaOnly\":false,\"appUrlEnabled\":false,\"interstitialOnly\":false,\"nonInterstitialOnly\":false,\"stripUdId\":false,\"stripZipCode\":false,\"stripLatlong\":false,\"isSiteInclusion\":false,\"impressionCeil\":0,\"ecpmBoost\":0.0,\"dst\":0,\"campaignIncId\":0},\"0_1_2_PricingEngineRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"rtbFloor\":0.0,\"dcpFloor\":0.0},\"0_1_2_ChannelSegmentFeedbackRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"eCPM\":0.0,\"fillRatio\":0.0,\"lastHourLatency\":0.0,\"todayRequests\":0,\"beacons\":0,\"clicks\":0,\"todayImpressions\":0},\"0_1_2_CreativeRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},\"0_1_2_ChannelFeedbackRepository\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"totalInflow\":0.0,\"totalBurn\":0.0,\"balance\":0.0,\"totalImpressions\":0,\"todayRequests\":0,\"todayImpressions\":0,\"averageLatency\":0.0,\"revenue\":0.0},\"0_1_2_aerospike\":{\"CGLIB$BOUND\":true,\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}}}", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        CasConfigUtil.repositoryHelper = mockRepositoryHelper;

        ServletGetSegment tested = new ServletGetSegment(mockRequestParser);
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletGetSegment tested = new ServletGetSegment(null);
        assertThat(tested.getName(), is(IsEqual.equalTo("getSegment")));
    }
}