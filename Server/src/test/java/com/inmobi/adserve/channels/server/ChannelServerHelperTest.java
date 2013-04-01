package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;


public class ChannelServerHelperTest extends TestCase {

  private ChannelServerHelper channelServerHelper;
  private Configuration mockConfig = null;

  public void setUp() throws Exception {
    mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("rr")).andReturn("rr").anyTimes();
    expect(mockConfig.getString("channel")).andReturn("channel").anyTimes();
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("advertiser")).andReturn("advertiser").anyTimes();
    expect(mockConfig.getString("sampledadvertiser")).andReturn("sampledadvertiser").anyTimes();
    expect(mockConfig.getString("repository")).andReturn("repository").anyTimes();
    expect(mockConfig.getString(" logger.sampledadvertisercount")).andReturn("5").anyTimes();
    expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
    expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
    replay(mockConfig);
    channelServerHelper = new ChannelServerHelper(Logger.getLogger(mockConfig.getString("debug")));
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
