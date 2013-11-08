package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.ajillion.DCPAjillionAdnetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPAjillionAdnetworkTest extends TestCase
{
    private Configuration         mockConfig      = null;
    private final String          debug           = "debug";
    private final String          loggerConf      = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap = null;
    private DebugLogger           logger;
    private DCPAjillionAdnetwork  dcpAjillionAdNetwork;
    private final String          AjilionHost     = "http://ad.AjillionMAX.com/ad/%s/4";
    private final String          AjilionStatus   = "on";
    private final String          defintiAdvId    = "Ajilionadv1";
    private final String          AjilionTest     = "1";
    private final String          placementId     = "240";
    private final String          fsPlacementId   = "230";

    public void prepareMockConfig()
    {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("Ajilion.host")).andReturn(AjilionHost).anyTimes();
        expect(mockConfig.getString("Ajilion.slot_4_p")).andReturn(placementId).anyTimes();
        expect(mockConfig.getString("Ajilion.slot_4_fs")).andReturn(fsPlacementId).anyTimes();
        expect(mockConfig.getString("Ajilion.status")).andReturn(AjilionStatus).anyTimes();
        expect(mockConfig.getString("Ajilion.test")).andReturn(AjilionTest).anyTimes();
        expect(mockConfig.getString("Ajilion.advertiserId")).andReturn(defintiAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception
    {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        Formatter.init();
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        DebugLogger.init(mockConfig);
        logger = new DebugLogger();
        dcpAjillionAdNetwork = new DCPAjillionAdnetwork(logger, mockConfig, clientBootstrap, base, serverEvent);
        dcpAjillionAdNetwork.setName("Ajilion");
    }

    @Test
    public void testDCPAjilionConfigureParameters()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDCPAjilionConfigureParametersBlankIP()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDCPAjilionConfigureParametersBlankExtKey()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        sasParams.setSiteType("PERFORMANCE");
        sasParams.setSlot("4");
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDCPAjilionConfigureParametersBlankUA()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSiteType("PERFORMANCE");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testAjilionRequestUri() throws Exception
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        sasParams.setCategories(Arrays.asList(new Long[]
        { 10l, 13l, 30l }));
        String externalKey = "240";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpAjillionAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ad.AjillionMAX.com/ad/240/4?format=json&keyword=Food+%26+Drink%2CAdventure%2CWord&pubid=00000000-0000-0000-0000-000000000000&clientip=206.29.182.240&clientua=Mozilla";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testAjilionRequestUriFamilySafe() throws Exception
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("4");
        sasParams.setSiteType("FAMILY_SAFE");
        sasParams.setCategories(Arrays.asList(new Long[]
        { 10l, 13l, 30l }));
        String externalKey = "240";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpAjillionAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ad.AjillionMAX.com/ad/230/4?format=json&keyword=Food+%26+Drink%2CAdventure%2CWord&pubid=00000000-0000-0000-0000-000000000000&clientip=206.29.182.240&clientua=Mozilla";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPAjilionParseResponseImg() throws Exception
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setCategories(Arrays.asList(new Long[]
        { 10l, 13l, 30l }));
        String externalKey = "19100";
        sasParams.setSiteType("PERFORMANCE");
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, beaconUrl);
        String response = "{\"click_url\": \"http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516\", \"pmodel\": 3, \"price\": 0.005, \"placement_id\": 240, \"height\": 50, \"creative_display\": \"static\", \"creative_url\": \"http://mead-production.s3.amazonaws.com/advertiser_creative/2013/05/02/300x50_dog_1.jpg\", \"success\": true, \"creative_type\": \"image\", \"width\": 300, \"error\": \"\"}";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href='http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://mead-production.s3.amazonaws.com/advertiser_creative/2013/05/02/300x50_dog_1.jpg'  /></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
            dcpAjillionAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAjilionParseResponseImgAppSDK360() throws Exception
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[]
        { 10l, 13l, 30l }));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "{\"click_url\": \"http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516\", \"pmodel\": 3, \"price\": 0.005, \"placement_id\": 240, \"height\": 50, \"creative_display\": \"static\", \"creative_url\": \"http://mead-production.s3.amazonaws.com/advertiser_creative/2013/05/02/300x50_dog_1.jpg\", \"success\": true, \"creative_type\": \"image\", \"width\": 300, \"error\": \"\"}";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516' onclick=\"document.getElementById('click').src='$IMClickUrl';mraid.openExternal('http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516'); return false;\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://mead-production.s3.amazonaws.com/advertiser_creative/2013/05/02/300x50_dog_1.jpg'  /></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
            dcpAjillionAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAjilionParseNoAd() throws Exception
    {
        String response = "{\"success\":\"false\"}";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAjilionParseEmptyResponseCode() throws Exception
    {
        String response = "";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAjilionGetId() throws Exception
    {
        assertEquals(dcpAjillionAdNetwork.getId(), "Ajilionadv1");
    }

    @Test
    public void testDCPAjilionGetImpressionId() throws Exception
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals(dcpAjillionAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPAjilionGetName() throws Exception
    {
        assertEquals(dcpAjillionAdNetwork.getName(), "Ajilion");
    }

    @Test
    public void testDCPAjilionIsClickUrlReq() throws Exception
    {
        assertTrue(dcpAjillionAdNetwork.isClickUrlRequired());
    }
}