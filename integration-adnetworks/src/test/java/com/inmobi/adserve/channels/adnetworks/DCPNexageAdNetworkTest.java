package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.adnetworks.nexage.DCPNexageAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPNexageAdNetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String NexageHost = "http://bos.ads.nexage.com/adServe?";
    private static final String NexageStatus = "on";
    private static final String NexageAdvId = "nexageadv1";
    private static final String NexageTest = "test";
    private static Configuration mockConfig = null;
    private static DCPNexageAdNetwork dcpNexageAdnetwork;
    private static RepositoryHelper repositoryHelper;

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

    @Before
    public void setUp() throws Exception {
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
        dcpNexageAdnetwork = new DCPNexageAdNetwork(mockConfig, null, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpNexageAdnetwork.setHost(NexageHost);
    }

    @Test
    public void testDCPNexageConfigureParameters() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPNexageConfigureParametersWithNoUid() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPNexageConfigureParametersWithAdditionalParamsNotSet() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{}"), new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
    }

    @Test
    public void testDCPNexageConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testNexageConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPNexageConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPNexageRequestUriWithSegmentCategory() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final Long[] categories = new Long[] {13l, 15l};
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, categories, true, true, externalKey, null, null, null, new Long[] {0L}, false, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject(new JSONObject("{\"pos\":\"header\"}")), new ArrayList<>(), 0.0d, null,
                        null, 32, new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&req(loc)=37.4429%2C-122.1514&cn=Adventure&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithRONSegmentSiteCategory() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 26l}));
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB9-30&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setSource("APP");
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&d(id12)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriforiPad() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(63l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"leader\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=leader&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28iPad%3B+U%3B+CPU+OS+3_2+like+Mac+OS+X%3B+en-us%29+AppleWebKit%2F531.21.10+%28KHTML%2C+like+Gecko%29+Version%2F4.0.4+Mobile%2F7B334b+Safari%2F531.21.10&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB20&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithBlindedSiteId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(48l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        sasParams.setSiteIncId(123456);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {56789L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 56789,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB7&p(blind_id)=00000000-0000-ddd5-0000-00000001e240";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithDoubleEncodedUserAgent() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("NokiaC3-00%252F5.0%2B%252808.65%2529%2BProfile%252FMIDP-2.1%2BConfiguration%252FCLDC-1.1%2BMozilla%252F5.0%2BAppleWebKit%252F420%252B%2B%2528KHTML%252C%2Blike%2BGecko%2529%2BSafari%252F420%252B");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=NokiaC3-00%2F5.0+%2808.65%29+Profile%2FMIDP-2.1+Configuration%2FCLDC-1.1+Mozilla%2F5.0+AppleWebKit%2F420++%28KHTML%2C+like+Gecko%29+Safari%2F420&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithEncodedUserAgent() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithCountry() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setCountryCode("US");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000&u(country)=USA";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutCountry() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithDMA() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setState(1);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000&u(dma)=1";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutDMA() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithZip() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        casInternalRequestParameters.setZipCode("123456");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&req(zip)=123456&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutZip() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithGender() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setGender("m");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&u(gender)=m&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutGender() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithAge() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setAge(Short.valueOf("30"));
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&u(age)=30&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriWithoutAge() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriSitePerformance() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=p&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriSiteFamilySafe() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        sasParams.setSiteContentType(ContentType.FAMILY_SAFE);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x50&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageRequestUriBlankSlot() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidIFA("TEST_IFA");
        casInternalRequestParameters.setTrackingAllowed(true);
        final List<Long> cat = new ArrayList<Long>();
        cat.add(46l);
        sasParams.setCategories(cat);
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpNexageAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://bos.ads.nexage.com/adServe?pos=header&p(size)=320x48&mode=test&dcn=8a809449013c3c643cad82cb412b5857&ip=206.29.182.240&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+en-us%3B+Galaxy+Nexus+Build%2FJRO03O%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&p(site)=fs&u(id)=202cb962ac59075b964b07152d234b70&d(id24)=TEST_IFA&req(loc)=37.4429%2C-122.1514&cn=IAB10-2&p(blind_id)=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPNexageParseResponseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("APP");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div>";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageParseResponseAdWAP() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("wap");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div>";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageParseResponseAdApp() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus "
                + "Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534" + ".30");
        sasParams.setSource("app");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div>";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageParseResponseAdAppIMAI() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        final String response =
                "<!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div>";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><!-- AdPlacement : header --><img src=\"http://bos.ads.nexage.com:80/admax/adEvent.do?dcn=8a809449013c3c643cad82cb412b5857&amp;pos=header&amp;nl=1359535663796&amp;pix=1&amp;et=1&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;mode=test&amp;xd=R2FsYXh5IE5leHVzfFNhbXN1bmd8NC4xLjF8QW5kcm9pZA..&amp;xo=V0lGSXxVU0E.\" style=\"display:none;width:1px;height:1px;border:0;\" width=\"1\" height=\"1\" alt=\"\" /><div> <a href=\"http://bos.ads.nexage.com:80/admax/adClick.do?dcn=8a809449013c3c643cad82cb412b5857&amp;n=Nexage&amp;id=8a80941f013c3c64abf38aa3eab36ceb&amp;tid=8a808aee3c264c09013c82e8b49e0205&amp;nid=8a808aee32b23b0e013311fe47710e86&amp;pos=header&amp;mode=test&amp;nl=1359535663795\"> <img src=\"http://files.nexage.com/testads/300x50-Nexage-Test-Adv2.gif\" /></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageGenerateStaticJsAdTag() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\",\"jsAdTag\":\"true\"}}"), new ArrayList<>(), 0.0d, null,
                        null, 0, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        dcpNexageAdnetwork.generateJsAdResponse();
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script> var suid = getSuid(); var admax_vars = { dcn: \"19100\",pos: \"header\",\"p(blind_id)\": \"00000000-0000-0000-0000-000000000000\",cn: \"IAB9-30\"};if (suid) admax_vars[\"u(id)\"]=suid;admaxAd(admax_vars);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageGenerateStaticJsAdTagSDK450Interstitial() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a450");
        sasParams.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\",\"jsAdTag\":\"true\"}}"), new ArrayList<>(), 0.0d, null,
                        null, 0, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        dcpNexageAdnetwork.generateJsAdResponse();
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var readyHandler=function(){_im_imai.fireAdReady();_im_imai.removeEventListener('ready',readyHandler);};_im_imai.addEventListener('ready',readyHandler); var suid = getSuid(); var admax_vars = { dcn: \"19100\",pos: \"header\",\"p(blind_id)\": \"00000000-0000-0000-0000-000000000000\",cn: \"IAB9-30\"};if (suid) admax_vars[\"u(id)\"]=suid;admaxAd(admax_vars);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageGenerateStaticJsAdTagMultipleCategories() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-us; Galaxy Nexus Build/JRO03O) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(3l);
        sasParams.setCategories(cat);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\",\"jsAdTag\":\"true\"}}"), new ArrayList<>(), 0.0d, null,
                        null, 0, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);

        dcpNexageAdnetwork.generateJsAdResponse();
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script> var suid = getSuid(); var admax_vars = { dcn: \"19100\",pos: \"header\",\"p(blind_id)\": \"00000000-0000-0000-0000-000000000000\",cn: \"IAB4\"};if (suid) admax_vars[\"u(id)\"]=suid;admaxAd(admax_vars);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpNexageAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPNexageParseNoAd() throws Exception {
        final String response = "";
        dcpNexageAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpNexageAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPNexageParseEmptyResponseCode() throws Exception {
        final String response = "";
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
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5" + ".0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        final List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(NexageAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        dcpNexageAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        assertEquals(dcpNexageAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPNexageGetName() throws Exception {
        assertEquals(dcpNexageAdnetwork.getName(), "nexageDCP");
    }

}
