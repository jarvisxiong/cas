package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.adnetworks.smaato.DCPSmaatoAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DocumentBuilderHelper;
import com.inmobi.adserve.channels.util.JaxbHelper;
import com.netflix.governator.guice.LifecycleInjector;


public class DCPSmaatoAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";

    private DCPSmaatoAdnetwork dcpSmaatoAdNetwork;
    private final String smaatoHost = "http://f101.smaato.com/oapi/reqAd.jsp";
    private final String smaatoStatus = "on";
    private final String smaatoAdvId = "mobfoxadv1";
    private final String smaatoTest = "1";
    private final String smaatoPublisherId = "923867039";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("smaato.host")).andReturn(smaatoHost).anyTimes();
        expect(mockConfig.getString("smaato.status")).andReturn(smaatoStatus).anyTimes();
        expect(mockConfig.getString("smaato.test")).andReturn(smaatoTest).anyTimes();
        expect(mockConfig.getString("smaato.advertiserId")).andReturn(smaatoAdvId).anyTimes();
        expect(mockConfig.getString("smaato.pubId")).andReturn(smaatoPublisherId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
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
        LifecycleInjector
                .builder()
                .withModules(Modules.combine(new AbstractModule() {

                    @Override
                    public void configure() {
                        bind(AsyncHttpClientProvider.class).toInstance(createMock(AsyncHttpClientProvider.class));
                        bind(JaxbHelper.class).asEagerSingleton();
                        bind(DocumentBuilderHelper.class).asEagerSingleton();
                        requestStaticInjection(BaseAdNetworkImpl.class);
                    }
                }), new TestScopeModule())
                .usingBasePackages("com.inmobi.adserve.channels.server.netty",
                        "com.inmobi.adserve.channels.api.provider").build().createInjector();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);
        dcpSmaatoAdNetwork = new DCPSmaatoAdnetwork(mockConfig, null, base, serverChannel);
        
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        
        dcpSmaatoAdNetwork.setHost(smaatoHost);
    }

    @Test
    public void testDCPSmaatoConfigureParameters() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(smaatoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":54235,\"pubId\":\"inmobi_1\"," + "\"site\":1234}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpSmaatoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPSmaatoConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(smaatoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpSmaatoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPSmaatoConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(smaatoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpSmaatoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPSmaatoRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("178.190.64.146");
        sasParams
                .setUserAgent("Mozilla/5.0 (iPod; CPU iPhone OS 6_1_5 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Mobile/10B400");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setGpid("TEST_GPID");
        casInternalRequestParameters.setUidADT("1");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setGender("m");
        sasParams.setAge(Short.valueOf("32"));
        final String externalKey = "6378ef4a7db50d955c90f7dffb05ee20";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(smaatoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpSmaatoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpSmaatoAdNetwork.getRequestUri().toString();

            final String expectedUrl =
                    "http://f101.smaato.com/oapi/reqAd.jsp?adspace=6378ef4a7db50d955c90f7dffb05ee20&pub=923867039&device=Mozilla%2F5.0+%28iPod%3B+CPU+iPhone+OS+6_1_5+like+Mac+OS+X%29+AppleWebKit%2F536.26+%28KHTML%2C+like+Gecko%29+Mobile%2F10B400&devip=178.190.64.146&format=all&formatstrict=false&dimension=mma&dimensionstrict=true&openudid=202cb962ac59075b964b07152d234b70&googleadid=TEST_GPID&googlednt=false&gps=37.4429%2C-122.1514&gender=m&kws=Business&width=320&height=50&age=32";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPSmaatoRequestUriBlankLatLong() throws Exception {
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
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(smaatoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpSmaatoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpSmaatoAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://f101.smaato.com/oapi/reqAd.jsp?adspace=01212121&pub=923867039&device=Mozilla&devip=206.29.182.240&format=all&formatstrict=false&dimension=mma&dimensionstrict=true&openudid=202cb962ac59075b964b07152d234b70&gps=38.5%2C-122.1514&kws=Business%2CBooks+%26+Reference&width=320&height=50";
            assertEquals(expectedUrl, actualUrl);

        }
    }

    @Test
    public void testDCPSmaatoParseAdImage() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(smaatoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpSmaatoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        final String response =
                "<?xml version=\"1.0\"?><response xmlns=\"http://soma.smaato.com/oapi/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://soma.smaato.com/oapi/ http://www.smaato.com/definitions/xsd/smaatoapi_v2.xsd\"><sessionid>CFF4454048445012E09748993D22E78C.ip-10-0-37-13</sessionid><status>success</status><user><id>900</id><ownid></ownid></user><ads><ad id=\"0\" type=\"IMG\"><log-id></log-id><valid start=\"0\" end=\"0\" max=\"1\"/><link>http://ec2-54-209-70-178.compute-1.amazonaws.com/oapi/getAd.jsp;jsessionid=CFF4454048445012E09748993D22E78C.ip-10-0-37-13</link><action target=\"http://ec2-54-209-70-178.compute-1.amazonaws.com/oapi/lp.jsp;jsessionid=CFF4454048445012E09748993D22E78C.ip-10-0-37-13\" acc=\"server\"/><beacons/></ad></ads></response>";
        dcpSmaatoAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpSmaatoAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://ec2-54-209-70-178.compute-1.amazonaws.com/oapi/lp.jsp;jsessionid=CFF4454048445012E09748993D22E78C.ip-10-0-37-13' onclick=\"document.getElementById('click').src='$IMClickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://ec2-54-209-70-178.compute-1.amazonaws.com/oapi/getAd.jsp;jsessionid=CFF4454048445012E09748993D22E78C.ip-10-0-37-13'  /></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpSmaatoAdNetwork.getHttpResponseContent());
    }

    public void testDCPSmaatoParseAdText() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(smaatoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpSmaatoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        final String response =
                "<?xml version=\"1.0\"?><response xmlns=\"http://soma.smaato.com/oapi/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://soma.smaato.com/oapi/ http://www.smaato.com/definitions/xsd/smaatoapi_v2.xsd\"><sessionid>DF1C3BAB96CD1E2AC2C69B8E3C990E1A.ip-10-0-40-106</sessionid><status>success</status><user><id>900</id><ownid></ownid></user><ads><ad id=\"0\" type=\"TXT\"><log-id></log-id><valid start=\"0\" end=\"0\" max=\"1\"/><link></link><action target=\"http://ec2-54-209-110-73.compute-1.amazonaws.com/oapi/lp.jsp;jsessionid=DF1C3BAB96CD1E2AC2C69B8E3C990E1A.ip-10-0-40-106\" acc=\"server\"/><adtext>Please visit smaato.com! </adtext><beacons><beacon>http://ec2-54-209-110-73.compute-1.amazonaws.com/oapi/getBeacon.jsp;jsessionid=DF1C3BAB96CD1E2AC2C69B8E3C990E1A.ip-10-0-40-106</beacon></beacons></ad></ads></response>";
        dcpSmaatoAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpSmaatoAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><a style=\"text-decoration:none; \" href=\"http://ec2-54-209-110-73.compute-1.amazonaws.com/oapi/lp.jsp;jsessionid=DF1C3BAB96CD1E2AC2C69B8E3C990E1A.ip-10-0-40-106\" onclick=\"document.getElementById('click').src='$IMClickUrl';\" target=\"_blank\"><div class=\"container template_320_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Please visit smaato.com! </div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img src=\"http://ec2-54-209-110-73.compute-1.amazonaws.com/oapi/getBeacon.jsp;jsessionid=DF1C3BAB96CD1E2AC2C69B8E3C990E1A.ip-10-0-40-106\" height=\"1\" width=\"1\" border=\"0\" style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpSmaatoAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPSmaatoParseNoAd() throws Exception {
        final String response =
                "<?xml version=\"1.0\"?><response xmlns=\"http://soma.smaato.com/oapi/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://soma.smaato.com/oapi/ http://www.smaato.com/definitions/xsd/smaatoapi_v2.xsd\"><sessionid>EBA0BE9C7F4F31B61574F95D298511A9.f116</sessionid><status>error</status><user><id>900</id></user><error><code>42</code><desc>Currently no ad available</desc></error></response>";
        dcpSmaatoAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpSmaatoAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPSmaatoParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpSmaatoAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpSmaatoAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpSmaatoAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPSmaatoGetId() throws Exception {
        assertEquals(smaatoAdvId, dcpSmaatoAdNetwork.getId());
    }

    @Test
    public void testDCPSmaatoGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(smaatoAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpSmaatoAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpSmaatoAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPSmaatoGetName() throws Exception {
        assertEquals("smaatoDCP", dcpSmaatoAdNetwork.getName());
    }

    @Test
    public void testDCPSmaatoIsClickUrlReq() throws Exception {
        assertEquals(true, dcpSmaatoAdNetwork.isClickUrlRequired());
    }
}
