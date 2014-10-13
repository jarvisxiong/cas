package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.repository.RepositoryStatsProvider;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
public class ServletErrorDetailsTest {

    @Test
    public void testHandleRequest() throws Exception {
        String errorDetails = "errorDetails";
        mockStatic(CasConfigUtil.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        RepositoryStatsProvider mockRepositoryStatsProvider = createMock(RepositoryStatsProvider.class);

        expect(mockRepositoryHelper.getRepositoryStatsProvider()).andReturn(mockRepositoryStatsProvider).times(1);
        expect(mockRepositoryStatsProvider.getErrorDetails()).andReturn(errorDetails).times(1);
        mockResponseSender.sendResponse(errorDetails, null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        CasConfigUtil.repositoryHelper = mockRepositoryHelper;

        ServletErrorDetails tested = new ServletErrorDetails();
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletErrorDetails tested = new ServletErrorDetails();
        assertThat(tested.getName(), is(IsEqual.equalTo("ErrorDetailstat")));
    }
}