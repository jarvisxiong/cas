package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.channels.adnetworks.appier.DCPAppierAdNetwork;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import com.inmobi.adserve.channels.adnetworks.appier.DCPAppierAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPAppierAdnetworkTest extends TestCase {
    private Configuration      mockConfig   = null;
    private final String       debug        = "debug";
    private final String       loggerConf   = "/tmp/channel-server.properties";
    private DCPAppierAdNetwork dcpAppierAdNetwork;
    private final String       appierHost   = "http://ed5.rtb.appier.net/imreq/inmobi_hk";
    private final String       appierStatus = "on";
    private final String       appierAdvId  = "appieradv1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("appier.host")).andReturn(appierHost).anyTimes();
        expect(mockConfig.getString("appier.status")).andReturn(appierStatus).anyTimes();
        expect(mockConfig.getString("appier.advertiserId")).andReturn(appierAdvId).anyTimes();
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
        Formatter.init();
        Channel serverChannel = createMock(Channel.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        dcpAppierAdNetwork = new DCPAppierAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testDCPAppierConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                true);
    }

    @Test
    public void testDCPAppierConfigureParametersBlankIP() {
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
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                false);
    }

    @Test
    public void testDCPAppierConfigureParametersBlankExtKey() {
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
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                false);
    }

    @Test
    public void testDCPAppierConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                false);
    }


    /*
     * @Test public void testDCPAppierRequestUri() throws Exception { SASRequestParameters sasParams = new
     * SASRequestParameters(); CasInternalRequestParameters casInternalRequestParameters = new
     * CasInternalRequestParameters(); sasParams.setRemoteHostIp("206.29.182.240"); sasParams .setUserAgent(
     * "Mozilla/5.0 (Linux; U; Android 2.2.2; es-us; Movistar Prime Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
     * ); casInternalRequestParameters.latLong = "37.4429,-122.1514";
     * sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9"); casInternalRequestParameters.uid =
     * "202cb962ac59075b964b07152d234b70"; sasParams.setSlot("15"); sasParams.setSource("wap"); sasParams.setOsId(3);
     * sasParams.setSiteType("PERFORMANCE"); Long[] cat = new Long[] { 15l, 13l };
     * sasParams.setCategories(Arrays.asList(cat)); String externalKey = "test"; SlotSizeMapping.init(); Long[]
     * segmentCategories = new Long[] { 13l, 15l }; String clurl =
     * "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1"
     * ; String blurl =
     * "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1"
     * ; ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
     * appierAdvId, null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null, 0, true,
     * null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null, new
     * ArrayList<Integer>(), 0.0d, null, null, 0)); if (dcpAppierAdNetwork.configureParameters(sasParams,
     * casInternalRequestParameters, entity, clurl, blurl)) { String expectedUrl =
     * "version=1&reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&segkey=test&clientip=206.29.182.240&bsiteid=00000000-0000-0000-0000-000000000000&sitetype=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.2.2%3B+es-us%3B+Movistar+Prime+Build%2FFRF91%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&categories=Board%2CAdventure&os=android&lat=37.4429&long=-122.1514&size=320x50&um5=202cb962ac59075b964b07152d234b70&siterating=Performance"
     * ; ChannelBuffer actualUrl = dcpAppierAdNetwork.getHttpRequest().getContent(); // .getRequestUri().toString();
     * ChannelBuffer expectedUrlBuffer = ChannelBuffers.copiedBuffer(expectedUrl, CharsetUtil.UTF_8);
     * assertEquals(actualUrl, expectedUrlBuffer); } }
     */

    /*
     * @Test public void testDCPAppierRequestUriBlankLatLong() throws Exception { SASRequestParameters sasParams = new
     * SASRequestParameters(); CasInternalRequestParameters casInternalRequestParameters = new
     * CasInternalRequestParameters(); sasParams.setRemoteHostIp("206.29.182.240"); sasParams.setUserAgent("Mozilla");
     * casInternalRequestParameters.latLong = ""; Long[] cat = new Long[] { 15l, 13l };
     * sasParams.setCategories(Arrays.asList(cat)); sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
     * casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70"; sasParams.setSlot("15");
     * sasParams.setSiteType("FAMILY_SAFE"); String externalKey = "1324"; SlotSizeMapping.init(); String clurl =
     * "http://c2.w.inmobi.com/c" + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd" +
     * "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1"; ChannelSegmentEntity entity = new
     * ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder( appierAdvId, null, null, null, 0, null, null,
     * true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false, false, false, false,
     * false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null, 0)); if
     * (dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
     * 
     * ChannelBuffer actualUrl = dcpAppierAdNetwork.getHttpRequest().getContent(); // .getRequestUri().toString();
     * String expectedUrl =
     * "version=1&reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&segkey=1324&clientip=206.29.182.240&bsiteid=00000000-0000-0000-0000-000000000000&sitetype=1&ua=Mozilla&categories=Board%2CAdventure&size=320x50&um5=202cb962ac59075b964b07152d234b70&siterating=FAMILY_SAFE"
     * ; ChannelBuffer expectedUrlBuffer = ChannelBuffers.copiedBuffer(expectedUrl, CharsetUtil.UTF_8);
     * assertEquals(actualUrl, expectedUrlBuffer); } }
     */

    /*
     * @Test public void testDCPAppierRequestUriBlankSlot() throws Exception { SASRequestParameters sasParams = new
     * SASRequestParameters(); CasInternalRequestParameters casInternalRequestParameters = new
     * CasInternalRequestParameters(); sasParams.setRemoteHostIp("206.29.182.240"); sasParams.setUserAgent("Mozilla");
     * casInternalRequestParameters.latLong = "37.4429,-122.1514";
     * sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9"); casInternalRequestParameters.uid =
     * "202cb962ac59075b964b07152d234b70"; sasParams.setSlot(""); sasParams.setSiteType("PERFORMANCE"); String
     * externalKey = "test"; Long[] cat = new Long[] { 15l, 13l }; sasParams.setCategories(Arrays.asList(cat));
     * SlotSizeMapping.init(); String clurl = "http://c2.w.inmobi.com/c" +
     * ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd" +
     * "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1"; ChannelSegmentEntity entity = new
     * ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder( appierAdvId, null, null, null, 0, null, null,
     * true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false, false, false, false,
     * false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null, 0)); if
     * (dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
     * ChannelBuffer actualUrl = dcpAppierAdNetwork.getHttpRequest().getContent(); // .getRequestUri().toString();
     * String expectedUrl =
     * "version=1&reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&segkey=test&clientip=206.29.182.240&bsiteid=00000000-0000-0000-0000-000000000000&sitetype=1&ua=Mozilla&categories=Board%2CAdventure&lat=37.4429&long=-122.1514&um5=202cb962ac59075b964b07152d234b70&siterating=Performance"
     * ; ChannelBuffer expectedUrlBuffer = ChannelBuffers.copiedBuffer(expectedUrl, CharsetUtil.UTF_8);
     * assertEquals(actualUrl, expectedUrlBuffer); } }
     */

    @Test
    public void testDCPAppierParseResponseImg() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        String response = "{\"click_landing\":\"http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net\",\"reqid\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"type\":\"img\",\"banner_url\":\"http://placehold.it/320x50\",\"imp_beacon\":\"http://inmobi-hk0.rtb.appier.net/show?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&impid=&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4lswmo121u1K4l1cmG12quso4QxeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO\"}";
        dcpAppierAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAppierAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href='http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://placehold.it/320x50'  /></a><img src='http://inmobi-hk0.rtb.appier.net/show?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&impid=&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4lswmo121u1K4l1cmG12quso4QxeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpAppierAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAppierParseResponseImgAppSDK360() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "{\"click_landing\":\"http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net\",\"reqid\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"type\":\"img\",\"banner_url\":\"http://placehold.it/320x50\",\"imp_beacon\":\"http://inmobi-hk0.rtb.appier.net/show?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&impid=&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4lswmo121u1K4l1cmG12quso4QxeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO\"}";
        dcpAppierAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAppierAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net' onclick=\"document.getElementById('click').src='$IMClickUrl';mraid.openExternal('http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net'); return false;\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://placehold.it/320x50'  /></a><img src='http://inmobi-hk0.rtb.appier.net/show?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=C4sWETu4SOmbi4yZEuhtqw&impid=&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4lswmo121u1K4l1cmG12quso4QxeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpAppierAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAppierParseResponseTextAdWAP() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot((short)4);
        sasParams.setSource("wap");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "{\"click_landing\": \"http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net\",\"reqid\": \"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"type\": \"txt\",\"text\": \"Best Shooting Game!\",\"imp_beacon\": \"http://inmobi-hk0.rtb.appier.net/show?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&impid=&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4lswmo121u1K4l1cmG12quso4QxeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO\"}";
        dcpAppierAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAppierAdNetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><a style=\"text-decoration:none; \" href=\"http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net\" onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Best Shooting Game!</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img src=\"http://inmobi-hk0.rtb.appier.net/show?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&impid=&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4lswmo121u1K4l1cmG12quso4QxeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO\" height=\"1\" width=\"1\" border=\"0\" style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpAppierAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAppierParseResponseTextAdAapSDK360() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot((short)4);
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setSource("app");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "{\"click_landing\": \"http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net\",\"reqid\": \"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"type\": \"txt\",\"text\": \"Best Shooting Game!\",\"imp_beacon\": \"http://inmobi-hk0.rtb.appier.net/show?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&impid=&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4lswmo121u1K4l1cmG12quso4QxeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO\"}";
        dcpAppierAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAppierAdNetwork.getHttpResponseStatusCode(), 200);
        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a style=\"text-decoration:none; \" href=\"http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net\" onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';mraid.openExternal('http://inmobi-hk0.rtb.appier.net/click?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4l1cmG12quso4QxK4lgC4u_CylP38JF3Hf121lR3H7Ru1x1wj7EJbiE34u_uqdxo0dxo0dxKrdxo0dWo0dxo4Rxo0dxKrGxo0dxo0dxo0dxo4lOeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO&bidder=inmobi-hk0.rtb.appier.net'); return false;\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Best Shooting Game!</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img src=\"http://inmobi-hk0.rtb.appier.net/show?reqid=4f8d98e2-4bbd-40bc-8795-22da170700f9&bidobjid=mgYR35C4S8OMOpyTwOV4SQ&impid=&cid=hasoffer_test_20130411&crid=hasoffer_test_20130412_1&partner_id=inmobi_hk&bx=Cylxwnu_oqIxo4QxK4lswmo121u1K4l1cmG12quso4QxeM&ui=CylNUYtyUmgfc8tW30I121uyo0lqwqfmoYSq7rfx7Pi12rwRwqdDorIy30uP7buDo4lO\" height=\"1\" width=\"1\" border=\"0\" style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpAppierAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAppierParseNoAd() throws Exception {
        String response = "";
        dcpAppierAdNetwork.parseResponse(response, HttpResponseStatus.NO_CONTENT);
        assertEquals(dcpAppierAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAppierAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAppierGetId() throws Exception {
        assertEquals(dcpAppierAdNetwork.getId(), "appieradv1");
    }

    @Test
    public void testDCPAppierGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                appierAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAppierAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals(dcpAppierAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPAppierGetName() throws Exception {
        assertEquals(dcpAppierAdNetwork.getName(), "appier");
    }

    @Test
    public void testDCPAppierIsClickUrlReq() throws Exception {
        assertEquals(dcpAppierAdNetwork.isClickUrlRequired(), true);
    }
}
