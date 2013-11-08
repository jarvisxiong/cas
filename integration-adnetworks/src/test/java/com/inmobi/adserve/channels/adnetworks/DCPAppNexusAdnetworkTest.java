package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.appnexus.DCPAppNexusAdnetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPAppNexusAdnetworkTest extends TestCase
{
    private Configuration         mockConfig      = null;
    private final String          debug           = "debug";
    private final String          loggerConf      = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap = null;
    private DebugLogger           logger;
    private DCPAppNexusAdnetwork  dcpAppNexusAdNetwork;
    private final String          appNexusHost    = "http://mobile.adnxs.com/mob?psa=0&format=json";
    private final String          appNexusStatus  = "on";
    private final String          defintiAdvId    = "appNexusAdv1";
    private final String          appNexusTest    = "1";

    public void prepareMockConfig()
    {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("appnexus.host")).andReturn(appNexusHost).anyTimes();
        expect(mockConfig.getString("appnexus.status")).andReturn(appNexusStatus).anyTimes();
        expect(mockConfig.getString("appnexus.test")).andReturn(appNexusTest).anyTimes();
        expect(mockConfig.getString("appnexus.advertiserId")).andReturn(defintiAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception
    {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        Formatter.init();
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        DebugLogger.init(mockConfig);
        logger = new DebugLogger();
        dcpAppNexusAdNetwork = new DCPAppNexusAdnetwork(logger, mockConfig, clientBootstrap, base, serverEvent);
        dcpAppNexusAdNetwork.setName("appnexus");
    }

    @Test
    public void testDCPAppNexusConfigureParameters()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertTrue(dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDCPAppNexusConfigureParametersBlankIP()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDCPAppNexusConfigureParametersBlankExtKey()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        sasParams.setSiteType("PERFORMANCE");
        sasParams.setSlot("4");
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDCPAppNexusConfigureParametersBlankUA()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSiteType("PERFORMANCE");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertFalse(dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null));
    }

    @Test
    public void testDCPAppnexusRequestUri() throws Exception
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        sasParams.setCategories(Arrays.asList(new Long[]
        { 10l, 13l, 30l }));
        String externalKey = "240";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpAppNexusAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://mobile.adnxs.com/mob?psa=0&format=json&ip=206.29.182.240&ua=Mozilla&id=240&size=300x50&loc=37.4429%2C-122.1514&pubclick=http%3A%2F%2Fc2.w.inmobi.com%2Fc.asm%2F4%2Fb%2Fbx5%2Fyaz%2F2%2Fb%2Fa5%2Fm%2F0%2F0%2F0%2F202cb962ac59075b964b07152d234b70%2F4f8d98e2-4bbd-40bc-87e5-22da170600f9%2F-1%2F1%2F9cddca11%3Fds%3D1";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPAppNexusParseResponse() throws Exception
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[]
        { 10l, 13l, 30l }));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "{ \"status\": \"ok\", \"ads\": [{\"type\":\"banner\", \"width\":300, \"height\":50, \"content\": \"<script type=\"text/javascript\">document.write('<a href=\"http://sin1.g.adnxs.com/click?AAAAAAAA4D8fhetRuB7dPwAAAAAAAPA_H4XrUbge3T8AAAAAAADgP5D-cuXdwZBvebWPt1919EczVUxSAAAAAHHIGwB2AgAAzgIAAAIAAAA_nHoASi8EAAYAAQBVU0QAVVNEACwBMgAgCAAAK4kABQUCAQIAAAAA1h2OcAAAAAA./cnd=%21mwarOwje030Qv7jqAxjK3hAgAA../clickenc=http%3A%2F%2Fcnct.tlvmedia.com%2Fckl.php%3Fs%3D1%26c%3Dsin1CPnqvrz7q536RxACGJD9y6veu7DIbyIOMjA2LjI5LjE4Mi4yNDAoAQ..%26mx%3Danx%26t%3D1%26d%3D93478%26r%3Dhttp%253A%252F%252Ftracking1.aleadpay.com%252FAdTag%252FClick%252FY21waWQ9MTc2NTEmdHNpZD01ODAg%253Fdp%253D%2524CLICKID%2524\" target=\"_blank\"><img width=\"300\" height=\"50\" style=\"border-style: none\" src=\"http://cdn.adnxs.com/p/01/db/74/d1/01db74d156327ffbb20463fcd3bda52c.gif\"/></a>');</script>\"}] }";
        // dcpAppNexusAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        // assertEquals(dcpAppNexusAdNetwork.getHttpResponseStatusCode(), 200);
        // assertEquals(
        // "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><script type=\"text/javascript\">document.write('<a href=\"http://sin1.g.adnxs.com/click?AAAAAAAA4D8fhetRuB7dPwAAAAAAAPA_H4XrUbge3T8AAAAAAADgP5D-cuXdwZBvebWPt1919EczVUxSAAAAAHHIGwB2AgAAzgIAAAIAAAA_nHoASi8EAAYAAQBVU0QAVVNEACwBMgAgCAAAK4kABQUCAQIAAAAA1h2OcAAAAAA./cnd=%21mwarOwje030Qv7jqAxjK3hAgAA../clickenc=http%3A%2F%2Fcnct.tlvmedia.com%2Fckl.php%3Fs%3D1%26c%3Dsin1CPnqvrz7q536RxACGJD9y6veu7DIbyIOMjA2LjI5LjE4Mi4yNDAoAQ..%26mx%3Danx%26t%3D1%26d%3D93478%26r%3Dhttp%253A%252F%252Ftracking1.aleadpay.com%252FAdTag%252FClick%252FY21waWQ9MTc2NTEmdHNpZD01ODAg%253Fdp%253D%2524CLICKID%2524\" target=\"_blank\"><img width=\"300\" height=\"50\" style=\"border-style: none\" src=\"http://cdn.adnxs.com/p/01/db/74/d1/01db74d156327ffbb20463fcd3bda52c.gif\"/></a>');</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
        // dcpAppNexusAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAppNexusParseNoAd() throws Exception
    {
        String response = "{\"status\": \"ok\",\"ads\": []}";
        dcpAppNexusAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAppNexusAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAppNexusAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAppNexusParseEmptyResponseCode() throws Exception
    {
        String response = "";
        dcpAppNexusAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAppNexusAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAppNexusAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAppNexusGetId() throws Exception
    {
        assertEquals(dcpAppNexusAdNetwork.getId(), "appNexusAdv1");
    }

    @Test
    public void testDCPAppNexusGetImpressionId() throws Exception
    {
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
        sasParams.setSlot("4");
        sasParams.setSiteType("PERFORMANCE");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals(dcpAppNexusAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPAppNexusGetName() throws Exception
    {
        assertEquals(dcpAppNexusAdNetwork.getName(), "appnexus");
    }

    @Test
    public void testDCPAppNexusIsClickUrlReq() throws Exception
    {
        assertTrue(dcpAppNexusAdNetwork.isClickUrlRequired());
    }
}