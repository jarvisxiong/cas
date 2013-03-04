package com.inmobi.adserve.channels.server;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.util.DebugLogger;

import junit.framework.TestCase;

public class clickUrlMakerV6Test extends TestCase {
  private static DebugLogger logger;

  static {
    logger = EasyMock.createMock(DebugLogger.class);
    expect(logger.isDebugEnabled()).andReturn(false).anyTimes();
    replay(logger);
  }

  @Test
  public void testClickUrlMaker() {
    ClickUrlMakerV6 clickUrlMaker = new ClickUrlMakerV6(logger, null);
    clickUrlMaker.setAge(20);
    clickUrlMaker.setBeaconEnabledOnSite(true);
    clickUrlMaker.setCarrierId(2);
    clickUrlMaker.setCountryId(12);
    clickUrlMaker.setSegmentId(201);
    clickUrlMaker.setCPC(true);
    clickUrlMaker.setGender("m");
    clickUrlMaker.setHandsetInternalId((long) 2.0);
    clickUrlMaker.setImageBeaconFlag(true);
    clickUrlMaker.setImpressionId("76256371268");
    clickUrlMaker.setImSdk("0");
    clickUrlMaker.setIpFileVersion((long) 67778);
    clickUrlMaker.setIsBillableDemog(false);
    clickUrlMaker.setImageBeaconURLPrefix("http://localhost:8800");
    clickUrlMaker.setRmBeaconURLPrefix("http://localhost:8800");
    clickUrlMaker.setClickURLPrefix("http://localhost:8800");
    clickUrlMaker.setLocation(0);
    clickUrlMaker.setRmAd(false);
    clickUrlMaker.setSiteIncId((long) 1);
    clickUrlMaker.setHandsetInternalId((long) 1);
    Map<String, String> updMap = new HashMap<String, String>();
    updMap.put("UDID", "uidvalue");
    clickUrlMaker.setUdIdVal(updMap);
    clickUrlMaker.setIpFileVersion((long) 1);
    clickUrlMaker.setCryptoSecretKey("clickmaker.key.1.value");
    clickUrlMaker.createClickUrls();
    assertEquals("clickUrl", clickUrlMaker.getBeaconUrl(null));
    assertEquals("clickUrl", clickUrlMaker.getClickUrl(null));
  }
}