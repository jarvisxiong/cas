package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.channels.adnetworks.widerplanet.DCPWiderPlanetAdnetwork;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class DCPWiderPlanetAdNetworkTest extends TestCase {
    private Configuration           mockConfig        = null;
    private final String            debug             = "debug";
    private final String            loggerConf        = "/tmp/channel-server.properties";
    private final ClientBootstrap   clientBootstrap   = null;
    private DCPWiderPlanetAdnetwork dcpWiderPlanetAdNetwork;
    private final String            widerPlanetHost   = "http://adtg.widerplanet.com/delivery/adw.php";
    private final String            widerPlanetStatus = "on";
    private final String            widerPlanetAdvId  = "widerplanetadv1";
    private final String            widerPlanetTest   = "1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("widerplanet.host")).andReturn(widerPlanetHost).anyTimes();
        expect(mockConfig.getString("widerplanet.status")).andReturn(widerPlanetStatus).anyTimes();
        expect(mockConfig.getString("widerplanet.test")).andReturn(widerPlanetTest).anyTimes();
        expect(mockConfig.getString("widerplanet.advertiserId")).andReturn(widerPlanetAdvId).anyTimes();
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
        Formatter.init();
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        dcpWiderPlanetAdNetwork = new DCPWiderPlanetAdnetwork(mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testDCPWiderPlanetConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUidParams("{\"imuc__5\":\"90c8fdb9-f109-4d1c-a791-830ff869f358\"}");
        sasParams.setSource("WAP");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            true);
    }

    @Test
    public void testDCPWiderPlanetConfigureParametersNonImucUId() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUidParams("{\"md5\":\"90c8fdb9-f109-4d1c-a791-830ff869f358\"}");
        sasParams.setSource("WAP");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWiderPlanetConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWiderPlanetConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWiderPlanetConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWiderPlanetConfigureParametersApp() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("mozilla ");
        sasParams.setSource("APP");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWiderPlanetRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setUidParams("{\"imuc__5\":\"90c8fdb9-f109-4d1c-a791-830ff869f358\"}");
        sasParams.setSource("WAP");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot(Short.valueOf("15"));
        String externalKey = "7211";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpWiderPlanetAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://adtg.widerplanet.com/delivery/adw.php?zoneid=7211&useragent=Mozilla&uip=206.29.182.240&wuid=50ef30779da35e2ea3d3bdb384ed305a&location=37.4429%2C-122.1514&duid=202cb962ac59075b964b07152d234b70";
            assertEquals(actualUrl, expectedUrl);
        }
    }

    @Test
    public void testDCPWiderPlanetRequestUriWithWC() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setUidParams("{\"wc\":\"90c8fdb9-f109-4d1c-a791-830ff869f358\"}");
        sasParams.setSource("WAP");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot(Short.valueOf("15"));
        String externalKey = "7211";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpWiderPlanetAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://adtg.widerplanet.com/delivery/adw.php?zoneid=7211&useragent=Mozilla&uip=206.29.182.240&wuid=50ef30779da35e2ea3d3bdb384ed305a&location=37.4429%2C-122.1514&duid=202cb962ac59075b964b07152d234b70";
            assertEquals(actualUrl, expectedUrl);
        }
    }

    @Test
    public void testDCPWiderPlanetRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "";
        sasParams.setUidParams("{\"imuc__5\":\"90c8fdb9-f109-4d1c-a791-830ff869f358\"}");
        sasParams.setSource("WAP");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot(Short.valueOf("15"));
        String externalKey = "7211";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpWiderPlanetAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://adtg.widerplanet.com/delivery/adw.php?zoneid=7211&useragent=Mozilla&uip=206.29.182.240&wuid=50ef30779da35e2ea3d3bdb384ed305a&duid=202cb962ac59075b964b07152d234b70";
            assertEquals(actualUrl, expectedUrl);
        }
    }

    @Test
    public void testDCPWiderPlanetParseResponse() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        String externalKey = "19100";
        sasParams.setUidParams("{\"imuc__5\":\"90c8fdb9-f109-4d1c-a791-830ff869f358\"}");
        sasParams.setSource("WAP");
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl,
            beaconUrl);
        String response = "{\"response\":\"ad\",\"imageUrl\":\"http://aitg.widerplanet.com/images/e815dc6f7def89ef6c814db04c99de6c.jpg\",\"imageWidth\":\"320\",\"imageHeight\":\"50\",\"landingUrl\":\"http://adtg.widerplanet.com/delivery/ck.php?oaparams=2__lid=7210__bannerid=24883__zoneid=7211__OXLCA=1__cb=224973e970__user_segment_id=0__context_segment_id=1__host=__domain=__category=__dlid=5b538eb890b86645481ab8f2e4ae5353136972180537__rv=8c__rvt=1__ctype=10__ft=0__oadest=http%3A%2F%2Fwww.inmobi.com&amp;qsc=1lghmpu&amp;wuid=50ef30779da35e2ea3d3bdb384ed305a&amp;uip=206.29.182.240\",\"beacon\":\"http://adtg.widerplanet.com/delivery/lg.php?lid=7210&bannerid=24883&campaignid=10470&zoneid=7211&OXLIA=1&cb=2faa3e2448&user_segment_id=0&context_segment_id=1&host=&domain=&category=&dlid=5b538eb890b86645481ab8f2e4ae5353136972180537&rv=8c&rvt=1&vk=%CB%CA%CF%CC%2B%8E%CFN%AD%2C%CF%2FJ%29%B6%D5I%2FJ-IM%CE%40%12%81%B3%00&ctype=10&sl=1&bc=32%B60%B7%B423%B7%D012%B1%B00%B623%B0%04%B2%0C%0D%81b%06+%86%91%01%90%01%00&ft=0&amp;qsc=1odkajr&amp;wuid=50ef30779da35e2ea3d3bdb384ed305a&amp;uip=206.29.182.240\",\"beacon_ext1\":\"http://sosa.semanticrep.com/rt/widerplanet.php?c=tg&ex=1370326605&or=N&rd=418d60dec8&pu=1369778400\",\"beacon_ext2\":\"http://adimg.wisenut.co.kr/tgates/?c=tg&ex=1369721805&or=N&rd=963fd53489&pu=1369778400\"}";
        dcpWiderPlanetAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWiderPlanetAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://adtg.widerplanet.com/delivery/ck.php?oaparams=2__lid=7210__bannerid=24883__zoneid=7211__OXLCA=1__cb=224973e970__user_segment_id=0__context_segment_id=1__host=__domain=__category=__dlid=5b538eb890b86645481ab8f2e4ae5353136972180537__rv=8c__rvt=1__ctype=10__ft=0__oadest=http%3A%2F%2Fwww.inmobi.com&amp;qsc=1lghmpu&amp;wuid=50ef30779da35e2ea3d3bdb384ed305a&amp;uip=206.29.182.240' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://aitg.widerplanet.com/images/e815dc6f7def89ef6c814db04c99de6c.jpg'  /></a><img src='http://adtg.widerplanet.com/delivery/lg.php?lid=7210&bannerid=24883&campaignid=10470&zoneid=7211&OXLIA=1&cb=2faa3e2448&user_segment_id=0&context_segment_id=1&host=&domain=&category=&dlid=5b538eb890b86645481ab8f2e4ae5353136972180537&rv=8c&rvt=1&vk=%CB%CA%CF%CC%2B%8E%CFN%AD%2C%CF%2FJ%29%B6%D5I%2FJ-IM%CE%40%12%81%B3%00&ctype=10&sl=1&bc=32%B60%B7%B423%B7%D012%B1%B00%B623%B0%04%B2%0C%0D%81b%06+%86%91%01%90%01%00&ft=0&amp;qsc=1odkajr&amp;wuid=50ef30779da35e2ea3d3bdb384ed305a&amp;uip=206.29.182.240' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://sosa.semanticrep.com/rt/widerplanet.php?c=tg&ex=1370326605&or=N&rd=418d60dec8&pu=1369778400' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://adimg.wisenut.co.kr/tgates/?c=tg&ex=1369721805&or=N&rd=963fd53489&pu=1369778400' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
            dcpWiderPlanetAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPWiderPlanetParseNoAd() throws Exception {
        String response = "{response: \"\"}";
        dcpWiderPlanetAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWiderPlanetAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpWiderPlanetAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPWiderPlanetParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpWiderPlanetAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWiderPlanetAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpWiderPlanetAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPWiderPlanetGetId() throws Exception {
        assertEquals(dcpWiderPlanetAdNetwork.getId(), "widerplanetadv1");
    }

    @Test
    public void testDCPWiderPlanetGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUidParams("{\"imuc__5\":\"90c8fdb9-f109-4d1c-a791-830ff869f358\"}");
        sasParams.setSource("WAP");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            widerPlanetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWiderPlanetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals(dcpWiderPlanetAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPWiderPlanetGetName() throws Exception {
        assertEquals(dcpWiderPlanetAdNetwork.getName(), "widerplanet");
    }

    @Test
    public void testDCPWiderPlanetIsClickUrlReq() throws Exception {
        assertEquals(dcpWiderPlanetAdNetwork.isClickUrlRequired(), true);
    }

}
