package com.inmobi.adserve.channels.util.Utils;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;


public class ClickUrlMakerV6Test extends TestCase {

	public void prepareMockConfig() {
		final Configuration mockConfig = createMock(Configuration.class);
		expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
		expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
		replay(mockConfig);
	}

	@Override
	public void setUp() throws Exception {
		prepareMockConfig();
	}

	@Test
	public void testClickUrlMaker() {
		final ClickUrlMakerV6.Builder builder = ClickUrlMakerV6.newBuilder();
		builder.setTestMode(false);
		builder.setAge(20);
		builder.setBeaconEnabledOnSite(true);
		builder.setCarrierId(2);
		builder.setCountryId(12);
		builder.setSegmentId(201);
		builder.setCPC(true);
		builder.setGender("m");
		builder.setHandsetInternalId((long) 2.0);
		builder.setImageBeaconFlag(true);
		builder.setImpressionId("76256371268");
		builder.setImSdk("0");
		builder.setIpFileVersion((long) 67778);
		builder.setIsBillableDemog(false);
		builder.setImageBeaconURLPrefix("http://localhost:8800");
		builder.setRmBeaconURLPrefix("http://localhost:8800");
		builder.setClickURLPrefix("http://localhost:8800");
		builder.setLocation(0);
		builder.setRmAd(false);
		builder.setSiteIncId((long) 1);
		builder.setHandsetInternalId((long) 1);
		builder.setBudgetBucketId("101");
		final Map<String, String> updMap = new HashMap<String, String>();
		updMap.put("UDID", "uidvalue");
		builder.setUdIdVal(updMap);
		builder.setIpFileVersion((long) 1);
		builder.setCryptoSecretKey("clickmaker.key.1.value");
		final ClickUrlMakerV6 clickUrlMakerV6 = new ClickUrlMakerV6(builder);
		clickUrlMakerV6.createClickUrls();
		assertEquals(
				"http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3",
				clickUrlMakerV6.getBeaconUrl());
		assertEquals(
				"http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/-1/1/0/x/0/nw/101/1/1/5f2b3532",
				clickUrlMakerV6.getClickUrl());
	}
}
