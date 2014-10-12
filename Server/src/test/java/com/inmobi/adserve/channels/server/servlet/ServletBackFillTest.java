package com.inmobi.adserve.channels.server.servlet;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class})
public class ServletBackFillTest {

    @Test
    public void testHandleRequest() throws Exception {
        mockStatic(InspectorStats.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        Provider<Marker> mockTraceMarkerProvider = createMock(Provider.class);
        AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);
        CasInternalRequestParameters mockCasInternalRequestParameters = createMock(CasInternalRequestParameters.class);
        HttpRequest mockHttpRequest = createMock(HttpRequest.class);
        HttpHeaders mockHttpHeaders = createMock((HttpHeaders.class));
        RequestFilters mockRequestFilters = createMock(RequestFilters.class);

        expect(mockTraceMarkerProvider.get()).andReturn(null).times(2);
        expect(mockResponseSender.getAuctionEngine()).andReturn(mockAuctionEngine).times(1);
        expect(mockHttpRequestHandler.getHttpRequest()).andReturn(mockHttpRequest).times(1);
        expect(mockHttpRequest.headers()).andReturn(mockHttpHeaders).times(1);
        expect(mockHttpHeaders.get("x-mkhoj-tracer")).andReturn("true");
        expect(mockRequestFilters.isDroppedInRequestFilters(mockHttpRequestHandler)).andReturn(true).times(1);
        InspectorStats.incrementStatCount(InspectorStrings.backFillRequests);
        expectLastCall().times(1);
        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
        expectLastCall().times(1);
        mockResponseSender.sendNoAdResponse(null);
        expectLastCall().times(1);

        // Powermock cannot currently suppress super methods
        // PowerMock.suppress(method(BaseServlet.class, "handleRequest"));
        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        mockResponseSender.sasParams = null;
        mockResponseSender.casInternalRequestParameters = mockCasInternalRequestParameters;

        ServletBackFill tested = new ServletBackFill(mockTraceMarkerProvider, null, mockRequestFilters, null, null, null, null, null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletBackFill tested = new ServletBackFill(null, null, null, null, null, null, null, null);
        assertThat(tested.getName(), is(IsEqual.equalTo("BackFill")));
    }

    @Test
    public void testGetLogger() throws Exception {
        ServletBackFill tested = new ServletBackFill(null, null, null, null, null, null, null, null);
        assertThat(tested.getLogger(), is(equalTo(LoggerFactory.getLogger(ServletBackFill.class))));
    }
}