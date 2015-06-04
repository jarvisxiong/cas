package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.inmobi.adserve.channels.adnetworks.mable.DCPMableAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.ning.http.client.RequestBuilder;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPMableAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String mableHost = "http://ad.ipredictive.com/d/ads";
    private static final String mableStatus = "on";
    private static final String mableAdvId = "mableadv1";
    private static final String mableTest = "1";
    private static final String mableAuthKey = "335eaf2639079ffa40b5f7d69f3051fb";
    private static Configuration mockConfig = null;
    private static DCPMableAdnetwork dcpMableAdNetwork;
    private static RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("mable.host")).andReturn(mableHost).anyTimes();
        expect(mockConfig.getString("mable.status")).andReturn(mableStatus).anyTimes();
        expect(mockConfig.getString("mable.test")).andReturn(mableTest).anyTimes();
        expect(mockConfig.getString("mable.advertiserId")).andReturn(mableAdvId).anyTimes();
        expect(mockConfig.getString("mable.authKey")).andReturn(mableAuthKey).anyTimes();
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
        dcpMableAdNetwork = new DCPMableAdnetwork(mockConfig, null, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpMableAdNetwork.setHost(mableHost);
    }

    @Test
    public void testDCPMableConfigureParameters() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":54235,\"pubId\":\"inmobi_1\"," + "\"site\":1234}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPMableConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPMableConfigureParametersAdditionalParams() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPMableConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, Short.MAX_VALUE, repositoryHelper));
    }

    @Test
    public void testDCPMableRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
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
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));

        if (dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpMableAdNetwork.getRequestUri().toString();
            final String expectedUrl = "http://ad.ipredictive.com/d/ads";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPMableRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("WAP");
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        casInternalRequestParameters.setLatLong("38.5,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        category.add(2l);
        sasParams.setCategories(category);

        final String externalKey = "01212121";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpMableAdNetwork.getRequestUri().toString();
            System.out.println(actualUrl);
            final String expectedUrl = "http://ad.ipredictive.com/d/ads";
            assertEquals(expectedUrl, actualUrl);
            RequestBuilder requestBuilder =
                    Whitebox.<RequestBuilder>invokeMethod(dcpMableAdNetwork, "getNingRequestBuilder");
            String actualData = requestBuilder.build().getStringData();
            String expectedData =
                    "{\"did_format\":\"UDID\",\"slot_size\":\"320x50\",\"client_agent\":\"Mozilla\",\"device_id\":\"202cb962ac59075b964b07152d234b70\",\"site_id\":\"01212121\",\"imp_beacon\":\"\",\"client_ip\":\"206.29.182.240\",\"auth_key\":\"335eaf2639079ffa40b5f7d69f3051fb\",\"blind_id\":\"00000000-0000-0000-0000-000000000000\",\"site_category\":\"IAB4,IAB19-15,IAB5,IAB5-15,IAB3,IAB1-1\",\"lat\":\"38.5\",\"long\":\"-122.1514\"}";
            assertEquals(expectedData, actualData);
        }
    }

    @Test
    public void testDCPMableParseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String response =
                "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dmable_int%26c%3DMable%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fmable%2Fmable5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpMableAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpMableAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dmable_int%26c%3DMable%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fmable%2Fmable5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpMableAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMableParseAppAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        final String externalKey = "19100";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String response =
                "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dmable_int%26c%3DMable%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fmable%2Fmable5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpMableAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpMableAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dmable_int%26c%3DMable%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fmable%2Fmable5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpMableAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMableParseNoAd() throws Exception {
        final String response = "";
        dcpMableAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMableAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPMableParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpMableAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMableAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpMableAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMableGetId() throws Exception {
        assertEquals(mableAdvId, dcpMableAdNetwork.getId());
    }

    @Test
    public void testDCPMableGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpMableAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPMableGetName() throws Exception {
        assertEquals("mableDCP", dcpMableAdNetwork.getName());
    }

}
