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
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.adnetworks.wapstart.DCPWapStartAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.DocumentBuilderHelper;
import com.inmobi.adserve.channels.util.JaxbHelper;
import com.netflix.governator.guice.LifecycleInjector;


/**
 * @author thushara
 * 
 */
public class DCPWapStartAdnetworkTest extends TestCase {
    private Configuration        mockConfig     = null;
    private final String         debug          = "debug";
    private final String         loggerConf     = "/tmp/channel-server.properties";
    private DCPWapStartAdNetwork dcpWapstartAdNetwork;
    private final String         wapstartHost   = "http://ro.plus1.wapstart.ru";
    private final String         wapstartStatus = "on";
    private final String         wapstartAdvId  = "wapstartadv1";
    private final String         wapstartTest   = "1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("wapstart.host")).andReturn(wapstartHost).anyTimes();
        expect(mockConfig.getString("wapstart.status")).andReturn(wapstartStatus).anyTimes();
        expect(mockConfig.getString("wapstart.test")).andReturn(wapstartTest).anyTimes();
        expect(mockConfig.getString("wapstart.advertiserId")).andReturn(wapstartAdvId).anyTimes();
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
        SlotSizeMapping.init();
        Injector injector = LifecycleInjector.builder().withModules(Modules.combine(new AbstractModule() {

            @Override
            public void configure() {
                bind(AsyncHttpClientProvider.class).toInstance(createMock(AsyncHttpClientProvider.class));
                bind(JaxbHelper.class).asEagerSingleton();
                bind(DocumentBuilderHelper.class).asEagerSingleton();
                requestStaticInjection(BaseAdNetworkImpl.class);
            }
        })).usingBasePackages("com.inmobi.adserve.channels.server.netty", "com.inmobi.adserve.channels.api.provider")
                .build().createInjector();
        dcpWapstartAdNetwork = new DCPWapStartAdNetwork(mockConfig, null, base, serverChannel);

    }

    @Test
    public void testDCPWapstartConfigureParameters() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("15"));
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                true);
    }

    @Test
    public void testDCPWapstartConfigureParametersBlankSlot() {
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
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                false);
    }

    @Test
    public void testDCPWapstartConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSlot(Short.valueOf("15"));
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                false);
    }

    @Test
    public void testDCPWapstartConfigureParametersBlankExtKey() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("15"));
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                false);
    }

    @Test
    public void testDCPWapstartConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSlot(Short.valueOf("15"));
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
                dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null),
                false);
    }

    @Test
    public void testDCPWapstartRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot(Short.valueOf("15"));
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        
        dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        String actualUrl = dcpWapstartAdNetwork.getRequestUri().toString();
        String expectedUrl = "http://ro.plus1.wapstart.ru?version=2&encoding=1&area=viewBanner&ip=206.29.182.240&id=1324&pageId=0000000000000000000000200000000000000000&kws=Food+%26+Drink%3BAdventure%3BWord&location=37.4429%2C-122.1514&callbackurl=http%3A%2F%2Fc2.w.inmobi.com%2Fc.asm%2F4%2Fb%2Fbx5%2Fyaz%2F2%2Fb%2Fa5%2Fm%2F0%2F0%2F0%2F202cb962ac59075b964b07152d234b70%2F4f8d98e2-4bbd-40bc-87e5-22da170600f9%2F-1%2F1%2F9cddca11%3Fds%3D1";
        assertEquals(expectedUrl, actualUrl);
        
    }

    @Test
    public void testDCPWapstartRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.latLong = " ,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot(Short.valueOf("15"));
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String externalKey = "1324";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11" + "?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        if (dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpWapstartAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ro.plus1.wapstart.ru?version=2&encoding=1&area=viewBanner&ip=206.29.182.240&id=1324&pageId=0000000000000000000000200000000000000000&kws=Food+%26+Drink%3BAdventure%3BWord&callbackurl=http%3A%2F%2Fc2.w.inmobi.com%2Fc.asm%2F4%2Fb%2Fbx5%2Fyaz%2F2%2Fb%2Fa5%2Fm%2F0%2F0%2F0%2F202cb962ac59075b964b07152d234b70%2F4f8d98e2-4bbd-40bc-87e5-22da170600f9%2F-1%2F1%2F9cddca11%3Fds%3D1";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPWapstartParseResponseHtml() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] { 10l, 13l, 30l }));
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<div class=\"plus1-ad\"	 style=\"background-color: #ffffff; border: solid 1px #a4cef2; padding: 5px; margin: 0; font-family: Arial, Helvetica, sans-serif; position: relative\">	<div style=\"border-bottom: 1px solid #e9e9e9; height: 10px;\"	><img style=\"margin: 0; padding: 0; vertical-align: top; border: none; width: 103px; height: 10px\"		  alt=\"#\"		  src=\"http://ru-img.ro.plus1.wapstart.ru/static/reklame/1.png\"	/></div>	<p style=\"margin: 0; padding: 0;\">		<a href=\"http://ru-click.ro.plus1.wapstart.ru/index.php?area=redirector&amp;type=1&amp;rsId=www15_c6fbad24605d29c468d26d9e929fcad2b36e3708_05221258&amp;site=10022&amp;banner=39073&amp;usr=792507&amp;conHash=8b4a9b3e95420778978b9966bc36be07a6c0f393\"				   style=\"color: #0e3fcc; margin: 0; padding: 0; text-decoration: underline;\"		>�������������� ������������ ���������������� ���� ������������!</a>		<br />		<span style=\"color: #000000; line-height: 11px; margin: 0; padding: 4px 0 3px 0;\">			���� ������������ ������������! ������������ ��������������		</span>	</p>	<p style=\"margin: 0; padding: 4px 0\">		<a href=\"http://ru-click.ro.plus1.wapstart.ru/index.php?area=redirector&amp;type=1&amp;rsId=www15_c6fbad24605d29c468d26d9e929fcad2b36e3708_05221258&amp;site=10022&amp;banner=39073&amp;usr=792507&amp;conHash=8b4a9b3e95420778978b9966bc36be07a6c0f393\" 				><img style=\"border: none; max-width:100%\"			  src=\"http://ru-img.ro.plus1.wapstart.ru/banners/15/1384942336_528c8b0048e1c.gif\"			  alt=\"\"		/></a>	</p></div>	<img src=\"http://ru-cntr.ro.plus1.wapstart.ru/?area=counter&amp;clientSession=62108fd97bd60d313dce896f3a42e93a94b30d4b&amp;bannerId=39073&amp;site=10022\"		 alt=\"\"		 style=\"width: 1px; height: 1px; position: absolute; left: -10000px\"	/>";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 200);
        String outputHttpResponseContent = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div class=\"plus1-ad\"	 style=\"background-color: #ffffff; border: solid 1px #a4cef2; padding: 5px; margin: 0; font-family: Arial, Helvetica, sans-serif; position: relative\">	<div style=\"border-bottom: 1px solid #e9e9e9; height: 10px;\"	><img style=\"margin: 0; padding: 0; vertical-align: top; border: none; width: 103px; height: 10px\"		  alt=\"#\"		  src=\"http://ru-img.ro.plus1.wapstart.ru/static/reklame/1.png\"	/></div>	<p style=\"margin: 0; padding: 0;\">		<a href=\"http://ru-click.ro.plus1.wapstart.ru/index.php?area=redirector&amp;type=1&amp;rsId=www15_c6fbad24605d29c468d26d9e929fcad2b36e3708_05221258&amp;site=10022&amp;banner=39073&amp;usr=792507&amp;conHash=8b4a9b3e95420778978b9966bc36be07a6c0f393\"				   style=\"color: #0e3fcc; margin: 0; padding: 0; text-decoration: underline;\"		>�������������� ������������ ���������������� ���� ������������!</a>		<br />		<span style=\"color: #000000; line-height: 11px; margin: 0; padding: 4px 0 3px 0;\">			���� ������������ ������������! ������������ ��������������		</span>	</p>	<p style=\"margin: 0; padding: 4px 0\">		<a href=\"http://ru-click.ro.plus1.wapstart.ru/index.php?area=redirector&amp;type=1&amp;rsId=www15_c6fbad24605d29c468d26d9e929fcad2b36e3708_05221258&amp;site=10022&amp;banner=39073&amp;usr=792507&amp;conHash=8b4a9b3e95420778978b9966bc36be07a6c0f393\" 				><img style=\"border: none; max-width:100%\"			  src=\"http://ru-img.ro.plus1.wapstart.ru/banners/15/1384942336_528c8b0048e1c.gif\"			  alt=\"\"		/></a>	</p></div>	<img src=\"http://ru-cntr.ro.plus1.wapstart.ru/?area=counter&amp;clientSession=62108fd97bd60d313dce896f3a42e93a94b30d4b&amp;bannerId=39073&amp;site=10022\"		 alt=\"\"		 style=\"width: 1px; height: 1px; position: absolute; left: -10000px\"	/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(outputHttpResponseContent, dcpWapstartAdNetwork.getHttpResponseContent());

    }

    

    @Test
    public void testDCPWapstartParseNoAd() throws Exception {
        String response = "";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPWapstartParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpWapstartAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpWapstartAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPWapstartGetId() throws Exception {
        assertEquals(dcpWapstartAdNetwork.getId(), "wapstartadv1");
    }

    @Test
    public void testDCPWapstartGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot(Short.valueOf("4"));
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                wapstartAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
                null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpWapstartAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals(dcpWapstartAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPWapstartGetName() throws Exception {
        assertEquals(dcpWapstartAdNetwork.getName(), "wapstart");
    }

    @Test
    public void testDCPWapstartIsClickUrlReq() throws Exception {
        assertEquals(dcpWapstartAdNetwork.isClickUrlRequired(), true);
    }
}