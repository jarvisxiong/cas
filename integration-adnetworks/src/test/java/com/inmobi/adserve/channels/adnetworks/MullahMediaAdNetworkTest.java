package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.mullahmedia.MullahMediaNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


/**
 * @author tushara
 * 
 */
public class MullahMediaAdNetworkTest extends TestCase {

    private Configuration      mockConfig        = null;
    private final String       debug             = "debug";
    private final String       loggerConf        = "/tmp/channel-server.properties";
    private MullahMediaNetwork dcpMMAdNetwork;
    private final String       mullahMediaHost   = "http://ads.160tracker.com/mobile_ad_api_indirect.php";
    private final String       mullahMediaStatus = "on";
    private final String       mullahMediaAdvId  = "mullahmedia";
    private final String       huntmadsTest      = "1";
    private final String       publisherId       = "1423";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("mullahmedia.host")).andReturn(mullahMediaHost).anyTimes();
        expect(mockConfig.getString("mullahmedia.status")).andReturn(mullahMediaStatus).anyTimes();
        expect(mockConfig.getString("mullahmedia.test")).andReturn(huntmadsTest).anyTimes();
        expect(mockConfig.getString("mullahmedia.advertiserId")).andReturn(mullahMediaAdvId).anyTimes();
        expect(mockConfig.getString("mullahmedia.publisherId")).andReturn(publisherId).anyTimes();
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
        Formatter.init();
        dcpMMAdNetwork = new MullahMediaNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testMullahMediaConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl),
                true);
    }

    @Test
    public void testMullahMediaConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl),
                false);
    }

    @Test
    public void testMullahMediaConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl),
                false);
    }

    @Test
    public void testMullahMediaConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl),
                false);
    }

    @Test
    public void testMullahMediaRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        String externalKey = "1324";
        JSONArray jsonArray = new JSONArray("[406,94,\"US\",15753,15787]");
        sasParams.setCarrier(jsonArray);
        sasParams.setSiteIncId(56789);
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        Long[] segmentCategories = new Long[] { 13l, 15l };
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null,
                null, 123456, true, null, null, 0, null, false, false, false, false, false, false, false, false, false,
                false, null, new ArrayList<Integer>(), 0.0d, null, null, 123456));
        if (dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl)) {
            String actualUrl = dcpMMAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ads.160tracker.com/mobile_ad_api_indirect.php?user_agent=Mozilla&user_ip=206.29.182.240&request_from=indirect&response_format=json&type=web&unique_key=202cb962ac59075b964b07152d234b70&hash_scheme=md5&lat=37.4429&long=-122.1514&w=320.0&h=50.0&sid=00000000-0001-e240-0000-00000000ddd5&pid=1423&site_name=1324&cat=Adventure";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testMullahMediaRequestUriRON() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        JSONArray jsonArray = new JSONArray("[406,94,\"US\",15753,15787]");
        sasParams.setCarrier(jsonArray);
        sasParams.setSlot("15");
        String externalKey = "1324";
        sasParams.setSiteIncId(56789);
        Long[] cat = new Long[] { 15l, 13l };
        sasParams.setCategories(Arrays.asList(cat));
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        Long[] segmentCategories = new Long[] { 1l };
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null,
                null, 123456, true, null, null, 0, null, false, false, false, false, false, false, false, false, false,
                false, null, new ArrayList<Integer>(), 0.0d, null, null, 123456));
        if (dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl)) {
            String actualUrl = dcpMMAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ads.160tracker.com/mobile_ad_api_indirect.php?user_agent=Mozilla&user_ip=206.29.182.240&request_from=indirect&response_format=json&type=web&unique_key=202cb962ac59075b964b07152d234b70&hash_scheme=md5&lat=37.4429&long=-122.1514&w=320.0&h=50.0&sid=00000000-0001-e240-0000-00000000ddd5&pid=1423&site_name=1324&cat=Board";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testMullahMediaRequestUriMisc() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        JSONArray jsonArray = new JSONArray("[406,94,\"US\",15753,15787]");
        sasParams.setCarrier(jsonArray);
        String externalKey = "1324";
        sasParams.setSiteIncId(56789);
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        Long[] segmentCategories = null;
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, segmentCategories, true, true, externalKey, null, null,
                null, 123456, true, null, null, 0, null, false, false, false, false, false, false, false, false, false,
                false, null, new ArrayList<Integer>(), 0.0d, null, null, 123456));
        if (dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl)) {
            String actualUrl = dcpMMAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ads.160tracker.com/mobile_ad_api_indirect.php?user_agent=Mozilla&user_ip=206.29.182.240&request_from=indirect&response_format=json&type=web&unique_key=202cb962ac59075b964b07152d234b70&hash_scheme=md5&lat=37.4429&long=-122.1514&w=320.0&h=50.0&sid=00000000-0001-e240-0000-00000000ddd5&pid=1423&site_name=1324&cat=miscellenous";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testMullahMediaRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = " ,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        JSONArray jsonArray = new JSONArray("[406,94,\"US\",15753,15787]");
        sasParams.setCarrier(jsonArray);
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                + "?ds=1&beacon=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl)) {
            assertNull(dcpMMAdNetwork.getRequestUri());
        }
    }

    @Test
    public void testMullahMediaRequestUriBlankSlot() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        JSONArray jsonArray = new JSONArray("[516,94,\"US\",15753,15787]");
        sasParams.setCarrier(jsonArray);
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uidMd5 = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("");
        String externalKey = "1324";
        sasParams.setSiteIncId(56789);
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String blurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 123456,
                true, null, null, 123456, null, false, false, false, false, false, false, false, false, false, false,
                null, new ArrayList<Integer>(), 0.0d, null, null, 123456));
        if (dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl)) {
            String actualUrl = dcpMMAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ads.160tracker.com/mobile_ad_api_indirect.php?user_agent=Mozilla&user_ip=206.29.182.240&request_from=indirect&response_format=json&type=web&unique_key=202cb962ac59075b964b07152d234b70&hash_scheme=md5&lat=37.4429&long=-122.1514&carrier_id=537&sid=00000000-0001-e240-0000-00000000ddd5&pid=1423&site_name=1324&cat=miscellenous";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testMullahMediaParseResponse() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);

        String response = "{\"img\":[\"http://www.mmnetwork.mobi/s.php?sig=a03a8024b8e0f5eccf240b164255de6a&adid=30014&caid=7&banner=320_50&cid=4305&advid=1651&e=dc&d=50331&f=m&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+T-Mobile+G2+Build%2FGRJ22%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&up=3493211533&cr=1vQtRSuXirEnkUbpJPLY&unique_key=&tl=4RYn3Dh0bzfK8Q8a6qM8kIW1342xs2C2P4tT602p&\"],\"landing\":[\"http://www.mmnetwork.mobi/c.php?sig=a03a8024b8e0f5eccf240b164255de6a&adid=30014&caid=7&cid=4305&advid=1651&e=dc&f=m&unique_key=&lat=&long=&zip=&banner=320_50&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+T-Mobile+G2+Build%2FGRJ22%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1\"],\"size\":[\"320_50\"]}";
        dcpMMAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMMAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href='http://www.mmnetwork.mobi/c.php?sig=a03a8024b8e0f5eccf240b164255de6a&adid=30014&caid=7&cid=4305&advid=1651&e=dc&f=m&unique_key=&lat=&long=&zip=&banner=320_50&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+T-Mobile+G2+Build%2FGRJ22%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1' onclick=\"document.getElementById('click').src='$IMClickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://www.mmnetwork.mobi/s.php?sig=a03a8024b8e0f5eccf240b164255de6a&adid=30014&caid=7&banner=320_50&cid=4305&advid=1651&e=dc&d=50331&f=m&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+T-Mobile+G2+Build%2FGRJ22%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&up=3493211533&cr=1vQtRSuXirEnkUbpJPLY&unique_key=&tl=4RYn3Dh0bzfK8Q8a6qM8kIW1342xs2C2P4tT602p&'  /></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpMMAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testMullahMediaParseInvalidResponse() throws Exception {
        String response = "HTTP/1.1 20a OK\nServer=Netscape-Enterprise/4.1\n\n[{\"error\" : \"No ads available\"}];";
        dcpMMAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMMAdNetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testMullahMediaParseNoAd() throws Exception {
        String response = "{\"error\":\"no ad is available\"}";
        dcpMMAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMMAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpMMAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testMullahMediaParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpMMAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMMAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpMMAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testMullahMediaGetId() throws Exception {
        assertEquals(dcpMMAdNetwork.getId(), mullahMediaAdvId);
    }

    @Test
    public void testMullahMediaGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        String blurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1&beacon=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mullahMediaAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpMMAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, blurl);
        assertEquals(dcpMMAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testMullahMediaGetName() throws Exception {
        assertEquals(dcpMMAdNetwork.getName(), "mullahmedia");
    }

    @Test
    public void testMullahMediaIsClickUrlReq() throws Exception {
        assertEquals(dcpMMAdNetwork.isClickUrlRequired(), true);
    }
}