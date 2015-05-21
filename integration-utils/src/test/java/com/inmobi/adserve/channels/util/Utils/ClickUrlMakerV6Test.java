package com.inmobi.adserve.channels.util.Utils;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;

import junit.framework.TestCase;


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


    private ClickUrlMakerV6.Builder generateBasicClickUrlMakerV6() {
        ClickUrlMakerV6.Builder builder = ClickUrlMakerV6.newBuilder();
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
        builder.setTestCryptoSecretKey("clickmaker.test.key.1.value");

        builder.setIntegrationDetails(new IntegrationDetails().setIntegrationType(IntegrationType.ANDROID_SDK)
                .setIntegrationVersion(370).setIntegrationMethod(IntegrationMethod.SDK));

        builder.setPlacementId(1234L);
        builder.setNormalizedUserId("normalizedUserId");
        builder.setAppBundleId("appBundleId");
        builder.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        return builder;
    }


    @Test
    public void testClickUrlMaker() {
        final ClickUrlMakerV6 clickUrlMakerV6 = new ClickUrlMakerV6(generateBasicClickUrlMakerV6());
        clickUrlMakerV6.createClickUrls();
        assertEquals(
                "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/-1/0/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/NqQTqBBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/73fe5594",
                clickUrlMakerV6.getBeaconUrl());
        assertEquals(
                "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/-1/1/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/NqQTqBBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/7ca42f24",
                clickUrlMakerV6.getClickUrl());
    }


    @Test
    public void testClickUrlMakerWithTest() {
        ClickUrlMakerV6.Builder builder = generateBasicClickUrlMakerV6();
        builder.setTestMode(true);
        final ClickUrlMakerV6 clickUrlMakerV6 = new ClickUrlMakerV6(builder);
        clickUrlMakerV6.createClickUrls();
        assertEquals(
                "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/-1/0/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/NqQTqBBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/2/ae867707",
                clickUrlMakerV6.getBeaconUrl());
        assertEquals(
                "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/-1/1/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/NqQTqBBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/2/4963d407",
                clickUrlMakerV6.getClickUrl());
    }

    @Test
    public void testClickUrlMakerIsCPCFalse() {
        ClickUrlMakerV6.Builder builder = generateBasicClickUrlMakerV6();
        builder.setTestMode(true);
        builder.setCPC(false);
        final ClickUrlMakerV6 clickUrlMakerV6 = new ClickUrlMakerV6(builder);
        clickUrlMakerV6.createClickUrls();
        assertEquals(
                "http://localhost:8800/C/b/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/-1/0/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/NqQTqBBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/2/a4af85ef",
                clickUrlMakerV6.getBeaconUrl());
        assertEquals(
                "http://localhost:8800/C/b/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/76256371268/0/5l/-1/1/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/NqQTqBBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/2/f58a2179",
                clickUrlMakerV6.getClickUrl());
    }

    @Test
    public void testClickUrlMakerSetNullParams() {
        ClickUrlMakerV6.Builder builder = generateBasicClickUrlMakerV6();
        builder.setSiteIncId(null);
        final ClickUrlMakerV6 clickUrlMakerV6 = new ClickUrlMakerV6(builder);
        clickUrlMakerV6.createClickUrls();
        assertEquals(null, clickUrlMakerV6.getBeaconUrl());
        assertEquals(null, clickUrlMakerV6.getClickUrl());


        // SiteIncId
        builder.setSiteIncId((long) 1);
        builder.setHandsetInternalId(null);
        final ClickUrlMakerV6 clickUrlMakerV62 = new ClickUrlMakerV6(builder);
        clickUrlMakerV62.createClickUrls();
        assertEquals(null, clickUrlMakerV62.getBeaconUrl());
        assertEquals(null, clickUrlMakerV62.getClickUrl());

        // IpFileVersion
        builder.setHandsetInternalId((long) 2.0);
        builder.setIpFileVersion(null);
        final ClickUrlMakerV6 clickUrlMakerV63 = new ClickUrlMakerV6(builder);
        clickUrlMakerV63.createClickUrls();
        assertEquals(null, clickUrlMakerV63.getBeaconUrl());
        assertEquals(null, clickUrlMakerV63.getClickUrl());

        // impressionId
        builder.setIpFileVersion((long) 67778);
        builder.setImpressionId(null);
        final ClickUrlMakerV6 clickUrlMakerV64 = new ClickUrlMakerV6(builder);
        clickUrlMakerV64.createClickUrls();
        assertEquals(null, clickUrlMakerV64.getBeaconUrl());
        assertEquals(null, clickUrlMakerV64.getClickUrl());

        builder.setImpressionId("76256371268");
        builder.setHandsetInternalId(null);
        final ClickUrlMakerV6 clickUrlMakerV65 = new ClickUrlMakerV6(builder);
        clickUrlMakerV65.createClickUrls();
        assertEquals(null, clickUrlMakerV65.getBeaconUrl());
        assertEquals(null, clickUrlMakerV65.getClickUrl());

    }

}
