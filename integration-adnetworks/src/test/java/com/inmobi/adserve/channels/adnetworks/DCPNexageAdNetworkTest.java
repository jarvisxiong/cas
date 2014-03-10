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
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.nexage.DCPNexageAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.HttpRequestHandlerBase;


public class DCPNexageAdNetworkTest extends TestCase {
    private Configuration         mockConfig      = null;
    private final String          debug           = "debug";
    private final String          loggerConf      = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap = null;

    private DCPNexageAdNetwork    dcpNexageAdnetwork;
    private final String          NexageHost      = "http://bos.ads.nexage.com/adServe?";
    private final String          NexageStatus    = "on";
    private final String          NexageAdvId     = "nexageadv1";
    private final String          NexageTest      = "test";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("nexage.host")).andReturn(NexageHost).anyTimes();
        expect(mockConfig.getString("nexage.status")).andReturn(NexageStatus).anyTimes();
        expect(mockConfig.getString("nexage.test")).andReturn(NexageTest).anyTimes();
        expect(mockConfig.getString("nexage.advertiserId")).andReturn(NexageAdvId).anyTimes();
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
        dcpNexageAdnetwork = new DCPNexageAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testDCPNexageConfigureParameters() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot("9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl));
    }

    @Test
    public void testDCPNexageConfigureParametersWithNoUid() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot("9");
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl));
    }

    @Test
    public void testDCPNexageConfigureParametersWithAdditionalParamsNotSet() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot("9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject("{}"),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl));
    }

    @Test
    public void testDCPNexageConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl));
    }

    @Test
    public void testNexageConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl));
    }

    @Test
    public void testDCPNexageConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl));
    }

    @Test
    public void testDCPNexageRequestUriWithSegmentCategory() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        Long[] categories = new Long[] { 13l, 15l };
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                + "?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, categories, true, true, externalKey, null, null, null, 0, false,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    new JSONObject("{\"pos\":\"header\"}")), new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=Adventure&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithRONSegmentSiteCategory() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 26l }));
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriforiPad() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(63l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"leader\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=leader&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28iPad%3B+U%3B+CPU+OS+3_2+like+Mac+OS+X%3B+en-us%29+AppleWebKit%2F531.21.10+%28KHTML%2C+like+Gecko%29+Version%2F4.0.4+Mobile%2F7B334b+Safari%2F531.21.10&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB20&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithBlindedSiteId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(48l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        sasParams.setSiteIncId(123456);
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 56789, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 56789));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB7&p(blind_id)=00000000-0000-ddd5-0000-00000001e240";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithDoubleEncodedUserAgent() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("NokiaC3-00%252F5.0%2B%252808.65%2529%2BProfile%252FMIDP-2.1%2BConfiguration%252FCLDC-1.1%2BMozilla%252F5.0%2BAppleWebKit%252F420%252B%2B%2528KHTML%252C%2Blike%2BGecko%2529%2BSafari%252F420%252B");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=NokiaC3-00%2F5.0+%2808.65%29+Profile%2FMIDP-2.1+Configuration%2FCLDC-1.1+Mozilla%2F5.0+AppleWebKit%2F420++%28KHTML%2C+like+Gecko%29+Safari%2F420&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithEncodedUserAgent() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithCountry() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setCountry("US");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000&u(country)=USA";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutCountry() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithDMA() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setArea("area");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000&u(dma)=area";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutDMA() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithZip() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        casInternalRequestParameters.zipCode = "123456";
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&req(zip)=123456&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutZip() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithGender() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setGender("m");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&u(gender)=m&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutGender() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithAge() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setAge("30");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&u(age)=30&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutAge() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriSitePerformance() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setSiteType("PERFORMANCE");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=p&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriSiteFamilySafe() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setSiteType("FAMILY_SAFE");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, blurl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x50&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriBlankSlot() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        SlotSizeMapping.init();
        String burl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl)) {
            String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=IAB1&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageParseResponseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSlot("4");
        sasParams.setSource("APP");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div>";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageParseResponseAdWAP() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSlot("4");
        sasParams.setSource("wap");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div>";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageParseResponseAdApp() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus "
                + "Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534" + ".30");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div>";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageParseResponseAdAppIMAI() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div>";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageGenerateStaticJsAdTag() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\",\"jsAdTag\":\"true\"}}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        dcpNexageAdnetwork.generateJsAdResponse();
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid(); var admax_vars = { dcn: \"19100\",pos: \"header\",\"p(blind_id)\": \"00000000-0000-0000-0000-000000000000\",cn: \"IAB20-1\"};if (suid) admax_vars[\"u(id)\"]=suid;admaxAd(admax_vars);</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageGenerateStaticJsAdTagMultipleCategories() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        List<Long> cat = new ArrayList<Long>();
        cat.add(3l);
        sasParams.setCategories(cat);
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\",\"jsAdTag\":\"true\"}}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        dcpNexageAdnetwork.generateJsAdResponse();
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid(); var admax_vars = { dcn: \"19100\",pos: \"header\",\"p(blind_id)\": \"00000000-0000-0000-0000-000000000000\",cn: \"IAB19-15\"};if (suid) admax_vars[\"u(id)\"]=suid;admaxAd(admax_vars);</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageParseNoAd() throws Exception {
        String response = "";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPNexageParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(StringUtils.isBlank(dcpNexageAdnetwork.getHttpResponseContent()), true);
    }

    @Test
    public void testDCPNexageGetId() throws Exception {
        assertEquals(dcpNexageAdnetwork.getId(), "nexageadv1");
    }

    @Test
    public void testDCPNexageGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5" + ".0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "8a809449013c3c643cad82cb412b5857";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            NexageAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"pos\":\"header\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, blurl);
        assertEquals(dcpNexageAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPNexageGetName() throws Exception {
        assertEquals(dcpNexageAdnetwork.getName(), "nexage");
    }

    @Test
    public void testDCPNexageIsClickUrlReq() throws Exception {
        assertEquals(dcpNexageAdnetwork.isClickUrlRequired(), false);
    }

    @Test
    public void testDCPNexageIsBeaconUrlReq() throws Exception {
        assertEquals(dcpNexageAdnetwork.isBeaconUrlRequired(), true);
    }
}