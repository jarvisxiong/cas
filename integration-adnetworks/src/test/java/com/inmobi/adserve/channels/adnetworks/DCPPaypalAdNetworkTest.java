package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.lang.reflect.Field;
import java.net.URI;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.IPRepository;

/**
 * Created by thushara on 24/12/14.
 */
public class DCPPaypalAdNetworkTest extends junit.framework.TestCase {
    private org.apache.commons.configuration.Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";

    private com.inmobi.adserve.channels.adnetworks.paypal.DCPPayPalAdNetwork dcpPaypalAdnetwork;
    private final String paypalHost = "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile";
    private final String paypalStatus = "on";
    private final String paypalAdvId = "paypaladv1";
    private final String paypalTest = "test";
    private final String paypalFormat = "html";
    private com.inmobi.adserve.channels.repository.RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(org.apache.commons.configuration.Configuration.class);
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

    @Override
    public void setUp() throws Exception {
        java.io.File f;
        f = new java.io.File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final io.netty.channel.Channel serverChannel = createMock(io.netty.channel.Channel.class);
        final com.inmobi.adserve.channels.api.HttpRequestHandlerBase
                base = createMock(com.inmobi.adserve.channels.api.HttpRequestHandlerBase.class);
        prepareMockConfig();
        com.inmobi.adserve.channels.api.Formatter.init();
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor1 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor1.getDimension()).andReturn(new java.awt.Dimension(120, 20)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor1);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor4 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new java.awt.Dimension(300, 50)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor4);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor9 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new java.awt.Dimension(320, 48)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor9);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor10 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor10.getDimension()).andReturn(new java.awt.Dimension(300, 250)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor10);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor11 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new java.awt.Dimension(728, 90)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor11);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor12 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor12.getDimension()).andReturn(new java.awt.Dimension(468, 60)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor12);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor14 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new java.awt.Dimension(320, 480)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor14);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor15 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new java.awt.Dimension(320, 50)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.repository.RepositoryHelper.class);
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 12))
                .andReturn(slotSizeMapEntityFor12).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        org.easymock.EasyMock.replay(repositoryHelper);
        dcpPaypalAdnetwork = new com.inmobi.adserve.channels.adnetworks.paypal.DCPPayPalAdNetwork(mockConfig, null, base, serverChannel);
        
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        
        dcpPaypalAdnetwork.setHost(paypalHost);
    }

    @org.testng.annotations.Test
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
        final String burl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 9, repositoryHelper));
    }

    @org.testng.annotations.Test
    public void testDCPPayPalConfigureParametersWithNoUid() throws org.json.JSONException {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String burl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 9, repositoryHelper));
    }

    @org.testng.annotations.Test
    public void testDCPPayPalConfigureParametersBlankIP() {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String burl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new java.util.ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 15, repositoryHelper));
    }

    @org.testng.annotations.Test
    public void testPaypalConfigureParametersBlankExtKey() {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String burl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new java.util.ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 15, repositoryHelper));
    }

    @org.testng.annotations.Test
    public void testDCPPayPalConfigureParametersBlankUA() {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String burl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new java.util.ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 15, repositoryHelper));
    }


    @org.testng.annotations.Test
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
        final String burl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&lat=37.4429&lng=-122.1514&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }


    @org.testng.annotations.Test
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
        final String burl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {56789L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new org.json.JSONObject("{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 56789,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-ddd5-0000-00000001e240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&lat=37.4429&lng=-122.1514&cat=Health%2C+Mind+%26+Body";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @org.testng.annotations.Test
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
        final String burl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&lat=37.4429&lng=-122.1514&zip=123456&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @org.testng.annotations.Test
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
        final String burl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&gender=m&lat=37.4429&lng=-122.1514&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @org.testng.annotations.Test
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
        final String burl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, burl, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x48&age=30&lat=37.4429&lng=-122.1514&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @org.testng.annotations.Test
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
        final String blurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, blurl, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpPaypalAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.where.com/jin/spotlight/ads?v=2.4&channel=mobile&format=html&ip=206.29.182.240&pubid=8a809449013c3c643cad82cb412b5857&site=00000000-0000-0000-0000-000000000000&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&width=320&placementtype=320x50&cat=Entertainment";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @org.testng.annotations.Test
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
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, io.netty.handler.codec.http.HttpResponseStatus.OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @org.testng.annotations.Test
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
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, io.netty.handler.codec.http.HttpResponseStatus.OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @org.testng.annotations.Test
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
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, io.netty.handler.codec.http.HttpResponseStatus.OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @org.testng.annotations.Test
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
        sasParams.setRqAdType("int");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, io.netty.handler.codec.http.HttpResponseStatus.OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><script type=\"text/javascript\">var readyHandler=function(){_im_imai.fireAdReady();_im_imai.removeEventListener('ready',readyHandler);};_im_imai.addEventListener('ready',readyHandler);</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @org.testng.annotations.Test
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
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div>";
        dcpPaypalAdnetwork.parseResponse(response, io.netty.handler.codec.http.HttpResponseStatus.OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><!-- AdPlacement : header --><img src=\"http://bos.ads.paypal.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.paypal.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=paypal&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.paypal.com/testads/300x50-paypal-Test-Adv2.gif\" /></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPaypalAdnetwork.getHttpResponseContent());
    }

    @org.testng.annotations.Test
    public void testDCPPayPalParseNoAd() throws Exception {
        final String response = "";
        dcpPaypalAdnetwork.parseResponse(response, io.netty.handler.codec.http.HttpResponseStatus.OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @org.testng.annotations.Test
    public void testDCPPayPalParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpPaypalAdnetwork.parseResponse(response, io.netty.handler.codec.http.HttpResponseStatus.OK);
        assertEquals(dcpPaypalAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(org.apache.commons.lang.StringUtils.isBlank(dcpPaypalAdnetwork.getHttpResponseContent()), true);
    }

    @org.testng.annotations.Test
    public void testDCPPayPalGetId() throws Exception {
        assertEquals(dcpPaypalAdnetwork.getId(), "paypaladv1");
    }

    @org.testng.annotations.Test
    public void testDCPPayPalGetImpressionId() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5" + ".0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String blurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&event=beacon";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(paypalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"pos\":\"header\"}"), new java.util.ArrayList<Integer>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        dcpPaypalAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, blurl, (short) 15, repositoryHelper);
        assertEquals(dcpPaypalAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @org.testng.annotations.Test
    public void testDCPPayPalGetName() throws Exception {
        assertEquals(dcpPaypalAdnetwork.getName(), "paypalDCP");
    }

    @org.testng.annotations.Test
    public void testDCPPayPalIsClickUrlReq() throws Exception {
        assertEquals(dcpPaypalAdnetwork.isClickUrlRequired(), false);
    }

    @org.testng.annotations.Test
    public void testDCPPayPalIsBeaconUrlReq() throws Exception {
        assertEquals(dcpPaypalAdnetwork.isBeaconUrlRequired(), true);
    }
}
