package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.inmobi.adserve.channels.adnetworks.collectcents.DCPCollectcentsAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.ning.http.client.RequestBuilder;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPCollectcentsAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String collectcentsHost = "http://ad.ipredictive.com/d/ads";
    private static final String collectcentsStatus = "on";
    private static final String collectcentsPubId = "0344343";
    private static final String collectcentsAdvId = "collectcentsadv1";
    private static final String collectcentsTest = "1";
    private static Configuration mockConfig = null;
    private static DCPCollectcentsAdnetwork dcpCollectcentsAdNetwork;
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("collectcents.host")).andReturn(
                collectcentsHost).anyTimes();
        expect(mockConfig.getString("collectcents.status")).andReturn(
                collectcentsStatus).anyTimes();
        expect(mockConfig.getString("collectcents.test")).andReturn(
                collectcentsTest).anyTimes();
        expect(mockConfig.getString("collectcents.advertiserId")).andReturn(
                collectcentsAdvId).anyTimes();
        expect(mockConfig.getString("collectcents.pubid")).andReturn(
                collectcentsPubId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn(
                "/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn(
                "/opt/mkhoj/conf/cas/channel-server.properties");
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
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension())
                .andReturn(new Dimension(300, 50)).anyTimes();
        expect(slotSizeMapEntityFor4.getSlotId()).andReturn((short) 4)
                .anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension())
                .andReturn(new Dimension(320, 48)).anyTimes();
        expect(slotSizeMapEntityFor9.getSlotId()).andReturn((short) 9)
                .anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor10.getDimension())
                .andReturn(new Dimension(300, 250)).anyTimes();
        expect(slotSizeMapEntityFor10.getSlotId())
                .andReturn((short) 10).anyTimes();
        replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension())
                .andReturn(new Dimension(728, 90)).anyTimes();
        expect(slotSizeMapEntityFor11.getSlotId())
                .andReturn((short) 11).anyTimes();
        replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor12 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor12.getDimension())
                .andReturn(new Dimension(468, 60)).anyTimes();
        expect(slotSizeMapEntityFor12.getSlotId())
                .andReturn((short) 12).anyTimes();
        replay(slotSizeMapEntityFor12);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension())
                .andReturn(new Dimension(320, 480)).anyTimes();
        expect(slotSizeMapEntityFor14.getSlotId())
                .andReturn((short) 14).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension())
                .andReturn(new Dimension(320, 50)).anyTimes();
        expect(slotSizeMapEntityFor15.getSlotId())
                .andReturn((short) 15).anyTimes();
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
        dcpCollectcentsAdNetwork = new DCPCollectcentsAdnetwork(mockConfig,
                null, base, serverChannel);
    }

	/* test */

    @Test
    public void testDCPcollectcentsConfigureParameters() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(
                        collectcentsAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null,
                        new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false,
                        false, new JSONObject(
                                "{\"spot\":54235,\"pubId\":\"inmobi_1\","
                                        + "\"site\":1234}"),
                        new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertEquals(true, dcpCollectcentsAdNetwork.configureParameters(
                sasParams, casInternalRequestParameters, entity,
                (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPcollectcentsConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(
                        collectcentsAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null,
                        new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false,
                        false, null, new ArrayList<>(), 0.0d, null,
                        null, 32, new Integer[] {0}));
        assertEquals(false, dcpCollectcentsAdNetwork.configureParameters(
                sasParams, casInternalRequestParameters, entity,
                (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPcollectcentsConfigureParametersAdditionalParams() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(
                        collectcentsAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null,
                        new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false,
                        false, null, new ArrayList<>(), 0.0d, null,
                        null, 32, new Integer[] {0}));
        assertEquals(false, dcpCollectcentsAdNetwork.configureParameters(
                sasParams, casInternalRequestParameters, entity,
                (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPcollectcentsConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(
                        collectcentsAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null,
                        new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false,
                        false, null, new ArrayList<>(), 0.0d, null,
                        null, 32, new Integer[] {0}));
        assertEquals(false, dcpCollectcentsAdNetwork.configureParameters(
                sasParams, casInternalRequestParameters, entity, Short.MAX_VALUE, repositoryHelper));
    }

    @Test
    public void testDCPcollectcentsRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays
                .asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final String externalKey = "0344343";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest
                        .getChannelSegmentEntityBuilder(
                                collectcentsAdvId,
                                null,
                                null,
                                null,
                                0,
                                null,
                                null,
                                true,
                                true,
                                externalKey,
                                null,
                                null,
                                null,
                                new Long[] {0L},
                                true,
                                null,
                                null,
                                0,
                                null,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                new JSONObject(
                                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                                new ArrayList<>(), 0.0d, null, null, 0,
                                new Integer[] {0}));

        if (dcpCollectcentsAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, (short) 15,
                repositoryHelper)) {
            final String actualUrl = dcpCollectcentsAdNetwork.getRequestUri()
                    .toString();
            final String expectedUrl = "http://ad.ipredictive.com/d/ads";

            assertEquals(expectedUrl, actualUrl);
            RequestBuilder requestBuilder = Whitebox.<RequestBuilder>invokeMethod(
                    dcpCollectcentsAdNetwork, "getNingRequestBuilder");
            String actualData = requestBuilder.build().getStringData();
            String expectedData =
                    "{\"responseformat\":\"HTML\",\"main\":[{\"pubid\":\"0344343\",\"ads\":1,\"adtype\":\"banner\",\"response\":\"HTML\",\"banner\":{\"adsize\":15}}],\"site\":{\"rated\":\"A\",\"category\":\"Business\",\"id\":\"00000000-0000-0000-0000-000000000000\"},\"device\":{\"ip\":\"206.29.182.240\",\"deviceid\":\"202cb962ac59075b964b07152d234b70\",\"ua\":\"Mozilla\",\"geo\":{\"geolat\":\"37.4429\",\"geolong\":\"-122.1514\"},\"type\":\"app\"},\"user\":{}}";
            assertEquals(actualData, expectedData);
        }
    }

    @Test
    public void testDCPcollectcentsRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("WAP");
        casInternalRequestParameters.setBlockedIabCategories(Arrays
                .asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        casInternalRequestParameters.setLatLong("38.5,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        category.add(2l);
        sasParams.setCategories(category);

        final String externalKey = "01212121";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest
                        .getChannelSegmentEntityBuilder(
                                collectcentsAdvId,
                                null,
                                null,
                                null,
                                0,
                                null,
                                null,
                                true,
                                true,
                                externalKey,
                                null,
                                null,
                                null,
                                new Long[] {0L},
                                true,
                                null,
                                null,
                                0,
                                null,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                new JSONObject(
                                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                                new ArrayList<>(), 0.0d, null, null, 0,
                                new Integer[] {0}));
        if (dcpCollectcentsAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, (short) 15,
                repositoryHelper)) {
            final String actualUrl = dcpCollectcentsAdNetwork.getRequestUri()
                    .toString();
            System.out.println(actualUrl);
            final String expectedUrl = "http://ad.ipredictive.com/d/ads";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPcollectcentsParseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays
                .asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";

        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest
                        .getChannelSegmentEntityBuilder(
                                collectcentsAdvId,
                                null,
                                null,
                                null,
                                0,
                                null,
                                null,
                                true,
                                true,
                                externalKey,
                                null,
                                null,
                                null,
                                new Long[] {0L},
                                true,
                                null,
                                null,
                                0,
                                null,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                new JSONObject(
                                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                                new ArrayList<>(), 0.0d, null, null, 32,
                                new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpCollectcentsAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity,
                (short) 15, repositoryHelper);
        final String response =
                "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dcollectcents_int%26c%3Dcollectcents%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fcollectcents%2Fcollectcents5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpCollectcentsAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpCollectcentsAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dcollectcents_int%26c%3Dcollectcents%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fcollectcents%2Fcollectcents5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpCollectcentsAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPcollectcentsParseAppAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays
                .asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        final String externalKey = "19100";

        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest
                        .getChannelSegmentEntityBuilder(
                                collectcentsAdvId,
                                null,
                                null,
                                null,
                                0,
                                null,
                                null,
                                true,
                                true,
                                externalKey,
                                null,
                                null,
                                null,
                                new Long[] {0L},
                                true,
                                null,
                                null,
                                0,
                                null,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                new JSONObject(
                                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                                new ArrayList<>(), 0.0d, null, null, 32,
                                new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpCollectcentsAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity,
                (short) 15, repositoryHelper);
        final String response =
                "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dcollectcents_int%26c%3Dcollectcents%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fcollectcents%2Fcollectcents5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpCollectcentsAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpCollectcentsAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dcollectcents_int%26c%3Dcollectcents%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fcollectcents%2Fcollectcents5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpCollectcentsAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPcollectcentsParseNoAd() throws Exception {
        final String response = "<!-- Collectcent: No advt for this position -->";
        dcpCollectcentsAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpCollectcentsAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPcollectcentsParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpCollectcentsAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpCollectcentsAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpCollectcentsAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPcollectcentsGetId() throws Exception {
        assertEquals(collectcentsAdvId, dcpCollectcentsAdNetwork.getId());
    }

    @Test
    public void testDCPcollectcentsGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest
                        .getChannelSegmentEntityBuilder(
                                collectcentsAdvId,
                                null,
                                null,
                                null,
                                0,
                                null,
                                null,
                                true,
                                true,
                                externalKey,
                                null,
                                null,
                                null,
                                new Long[] {0L},
                                true,
                                null,
                                null,
                                0,
                                null,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                false,
                                new JSONObject(
                                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                                new ArrayList<>(), 0.0d, null, null, 32,
                                new Integer[] {0}));
        dcpCollectcentsAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9",
                dcpCollectcentsAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPcollectcentsGetName() throws Exception {
        assertEquals("collectcentsDCP", dcpCollectcentsAdNetwork.getName());
    }

}
