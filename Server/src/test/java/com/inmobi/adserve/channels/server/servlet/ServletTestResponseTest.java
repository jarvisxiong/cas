package com.inmobi.adserve.channels.server.servlet;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServletTestResponse.class, Files.class})
public class ServletTestResponseTest {
    private static final String FILE_NAME = "/opt/mkhoj/test/cas/testResponse.txt";

    @Test
    public void testHandleRequest() throws Exception {
        String response = "response";

        mockStatic(Files.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);
        File mockFile = createMock(File.class);

        expectNew(File.class, new Class[]{String.class}, FILE_NAME)
            .andReturn(mockFile).times(1);
        expect(Files.toString(mockFile, Charsets.UTF_8)).andReturn(response).times(1);

        mockResponseSender.sendResponse(response, null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletTestResponse tested = new ServletTestResponse();
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletTestResponse tested = new ServletTestResponse();
        assertThat(tested.getName(), is(IsEqual.equalTo("testResponse")));
    }
}