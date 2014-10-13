package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.utils.JarVersionUtil;
import com.inmobi.adserve.channels.util.InspectorStats;
import org.hamcrest.core.IsEqual;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JarVersionUtil.class, InspectorStats.class})
public class ServletStatTest {
    @Ignore
    @Test
    //todo Ishan fix this
    public void testHandleRequest() throws Exception {
        JSONObject inspectorJson = new JSONObject();
        JSONObject inspectorJsonWithManifest = new JSONObject();
        inspectorJson.put("workflow", 50);
        inspectorJsonWithManifest.put("workflow", 50);

        Map<String, String> manifestData = new HashMap<>();
        manifestData.put("key", "value");
        inspectorJsonWithManifest.put("manifestData", manifestData);

        mockStatic(InspectorStats.class);
        mockStatic(JarVersionUtil.class);
        HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        ResponseSender mockResponseSender = createMock(ResponseSender.class);

        expect(InspectorStats.getStatsObj()).andReturn(inspectorJson).times(1);
        expect(JarVersionUtil.getManifestData()).andReturn(manifestData).times(1);

        mockResponseSender.sendResponse(inspectorJsonWithManifest.toString(), null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        ServletStat tested = new ServletStat(null);
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        ServletStat tested = new ServletStat(null);
        assertThat(tested.getName(), is(IsEqual.equalTo("stat")));
    }
}