package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.generic.GenericAdapter;
import com.inmobi.adserve.channels.adnetworks.generic.MacrosAndStrings;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class GenericAdapterTest extends TestCase {

    private final String          httpoolHost         = "http://a.mobile.toboads.com/get";
    private final String          httpoolAdvertiserId = "9999";
    private GenericAdapter        genericAdapter;
    private final ClientBootstrap clientBootstrap     = new ClientBootstrap();
    private Configuration         mockConfig          = null;
    // private String loggerConf = "/tmp/channel-server.properties";
    private final String          debug               = "debug";
    private final String          advertiserName      = "httpool";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.HOST))).andReturn(httpoolHost).anyTimes();
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.REQUEST_METHOD)))
                .andReturn("get")
                    .anyTimes();
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.MANDATORY_PARAMETERS))).andReturn(
            "$userId&$externalSiteKey&$format&$userIp&$userAgent").anyTimes();
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.REQUEST_PARAMETERS)))
                .andReturn(
                    "did=$userId&zid=$externalSiteKey&format=$format&sdkid=api&sdkver=100&uip=$userIp&ua=$userAgent&ormma=0&fh=1&test=0")
                    .anyTimes();
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.RESPONSE_FORMAT)))
                .andReturn("json")
                    .anyTimes();
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.RESPONSE_STATUS)))
                .andReturn("status")
                    .anyTimes();
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.STATUS_NO_AD))).andReturn("0").anyTimes();
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.IMPRESSION_URL_FIELD))).andReturn(
            "impression_url").anyTimes();
        expect(mockConfig.getString(advertiserName.concat(MacrosAndStrings.CONTENT))).andReturn("content").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
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
        genericAdapter = new GenericAdapter(mockConfig, clientBootstrap, base, serverEvent, "httpool");
    }

    @Test
    public void testGenericAdapterConfigureParameters() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.uid = "1234";
        String externalKey = "118398";
        sasParams.setSlot("9");
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            httpoolAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            genericAdapter.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl), true);
    }

    @Test
    public void testGenericAdapterConfigureParametersNullExtSiteKey() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.uid = "1234";
        sasParams.setAdIncId(32);
        sasParams.setSiteIncId(18);
        sasParams.setSlot("9");
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            httpoolAdvertiserId, null, null, null, 0, null, null, true, true, null, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(genericAdapter.configureParameters(sasParams, casInternalRequestParameters, entity, null, null),
            false);
    }

    @Test
    public void testGenericAdapterRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.uid = "1234";
        String externalKey = "118398";
        sasParams.setSlot("9");
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            httpoolAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (genericAdapter.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl)) {
            String actualUrl = genericAdapter.getRequestUri().toString();
            String expectedUrl = "http://a.mobile.toboads.com/get?did=1234&zid=118398&format=320x48&sdkid=api&sdkver=100&uip=206.29.182.240&ua=Mozilla&ormma=0&fh=1&test=0";
            assertEquals(actualUrl, expectedUrl);
        }
    }

    /*
     * @Test public void testGenericAdapterParseResponseInHtml() throws Exception { SASRequestParameters sasParams = new
     * SASRequestParameters();CasInternalRequestParameters casInternalRequestParameters = new
     * CasInternalRequestParameters(); sasParams.setRemoteHostIp("206.29.182.240");private String loggerConf =
     * "/tmp/channel-server.properties"; sasParams.setUserAgent("Mozilla");
     * sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9"); casInternalRequestParameters.uid = "1234";
     * String externalKey = "118398"; String beaconUrl =
     * "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1"
     * ; genericAdapter.configureParameters(sasParams, casInternalRequestParameters, externalKey, null , beaconUrl);
     * String response = "<div><img src=\"\" height=1 width=1 /></div>"; genericAdapter.parseResponse(response, new
     * HttpResponseStatus(200, "Succcess response")); assertEquals(genericAdapter.getHttpResponseStatusCode(), 200);
     * assertEquals(genericAdapter.getHttpResponseContent(),
     * "<html><body><div><img src=\"\" height=1 width=1 /></div><img src=\""
     * +beaconUrl+"\" height=1 width=1 border=0 /></body></html>"); }
     */

    @Test
    public void testGenericAdapterParseResponseInJson() throws Exception {
        String response = "{\"status\":\"1337\",\"ad_type\":\"tpt\",\"content\":\"<!DOCTYPEhtml><htmlxmlns=\\\"http://www.w3.org/1999/xhtml\\\"><head><title>Httpool</title><metahttp-equiv=\\\"Content-type\\\"content=\\\"text/html;charset=utf-8\\\"/></head>n<bodystyle=\\\"margin:0px;padding:0px\\\">n<ahref=\\\"http://a.mobile.toboads.com/click?adh=5d8b189c-6ba8-40ec-883d-f1c1d7d039b4&add=RegibO8TU8mLvGbkwfKZzdOMhDa--dmGlIFZIELVZ5gP1DZXyAiG8N8xDOKctXZ44G33Q4QK38Y.&did=123456789&url=http://labs.httpool.com\\\"><imgsrc=\\\"http://labs.httpool.com/your_ad_here.png\\\"width=\\\"320\\\"height=\\\"50\\\"/></a></body></html>\",\"impression_url\":\"http://a.mobile.toboads.com/impress?adh=5d8b189c-6ba8-40ec-883d-f1c1d7d039b4&add=RegibO8TU8mLvGbkwfKZzdOMhDa--dmGlIFZIELVZ5gP1DZXyAiG8N8xDOKctXZ44G33Q4QK38Y.&did=123456789\",\"extra\":{\"bg_color\":\"#000000\",\"text_color\":\"#FFFFFF\",\"refresh_time\":\"0\",\"transition\":\"0\"}}";
        genericAdapter.parseResponse(response, new HttpResponseStatus(200, "Succcess response"));
        assertEquals(genericAdapter.getHttpResponseStatusCode(), 200);
        assertEquals(
            genericAdapter.getHttpResponseContent(),
            "<!DOCTYPEhtml><htmlxmlns=\"http://www.w3.org/1999/xhtml\"><head><title>Httpool</title><metahttp-equiv=\"Content-type\"content=\"text/html;charset=utf-8\"/></head>n<bodystyle=\"margin:0px;padding:0px\">n<ahref=\"http://a.mobile.toboads.com/click?adh=5d8b189c-6ba8-40ec-883d-f1c1d7d039b4&add=RegibO8TU8mLvGbkwfKZzdOMhDa--dmGlIFZIELVZ5gP1DZXyAiG8N8xDOKctXZ44G33Q4QK38Y.&did=123456789&url=http://labs.httpool.com\"><imgsrc=\"http://labs.httpool.com/your_ad_here.png\"width=\"320\"height=\"50\"/></a><img src=\"http://a.mobile.toboads.com/impress?adh=5d8b189c-6ba8-40ec-883d-f1c1d7d039b4&add=RegibO8TU8mLvGbkwfKZzdOMhDa--dmGlIFZIELVZ5gP1DZXyAiG8N8xDOKctXZ44G33Q4QK38Y.&did=123456789\" height=1 width=1 border=0 /></body></html>");
    }
}