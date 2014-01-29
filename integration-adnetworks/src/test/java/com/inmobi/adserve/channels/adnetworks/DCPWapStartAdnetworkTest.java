package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.channels.adnetworks.wapstart.DCPWapStartAdNetwork;
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
import java.util.Arrays;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


/**
 * @author thushara
 * 
 */
public class DCPWapStartAdnetworkTest extends TestCase {
    private Configuration         mockConfig      = null;
    private final String          debug           = "debug";
    private final String          loggerConf      = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap = null;
    private DCPWapStartAdNetwork  dcpWapstartAdNetwork;
    private final String          wapstartHost    = "http://ro.plus1.wapstart.ru";
    private final String          wapstartStatus  = "on";
    private final String          wapstartAdvId   = "wapstartadv1";
    private final String          wapstartTest    = "1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("wapstart.host")).andReturn(wapstartHost).anyTimes();
        expect(mockConfig.getString("wapstart.status")).andReturn(wapstartStatus).anyTimes();
        expect(mockConfig.getString("wapstart.test")).andReturn(wapstartTest).anyTimes();
        expect(mockConfig.getString("wapstart.advertiserId")).andReturn(wapstartAdvId).anyTimes();
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
        SlotSizeMapping.init();
        dcpWapstartAdNetwork = new DCPWapStartAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testDCPWapstartConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("15"));
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            true);
    }

    @Test
    public void testDCPWapstartConfigureParametersBlankSlot() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWapstartConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSlot(Short.valueOf("15"));
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWapstartConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("15"));
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWapstartConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSlot(Short.valueOf("15"));
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPWapstartRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot(Short.valueOf("15"));
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpWapstartAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ro.plus1.wapstart.ru?version=2&encoding=1&area=viewBannerXml&ip=206.29.182.240&id=1324&pageId=0000000000000000000000200000000000000000&kws=Food+%26+Drink%3BAdventure%3BWord&location=37.4429%2C-122.1514";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPWapstartRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = " ,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot(Short.valueOf("15"));
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11" + "?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpWapstartAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ro.plus1.wapstart.ru?version=2&encoding=1&area=viewBannerXml&ip=206.29.182.240&id=1324&pageId=0000000000000000000000200000000000000000&kws=Food+%26+Drink%3BAdventure%3BWord";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPWapstartParseResponseImg() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><banner>  <id>11864</id>    <title></title>    <content></content>    <link>http://ro.plus1.wapstart.ru/index.php?area=redirector&amp;type=1&amp;rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&amp;site=2562&amp;banner=11864&amp;usr=99895f</link>    <pictureUrl>http://img.ads.wapstart.com/img/4f6/345/c8115c2/image_320x50.gif</pictureUrl>    <cookieSetterUrl>http://ro.plus1.wapstart.ru//?area=counter&amp;clientSession=c9101a8da8de89d9f93357b0615778dc314eef18&amp;bannerId=11864&amp;site=2562</cookieSetterUrl>    <singleLineContent>Новый мобильный сайт AVITO.ru!</singleLineContent></banner>";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href='http://ro.plus1.wapstart.ru/index.php?area=redirector&type=1&rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&site=2562&banner=11864&usr=99895f' onclick=\"document.getElementById('click').src='$IMClickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://img.ads.wapstart.com/img/4f6/345/c8115c2/image_320x50.gif'  /></a><img src='http://ro.plus1.wapstart.ru//?area=counter&clientSession=c9101a8da8de89d9f93357b0615778dc314eef18&bannerId=11864&site=2562' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
            dcpWapstartAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPWapstartParseResponseImgAppSDK360() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><banner>  <id>11864</id>    <title></title>    <content></content>    <link>http://ro.plus1.wapstart.ru/index.php?area=redirector&amp;type=1&amp;rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&amp;site=2562&amp;banner=11864&amp;usr=99895f</link>    <pictureUrl>http://img.ads.wapstart.com/img/4f6/345/c8115c2/image_320x50.gif</pictureUrl>    <cookieSetterUrl>http://ro.plus1.wapstart.ru//?area=counter&amp;clientSession=c9101a8da8de89d9f93357b0615778dc314eef18&amp;bannerId=11864&amp;site=2562</cookieSetterUrl>    <singleLineContent>Новый мобильный сайт AVITO.ru!</singleLineContent></banner>";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://ro.plus1.wapstart.ru/index.php?area=redirector&type=1&rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&site=2562&banner=11864&usr=99895f' onclick=\"document.getElementById('click').src='$IMClickUrl';mraid.openExternal('http://ro.plus1.wapstart.ru/index.php?area=redirector&type=1&rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&site=2562&banner=11864&usr=99895f'); return false;\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://img.ads.wapstart.com/img/4f6/345/c8115c2/image_320x50.gif'  /></a><img src='http://ro.plus1.wapstart.ru//?area=counter&clientSession=c9101a8da8de89d9f93357b0615778dc314eef18&bannerId=11864&site=2562' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
            dcpWapstartAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPWapstartParseResponseTextAdWAP() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot(Short.valueOf("4"));
        sasParams.setSource("wap");
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                + "?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><banner>  <id>11864</id>    <title>Новый мобильный сайт AVITO.ru!</title>    <content>Узнай сколько стоит твой беспорядок</content>    <link>http://ro.plus1.wapstart.ru/index.php?area=redirector&amp;type=1&amp;rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&amp;site=2562&amp;banner=11864&amp;usr=99895f</link>    <pictureUrl></pictureUrl>    <cookieSetterUrl>http://ro.plus1.wapstart.ru//?area=counter&amp;clientSession=c9101a8da8de89d9f93357b0615778dc314eef18&amp;bannerId=11864&amp;site=2562</cookieSetterUrl>    <singleLineContent>Новый мобильный сайт AVITO.ru!</singleLineContent></banner>";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><a style=\"text-decoration:none; \" href=\"http://ro.plus1.wapstart.ru/index.php?area=redirector&type=1&rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&site=2562&banner=11864&usr=99895f\" onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Новый мобильный сайт AVITO.ru!</div><div class=\"adtext-cell-div\" style=\"font-weight:normal;padding:0\">Узнай сколько стоит твой беспорядок</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img src=\"http://ro.plus1.wapstart.ru//?area=counter&clientSession=c9101a8da8de89d9f93357b0615778dc314eef18&bannerId=11864&site=2562\" height=\"1\" width=\"1\" border=\"0\" style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpWapstartAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPWapstartParseResponseTextAdAapSDK360() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot(Short.valueOf("4"));
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setSource("app");
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><banner>  <id>11864</id>    <title>Новый мобильный сайт AVITO.ru!</title>    <content>Узнай сколько стоит твой беспорядок</content>    <link>http://ro.plus1.wapstart.ru/index.php?area=redirector&amp;type=1&amp;rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&amp;site=2562&amp;banner=11864&amp;usr=99895f</link>    <pictureUrl></pictureUrl>    <cookieSetterUrl>http://ro.plus1.wapstart.ru//?area=counter&amp;clientSession=c9101a8da8de89d9f93357b0615778dc314eef18&amp;bannerId=11864&amp;site=2562</cookieSetterUrl>    <singleLineContent>Новый мобильный сайт AVITO.ru!</singleLineContent></banner>";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a style=\"text-decoration:none; \" href=\"http://ro.plus1.wapstart.ru/index.php?area=redirector&type=1&rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&site=2562&banner=11864&usr=99895f\" onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';mraid.openExternal('http://ro.plus1.wapstart.ru/index.php?area=redirector&type=1&rsId=at_f42938923cbe508082fcc174d9add416371b57e9_041200&site=2562&banner=11864&usr=99895f'); return false;\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Новый мобильный сайт AVITO.ru!</div><div class=\"adtext-cell-div\" style=\"font-weight:normal;padding:0\">Узнай сколько стоит твой беспорядок</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img src=\"http://ro.plus1.wapstart.ru//?area=counter&clientSession=c9101a8da8de89d9f93357b0615778dc314eef18&bannerId=11864&site=2562\" height=\"1\" width=\"1\" border=\"0\" style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpWapstartAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPWapstartParseNoAd() throws Exception {
        String response = "";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPWapstartParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPWapstartGetId() throws Exception {
        assertEquals(dcpWapstartAdNetwork.getId(), "wapstartadv1");
    }

    @Test
    public void testDCPWapstartGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("4"));
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals(dcpWapstartAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPWapstartGetName() throws Exception {
        assertEquals(dcpWapstartAdNetwork.getName(), "wapstart");
    }

    @Test
    public void testDCPWapstartIsClickUrlReq() throws Exception {
        assertEquals(dcpWapstartAdNetwork.isClickUrlRequired(), true);
    }
}