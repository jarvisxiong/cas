package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.atnt.ATNTAdNetwork;
import com.inmobi.adserve.channels.adnetworks.drawbridge.DrawBridgeAdNetwork;
import com.inmobi.adserve.channels.adnetworks.ifd.IFDAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mobilecommerce.MobileCommerceAdNetwork;
import com.inmobi.adserve.channels.adnetworks.openx.OpenxAdNetwork;
import com.inmobi.adserve.channels.adnetworks.tapit.DCPTapitAdNetwork;
import com.inmobi.adserve.channels.adnetworks.webmoblink.WebmobLinkAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.phoenix.logging.DebugLogger;


public class AdNetworksTest extends TestCase {
    private final String            atntAdvertiserId         = "2345";
    private final String            atntHost                 = "http://api.yp.com/display/v1/ad?apikey=";

    private final String            mcHostus                 = "http://ms-api.us.mcproton.com/search/inmobi/banner/v1/service.svc/";
    private final String            mcHostuk                 = "http://ms-api.uk.mcproton.com/search/inmobi/banner/v1/service.svc/";
    private final String            mcResponseFormat         = "html";
    private final String            isTest                   = "0";
    private final String            filter                   = "clean";
    private final String            mcAdvertiserId           = "1234";
    private final String            openxHost                = "http://openx.com/get?auid=";
    private final String            drawBridgeHost           = "http://drawbridge.com/get?_pid=";
    private Configuration           mockConfig               = null;
    private final String            debug                    = "debug";
    private final String            atntUsername             = "inmobi";
    private final String            atntPassword             = "inmobi123";
    private final String            atntFormat               = "xml";
    private final String            ifdAdvertiserId          = "4028cb1e37361021013750f93b4d03c1";
    private final String            ifdHost                  = "http://10.14.118.75:8080/phoenix/phoenix?";
    private final String            ifdResponseFormat        = "xhml";
    private final String            dbAdvertiserId           = "9999";
    private final String            openxAdvertiserId        = "9999";
    private final String            partnerId                = "aabb";
    private final String            partnerSignature         = "inmobi";
    private final String            adType                   = "sb";
    // private final String loggerConf = "/tmp/channel-server.properties";
    private IFDAdNetwork            ifdAdNetwork;
    private ATNTAdNetwork           atntAdNetwork;
    private final ClientBootstrap   clientBootstrap          = null;
    private MobileCommerceAdNetwork mobileCommerceAdNetwork;

    private DrawBridgeAdNetwork     drawBridgeAdNetwork;
    private OpenxAdNetwork          openxAdNetwork;
    private static String           test                     = "1";
    // Tapit
    private DCPTapitAdNetwork       dcpTapitAdNetwork;
    private final String            tapitHost                = "http://r.tapit.com/adrequest.php";
    private final String            tapitStatus              = "on";
    private final String            tapitResponseFormat      = "json";
    private final String            tapitAdvId               = "54321";
    private final String            tapitTest                = "0";
    // Webmoblink
    private WebmobLinkAdNetwork     webmoblinkAdNetwork;
    private final String            webmoblinkHost           = "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx";
    private final String            webmoblinkStatus         = "on";
    private final String            webmoblinkResponseFormat = "html";
    private final String            webmoblinkAdvId          = "54321";
    private final String            webmoblinkMode           = "LIVE";
    private final String            webmoblinkAdFormat       = "IMG";
    private DebugLogger             logger;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("ifd.host")).andReturn(ifdHost).anyTimes();
        expect(mockConfig.getString("ifd.advertiserId")).andReturn(ifdAdvertiserId).anyTimes();
        expect(mockConfig.getString("ifd.responseFormat")).andReturn(ifdResponseFormat).anyTimes();
        expect(mockConfig.getString("atnt.host")).andReturn(atntHost).anyTimes();
        expect(mockConfig.getString("atnt.advertiserId")).andReturn(atntAdvertiserId).anyTimes();
        expect(mockConfig.getString("atnt.username")).andReturn(atntUsername).anyTimes();
        expect(mockConfig.getString("atnt.password")).andReturn(atntPassword).anyTimes();
        expect(mockConfig.getString("atnt.format")).andReturn(atntFormat).anyTimes();
        expect(mockConfig.getString("mobilecommerce.advertiserId")).andReturn(mcAdvertiserId).anyTimes();
        expect(mockConfig.getString("drawbridge.advertiserId")).andReturn(dbAdvertiserId).anyTimes();
        expect(mockConfig.getString("openx.advertiserId")).andReturn(openxAdvertiserId).anyTimes();
        expect(mockConfig.getString("drawbridge.host")).andReturn(drawBridgeHost).anyTimes();
        expect(mockConfig.getString("drawbridge.partnerId")).andReturn(partnerId).anyTimes();
        expect(mockConfig.getString("drawbridge.partnerSignature")).andReturn(partnerSignature).anyTimes();
        expect(mockConfig.getString("drawbridge.adType")).andReturn(adType).anyTimes();
        expect(mockConfig.getString("drawbridge.test")).andReturn(test).anyTimes();
        expect(mockConfig.getInt("drawbridge.udidFilter")).andReturn(1).anyTimes();
        expect(mockConfig.getString("openx.host")).andReturn(openxHost).anyTimes();
        expect(mockConfig.getString("mobilecommerce.hostus")).andReturn(mcHostus).anyTimes();
        expect(mockConfig.getString("mobilecommerce.hostuk")).andReturn(mcHostuk).anyTimes();
        expect(mockConfig.getString("mobilecommerce.responseFormat")).andReturn(mcResponseFormat).anyTimes();
        expect(mockConfig.getString("mobilecommerce.isTest")).andReturn(isTest).anyTimes();
        expect(mockConfig.getString("mobilecommerce.filter")).andReturn(filter).anyTimes();
        expect(mockConfig.getString("tapit.host")).andReturn(tapitHost).anyTimes();
        expect(mockConfig.getString("tapit.status")).andReturn(tapitStatus).anyTimes();
        expect(mockConfig.getString("tapit.responseFormat")).andReturn(tapitResponseFormat).anyTimes();
        expect(mockConfig.getString("tapit.test")).andReturn(tapitTest).anyTimes();
        expect(mockConfig.getString("tapit.advertiserId")).andReturn(tapitAdvId).anyTimes();
        expect(mockConfig.getString("webmoblink.host")).andReturn(webmoblinkHost).anyTimes();
        expect(mockConfig.getString("webmoblink.status")).andReturn(webmoblinkStatus).anyTimes();
        expect(mockConfig.getString("webmoblink.mode")).andReturn(webmoblinkMode).anyTimes();
        expect(mockConfig.getString("webmoblink.adformat")).andReturn(webmoblinkAdFormat).anyTimes();
        expect(mockConfig.getString("webmoblink.resformat")).andReturn(webmoblinkResponseFormat).anyTimes();
        expect(mockConfig.getString("webmoblink.advertiserId")).andReturn(webmoblinkAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File("/tmp/channel-server.properties");
        if (!f.exists()) {
            f.createNewFile();
        }
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        SlotSizeMapping.init();
        Formatter.init();
        ifdAdNetwork = new IFDAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
        atntAdNetwork = new ATNTAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
        mobileCommerceAdNetwork = new MobileCommerceAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
        drawBridgeAdNetwork = new DrawBridgeAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
        openxAdNetwork = new OpenxAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
        dcpTapitAdNetwork = new DCPTapitAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
        webmoblinkAdNetwork = new WebmobLinkAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testIFDConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(ifdAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        assertEquals(true,
            ifdAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
    }

    @Test
    public void testAtntConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(atntAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        assertEquals(true,
            atntAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
    }

    @Test
    public void testDCPTapitConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        assertEquals(true,
            dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPTapitConfigureParametersForBlockingOpera() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Opera%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        assertEquals(false,
            dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPTapitConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        assertEquals(false,
            dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPTapitConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        assertEquals(false,
            dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPTapitConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        assertEquals(false,
            dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testWebmoblinkConfigureParameters() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        JSONArray jsonArray = new JSONArray("[365,0,\"us\",10224,10225]");
        sasParams.setCarrier(jsonArray);
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(true,
            webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl));
    }

    @Test
    public void testWebmoblinkConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setAllParametersJson("{\"carrier\": [365,0,\"us\",10224,10225]}");
        Long[] cats = { 10l, 13l, 30l };
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(false,
            webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl));
    }

    @Test
    public void testWebmoblinkConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setAllParametersJson("{\"carrier\": [365,0,\"us\",10224,10225]}");
        Long[] cats = { 10l, 13l, 30l };
        sasParams.setCategories(Arrays.asList(cats));

        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = null;
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(false,
            webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl));
    }

    @Test
    public void testWebmoblinkConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setAllParametersJson("{\"carrier\": [365,0,\"us\",10224,10225]}");
        Long[] cats = { 10l, 13l, 30l };
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(false,
            webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl));
    }

    @Test
    public void testWebmoblinkConfigureParametersBlankCategories() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        JSONArray jsonArray = new JSONArray("[365,0,\"us\",10224,10225]");
        sasParams.setCarrier(jsonArray);
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1" + "/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(true,
            webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl));
    }

    @Test
    public void testWebmoblinkConfigureParametersInvalidCarrier() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        JSONArray jsonArray = new JSONArray("[365,0,\"us\",10224,10225]");
        sasParams.setCarrier(jsonArray);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        Long[] cats = { 10l, 13l, 30l };
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1" + "/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(true,
            webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl));
    }

    @Test
    public void testMobileCommerceConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(mcAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        assertEquals(true,
            mobileCommerceAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
    }

    @Test
    public void testOpenxRequestUriWithIFA() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "1234";
        casInternalRequestParameters.uidIFA = "dfjksahfdjksahdkaw2e23231";
        sasParams.setCountry("us");
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        String externalKey = "118398";
        sasParams.setSiteIncId(18);
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 32, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        if (openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = openxAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://openx.com/get?auid=118398&cnt=us&ip=206.29.182.240&lat=37.4429&lon=-122.1514&lt=3&c.siteId=00000000-0000-0020-0000-000000000012&did.ia=dfjksahfdjksahdkaw2e23231&did=1234";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testOpenxRequestUriWithO1() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "1234";
        casInternalRequestParameters.uidO1 = "dfjksahfdjksahdkaw2e23231";
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        sasParams.setCountry("us");
        String externalKey = "118398";
        sasParams.setSiteIncId(18);
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 32, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        if (openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = openxAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://openx.com/get?auid=118398&cnt=us&ip=206.29.182.240&lat=37.4429&lon=-122.1514&lt=3&c.siteId=00000000-0000-0020-0000-000000000012&did.o1=dfjksahfdjksahdkaw2e23231&did=1234";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testOpenxRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "1234";
        sasParams.setCountry("us");
        String externalKey = "118398";
        sasParams.setSiteIncId(18);
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 32, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject(), new ArrayList<Integer>(),
            0.0d, null, null, 32));
        if (openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = openxAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://openx.com/get?auid=118398&cnt=us&ip=206.29.182.240&lat=37.4429&lon=-122.1514&lt=3&c.siteId=00000000-0000-0020-0000-000000000012&did=1234";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDrawBridgeConfigureParametersNullUid() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1" + "/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject(), new ArrayList<Integer>(),
            0.0d, null, null, 32));
        assertTrue(drawBridgeAdNetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDrawBridgeConfigureParametersWithfilter_iPodNotSet() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject(), new ArrayList<Integer>(),
            0.0d, null, null, 32));
        assertTrue(drawBridgeAdNetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDrawBridgeConfigureParametersWithfilter_iPod_1_RequestNoniPodDevice() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject("{\"filter_iPod\":\"1\"}"),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDrawBridgeConfigureParametersWithfilter_iPod_1_RequestiPodDevice() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPod+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject("{\"filter_iPod\":\"1\"}"),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(drawBridgeAdNetwork
                .configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDrawBridgeConfigureParametersWithfilter_iPod_2_RequestiPodDevice() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPod+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject("{\"filter_iPod\":\"2\"}"),
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDrawBridgeRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setCategories(new ArrayList<Long>());
        sasParams.setUserAgent("Mozilla");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.latLong = "37.4429";
        casInternalRequestParameters.uid = "1234";
        casInternalRequestParameters.uidSO1 = "dac95d86b46afdce78e1b36c082fda03";
        sasParams
                .setAllParametersJson("{\"ctr-file-ver\":\"1.0@2012-11-27 00:15:52.797193\",\"site-type\":\"FAMILY_SAFE\",\"rq-h-user-agent\":\"Mozilla/5.0 (Linux; U; Android 4.0.4; en-us; DROID RAZR Build/6.7.2-180_DHD-16_M4-31) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30\",\"handset\":[45907,\"motorola_droid_razr_ver1_subuano4gics\",5,5,46137418,38281146588463104],\"rq-mk-adcount\":\"1\",\"tp\":\"c_gwhirl\",\"new-category\":[23],\"site-floor\":0.02,\"rq-mk-ad-slot\":\"15\",\"u-id-params\":{\"O1\":\"dac95d86b46afdce78e1b36c082fda03\",\"u-id-s\":\"O1\"},\"carrier\":[406,94,\"US\",11326,11945],\"site-url\":\"https://play.google.com/store/apps/details?id=com.game.JewelsStar\",\"tid\":\"2d965a58-cbac-4d01-bdc9-45085ee0f841\",\"rq-mk-siteid\":\"4028cbff3af511e5013b07c6d64e01f7\",\"site\":[144419,65],\"w-s-carrier\":\"174.65.0.112\",\"loc-src\":\"wifi\",\"slot-served\":\"15\",\"uparams\":{\"u-appdnm\":\"Jewels Star\",\"u-inmobi_androidwebsdkversion\":\"3.5.4\",\"u-appver\":\"2.4\",\"u-key-ver\":\"1\",\"u-income\":\"65000\",\"u-appbid\":\"com.game.JewelsStar\"},\"r-format\":\"xhtml\",\"site-allowBanner\":true,\"category\":[1,13,70,110,224,228,238,249,337,359],\"source\":\"ANDROID\",\"rich-media\":false,\"adcode\":\"NON-JS\",\"sdk-version\":\"a354\",\"pub-id\":\"d24f9e2e04d64829bcd4eae79502045f\"}");
        sasParams.setSiteIncId(121212);
        String externalKey = "191000";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject(), new ArrayList<Integer>(),
            0.0d, null, null, 454545));
        if (drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = drawBridgeAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://drawbridge.com/get?_pid=aabb&_psign=inmobi&_clip=206.29.182.240&_did=1234&_odin1=dac95d86b46afdce78e1b36c082fda03&_ua=Mozilla&_art=sb&_pubcat=miscellenous&_adw=320&_adh=50&_clickbeacon=&_aid=00000000-0006-ef91-0000-00000001d97c&_test=1&_impressionbeacon=&_app=0";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDrawBridgeRequestUriWithSegmentCategory() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "1234";
        casInternalRequestParameters.uidO1 = "dac95d86b46afdce78e1b36c082fda03";
        sasParams
                .setAllParametersJson("{\"ctr-file-ver\":\"1.0@2012-11-27 00:15:52.797193\",\"site-type\":\"FAMILY_SAFE\",\"rq-h-user-agent\":\"Mozilla/5.0 (Linux; U; Android 4.0.4; en-us; DROID RAZR Build/6.7.2-180_DHD-16_M4-31) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30\",\"handset\":[45907,\"motorola_droid_razr_ver1_subuano4gics\",5,5,46137418,38281146588463104],\"rq-mk-adcount\":\"1\",\"tp\":\"c_gwhirl\",\"new-category\":[23],\"site-floor\":0.02,\"rq-mk-ad-slot\":\"15\",\"u-id-params\":{\"O1\":\"dac95d86b46afdce78e1b36c082fda03\",\"u-id-s\":\"O1\"},\"carrier\":[406,94,\"US\",11326,11945],\"site-url\":\"https://play.google.com/store/apps/details?id=com.game.JewelsStar\",\"tid\":\"2d965a58-cbac-4d01-bdc9-45085ee0f841\",\"rq-mk-siteid\":\"4028cbff3af511e5013b07c6d64e01f7\",\"site\":[144419,65],\"w-s-carrier\":\"174.65.0.112\",\"loc-src\":\"wifi\",\"slot-served\":\"15\",\"uparams\":{\"u-appdnm\":\"Jewels Star\",\"u-inmobi_androidwebsdkversion\":\"3.5.4\",\"u-appver\":\"2.4\",\"u-key-ver\":\"1\",\"u-income\":\"65000\",\"u-appbid\":\"com.game.JewelsStar\"},\"r-format\":\"xhtml\",\"site-allowBanner\":true,\"category\":[1,13,70,110,224,228,238,249,337,359],\"source\":\"ANDROID\",\"rich-media\":false,\"adcode\":\"NON-JS\",\"sdk-version\":\"a354\",\"pub-id\":\"d24f9e2e04d64829bcd4eae79502045f\"}");
        sasParams.setSiteIncId(121212);
        Long[] cats = new Long[] { 7l, 8l };
        sasParams.setCategories(Arrays.asList(cats));
        String externalKey = "191000";
        Long[] segmentCategories = new Long[] { 13l, 15l };
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 454545));
        if (drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = drawBridgeAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://drawbridge.com/get?_pid=aabb&_psign=inmobi&_clip=206.29.182.240&_did=1234&_macsha1=dac95d86b46afdce78e1b36c082fda03&_ua=Mozilla&_art=sb&_pubcat=Education%2CEntertainment&_adw=320&_adh=50&_clickbeacon=&_aid=00000000-0006-ef91-0000-00000001d97c&_test=1&_impressionbeacon=&_app=0";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDrawBridgeRequestUriWithSiteCategorySegmentRON() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "1234";
        casInternalRequestParameters.uidO1 = "dac95d86b46afdce78e1b36c082fda03";
        sasParams
                .setAllParametersJson("{\"ctr-file-ver\":\"1.0@2012-11-27 00:15:52.797193\",\"site-type\":\"FAMILY_SAFE\",\"rq-h-user-agent\":\"Mozilla/5.0 (Linux; U; Android 4.0.4; en-us; DROID RAZR Build/6.7.2-180_DHD-16_M4-31) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30\",\"handset\":[45907,\"motorola_droid_razr_ver1_subuano4gics\",5,5,46137418,38281146588463104],\"rq-mk-adcount\":\"1\",\"tp\":\"c_gwhirl\",\"new-category\":[23],\"site-floor\":0.02,\"rq-mk-ad-slot\":\"15\",\"u-id-params\":{\"O1\":\"dac95d86b46afdce78e1b36c082fda03\",\"u-id-s\":\"O1\"},\"carrier\":[406,94,\"US\",11326,11945],\"site-url\":\"https://play.google.com/store/apps/details?id=com.game.JewelsStar\",\"tid\":\"2d965a58-cbac-4d01-bdc9-45085ee0f841\",\"rq-mk-siteid\":\"4028cbff3af511e5013b07c6d64e01f7\",\"site\":[144419,65],\"w-s-carrier\":\"174.65.0.112\",\"loc-src\":\"wifi\",\"slot-served\":\"15\",\"uparams\":{\"u-appdnm\":\"Jewels Star\",\"u-inmobi_androidwebsdkversion\":\"3.5.4\",\"u-appver\":\"2.4\",\"u-key-ver\":\"1\",\"u-income\":\"65000\",\"u-appbid\":\"com.game.JewelsStar\"},\"r-format\":\"xhtml\",\"site-allowBanner\":true,\"category\":[1,13,70,110,224,228,238,249,337,359],\"source\":\"ANDROID\",\"rich-media\":false,\"adcode\":\"NON-JS\",\"sdk-version\":\"a354\",\"pub-id\":\"d24f9e2e04d64829bcd4eae79502045f\"}");
        sasParams.setSiteIncId(121212);
        Long[] cats = new Long[] { 7l, 8l };
        sasParams.setCategories(Arrays.asList(cats));
        String externalKey = "191000";
        Long[] segmentCategories = new Long[] { 1l };
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 454545));
        if (drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = drawBridgeAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://drawbridge.com/get?_pid=aabb&_psign=inmobi&_clip=206.29.182.240&_did=1234&_macsha1=dac95d86b46afdce78e1b36c082fda03&_ua=Mozilla&_art=sb&_pubcat=Education%2CEntertainment&_adw=320&_adh=50&_clickbeacon=&_aid=00000000-0006-ef91-0000-00000001d97c&_test=1&_impressionbeacon=&_app=0";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDrawBridgeRequestUriWithoutAnyCategory() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(new ArrayList<Long>());
        casInternalRequestParameters.latLong = "37.4429";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "1234";
        casInternalRequestParameters.uidIDUS1 = "dac95d86b46afdce78e1b36c082fda03";
        sasParams
                .setAllParametersJson("{\"ctr-file-ver\":\"1.0@2012-11-27 00:15:52.797193\",\"site-type\":\"FAMILY_SAFE\",\"rq-h-user-agent\":\"Mozilla/5.0 (Linux; U; Android 4.0.4; en-us; DROID RAZR Build/6.7.2-180_DHD-16_M4-31) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30\",\"handset\":[45907,\"motorola_droid_razr_ver1_subuano4gics\",5,5,46137418,38281146588463104],\"rq-mk-adcount\":\"1\",\"tp\":\"c_gwhirl\",\"new-category\":[23],\"site-floor\":0.02,\"rq-mk-ad-slot\":\"15\",\"u-id-params\":{\"O1\":\"dac95d86b46afdce78e1b36c082fda03\",\"u-id-s\":\"O1\"},\"carrier\":[406,94,\"US\",11326,11945],\"site-url\":\"https://play.google.com/store/apps/details?id=com.game.JewelsStar\",\"tid\":\"2d965a58-cbac-4d01-bdc9-45085ee0f841\",\"rq-mk-siteid\":\"4028cbff3af511e5013b07c6d64e01f7\",\"site\":[144419,65],\"w-s-carrier\":\"174.65.0.112\",\"loc-src\":\"wifi\",\"slot-served\":\"15\",\"uparams\":{\"u-appdnm\":\"Jewels Star\",\"u-inmobi_androidwebsdkversion\":\"3.5.4\",\"u-appver\":\"2.4\",\"u-key-ver\":\"1\",\"u-income\":\"65000\",\"u-appbid\":\"com.game.JewelsStar\"},\"r-format\":\"xhtml\",\"site-allowBanner\":true,\"category\":[1,13,70,110,224,228,238,249,337,359],\"source\":\"ANDROID\",\"rich-media\":false,\"adcode\":\"NON-JS\",\"sdk-version\":\"a354\",\"pub-id\":\"d24f9e2e04d64829bcd4eae79502045f\"}");
        sasParams.setSiteIncId(121212);
        String externalKey = "191000";
        Long[] segmentCategories = null;
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, segmentCategories, true, true, externalKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
            new ArrayList<Integer>(), 0.0d, null, null, 454545));
        if (drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = drawBridgeAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://drawbridge.com/get?_pid=aabb&_psign=inmobi&_clip=206.29.182.240&_did=dac95d86b46afdce78e1b36c082fda03&_ua=Mozilla&_art=sb&_pubcat=miscellenous&_adw=320&_adh=50&_clickbeacon=&_aid=00000000-0006-ef91-0000-00000001d97c&_test=1&_impressionbeacon=&_app=0";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDrawBridgeRequestUriWithIDA() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setCategories(new ArrayList<Long>());
        casInternalRequestParameters.uid = "1234";
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        casInternalRequestParameters.uidIFA = "dac95d86b46afdce78e1b36c082fda03";
        casInternalRequestParameters.uidADT = "0";
        sasParams
                .setAllParametersJson("{\"ctr-file-ver\":\"1.0@2012-11-27 00:15:52.797193\",\"site-type\":\"FAMILY_SAFE\",\"rq-h-user-agent\":\"Mozilla/5.0 (Linux; U; Android 4.0.4; en-us; DROID RAZR Build/6.7.2-180_DHD-16_M4-31) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30\",\"handset\":[45907,\"motorola_droid_razr_ver1_subuano4gics\",5,5,46137418,38281146588463104],\"rq-mk-adcount\":\"1\",\"tp\":\"c_gwhirl\",\"new-category\":[23],\"site-floor\":0.02,\"rq-mk-ad-slot\":\"15\",\"u-id-params\":{\"IDA\":\"dac95d86b46afdce78e1b36c082fda03\",\"u-id-s\":\"IDA\"},\"carrier\":[406,94,\"US\",11326,11945],\"site-url\":\"https://play.google.com/store/apps/details?id=com.game.JewelsStar\",\"tid\":\"2d965a58-cbac-4d01-bdc9-45085ee0f841\",\"rq-mk-siteid\":\"4028cbff3af511e5013b07c6d64e01f7\",\"site\":[144419,65],\"w-s-carrier\":\"174.65.0.112\",\"loc-src\":\"wifi\",\"slot-served\":\"15\",\"uparams\":{\"u-appdnm\":\"Jewels Star\",\"u-inmobi_androidwebsdkversion\":\"3.5.4\",\"u-appver\":\"2.4\",\"u-key-ver\":\"1\",\"u-income\":\"65000\",\"u-appbid\":\"com.game.JewelsStar\"},\"r-format\":\"xhtml\",\"site-allowBanner\":true,\"category\":[1,13,70,110,224,228,238,249,337,359],\"source\":\"ANDROID\",\"rich-media\":false,\"adcode\":\"NON-JS\",\"sdk-version\":\"a354\",\"pub-id\":\"d24f9e2e04d64829bcd4eae79502045f\"}");
        sasParams.setSiteIncId(121212);
        String externalKey = "191000";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject(), new ArrayList<Integer>(),
            0.0d, null, null, 454545));
        if (drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = drawBridgeAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://drawbridge.com/get?_pid=aabb&_psign=inmobi&_clip=206.29.182.240&_ifa=dac95d86b46afdce78e1b36c082fda03&_optout=0&_did=1234&_ua=Mozilla&_art=sb&_pubcat=miscellenous&_adw=320&_adh=50&_clickbeacon=&_aid=00000000-0006-ef91-0000-00000001d97c&_test=1&_impressionbeacon=&_app=0";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testAtntRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(atntAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        atntAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        String actualUrl = atntAdNetwork.getRequestUri().toString();
        String expectedUrlPrefix = "http://api\\.yp\\.com/display/v1/ad\\?apikey=f6wqjq1r5v&ip=206\\.29\\.182\\.240&useragent=Mozilla&loc=37\\.4429:-122\\.1514&listingcount=1&visitorid=";
        String expectedUrlSuffix = "&clkpxl=http%3A%2F%2Fc2\\.w\\.inmobi\\.com%2Fc\\.asm%2F4%2Fb%2Fbx5%2Fyaz%2F2%2Fb%2Fa5%2Fm%2F0%2F0%2F0%2F202cb962ac59075b964b07152d234b70%2F4f8d98e2-4bbd-40bc-87e5-22da170600f9%2F-1%2F1%2F9cddca11%3Fds%3D1";
        assertTrue(actualUrl.matches(expectedUrlPrefix + "\\d+" + expectedUrlSuffix));
    }

    @Test
    public void testDCPTapitRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSlot("15");
        sasParams.setSiteIncId(18);
        casInternalRequestParameters.uidIFA = "202cb962ac59075b964b07152d234b70";
        String externalKey = "19100";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 32, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpTapitAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://r.tapit.com/adrequest.php?format=json&ip=206.29.182.240&ua=Mozilla&zone=19100&lat=37.4429&long=-122.1514&enctype=raw&idfa=202cb962ac59075b964b07152d234b70&w=320.0&h=50.0&tpsid=00000000-0000-0020-0000-000000000012";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPTapitRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = " ,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        sasParams.setSiteIncId(18);
        casInternalRequestParameters.uidO1 = "202cb962ac59075b964b07152d234b70";
        sasParams.setSource("iphone");
        String externalKey = "19100";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 32, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpTapitAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://r.tapit.com/adrequest.php?format=json&ip=206.29.182.240&ua=Mozilla&zone=19100&enctype=sha1&udid=202cb962ac59075b964b07152d234b70&w=320.0&h=50.0&tpsid=00000000-0000-0020-0000-000000000012";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPTapitRequestUriBlankSlot() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSlot("");
        sasParams.setSiteIncId(18);
        casInternalRequestParameters.uidMd5 = "202cb962ac59075b964b07152d234b70";
        sasParams.setSource("android");
        String externalKey = "19100";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 32, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpTapitAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://r.tapit.com/adrequest.php?format=json&ip=206.29.182.240&ua=Mozilla&zone=19100&lat=37.4429&long=-122.1514&enctype=md5&udid=202cb962ac59075b964b07152d234b70&tpsid=00000000-0000-0020-0000-000000000012";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testWebmoblinkRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        JSONArray jsonArray = new JSONArray("[365,0,\"us\",10224,10225]");
        sasParams.setCarrier(jsonArray);
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        Long[] cats = { 10l, 13l, 30l };
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        if (webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl)) {
            String actualUrl = webmoblinkAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?pid=10023&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=IMG&result=html&cc=us&channels=1,3,10";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testWebmoblinkRequestUriBlankCountry() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        JSONArray jsonArray = new JSONArray("[365,0,\"us\",10224,10225]");
        sasParams.setCarrier(jsonArray);
        Long[] cats = { 10l, 13l, 30l };
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        if (webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl)) {
            String actualUrl = webmoblinkAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?pid=10023&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=IMG&result=html&cc=us&channels=1,3,10";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testWebmoblinkRequestUriBlankChannels() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        JSONArray jsonArray = new JSONArray("[365,0,\"us\",10224,10225]");
        sasParams.setCarrier(jsonArray);
        Long[] cats = {};
        sasParams.setCategories(Arrays.asList(cats));
        ;
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(webmoblinkAdvId, null,
            null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0, null,
            false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        if (webmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl)) {
            String actualUrl = webmoblinkAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?pid=10023&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=IMG&result=html&cc=us";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testOpenxResponse() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));

        openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script>    ";
        openxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(openxAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
            openxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testOpenxResponseWap() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("wap");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));

        openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script>    ";
        openxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(openxAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
            openxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testOpenxResponseApp() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        sasParams.setSlot("4");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));

        openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script>    ";
        openxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(openxAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
            openxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testOpenxResponseAppIMAI() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        sasParams.setSlot("4");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));

        openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);

        String response = "<div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script>    ";
        openxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(openxAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
            openxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDrawbridgeParseResponseWAP() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("WAP");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject(), new ArrayList<Integer>(),
            0.0d, null, null, 32));
        if (drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl,
            beaconUrl)) {
            String response = "<style type='text/css'>     body { margin:0;padding:0 } </style>  <p align='center'>     <a href='http://c.adsymptotic.com/c/c?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D' target='_blank' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1'\"><img src='http://mf.adsymptotic.com/i/00/00/00/06/6452_mg_300x50_20120227_0001.jpg' border='0'/></a> </p> <img src='http://c.adsymptotic.com/c/i?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D&_pid=10218' width='1' height='1'></img> <img src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1&event=beacon' width='1' height='1'></img> <img id=\"click\" width=\"1\" height=\"1\"></img>";
            drawBridgeAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(drawBridgeAdNetwork.getHttpResponseStatusCode(), 200);
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><style type='text/css'>     body { margin:0;padding:0 } </style>  <p align='center'>     <a href='http://c.adsymptotic.com/c/c?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D' target='_blank' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1'\"><img src='http://mf.adsymptotic.com/i/00/00/00/06/6452_mg_300x50_20120227_0001.jpg' border='0'/></a> </p> <img src='http://c.adsymptotic.com/c/i?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D&_pid=10218' width='1' height='1'></img> <img src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1&event=beacon' width='1' height='1'></img> <img id=\"click\" width=\"1\" height=\"1\"></img></body></html>",
                drawBridgeAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testDrawbridgeParseResponseApp() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("android");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject(), new ArrayList<Integer>(),
            0.0d, null, null, 32));
        if (drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl,
            beaconUrl)) {
            String response = "<style type='text/css'>     body { margin:0;padding:0 } </style>  <p align='center'>     <a href='http://c.adsymptotic.com/c/c?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D' target='_blank' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1'\"><img src='http://mf.adsymptotic.com/i/00/00/00/06/6452_mg_300x50_20120227_0001.jpg' border='0'/></a> </p> <img src='http://c.adsymptotic.com/c/i?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D&_pid=10218' width='1' height='1'></img> <img src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1&event=beacon' width='1' height='1'></img> <img id=\"click\" width=\"1\" height=\"1\"></img>";
            drawBridgeAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(drawBridgeAdNetwork.getHttpResponseStatusCode(), 200);
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><style type='text/css'>     body { margin:0;padding:0 } </style>  <p align='center'>     <a href='http://c.adsymptotic.com/c/c?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D' target='_blank' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1'\"><img src='http://mf.adsymptotic.com/i/00/00/00/06/6452_mg_300x50_20120227_0001.jpg' border='0'/></a> </p> <img src='http://c.adsymptotic.com/c/i?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D&_pid=10218' width='1' height='1'></img> <img src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1&event=beacon' width='1' height='1'></img> <img id=\"click\" width=\"1\" height=\"1\"></img></body></html>",
                drawBridgeAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testDrawbridgeParseResponseAppIMAI() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("android");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(dbAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, new JSONObject(), new ArrayList<Integer>(),
            0.0d, null, null, 32));
        if (drawBridgeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl,
            beaconUrl)) {
            String response = "<style type='text/css'>     body { margin:0;padding:0 } </style>  <p align='center'>     <a href='http://c.adsymptotic.com/c/c?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D' target='_blank' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1'\"><img src='http://mf.adsymptotic.com/i/00/00/00/06/6452_mg_300x50_20120227_0001.jpg' border='0'/></a> </p> <img src='http://c.adsymptotic.com/c/i?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D&_pid=10218' width='1' height='1'></img> <img src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1&event=beacon' width='1' height='1'></img> <img id=\"click\" width=\"1\" height=\"1\"></img>";
            drawBridgeAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(drawBridgeAdNetwork.getHttpResponseStatusCode(), 200);
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><style type='text/css'>     body { margin:0;padding:0 } </style>  <p align='center'>     <a href='http://c.adsymptotic.com/c/c?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D' target='_blank' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1'\"><img src='http://mf.adsymptotic.com/i/00/00/00/06/6452_mg_300x50_20120227_0001.jpg' border='0'/></a> </p> <img src='http://c.adsymptotic.com/c/i?p=CiBiMTEzNGIwODMxNGY4ZWQ5ZjI0MGUwODk1MTgyMTNhZhCqJhoFMTAyMTggAigBMP7%2BjsIHOOjL%0A2oMFQABaAGD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BaAByJnNpZy5TfDJiNDQwZjkwZTBkYzUzZWJlMDUzMGVlMjc2NGY4%0ANTYxeAKAAY8EigEiU3wyYjQ0MGY5MGUwZGM1M2ViZTA1MzBlZTI3NjRmODU2MQ%3D%3D&_pid=10218' width='1' height='1'></img> <img src='http://c2.w.inmobi.com/c.asm/4/t/2xsl/ftl/1/2m/ba/u/0/0/0/x/4f5f96fa-013a-1000-e2fa-7d3018aa0040/-1/1/74263297?ds=1&event=beacon' width='1' height='1'></img> <img id=\"click\" width=\"1\" height=\"1\"></img></body></html>",
                drawBridgeAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseBanner() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("wap");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl)) {
            String response = "{\"type\":\"banner\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"\",\"adtext\":\"\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://c.tapit.com/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://i.tapit.com/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50'  width='320'   height='50' /></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseHtmlWap() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("wap");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl)) {
            String response = "{\"type\":\"html\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"\",\"adtext\":\"\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=\"http://c.tapit.com/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\" target=\"_blank\"><img src=\"http://i.tapit.com/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\" width=\"320\" height=\"50\" alt=\"\"/></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseHtmlApp() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl)) {
            String response = "{\"type\":\"html\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"\",\"adtext\":\"\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href=\"http://c.tapit.com/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\" target=\"_blank\"><img src=\"http://i.tapit.com/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\" width=\"320\" height=\"50\" alt=\"\"/></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseHtmlAppIMAI() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl)) {
            String response = "{\"type\":\"html\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"\",\"adtext\":\"\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href=\"http://c.tapit.com/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\" target=\"_blank\"><img src=\"http://i.tapit.com/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\" width=\"320\" height=\"50\" alt=\"\"/></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseTextWap() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("wap");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl)) {
            String response = "{\"type\":\"text\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"sample title\",\"adtext\":\"sample text\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><a style=\"text-decoration:none; \" href=\"http://c.tapit.com/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\" onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">sample text</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseTextAppSDK360() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null,
            null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false, false,
            false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null,
            32));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl)) {
            String response = "{\"type\":\"text\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"sample title\",\"adtext\":\"sample text\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a style=\"text-decoration:none; \" href=\"http://c.tapit.com/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\" onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';mraid.openExternal('http://c.tapit.com/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348'); return false;\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">sample text</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testMobileCommerceParseResponse() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(mcAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        mobileCommerceAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl,
            beaconUrl);
        String response = "<div style=\"margin:0px; border:0px; padding:0px; height:50px; font:14px arial, helvetica, sans-serif; line-height:normal; overflow:hidden;\"><img src=\"?uid=191023&region=US&mocop=7sh\" width=\"1\" height=\"1\" border=\"0\"/><img src=\"2p1z/vul/1/2m/b5/u/0/0/0/x/2a2509a1-013a-1000-db68-7d00182800c0/-1/1/db7f29d5?ds=1&event=beacon\" width=\"1\" height=\"1\" border=\"0\"/><a href=\"http://meta.7search.com/click/click.aspx?x=9NrXsMK4ECOm5nshfzWk7g%3d%3d_NT0wtTT%2b22GVQ0rNWzcEelu9DY%2b7nX%2fSgltrlFKIpd97Z9zFiz1B8dRrqYmL6ixpEzruB6mGYEU%2b0qvI4Q0e2a09rNn3H1NmFs4czZiFZnWcH3r6zjeWilAKHB67Zy0tM7ZuHxqG5udibNFFxAJKtoqt4s2aRuRtWYcyAm2rNQ2418moGXUGbE9C1lO6%2bGXQ2AZv9LiPDx%2b1O2VUYtJHa1hj3UyIQ6M%2fO3YZ8plzv%2boy9RTWdwD0YbeKfbs6%2bfOV\" onclick=\"document.getElementById('click').src='2p1z/vul/1/2m/b5/u/0/0/0/x/2a2509a1-013a-1000-db68-7d00182800c0/-1/1/db7f29d5?ds=1'\" style=\"display:block; text-decoration:none;\"><p style=\"margin:0px; border-radius:5px 5px 0px 0px; padding:1px 5px 0px 5px; width:320px; font-weight:bold; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; color:#000000; background-color:#FBD67C;\"><span style=\"font-size:10px;\">Ad: </span>Find Our Your Top Search Results Now! </p> <p style=\"margin:0px; border-radius:0px 0px 5px 5px; padding:1px 5px 1px 5px; width:320px; font-size: 12px; color:#000000; background-color:#FBD67C;\">Immediately Get Search Results on the Products, Resources & Service...<span style=\"font-size:10px;\">[http://fooffa.com]</span></p>     </a><img id=\"click\" width=\"1\" height=\"1\"></div>";
        mobileCommerceAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, mobileCommerceAdNetwork.getHttpResponseStatusCode());
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div style=\"margin:0px; border:0px; padding:0px; height:50px; font:14px arial, helvetica, sans-serif; line-height:normal; overflow:hidden;\"><img src=\"?uid=191023&region=US&mocop=7sh\" width=\"1\" height=\"1\" border=\"0\"/><img src=\"2p1z/vul/1/2m/b5/u/0/0/0/x/2a2509a1-013a-1000-db68-7d00182800c0/-1/1/db7f29d5?ds=1&event=beacon\" width=\"1\" height=\"1\" border=\"0\"/><a href=\"http://meta.7search.com/click/click.aspx?x=9NrXsMK4ECOm5nshfzWk7g%3d%3d_NT0wtTT%2b22GVQ0rNWzcEelu9DY%2b7nX%2fSgltrlFKIpd97Z9zFiz1B8dRrqYmL6ixpEzruB6mGYEU%2b0qvI4Q0e2a09rNn3H1NmFs4czZiFZnWcH3r6zjeWilAKHB67Zy0tM7ZuHxqG5udibNFFxAJKtoqt4s2aRuRtWYcyAm2rNQ2418moGXUGbE9C1lO6%2bGXQ2AZv9LiPDx%2b1O2VUYtJHa1hj3UyIQ6M%2fO3YZ8plzv%2boy9RTWdwD0YbeKfbs6%2bfOV\" onclick=\"document.getElementById('click').src='2p1z/vul/1/2m/b5/u/0/0/0/x/2a2509a1-013a-1000-db68-7d00182800c0/-1/1/db7f29d5?ds=1'\" style=\"display:block; text-decoration:none;\"><p style=\"margin:0px; border-radius:5px 5px 0px 0px; padding:1px 5px 0px 5px; width:320px; font-weight:bold; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; color:#000000; background-color:#FBD67C;\"><span style=\"font-size:10px;\">Ad: </span>Find Our Your Top Search Results Now! </p> <p style=\"margin:0px; border-radius:0px 0px 5px 5px; padding:1px 5px 1px 5px; width:320px; font-size: 12px; color:#000000; background-color:#FBD67C;\">Immediately Get Search Results on the Products, Resources & Service...<span style=\"font-size:10px;\">[http://fooffa.com]</span></p>     </a><img id=\"click\" width=\"1\" height=\"1\"></div></body></html>",
            mobileCommerceAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testMobileCommerceParseResponseSDK360O() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSource("App");
        sasParams.setSdkVersion("a360");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(getChannelSegmentEntityBuilder(mcAdvertiserId, null,
            null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32));
        mobileCommerceAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl,
            beaconUrl);
        String response = "<div style=\"margin:0px; border:0px; padding:0px; height:50px; font:14px arial, helvetica, sans-serif; line-height:normal; overflow:hidden;\"><img src=\"?uid=191023&region=US&mocop=7sh\" width=\"1\" height=\"1\" border=\"0\"/><img src=\"2p1z/vul/1/2m/b5/u/0/0/0/x/2a2509a1-013a-1000-db68-7d00182800c0/-1/1/db7f29d5?ds=1&event=beacon\" width=\"1\" height=\"1\" border=\"0\"/><a href=\"http://meta.7search.com/click/click.aspx?x=9NrXsMK4ECOm5nshfzWk7g%3d%3d_NT0wtTT%2b22GVQ0rNWzcEelu9DY%2b7nX%2fSgltrlFKIpd97Z9zFiz1B8dRrqYmL6ixpEzruB6mGYEU%2b0qvI4Q0e2a09rNn3H1NmFs4czZiFZnWcH3r6zjeWilAKHB67Zy0tM7ZuHxqG5udibNFFxAJKtoqt4s2aRuRtWYcyAm2rNQ2418moGXUGbE9C1lO6%2bGXQ2AZv9LiPDx%2b1O2VUYtJHa1hj3UyIQ6M%2fO3YZ8plzv%2boy9RTWdwD0YbeKfbs6%2bfOV\" onclick=\"document.getElementById('click').src='2p1z/vul/1/2m/b5/u/0/0/0/x/2a2509a1-013a-1000-db68-7d00182800c0/-1/1/db7f29d5?ds=1'\" style=\"display:block; text-decoration:none;\"><p style=\"margin:0px; border-radius:5px 5px 0px 0px; padding:1px 5px 0px 5px; width:320px; font-weight:bold; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; color:#000000; background-color:#FBD67C;\"><span style=\"font-size:10px;\">Ad: </span>Find Our Your Top Search Results Now! </p> <p style=\"margin:0px; border-radius:0px 0px 5px 5px; padding:1px 5px 1px 5px; width:320px; font-size: 12px; color:#000000; background-color:#FBD67C;\">Immediately Get Search Results on the Products, Resources & Service...<span style=\"font-size:10px;\">[http://fooffa.com]</span></p>     </a><img id=\"click\" width=\"1\" height=\"1\"></div>";
        mobileCommerceAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, mobileCommerceAdNetwork.getHttpResponseStatusCode());
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div style=\"margin:0px; border:0px; padding:0px; height:50px; font:14px arial, helvetica, sans-serif; line-height:normal; overflow:hidden;\"><img src=\"?uid=191023&region=US&mocop=7sh\" width=\"1\" height=\"1\" border=\"0\"/><img src=\"2p1z/vul/1/2m/b5/u/0/0/0/x/2a2509a1-013a-1000-db68-7d00182800c0/-1/1/db7f29d5?ds=1&event=beacon\" width=\"1\" height=\"1\" border=\"0\"/><a href=\"http://meta.7search.com/click/click.aspx?x=9NrXsMK4ECOm5nshfzWk7g%3d%3d_NT0wtTT%2b22GVQ0rNWzcEelu9DY%2b7nX%2fSgltrlFKIpd97Z9zFiz1B8dRrqYmL6ixpEzruB6mGYEU%2b0qvI4Q0e2a09rNn3H1NmFs4czZiFZnWcH3r6zjeWilAKHB67Zy0tM7ZuHxqG5udibNFFxAJKtoqt4s2aRuRtWYcyAm2rNQ2418moGXUGbE9C1lO6%2bGXQ2AZv9LiPDx%2b1O2VUYtJHa1hj3UyIQ6M%2fO3YZ8plzv%2boy9RTWdwD0YbeKfbs6%2bfOV\" onclick=\"document.getElementById('click').src='2p1z/vul/1/2m/b5/u/0/0/0/x/2a2509a1-013a-1000-db68-7d00182800c0/-1/1/db7f29d5?ds=1'\" style=\"display:block; text-decoration:none;\"><p style=\"margin:0px; border-radius:5px 5px 0px 0px; padding:1px 5px 0px 5px; width:320px; font-weight:bold; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; color:#000000; background-color:#FBD67C;\"><span style=\"font-size:10px;\">Ad: </span>Find Our Your Top Search Results Now! </p> <p style=\"margin:0px; border-radius:0px 0px 5px 5px; padding:1px 5px 1px 5px; width:320px; font-size: 12px; color:#000000; background-color:#FBD67C;\">Immediately Get Search Results on the Products, Resources & Service...<span style=\"font-size:10px;\">[http://fooffa.com]</span></p>     </a><img id=\"click\" width=\"1\" height=\"1\"></div></body></html>",
            mobileCommerceAdNetwork.getHttpResponseContent());
    }

    public static ChannelSegmentEntity.Builder getChannelSegmentEntityBuilder(final String advertiserId,
            final String adgroupId, final String adId, final String channelId, final long platformTargeting,
            final Long[] rcList, final Long[] tags, final boolean status, final boolean isTestMode,
            final String externalSiteKey, final Timestamp modified_on, final String campaignId, final Long[] slotIds,
            final long incId, final boolean allTags, final String pricingModel, final Integer[] siteRatings,
            final int targetingPlatform, final ArrayList<Integer> osIds, final boolean udIdRequired,
            final boolean zipCodeRequired, final boolean latlongRequired, final boolean richMediaOnly,
            final boolean appUrlEnabled, final boolean interstitialOnly, final boolean nonInterstitialOnly,
            final boolean stripUdId, final boolean stripZipCode, final boolean stripLatlong,
            final JSONObject additionalParams, final List<Integer> manufModelTargetingList, final double ecpmBoost,
            final Timestamp eCPMBoostDate, final Long[] tod, final long adGroupIncId) {
        ChannelSegmentEntity.Builder builder = ChannelSegmentEntity.newBuilder();
        builder.setAdvertiserId(advertiserId);
        builder.setAdvertiserId(advertiserId);
        builder.setAdgroupId(adgroupId);
        builder.setAdId(adId);
        builder.setChannelId(channelId);
        builder.setPlatformTargeting(platformTargeting);
        builder.setRcList(rcList);
        builder.setTags(tags);
        builder.setCategoryTaxonomy(tags);
        builder.setAllTags(allTags);
        builder.setStatus(status);
        builder.setTestMode(isTestMode);
        builder.setExternalSiteKey(externalSiteKey);
        builder.setModified_on(modified_on);
        builder.setCampaignId(campaignId);
        builder.setSlotIds(slotIds);
        builder.setIncId(incId);
        builder.setAdgroupIncId(incId);
        builder.setPricingModel(pricingModel);
        builder.setSiteRatings(siteRatings);
        builder.setTargetingPlatform(targetingPlatform);
        builder.setOsIds(osIds);
        builder.setUdIdRequired(udIdRequired);
        builder.setLatlongRequired(latlongRequired);
        builder.setStripZipCode(zipCodeRequired);
        builder.setRestrictedToRichMediaOnly(richMediaOnly);
        builder.setAppUrlEnabled(appUrlEnabled);
        builder.setInterstitialOnly(interstitialOnly);
        builder.setNonInterstitialOnly(nonInterstitialOnly);
        builder.setStripUdId(stripUdId);
        builder.setStripLatlong(stripLatlong);
        builder.setStripZipCode(stripZipCode);
        builder.setAdditionalParams(additionalParams);
        builder.setManufModelTargetingList(manufModelTargetingList);
        builder.setEcpmBoost(ecpmBoost);
        builder.setEcpmBoostExpiryDate(eCPMBoostDate);
        builder.setTod(tod);
        builder.setAdgroupIncId(adGroupIncId);
        return builder;
    }

}