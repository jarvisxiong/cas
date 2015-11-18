package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.apache.commons.configuration.Configuration;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

import io.netty.handler.codec.http.HttpRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, CasConfigUtil.class})
public class ServletRtbdTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleRequest() throws Exception {
        mockStatic(InspectorStats.class);
        final HttpRequestHandler mockHttpRequestHandler = createNiceMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final Provider<Marker> mockTraceMarkerProvider = createMock(Provider.class);
        final AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
            createMock(CasInternalRequestParameters.class);
        final HttpRequest mockHttpRequest = createMock(HttpRequest.class);
        final RequestFilters mockRequestFilters = createMock(RequestFilters.class);
        final Configuration mockServerConfig = createMock(Configuration.class);
        final CasUtils mockCasUtils = createMock(CasUtils.class);
        final SASRequestParameters sasRequestParameters = new SASRequestParameters();

        expect(mockCasUtils.isVideoSupported(sasRequestParameters)).andReturn(false).anyTimes();
        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        expect(mockResponseSender.getSasParams()).andReturn(sasRequestParameters).anyTimes();
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).anyTimes();
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(true).times(1);
        expect(mockServerConfig.getBoolean("isRtbEnabled", true)).andReturn(true).anyTimes();
        InspectorStats.incrementStatCount(InspectorStrings.RULE_ENGINE_REQUESTS);
        expectLastCall().times(1);
        InspectorStats.incrementYammerMeter(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUESTS);
        expectLastCall().times(1);
        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().times(1);
        mockStatic(CasConfigUtil.class);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockServerConfig);

        // PowerMock cannot currently suppress super methods
        // PowerMock.suppress(method(BaseServlet.class, "handleRequest"));
        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        final ServletRtbd tested =
            new ServletRtbd(mockTraceMarkerProvider, null, mockRequestFilters, null, mockCasUtils, null, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletRtbd tested = new ServletRtbd(null, null, null, null, null, null, null, null);
        assertThat(tested.getName(), is(IsEqual.equalTo("rtbdFill")));
    }

}
