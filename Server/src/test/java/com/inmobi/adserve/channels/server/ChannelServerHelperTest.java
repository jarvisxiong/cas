package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ChannelServerHelper.class, InetAddress.class})
public class ChannelServerHelperTest {

	private static ChannelServerHelper channelServerHelper;
	private static Configuration mockConfig;

	private static void prepareMockConfig() {
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
	}

	@BeforeClass
	public static void setUp() {
		prepareMockConfig();

		channelServerHelper = new ChannelServerHelper();
	}

	@Test
	public void testGetDataCentreIdNotSet() {
		assertThat(channelServerHelper.getDataCenterId("dc.id"), is(equalTo((byte) 0)));
	}

	@Test
	public void testGetDataCentreIdAlreadySet() {
		System.setProperty("dc.id", "2");
		assertThat(channelServerHelper.getDataCenterId("dc.id"), is(equalTo((byte) 2)));
		System.clearProperty("dc.id");
	}

	@Test
	public void testGetHostIdNotSet() throws UnknownHostException {
		mockStatic(InetAddress.class);
		expect(InetAddress.getLocalHost()).andThrow(new UnknownHostException("Unknown Host")).times(1);
		PowerMock.replay(InetAddress.class);

		assertThat(channelServerHelper.getHostId("host.name"), is(equalTo((short) 0)));
	}

	@Test
	public void testGetHostDataCenterOutOfBoundException() {
		System.setProperty("host.name", "web200");
		assertThat(channelServerHelper.getHostId("host.name"), is(equalTo((short) 0)));
		System.clearProperty("host.name");
	}

	@Test
	public void testGetHostDataCenterNumberFormatException() {
		System.setProperty("host.name", "web200abcd");
		assertThat(channelServerHelper.getHostId("host.name"), is(equalTo((short) 0)));
		System.clearProperty("host.name");
	}

	@Test
	public void testgetHostDataCenterPositive() {
		System.setProperty("host.name", "web2004.ads.lhr1.inmobi.com");
		final short expected = 2004;
		assertThat(channelServerHelper.getHostId("host.name"), is(equalTo(expected)));
		System.clearProperty("host.name");
	}

	@Test
	public void testGetDataCentreName() {
		final String dummyHostName = "test";
		System.setProperty("host.name", dummyHostName);
		assertThat(channelServerHelper.getDataCentreName("host.name"), is(equalTo(dummyHostName)));
		System.clearProperty("host.name");
	}
}
