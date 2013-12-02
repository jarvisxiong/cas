package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.placeiq.DCPPlaceIQAdnetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPPlaceIQAdnetworkTest extends TestCase {
    private Configuration         mockConfig       = null;
    private final String          debug            = "debug";
    private final String          loggerConf       = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap  = null;
    private DebugLogger           logger;

    private DCPPlaceIQAdnetwork   dcpPlaceIQAdNetwork;
    private final String          placeiqHost      = "http://test.ads.placeiq.com/1.41/ad";
    private final String          placeiqStatus    = "on";
    private final String          placeiqAdvId     = "placeiqadv1";
    private final String          placeiqTest      = "1";
    private final String          placeiqSeed      = "EJoU6f9DsqDyyxB";
    private final String          placeiqPartnerId = "IMB";
    private final String          placeiqFormat    = "xml";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("placeiq.host")).andReturn(placeiqHost).anyTimes();
        expect(mockConfig.getString("placeiq.status")).andReturn(placeiqStatus).anyTimes();
        expect(mockConfig.getString("placeiq.test")).andReturn(placeiqTest).anyTimes();
        expect(mockConfig.getString("placeiq.advertiserId")).andReturn(placeiqAdvId).anyTimes();
        expect(mockConfig.getString("placeiq.partnerId")).andReturn(placeiqPartnerId).anyTimes();
        expect(mockConfig.getString("placeiq.seed")).andReturn(placeiqSeed).anyTimes();
        expect(mockConfig.getString("placeiq.format")).andReturn(placeiqFormat).anyTimes();
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
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        SlotSizeMapping.init();
        DebugLogger.init(mockConfig);
        Formatter.init();
        logger = new DebugLogger();
        dcpPlaceIQAdNetwork = new DCPPlaceIQAdnetwork(logger, mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testDCPPlaceiqConfigureParametersAndroid() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot("11");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "23e2ewq445545";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(true,
            dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersIOS() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot("11");
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(true,
            dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersWap() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot("11");
        sasParams.setOsId(HandSetOS.webOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(true,
            dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
            dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
            dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPPlaceiqRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        String externalKey = "PlaceIQ_test_7";
        SlotSizeMapping.init();
        Calendar now = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String secret = getHashedValue(dateFormat.format(now.getTime()) + placeiqSeed, "MD5");
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = dcpPlaceIQAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://test.ads.placeiq.com/1.41/ad?RT=xml&SK="
                    + secret
                    + "&PT=IMB&AU=IMB%2Fbz%2F6456fc%2F0&IP=206.29.182.240&UA=Mozilla&DO=Android&LT=37.4429&LG=-122.1514&SZ=320x50&AM=202cb962ac59075b964b07152d234b70&AP=6575868&AT=STI";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPPlaceiqRequestUriWithNoCat() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        List<Long> category = new ArrayList<Long>();
        category.add(1l);
        sasParams.setCategories(category);
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        String externalKey = "PlaceIQ_test_7";
        SlotSizeMapping.init();
        Calendar now = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String secret = getHashedValue(dateFormat.format(now.getTime()) + placeiqSeed, "MD5");
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = dcpPlaceIQAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://test.ads.placeiq.com/1.41/ad?RT=xml&SK="
                    + secret
                    + "&PT=IMB&AU=IMB%2Fuc%2F6456fc%2F0&IP=206.29.182.240&UA=Mozilla&DO=Android&LT=37.4429&LG=-122.1514&SZ=320x50&AM=202cb962ac59075b964b07152d234b70&AP=6575868&AT=STI";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPPlaceiqParseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("15");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setOsId(HandSetOS.Android.getValue());
        casInternalRequestParameters.uid = "23e2ewq445545saasw232323";
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PLACEIQ><CONTENT>&lt;div class=”piq_creative”&gt;<![CDATA[<a href=\"http://adclick.g.doubleclick.net/aclk?sa=L&ai=B0ta75SXDUfqjFe_Q6QGwwYGgCuKKzYgDAAAAEAEgquv7HjgAWLLM27hMYMmGgIDMo8AXugEJZ2ZwX2ltYWdlyAEJwAIC4AIA6gIdNTEwMjQyOTAvUGxhY2VJUV90ZXN0aW5nX3NpdGX4AvzRHoADAZAD6AKYA-ADqAMB4AQBoAYW2AYC&num=0&sig=AOD64_2De-k5A7OhskDIPybdJjPZLbA6bw&client=ca-pub-9004609665008229&adurl=\"><img width=\"320\" height=\"50\" border=\"0\"src=\"http://tpc.googlesyndication.com/pageadimg/imgad?id=CICAgIDQnf39JBDAAhgyKAEyCDCZUVf6Jre0\"/></a> <span id=\"te-clearads-js-truste01cont1\"><script type=\"text/javascript\" src=\"http://choices.truste.com/ca?pid=placeiq01&aid=placeiq01&cid=testsizemacro0613&c=truste01cont1&w=320&h=50&sid=0\"></script></span></div>]]></CONTENT><NETWORK>50024410</NETWORK><CREATIVEID>20520035890</CREATIVEID><LINEITEMID>42680050</LINEITEMID><CLICKTHRU><![CDATA[\"http://adclick.g.doubleclick.net/aclk?sa=L&ai=B0ta75SXDUfqjFe_Q6QGwwYGgCuKKzYgDAAAAEAEgquv7HjgAWLLM27hMYMmGgIDMo8AXugEJZ2ZwX2ltYWdlyAEJwAIC4AIA6gIdNTEwMjQyOTAvUGxhY2VJUV90ZXN0aW5nX3NpdGX4AvzRHoADAZAD6AKYA-ADqAMB4AQBoAYW2AYC&num=0&sig=AOD64_2De-k5A7OhskDIPybdJjPZLbA6bw&client=ca-pub-9004609665008229&adurl=\"]]></CLICKTHRU><ADTYPE>STG</ADTYPE></PLACEIQ>";
        dcpPlaceIQAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpPlaceIQAdNetwork.getHttpResponseStatusCode());
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div class=”piq_creative”><a href=\"http://adclick.g.doubleclick.net/aclk?sa=L&ai=B0ta75SXDUfqjFe_Q6QGwwYGgCuKKzYgDAAAAEAEgquv7HjgAWLLM27hMYMmGgIDMo8AXugEJZ2ZwX2ltYWdlyAEJwAIC4AIA6gIdNTEwMjQyOTAvUGxhY2VJUV90ZXN0aW5nX3NpdGX4AvzRHoADAZAD6AKYA-ADqAMB4AQBoAYW2AYC&num=0&sig=AOD64_2De-k5A7OhskDIPybdJjPZLbA6bw&client=ca-pub-9004609665008229&adurl=\"><img width=\"320\" height=\"50\" border=\"0\"src=\"http://tpc.googlesyndication.com/pageadimg/imgad?id=CICAgIDQnf39JBDAAhgyKAEyCDCZUVf6Jre0\"/></a> <span id=\"te-clearads-js-truste01cont1\"><script type=\"text/javascript\" src=\"http://choices.truste.com/ca?pid=placeiq01&aid=placeiq01&cid=testsizemacro0613&c=truste01cont1&w=320&h=50&sid=0\"></script></span></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
            dcpPlaceIQAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPlaceiqParseNoAd() throws Exception {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE PLACEIQ_AD_RESPONSE SYSTEM \"http://ads.placeiq.com/1/ad/placeiq_no_ad_response.dtd\"><PLACEIQ><NOAD></NOAD></PLACEIQ>";
        dcpPlaceIQAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpPlaceIQAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPPlaceiqParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpPlaceIQAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpPlaceIQAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpPlaceIQAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPlaceiqGetId() throws Exception {
        assertEquals(placeiqAdvId, dcpPlaceIQAdNetwork.getId());
    }

    @Test
    public void testDCPPlaceiqGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            placeiqAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpPlaceIQAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPPlaceiqGetName() throws Exception {
        assertEquals("placeiq", dcpPlaceIQAdNetwork.getName());
    }

    @Test
    public void testDCPPlaceiqIsClickUrlReq() throws Exception {
        assertEquals(false, dcpPlaceIQAdNetwork.isClickUrlRequired());
    }

    private String getHashedValue(String message, String hashingType) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashingType);
            byte[] array = md.digest(message.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }
}