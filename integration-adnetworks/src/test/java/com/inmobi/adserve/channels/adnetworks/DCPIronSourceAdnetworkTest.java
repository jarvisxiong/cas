package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.ironsource.DCPIronSourceAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPIronSourceAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final Bootstrap clientBootstrap = null;
    private DCPIronSourceAdnetwork dcpIronSourceAdnetwork;
    private String IronSourceHost = "http://dynamic.mobilecore.com:80/api/v1?";
    private final String IronSourceStatus = "on";
    private final String IronsourceAdvId = "IronSourceadv1";
    private final String IronSourceTokenId = "w9pR1SCLuM54sziOtvJw";
    private final String placementId = "240";
    private final String fsPlacementId = "230";
    private RepositoryHelper repositoryHelper;


    public void prepareMockConfig() {
        mockConfig = EasyMock.createMock(Configuration.class);
        expect(mockConfig.getString("ironsource.host")).andReturn(IronSourceHost).anyTimes();
        expect(mockConfig.getString("ironsource.status")).andReturn(IronSourceStatus).anyTimes();
        expect(mockConfig.getString("ironsource.tokenid")).andReturn(IronSourceTokenId).anyTimes();
        expect(mockConfig.getString("ironsource.advertiserId")).andReturn(IronsourceAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        EasyMock.replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        Formatter.init();
        final Channel serverChannel = EasyMock.createMock(Channel.class);
        final HttpRequestHandlerBase base = EasyMock.createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);

        dcpIronSourceAdnetwork = new DCPIronSourceAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpIronSourceAdnetwork.setName("IronSource");
        
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        
        IronSourceHost = String.format(IronSourceHost, placementId);
        dcpIronSourceAdnetwork.setHost(IronSourceHost);
    }

    @Test
    public void testDCPIronSourceConfigureParameters() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        casInternalRequestParameters.setGpid("gpidtest123");
        casInternalRequestParameters.setUidIFA("uidifsa123");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(IronsourceAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Long>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        assertTrue(dcpIronSourceAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPIronSourceConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(IronsourceAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Long>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        assertFalse(dcpIronSourceAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPIronSourceConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(IronsourceAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Long>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        assertFalse(dcpIronSourceAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

  @Test
  public void testDCPIronSourceConfigureParametersBlankUA() {
      final SASRequestParameters sasParams = new SASRequestParameters();
      final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
      sasParams.setRemoteHostIp("206.29.182.240");
      sasParams.setUserAgent(" ");
      sasParams.setSiteContentType(ContentType.PERFORMANCE);
      casInternalRequestParameters.setLatLong("37.4429,-122.1514");
      sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
      final String externalKey = "f6wqjq1r5v";
      final ChannelSegmentEntity entity =
              new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(IronsourceAdvId, null, null, null,
                      0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                      null, false, false, false, false, false, false, false, false, false, false, null,
                      new ArrayList<Long>(), 0.0d, null, null, 32, new Integer[] {0}));
      AdapterTestHelper.setBeaconAndClickStubs();
      assertFalse(dcpIronSourceAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
  }

 @Test
 public void testIronSourceRequestUri() throws Exception {
     final SASRequestParameters sasParams = new SASRequestParameters();
     final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
     sasParams.setRemoteHostIp("206.29.182.240");
     sasParams.setUserAgent("Mozilla");
     sasParams.setOsId(3);
     casInternalRequestParameters.setLatLong("37.4429,-122.1514");
     sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
     sasParams.setOsMajorVersion("4.4");
     sasParams.setOsId(SASRequestParameters.HandSetOS.Android.getValue());
     casInternalRequestParameters.setUidADT("1");
     casInternalRequestParameters.setGpid("gpidtest123");
     casInternalRequestParameters.setUidIFA("uidifsa123");
     sasParams.setUserAgent("Mozilla");
     sasParams.setSiteContentType(ContentType.PERFORMANCE);
     sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
     final String externalKey = "66";
     final ChannelSegmentEntity entity =
             new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(IronsourceAdvId, null, null, null,
                     0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                     null, false, false, false, false, false, false, false, false, false, false, null,
                     new ArrayList<Long>(), 0.0d, null, null, 0, new Integer[] {0}));
     AdapterTestHelper.setBeaconAndClickStubs();
     if (dcpIronSourceAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper)) {
         final String actualUrl = dcpIronSourceAdnetwork.getRequestUri().toString();
         final String expectedUrl =
                 "http://dynamic.mobilecore.com:80/api/v1??siteid=66&token=w9pR1SCLuM54sziOtvJw&packageName=00000000-0000-0000-0000-000000000000&ip=206.29.182.240&osVersion=4.4&gaid=gpidtest123&platform=Android&ua=Mozilla";
         AdapterTestHelper.setBeaconAndClickStubs();
         assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
         assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
     }
 }
    @Test
    public void testDCPIronSourceParseResponseImgAppSDK360() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setGpid("gpidtest123");
        casInternalRequestParameters.setUidIFA("uidifsa123");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(IronsourceAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Long>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpIronSourceAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper);
        final String response =
                "{\"error\":false,\"error_message\":\"\",\"ads\":[{\"packageName\":\"com.innogames.foeandroid\",\"title\":\"Forge of Empires\",\"description\":\"Take control over a city and become the leader of an aspiring kingdom.\",\"creatives\":{\"img\":\"http://cdn.castplatform.com/images/84d98bfe-5cbc-4c9f-924a-9de47d441aae.jpg\",\"banner\":\"http://cdn.castplatform.com/images/67b8394c-e375-4c4b-9e28-ab85e3cbe0d8.jpg\",\"banner300x250\":\"http://cdn.castplatform.com/images/bd1d414a-fa85-404a-8ada-ec407b4ce18e.jpg\",\"banner320x480\":\"http://cdn.castplatform.com/images/26b54dd5-5e1b-495a-8273-2b245b697d30.jpg\",\"banner480x320\":\"http://cdn.castplatform.com/images/ac491844-ce1e-4687-8b61-1eee98ecdd42.jpg\"},\"impressionURL\":\"http://thor.mobilecore.com/stats/impression?aff_id=66&carrier=00000000-000c-905a-0004-f212e8baef8e&accountid=48F0B&appid=com.innogames.foeandroid&offer_id=101870\",\"pricingModel\":\"cpi\",\"bid\":3.04,\"categories\":[\"GAME_STRATEGY\"],\"clickURL\":\"http://media.mobilecore.com/get?t=d&aff_id=66&packageName=00000000-000c-905a-0004-f212e8baef8e&id=101870&fos=Android&deviceId=gpid80571d65720efasdfaf\"}]}";
        dcpIronSourceAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpIronSourceAdnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://media.mobilecore.com/get?t=d&aff_id=66&packageName=00000000-000c-905a-0004-f212e8baef8e&id=101870&fos=Android&deviceId=gpid80571d65720efasdfaf' onclick=\"document.getElementById('click').src='clickUrl';imraid.openExternal('http://media.mobilecore.com/get?t=d&aff_id=66&packageName=00000000-000c-905a-0004-f212e8baef8e&id=101870&fos=Android&deviceId=gpid80571d65720efasdfaf'); return false;\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://cdn.castplatform.com/images/26b54dd5-5e1b-495a-8273-2b245b697d30.jpg'  /></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpIronSourceAdnetwork.getHttpResponseContent());
    }


    @Test
    public void testDCPIronSourceParseNoAd() throws Exception {
        final String response = "{\"error\":true}";
        dcpIronSourceAdnetwork.parseResponse(response,HttpResponseStatus.INTERNAL_SERVER_ERROR);
        assertEquals(dcpIronSourceAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpIronSourceAdnetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPIronSourceGetTokenId() throws Exception {
        assertEquals(dcpIronSourceAdnetwork.getToken(), IronSourceTokenId);
    }

    @Test
    public void testDCPIronSourceGetName() throws Exception {
        assertEquals(dcpIronSourceAdnetwork.getName(), "ironsourceDCP");
    }
}
