package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.mobfox.DCPMobFoxAdnetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPMobfoxAdnetworkTest extends TestCase {
    private Configuration      mockConfig        = null;
    private final String       debug             = "debug";
    private final String       loggerConf        = "/tmp/channel-server.properties";

    private DCPMobFoxAdnetwork dcpMobfoxAdNetwork;
    private final String       mobfoxHost        = "http://my.mobfox.com/request.php";
    private final String       mobfoxStatus      = "on";
    private final String       mobfoxAdvId       = "mobfoxadv1";
    private final String       mobfoxTest        = "1";
    private final String       mobfoxAdNetworkId = "test";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("mobfox.host")).andReturn(mobfoxHost).anyTimes();
        expect(mockConfig.getString("mobfox.status")).andReturn(mobfoxStatus).anyTimes();
        expect(mockConfig.getString("mobfox.test")).andReturn(mobfoxTest).anyTimes();
        expect(mockConfig.getString("mobfox.advertiserId")).andReturn(mobfoxAdvId).anyTimes();
        expect(mockConfig.getString("mobfox.adnetworkId")).andReturn(mobfoxAdNetworkId).anyTimes();
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
        Channel serverChannel = createMock(Channel.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        SlotSizeMapping.init();
        Formatter.init();
        dcpMobfoxAdNetwork = new DCPMobFoxAdnetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testDCPMobfoxConfigureParameters() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot("11");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "23e2ewq445545";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mobfoxAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":54235,\"pubId\":\"inmobi_1\"," + "\"site\":1234}"), new ArrayList<Integer>(), 0.0d,
                null, null, 32));
        assertEquals(true,
                dcpMobfoxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMobfoxConfigureParametersBlankIP() {
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
                mobfoxAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpMobfoxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMobfoxConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mobfoxAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpMobfoxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMobfoxRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("178.190.64.146");
        sasParams
                .setUserAgent("Mozilla/5.0 (iPod; CPU iPhone OS 6_1_5 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Mobile/10B400");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        sasParams.setGender("m");
        sasParams.setAge("32");
        String externalKey = "6378ef4a7db50d955c90f7dffb05ee20";
        SlotSizeMapping.init();
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mobfoxAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 0));
        if (dcpMobfoxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = dcpMobfoxAdNetwork.getRequestUri().toString();

            String expectedUrl = "http://my.mobfox.com/request.php?rt=api&s=6378ef4a7db50d955c90f7dffb05ee20&u=Mozilla%2F5.0+%28iPod%3B+CPU+iPhone+OS+6_1_5+like+Mac+OS+X%29+AppleWebKit%2F536.26+%28KHTML%2C+like+Gecko%29+Mobile%2F10B400&i=178.190.64.146&m=live&c_mraid=1&o_mcmd5=202cb962ac59075b964b07152d234b70&v=2.0&latitude=37.4429&longitude=-122.1514&demo.gender=m&demo.keywords=Business&adspace.width=320&adspace.height=50&demo.age=32&s_subid=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPMobfoxRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("WAP");
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        casInternalRequestParameters.latLong = "38.5,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        List<Long> category = new ArrayList<Long>();
        category.add(3l);
        category.add(2l);
        sasParams.setCategories(category);

        String externalKey = "01212121";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mobfoxAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 0));
        if (dcpMobfoxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpMobfoxAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://my.mobfox.com/request.php?rt=api&s=01212121&u=Mozilla&i=206.29.182.240&m=live&c_mraid=1&o_mcmd5=202cb962ac59075b964b07152d234b70&v=2.0&latitude=38.5&longitude=-122.1514&demo.keywords=Business%2CBooks+%26+Reference&adspace.width=320&adspace.height=50&s_subid=00000000-0000-0000-0000-000000000000";
            assertEquals(expectedUrl, actualUrl);

        }
    }

    @Test
    public void testDCPMobfoxParseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("15");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mobfoxAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 32));
        dcpMobfoxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><request type=\"textAd\"><htmlString><![CDATA[<body style=\"text-align:center;margin:0;padding:0;\"><div align=\"center\"><a href=\"http://account.mobfox.com/activation-info.php\" target=\"_self\"><img src=\"http://creative1cdn.mobfox.com/static/documents/testbanner/300x250.jpg\" border=\"0\"/></a></div></body>]]></htmlString><clicktype>inapp</clicktype><clickurl><![CDATA[http://account.mobfox.com/activation-info.php]]></clickurl><urltype>link</urltype><refresh>60</refresh><scale>no</scale><skippreflight>yes</skippreflight></request>";
        dcpMobfoxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpMobfoxAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><body style=\"text-align:center;margin:0;padding:0;\"><div align=\"center\"><a href=\"http://account.mobfox.com/activation-info.php\" target=\"_self\"><img src=\"http://creative1cdn.mobfox.com/static/documents/testbanner/300x250.jpg\" border=\"0\"/></a></div></body><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpMobfoxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMobfoxParseNoAd() throws Exception {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><request type=\"noAd\"></request>";
        dcpMobfoxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMobfoxAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPMobfoxParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpMobfoxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMobfoxAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpMobfoxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMobfoxGetId() throws Exception {
        assertEquals(mobfoxAdvId, dcpMobfoxAdNetwork.getId());
    }

    @Test
    public void testDCPMobfoxGetImpressionId() throws Exception {
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
                mobfoxAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 32));
        dcpMobfoxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpMobfoxAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPMobfoxGetName() throws Exception {
        assertEquals("mobfox", dcpMobfoxAdNetwork.getName());
    }

    @Test
    public void testDCPMobfoxIsClickUrlReq() throws Exception {
        assertEquals(false, dcpMobfoxAdNetwork.isClickUrlRequired());
    }
}