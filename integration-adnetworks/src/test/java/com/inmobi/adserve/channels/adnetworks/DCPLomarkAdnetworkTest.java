package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.lomark.DCPLomarkAdNetwork;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPLomarkAdnetworkTest extends TestCase {
    private Configuration         mockConfig      = null;
    private final String          debug           = "debug";
    private final String          loggerConf      = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap = null;
    private DCPLomarkAdNetwork    dcpLomarkAdnetwork;
    private final String          lomarkHost      = "http://apitest.lomark.cn/v2/get";
    private final String          lomarkStatus    = "on";
    private final String          lomarkAdvId     = "lomarkadv1";
    private final String          lomarkKey       = "1000";
    private final String          lomarkSecretKey = "SecretKey";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("lomark.host")).andReturn(lomarkHost).anyTimes();
        expect(mockConfig.getString("lomark.status")).andReturn(lomarkStatus).anyTimes();
        expect(mockConfig.getString("lomark.advertiserId")).andReturn(lomarkAdvId).anyTimes();
        expect(mockConfig.getString("lomark.key")).andReturn(lomarkKey).anyTimes();
        expect(mockConfig.getString("lomark.secretkey")).andReturn(lomarkSecretKey).anyTimes();
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
        dcpLomarkAdnetwork = new DCPLomarkAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testDCPLomarkConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot((short)9);
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSource("app");
        sasParams.setOsId(5); // iphone
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPLomarkConfigureParametersAppWithoutUdid() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot((short)9);
        sasParams.setOsId(5); // iphone
        sasParams.setSource("app");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpLomarkAdnetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPLomarkConfigureParametersWAP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setSlot((short)9);
        sasParams.setOsId(HandSetOS.Windows_CE.getValue());
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSource("wap");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPLomarkConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSource("app");
        sasParams.setOsId(5);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");

        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpLomarkAdnetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testLomarkConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";

        sasParams.setSource("app");
        sasParams.setOsId(5);

        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";

        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpLomarkAdnetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPLomarkConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSource("iphone");
        sasParams.setOsId(5);

        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpLomarkAdnetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPLomarkRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("5.63.24.1");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot((short)9);
        sasParams.setSource("app");
        sasParams.setSiteIncId(12121212);
        sasParams.setSdkVersion("a3.5.2");
        sasParams.setOsId(5);
        sasParams.setCategories(new ArrayList<Long>());

        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 12121212, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 12121212));
        if (dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpLomarkAdnetwork.getRequestUri().toString();
            HashMap<String, String> requestMap = new HashMap<String, String>();
            String millisec = "";
            String[] params = actualUrl.split("&");
            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];

                if (name.equalsIgnoreCase("Timestamp")) {
                    requestMap.put("Timestamp", value);
                    millisec = value;
                }
            }

            requestMap.put("Format", "json");
            requestMap.put("Ip", "5.63.24.1");
            requestMap.put("Client", "2");
            requestMap.put("Key", lomarkKey);
            requestMap.put("AppId", "1324");
            requestMap.put("AdSpaceType", "1");
            requestMap.put("Operator", "4");
            requestMap.put("Lat", "37.4429");
            requestMap.put("Long", "-122.1514");
            requestMap.put("Uuid", "202cb962ac59075b964b07152d234b70");
            requestMap.put("DeviceType", "1");
            requestMap.put("AppName", "00000000-00b8-f47c-0000-000000b8f47c");
            requestMap.put("Category", "12");
            requestMap.put("SiteType", "1");
            requestMap.put("Aw", "320");
            requestMap.put("Ah", "48");
            requestMap.put("SdkVersion", "a3.5.2");

            String expectedUrl = "http://apitest.lomark.cn/v2/get?Format=json&Ip=5.63.24.1&Client=2&SiteType=1&Key=1000&AppId=1324&AdSpaceType=1&Operator=4&Lat=37.4429&Long=-122.1514&Uuid=202cb962ac59075b964b07152d234b70&DeviceType=1&AppName=00000000-00b8-f47c-0000-000000b8f47c&Category=12&Aw=320&Ah=48&SdkVersion=a3.5.2&Timestamp="
                    + millisec + "&Sign=" + getSignature(requestMap, lomarkSecretKey);
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPLomarkRequestUriWithSegmentCategory() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("5.63.24.1");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot((short)9);
        sasParams.setSource("app");
        sasParams.setSiteIncId(12121212);
        sasParams.setSdkVersion("a3.5.2");
        sasParams.setOsId(5);

        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        Long[] segmentCategories = new Long[] { 11l, 15l };
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null,
            12121212, true, null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
            null, new ArrayList<Integer>(), 0.0d, null, null, 12121212));
        if (dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpLomarkAdnetwork.getRequestUri().toString();
            HashMap<String, String> requestMap = new HashMap<String, String>();
            String millisec = "";
            String[] params = actualUrl.split("&");
            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];

                if (name.equalsIgnoreCase("Timestamp")) {
                    requestMap.put("Timestamp", value);
                    millisec = value;
                }
            }

            requestMap.put("Format", "json");
            requestMap.put("Ip", "5.63.24.1");
            requestMap.put("Client", "2");
            requestMap.put("Key", lomarkKey);
            requestMap.put("AppId", "1324");
            requestMap.put("AdSpaceType", "1");
            requestMap.put("Operator", "4");
            requestMap.put("Lat", "37.4429");
            requestMap.put("Long", "-122.1514");
            requestMap.put("Uuid", "202cb962ac59075b964b07152d234b70");
            requestMap.put("DeviceType", "1");
            requestMap.put("AppName", "00000000-00b8-f47c-0000-000000b8f47c");
            requestMap.put("Category", "8");
            requestMap.put("SiteType", "1");
            requestMap.put("Aw", "320");
            requestMap.put("Ah", "48");
            requestMap.put("SdkVersion", "a3.5.2");

            String expectedUrl = "http://apitest.lomark.cn/v2/get?Format=json&Ip=5.63.24.1&Client=2&SiteType=1&Key=1000&AppId=1324&AdSpaceType=1&Operator=4&Lat=37.4429&Long=-122.1514&Uuid=202cb962ac59075b964b07152d234b70&DeviceType=1&AppName=00000000-00b8-f47c-0000-000000b8f47c&Category=8&Aw=320&Ah=48&SdkVersion=a3.5.2&Timestamp="
                    + millisec + "&Sign=" + getSignature(requestMap, lomarkSecretKey);
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPLomarkRequestUriWithSiteCategorySegmentRON() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("5.63.24.1");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot((short)9);
        sasParams.setSource("app");
        sasParams.setSiteIncId(12121212);
        sasParams.setSdkVersion("a3.5.2");
        sasParams.setOsId(5);

        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        Long[] segmentCategories = new Long[] { 1l };
        Long[] cat = new Long[] { 99l, 15l };
        sasParams.setCategories(Arrays.asList(cat));
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null,
            12121212, true, null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
            null, new ArrayList<Integer>(), 0.0d, null, null, 12121212));
        if (dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpLomarkAdnetwork.getRequestUri().toString();
            HashMap<String, String> requestMap = new HashMap<String, String>();
            String millisec = "";
            String[] params = actualUrl.split("&");
            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];

                if (name.equalsIgnoreCase("Timestamp")) {
                    requestMap.put("Timestamp", value);
                    millisec = value;
                }
            }

            requestMap.put("Format", "json");
            requestMap.put("Ip", "5.63.24.1");
            requestMap.put("Client", "2");
            requestMap.put("Key", lomarkKey);
            requestMap.put("AppId", "1324");
            requestMap.put("AdSpaceType", "1");
            requestMap.put("Operator", "4");
            requestMap.put("Lat", "37.4429");
            requestMap.put("Long", "-122.1514");
            requestMap.put("Uuid", "202cb962ac59075b964b07152d234b70");
            requestMap.put("DeviceType", "1");
            requestMap.put("AppName", "00000000-00b8-f47c-0000-000000b8f47c");
            requestMap.put("Category", "10");
            requestMap.put("SiteType", "1");
            requestMap.put("Aw", "320");
            requestMap.put("Ah", "48");
            requestMap.put("SdkVersion", "a3.5.2");

            String expectedUrl = "http://apitest.lomark.cn/v2/get?Format=json&Ip=5.63.24.1&Client=2&SiteType=1&Key=1000&AppId=1324&AdSpaceType=1&Operator=4&Lat=37.4429&Long=-122.1514&Uuid=202cb962ac59075b964b07152d234b70&DeviceType=1&AppName=00000000-00b8-f47c-0000-000000b8f47c&Category=10&Aw=320&Ah=48&SdkVersion=a3.5.2&Timestamp="
                    + millisec + "&Sign=" + getSignature(requestMap, lomarkSecretKey);
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPLomarkRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = ",-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot((short)15);
        sasParams.setSource("app");
        sasParams.setRemoteHostIp("5.63.24.1");
        sasParams.setSiteIncId(12121212);
        sasParams.setOsId(3);

        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 12121212, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 12121212));
        if (dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpLomarkAdnetwork.getRequestUri().toString();
            HashMap<String, String> requestMap = new HashMap<String, String>();
            requestMap.put("Format", "json");
            requestMap.put("Ip", "5.63.24.1");
            requestMap.put("Client", "1");
            requestMap.put("Key", lomarkKey);
            requestMap.put("AppId", "1324");
            requestMap.put("AdSpaceType", "1");
            requestMap.put("Operator", "4");
            requestMap.put("Uuid", "202cb962ac59075b964b07152d234b70");
            requestMap.put("DeviceType", "1");
            requestMap.put("AppName", "00000000-00b8-f47c-0000-000000b8f47c");
            requestMap.put("Category", "12");
            requestMap.put("SiteType", "1");
            requestMap.put("Aw", "320");
            requestMap.put("Ah", "50");
            String millisec = "";
            String[] params = actualUrl.split("&");
            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];

                if (name.equalsIgnoreCase("Timestamp")) {
                    requestMap.put("Timestamp", value);
                    millisec = value;
                }
            }

            String expectedUrl = "http://apitest.lomark.cn/v2/get?Format=json&Ip=5.63.24.1&Client=1&SiteType=1&Key=1000&AppId=1324&AdSpaceType=1&Operator=4&Uuid=202cb962ac59075b964b07152d234b70&DeviceType=1&AppName=00000000-00b8-f47c-0000-000000b8f47c&Category=12&Aw=320&Ah=50&Timestamp="
                    + millisec + "&Sign=" + getSignature(requestMap, lomarkSecretKey);
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPLomarkParseResponseAdForImageAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot((short)4);
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "{\"status\":\"100\",\"msg\":\"成功\",\"data\":{\"ad\":{\"aid\":\"1\",\"ts\":\"1355799274\",\"sessionid\":\"4d2b62c51e6e4b069e380017168beff3\",\"creative\":{\"cid\":\"1\",\"ts\":\"1355799221\",\"displayinfo\":{\"type\":\"1\",\"img\":\"http://apitest.lomark.cn/upload/201212/201212181053416444os_320_50.jpg\",\"schema\":\"fb98e702541d4f17acd7af0015e0779a\"},\"clkinfos\":[{\"type\":\"1\",\"schema\":\"fb98e702541d4f17acd7af0015e0779a\",\"url\":\"http://www.donson.com.cn\"}],\"trackers\":{\"clicks\":[{\"schema\":\"fb98e702541d4f17acd7af0015e0779a\",\"urls\":[\"http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}\"]}],\"display\":{\"urls\":[\"http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}\"]}}}}}}";
        dcpLomarkAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpLomarkAdnetwork.getHttpResponseStatusCode());
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href='http://www.donson.com.cn' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';document.getElementById('partnerClick').src='http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://apitest.lomark.cn/upload/201212/201212181053416444os_320_50.jpg'  /></a><img src='http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/><img id=\"partnerClick\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpLomarkAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPLomarkParseResponseAdForGifAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot((short)4);
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                + "?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "{\"status\":\"100\",\"msg\":\"成功\",\"data\":{\"ad\":{\"aid\":\"7\",\"ts\":\"1355801288\",\"sessionid\":\"e13ef4470f1e4f798c17e066ed4f1de1\",\"creative\":{\"cid\":\"9\",\"ts\":\"1355801272\",\"displayinfo\":{\"type\":\"4\",\"img\":\"http://apitest.lomark.cn/upload/201212/201212181127521380l9_320_50.gif\",\"schema\":\"bf9e2a0844e840b3aa8af4ad14b6924a\"},\"clkinfos\":[{\"type\":\"1\",\"schema\":\"bf9e2a0844e840b3aa8af4ad14b6924a\",\"url\":\"http://www.sohu.com\"}],\"trackers\":{\"clicks\":[{\"schema\":\"bf9e2a0844e840b3aa8af4ad14b6924a\",\"urls\":[\"http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}\"]}],\"display\":{\"urls\":[\"http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}\"]}}}}}}";
        dcpLomarkAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpLomarkAdnetwork.getHttpResponseStatusCode());
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href='http://www.sohu.com' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';document.getElementById('partnerClick').src='http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://apitest.lomark.cn/upload/201212/201212181127521380l9_320_50.gif'  /></a><img src='http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/><img id=\"partnerClick\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpLomarkAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPLomarkParseNoAd() throws Exception {
        String response = "";
        dcpLomarkAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpLomarkAdnetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPLomarkParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpLomarkAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpLomarkAdnetwork.getHttpResponseStatusCode());
        assertEquals("", dcpLomarkAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPLomarkGetId() throws Exception {
        assertEquals("lomarkadv1", dcpLomarkAdnetwork.getId());
    }

    @Test
    public void testDCPLomarkGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            lomarkAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpLomarkAdnetwork.getImpressionId());
    }

    @Test
    public void testDCPLomarkGetName() throws Exception {
        assertEquals("lomark", dcpLomarkAdnetwork.getName());
    }

    @Test
    public void testDCPLomarkIsClickUrlReq() throws Exception {
        assertTrue(dcpLomarkAdnetwork.isClickUrlRequired());
    }

    private String getSignature(final HashMap<String, String> params, final String secret) throws IOException

    {
        // first sort asc as per the paramter names
        Map<String, String> sortedParams = new TreeMap<String, String>(params);
        Set<Entry<String, String>> entrys = sortedParams.entrySet();
        // after sorting，organize all paramters with key=value"format
        StringBuilder basestring = new StringBuilder();
        for (Entry<String, String> param : entrys) {
            basestring.append(param.getKey()).append("=").append(param.getValue());
        }
        basestring.append(secret);
        // MD5 Hashed
        byte[] bytes = DigestUtils.md5(basestring.toString().getBytes());
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }
}
