package com.inmobi.adserve.channels.adnetworks;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.lang.reflect.Field;
import java.net.URI;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.adnetworks.paypal.DCPPayPalAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

/**
 * Created by thushara on 24/12/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPPaypalAdNetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String paypalHost = "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile";
    private static final String paypalStatus = "on";
    private static final String paypalAdvId = "paypaladv1";
    private static final String paypalTest = "test";
    private static final String paypalFormat = "html";
    private static Configuration mockConfig = null;
    private static DCPPayPalAdNetwork dcpPaypalAdnetwork;
    private static RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("paypal.host")).andReturn(paypalHost).anyTimes();
        expect(mockConfig.getString("paypal.status")).andReturn(paypalStatus).anyTimes();
        expect(mockConfig.getString("paypal.test")).andReturn(paypalTest).anyTimes();
        expect(mockConfig.getString("paypal.advertiserId")).andReturn(paypalAdvId).anyTimes();
        expect(mockConfig.getString("paypal.format")).andReturn(paypalFormat).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Before
    public void setUp() throws Exception {
        java.io.File f;
        f = new java.io.File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final io.netty.channel.Channel serverChannel = createMock(io.netty.channel.Channel.class);
        final HttpRequestHandlerBase
                base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        com.inmobi.adserve.channels.api.Formatter.init();
        final SlotSizeMapEntity
                slotSizeMapEntityFor1 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor1.getDimension()).andReturn(new java.awt.Dimension(120, 20)).anyTimes();
        replay(slotSizeMapEntityFor1);
        final SlotSizeMapEntity
                slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new java.awt.Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity
                slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new java.awt.Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity
                slotSizeMapEntityFor10 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor10.getDimension()).andReturn(new java.awt.Dimension(300, 250)).anyTimes();
        replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity
                slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new java.awt.Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity
                slotSizeMapEntityFor12 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor12.getDimension()).andReturn(new java.awt.Dimension(468, 60)).anyTimes();
        replay(slotSizeMapEntityFor12);
        final SlotSizeMapEntity
                slotSizeMapEntityFor14 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new java.awt.Dimension(320, 480)).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity
                slotSizeMapEntityFor15 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new java.awt.Dimension(320, 50)).anyTimes();
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
        dcpPaypalAdnetwork = new DCPPayPalAdNetwork(mockConfig, null, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpPaypalAdnetwork.setHost(paypalHost);
    }

    @Test
    public void testDCPPayPalConfigureParameters() throws org.json.JSONException {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPPayPalConfigureParametersWithNoUid() throws org.json.JSONException {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPPayPalConfigureParametersBlankIP() {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new java.util.ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testPaypalConfigureParametersBlankExtKey() {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new java.util.ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPPayPalConfigureParametersBlankUA() {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new java.util.ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }


    @Test
    public void testDCPPayPalRequestUri() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final java.util.List<Long> cat = new java.util.ArrayList<Long>();
        cat.add(46l);
        sasParams.setSource("APP");
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&lat=37.4429&lng=-122.1514&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }


    @Test
    public void testDCPPayPalRequestUriWithBlindedSiteId() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final java.util.List<Long> cat = new java.util.ArrayList<Long>();
        cat.add(48l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        sasParams.setSiteIncId(123456);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {56789L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new org.json.JSONObject("{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 56789,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-ddd5-0000-00000001e240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&lat=37.4429&lng=-122.1514&cat=Health%2C+Mind+%26+Body";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPPayPalRequestUriWithZip() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final java.util.List<Long> cat = new java.util.ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        casInternalRequestParameters.setZipCode("123456");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&lat=37.4429&lng=-122.1514&zip=123456&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPPayPalRequestUriWithGender() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final java.util.List<Long> cat = new java.util.ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setGender("m");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&gender=m&lat=37.4429&lng=-122.1514&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPPayPalRequestUriWithAge() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final java.util.List<Long> cat = new java.util.ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setAge(Short.valueOf("30"));
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&age=30&lat=37.4429&lng=-122.1514&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPPayPalRequestUriBlankLatLong() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final java.util.List<Long> cat = new java.util.ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x50&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPPayPalParseResponseAd() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("APP");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPayPalParseResponseAdWAP() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("wap");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPayPalParseResponseAdApp() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus "
                + "Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534" + ".30");
        sasParams.setSource("app");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPayPalParseResponseAdAppIMAISDK450Interstitals() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a450");
        sasParams.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><script type=\"text/javascript\">var readyHandler=function(){_im_imai.fireAdReady();_im_imai.removeEventListener('ready',readyHandler);};_im_imai.addEventListener('ready',readyHandler);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPayPalParseResponseAdAppIMAI() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");

        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPayPalParseNoAd() throws Exception {
        final String response = "";
        dcpPaypalAdnetwork.parseResponse(response, OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPPayPalParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpPaypalAdnetwork.parseResponse(response, OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(org.apache.commons.lang.StringUtils.isBlank(dcpPaypalAdnetwork.getHttpResponseContent()), true);
    }

    @Test
    public void testDCPPayPalGetId() throws Exception {
        assertEquals(dcpPaypalAdnetwork.getId(), "paypaladv1");
    }

    @Test
    public void testDCPPayPalGetImpressionId() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5" + ".0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        assertEquals(dcpPaypalAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPPayPalGetName() throws Exception {
        assertEquals(dcpPaypalAdnetwork.getName(), "paypalDCP");
    }

}
