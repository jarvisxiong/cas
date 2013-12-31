package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;


public class ClickUrlMakerV6Test extends TestCase {
    private Configuration mockConfig = null;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
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
        ClickUrlMakerV6 clickUrlMaker = new ClickUrlMakerV6(null);
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
        assertEquals(
            "http://localhost:8800/6/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/1/e9805b5e",
            clickUrlMaker.getBeaconUrl(new HashMap<String, String>()));
        assertEquals(
            "http://localhost:8800/6/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/1/e9805b5e",
            clickUrlMaker.getClickUrl(new HashMap<String, String>()));
    }
}