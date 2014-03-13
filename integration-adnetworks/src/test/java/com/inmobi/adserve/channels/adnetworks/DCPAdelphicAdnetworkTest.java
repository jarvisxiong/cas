package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.bootstrap.Bootstrap;
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

import com.inmobi.adserve.channels.adnetworks.adelphic.DCPAdelphicAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPAdelphicAdnetworkTest extends TestCase {
    private Configuration        mockConfig      = null;
    private final String         debug           = "debug";
    private final String         loggerConf      = "/tmp/channel-server.properties";
    private final Bootstrap      clientBootstrap = null;

    private DCPAdelphicAdNetwork dcpAdelphicAdNetwork;
    private final String         adelphicHost    = "http://ad.ipredictive.com/d/ads";
    private final String         adelphicStatus  = "on";
    private final String         adelphicAdvId   = "adelphicadv1";
    private final String         adelphicTest    = "1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("adelphic.host")).andReturn(adelphicHost).anyTimes();
        expect(mockConfig.getString("adelphic.status")).andReturn(adelphicStatus).anyTimes();
        expect(mockConfig.getString("adelphic.test")).andReturn(adelphicTest).anyTimes();
        expect(mockConfig.getString("adelphic.advertiserId")).andReturn(adelphicAdvId).anyTimes();
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
        dcpAdelphicAdNetwork = new DCPAdelphicAdNetwork(mockConfig, clientBootstrap, base, serverChannel);
    }

    @Test
    public void testDCPAdelphicConfigureParameters() throws JSONException {
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
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                new JSONObject("{\"spot\":54235,\"pubId\":\"inmobi_1\"," + "\"site\":1234}"), new ArrayList<Integer>(),
                0.0d, null, null, 32));
        assertEquals(true,
                dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPAdelphicConfigureParametersBlankIP() {
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
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPAdelphicConfigureParametersAdditionalParams() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPAdelphicConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPAdelphicRequestUri() throws Exception {
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
        String externalKey = "0344343";
        SlotSizeMapping.init();
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                new JSONObject("{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(),
                0.0d, null, null, 0));
        if (dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = dcpAdelphicAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ad.ipredictive.com/d/ads?pub=inmobi_1&site=0&spot=1_testkey&msi.name=00000000-0000-0000-0000-000000000000&msi.id=00000000-0000-0000-0000-000000000000&msi.type=a&version=1.0&ua=Mozilla&cliend_ip=206.29.182.240&ctype=banner&csize=320x50&lat=37.4429&lon=-122.1514&bcat=IAB25-5%2CIAB25-4%2CIAB25-7%2CIAB25-1%2CIAB25-3%2CIAB25-2%2CIAB9-9%2CIAB7-9%2CIAB7-8%2CIAB14-1%2CIAB14-2%2CIAB14-3%2CIAB5-2%2CIAB7-45%2CIAB7-44%2CIAB26%2CIAB8-5%2CIAB7-3%2CIAB25%2CIAB23-9%2CIAB7-2%2CIAB7-5%2CIAB23-2%2CIAB13-5%2CIAB7-10%2CIAB7-4%2CIAB13-7%2CIAB21%2CIAB7-6%2CIAB7-11%2CIAB7-12%2CIAB6-7%2CIAB7-13%2CIAB7-14%2CIAB7-16%2CIAB7-18%2CIAB7-19%2CIAB7%2CIAB10%2CIAB12%2CIAB7-21%2CIAB11%2CIAB7-20%2CIAB7-28%2CIAB7-29%2CIAB7-27%2CIAB7-24%2CIAB7-25%2CIAB7-22%2CIAB19-3%2CIAB17-18%2CIAB7-31%2CIAB7-30%2CIAB7-37%2CIAB11-1%2CIAB7-38%2CIAB11-2%2CIAB7-39%2CIAB7-34%2CIAB7-36%2CIAB23-10%2CIAB15-5%2CIAB12-1%2CIAB12-3%2CIAB26-3%2CIAB12-2%2CIAB26-4%2CIAB26-1%2CIAB26-2%2CIAB7-41%2CIAB7-40%2CIAB11-5%2CIAB11-4%2CIAB11-3&scat=IAB19-15%2CIAB5-15%2CIAB3%2CIAB4";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPAdelphicRequestUriBlankLatLong() throws Exception {
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
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                new JSONObject("{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(),
                0.0d, null, null, 0));
        if (dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpAdelphicAdNetwork.getRequestUri().toString();
            System.out.println(actualUrl);
            String expectedUrl = "http://ad.ipredictive.com/d/ads?pub=inmobi_1&site=0&spot=1_testkey&msi.name=00000000-0000-0000-0000-000000000000&msi.id=00000000-0000-0000-0000-000000000000&msi.type=w&version=1.0&ua=Mozilla&cliend_ip=206.29.182.240&ctype=banner&csize=320x50&lat=38.5&lon=-122.1514&bcat=IAB25-5%2CIAB25-4%2CIAB25-7%2CIAB25-1%2CIAB25-3%2CIAB25-2%2CIAB9-9%2CIAB7-9%2CIAB7-8%2CIAB14-1%2CIAB14-2%2CIAB14-3%2CIAB5-2%2CIAB7-45%2CIAB7-44%2CIAB26%2CIAB8-5%2CIAB7-3%2CIAB25%2CIAB23-9%2CIAB7-2%2CIAB7-5%2CIAB23-2%2CIAB13-5%2CIAB7-10%2CIAB7-4%2CIAB13-7%2CIAB21%2CIAB7-6%2CIAB7-11%2CIAB7-12%2CIAB6-7%2CIAB7-13%2CIAB7-14%2CIAB7-16%2CIAB7-18%2CIAB7-19%2CIAB7%2CIAB10%2CIAB12%2CIAB7-21%2CIAB11%2CIAB7-20%2CIAB7-28%2CIAB7-29%2CIAB7-27%2CIAB7-24%2CIAB7-25%2CIAB7-22%2CIAB19-3%2CIAB17-18%2CIAB7-31%2CIAB7-30%2CIAB7-37%2CIAB11-1%2CIAB7-38%2CIAB11-2%2CIAB7-39%2CIAB7-34%2CIAB7-36%2CIAB23-10%2CIAB15-5%2CIAB12-1%2CIAB12-3%2CIAB26-3%2CIAB12-2%2CIAB26-4%2CIAB26-1%2CIAB26-2%2CIAB7-41%2CIAB7-40%2CIAB11-5%2CIAB11-4%2CIAB11-3&scat=IAB1-1%2CIAB19-15%2CIAB5-15%2CIAB3%2CIAB4%2CIAB5";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPAdelphicParseAd() throws Exception {
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
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                new JSONObject("{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(),
                0.0d, null, null, 32));
        dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpAdelphicAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpAdelphicAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpAdelphicAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAdelphicParseAppAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("15");
        sasParams.setSource("APP");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                new JSONObject("{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(),
                0.0d, null, null, 32));
        dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpAdelphicAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpAdelphicAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpAdelphicAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAdelphicParseNoAd() throws Exception {
        String response = "";
        dcpAdelphicAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpAdelphicAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPAdelphicParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpAdelphicAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpAdelphicAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpAdelphicAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAdelphicGetId() throws Exception {
        assertEquals(adelphicAdvId, dcpAdelphicAdNetwork.getId());
    }

    @Test
    public void testDCPAdelphicGetImpressionId() throws Exception {
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
                adelphicAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                new JSONObject("{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(),
                0.0d, null, null, 32));
        dcpAdelphicAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpAdelphicAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPAdelphicGetName() throws Exception {
        assertEquals("adelphic", dcpAdelphicAdNetwork.getName());
    }

    @Test
    public void testDCPAdelphicIsClickUrlReq() throws Exception {
        assertEquals(false, dcpAdelphicAdNetwork.isClickUrlRequired());
    }
}