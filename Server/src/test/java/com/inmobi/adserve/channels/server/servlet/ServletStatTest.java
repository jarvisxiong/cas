package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.core.IsEqual;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.utils.JarVersionUtil;
import com.inmobi.adserve.channels.util.InspectorStats;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JarVersionUtil.class, InspectorStats.class})
public class ServletStatTest {

    @Test
    public void testHandleRequest() throws Exception {
        final JSONObject inspectorJson = new JSONObject();
        final JSONObject connectionData = new JSONObject();
        final JSONObject inspectorJsonWithManifest = new JSONObject();
        inspectorJson.put("workflow", 50);
        inspectorJsonWithManifest.put("workflow", 50);

        final Map<String, String> manifestData = new HashMap<>();
        manifestData.put("key", "value");
        inspectorJsonWithManifest.put("manifestData", manifestData);

        connectionData.put("key2", "value2");
        inspectorJsonWithManifest.put("connectionData", connectionData);

        mockStatic(InspectorStats.class);
        mockStatic(JarVersionUtil.class);
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);
        final ConnectionLimitHandler mockConnectionLimitHandler = createMock(ConnectionLimitHandler.class);

        expect(InspectorStats.getStatsObj()).andReturn(inspectorJson).times(1);
        expect(JarVersionUtil.getManifestData()).andReturn(manifestData).times(1);
        expect(mockConnectionLimitHandler.getConnectionJson()).andReturn(connectionData).anyTimes();

        mockResponseSender.sendResponse(inspectorJsonWithManifest.toString(), null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        final ServletStat tested = new ServletStat(mockConnectionLimitHandler);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletStat tested = new ServletStat(null);
        assertThat(tested.getName(), is(IsEqual.equalTo("stat")));
    }
}
