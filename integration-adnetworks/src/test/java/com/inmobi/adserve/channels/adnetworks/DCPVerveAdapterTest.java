package com.inmobi.adserve.channels.adnetworks;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import com.inmobi.adserve.channels.adnetworks.verve.DCPVerveAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPVerveAdapterTest extends TestCase {
    private Configuration         mockConfig      = null;
    private final String          debug           = "debug";
    private final String          loggerConf      = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap = null;
    private DebugLogger           logger;
    private DCPVerveAdNetwork     dcpVerveAdnetwork;
    private final String          verveHost       = "http://adcel.vrvm.com/htmlad";
    private final String          verveStatus     = "on";
    private final String          verveAdvId      = "verveadv1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("verve.host")).andReturn(verveHost).anyTimes();
        expect(mockConfig.getString("verve.status")).andReturn(verveStatus).anyTimes();
        expect(mockConfig.getString("verve.advertiserId")).andReturn(verveAdvId).anyTimes();
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
        DebugLogger.init(mockConfig);
        SlotSizeMapping.init();
        Formatter.init();
        logger = new DebugLogger();
        dcpVerveAdnetwork = new DCPVerveAdNetwork(logger, mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testDCPVerveConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot("9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPVerveConfigureParametersIPOnlySet() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot("9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"trueLatLongOnly\":\"false\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPVerveConfigureParametersTrueLatLongSet() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot("9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"trueLatLongOnly\":\"true\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPVerveConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSource("iphone");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testVerveConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSource("iphone");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPVerveConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSource("iphone");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPVerveConfigureParametersBlockAndroid() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("mozilla");
        sasParams.setSource("android");
        sasParams.setSdkVersion("a354");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPVerveConfigureParametersUnblockAndroidVersion() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("mozilla");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setSdkVersion("a360");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPVerveRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        sasParams.setLocSrc("true-lat-lon");
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"trueLatLongOnly\":\"true\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpVerveAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://adcel.vrvm.com/htmlad?ip=206.29.182.240&p=iphn&b=1324&site=00000000-0000-0000-0000-000000000000&ua=Mozilla&lat=37.4429&long=-122.1514&uis=v&ui=202cb962ac59075b964b07152d234b70&c=999&adunit=320x48";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPVerveRequestUriWithIPOnly() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        sasParams.setLocSrc("derived-lat-lon");
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"trueLatLongOnly\":\"false\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpVerveAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://adcel.vrvm.com/htmlad?ip=206.29.182.240&p=iphn&b=1324&site=00000000-0000-0020-0000-000000000000&ua=Mozilla&uis=v&ui=202cb962ac59075b964b07152d234b70&c=999&adunit=320x48";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPVerveRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = ",-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        sasParams.setOsId(HandSetOS.Android.getValue());
        String externalKey = "1324";
        sasParams.setLocSrc("true-lat-lon");
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"trueLatLongOnly\":\"true\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpVerveAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://adcel.vrvm.com/htmlad?ip=206.29.182.240&p=iphn&b=1324&site=00000000-0000-0020-0000-000000000000&ua=Mozilla&uis=v&ui=202cb962ac59075b964b07152d234b70&c=999&adunit=320x48";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPVerveRequestUriBlankSlot() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("9");
        sasParams.setSource("wap");
        String externalKey = "1324";
        sasParams.setLocSrc("true-lat-lon");
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"trueLatLongOnly\":\"true\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpVerveAdnetwork.getRequestUri().toString();
            String expectedUrl = "http://adcel.vrvm.com/htmlad?ip=206.29.182.240&p=ptnr&b=1324&site=00000000-0000-0000-0000-000000000000&ua=Mozilla&lat=37.4429&long=-122.1514&c=999&adunit=320x48";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPVerveParseResponseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        String response = "<a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/>";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), expectedResponse);
    }

    @Test
    public void testDCPVerveParseResponseAdWap() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("wap");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/>";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), expectedResponse);
    }

    @Test
    public void testDCPVerveParseResponseAdApp() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                + "?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/>";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), expectedResponse);
    }

    @Test
    public void testDCPVerveParseResponseAdAppIMAI() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a371");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/>";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href=\"http://c.ypcdn.com/2/c/rtd?rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&vrid=6318744211295763877&&lsrc=SP&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&ptsid=imtest&tl=1933&dest=http%3A%2F%2Fapi.yp.com%2Fdisplay%2Fv1%2Fmip%3Fappid%3Dnchx22pyyt%26requestid%3D3258f028-8ab0-4b04-86ed-d7935132def1%26category%3DMedical%2520Clinics%26visitorid%3D6318744211295763877%26srid%3Dcffe8661-a6ba-4a45-817b-41a68eca9e84_%26listings%3D171643024_%26uselid%3D1%26distance%3D171643024%3A25.8_%26numListings%3D1%26city%3DPalo%2BAlto%252C%2BCA%252C%2B94301%26searchLat%3D37.444324%26searchLon%3D-122.14969%26product%3Dimage_snapshot%26selected%3D0%26selectedLat%3D37.80506134033203%26selectedLon%3D-122.27301788330078%26time%3D1355818336208\" target=\"_blank\"><img src=\"http://display-img.g.atti.com/image-api/adimage?x=eNoVxkEKwjAQAMDXuBeppGmr8bAHEbwJBV-wTVJdXJOSpGB_bzzNJHbY6cHMSpvG0KSaflJ9Y47eNe507oa2087PLVgqePeOLclOq6twYJvrgqdUGUli5SIlwrRmvNGHZduPQiFweO4fi7dMwrlkcJxRDwcDiYpFWF4IuVBCsHGtlfifoKP0hm0RVOC_BX9v6Tat\"/></a><img src=\"http://c.ypcdn.com/2/i/rtd?vrid=6318744211295763877&rid=3258f028-8ab0-4b04-86ed-d7935132def1&ptid=nchx22pyyt&iid=7f173043-fad2-4708-9f4c-f6ddb38670bd&srid=cffe8661-a6ba-4a45-817b-41a68eca9e84&lsrc=SP&cp=_&ptsid=imtest\" style=\"display:none;\"/><img src=\"http://b.scorecardresearch.com/p?c1=3&amp;c2=6035991&amp;c14=1\" height=\"1\" width=\"1\" alt=\"*\"><img src=\"http://go.vrvm.com/t?poid=4&adnet=33&r=6405519111883133169&e=AdImpInternal&paid=5721\" width=\"1\" height=\"1\" style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), expectedResponse);
    }

    @Test
    public void testDCPVerveParseNoAd() throws Exception {
        String response = "";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPVerveParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpVerveAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpVerveAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpVerveAdnetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPVerveGetId() throws Exception {
        assertEquals(dcpVerveAdnetwork.getId(), "verveadv1");
    }

    @Test
    public void testDCPVerveGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            verveAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpVerveAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals(dcpVerveAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPVerveGetName() throws Exception {
        assertEquals(dcpVerveAdnetwork.getName(), "verve");
    }

    @Test
    public void testDCPVerveIsClickUrlReq() throws Exception {
        assertEquals(dcpVerveAdnetwork.isClickUrlRequired(), false);
    }
}
