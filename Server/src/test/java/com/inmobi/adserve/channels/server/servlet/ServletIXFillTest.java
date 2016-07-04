package com.inmobi.adserve.channels.server.servlet;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.WAP;
import static java.util.Collections.singletonList;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Marker;
import org.testng.annotations.BeforeMethod;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.server.requesthandler.ResponseFormat;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplier;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.casthrift.DemandSourceType;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, CasConfigUtil.class, BaseServlet.class, ServletIXFill.class})
public class ServletIXFillTest {

    @BeforeMethod
    public void setUp() throws IOException, IllegalAccessException {
        ImpressionIdGenerator.init((short) 123, (byte) 10);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleRequestDroppedInRequestFilters() throws Exception {
        /**
         * Branches/Conditions followed: Is dropped in request filters
         */
        mockStatic(InspectorStats.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final Provider<Marker> mockTraceMarkerProvider = createMock(Provider.class);
        final AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
            createMock(CasInternalRequestParameters.class);
        final HttpRequest mockHttpRequest = createMock(HttpRequest.class);
        final HttpHeaders mockHttpHeaders = createMock(HttpHeaders.class);
        final RequestFilters mockRequestFilters = createMock(RequestFilters.class);
        final CasUtils mockCasUtils = createMock(CasUtils.class);
        final SASRequestParameters sasRequestParameters = new SASRequestParameters();

        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        expect(mockResponseSender.getSasParams()).andReturn(sasRequestParameters).anyTimes();
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).anyTimes();
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).anyTimes();
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(true).anyTimes();
        expect(mockCasUtils.isVideoSupportedSite(sasRequestParameters)).andReturn(false).anyTimes();
        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        expectLastCall().anyTimes();
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().anyTimes();
        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().anyTimes();

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        final ServletIXFill tested =
            new ServletIXFill(mockTraceMarkerProvider, null, mockRequestFilters, null, mockCasUtils, null, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleRequestNoMatchedSegmentDetails() throws Exception {
        /**
         * Branches/Conditions followed: Is not dropped in request filters IsResponseOnlyFromDcp is false
         * DemandSourceType is IX ResponseFormat is XHTML MatchedSegmentDetails is empty
         */
        mockStatic(InspectorStats.class);
        mockStatic(CasConfigUtil.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final Provider<Marker> mockTraceMarkerProvider = createMock(Provider.class);
        final AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
            createMock(CasInternalRequestParameters.class);
        final HttpRequest mockHttpRequest = createMock(HttpRequest.class);
        final HttpHeaders mockHttpHeaders = createMock(HttpHeaders.class);
        final RequestFilters mockRequestFilters = createMock(RequestFilters.class);
        final SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        final Configuration mockConfig = createMock(Configuration.class);
        final MatchSegments mockMatchSegments = createMock(MatchSegments.class);
        final CasUtils mockCasUtils = createMock(CasUtils.class);

        expect(mockCasUtils.isVideoSupportedSite(mockSASRequestParameters)).andReturn(false).anyTimes();
        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).anyTimes();
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).anyTimes();
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(false).anyTimes();
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).anyTimes();
        expect(mockConfig.getBoolean("photon.enable", false)).andReturn(false).anyTimes();
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.IX.getValue()).anyTimes();
        expect(mockSASRequestParameters.getSource()).andReturn(WAP).anyTimes();
        expect(mockResponseSender.getResponseFormat()).andReturn(ResponseFormat.XHTML).anyTimes();
        expect(mockResponseSender.getSasParams()).andReturn(mockSASRequestParameters).anyTimes();
        expect(mockMatchSegments.matchSegments(mockSASRequestParameters)).andReturn(new ArrayList<>());
        expect(mockSASRequestParameters.getImaiBaseUrl()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.isCoppaEnabled()).andReturn(true).anyTimes();

        mockSASRequestParameters.setImaiBaseUrl(null);
        expectLastCall().anyTimes();
        mockSASRequestParameters.setVideoSupported(false);
        expectLastCall().anyTimes();
        mockSASRequestParameters.setMovieBoardRequest(false);
        expectLastCall().anyTimes();
        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        expectLastCall().anyTimes();
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().anyTimes();

        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().anyTimes();

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        final ServletIXFill tested =
            new ServletIXFill(mockTraceMarkerProvider, mockMatchSegments, mockRequestFilters, null, mockCasUtils, null, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleRequestNullFilteredSegments() throws Exception {
        /**
         * Branches/Conditions followed: Is not dropped in request filters IsResponseOnlyFromDcp is false
         * DemandSourceType is IX ResponseFormat is XHTML MatchedSegmentDetails is not empty siteMetaDataEntity is null
         * filteredSegments are null
         */
        mockStatic(InspectorStats.class);
        mockStatic(CasConfigUtil.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final Provider<Marker> mockTraceMarkerProvider = createMock(Provider.class);
        final AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
            createMock(CasInternalRequestParameters.class);
        final HttpRequest mockHttpRequest = createMock(HttpRequest.class);
        final HttpHeaders mockHttpHeaders = createMock(HttpHeaders.class);
        final RequestFilters mockRequestFilters = createMock(RequestFilters.class);
        final SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        final Configuration mockConfig = createMock(Configuration.class);
        final MatchSegments mockMatchSegments = createMock(MatchSegments.class);
        final CasUtils mockCasUtils = createMock(CasUtils.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final ChannelSegmentFilterApplier mockChannelSegmentFilterApplier =
            createMock(ChannelSegmentFilterApplier.class);
        final CasContext mockCasContext = createMock(CasContext.class);

        final List<AdvertiserMatchedSegmentDetail> mockList = Arrays.asList(new AdvertiserMatchedSegmentDetail(null));

        expectNew(CasContext.class).andReturn(mockCasContext).anyTimes();

        expect(mockCasUtils.isVideoSupportedSite(mockSASRequestParameters)).andReturn(false).anyTimes();
        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).anyTimes();
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).anyTimes();
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(false).anyTimes();
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).anyTimes();
        expect(mockConfig.getBoolean("photon.enable", false)).andReturn(false).anyTimes();
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.IX.getValue()).anyTimes();
        expect(mockResponseSender.getResponseFormat()).andReturn(ResponseFormat.XHTML).anyTimes();
        expect(mockResponseSender.getSasParams()).andReturn(mockSASRequestParameters).anyTimes();
        expect(mockMatchSegments.matchSegments(mockSASRequestParameters)).andReturn(mockList).anyTimes();
        expect(mockSASRequestParameters.getImaiBaseUrl()).andReturn(null).anyTimes();
        expect(mockMatchSegments.getRepositoryHelper()).andReturn(mockRepositoryHelper).anyTimes();
        expect(mockSASRequestParameters.getSiteId()).andReturn(TestUtils.SampleStrings.siteId).anyTimes();
        expect(mockSASRequestParameters.getSource()).andReturn(WAP).anyTimes();
        expect(mockRepositoryHelper.querySiteMetaDetaRepository(TestUtils.SampleStrings.siteId)).andReturn(null)
            .anyTimes();
        expect(mockChannelSegmentFilterApplier
                .getChannelSegments(mockList, mockSASRequestParameters, mockCasContext, null, null)).andReturn(null)
            .anyTimes();

        mockSASRequestParameters.setImaiBaseUrl(null);
        expectLastCall().anyTimes();
        mockSASRequestParameters.setVideoSupported(false);
        expectLastCall().anyTimes();
        mockSASRequestParameters.setMovieBoardRequest(false);
        expectLastCall().anyTimes();

        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        expectLastCall().anyTimes();
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().anyTimes();

        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().anyTimes();

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        final ServletIXFill tested =
            new ServletIXFill(mockTraceMarkerProvider, mockMatchSegments, mockRequestFilters, mockChannelSegmentFilterApplier, mockCasUtils, null, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleRequestRankListIsEmpty() throws Exception {
        /**
         * Branches/Conditions followed: Is not dropped in request filters IsResponseOnlyFromDcp is false
         * DemandSourceType is IX ResponseFormat is XHTML MatchedSegmentDetails is not empty siteMetaDataEntity is null
         * filteredSegments are not empty rankList is empty
         */
        mockStatic(InspectorStats.class);
        mockStatic(CasConfigUtil.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final Provider<Marker> mockTraceMarkerProvider = createMock(Provider.class);
        final AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
            createNiceMock(CasInternalRequestParameters.class);
        final HttpRequest mockHttpRequest = createMock(HttpRequest.class);
        final HttpHeaders mockHttpHeaders = createMock(HttpHeaders.class);
        final RequestFilters mockRequestFilters = createMock(RequestFilters.class);
        final SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        final Configuration mockConfig = createMock(Configuration.class);
        final MatchSegments mockMatchSegments = createMock(MatchSegments.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final ChannelSegmentFilterApplier mockChannelSegmentFilterApplier =
            createMock(ChannelSegmentFilterApplier.class);
        final CasContext mockCasContext = createMock(CasContext.class);
        final CasUtils mockCasUtils = createMock(CasUtils.class);
        final AsyncRequestMaker mockAsyncRequestMaker = createNiceMock(AsyncRequestMaker.class);

        final List<AdvertiserMatchedSegmentDetail> mockList = singletonList(new AdvertiserMatchedSegmentDetail(null));
        final List<ChannelSegment> mockChannelSegmentList =
                singletonList(new ChannelSegment(null, null, null, null, null, null, 0.5));

        expectNew(CasContext.class).andReturn(mockCasContext).anyTimes();

        expect(mockCasUtils.isVideoSupportedSite(mockSASRequestParameters)).andReturn(false).anyTimes();
        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).anyTimes();
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).anyTimes();
        expect(mockHttpHeaders.get("x-mkhoj-tracer")).andReturn("true");
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(false).anyTimes();
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).times(1);
        expect(mockConfig.getBoolean("photon.enable", false)).andReturn(false).anyTimes();
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.IX.getValue()).anyTimes();
        expect(mockResponseSender.getResponseFormat()).andReturn(ResponseFormat.XHTML).anyTimes();
        expect(mockResponseSender.getSasParams()).andReturn(mockSASRequestParameters).anyTimes();
        expect(mockMatchSegments.matchSegments(mockSASRequestParameters)).andReturn(mockList).anyTimes();
        expect(mockSASRequestParameters.getImaiBaseUrl()).andReturn(null).anyTimes();
        expect(mockMatchSegments.getRepositoryHelper()).andReturn(mockRepositoryHelper).anyTimes();
        expect(mockSASRequestParameters.getSiteId()).andReturn(TestUtils.SampleStrings.siteId).anyTimes();
        expect(mockSASRequestParameters.getMarketRate()).andReturn(1.5).anyTimes();
        mockSASRequestParameters.setMarketRate(1.5);
        expectLastCall().anyTimes();
        expect(mockRepositoryHelper.querySiteMetaDetaRepository(TestUtils.SampleStrings.siteId)).andReturn(null)
            .times(1);
        expect(mockChannelSegmentFilterApplier
                .getChannelSegments(mockList, mockSASRequestParameters, mockCasContext, null, null))
            .andReturn(mockChannelSegmentList).anyTimes();
        expect(mockSASRequestParameters.getSiteFloor()).andReturn(0.5).anyTimes();
        expect(mockSASRequestParameters.getSiteIncId()).andReturn(5L).anyTimes();
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).anyTimes();
        expect(CasConfigUtil.getRtbConfig()).andReturn(mockConfig).anyTimes();
        expect(CasConfigUtil.getAdapterConfig()).andReturn(mockConfig).anyTimes();
        expect(mockSASRequestParameters.getUAdapters()).andReturn(null).anyTimes();
        expect(mockSASRequestParameters.getSource()).andReturn(WAP).anyTimes();
        expect(mockCasInternalRequestParameters.getAuctionBidFloor()).andReturn(0.5).anyTimes();
        expect(mockSASRequestParameters.isCoppaEnabled()).andReturn(true).anyTimes();

        mockSASRequestParameters.setImaiBaseUrl(null);
        expectLastCall().anyTimes();
        mockSASRequestParameters.setVideoSupported(false);
        expectLastCall().anyTimes();
        mockSASRequestParameters.setMovieBoardRequest(false);
        expectLastCall().anyTimes();

        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        expectLastCall().anyTimes();
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().anyTimes();

        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().anyTimes();
        mockCasInternalRequestParameters.setSiteAccountType(AccountType.SELF_SERVE);
        expectLastCall();

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;


        MemberModifier.suppress(ServletIXFill.class
            .getDeclaredMethod("specificEnrichment", CasContext.class, SASRequestParameters.class, CasInternalRequestParameters.class));
        MemberModifier.suppress(BaseServlet.class.getDeclaredMethod("incrementTotalSelectedSegmentStats", List.class));

        ImpressionIdGenerator.init((short) 123, (byte) 10);
        final ServletIXFill tested =
            new ServletIXFill(mockTraceMarkerProvider, mockMatchSegments, mockRequestFilters, mockChannelSegmentFilterApplier, mockCasUtils, mockAsyncRequestMaker, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletIXFill tested = new ServletIXFill(null, null, null, null, null, null, null, null);
        assertThat(tested.getName(), is(IsEqual.equalTo("ixFill")));
    }

}
