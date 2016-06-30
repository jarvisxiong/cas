package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.onedigitalad.DCPOneDigitalAdNetwork;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 * Created by deepak.jha on 5/10/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPOneDigitalAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final Bootstrap clientBootstrap = null;
    private static final String OneDigitalAdvId = "OneDigitaladv1";
    private static Configuration mockConfig = null;
    private static DCPOneDigitalAdNetwork dcpOneDigitalAdNetwork;
    private static String OneDigitalHost = "http://imsgrtb.onedigitalad.com/is2s";
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("onedigitalad.host")).andReturn(OneDigitalHost).anyTimes();
        expect(mockConfig.getString("onedigitalad.advertiserId")).andReturn(OneDigitalAdvId).anyTimes();
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

        dcpOneDigitalAdNetwork = new DCPOneDigitalAdNetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpOneDigitalAdNetwork.setName("OneDigital");

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpOneDigitalAdNetwork.setHost(OneDigitalHost);
    }

    @Test
    public void testDCPOneDigitalConfigureParameters() {

        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.setGpid("gpidtest123");
        casInternalRequestParameters.setImpressionId("testImp");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+" +
                "%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String externalKey = "f6wqjq1r5v";
        JSONObject additionalParams = new JSONObject();
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(OneDigitalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, additionalParams,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpOneDigitalAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPOneDigitalConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        casInternalRequestParameters.setImpressionId("testImp");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+" +
                "%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        JSONObject additionalParams = new JSONObject();
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(OneDigitalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, additionalParams,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpOneDigitalAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPOneDigitalConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.setImpressionId("testImp");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%2" +
                        "8KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        JSONObject additionalParams = new JSONObject();
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(OneDigitalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, additionalParams,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpOneDigitalAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testOneDigitalRequestUri() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setImpressionId("testImp");
        sasParams.setOsId(3);
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setImpressionId("IMPID");
        sasParams.setOsMajorVersion("4.4");
        sasParams.setDeviceMake("Apple iPhone");
        sasParams.setDeviceModel("J5");
        sasParams.setConnectionType(ConnectionType.WIFI);
        casInternalRequestParameters.setUidIFA("idfa202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setGpid("gpid202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "123qwe";
        JSONObject additionalParams = new JSONObject();
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(OneDigitalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, additionalParams,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpOneDigitalAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper)) {
            final String actualUrl = dcpOneDigitalAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://imsgrtb.onedigitalad.com/is2s";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        }
    }

    @Test
    public void testDCPOneDigitalParseResponse() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setUid("uid");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "19100";
        JSONObject additionalParams = new JSONObject();
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(OneDigitalAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, additionalParams,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpOneDigitalAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
                repositoryHelper);
        Whitebox.setInternalState(dcpOneDigitalAdNetwork, "floor", 0.1);
        final String response =
                "{\"reqId\":\"9d768fa6-0155-1000-d89e-000029ef0000\",\"adm\":\" <div> <a target='_blank' href='http:" +
                        "//imsgrtb.onedigitalad.com/clicktrack2/bGFuZGluZ1VybD13d3cubXludHJhLmNvbSUyRmZsYXQtMzAtc2Fs" +
                        "ZSUzRnV0bV9zb3VyY2UlM0RvbmVkaWdpdGFsJTI2dXRtX21lZGl1bSUzRGNwYyUyNnV0bV9jYW1wYWlnbiUzRGJfVHJ" +
                        "hZmZpY19jYWxsX2JhbGxfZ2FsbF8yMi00NF9JbmRpYV8yNy1NYXlfTWF5RnVuY3Rpb25hbF9hbGxfT25lRGlnaXRhbC" +
                        "UyNmNhbXBhaWduSWQlM0Q1NzczN2E3NmFlZDI3MDJlNTQzMGJhYmUlMjZhZElkJTNENTc3MzdkMmFhZWQyNzAyZTU0M" +
                        "zBiYWM0JmNhbXBhaWduSWQ9NTc3MzdhNzZhZWQyNzAyZTU0MzBiYWJlJnJlcUlkPTlkNzY4ZmE2LTAxNTUtMTAwMC1k" +
                        "ODllLTAwMDAyOWVmMDAwMCZiaWRJZD1kN2UzZGZmZS0yZmUwLTRhYjMtODIyNS02OGMyNThjMTE3OWU='>    <img " +
                        "style = 'border: 0' width = '320' height='50' src='http://cdn.onedigitalad.com/creatives/20" +
                        "16/6/29/320x50.gif-mJIztKibvApC-XQF0d3-EyyC.gif' alt=''/> </a><img src='http://imsgrtb.oned" +
                        "igitalad.com/adpixel.gif?reqId=9d768fa6-0155-1000-d89e-000029ef0000&amp;bidId=d7e3dffe-2fe0" +
                        "-4ab3-8225-68c258c1179e&amp;exchangeId=38E31250-DD17-4C48-A9B9-15B8B9AE5D02&amp;exchangeNam" +
                        "e=is2s' style='width:1px; height:1px; display:None' alt=''/></div>\",\"price\":1}";
        dcpOneDigitalAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpOneDigitalAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, m" +
                        "aximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style" +
                        "></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.j" +
                        "s\"></script> <div> <a target='_blank' href='http://imsgrtb.onedigitalad.com/clicktrack2/b" +
                        "GFuZGluZ1VybD13d3cubXludHJhLmNvbSUyRmZsYXQtMzAtc2FsZSUzRnV0bV9zb3VyY2UlM0RvbmVkaWdpdGFsJTI" +
                        "2dXRtX21lZGl1bSUzRGNwYyUyNnV0bV9jYW1wYWlnbiUzRGJfVHJhZmZpY19jYWxsX2JhbGxfZ2FsbF8yMi00NF9Jb" +
                        "mRpYV8yNy1NYXlfTWF5RnVuY3Rpb25hbF9hbGxfT25lRGlnaXRhbCUyNmNhbXBhaWduSWQlM0Q1NzczN2E3NmFlZDI" +
                        "3MDJlNTQzMGJhYmUlMjZhZElkJTNENTc3MzdkMmFhZWQyNzAyZTU0MzBiYWM0JmNhbXBhaWduSWQ9NTc3MzdhNzZhZ" +
                        "WQyNzAyZTU0MzBiYWJlJnJlcUlkPTlkNzY4ZmE2LTAxNTUtMTAwMC1kODllLTAwMDAyOWVmMDAwMCZiaWRJZD1kN2U" +
                        "zZGZmZS0yZmUwLTRhYjMtODIyNS02OGMyNThjMTE3OWU='>    <img style = 'border: 0' width = '320' " +
                        "height='50' src='http://cdn.onedigitalad.com/creatives/2016/6/29/320x50.gif-mJIztKibvApC-X" +
                        "QF0d3-EyyC.gif' alt=''/> </a><img src='http://imsgrtb.onedigitalad.com/adpixel.gif?reqId=9" +
                        "d768fa6-0155-1000-d89e-000029ef0000&amp;bidId=d7e3dffe-2fe0-4ab3-8225-68c258c1179e&amp;exc" +
                        "hangeId=38E31250-DD17-4C48-A9B9-15B8B9AE5D02&amp;exchangeName=is2s' style='width:1px; hei" +
                        "ght:1px; display:None' alt=''/></div><img src='beaconUrl' height=1 width=1 border=0 style=" +
                        "\"display:none;\"/></body></html>",
                dcpOneDigitalAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPOneDigitalGetName() throws Exception {
        assertEquals(dcpOneDigitalAdNetwork.getName(), "onedigitaladDCP");
    }
}