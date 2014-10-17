package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationConverter;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CasConfigUtil.class, ConfigurationConverter.class})
public class ServletGetAdapterConfigTest {

  private Map<String, List<String>> createMapFromStringPair(final String key, final String value) {
    final Map<String, List<String>> params = new HashMap<>();
    params.put(key, Arrays.asList(value));
    return params;
  }

  @Test
  public void testHandleRequest() throws Exception {
    final String key = "key";
    final String response = "response";

    mockStatic(CasConfigUtil.class);
    mockStatic(ConfigurationConverter.class);

    final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
    final ResponseSender mockResponseSender = createMock(ResponseSender.class);

    expect(CasConfigUtil.getAdapterConfig()).andReturn(null).times(1);
    expect(ConfigurationConverter.getMap(null)).andReturn(createMapFromStringPair(key, response)).times(1);
    mockResponseSender.sendResponse("{\"" + key + "\":[\"" + response + "\"]}", null);
    expectLastCall().times(1);

    replayAll();
    mockHttpRequestHandler.responseSender = mockResponseSender;

    final ServletGetAdapterConfig tested = new ServletGetAdapterConfig();
    tested.handleRequest(mockHttpRequestHandler, null, null);

    verifyAll();
  }

  @Test
  public void testGetName() throws Exception {
    final ServletGetAdapterConfig tested = new ServletGetAdapterConfig();
    assertThat(tested.getName(), is(IsEqual.equalTo("getAdapterConfig")));
  }
}
