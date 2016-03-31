package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.hamcrest.core.IsEqual;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;

import io.netty.handler.codec.http.QueryStringDecoder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CasConfigUtil.class, InspectorStats.class, CasUtils.class})
public class ServletGetSegmentTest {

    @Test
    public void testHandleRequestJsonException() throws Exception {
        mockStatic(InspectorStats.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        mockStatic(CasUtils.class);

        expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
        expect(CasUtils.extractParams(null, "segments")).andThrow(new JSONException("Json Exception"));
        mockHttpRequestHandler.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
        expectLastCall().times(1);
        mockResponseSender.sendResponse("Incorrect Json", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        final ServletGetSegment tested = new ServletGetSegment();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestJsonObjectIsNull() throws Exception {
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        mockStatic(CasUtils.class);

        expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
        expect(CasUtils.extractParams(null, "segments")).andReturn(null).times(1);
        mockHttpRequestHandler.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
        expectLastCall().times(1);
        mockResponseSender.sendResponse("Incorrect Json", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        final ServletGetSegment tested = new ServletGetSegment();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testHandleRequestJsonObjectIsNotNull() throws Exception {
        final String id = "0_1_2";
        final String id2 = "0";
        final int mockJSONArraySize = 15;

        mockStatic(CasConfigUtil.class);
        mockStatic(CasUtils.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final QueryStringDecoder mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        final JSONObject mockJSONObject = createMock(JSONObject.class);
        final JSONArray mockJSONArray = createMock(JSONArray.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);

        final ChannelEntity mockChannelEntity = createMock(ChannelEntity.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final ChannelFeedbackEntity mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);
        final ChannelSegmentFeedbackEntity mockChannelSegmentFeedbackEntity =
                createMock(ChannelSegmentFeedbackEntity.class);
        final SiteMetaDataEntity mockSiteMetaDataEntity = createMock(SiteMetaDataEntity.class);
        final SiteTaxonomyEntity mockSiteTaxonomyEntity = createMock(SiteTaxonomyEntity.class);
        final PricingEngineEntity mockPricingEngineEntity = createMock(PricingEngineEntity.class);
        final SiteFilterEntity mockSiteFilterEntity = createMock(SiteFilterEntity.class);
        final SegmentAdGroupFeedbackEntity mockSegmentAdGroupFeedbackEntity =
                createMock(SegmentAdGroupFeedbackEntity.class);
        final SiteFeedbackEntity mockSiteFeedbackEntity = createMock(SiteFeedbackEntity.class);
        final SiteEcpmEntity mockSiteEcpmEntity = createMock(SiteEcpmEntity.class);
        final CurrencyConversionEntity mockCurrencyConversionEntity = createMock(CurrencyConversionEntity.class);
        final WapSiteUACEntity mockWapSiteUACEntity = createMock(WapSiteUACEntity.class);
        final IXAccountMapEntity mockIXAccountMapEntity = createMock(IXAccountMapEntity.class);
        final CreativeEntity mockCreativeEntity = createMock(CreativeEntity.class);

        expect(mockQueryStringDecoder.parameters()).andReturn(null).times(1);
        expect(CasUtils.extractParams(null, "segments")).andReturn(mockJSONObject).times(1);
        expect(mockJSONObject.getJSONArray("segment-list")).andReturn(mockJSONArray).times(1);
        expect(mockJSONArray.length()).andReturn(mockJSONArraySize).times(mockJSONArraySize + 1);
        for (int i = 0; i < mockJSONArraySize; ++i) {
            expect(mockJSONArray.getJSONObject(i)).andReturn(mockJSONObject).times(1);
        }
        expect(mockJSONObject.getString("id")).andReturn(id).times(9).andReturn(id2).times(1).andReturn(id)
                .times(mockJSONArraySize - 10);

        // Sum of all times should be equal to mockJSONArraySize
        expect(mockJSONObject.getString("repo-name")).andReturn(ChannelServerStringLiterals.CHANNEL_REPOSITORY).times(1)
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

        expect(mockRepositoryHelper.queryChannelRepository(id)).andReturn(mockChannelEntity).times(1);
        expect(mockRepositoryHelper.queryChannelAdGroupRepository(id)).andReturn(mockChannelSegmentEntity).times(1);
        expect(mockRepositoryHelper.queryChannelFeedbackRepository(id)).andReturn(mockChannelFeedbackEntity).times(1);
        expect(mockRepositoryHelper.queryChannelSegmentFeedbackRepository(id))
                .andReturn(mockChannelSegmentFeedbackEntity).times(1);
        expect(mockRepositoryHelper.querySiteMetaDetaRepository(id)).andReturn(mockSiteMetaDataEntity).times(1);
        expect(mockRepositoryHelper.querySiteTaxonomyRepository(id)).andReturn(mockSiteTaxonomyEntity).times(1);
        expect(mockRepositoryHelper.queryPricingEngineRepository(Integer.parseInt(id.split("_")[0]),
                Integer.parseInt(id.split("_")[1]))).andReturn(mockPricingEngineEntity).times(1);
        expect(mockRepositoryHelper.querySiteFilterRepository(id.split("_")[0], Integer.parseInt(id.split("_")[1])))
                .andReturn(mockSiteFilterEntity).times(1);
        expect(mockRepositoryHelper.querySiteAerospikeFeedbackRepository(id.split("_")[0],
                Integer.parseInt(id.split("_")[1]))).andReturn(mockSegmentAdGroupFeedbackEntity).times(1);
        expect(mockRepositoryHelper.querySiteAerospikeFeedbackRepository(id2)).andReturn(mockSiteFeedbackEntity)
                .times(1);
        expect(mockRepositoryHelper.querySiteEcpmRepository(id.split("_")[0], Integer.parseInt(id.split("_")[1]),
                Integer.parseInt(id.split("_")[2]))).andReturn(mockSiteEcpmEntity).times(1);
        expect(mockRepositoryHelper.queryCurrencyConversionRepository(id)).andReturn(mockCurrencyConversionEntity)
                .times(1);
        expect(mockRepositoryHelper.queryWapSiteUACRepository(id.split("_")[0])).andReturn(mockWapSiteUACEntity)
                .times(1);
        expect(mockRepositoryHelper.queryIXAccountMapRepository(Long.parseLong(id.split("_")[0])))
                .andReturn(mockIXAccountMapEntity).times(1);
        expect(mockRepositoryHelper.queryCreativeRepository(id.split("_")[0], id.split("_")[1]))
                .andReturn(mockCreativeEntity).times(1);

        mockResponseSender.sendResponse(
                "{\"0_1_2_SiteFilterRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},"
                        + "\"isExpired\":false},\"0_1_2_CurrencyConversionRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},"
                        + "\"0_1_2_IXAccountMapRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},"
                        + "\"0_1_2_aerospike\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},"
                        + "\"0_1_2_ChannelRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},"
                        + "\"isActive\":false,\"isTestMode\":false,\"burstQps\":0,\"impressionCeil\":0,"
                        + "\"impressionFloor\":0,\"requestCap\":0,\"priority\":0,\"demandSourceTypeId\":0,"
                        + "\"isRtb\":false,\"wnRequied\":false,\"wnFromClient\":false,\"isSiteInclusion\":false,"
                        + "\"accountSegment\":0},\"0_1_2_ChannelFeedbackRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},"
                        + "\"totalInflow\":0.0,\"totalBurn\":0.0,\"balance\":0.0,\"totalImpressions\":0,"
                        + "\"todayRequests\":0,\"todayImpressions\":0,\"averageLatency\":0,\"revenue\":0.0},"
                        + "\"0_1_2_ChannelAdGroupRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},"
                        + "\"platformTargeting\":0,\"status\":false,\"isTestMode\":false,\"adgroupIncId\":0,"
                        + "\"allTags\":false,\"udIdRequired\":false,\"zipCodeRequired\":false,"
                        + "\"latlongRequired\":false,\"restrictedToRichMediaOnly\":false,\"appUrlEnabled\":false,"
                        + "\"interstitialOnly\":false,\"nonInterstitialOnly\":false,\"stripUdId\":false,"
                        + "\"stripZipCode\":false,\"stripLatlong\":false,\"secure\":false,\"isSiteInclusion\":false,"
                        + "\"impressionCeil\":0,\"ecpmBoost\":0.0,\"dst\":0,\"campaignIncId\":0},"
                        + "\"0_1_2_ChannelSegmentFeedbackRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"eCPM\":0.0,"
                        + "\"fillRatio\":0.0,\"lastHourLatency\":0.0,\"todayRequests\":0,\"beacons\":0,\"clicks\":0,"
                        + "\"todayImpressions\":0},\"0_1_2_PricingEngineRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"rtbFloor\":0"
                        + ".0,\"dcpFloor\":0.0},\"0_aerospike\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},"
                        + "\"lastUpdated\":0},\"0_1_2_WapSiteUACRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},"
                        + "\"siteTypeId\":0,\"isCoppaEnabled\":false,\"isTransparencyEnabled\":false},"
                        + "\"0_1_2_CreativeRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},"
                        + "\"0_1_2_SiteTaxonomyRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}},"
                        + "\"0_1_2_SiteEcpmRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}},\"ecpm\":0.0,"
                        + "\"networkEcpm\":0.0},\"0_1_2_SiteMetaDataRepository\":{\"CGLIB$BOUND\":true,"
                        + "\"CGLIB$CALLBACK_0\":{\"handler\":{\"delegate\":{\"control\":{\"state\":{\"behavior"
                        + "\":{\"behaviorLists\":[],\"stubResults\":[],\"nice\":false,\"checkOrder\":false,"
                        + "\"isThreadSafe\":true,\"shouldBeUsedInOneThread\":false,\"position\":0},"
                        + "\"lock\":{\"sync\":{\"state\":0}}},\"behavior\":{\"behaviorLists\":[],\"stubResults\":[],"
                        + "\"nice\":false,\"checkOrder\":false,\"isThreadSafe\":true,"
                        + "\"shouldBeUsedInOneThread\":false,\"position\":0},\"type\":\"DEFAULT\"}}}}}}", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        CasConfigUtil.repositoryHelper = mockRepositoryHelper;

        final ServletGetSegment tested = new ServletGetSegment();
        tested.handleRequest(mockHttpRequestHandler, mockQueryStringDecoder, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletGetSegment tested = new ServletGetSegment();
        assertThat(tested.getName(), is(IsEqual.equalTo("getSegment")));
    }
}
