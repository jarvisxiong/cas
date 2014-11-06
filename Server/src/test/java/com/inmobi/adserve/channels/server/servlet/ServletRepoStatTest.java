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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.repository.RepositoryStatsProvider;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;

@RunWith(PowerMockRunner.class)
public class ServletRepoStatTest {

    @Test
    public void testHandleRequest() throws Exception {
        final String repoStats = "repoStats";
        mockStatic(CasConfigUtil.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final RepositoryStatsProvider mockRepositoryStatsProvider = createMock(RepositoryStatsProvider.class);

        expect(mockRepositoryHelper.getRepositoryStatsProvider()).andReturn(mockRepositoryStatsProvider).times(1);
        expect(mockRepositoryStatsProvider.getStats()).andReturn(repoStats).times(1);
        mockResponseSender.sendResponse(repoStats, null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;
        CasConfigUtil.repositoryHelper = mockRepositoryHelper;

        final ServletRepoStat tested = new ServletRepoStat();
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletRepoStat tested = new ServletRepoStat();
        assertThat(tested.getName(), is(IsEqual.equalTo("repostat")));
    }
}
