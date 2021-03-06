package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
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

import com.inmobi.adserve.channels.adnetworks.adsmogo.DCPAdsMogoAdnetwork;
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
public class DCPAdsMogoAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final Bootstrap clientBootstrap = null;
    private static final String adsmogoHost = "http://api2.adsmogo.com/ad/?ver=100&fmt=0&mk=H";
    private static final String adsmogoStatus = "on";
    private static final String adsmogoAdvId = "adsmogoadv1";
    private static final String adsmogoTest = "1";
    private static final String adsmogoAuth = "inmobi_ssp";
    private static final String adsmogoSecret = "eb2hedsdhn7oe2cj6393rhoz74en72ac";
    private static Configuration mockConfig = null;
    private static DCPAdsMogoAdnetwork dcpadsmogoAdNetwork;
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("adsmogo.host")).andReturn(adsmogoHost).anyTimes();
        expect(mockConfig.getString("adsmogo.status")).andReturn(adsmogoStatus).anyTimes();
        expect(mockConfig.getString("adsmogo.test")).andReturn(adsmogoTest).anyTimes();
        expect(mockConfig.getString("adsmogo.authkey")).andReturn(adsmogoAuth).anyTimes();
        expect(mockConfig.getString("adsmogo.authsecret")).andReturn(adsmogoSecret).anyTimes();
        expect(mockConfig.getString("adsmogo.advertiserId")).andReturn(adsmogoAdvId).anyTimes();
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

        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        final Channel serverChannel = createMock(Channel.class);
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
        dcpadsmogoAdNetwork = new DCPAdsMogoAdnetwork(mockConfig, clientBootstrap, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
    }

    @Test
    public void testDCPadsmogoConfigureParametersAndroid() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPadsmogoConfigureParametersWithNoUid() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPadsmogoConfigureParametersIOS() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPadsmogoConfigureParametersWap() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.webOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPadsmogoConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPadsmogoConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPadsmogoRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent(URLEncoder
                        .encode("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_5 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/11B601",
                                "UTF-8"));
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "adsmogo_test_7";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));

        dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);

        final String actualUrl = dcpadsmogoAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://api2.adsmogo.com/ad/?ver=100&fmt=0&mk=H&aid=00000000-0000-0000-0000-0000006456fc&ip=206.29.182.240&ast=1&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&os=Android&lat=37.4429&lon=-122.1514&w=320&h=50&anid=202cb962ac59075b964b07152d234b70&p=2";

        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        final com.ning.http.client.Request request = dcpadsmogoAdNetwork.getNingRequestBuilder().build();
        final String actualMd5Value = request.getHeaders().get("MOGO_API_SIGNATURE").get(0);

        // Verifying the expected MD5 for the Query String.
        assertEquals("5f4437c91873e31c00b17a432c17832a", actualMd5Value);
    }

    @Test
    public void testDCPadsmogoRequestUriWithNoCat() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(1l);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "adsmogo_test_7";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpadsmogoAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://api2.adsmogo.com/ad/?ver=100&fmt=0&mk=H&aid=00000000-0000-0000-0000-0000006456fc&ip=206.29.182.240&ast=1&ua=Mozilla&os=Android&lat=37.4429&lon=-122.1514&w=320&h=50&anid=202cb962ac59075b964b07152d234b70&p=2";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPAdsMogoIOSRequest() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
        final String actualUrl = dcpadsmogoAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://api2.adsmogo.com/ad/?ver=100&fmt=0&mk=H&aid=00000000-0000-0020-0000-000000000000&ip=206.29.182.240&ast=1&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&os=iOS&lat=37.4429&lon=-122.1514&w=728&h=90&ida=23e2ewq445545&p=1";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
    }

    @Test
    public void testDCPadsmogoParseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(HandSetOS.Android.getValue());
        casInternalRequestParameters.setUid("23e2ewq445545saasw232323");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));

        AdapterTestHelper.setBeaconAndClickStubs();
        dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String response =
                "<meta http-equiv='Content-Type'content='text/html; charset=UTF-8'/><style type='text/css'>*{padding:0px;margin:0px;-webkit-touch-callout: none;} a:link{text-decoration:none;}.tit{ font-size:[font_1]em;text-decoration: underline;font-weight;}.desc{ font-size:[font_2]em;margin-top:2px;}</style><a href='���http://#####'style='display: block; width: 100%; height: 100%;background-color: #000000'><table border='0'cellpadding='0'cellspacing='0'style='width: 320px; height: 50px;'><tr><td style='padding: 0px 2px; color: #ffffff'id='con'><span class='tit'>ad tilte</span><p class='desc'> Ad Text</p></td></tr></table></a>";
        dcpadsmogoAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpadsmogoAdNetwork.getHttpResponseStatusCode());
        final String outputHttpResponseContent =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><meta http-equiv='Content-Type'content='text/html; charset=UTF-8'/><style type='text/css'>*{padding:0px;margin:0px;-webkit-touch-callout: none;} a:link{text-decoration:none;}.tit{ font-size:[font_1]em;text-decoration: underline;font-weight;}.desc{ font-size:[font_2]em;margin-top:2px;}</style><a href='���http://#####'style='display: block; width: 100%; height: 100%;background-color: #000000'><table border='0'cellpadding='0'cellspacing='0'style='width: 320px; height: 50px;'><tr><td style='padding: 0px 2px; color: #ffffff'id='con'><span class='tit'>ad tilte</span><p class='desc'> Ad Text</p></td></tr></table></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(outputHttpResponseContent, dcpadsmogoAdNetwork.getHttpResponseContent());
    }


    @Test
    public void testDCPadsmogoParseNoAd() throws Exception {
        final String response = "";
        dcpadsmogoAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpadsmogoAdNetwork.getHttpResponseStatusCode());
    }


    @Test
    public void testDCPadsmogoGetId() throws Exception {
        assertEquals(adsmogoAdvId, dcpadsmogoAdNetwork.getId());
    }

    @Test
    public void testDCPadsmogoGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpadsmogoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpadsmogoAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPadsmogoGetName() throws Exception {
        assertEquals("adsmogoDCP", dcpadsmogoAdNetwork.getName());
    }

}
