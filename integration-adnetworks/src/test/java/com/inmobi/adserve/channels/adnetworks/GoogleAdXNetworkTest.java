package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.channels.adnetworks.googleadx.GoogleAdXAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Created by naresh.kapse on 24/05/14.
 */
public class GoogleAdXNetworkTest extends TestCase {
  private Configuration mockConfig   = null;
  private final String       debug        = "debug";
  private final String       loggerConf   = "/tmp/channel-server.properties";

  private GoogleAdXAdNetwork googleAdXNetwork;
  private final String googleAdXStatus = "on";
  private final String googleAdXAdvertiserId = "ca-pub-7457767528341420";

  public void prepareMockConfig() {
    mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("googleadx.status")).andReturn(googleAdXStatus).anyTimes();
    expect(mockConfig.getString("googleadx.advertiserId")).andReturn(googleAdXAdvertiserId).anyTimes();
    expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
    expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
    expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
    replay(mockConfig);
  }

  @Override
  public void setUp() throws Exception {
    File f;
    f = new File(loggerConf);
    if (!f.exists()) {
      f.createNewFile();
    }
    Channel serverChannel = createMock(Channel.class);
    HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
    prepareMockConfig();
    SlotSizeMapping.init();
    Formatter.init();
    googleAdXNetwork = new GoogleAdXAdNetwork(mockConfig, null, base, serverChannel);
  }

  @Test
  public void testGoogleAdXNetworkConfigureParameters() throws JSONException {
    SASRequestParameters sasParams = new SASRequestParameters();
    CasInternalRequestParameters
        casInternalRequestParameters = new CasInternalRequestParameters();
    sasParams.setSiteId("c92664f2645c4242bf1c033b2c8184df");
    sasParams.setSlot(Short.valueOf("15"));

    String externalKey = "8a809449013c3c643cad82cb412b5857";
    ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
        googleAdXAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
        null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
        "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
    assertTrue(googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters,                                                    entity, null, null));
  }

  @Test
  public void testGoogleAdXNetworkResponse() throws Exception {
    SASRequestParameters sasParams = new SASRequestParameters();
    CasInternalRequestParameters
        casInternalRequestParameters = new CasInternalRequestParameters();
    sasParams.setSiteId("c92664f2645c4242bf1c033b2c8184df");
    //Setting slot for 320x50
    sasParams.setSlot(Short.valueOf("15"));

    String externalKey = "8a809449013c3c643cad82cb412b5857";
    String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
    ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
        googleAdXAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
        null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
        "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
    googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                                           null, beaconUrl);

    googleAdXNetwork.generateJsAdResponse();
    assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);

    String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"c92664f2645c4242bf1c033b2c8184df\";google_ad_width = \"320\";google_ad_height = \"50\";</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
    assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());
  }
}
