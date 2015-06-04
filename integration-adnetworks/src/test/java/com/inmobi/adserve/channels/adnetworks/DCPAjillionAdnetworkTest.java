package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.ajillion.DCPAjillionAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPAjillionAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final Bootstrap clientBootstrap = null;
    private static final String AjilionStatus = "on";
    private static final String defintiAdvId = "Ajilionadv1";
    private static final String AjilionTest = "1";
    private static final String placementId = "240";
    private static final String fsPlacementId = "230";
    private static Configuration mockConfig = null;
    private static DCPAjillionAdnetwork dcpAjillionAdNetwork;
    private static String AjilionHost = "http://ad.AjillionMAX.com/ad/%s/4";
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
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

    @BeforeClass
    public static void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        Formatter.init();
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        replay(slotSizeMapEntityFor15);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        replay(repositoryHelper);

        dcpAjillionAdNetwork = new DCPAjillionAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpAjillionAdNetwork.setName("Ajilion");

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        AjilionHost = String.format(AjilionHost, placementId);
        dcpAjillionAdNetwork.setHost(AjilionHost);
    }

    @Test
    public void testDCPAjilionConfigureParameters() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAjilionConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAjilionConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAjilionConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testAjilionRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "240";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper)) {
            final String actualUrl = dcpAjillionAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.AjillionMAX.com/ad/240/4?format=json&use_beacon=1&keyword=Food+%26+Drink%2CAdventure%2CWord&pubid=00000000-0000-0000-0000-000000000000&clientip=206.29.182.240&clientua=Mozilla";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testAjilionRequestUriFamilySafe() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteContentType(ContentType.FAMILY_SAFE);
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "240";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper)) {
            final String actualUrl = dcpAjillionAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ad.AjillionMAX.com/ad/230/4?format=json&use_beacon=1&keyword=Food+%26+Drink%2CAdventure%2CWord&pubid=00000000-0000-0000-0000-000000000000&clientip=206.29.182.240&clientua=Mozilla";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPAjilionParseResponseImg() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "19100";
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);
        final String response =
                "{\"click_url\": \"http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516\", \"pmodel\": 3, \"price\": 0.005, \"placement_id\": 240, \"height\": 50, \"creative_display\": \"static\", \"creative_url\": \"http://mead-production.s3.amazonaws.com/advertiser_creative/2013/05/02/300x50_dog_1.jpg\", \"success\": true, \"creative_type\": \"image\", \"width\": 300, \"error\": \"\"}";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516' onclick=\"document.getElementById('click').src='clickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://mead-production.s3.amazonaws.com/advertiser_creative/2013/05/02/300x50_dog_1.jpg'  /></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpAjillionAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAjilionParseResponseImgWithBeacon() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "19100";
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);
        final String response =
                "{\"click_url\": \"http://ad.ajillionmax.com/traffic/226044fbef96400fa80ba238a665122d/4/424/13075/1/a21fec1952f04fb0a2e7e225b941216b-198/?keyword=Media+%26+Video&cid=131230&pubid=00000000-000b-b7fd-0000-00000002022a&csize=320x50&use_beacon=1&clientua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+es-us%3B+ALCATEL+ONE+TOUCH+5020A+Build%2FJRO03C%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.1+Mobile+Safari%2F534.30%5E&clientip=189.186.213.199\", \"pmodel\": 2, \"price\": 0.003, \"placement_id\": 424, \"height\": 50, \"rate\": 0.0012414800389483934, \"creative_display\": \"static\", \"creative_url\": \"http://mead-production.s3.amazonaws.com/advertiser_creative/2014/01/26/078bd4e7-6572-44b8-b583-5dd8639fe89a.gif\", \"success\": true, \"creative_type\": \"image\", \"content_identifier\": \"\", \"width\": 320, \"error\": \"\", \"beacon_url\": \"http://ad.ajillionmax.com/ad/beacon/a21fec1952f04fb0a2e7e225b941216b-198/11826/0.000000/\"}";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://ad.ajillionmax.com/traffic/226044fbef96400fa80ba238a665122d/4/424/13075/1/a21fec1952f04fb0a2e7e225b941216b-198/?keyword=Media+%26+Video&cid=131230&pubid=00000000-000b-b7fd-0000-00000002022a&csize=320x50&use_beacon=1&clientua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.1.1%3B+es-us%3B+ALCATEL+ONE+TOUCH+5020A+Build%2FJRO03C%29+AppleWebKit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.1+Mobile+Safari%2F534.30%5E&clientip=189.186.213.199' onclick=\"document.getElementById('click').src='clickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://mead-production.s3.amazonaws.com/advertiser_creative/2014/01/26/078bd4e7-6572-44b8-b583-5dd8639fe89a.gif'  /></a><img src='http://ad.ajillionmax.com/ad/beacon/a21fec1952f04fb0a2e7e225b941216b-198/11826/0.000000/' height=1 width=1 border=0 style=\"display:none;\"/><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpAjillionAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAjilionParseResponseImgAppSDK360() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);
        final String response =
                "{\"click_url\": \"http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516\", \"pmodel\": 3, \"price\": 0.005, \"placement_id\": 240, \"height\": 50, \"creative_display\": \"static\", \"creative_url\": \"http://mead-production.s3.amazonaws.com/advertiser_creative/2013/05/02/300x50_dog_1.jpg\", \"success\": true, \"creative_type\": \"image\", \"width\": 300, \"error\": \"\"}";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516' onclick=\"document.getElementById('click').src='clickUrl';mraid.openExternal('http://ad.Ajilionmedia.com/traffic/f446a48aa6b011e2aa3a06311cd1216a/4/240/1541/1/8d35d80adcbc11e2b8aa06311cc966ad/?clientip=206.29.182.240&clientua=Mozilla&csize=300X50&keyword=Food+%26+Drink%2CAdventure%2CWord&cid=1516'); return false;\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://mead-production.s3.amazonaws.com/advertiser_creative/2013/05/02/300x50_dog_1.jpg'  /></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpAjillionAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAjilionParseNoAd() throws Exception {
        final String response = "{\"success\":\"false\"}";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAjilionParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpAjillionAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAjillionAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAjilionGetId() throws Exception {
        assertEquals(dcpAjillionAdNetwork.getId(), "Ajilionadv1");
    }

    @Test
    public void testDCPAjilionGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpAjillionAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);
        assertEquals(dcpAjillionAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPAjilionGetName() throws Exception {
        assertEquals(dcpAjillionAdNetwork.getName(), "AjilionDCP");
    }

}
