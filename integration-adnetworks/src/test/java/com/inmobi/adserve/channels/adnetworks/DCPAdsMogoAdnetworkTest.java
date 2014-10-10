package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.bootstrap.Bootstrap;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.adsmogo.DCPAdsMogoAdnetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;

public class DCPAdsMogoAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final Bootstrap clientBootstrap = null;

    private DCPAdsMogoAdnetwork dcpadsmogoAdNetwork;
    private final String adsmogoHost = "http://api2.adsmogo.com/ad/?ver=100&fmt=0&mk=H";
    private final String adsmogoStatus = "on";
    private final String adsmogoAdvId = "adsmogoadv1";
    private final String adsmogoTest = "1";
    private final String adsmogoAuth = "inmobi_ssp";
    private final String adsmogoSecret = "eb2hedsdhn7oe2cj6393rhoz74en72ac";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("adsmogo.host")).andReturn(adsmogoHost)
                .anyTimes();
        expect(mockConfig.getString("adsmogo.status")).andReturn(adsmogoStatus)
                .anyTimes();
        expect(mockConfig.getString("adsmogo.test")).andReturn(adsmogoTest)
                .anyTimes();
        expect(mockConfig.getString("adsmogo.authkey")).andReturn(
                                adsmogoAuth).anyTimes();
        expect(mockConfig.getString("adsmogo.authsecret")).andReturn(
                                adsmogoSecret).anyTimes();
        expect(mockConfig.getString("adsmogo.advertiserId")).andReturn(
                adsmogoAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn(
                "/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn(
                "/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
	
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        Channel serverChannel = createMock(Channel.class);
        prepareMockConfig();
        SlotSizeMapping.init();
        Formatter.init();
        dcpadsmogoAdNetwork = new DCPAdsMogoAdnetwork(mockConfig,
                clientBootstrap, base, serverChannel);
        ;
    }

    @Test
    public void testDCPadsmogoConfigureParametersAndroid() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("11"));
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true, dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPadsmogoConfigureParametersWithNoUid()
            throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("11"));
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false, dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPadsmogoConfigureParametersIOS() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("11"));
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true, dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPadsmogoConfigureParametersWap() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("11"));
        sasParams.setOsId(HandSetOS.webOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true, dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPadsmogoConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false, dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPadsmogoConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false, dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPadsmogoRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(URLEncoder.encode("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_5 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/11B601", "UTF-8"));
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSlot(Short.valueOf("15"));
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        String externalKey = "adsmogo_test_7";
        SlotSizeMapping.init();
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 0, new Integer[] {0}));

      dcpadsmogoAdNetwork.configureParameters(sasParams,casInternalRequestParameters, entity, null, null);

      String actualUrl = dcpadsmogoAdNetwork.getRequestUri().toString();
      String expectedUrl = "http://api2.adsmogo.com/ad/?ver=100&fmt=0&mk=H&aid=adsmogo_test_7&ip=206.29.182.240&ast=banner&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&lat=37.4429&lon=-122.1514&w=320&h=50&anid=202cb962ac59075b964b07152d234b70";

      assertEquals(expectedUrl, actualUrl);

      com.ning.http.client.Request request = dcpadsmogoAdNetwork.getNingRequest();
      String actualMd5Value = ((List<String>)request.getHeaders().get("MOGO_API_SIGNATURE")).get(0);

      // Verifying the expected MD5 for the Query String.
      assertEquals("91f3cbeba1d5c9600b7ed726c3ec2be8", actualMd5Value);
    }

    @Test
    public void testDCPadsmogoRequestUriWithNoCat() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        List<Long> category = new ArrayList<Long>();
        category.add(1l);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSlot(Short.valueOf("15"));
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        String externalKey = "adsmogo_test_7";
        SlotSizeMapping.init();
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 0, new Integer[] {0}));
        if (dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, null, null)) {
            String actualUrl = dcpadsmogoAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://api2.adsmogo.com/ad/?ver=100&fmt=0&mk=H&aid=adsmogo_test_7&ip=206.29.182.240&ast=banner&ua=Mozilla&lat=37.4429&lon=-122.1514&w=320&h=50&anid=202cb962ac59075b964b07152d234b70";
            assertEquals(expectedUrl, actualUrl);
        }
    }
    
    @Test
    public void testDCPAdsMogoIOSRequest() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("11"));
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(adsmogoAdvId,
                        null, null, null, 0, null, null, true, true,
                        externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false,
                        false, false, false, null, new ArrayList<Integer>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true, dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, clurl, null));
        String actualUrl = dcpadsmogoAdNetwork.getRequestUri().toString();
        String expectedUrl = "http://api2.adsmogo.com/ad/?ver=100&fmt=0&mk=H&aid=f6wqjq1r5v&ip=206.29.182.240&ast=banner&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&lat=37.4429&lon=-122.1514&w=728&h=90&ida=23e2ewq445545";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void testDCPadsmogoParseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot(Short.valueOf("15"));
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(HandSetOS.Android.getValue());
        casInternalRequestParameters.setUid("23e2ewq445545saasw232323");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest
                        .getChannelSegmentEntityBuilder(
                                adsmogoAdvId,
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
                                new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<meta http-equiv='Content-Type'content='text/html; charset=UTF-8'/><style type='text/css'>*{padding:0px;margin:0px;-webkit-touch-callout: none;} a:link{text-decoration:none;}.tit{ font-size:[font_1]em;text-decoration: underline;font-weight;}.desc{ font-size:[font_2]em;margin-top:2px;}</style><a href='���http://#####'style='display: block; width: 100%; height: 100%;background-color: #000000'><table border='0'cellpadding='0'cellspacing='0'style='width: 320px; height: 50px;'><tr><td style='padding: 0px 2px; color: #ffffff'id='con'><span class='tit'>ad tilte</span><p class='desc'> Ad Text</p></td></tr></table></a>";
        dcpadsmogoAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpadsmogoAdNetwork.getHttpResponseStatusCode());
        String outputHttpResponseContent = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><meta http-equiv='Content-Type'content='text/html; charset=UTF-8'/><style type='text/css'>*{padding:0px;margin:0px;-webkit-touch-callout: none;} a:link{text-decoration:none;}.tit{ font-size:[font_1]em;text-decoration: underline;font-weight;}.desc{ font-size:[font_2]em;margin-top:2px;}</style><a href='���http://#####'style='display: block; width: 100%; height: 100%;background-color: #000000'><table border='0'cellpadding='0'cellspacing='0'style='width: 320px; height: 50px;'><tr><td style='padding: 0px 2px; color: #ffffff'id='con'><span class='tit'>ad tilte</span><p class='desc'> Ad Text</p></td></tr></table></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(outputHttpResponseContent, dcpadsmogoAdNetwork.getHttpResponseContent());
    }

    
    @Test
    public void testDCPadsmogoParseNoAd() throws Exception {
        String response = "";
        dcpadsmogoAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpadsmogoAdNetwork.getHttpResponseStatusCode());
    }

    
    @Test
    public void testDCPadsmogoGetId() throws Exception {
        assertEquals(adsmogoAdvId, dcpadsmogoAdNetwork.getId());
    }

    @Test
    public void testDCPadsmogoGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest
                        .getChannelSegmentEntityBuilder(
                                adsmogoAdvId,
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
                                new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpadsmogoAdNetwork.configureParameters(sasParams,
                casInternalRequestParameters, entity, clurl, null);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9",
                dcpadsmogoAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPadsmogoGetName() throws Exception {
        assertEquals("adsmogo", dcpadsmogoAdNetwork.getName());
    }

    @Test
    public void testDCPadsmogoIsClickUrlReq() throws Exception {
        assertEquals(false, dcpadsmogoAdNetwork.isClickUrlRequired());
    }

}
