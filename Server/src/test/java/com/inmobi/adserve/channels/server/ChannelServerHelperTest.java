package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;


public class ChannelServerHelperTest extends TestCase {

  private ChannelServerHelper channelServerHelper;
  private static ConfigurationLoader config;
  private Configuration mockConfig = null;

  public void setUp() throws Exception {
    mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties").anyTimes();
    replay(mockConfig);
    config = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
    DebugLogger.init(config.loggerConfiguration());
    channelServerHelper = new ChannelServerHelper(Logger.getLogger(config.loggerConfiguration().getString("debug")));
  }

  @Test
  public void testGetDcIdWithoutSystemProperty() {
    System.clearProperty("dc.id");
    assertEquals(0, channelServerHelper.getDataCenterId("dc.id"));
  }

  @Test
  public void testGetDcIdAlreadySet() {
    System.setProperty("dc.id", "2");
    assertEquals(2, channelServerHelper.getDataCenterId("dc.id"));
  }

  @Test
  public void testGetHostIdWithoutSystemProperty() {
    System.clearProperty("host.name");
    assertEquals(0, channelServerHelper.getHostId("host.name"));
  }

  @Test
  public void testGetHostDataCenterOutOfBoundException() {
    System.setProperty("host.name", "web200");
    assertEquals(0, channelServerHelper.getHostId("host.name"));
  }

  @Test
  public void testGetHostDataCenterNumberFormatException() {
    System.setProperty("host.name", "web200abcd");
    assertEquals(0, channelServerHelper.getHostId("host.name"));
  }

  @Test
  public void testgetHostAlreadySet() {
    System.setProperty("host.name", "web2004.ads.lhr1.inmobi.com");
    short expected = 2004;
    assertEquals(expected, channelServerHelper.getHostId("host.name"));
  }
}
