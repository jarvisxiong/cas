package com.inmobi.adserve.channels.adnetworks;

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
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static org.easymock.EasyMock.expect;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPIronSourceAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final Bootstrap clientBootstrap = null;
    private DCPIronSourceAdnetwork dcpIronSourceAdnetwork;
    private String IronSourceHost = "http://dev.dynamic.mobilecore.com:80/api/v1?";
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
                 "http://dev.dynamic.mobilecore.com:80/api/v1??siteid=66&token=w9pR1SCLuM54sziOtvJw&packageName=00000000-0000-0000-0000-000000000000&ip=206.29.182.240&osVersion=4.4&gaid=gpidtest123&ua=Mozilla";
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
        dcpIronSourceAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String response =
                "{\"error\":false,\"error_message\":\"\",\"ads\":[{\"packageName\":\"air.com.goodgamestudios.empirefourkingdoms\",\"title\":\"Empire: Four Kingdoms\",\"description\":\"Example Description!\",\"creatives\":{\"img\":\"http://lh3.ggpht.com/H8Xm0OMtkOh2qJTcHS4Psdvsgx94S38iSc-m5WqNlfE8Z7qB9d0NsBxdNugAP5VxFGs=w100\",\"banner\":\"http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/955774dad5d046df88df70661165af04_512x250_Farm-Story-Pig-Device.jpg\",\"banner300x250\":\"http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/a35d3bff21cc43579c6d1577e5514ef9_300x250_Farm-Story-Pig-Device.jpg\",\"banner320x480\":\"http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/621ab6ce41a2484abe85fbd82f7830b4_320x480_Farm-Story-Pig-Device.jpg\",\"banner480x320\":\"http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/1c5b6bdf29ed49f0afef935bbf704ed8_480x320_Farm-Story-Pig-Device.jpg\",\"banner320x568\":\"http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/144dae8e7a5f4bb0987ddde64c881899_720x1280_Farm-Story-Pig-Device.jpg\",\"banner1024x768\":\"http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/c07a48b1acaa4279a15011cfeb7ff189_1024x768_Farm-Story-Pig-Device.jpg\",\"banner768x1024\":\"http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/c565fcc3d3704b54a76100f28182d7a1_768x1024_Farm-Story-Pig-Device.jpg\",\"banner320x50\":\"http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/fd904fe73ae2424c9e7ce7c95d80e22b_320x50_Farm-Story-Pig-Device.jpg\"},\"impressionURL\":\"http://impression.mobilecore.com/stats/impression?aff_id=66&carrier=00000000-000b-2b10-0004-f212e8baef8e&accountid=48F0B&appid=air.com.goodgamestudios.empirefourkingdoms&offer_id=79\",\"pricingModel\":\"cpi\",\"bid\":0.76,\"clickURL\":\"http://play.google.com/store/apps/details?id=com.king.candycrushsaga&deviceId=gpid80571d65720efasdfaf\"}]}";
        dcpIronSourceAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpIronSourceAdnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://play.google.com/store/apps/details?id=com.king.candycrushsaga&deviceId=gpid80571d65720efasdfaf' onclick=\"document.getElementById('click').src='$IMClickUrl';mraid.openExternal('http://play.google.com/store/apps/details?id=com.king.candycrushsaga&deviceId=gpid80571d65720efasdfaf'); return false;\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://77312bc1cfc3c28ac80c-6d4b9704fc8953c8a8632c8819becf02.r62.cf2.rackcdn.com/16280/20150326/fd904fe73ae2424c9e7ce7c95d80e22b_320x50_Farm-Story-Pig-Device.jpg'  /></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
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
        assertEquals(dcpIronSourceAdnetwork.getName(), "ironsource");
    }
}
