package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.baidu.BaiduBidRequest.BidRequest;
import com.baidu.BaiduBidResponse.Ad;
import com.baidu.BaiduBidResponse.BidResponse;
import com.baidu.BaiduBidResponse.MaterialMeta;
import com.inmobi.adserve.channels.adnetworks.baidu.DCPBaiduAdNetwork;
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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DcpBaiduAdNetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final Bootstrap clientBootstrap = null;
    private static final String baiduHost = "http://mobads.baidu.com/cpro/ui/mads.php";
    private static final String baiduStatus = "on";
    private static final String baiduAdvId = "baiduadv1";
    private static final String baiduTest = "1";
    private static final String baiduFormat = "xml";
    private static Configuration mockConfig = null;
    private static DCPBaiduAdNetwork dcpBaiduAdNetwork;
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("baidu.host")).andReturn(baiduHost).anyTimes();
        expect(mockConfig.getString("baidu.status")).andReturn(baiduStatus).anyTimes();
        expect(mockConfig.getString("baidu.test")).andReturn(baiduTest).anyTimes();
        expect(mockConfig.getString("baidu.advertiserId")).andReturn(baiduAdvId).anyTimes();
        expect(mockConfig.getString("baidu.format")).andReturn(baiduFormat).anyTimes();
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
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
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
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        replay(repositoryHelper);
        dcpBaiduAdNetwork = new DCPBaiduAdNetwork(mockConfig, clientBootstrap, base, serverChannel);


        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpBaiduAdNetwork.setHost(baiduHost);
    }

    @Test
    public void testDCPbaiduConfigureParameterBlankUid() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");

        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "debug";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPbaiduConfigureParameterSuccess() throws org.json.JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");

        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"slot\":\"123\"}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPbaiduConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPbaiduConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPbaiduRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");

        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "debug";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"slot\":\"123\"}"),
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        assertTrue(dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
        final String actualUrl = dcpBaiduAdNetwork.getRequestUri().toString();

        final String expectedUrl =
                "http://mobads.baidu.com/cpro/ui/mads.php";
        assertEquals(expectedUrl, actualUrl);

    }

    @Test
    public void testDCPbaiduRequestUriWithLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        sasParams.setOsMajorVersion("3.5");
        casInternalRequestParameters.setUidIFA("ISDSDSD2323SDSDSDSDGHFGDDA");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(1l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "debug";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"slot\":\"123\"}"),
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        assertTrue(dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
        dcpBaiduAdNetwork.getNingRequestBuilder().build().getStringData();
        BidRequest
                bidRequest = Whitebox.<BidRequest>invokeMethod(dcpBaiduAdNetwork, "getRequest");
        assertEquals(bidRequest.getApp().getId(), debug);
        assertEquals(bidRequest.getAdslots(0).getId(), "123");
    }

    @Test
    public void testDCPbaiduParseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setOsMajorVersion("3.5");

        casInternalRequestParameters.setUid("23e2ewq445545saasw232323");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"slot\":\"123\"}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        BidResponse.Builder responseBuilder = BidResponse.newBuilder();
        Ad.Builder adBuilder = Ad.newBuilder();
        adBuilder.setHtmlSnippet("<body>sample html</body>");
        MaterialMeta.Builder metaBuilder = MaterialMeta.newBuilder();
        metaBuilder.setCreativeType(MaterialMeta.CreativeType.HTML);
        metaBuilder.setInteractionType(MaterialMeta.InteractionType.DOWNLOAD);
        adBuilder.setMaterialMeta(metaBuilder);
        responseBuilder.setRequestId("sdasasass");
        responseBuilder.addAds(adBuilder);
        dcpBaiduAdNetwork.parseResponse(responseBuilder.build().toByteArray(), HttpResponseStatus.OK);
        assertEquals(200, dcpBaiduAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><body>sample html</body><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpBaiduAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPbaiduParseNoAd() throws Exception {
        final String response = "";
        dcpBaiduAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpBaiduAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPbaiduParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpBaiduAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpBaiduAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpBaiduAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPbaiduGetId() throws Exception {
        assertEquals(baiduAdvId, dcpBaiduAdNetwork.getId());
    }

    @Test
    public void testDCPbaiduGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(baiduAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpBaiduAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpBaiduAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPbaiduGetName() throws Exception {
        assertEquals("baiduDCP", dcpBaiduAdNetwork.getName());
    }

}
