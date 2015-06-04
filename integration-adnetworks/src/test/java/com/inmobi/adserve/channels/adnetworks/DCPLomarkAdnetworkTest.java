package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.adnetworks.lomark.DCPLomarkAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPLomarkAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String lomarkHost = "http://apitest.lomark.cn/v2/get";
    private static final String lomarkStatus = "on";
    private static final String lomarkAdvId = "lomarkadv1";
    private static final String lomarkKey = "1000";
    private static final String lomarkSecretKey = "SecretKey";
    private static Configuration mockConfig = null;
    private static DCPLomarkAdNetwork dcpLomarkAdnetwork;
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
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

    @BeforeClass
    public static void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        final SlotSizeMapEntity slotSizeMapEntityFor1 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor1.getDimension()).andReturn(new Dimension(120, 20)).anyTimes();
        replay(slotSizeMapEntityFor1);
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor12 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor12.getDimension()).andReturn(new Dimension(468, 60)).anyTimes();
        replay(slotSizeMapEntityFor12);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        replay(slotSizeMapEntityFor15);
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 12))
                .andReturn(slotSizeMapEntityFor12).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        replay(repositoryHelper);
        dcpLomarkAdnetwork = new DCPLomarkAdNetwork(mockConfig, null, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpLomarkAdnetwork.setHost(lomarkHost);
    }

    @Test
    public void testDCPLomarkConfigureParameters() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("app");
        sasParams.setOsId(5); // iphone
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPLomarkConfigureParametersAppWithoutUdid() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(5); // iphone
        sasParams.setSource("app");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpLomarkAdnetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPLomarkConfigureParametersWAP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(HandSetOS.Windows_CE.getValue());
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("wap");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPLomarkConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSource("app");
        sasParams.setOsId(5);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");

        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpLomarkAdnetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testLomarkConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");

        sasParams.setSource("app");
        sasParams.setOsId(5);

        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");

        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpLomarkAdnetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPLomarkConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSource("iphone");
        sasParams.setOsId(5);

        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpLomarkAdnetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPLomarkRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("5.63.24.1");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("app");
        sasParams.setSiteIncId(12121212);
        sasParams.setSdkVersion("a3.5.2");
        sasParams.setOsId(5);
        sasParams.setCategories(new ArrayList<Long>());

        final String externalKey = "1324";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {12121212L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 12121212, new Integer[] {0}));
        if (dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpLomarkAdnetwork.getRequestUri().toString();
            final HashMap<String, String> requestMap = new HashMap<String, String>();
            String millisec = "";
            final String[] params = actualUrl.split("&");
            for (final String param : params) {
                final String name = param.split("=")[0];
                final String value = param.split("=")[1];

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

            final String expectedUrl =
                    "http://apitest.lomark.cn/v2/get?Format=json&Ip=5.63.24.1&Client=2&SiteType=1&Key=1000&AppId=1324&AdSpaceType=1&Operator=4&Lat=37.4429&Long=-122.1514&Uuid=202cb962ac59075b964b07152d234b70&DeviceType=1&AppName=00000000-00b8-f47c-0000-000000b8f47c&Category=12&Aw=320&Ah=48&SdkVersion=a3.5.2&Timestamp="
                            + millisec + "&Sign=" + getSignature(requestMap, lomarkSecretKey);
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPLomarkRequestUriWithSegmentCategory() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("5.63.24.1");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("app");
        sasParams.setSiteIncId(12121212);
        sasParams.setSdkVersion("a3.5.2");
        sasParams.setOsId(5);

        final String externalKey = "1324";
        final Long[] segmentCategories = new Long[] {11l, 15l};
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, segmentCategories, true, true, externalKey, null, null, null, new Long[] {12121212L},
                        true, null, null, 0, null, false, false, false, false, false, false, false, false, false,
                        false, null, new ArrayList<>(), 0.0d, null, null, 12121212, new Integer[] {0}));
        if (dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpLomarkAdnetwork.getRequestUri().toString();
            final HashMap<String, String> requestMap = new HashMap<String, String>();
            String millisec = "";
            final String[] params = actualUrl.split("&");
            for (final String param : params) {
                final String name = param.split("=")[0];
                final String value = param.split("=")[1];

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

            final String expectedUrl =
                    "http://apitest.lomark.cn/v2/get?Format=json&Ip=5.63.24.1&Client=2&SiteType=1&Key=1000&AppId=1324&AdSpaceType=1&Operator=4&Lat=37.4429&Long=-122.1514&Uuid=202cb962ac59075b964b07152d234b70&DeviceType=1&AppName=00000000-00b8-f47c-0000-000000b8f47c&Category=8&Aw=320&Ah=48&SdkVersion=a3.5.2&Timestamp="
                            + millisec + "&Sign=" + getSignature(requestMap, lomarkSecretKey);
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        }
    }

    @Test
    public void testDCPLomarkRequestUriWithSiteCategorySegmentRON() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("5.63.24.1");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("app");
        sasParams.setSiteIncId(12121212);
        sasParams.setSdkVersion("a3.5.2");
        sasParams.setOsId(5);

        final String externalKey = "1324";
        final Long[] segmentCategories = new Long[] {1l};
        final Long[] cat = new Long[] {99l, 15l};
        sasParams.setCategories(Arrays.asList(cat));
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, segmentCategories, true, true, externalKey, null, null, null, new Long[] {12121212L},
                        true, null, null, 0, null, false, false, false, false, false, false, false, false, false,
                        false, null, new ArrayList<>(), 0.0d, null, null, 12121212, new Integer[] {0}));
        if (dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpLomarkAdnetwork.getRequestUri().toString();
            final HashMap<String, String> requestMap = new HashMap<String, String>();
            String millisec = "";
            final String[] params = actualUrl.split("&");
            for (final String param : params) {
                final String name = param.split("=")[0];
                final String value = param.split("=")[1];

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

            final String expectedUrl =
                    "http://apitest.lomark.cn/v2/get?Format=json&Ip=5.63.24.1&Client=2&SiteType=1&Key=1000&AppId=1324&AdSpaceType=1&Operator=4&Lat=37.4429&Long=-122.1514&Uuid=202cb962ac59075b964b07152d234b70&DeviceType=1&AppName=00000000-00b8-f47c-0000-000000b8f47c&Category=10&Aw=320&Ah=48&SdkVersion=a3.5.2&Timestamp="
                            + millisec + "&Sign=" + getSignature(requestMap, lomarkSecretKey);
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        }
    }

    @Test
    public void testDCPLomarkRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong(",-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("app");
        sasParams.setRemoteHostIp("5.63.24.1");
        sasParams.setSiteIncId(12121212);
        sasParams.setOsId(3);

        final String externalKey = "1324";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {12121212L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 12121212, new Integer[] {0}));
        if (dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpLomarkAdnetwork.getRequestUri().toString();
            final HashMap<String, String> requestMap = new HashMap<String, String>();
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
            final String[] params = actualUrl.split("&");
            for (final String param : params) {
                final String name = param.split("=")[0];
                final String value = param.split("=")[1];

                if (name.equalsIgnoreCase("Timestamp")) {
                    requestMap.put("Timestamp", value);
                    millisec = value;
                }
            }

            final String expectedUrl =
                    "http://apitest.lomark.cn/v2/get?Format=json&Ip=5.63.24.1&Client=1&SiteType=1&Key=1000&AppId=1324&AdSpaceType=1&Operator=4&Uuid=202cb962ac59075b964b07152d234b70&DeviceType=1&AppName=00000000-00b8-f47c-0000-000000b8f47c&Category=12&Aw=320&Ah=50&Timestamp="
                            + millisec + "&Sign=" + getSignature(requestMap, lomarkSecretKey);
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPLomarkParseResponseAdForImageAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "{\"status\":\"100\",\"msg\":\"������\",\"data\":{\"ad\":{\"aid\":\"1\",\"ts\":\"1355799274\",\"sessionid\":\"4d2b62c51e6e4b069e380017168beff3\",\"creative\":{\"cid\":\"1\",\"ts\":\"1355799221\",\"displayinfo\":{\"type\":\"1\",\"img\":\"http://apitest.lomark.cn/upload/201212/201212181053416444os_320_50.jpg\",\"schema\":\"fb98e702541d4f17acd7af0015e0779a\"},\"clkinfos\":[{\"type\":\"1\",\"schema\":\"fb98e702541d4f17acd7af0015e0779a\",\"url\":\"http://www.donson.com.cn\"}],\"trackers\":{\"clicks\":[{\"schema\":\"fb98e702541d4f17acd7af0015e0779a\",\"urls\":[\"http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}\"]}],\"display\":{\"urls\":[\"http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}\"]}}}}}}";
        dcpLomarkAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpLomarkAdnetwork.getHttpResponseStatusCode());
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://www.donson.com.cn' onclick=\"document.getElementById('click').src='clickUrl';document.getElementById('partnerClick').src='http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://apitest.lomark.cn/upload/201212/201212181053416444os_320_50.jpg'  /></a><img src='http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}' height=1 width=1 border=0 style=\"display:none;\"/><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/><img id=\"partnerClick\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpLomarkAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPLomarkParseResponseAdForGifAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "{\"status\":\"100\",\"msg\":\"������\",\"data\":{\"ad\":{\"aid\":\"7\",\"ts\":\"1355801288\",\"sessionid\":\"e13ef4470f1e4f798c17e066ed4f1de1\",\"creative\":{\"cid\":\"9\",\"ts\":\"1355801272\",\"displayinfo\":{\"type\":\"4\",\"img\":\"http://apitest.lomark.cn/upload/201212/201212181127521380l9_320_50.gif\",\"schema\":\"bf9e2a0844e840b3aa8af4ad14b6924a\"},\"clkinfos\":[{\"type\":\"1\",\"schema\":\"bf9e2a0844e840b3aa8af4ad14b6924a\",\"url\":\"http://www.sohu.com\"}],\"trackers\":{\"clicks\":[{\"schema\":\"bf9e2a0844e840b3aa8af4ad14b6924a\",\"urls\":[\"http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}\"]}],\"display\":{\"urls\":[\"http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}\"]}}}}}}";
        dcpLomarkAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpLomarkAdnetwork.getHttpResponseStatusCode());
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://www.sohu.com' onclick=\"document.getElementById('click').src='clickUrl';document.getElementById('partnerClick').src='http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://apitest.lomark.cn/upload/201212/201212181127521380l9_320_50.gif'  /></a><img src='http://apitest.lomark.cn/v2/callback?sessionid={$sessionid$}&key={$key$}&appid={$appid$}&appname={$appname$}&uuid={$uuid$}&client={$client$}&operator={$operator$}&net={$net$}&devicetype={$devicetype$}&adspacetype={$adspacetype$}&category={$category$}&ip={$ip$}&os_version={$os_version$}&aw={$aw$}&ah={$ah$}&timestamp={$timestamp$}&sign={$sign$}&format={$format$}&density={$density$}&long={$long$}&lat={$lat$}&devicenum={$devicenum$}&sdkversion={$sdkversion$}&cid={$cid$}&callbacktype={$callbacktype$}&schema={$schema$}&pw={$pw$}&ph={$ph$}' height=1 width=1 border=0 style=\"display:none;\"/><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/><img id=\"partnerClick\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpLomarkAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPLomarkParseNoAd() throws Exception {
        final String response = "";
        dcpLomarkAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpLomarkAdnetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPLomarkParseEmptyResponseCode() throws Exception {
        final String response = "";
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
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(lomarkAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpLomarkAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpLomarkAdnetwork.getImpressionId());
    }

    @Test
    public void testDCPLomarkGetName() throws Exception {
        assertEquals("lomarkDCP", dcpLomarkAdnetwork.getName());
    }

    private String getSignature(final HashMap<String, String> params, final String secret) throws IOException

    {
        // first sort asc as per the paramter names
        final Map<String, String> sortedParams = new TreeMap<String, String>(params);
        final Set<Entry<String, String>> entrys = sortedParams.entrySet();
        // after sorting���organize all paramters with key=value"format
        final StringBuilder basestring = new StringBuilder();
        for (final Entry<String, String> param : entrys) {
            basestring.append(param.getKey()).append("=").append(param.getValue());
        }
        basestring.append(secret);
        // MD5 Hashed
        final byte[] bytes = DigestUtils.md5(basestring.toString().getBytes());
        final StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            final String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }
}
