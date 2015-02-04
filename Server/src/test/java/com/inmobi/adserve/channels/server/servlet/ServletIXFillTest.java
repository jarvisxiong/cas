package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

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
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

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
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplier;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.types.AccountType;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.casthrift.DemandSourceType;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, CasConfigUtil.class, BaseServlet.class})
public class ServletIXFillTest {

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

        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).times(1);
        expect(mockResponseSender.getSasParams()).andReturn(null).anyTimes();
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).times(1);
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).times(1);
        expect(mockHttpHeaders.get("x-mkhoj-tracer")).andReturn("true");
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(true).times(1);
        mockCasInternalRequestParameters.setTraceEnabled(true);
        expectLastCall();
        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().times(1);
        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        final ServletIXFill tested =
                new ServletIXFill(mockTraceMarkerProvider, null, mockRequestFilters, null, null, null, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

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

        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).times(1);
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).times(1);
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).times(1);
        expect(mockHttpHeaders.get("x-mkhoj-tracer")).andReturn("true");
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(false).times(1);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).times(1);
        expect(mockConfig.getBoolean("isResponseOnyFromDCP", false)).andReturn(false).times(1);
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.IX.getValue()).times(1);
        expect(mockResponseSender.getResponseFormat()).andReturn(ResponseSender.ResponseFormat.XHTML).times(1);
        expect(mockResponseSender.getSasParams()).andReturn(mockSASRequestParameters).anyTimes();
        expect(mockMatchSegments.matchSegments(mockSASRequestParameters)).andReturn(
                new ArrayList<AdvertiserMatchedSegmentDetail>());
        expect(mockSASRequestParameters.getImaiBaseUrl()).andReturn(null).times(1);
        expect(mockCasUtils.isVideoSupported(mockSASRequestParameters)).andReturn(false);
        mockCasInternalRequestParameters.setTraceEnabled(true);
        expectLastCall();

        mockSASRequestParameters.setResponseOnlyFromDcp(false);
        expectLastCall().times(1);
        mockSASRequestParameters.setImaiBaseUrl(null);
        expectLastCall().times(1);
        mockSASRequestParameters.setVideoSupported(false);
        expectLastCall().times(1);

        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().times(1);

        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        final ServletIXFill tested =
                new ServletIXFill(mockTraceMarkerProvider, mockMatchSegments, mockRequestFilters, null, mockCasUtils, null,
                        null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

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

        expectNew(CasContext.class).andReturn(mockCasContext).times(1);

        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).times(1);
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).times(1);
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).times(1);
        expect(mockHttpHeaders.get("x-mkhoj-tracer")).andReturn("true");
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(false).times(1);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).times(1);
        expect(mockConfig.getBoolean("isResponseOnyFromDCP", false)).andReturn(false).times(1);
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.IX.getValue()).times(1);
        expect(mockResponseSender.getResponseFormat()).andReturn(ResponseSender.ResponseFormat.XHTML).times(1);
        expect(mockResponseSender.getSasParams()).andReturn(mockSASRequestParameters).anyTimes();
        expect(mockMatchSegments.matchSegments(mockSASRequestParameters)).andReturn(mockList).times(1);
        expect(mockSASRequestParameters.getImaiBaseUrl()).andReturn(null).times(1);
        expect(mockCasUtils.isVideoSupported(mockSASRequestParameters)).andReturn(false);
        expect(mockMatchSegments.getRepositoryHelper()).andReturn(mockRepositoryHelper).times(1);
        expect(mockSASRequestParameters.getSiteId()).andReturn(TestUtils.SampleStrings.siteId).times(1);
        expect(mockRepositoryHelper.querySiteMetaDetaRepository(TestUtils.SampleStrings.siteId)).andReturn(null).times(
                1);
        expect(
                mockChannelSegmentFilterApplier.getChannelSegments(mockList, mockSASRequestParameters, mockCasContext,
                        null, null)).andReturn(null).times(1);
        mockCasInternalRequestParameters.setSiteAccountType(AccountType.SELF_SERVE);
        expectLastCall();
        mockCasInternalRequestParameters.setTraceEnabled(true);
        expectLastCall();

        mockSASRequestParameters.setResponseOnlyFromDcp(false);
        expectLastCall().times(1);
        mockSASRequestParameters.setImaiBaseUrl(null);
        expectLastCall().times(1);
        mockSASRequestParameters.setVideoSupported(false);
        expectLastCall().times(1);

        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().times(1);

        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        final ServletIXFill tested =
                new ServletIXFill(mockTraceMarkerProvider, mockMatchSegments, mockRequestFilters,
                        mockChannelSegmentFilterApplier, mockCasUtils, null, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

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
                createMock(CasInternalRequestParameters.class);
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

        final List<AdvertiserMatchedSegmentDetail> mockList = Arrays.asList(new AdvertiserMatchedSegmentDetail(null));
        final List<ChannelSegment> mockChannelSegmentList =
                Arrays.asList(new ChannelSegment(null, null, null, null, null, null, 0.5));

        expectNew(CasContext.class).andReturn(mockCasContext).times(1);

        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).times(2);
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).times(1);
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).times(1);
        expect(mockHttpHeaders.get("x-mkhoj-tracer")).andReturn("true");
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(false).times(1);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).times(2);
        expect(mockConfig.getBoolean("isResponseOnyFromDCP", false)).andReturn(false).times(1);
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.IX.getValue()).times(1);
        expect(mockResponseSender.getResponseFormat()).andReturn(ResponseSender.ResponseFormat.XHTML).times(1);
        expect(mockResponseSender.getSasParams()).andReturn(mockSASRequestParameters).anyTimes();
        expect(mockMatchSegments.matchSegments(mockSASRequestParameters)).andReturn(mockList).times(1);
        expect(mockSASRequestParameters.getImaiBaseUrl()).andReturn(null).times(1);
        expect(mockMatchSegments.getRepositoryHelper()).andReturn(mockRepositoryHelper).times(1);
        expect(mockSASRequestParameters.getSiteId()).andReturn(TestUtils.SampleStrings.siteId).times(1);
        expect(mockRepositoryHelper.querySiteMetaDetaRepository(TestUtils.SampleStrings.siteId)).andReturn(null).times(
                1);
        expect(
                mockChannelSegmentFilterApplier.getChannelSegments(mockList, mockSASRequestParameters, mockCasContext,
                        null, null)).andReturn(mockChannelSegmentList).times(1);
        expect(mockCasUtils.getNetworkSiteEcpm(mockCasContext, mockSASRequestParameters)).andReturn(0.5).times(1);
        expect(mockCasUtils.getRtbFloor(mockCasContext, mockSASRequestParameters)).andReturn(0.5).times(1);
        expect(mockCasUtils.isVideoSupported(mockSASRequestParameters)).andReturn(false);
        expect(mockSASRequestParameters.getSiteFloor()).andReturn(0.5).times(1);
        expect(mockSASRequestParameters.getSiteIncId()).andReturn(5L).times(1);
        expect(CasConfigUtil.getRtbConfig()).andReturn(mockConfig).times(1);
        expect(CasConfigUtil.getAdapterConfig()).andReturn(mockConfig).times(1);
        expect(mockSASRequestParameters.getUAdapters()).andReturn(null).times(1);
        expect(mockCasInternalRequestParameters.getAuctionBidFloor()).andReturn(0.5).times(1);
        mockCasInternalRequestParameters.setTraceEnabled(true);
        expectLastCall();

        mockSASRequestParameters.setResponseOnlyFromDcp(false);
        expectLastCall().times(1);
        mockSASRequestParameters.setImaiBaseUrl(null);
        expectLastCall().times(1);
        mockSASRequestParameters.setVideoSupported(false);
        expectLastCall().times(1);

        InspectorStats.incrementStatCount(InspectorStrings.IX_REQUESTS);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().times(1);

        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().times(1);
        mockCasInternalRequestParameters.setSiteAccountType(AccountType.SELF_SERVE);
        expectLastCall();

        suppress(method(BaseServlet.class, "enrichCasInternalRequestParameters"));

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        MemberModifier.suppress(BaseServlet.class
                .getDeclaredMethod("incrementTotalSelectedSegmentStats", ChannelSegment.class));

        final ServletIXFill tested =
                new ServletIXFill(mockTraceMarkerProvider, mockMatchSegments, mockRequestFilters,
                        mockChannelSegmentFilterApplier, mockCasUtils, mockAsyncRequestMaker, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletIXFill tested = new ServletIXFill(null, null, null, null, null, null, null, null);
        assertThat(tested.getName(), is(IsEqual.equalTo("ixFill")));
    }

    @Test
    public void testGetLogger() throws Exception {
        final ServletIXFill tested = new ServletIXFill(null, null, null, null, null, null, null, null);
        assertThat(tested.getLogger(), is(equalTo(LoggerFactory.getLogger(ServletIXFill.class))));
    }
}
