package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.util.InspectorStats;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest(InspectorStats.class)
public class ServletStatTest {

    //@Test
    //public void testHandleRequest() throws Exception {
    //    mockStatic(InspectorStats.class);
    //    HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
    //    ResponseSender mockResponseSender = createMock(ResponseSender.class);
//
    //    String jsonReply = "dummy";
    //    expect(InspectorStats.getStats()).andReturn(jsonReply).times(1);
    //    mockResponseSender.sendResponse(jsonReply, null);
    //    expectLastCall().times(1);
//
    //    replayAll();
    //    mockHttpRequestHandler.responseSender = mockResponseSender;
//
    //    ServletStat tested = new ServletStat();
    //    tested.handleRequest(mockHttpRequestHandler, null, null);
//
    //    verifyAll();
    //}

    @Test
    public void testGetName() throws Exception {
        ServletStat tested = new ServletStat();
        assertThat(tested.getName(), is(IsEqual.equalTo("stat")));
    }
}