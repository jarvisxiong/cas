package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.microsoft.DCPMicrosoftAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.ning.http.client.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.Test;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 * Created by deepak.jha on 3/10/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPMicrosoftAdnetworkTest extends TestCase {

    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final Bootstrap clientBootstrap = null;
    private String MicrosoftHost = "https://www.bing.com/api/beta/v4/ads/native/search?q=requestpayload";
    private String MicrosoftAdvId = "123qwe";
    private DCPMicrosoftAdnetwork dcpmicrosoftadnetwork;
    private RepositoryHelper repositoryHelper;


    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("microsoft.host")).andReturn(MicrosoftHost).anyTimes();
        expect(mockConfig.getString("microsoft.advertiserId")).andReturn(MicrosoftAdvId).anyTimes();
        expect(mockConfig.getString("microsoft.status")).andReturn("on").anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Before
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        Formatter.init();
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        replay(slotSizeMapEntityFor15);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        replay(slotSizeMapEntityFor10);
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4)).andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9)).andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 10)).andReturn(slotSizeMapEntityFor10).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11)).andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14)).andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15)).andReturn(slotSizeMapEntityFor15).anyTimes();
        replay(repositoryHelper);

        dcpmicrosoftadnetwork = new DCPMicrosoftAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpmicrosoftadnetwork.setName("Microsoft");

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpmicrosoftadnetwork.setHost(MicrosoftHost);
    }


    @Test
    public void testDCPMicrosoftConfigureParameters() {

        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.setGpid("gpidtest123");
        sasParams.setUserAgent(
            "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
            AdNetworksTest.getChannelSegmentEntityBuilder(MicrosoftAdvId, null, null, null, 0, null, null, true, true,
                externalKey, null, null, null, new Long[] {0L}, true,
                null, null, 0, null, false, false, false, false, false,
                false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32,
                new Integer[] {0}));
        assertTrue(dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
            repositoryHelper));
    }

    @Test
    public void testDCPMicrosoftConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setUserAgent(
            "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
            AdNetworksTest.getChannelSegmentEntityBuilder(MicrosoftAdvId, null, null, null, 0, null, null, true, true,
                externalKey, null, null, null, new Long[] {0L}, true,
                null, null, 0, null, false, false, false, false, false,
                false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32,
                new Integer[] {0}));
        assertFalse(dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
            repositoryHelper));
    }

    @Test
    public void testDCPMicrosoftConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(null);
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
            AdNetworksTest.getChannelSegmentEntityBuilder(MicrosoftAdvId, null, null, null, 0, null, null, true, true,
                externalKey, null, null, null, new Long[] {0L}, true,
                null, null, 0, null, false, false, false, false, false,
                false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32,
                new Integer[] {0}));
        assertFalse(dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
            repositoryHelper));
    }

    @Test
    public void testDCPMicrosoftConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(
            "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
            AdNetworksTest.getChannelSegmentEntityBuilder(MicrosoftAdvId, null, null, null, 0, null, null, true, true,
                externalKey, null, null, null, new Long[] {0L}, true,
                null, null, 0, null, false, false, false, false, false,
                false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32,
                new Integer[] {0}));
        assertFalse(dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
            repositoryHelper));
    }

    @Test
    public void testDCPMicrosoftRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7");
        sasParams.setSiteIncId(23232);
        sasParams.setConnectionType(ConnectionType.WIFI);
        sasParams.setGender("F");
        sasParams.setOsId(5);
        sasParams.setLatLong("32.87,76.21");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setImpressionId("test");
        sasParams.setLanguage("es");
        sasParams.setCountryCode("us");
        sasParams.setAppUrl("www.monetize.com");
        sasParams.setDeviceMake("HTC");
        sasParams.setDeviceModel("Dezire");
        sasParams.setOsMajorVersion("2");
        sasParams.setConnectionType(ConnectionType.WIFI);
        sasParams.setSiteContentType(ContentType.FAMILY_SAFE);
        sasParams.setAge((short) 24);
        sasParams.setSdkVersion("454");
        sasParams.setAppBundleId("test.bundle");
        casInternalRequestParameters.setLatLong("100,100");
        sasParams.setCategories(Arrays.asList(new Long[]{10l, 13l, 30l}));
        final String externalKey =
            "1324";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(MicrosoftAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));

        dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final Request request = dcpmicrosoftadnetwork.getNingRequestBuilder().build();
        final String actualResponse = request.getStringData();
        final String expectedResponse =
            "{\"_type\":\"Ads/NativeAdsRequest\",\"imp\":{\"id\":\"test\",\"displaymanager\":\"inmobi_sdk\",\"displaymanagerver\":\"454\",\"native\":{\"request\":{\"maxnumberofads\":1}}},\"app\":{\"name\":\"Food \\u0026 Drink\",\"cat\":[\"IAB9-30\",\"IAB5\",\"IAB8\"],\"bundle\":\"test.bundle\",\"Language\":\"es\",\"Country\":\"us\",\"storeurl\":\"www.monetize.com\",\"content\":{\"ext\":{\"bcat\":[\"IAB7-27\",\"IAB7-29\",\"IAB7-28\",\"IAB7-22\",\"IAB7-25\",\"IAB7-24\",\"IAB7-30\",\"IAB7-31\",\"IAB25-3\",\"IAB25-2\",\"IAB25-1\",\"IAB23-2\",\"IAB23-9\",\"IAB25-7\",\"IAB25-5\",\"IAB25-4\",\"IAB11-1\",\"IAB11-2\",\"IAB7-38\",\"IAB7-37\",\"IAB7-39\",\"IAB7-34\",\"IAB7-36\",\"IAB7-41\",\"IAB7-40\",\"IAB6-7\",\"IAB8-5\",\"IAB19-3\",\"IAB11-5\",\"IAB11-3\",\"IAB11-4\",\"IAB13-7\",\"IAB15-5\",\"IAB13-5\",\"IAB7-45\",\"IAB26\",\"IAB7-44\",\"IAB25\",\"IAB17-18\",\"IAB7-10\",\"IAB26-2\",\"IAB26-1\",\"IAB26-4\",\"IAB26-3\",\"IAB23-10\",\"IAB12-1\",\"IAB7-19\",\"IAB12\",\"IAB7-16\",\"IAB11\",\"IAB7-18\",\"IAB7-12\",\"IAB7-11\",\"IAB7-14\",\"IAB7\",\"IAB7-13\",\"IAB7-4\",\"IAB7-5\",\"IAB7-21\",\"IAB7-6\",\"IAB7-20\",\"IAB5-2\",\"IAB7-2\",\"IAB7-3\",\"IAB14-3\",\"IAB14-2\",\"IAB12-2\",\"IAB14-1\",\"IAB12-3\",\"IAB7-8\",\"IAB7-9\",\"IAB9-9\"]}},\"publisher\":{\"name\":\"InMobi\"},\"ext\":{\"PreferredLanguage\":\"es\",\"RequestAgent\":\"MyAdnetworks\"}},\"device\":{\"dnt\":\"0\",\"lmt\":\"0\",\"ua\":\"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7\",\"ip\":\"206.29.182.240\",\"ipv6\":\"\",\"language\":\"es\",\"make\":\"HTC\",\"hwv\":\"Dezire\",\"os\":\"iOS\",\"osv\":\"2\",\"osvname\":\"\",\"connectiontype\":\"2\",\"devicetype\":\"4\",\"h\":\"0\",\"w\":\"0\",\"geo\":{\"lat\":\"100.0000\",\"lon\":\"100.0000\",\"country\":\"USA\",\"city\":\"\",\"region\":\"\",\"utcoffset\":\"\"}},\"user\":{\"yob\":\"1992\",\"gender\":\"F\",\"keywords\":[]},\"regs\":{\"coppa\":\"0\",\"ext\":{\"IsDesignedForFamilies\":\"\"}},\"test\":1,\"query\":\"games\",\"content\":[],\"URL\":\"http://www.inmobi.com/en-us/autos/?test\\u003d23\",\"referralURL\":\"http://inmobi.com\",\"queryType\":\"AdsRequest\",\"publisherData\":{\"providerId\":\"inmobi\"}}";
        assertEquals(actualResponse, expectedResponse);
    }

    @Test
    public void testDCPMicrosoftParseResponse() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        //casInternalRequestParameters.setUdid("weweweweee");
        casInternalRequestParameters.setUid("uid");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[]{10l, 13l, 30l}));
        sasParams.setImaiBaseUrl("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
            AdNetworksTest.getChannelSegmentEntityBuilder(MicrosoftAdvId, null, null, null, 0, null, null, true, true,
                externalKey, null, null, null, new Long[] {0L}, true,
                null, null, 0, null, false, false, false, false, false,
                false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32,
                new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15,
            repositoryHelper);
        String response =
            "{\n" + "    \"_type\": \"Ads/NativeAdsResponse\",\n" + "    \"ads\": [\n" + "        {\n"
                + "            \"_type\": \"Ads/PaidSearchForNative/AdsResponse/OneClickAd\",\n"
                + "            \"title\": \"The 2015 BMW X5\",\n"
                + "            \"description\": \"The X5 Can Keep Up with Anything. Even You. Book a Test Drive Online\",\n"
                + "            \"displayUrl\": \"bmw.com/BMW-X5\u200E\",\n"
                + "            \"targetUrl\": \"http://0.r.msn.com/?ld=link0\",\n"
                + "            \"targetId\": \"inArticle2Para\",\n"
                + "            \"templateId\": \"inArticleFavDisplayBorderLeft\",\n" + "            \"images\": [\n"
                + "                {\n"
                + "                    \"imageUrl\": \"http://www.bing.com/th?id=A8ep0xDJzbQjiZw16x16&pid=AdsPlus\",\n"
                + "                    \"imageType\": \"Favicon\"\n" + "                },\n" + "                {\n"
                + "                    \"imageUrl\": \"https://www.bing.com/th?id=OAIP.095201a525482a209adef1190d21f7a0&pid=AdsNative\",\n"
                + "                    \"imageType\": \"AdvertiserUploadImage\"\n" + "                }\n"
                + "            ],\n" + "            \"decorations\": [\n" + "                {\n"
                + "                    \"_type\": \"Ads/CallExtension\",\n"
                + "                    \"PhoneNumber\": \"+1-800-888-9000\",\n"
                + "                    \"PhoneNumberUrl\": \"39400\"\n" + "                }\n" + "            ]\n"
                + "        }\n" + "        ]\n" + "    }";

        dcpmicrosoftadnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpmicrosoftadnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<!DOCTYPE html> <html> <head> <meta name='viewport' content='initial-scale=1,width=device-width,user-scalable=no'> <style type=\"text/css\"> body {margin: 0; padding: 0; } </style> <script type=\"text/javascript\"> window.onerror = function(e,url,l,c) {alert(\"error: \"+e + \", l:\" + l + \",  c:\" + c) } </script>  <script type=\"text/javascript\" src= \"http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js\"></script> <script type=\"text/javascript\"> window['im_2670_fireAdReady'] = function() {_im_imai.fireAdReady(); }; window['im_2670_fireAdFailed'] = function() {_im_imai.fireAdFailed(); }; window['im_2670_fireAutoAdReady'] = true; window['im_2670_disableAutoFireAdReady'] = function() {window['im_2670_fireAutoAdReady'] = false; }; var readyHandler = function(val) {if (window['im_2670_fireAutoAdReady']) {window['im_2670_fireAdReady'](); } }; mraid.addEventListener('ready', readyHandler); window['im_2670_replaceTimeStamp'] = function(url) {return url.replace(/\\$TS/g, new Date().getTime()); }; window['im_2670_openLandingPage'] = function() {var landingUrl = 'http://0.r.msn.com/?ld=link0'; var url = window['im_2670_replaceTimeStamp'](landingUrl); imraid.openExternal(url); }; window['im_2670_recordEvent'] = function(id, params) {var firePixel = function(source, retryTime, times) {if (times <= 0) {return; } var clickTarget = document.getElementById('im_2670_clickTarget'); var img = document.createElement('img'); img.setAttribute('src', source); img.setAttribute('height', '0'); img.setAttribute('width', '2'); if (img['addEventListener'] != undefined) {img.addEventListener('error', function() {window.setTimeout(function() {if (retryTime > 300000) {retryTime = 300000; } firePixel(source, retryTime * 2, times - 1); }, retryTime + Math.random()); }, false); } clickTarget.appendChild(img); }; var beacon = \"http://placehold.it/1x1\"; beacon += \"?m=\" + id; beacon += \"&t=\" + new Date().getTime(); if (params) {for (var key in params) {beacon += \"&\" + encodeURIComponent(key) + \"=\" + encodeURIComponent(params[key]); } } firePixel(beacon, 1000, 5); }; </script> </head> <body> <div style=\"display:none; position:absolute;\" id=\"im_2670_clickTarget\"></div> <style>.im_2670_bg{background:-webkit-linear-gradient(top,#333,#000);background:linear-gradient(top,#333,#000)}#im_2670_wrapperDiv.flip #im_2670_bannerContainer{transform:rotateX(180deg);-webkit-transform:rotateX(180deg);transform-origin:100% 25px;-webkit-transform-origin:100% 25px}#im_2670_wrapperDiv,.back,.front{width:320px;height:50px}#im_2670_bannerContainer{position:relative;transform-style:preserve-3d;-webkit-transform-style:preserve-3d;transform-origin:100% 25px;-webkit-transform-origin:100% 25px;transition:.6s;-webkit-transition:.6s}.back,.front{-webkit-backface-visibility:hidden;position:absolute;top:0;left:0}.front{z-index:2;background-color:#fff;opacity:.85;transform:rotateX(0deg);-webkit-transform:rotateX(0deg)}.back{z-index:0;background-color:#fff;opacity:.85;transform:rotateX(180deg);-webkit-transform:rotateX(180deg)}#im_2670_bigOverlay{position:absolute;display:inline-block;float:left;width:230px;height:50px;z-index:5}#im_2670_smallOverlay{position:absolute;display:inline-block;float:right;width:90px;height:50px;z-index:5}#im_2670_text{display:inline-block}#im_2670_firstLine{font-family:'Segoe UI',Frutiger,'Frutiger Linotype','Dejavu Sans','Helvetica Neue',Arial,sans-serif;font-size:15px;color:#4d4d4d;position:relative;top:6px;padding-left:10px;width:220px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis}#im_2670_secondLine{position:relative;top:10px;padding-left:10px;width:220px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;font-family:'Segoe UI',Frutiger,'Frutiger Linotype','Dejavu Sans','Helvetica Neue',Arial,sans-serif;font-size:13px;color:#4d4d4d;vertical-align:middle}#im_2670_imgTag{width:15px;height:15px;vertical-align:middle}#im_2670_image{display:inline-block;float:right;width:90px;height:50px}.fitted img:first-child{min-width:100%;max-height:100%}</style> <div id=\"im_2670_wrapperDiv\"> <div id=\"im_2670_bannerContainer\"> <div class=\"front\"> <div id=\"im_2670_bigOverlay\"></div> <div id=\"im_2670_text\"> <div id=\"im_2670_firstLine\">The 2015 BMW X5</div> <div id=\"im_2670_secondLine\"><span id=\"im_2670_secondLineImage\"><img id=\"im_2670_imgTag\" src=\"http://www.bing.com/th?id=A8ep0xDJzbQjiZw16x16&pid=AdsPlus\"></span> <span id=\"im_2670_textSecondLine\">bmw.com/BMW-X5\u200E | Sponsored</span></div> </div> <div id=\"im_2670_smallOverlay\"></div> <div id=\"im_2670_image\" class=\"fitted\"><img src=\"https://i.l.inmobicdn.net/banners/programmatic/call.png\"> </div> </div> <div class=\"back\"> <div id=\"im_2670_bigOverlay\"></div> <div id=\"im_2670_text\"> <div id=\"im_2670_firstLine\">The X5 Can Keep Up with Anything. Even You. Book a Test Drive Online</div> <div id=\"im_2670_secondLine\"><span id=\"im_2670_secondLineImage\"><img id=\"im_2670_imgTag\" src=\"http://www.bing.com/th?id=A8ep0xDJzbQjiZw16x16&pid=AdsPlus\"></span> <span id=\"im_2670_textSecondLine\">bmw.com/BMW-X5\u200E | Sponsored</span></div> </div> <div id=\"im_2670_smallOverlay\"></div> <div id=\"im_2670_image\" class=\"fitted\"><img src=\"https://i.l.inmobicdn.net/banners/programmatic/call.png\"> </div> </div> </div> </div> <script>!function(){function a(a){return g.getElementById(a)}function b(a,b,c){for(var d=i.length-1;d>=0;d--)c?a.addEventListener(i[d],b,!1):a.removeEventListener(i[d],b,!1)}function c(b){var c=\"object\"==typeof b?b:a(b);_this=this,_this.element=c,_this.moved=!1,_this.startX=0,_this.startY=0,_this.hteo=!1,c.addEventListener(j[0],_this,!1),c.addEventListener(j[1],_this,!1)}function d(a){var b=new XMLHttpRequest;b.open(\"GET\",a,!0),b.send(null)}function e(a){d(\"clickUrl\"),window.clearInterval(flipInterval),a.target.id==h+\"bigOverlay\"?imraid&&imraid.openExternal&&\"function\"==typeof imraid.openExternal?imraid.openExternal(\"http://www.bmw.com/BMW-X5\u200E\"):mraid&&mraid.openExternal&&\"function\"==typeof mraid.openExternal?mraid.openExternal(\"http://www.bmw.com/BMW-X5\u200E\"):window.open(\"http://www.bmw.com/BMW-X5\u200E\"):a.target.id==h+\"smallOverlay\"&&(imraid&&imraid.openExternal&&\"function\"==typeof imraid.openExternal?imraid.openExternal(\"tel:+1-800-888-9000\"):mraid&&mraid.openExternal&&\"function\"==typeof mraid.openExternal?mraid.openExternal(\"tel:+1-800-888-9000\"):window.open(\"tel:+1-800-888-9000\"))}var f=window,g=document,h=\"im_2670_\",i=(f[h+\"recordEvent\"],f[h+\"openLandingPage\"],[\"touchmove\",\"touchend\",\"touchcancel\",\"mousemove\",\"mouseup\"]),j=[\"touchstart\",\"mousedown\"];c.prototype.start=function(a){var c=this.element;a.type===j[0]&&(this.hteo=!0),this.moved=!1,this.startX=a.type===j[0]?a.touches[0].clientX:a.clientX,this.startY=a.type===j[0]?a.touches[0].clientY:a.clientY,b(c,this,!0)},c.prototype.move=function(a){var b=a.type===i[0]?a.touches[0].clientX:a.clientX,c=a.type===i[0]?a.touches[0].clientY:a.clientY;(Math.abs(b-this.startX)>10||Math.abs(c-this.startY)>10)&&(this.moved=!0)},c.prototype.end=function(a){var c,d=this.element;if(this.hteo&&a.type===i[4])return a.preventDefault(),a.stopPropagation(),void(this.hteo=!1);if(!this.moved){var e=a.type===i[1]?a.changedTouches[0].clientX:a.clientX,f=a.type===i[1]?a.changedTouches[0].clientY:a.clientY;if(\"function\"==typeof CustomEvent)c=new CustomEvent(\"tap\",{bubbles:!0,cancelable:!0,detail:{x:e,y:f}});else try{c=g.createEvent(\"CustomEvent\"),c.initCustomEvent(\"tap\",!0,!0,{x:e,y:f})}catch(h){c=g.createEvent(\"Event\"),c.initEvent(\"tap\",!0,!0)}a.target.dispatchEvent(c)}b(d,this,!1)},c.prototype.cancel=function(a){var c=this.element;this.moved=!1,this.startX=0,this.startY=0,b(c,this,!1)},c.prototype.handleEvent=function(a){switch(_this=this,a.type){case j[0]:_this.start(a);break;case i[0]:_this.move(a);break;case i[1]:_this.end(a);break;case i[2]:_this.cancel(a);break;case j[1]:_this.start(a);break;case i[3]:_this.move(a);break;case i[4]:_this.end(a)}},g.addEventListener(i[0],function(a){a.preventDefault()}),clickElement=a(h+\"wrapperDiv\"),bannerContainer=a(h+\"bannerContainer\"),textDiv=a(h+\"text\"),image=a(h+\"image\"),imgTag=a(h+\"imgTag\"),text=\"The 2015 BMW X5\",flipInterval=window.setInterval(function(){clickElement.classList.toggle(\"flip\")},2500),clickElement&&(new c(clickElement),clickElement.addEventListener(\"tap\",e,!1),d(\"beaconUrl\"))}();</script> </body> </html>\n",
            dcpmicrosoftadnetwork.getHttpResponseContent());

        dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15,
            repositoryHelper);
        response =
            "{\n" + "    \"_type\": \"Ads/PaidSearchForNative/AdsResponse/TwoClickAd\",\n"
                + "    \"targetId\": \"endOfArticle\",\n" + "    \"templateId\": \"Mobile\",\n"
                + "    \"intentLinks\": [\n" + "        {\n" + "            \"title\": \"Island Hopping Cruises\",\n"
                + "            \"targetUrl\": \"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\",\n"
                + "            \"image\": {\n"
                + "                \"imageUrl\": \"\",\n"
                + "                \"imageType\": \"AdvertiserUploadImage\",\n" + "                \"altText\": \"\"\n"
                + "            }\n" + "        }\n" + "    ]\n" + "}";

        dcpmicrosoftadnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpmicrosoftadnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<!DOCTYPE html> <html> <head> <meta name='viewport' content='initial-scale=1,width=device-width,user-scalable=no'> <style type=\"text/css\"> body {margin: 0; padding: 0; } </style> <script type=\"text/javascript\"> window.onerror = function(e, url, l, c) {alert(\"error: \" + e + \", l:\" + l + \",  c:\" + c) } </script> <script type=\"text/javascript\" src= \"http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js\"></script> <script type=\"text/javascript\"> window['im_2670_fireAdReady'] = function() {_im_imai.fireAdReady(); }; window['im_2670_fireAdFailed'] = function() {_im_imai.fireAdFailed(); }; window['im_2670_fireAutoAdReady'] = true; window['im_2670_disableAutoFireAdReady'] = function() {window['im_2670_fireAutoAdReady'] = false; }; var readyHandler = function(val) {if (window['im_2670_fireAutoAdReady']) {window['im_2670_fireAdReady'](); } }; mraid.addEventListener('ready', readyHandler); window['im_2670_replaceTimeStamp'] = function(url) {return url.replace(/\\$TS/g, new Date().getTime()); }; window['im_2670_openLandingPage'] = function() {var landingUrl = 'http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908'; var url = window['im_2670_replaceTimeStamp'](landingUrl); imraid.openExternal(url); }; window['im_2670_recordEvent'] = function(id, params) {var firePixel = function(source, retryTime, times) {if (times <= 0) {return; } var clickTarget = document.getElementById('im_2670_clickTarget'); var img = document.createElement('img'); img.setAttribute('src', source); img.setAttribute('height', '0'); img.setAttribute('width', '2'); if (img['addEventListener'] != undefined) {img.addEventListener('error', function() {window.setTimeout(function() {if (retryTime > 300000) {retryTime = 300000; } firePixel(source, retryTime * 2, times - 1); }, retryTime + Math.random()); }, false); } clickTarget.appendChild(img); }; var beacon = \"http://placehold.it/1x1\"; beacon += \"?m=\" + id; beacon += \"&t=\" + new Date().getTime(); if (params) {for (var key in params) {beacon += \"&\" + encodeURIComponent(key) + \"=\" + encodeURIComponent(params[key]); } } firePixel(beacon, 1000, 5); }; </script> </head> <body> <div style=\"display:none; position:absolute;\" id=\"im_2670_clickTarget\"></div> <style> .im_2670_bg {background: -webkit-linear-gradient(top, #333, #000); background: linear-gradient(top, #333, #000) } #im_2670_wrapperDiv {background-color:#FFFFFF ;opacity:1; width: 320px; height: 50px } #im_2670_bannerContainer {position: relative } #im_2670_bigOverlay {position: absolute; display: inline-block; float: left; width: 320px; height: 50px; z-index: 5 } #im_2670_text {display: inline-block } #im_2670_ad {display: inline-block; position: relative; top: 5px; right: 5px; float:right; font-family: 'Segoe UI', Frutiger, 'Frutiger Linotype', 'Dejavu Sans', 'Helvetica Neue', Arial, sans-serif; font-size: 10px; color: #9b9b9b } #im_2670_firstLine {display: block; display: -webkit-box; font-family: 'Segoe UI', Frutiger, 'Frutiger Linotype', 'Dejavu Sans', 'Helvetica Neue', Arial, sans-serif; font-size: 17px; color: #4d4d4d; position: relative; top: 1px; padding-left: 10px; width: 200px; margin: 0 auto; line-height: 25px; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis } #im_2670_imgTag {width: 20px; height: 20px; vertical-align: middle } #im_2670_image {display: inline-block; float: right; width: 90px; height: 50px } .fitted img:first-child {min-width: 100%; max-height: 100% } </style> <div id=\"im_2670_wrapperDiv\"> <div id=\"im_2670_bannerContainer\"> <div id=\"im_2670_bigOverlay\"></div> <div id=\"im_2670_text\"> <div id=\"im_2670_firstLine\">Island Hopping Cruises</div> </div>  <div id=\"im_2670_ad\">Ad</div> </div> </div> <script> ! function() {function a(a) {return g.getElementById(a) } function b(a, b, c) {for (var d = i.length - 1; d >= 0; d--) c ? a.addEventListener(i[d], b, !1) : a.removeEventListener(i[d], b, !1) } function c(b) {var c = \"object\" == typeof b ? b : a(b); _this = this, _this.element = c, _this.moved = !1, _this.startX = 0, _this.startY = 0, _this.hteo = !1, c.addEventListener(j[0], _this, !1), c.addEventListener(j[1], _this, !1) } function d(a) {var b = new XMLHttpRequest; b.open(\"GET\", a, !0), b.send(null) } function e(a) {d(\"clickUrl\"), a.target.id == h + \"bigOverlay\" && (imraid && imraid.openExternal && \"function\" == typeof imraid.openExternal ? imraid.openExternal(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\") : mraid && mraid.openExternal && \"function\" == typeof mraid.openExternal ? mraid.openExternal(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\") : window.open(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\")) } var f = window, g = document, h = \"im_2670_\", i = (f[h + \"recordEvent\"], f[h + \"openLandingPage\"], [\"touchmove\", \"touchend\", \"touchcancel\", \"mousemove\", \"mouseup\"]), j = [\"touchstart\", \"mousedown\"]; c.prototype.start = function(a) {var c = this.element; a.type === j[0] && (this.hteo = !0), this.moved = !1, this.startX = a.type === j[0] ? a.touches[0].clientX : a.clientX, this.startY = a.type === j[0] ? a.touches[0].clientY : a.clientY, b(c, this, !0) }, c.prototype.move = function(a) {var b = a.type === i[0] ? a.touches[0].clientX : a.clientX, c = a.type === i[0] ? a.touches[0].clientY : a.clientY; (Math.abs(b - this.startX) > 10 || Math.abs(c - this.startY) > 10) && (this.moved = !0) }, c.prototype.end = function(a) {var c, d = this.element; if (this.hteo && a.type === i[4]) return a.preventDefault(), a.stopPropagation(), void(this.hteo = !1); if (!this.moved) {var e = a.type === i[1] ? a.changedTouches[0].clientX : a.clientX, f = a.type === i[1] ? a.changedTouches[0].clientY : a.clientY; if (\"function\" == typeof CustomEvent) c = new CustomEvent(\"tap\", {bubbles: !0, cancelable: !0, detail: {x: e, y: f } }); else try {c = g.createEvent(\"CustomEvent\"), c.initCustomEvent(\"tap\", !0, !0, {x: e, y: f }) } catch (h) {c = g.createEvent(\"Event\"), c.initEvent(\"tap\", !0, !0) } a.target.dispatchEvent(c) } b(d, this, !1) }, c.prototype.cancel = function(a) {var c = this.element; this.moved = !1, this.startX = 0, this.startY = 0, b(c, this, !1) }, c.prototype.handleEvent = function(a) {switch (_this = this, a.type) {case j[0]: _this.start(a); break; case i[0]: _this.move(a); break; case i[1]: _this.end(a); break; case i[2]: _this.cancel(a); break; case j[1]: _this.start(a); break; case i[3]: _this.move(a); break; case i[4]: _this.end(a) } }, g.addEventListener(i[0], function(a) {a.preventDefault() }), clickElement = a(h + \"wrapperDiv\"), bannerContainer = a(h + \"bannerContainer\"), textDiv = a(h + \"text\"), image = a(h + \"image\"), imgTag = a(h + \"imgTag\"), text = \"Island Hopping Cruises\", clickElement && (new c(clickElement), clickElement.addEventListener(\"tap\", e, !1), d(\"beaconUrl\")) }(); </script> </body> </html>\n",
            dcpmicrosoftadnetwork.getHttpResponseContent());

        dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 10,
            repositoryHelper);
        response =
            "{\n" + "    \"_type\": \"Ads/PaidSearchForNative/AdsResponse/TwoClickAd\",\n"
                + "    \"targetId\": \"endOfArticle\",\n" + "    \"templateId\": \"Mobile\",\n"
                + "    \"intentLinks\": [\n" + "        {\n" + "            \"title\": \"Island Hopping Cruises\",\n"
                + "            \"targetUrl\": \"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\",\n"
                + "            \"image\": {\n"
                + "                \"imageUrl\": \"https://www.bing.com/th?id=OAIP.095201a525482a209adef1190d21f7a0&pid=AdsNative\",\n"
                + "                \"imageType\": \"AdvertiserUploadImage\",\n" + "                \"altText\": \"\"\n"
                + "            }\n" + "        }\n" + "    ]\n" + "}";

        dcpmicrosoftadnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpmicrosoftadnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<!DOCTYPE html> <html> <head> <meta name='viewport' content='initial-scale=1,width=device-width,user-scalable=no'> <style type=\"text/css\"> body {margin: 0; padding: 0; } </style> <script type=\"text/javascript\"> window.onerror = function(e,url,l,c) {alert(\"error: \"+e + \", l:\" + l + \",  c:\" + c) } </script>  <script type=\"text/javascript\" src= \"http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js\"></script> <script type=\"text/javascript\"> window['im_2670_fireAdReady'] = function() {_im_imai.fireAdReady(); }; window['im_2670_fireAdFailed'] = function() {_im_imai.fireAdFailed(); }; window['im_2670_fireAutoAdReady'] = true; window['im_2670_disableAutoFireAdReady'] = function() {window['im_2670_fireAutoAdReady'] = false; }; var readyHandler = function(val) {if (window['im_2670_fireAutoAdReady']) {window['im_2670_fireAdReady'](); } }; mraid.addEventListener('ready', readyHandler); window['im_2670_replaceTimeStamp'] = function(url) {return url.replace(/\\$TS/g, new Date().getTime()); }; window['im_2670_openLandingPage'] = function() {var landingUrl = '$PartnerClickUrl'; var url = window['im_2670_replaceTimeStamp'](landingUrl); imraid.openExternal(url); }; window['im_2670_recordEvent'] = function(id, params) {var firePixel = function(source, retryTime, times) {if (times <= 0) {return; } var clickTarget = document.getElementById('im_2670_clickTarget'); var img = document.createElement('img'); img.setAttribute('src', source); img.setAttribute('height', '0'); img.setAttribute('width', '2'); if (img['addEventListener'] != undefined) {img.addEventListener('error', function() {window.setTimeout(function() {if (retryTime > 300000) {retryTime = 300000; } firePixel(source, retryTime * 2, times - 1); }, retryTime + Math.random()); }, false); } clickTarget.appendChild(img); }; var beacon = \"http://placehold.it/1x1\"; beacon += \"?m=\" + id; beacon += \"&t=\" + new Date().getTime(); if (params) {for (var key in params) {beacon += \"&\" + encodeURIComponent(key) + \"=\" + encodeURIComponent(params[key]); } } firePixel(beacon, 1000, 5); }; </script> </head> <body> <div style=\"display:none; position:absolute;\" id=\"im_2670_clickTarget\"></div> <style>.im_2670_bg{background:-webkit-linear-gradient(top,#333,#000);background:linear-gradient(top,#333,#000)}#im_2670_wrapperDiv{background-color:#FFFFFF;opacity:1;width:300px;height:250px}#im_2670_bannerContainer{position:relative}#im_2670_bigOverlay{position:absolute;float:left;width:300px;height:250px;z-index:5}#im_2670_sponsored{position:absolute;top:0;left:10px;font-family:'Segoe UI',Frutiger,'Frutiger Linotype','Dejavu Sans','Helvetica Neue',Arial,sans-serif;font-size:14px;color:#9b9b9b}#im_2670_firstLine{display:block;display:-webkit-box;font-family:'Segoe UI',Frutiger,'Frutiger Linotype','Dejavu Sans','Helvetica Neue',Arial,sans-serif;font-size:25px;color:#4d4d4d;position:relative;top:25px;padding-left:17px;width:300px;margin:0 auto;line-height:40px;-webkit-line-clamp:1;-webkit-box-orient:vertical;overflow:hidden;text-overflow:ellipsis}#im_2670_image{position:relative;top:17px;width:300px;height:175px}.fitted img:first-child{min-width:100%;max-height:100%}</style> <div id=\"im_2670_wrapperDiv\"> <div id=\"im_2670_bannerContainer\"> <div id=\"im_2670_bigOverlay\"></div> <div id=\"im_2670_sponsored\">Sponsored</div> <div id=\"im_2670_image\" class=\"fitted\"><img src=\"https://www.bing.com/th?id=OAIP.095201a525482a209adef1190d21f7a0&pid=AdsNative\"></div> <div id=\"im_2670_firstLine\">Island Hopping Cruises</div> </div> </div> <script>!function(){function a(a){return g.getElementById(a)}function b(a,b,c){for(var d=i.length-1;d>=0;d--)c?a.addEventListener(i[d],b,!1):a.removeEventListener(i[d],b,!1)}function c(b){var c=\"object\"==typeof b?b:a(b);_this=this,_this.element=c,_this.moved=!1,_this.startX=0,_this.startY=0,_this.hteo=!1,c.addEventListener(j[0],_this,!1),c.addEventListener(j[1],_this,!1)}function d(a){var b=new XMLHttpRequest;b.open(\"GET\",a,!0),b.send(null)}function e(a){d(\"clickUrl\"),a.target.id==h+\"bigOverlay\"&&(imraid&&imraid.openExternal&&\"function\"==typeof imraid.openExternal?imraid.openExternal(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\"):mraid&&mraid.openExternal&&\"function\"==typeof mraid.openExternal?mraid.openExternal(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\"):window.open(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\"))}var f=window,g=document,h=\"im_2670_\",i=(f[h+\"recordEvent\"],f[h+\"openLandingPage\"],[\"touchmove\",\"touchend\",\"touchcancel\",\"mousemove\",\"mouseup\"]),j=[\"touchstart\",\"mousedown\"];c.prototype.start=function(a){var c=this.element;a.type===j[0]&&(this.hteo=!0),this.moved=!1,this.startX=a.type===j[0]?a.touches[0].clientX:a.clientX,this.startY=a.type===j[0]?a.touches[0].clientY:a.clientY,b(c,this,!0)},c.prototype.move=function(a){var b=a.type===i[0]?a.touches[0].clientX:a.clientX,c=a.type===i[0]?a.touches[0].clientY:a.clientY;(Math.abs(b-this.startX)>10||Math.abs(c-this.startY)>10)&&(this.moved=!0)},c.prototype.end=function(a){var c,d=this.element;if(this.hteo&&a.type===i[4])return a.preventDefault(),a.stopPropagation(),void(this.hteo=!1);if(!this.moved){var e=a.type===i[1]?a.changedTouches[0].clientX:a.clientX,f=a.type===i[1]?a.changedTouches[0].clientY:a.clientY;if(\"function\"==typeof CustomEvent)c=new CustomEvent(\"tap\",{bubbles:!0,cancelable:!0,detail:{x:e,y:f}});else try{c=g.createEvent(\"CustomEvent\"),c.initCustomEvent(\"tap\",!0,!0,{x:e,y:f})}catch(h){c=g.createEvent(\"Event\"),c.initEvent(\"tap\",!0,!0)}a.target.dispatchEvent(c)}b(d,this,!1)},c.prototype.cancel=function(a){var c=this.element;this.moved=!1,this.startX=0,this.startY=0,b(c,this,!1)},c.prototype.handleEvent=function(a){switch(_this=this,a.type){case j[0]:_this.start(a);break;case i[0]:_this.move(a);break;case i[1]:_this.end(a);break;case i[2]:_this.cancel(a);break;case j[1]:_this.start(a);break;case i[3]:_this.move(a);break;case i[4]:_this.end(a)}},g.addEventListener(i[0],function(a){a.preventDefault()}),clickElement=a(h+\"wrapperDiv\"),bannerContainer=a(h+\"bannerContainer\"),textDiv=a(h+\"text\"),image=a(h+\"image\"),imgTag=a(h+\"imgTag\"),text=\"Island Hopping Cruises\",clickElement&&(new c(clickElement),clickElement.addEventListener(\"tap\",e,!1),d(\"beaconUrl\"))}();</script> </body> </html>\n",
            dcpmicrosoftadnetwork.getHttpResponseContent());

        dcpmicrosoftadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14,
            repositoryHelper);
        response =
                "{ \n" + "\n" + "\"_type\" : \"Ads/PaidSearchForNative/AdsResponse/TwoClickAd\", \n" + "\n"
                    + "\"targetId\" : \"endOfArticle\", \n" + "\n" + "\"templateId\" : \"Mobile\", \n" + "\n"
                    + "\"intentLinks\" : [{ \n" + "\n" + "\"title\" : \"Island Hopping Cruises\", \n" + "\n"
                    + "\"targetUrl\" : \"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\", \n"
                    + "\n" + "\"image\" : { \n" + "\n"
                    + "\"imageUrl\" : \"https://www.bing.com/th?id=OAIP.095201a525482a209adef1190d21f7a0&pid=AdsNative\", \n"
                    + "\n" + "\"imageType\" : \"AdvertiserUploadImage\", \n" + "\n" + "\"altText\" : \"\" \n" + "\n"
                    + "} \n" + "\n" + "} \n" +",{ \n" + "\n" + "\"title\" : \"Island Hopping Cruises\", \n" + "\n"
                    + "\"targetUrl\" : \"http://www.google.com\", \n"
                    + "\n" + "\"image\" : { \n" + "\n"
                    + "\"imageUrl\" : \"http://goingluxury.com/wp-content/uploads/2015/10/2-bed-villa-300x145.jpg\", \n"
                    + "\n" + "\"imageType\" : \"AdvertiserUploadImage\", \n" + "\n" + "\"altText\" : \"\" \n" + "\n"
                    + "} \n" + "\n" + "} "+ "\n" + "] \n" + "\n" + "} ";

        dcpmicrosoftadnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpmicrosoftadnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<!DOCTYPE html> <html> <head> <meta name='viewport' content='initial-scale=1,width=device-width,user-scalable=no'> <style type=\"text/css\"> body {margin: 0; padding: 0; } </style> <script type=\"text/javascript\"> window.onerror = function(e,url,l,c) {alert(\"error: \"+e + \", l:\" + l + \",  c:\" + c) } </script>  <script type=\"text/javascript\" src= \"http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js\"></script> <script type=\"text/javascript\"> window['im_2670_fireAdReady'] = function() {_im_imai.fireAdReady(); }; window['im_2670_fireAdFailed'] = function() {_im_imai.fireAdFailed(); }; window['im_2670_fireAutoAdReady'] = true; window['im_2670_disableAutoFireAdReady'] = function() {window['im_2670_fireAutoAdReady'] = false; }; var readyHandler = function(val) {if (window['im_2670_fireAutoAdReady']) {window['im_2670_fireAdReady'](); } }; mraid.addEventListener('ready', readyHandler); window['im_2670_replaceTimeStamp'] = function(url) {return url.replace(/\\$TS/g, new Date().getTime()); }; window['im_2670_openLandingPage'] = function() {var landingUrl = 'http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908'; var url = window['im_2670_replaceTimeStamp'](landingUrl); imraid.openExternal(url); }; window['im_2670_recordEvent'] = function(id, params) {var firePixel = function(source, retryTime, times) {if (times <= 0) {return; } var clickTarget = document.getElementById('im_2670_clickTarget'); var img = document.createElement('img'); img.setAttribute('src', source); img.setAttribute('height', '0'); img.setAttribute('width', '2'); if (img['addEventListener'] != undefined) {img.addEventListener('error', function() {window.setTimeout(function() {if (retryTime > 300000) {retryTime = 300000; } firePixel(source, retryTime * 2, times - 1); }, retryTime + Math.random()); }, false); } clickTarget.appendChild(img); }; var beacon = \"http://placehold.it/1x1\"; beacon += \"?m=\" + id; beacon += \"&t=\" + new Date().getTime(); if (params) {for (var key in params) {beacon += \"&\" + encodeURIComponent(key) + \"=\" + encodeURIComponent(params[key]); } } firePixel(beacon, 1000, 5); }; </script> </head> <body> <div style=\"display:none; position:absolute;\" id=\"im_2670_clickTarget\"></div> <style>.im_2670_bg{background:-webkit-linear-gradient(top,#333,#000);background:linear-gradient(top,#333,#000)}#im_2670_wrapperDiv{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);-webkit-transform:translate(-50%,-50%);width:320px;height:480px;background-color:rgba(0,0,0,.75)}#im_2670_firstCard{position:relative;top:30px;width:300px;height:205px;background-color:#fff;margin:0 auto}#im_2670_secondCard{position:relative;top:50px;width:300px;height:205px;background-color:#fff;margin:0 auto}#im_2670_firstOverlay,#im_2670_secondOverlay{position:absolute;float:left;width:300px;height:205px;z-index:5}.im_2670_sponsored{position:absolute;top:3px;left:10px;font-family:'Segoe UI',Frutiger,'Frutiger Linotype','Dejavu Sans','Helvetica Neue',Arial,sans-serif;font-size:14px;color:#9b9b9b}.im_2670_firstLine{display:-webkit-box;font-family:'Segoe UI',Frutiger,'Frutiger Linotype','Dejavu Sans','Helvetica Neue',Arial,sans-serif;font-size:20px;color:#4d4d4d;position:relative;top:28px;left:10px;width:280px;line-height:23px;-webkit-line-clamp:1;-webkit-box-orient:vertical;overflow:hidden;text-overflow:ellipsis}#im_2670_closeButton{position:absolute;right:10px;top:5px;width:18px;height:18px}.im_2670_firstImage{display:inline-block;position:relative;top:23px;width:300px;height:145px}.fitted img:first-child{width:100%;height:100%}</style> <div id=\"im_2670_wrapperDiv\"> <div id=\"im_2670_closeButton\" class=\"fitted\"><img src=\"https://upload.wikimedia.org/wikipedia/commons/thumb/8/89/Media_Viewer_Icon_-_Close.svg/2000px-Media_Viewer_Icon_-_Close.svg.png\"></div> <div id=\"im_2670_firstCard\"> <div id=\"im_2670_firstOverlay\"></div> <div class=\"im_2670_sponsored\">Sponsored</div> <div class=\"im_2670_firstImage fitted\"><img src=\"https://www.bing.com/th?id=OAIP.095201a525482a209adef1190d21f7a0&pid=AdsNative\"></div> <div class=\"im_2670_firstLine\">Island Hopping Cruises</div> </div> <div id=\"im_2670_secondCard\"> <div id=\"im_2670_secondOverlay\"></div> <div class=\"im_2670_sponsored\">Sponsored</div> <div class=\"im_2670_firstImage fitted\"><img src=\"http://goingluxury.com/wp-content/uploads/2015/10/2-bed-villa-300x145.jpg\"></div> <div class=\"im_2670_firstLine\">Island Hopping Cruises</div> </div> </div> <script>!function(){function a(a){return h.getElementById(a)}function b(a,b,c){for(var d=j.length-1;d>=0;d--)c?a.addEventListener(j[d],b,!1):a.removeEventListener(j[d],b,!1)}function c(b){var c=\"object\"==typeof b?b:a(b);_this=this,_this.element=c,_this.moved=!1,_this.startX=0,_this.startY=0,_this.hteo=!1,c.addEventListener(k[0],_this,!1),c.addEventListener(k[1],_this,!1)}function d(a){var b=new XMLHttpRequest;b.open(\"GET\",a,!0),b.send(null)}function e(a){d(\"clickUrl\"),a.target.id==i+\"firstOverlay\"?(d(\"clickUrl\"),imraid&&imraid.openExternal&&\"function\"==typeof imraid.openExternal?imraid.openExternal(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\"):mraid&&mraid.openExternal&&\"function\"==typeof mraid.openExternal?mraid.openExternal(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\"):window.open(\"http://www.bing.com/moreads?q=Island%20Hopping%20Cruises&reqId=d465c88cbee8463080bc81a819a90efb&form=PSN910&adpk=11579908\")):a.target.id==i+\"secondOverlay\"&&(d(\"clickUrl\"),imraid&&imraid.openExternal&&\"function\"==typeof imraid.openExternal?imraid.openExternal(\"http://www.google.com\"):mraid&&mraid.openExternal&&\"function\"==typeof mraid.openExternal?mraid.openExternal(\"http://www.google.com\"):window.open(\"http://www.google.com\"))}function f(a){a.stopPropagation(),imraid&&imraid.openExternal&&\"function\"==typeof imraid.openExternal?imraid.close():mraid&&mraid.openExternal&&\"function\"==typeof mraid.openExternal&&mraid.close()}var g=window,h=document,i=\"im_2670_\",j=(g[i+\"recordEvent\"],g[i+\"openLandingPage\"],[\"touchmove\",\"touchend\",\"touchcancel\",\"mousemove\",\"mouseup\"]),k=[\"touchstart\",\"mousedown\"];c.prototype.start=function(a){var c=this.element;a.type===k[0]&&(this.hteo=!0),this.moved=!1,this.startX=a.type===k[0]?a.touches[0].clientX:a.clientX,this.startY=a.type===k[0]?a.touches[0].clientY:a.clientY,b(c,this,!0)},c.prototype.move=function(a){var b=a.type===j[0]?a.touches[0].clientX:a.clientX,c=a.type===j[0]?a.touches[0].clientY:a.clientY;(Math.abs(b-this.startX)>10||Math.abs(c-this.startY)>10)&&(this.moved=!0)},c.prototype.end=function(a){var c,d=this.element;if(this.hteo&&a.type===j[4])return a.preventDefault(),a.stopPropagation(),void(this.hteo=!1);if(!this.moved){var e=a.type===j[1]?a.changedTouches[0].clientX:a.clientX,f=a.type===j[1]?a.changedTouches[0].clientY:a.clientY;if(\"function\"==typeof CustomEvent)c=new CustomEvent(\"tap\",{bubbles:!0,cancelable:!0,detail:{x:e,y:f}});else try{c=h.createEvent(\"CustomEvent\"),c.initCustomEvent(\"tap\",!0,!0,{x:e,y:f})}catch(g){c=h.createEvent(\"Event\"),c.initEvent(\"tap\",!0,!0)}a.target.dispatchEvent(c)}b(d,this,!1)},c.prototype.cancel=function(a){var c=this.element;this.moved=!1,this.startX=0,this.startY=0,b(c,this,!1)},c.prototype.handleEvent=function(a){switch(_this=this,a.type){case k[0]:_this.start(a);break;case j[0]:_this.move(a);break;case j[1]:_this.end(a);break;case j[2]:_this.cancel(a);break;case k[1]:_this.start(a);break;case j[3]:_this.move(a);break;case j[4]:_this.end(a)}},h.addEventListener(j[0],function(a){a.preventDefault()}),clickElement=a(i+\"wrapperDiv\"),bannerContainer=a(i+\"bannerContainer\"),closeButton=a(i+\"closeButton\"),imraid&&imraid.useCustomClose&&\"function\"==typeof imraid.useCustomClose?imraid.useCustomClose(!0):mraid&&mraid.useCustomClose&&\"function\"==typeof mraid.useCustomClose&&mraid.useCustomClose(!0),closeButton&&(new c(closeButton),closeButton.addEventListener(\"tap\",f,!1)),clickElement&&(new c(clickElement),clickElement.addEventListener(\"tap\",e,!1),d(\"beaconUrl\"))}();</script>   </body> </html>\n",
            dcpmicrosoftadnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMicrosoftGetName() throws Exception {
        assertEquals(dcpmicrosoftadnetwork.getName(), "microsoftDCP");
    }
}
