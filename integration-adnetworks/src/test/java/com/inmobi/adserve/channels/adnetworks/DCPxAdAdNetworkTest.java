package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.xad.DCPxAdAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPxAdAdNetworkTest extends TestCase {
    private Configuration         mockConfig      = null;
    private final String          debug           = "debug";
    private final String          loggerConf      = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap = null;

    private DCPxAdAdNetwork       dcpxAdAdnetwork;
    private final String          xAdHost         = "http://xad.com/rest/banner";
    private final String          xAdStatus       = "on";
    private final String          xAdAdvId        = "xadadv1";
    private final String          xAdTest         = "1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("xad.host")).andReturn(xAdHost).anyTimes();
        expect(mockConfig.getString("xad.status")).andReturn(xAdStatus).anyTimes();
        expect(mockConfig.getString("xad.test")).andReturn(xAdTest).anyTimes();
        expect(mockConfig.getString("xad.advertiserId")).andReturn(xAdAdvId).anyTimes();
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
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        SlotSizeMapping.init();
        Formatter.init();
        dcpxAdAdnetwork = new DCPxAdAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testDCPxAdConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot("9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertTrue(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPxAdConfigureParametersWithNoUid() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot("9");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertTrue(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPxAdConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testxAdConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPxAdConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
            false);
    }

    @Test
    public void testDCPxAdRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] { 9l, 10l, 13l, 30l }));
        Long[] segmentCategories = { 10l };
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        sasParams.setOsId(3);
        sasParams.setSiteId("12345");
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null, 0, false, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpxAdAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://xad.com/rest/banner?v=1.1&o_fmt=html5&ip=206.29.182.240&k=1324&appid=00000000-0000-0000-0000-000000000000_Food_&_Drink&devid=Mozilla&lat=37.4429&long=-122.1514&uid=202cb962ac59075b964b07152d234b70&uid_type=android_id%7Cmd5&size=320x48&category=Food+%26+Drink&os=Android";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPxAdRequestUriWithMoreCategories() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] { 9l, 10l, 13l, 30l }));
        Long[] segmentCategories = { 10l };
        List<Long> siteCategories = new ArrayList<Long>();
        siteCategories.add(12l);
        siteCategories.add(13l);
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uidIFA = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        sasParams.setOsId(5);
        sasParams.setSiteId("12345");
        sasParams.setCategories(siteCategories);
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpxAdAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://xad.com/rest/banner?v=1.1&o_fmt=html5&ip=206.29.182.240&k=1324&appid=00000000-0000-0000-0000-000000000000_Action&devid=Mozilla&lat=37.4429&long=-122.1514&uid=202cb962ac59075b964b07152d234b70&uid_type=idfa%7Cplain&size=320x48&category=Action%3AAdventure&os=iPhone_OS";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPxAdRequestUriWithDerivedLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("iPhone");
        sasParams.setCategories(Arrays.asList(new Long[] { 9l, 10l, 13l, 30l }));
        Long[] segmentCategories = { 10l };
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uidO1 = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        sasParams.setOsId(5);
        sasParams.setSiteId("12345");
        sasParams.setLocSrc("derived-lat-lon");
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11" + "?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null, 0, false, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpxAdAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://xad.com/rest/banner?v=1.1&o_fmt=html5&ip=206.29.182.240&k=1324&appid=00000000-0000-0000-0000-000000000000_Food_&_Drink&devid=iPhone&uid=202cb962ac59075b964b07152d234b70&uid_type=uuid%7Csha1&size=320x48&category=Food+%26+Drink&os=iPhone_OS";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPxAdRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        sasParams.setOsId(3);
        sasParams.setCategories(new ArrayList<Long>());
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertTrue(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPxAdRequestUriNoUdid() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "11.6,-11.87";
        sasParams.setCategories(Arrays.asList(new Long[] { 9l, 10l, 13l, 30l }));
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSlot("15");
        sasParams.setOsId(3);
        sasParams.setCategories(new ArrayList<Long>());
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 0));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpxAdAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://xad.com/rest/banner?v=1.1&o_fmt=html5&ip=206.29.182.240&k=1324&appid=00000000-0000-0000-0000-000000000000_miscellenous&devid=Mozilla&lat=11.6&long=-11.87&uid=7a31d90b848f70062e6281b058cc52fc&uid_type=uuid%7Cmd5&size=320x50&category=miscellenous&os=Android";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPxAdRequestUriBlankSlot() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot(null);
        sasParams.setCategories(new ArrayList<Long>());
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertFalse(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPxAdParseResponseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setOsId(3);
        sasParams.setSlot("4");
        sasParams.setSiteId("1234567");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                + "?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl)) {
            String response = "<meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script>";
            dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 200);
            String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
            assertEquals(expectedResponse, dcpxAdAdnetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testDCPxAdParseResponseAdWAP() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("wap");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script>";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpxAdAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPxAdParseResponseAdApp() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        String externalKey = "19100";
        sasParams.setSource("app");
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script>";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpxAdAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPxAdParseResponseAdAppIMAI() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        String externalKey = "19100";
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script>";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpxAdAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPxAdParseNoAd() throws Exception {
        String response = "";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPxAdParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpxAdAdnetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPxAdGetId() throws Exception {
        assertEquals(dcpxAdAdnetwork.getId(), "xadadv1");
    }

    @Test
    public void testDCPxAdGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId,
            null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals(dcpxAdAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPxAdGetName() throws Exception {
        assertEquals(dcpxAdAdnetwork.getName(), "xad");
    }

    @Test
    public void testDCPxAdIsClickUrlReq() throws Exception {
        assertEquals(dcpxAdAdnetwork.isClickUrlRequired(), false);
    }

    @Test
    public void testDCPxAdIsBeaconUrlReq() throws Exception {
        assertEquals(dcpxAdAdnetwork.isBeaconUrlRequired(), true);
    }
}