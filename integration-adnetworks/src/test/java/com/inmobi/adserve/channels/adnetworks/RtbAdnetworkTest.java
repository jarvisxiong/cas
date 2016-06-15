package com.inmobi.adserve.channels.adnetworks;

import static com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType.APP_WALL;
import static com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType.CAROUSEL;
import static com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType.CHAT_LIST;
import static com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType.CONTENT_STREAM;
import static com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType.CONTENT_WALL;
import static com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType.NEWS_FEED;
import static com.inmobi.adserve.contracts.misc.contentjson.NativeAdContentAsset.CTA;
import static com.inmobi.adserve.contracts.misc.contentjson.NativeAdContentAsset.DESCRIPTION;
import static com.inmobi.adserve.contracts.misc.contentjson.NativeAdContentAsset.ICON;
import static com.inmobi.adserve.contracts.misc.contentjson.NativeAdContentAsset.SCREENSHOT;
import static com.inmobi.adserve.contracts.misc.contentjson.NativeAdContentAsset.STAR_RATING;
import static com.inmobi.adserve.contracts.misc.contentjson.NativeAdContentAsset.TITLE;
import static com.inmobi.casthrift.DemandSourceType.RTBD;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.DataProvider;

import com.google.common.collect.Lists;
import com.inmobi.adserve.channels.adnetworks.rtb.ImpressionCallbackHelper;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.api.natives.CommonNativeBuilderImpl;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.contracts.common.request.nativead.Data;
import com.inmobi.adserve.contracts.common.request.nativead.Image;
import com.inmobi.adserve.contracts.common.request.nativead.Native;
import com.inmobi.adserve.contracts.iab.NativeLayoutId;
import com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType;
import com.inmobi.adserve.contracts.misc.contentjson.CommonAssetAttributes;
import com.inmobi.adserve.contracts.misc.contentjson.ImageAsset;
import com.inmobi.adserve.contracts.misc.contentjson.NativeAdContentAsset;
import com.inmobi.adserve.contracts.misc.contentjson.NativeContentJsonObject;
import com.inmobi.adserve.contracts.misc.contentjson.OtherAsset;
import com.inmobi.adserve.contracts.misc.contentjson.TextAsset;
import com.inmobi.adserve.contracts.rtb.response.Bid;
import com.inmobi.adserve.contracts.rtb.response.BidResponse;
import com.inmobi.adserve.contracts.rtb.response.SeatBid;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.types.LocationSource;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseAdNetworkImpl.class, NativeAdTemplateEntity.class})
public class RtbAdnetworkTest {
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final SASRequestParameters sas = new SASRequestParameters();
    private final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    private final String rtbAdvId = "id";
    private Configuration mockConfig = null;
    private RTBAdNetworkForTest rtbAdNetwork;
    private BidResponse bidResponse;
    private RepositoryHelper repositoryHelper;

    private static class RTBAdNetworkForTest extends RtbAdNetwork {

        static {
            templateConfiguration = new DefaultConfiguration() {
                @Override
                public GsonManager getGsonManager() {
                    return new GsonManager();
                }
            };
        }

        RTBAdNetworkForTest(final Configuration config, final Bootstrap clientBootstrap,
                final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel, final String host,
                final String advertiserName) {
            super(config, clientBootstrap, baseRequestHandler, serverChannel, host, advertiserName);
        }
    }

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        final String advertiserName = "rtb";
        expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".urlArg")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".rtbVer", "2.0")).andReturn("2.0").anyTimes();
        expect(mockConfig.getString(advertiserName + ".wnUrlback")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".rtbMethod")).andReturn("").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWinFromClient")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".siteBlinded")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".htmlSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", false)).andReturn(false).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".bannerVideoSupported", false)).andReturn(true).once();
        expect(mockConfig.getStringArray("rtb.blockedAdvertisers")).andReturn(
                new String[] {"king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com",
                        "supercell.com"}).anyTimes();
        expect(mockConfig.getString("key.1.value")).andReturn("clickmaker.key.1.value").anyTimes();
        expect(mockConfig.getString("key.2.value")).andReturn("clickmaker.key.2.value").anyTimes();
        expect(mockConfig.getString("beaconURLPrefix")).andReturn("clickmaker.beaconURLPrefix").anyTimes();
        expect(mockConfig.getString("clickURLPrefix")).andReturn("clickmaker.clickURLPrefix").anyTimes();
        expect(mockConfig.getString(advertiserName + ".currency", "USD")).andReturn("USD").anyTimes();
        replay(mockConfig);
    }

    @Before
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        sas.setSource("APP");
        sas.setDst(6);
        sas.setDemandSourceType(RTBD);
        sas.setCountryId(94L);

        final String urlBase = "";
        final CurrencyConversionEntity currencyConversionEntity = createMock(CurrencyConversionEntity.class);
        expect(currencyConversionEntity.getConversionRate()).andReturn(10.0).anyTimes();
        replay(currencyConversionEntity);
        repositoryHelper = createNiceMock(RepositoryHelper.class);
        expect(repositoryHelper.queryCurrencyConversionRepository(EasyMock.isA(String.class)))
                .andReturn(currencyConversionEntity).anyTimes();
        final SlotSizeMapEntity slotSizeMapEntityFor1 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor1.getDimension()).andReturn(new Dimension(120, 20)).anyTimes();
        replay(slotSizeMapEntityFor1);
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
        expect(repositoryHelper.querySlotSizeMapRepository((short) 1)).andReturn(slotSizeMapEntityFor1)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4)).andReturn(slotSizeMapEntityFor4)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9)).andReturn(slotSizeMapEntityFor9)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11)).andReturn(slotSizeMapEntityFor11)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14)).andReturn(slotSizeMapEntityFor14)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15)).andReturn(slotSizeMapEntityFor15)
                .anyTimes();
        expect(repositoryHelper.queryCcidMapRepository(null)).andReturn(null)
                .anyTimes();
        expect(repositoryHelper.queryDealById(anyObject(String.class), eq(false))).andReturn(Optional.empty()).anyTimes();

        replay(repositoryHelper);

        rtbAdNetwork = new RTBAdNetworkForTest(mockConfig, null, base, serverChannel, urlBase, "rtb");

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        rtbAdNetwork.setHost("http://localhost:8080");


        final Field asyncHttpClientProviderField = RtbAdNetwork.class.getDeclaredField("asyncHttpClientProvider");
        asyncHttpClientProviderField.setAccessible(true);
        final ServerConfig serverConfig = createMock(ServerConfig.class);
        expect(serverConfig.getNingTimeoutInMillisForDCP()).andReturn(800).anyTimes();
        expect(serverConfig.getNingTimeoutInMillisForRTB()).andReturn(200).anyTimes();
        expect(serverConfig.getMaxDcpOutGoingConnections()).andReturn(200).anyTimes();
        expect(serverConfig.getMaxRtbOutGoingConnections()).andReturn(200).anyTimes();
        expect(serverConfig.getNingTimeoutInMillisForPhoton()).andReturn(200).anyTimes();
        expect(serverConfig.getMaxPhotonOutGoingConnections()).andReturn(200).anyTimes();
        replay(serverConfig);
        final AsyncHttpClientProvider asyncHttpClientProvider = new AsyncHttpClientProvider(serverConfig);
        asyncHttpClientProvider.setup();
        asyncHttpClientProviderField.set(null, asyncHttpClientProvider);

        final Bid bid2 = new Bid();
        bid2.setId("ab73dd4868a0bbadf8fd7527d95136b4");
        bid2.setAdid("1335571993285");
        bid2.setPrice(0.2);
        bid2.setCid("cid");
        bid2.setCrid("crid");
        bid2.setAdm("<style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='http://www.inmobi.com/' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
        bid2.setImpid("impressionId");
        final List<Bid> bidList = new ArrayList<Bid>();
        bidList.add(bid2);
        final SeatBid seatBid = new SeatBid();
        seatBid.setSeat("TO-BE-DETERMINED");
        seatBid.setBid(bidList);
        final List<SeatBid> seatBidList = new ArrayList<SeatBid>();
        seatBidList.add(seatBid);
        bidResponse = new BidResponse();
        bidResponse.setSeatbid(seatBidList);
        bidResponse.setId("SGu1Jpq1IO");
        bidResponse.setBidid("ac1a2c944cff0a176643079625b0cad4a1bbe4a3");
        bidResponse.setCur("USD");
        rtbAdNetwork.setBidResponse(bidResponse);
    }

    @Test
    public void testImpressionCallback() {
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        RtbAdNetwork.impressionCallbackHelper = createMock(ImpressionCallbackHelper.class);
        expect(
                RtbAdNetwork.impressionCallbackHelper.writeResponse(isA(URI.class), isA(Request.class),
                        isA(AsyncHttpClient.class))).andReturn(true).anyTimes();
        replay(RtbAdNetwork.impressionCallbackHelper);
        rtbAdNetwork.setBidResponse(bidResponse);
        rtbAdNetwork.impressionCallback();
    }

    @Test
    public void testGetRequestUri() throws URISyntaxException {
        final URI uri = new URI("urlBase");
        rtbAdNetwork.setUrlArg("urlArg");
        rtbAdNetwork.setHost("urlBase");
        assertEquals(uri, rtbAdNetwork.getRequestUri());
    }

    @Test
    public void testConfigureParameters() {
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSource("wap");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setAuctionId("auctionId");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity,
                (short) 15, repositoryHelper), false);
    }

    @Test
    public void testShouldTestCategorySetForSiteNameOrAppName() {
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("auctionId");

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("app");
        sas.setWapSiteUACEntity(null);
        sas.setCategories(Lists.newArrayList(15L, 12L, 11L));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        // 15 mean board games. Refer to CategoryList
        assertEquals("Board", rtbAdNetwork.getBidRequest().getApp().getName());

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("wap");
        sas.setWapSiteUACEntity(null);
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals("Games", rtbAdNetwork.getBidRequest().getSite().getName());

        // If WapSiteUACEntity and InMobi categories are null.
        sas.setSource("wap");
        sas.setWapSiteUACEntity(null);
        final ArrayList<Long> list = new ArrayList<Long>();
        sas.setCategories(list);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);

        assertEquals("miscellenous", rtbAdNetwork.getBidRequest().getSite().getName());

        sas.setSource("wap");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.appType("Games");
        sas.setWapSiteUACEntity(builder.build());
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);

        // First UAC Entity Category should be present as Site Name.
        assertEquals("Games", rtbAdNetwork.getBidRequest().getSite().getName());

        // For App, First UAC Entity Category should be present as App Name.
        sas.setSource("app");
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        assertEquals("Games", rtbAdNetwork.getBidRequest().getApp().getName());


        // If WapSiteUACEntity is not null, then it should set primary category name from uac.
        sas.setSource("app");
        builder = WapSiteUACEntity.newBuilder();
        builder.appType("Social");
        sas.setWapSiteUACEntity(builder.build());
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        // Setting primary category name from uac.
        assertEquals("Social", rtbAdNetwork.getBidRequest().getApp().getName());


    }


    @Test
    public void testShouldHaveFixedBlockedAdvertisers() {
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setAuctionId("auctionId");
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSource("wap");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);

        // Expected Blocked Advertisers
        final ArrayList<String> expectedBlockedAdvertisers =
                Lists.newArrayList("king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com",
                        "supercell.com");
        assertNull(casInternalRequestParameters.getBlockedAdvertisers());
        assertEquals(6, rtbAdNetwork.getBidRequest().getBadv().size());
        assertTrue(rtbAdNetwork.getBidRequest().getBadv().containsAll(expectedBlockedAdvertisers));
    }

    @Test
    public void testShouldAddFixedBlockedAdvertisersForExistingBlockedList() {
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedAdvertisers(Lists.newArrayList("abcd.com"));
        casInternalRequestParameters.setAuctionId("auctionId");

        sas.setRemoteHostIp("206.29.182.240");
        sas.setSource("wap");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);

        // Expected Blocked Advertisers
        final ArrayList<String> expectedBlockedAdvertisers =
                Lists.newArrayList("abcd.com", "king.com", "supercell.net", "paps.com", "fhs.com",
                        "china.supercell.com", "supercell.com");
        assertEquals(1, casInternalRequestParameters.getBlockedAdvertisers().size());
        assertEquals(7, rtbAdNetwork.getBidRequest().getBadv().size());
        assertTrue(rtbAdNetwork.getBidRequest().getBadv().containsAll(expectedBlockedAdvertisers));
    }

    @Test
    public void testShouldHaveBlockedCategories() {
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSource("wap");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setBlockedIabCategories(Lists.newArrayList("IAB-1", "IAB-2", "IAB-3"));
        casInternalRequestParameters.setAuctionId("auctionId");

        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);

        // Expected Blocked Categories
        final List<String> expectedBlockedCategories = Lists.newArrayList("IAB-1", "IAB-2", "IAB-3");

        // Add family safe blocked categories to the expected list
        final List<String> familySafeBlockedCategories =
                IABCategoriesMap.getIABCategories(IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES);
        expectedBlockedCategories.addAll(familySafeBlockedCategories);

        assertEquals(expectedBlockedCategories.size(), rtbAdNetwork.getBidRequest().getBcat().size());
        assertTrue(rtbAdNetwork.getBidRequest().getBcat().containsAll(expectedBlockedCategories));
    }


    @Test
    public void testConfigureParametersWithAllsasparams() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setSiteId("123");
        sasParams.setSource("app");
        final Long[] catLong = new Long[2];
        catLong[0] = (long) 1;
        catLong[1] = (long) 2;
        sasParams.setCategories(Arrays.asList(catLong));
        sasParams.setLocationSource(LocationSource.WIFI);
        sasParams.setGender("Male");
        sasParams.setCountryId(94L);
        sasParams.setDemandSourceType(RTBD);
        casInternalRequestParameters.setUid("1234");
        sasParams.setAge((short) 26);
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("auctionId");

        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 1, repositoryHelper), true);
    }

    @Test
    public void testRtbGetName() throws Exception {
        assertEquals(rtbAdNetwork.getName(), "rtb");
    }

    @Test
    public void testReplaceMacros() {
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        rtbAdNetwork.setCallbackUrl("http://rtb:8970/${AUCTION_ID}/${AUCTION_BID_ID}");
        rtbAdNetwork.setCallbackUrl(rtbAdNetwork.replaceRTBMacros(rtbAdNetwork.getCallbackUrl()));
        assertEquals("http://rtb:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3",
                rtbAdNetwork.getCallbackUrl());
    }

    @Test
    public void testWapWithTransparency() {

        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("auctionId");

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("wap");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.appType("Games");
        builder.isTransparencyEnabled(true);
        builder.siteUrl("www.inmobi.com");
        sas.setWapSiteUACEntity(builder.build());
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals(null, rtbAdNetwork.getBidRequest().getSite().getName());
        assertEquals("www.inmobi.com", rtbAdNetwork.getBidRequest().getSite().getDomain());
        assertEquals("some_site_id", rtbAdNetwork.getBidRequest().getSite().getId());

    }

    @Test
    public void testWapWithBlind() {

        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("auctionId");

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("wap");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.appType("Games");
        sas.setWapSiteUACEntity(builder.build());
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals("Games", rtbAdNetwork.getBidRequest().getSite().getName());
        assertEquals("http://www.ix.com/7dea362b-3fac-3e00-956a-4952a3d4f474", rtbAdNetwork.getBidRequest().getSite()
                .getDomain());
        assertEquals("7dea362b-3fac-3e00-956a-4952a3d4f474", rtbAdNetwork.getBidRequest().getSite().getId());
    }

    @Test
    public void testWapWithBlindWithoutWapSiteUACEntity() {
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("auctionId");

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("wap");
        sas.setWapSiteUACEntity(null);
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals("Games", rtbAdNetwork.getBidRequest().getSite().getName());
        assertEquals("http://www.ix.com/7dea362b-3fac-3e00-956a-4952a3d4f474", rtbAdNetwork.getBidRequest().getSite()
                .getDomain());
        assertEquals("7dea362b-3fac-3e00-956a-4952a3d4f474", rtbAdNetwork.getBidRequest().getSite().getId());
    }

    @Test
    public void testAppWithTransparency() {

        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("auctionId");

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("app");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.appType("Games");
        builder.isTransparencyEnabled(true);
        builder.siteUrl("www.inmobi.com");
        builder.bundleId("com.android.app.bundleId");
        builder.marketId("com.android.app.marketId");
        sas.setWapSiteUACEntity(builder.build());
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals("Games", rtbAdNetwork.getBidRequest().getApp().getName());
        assertEquals("com.android.app.marketId", rtbAdNetwork.getBidRequest().getApp().getBundle());
        assertEquals("some_site_id", rtbAdNetwork.getBidRequest().getApp().getId());

    }

    @Test
    public void testAppWithBlind() {

        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("auctionId");

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("app");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.appType("Games");
        sas.setWapSiteUACEntity(builder.build());
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals("Games", rtbAdNetwork.getBidRequest().getApp().getName());
        assertEquals("com.ix.7dea362b-3fac-3e00-956a-4952a3d4f474", rtbAdNetwork.getBidRequest().getApp().getBundle());
        assertEquals("7dea362b-3fac-3e00-956a-4952a3d4f474", rtbAdNetwork.getBidRequest().getApp().getId());
    }

    @DataProvider(name = "DataProviderForRtbNative")
    public Object[][] paramDataProviderForRtbNative() {
        return new Object[][] {
                {75, 75, ICON, TITLE, 69, STAR_RATING, APP_WALL}, 
                {480, 320, ICON, TITLE, 69, STAR_RATING, CAROUSEL}, 
                {480, 320, ICON, TITLE, 69, STAR_RATING, CHAT_LIST}, 
                {480, 320, ICON, TITLE, 69, STAR_RATING, CONTENT_STREAM}, 
                {480, 320, ICON, TITLE, 69, STAR_RATING, CONTENT_WALL}, 
                {480, 320, ICON, TITLE, 69, STAR_RATING, NEWS_FEED},

                {75, 75, ICON, TITLE, 69, CTA, APP_WALL}, 
                {480, 320, ICON, TITLE, 69, CTA, CAROUSEL}, 
                {480, 320, ICON, TITLE, 69, CTA, CHAT_LIST}, 
                {480, 320, ICON, TITLE, 69, CTA, CONTENT_STREAM}, 
                {480, 320, ICON, TITLE, 69, CTA, CONTENT_WALL}, 
                {480, 320, ICON, TITLE, 69, CTA, NEWS_FEED},

                {75, 75, ICON, TITLE, 0, STAR_RATING, APP_WALL}, 
                {480, 320, ICON, TITLE, 0, STAR_RATING, CAROUSEL}, 
                {480, 320, ICON, TITLE, 0, STAR_RATING, CHAT_LIST}, 
                {480, 320, ICON, TITLE, 0, STAR_RATING, CONTENT_STREAM}, 
                {480, 320, ICON, TITLE, 0, STAR_RATING, CONTENT_WALL}, 
                {480, 320, ICON, TITLE, 0, STAR_RATING, NEWS_FEED}, 
                {75, 75, ICON, TITLE, 0, CTA, APP_WALL}, 
                {480, 320, ICON, TITLE, 0, CTA, CAROUSEL}, 
                {480, 320, ICON, TITLE, 0, CTA, CHAT_LIST}, 
                {480, 320, ICON, TITLE, 0, CTA, CONTENT_STREAM}, 
                {480, 320, ICON, TITLE, 0, CTA, CONTENT_WALL}, 
                {480, 320, ICON, TITLE, 0, CTA, NEWS_FEED},

                {75, 75, ICON, DESCRIPTION, 69, STAR_RATING, APP_WALL}, 
                {480, 320, ICON, DESCRIPTION, 69, STAR_RATING, CAROUSEL}, 
                {480, 320, ICON, DESCRIPTION, 69, STAR_RATING, CHAT_LIST}, 
                {480, 320, ICON, DESCRIPTION, 69, STAR_RATING, CONTENT_STREAM}, 
                {480, 320, ICON, DESCRIPTION, 69, STAR_RATING, CONTENT_WALL}, 
                {480, 320, ICON, DESCRIPTION, 69, STAR_RATING, NEWS_FEED}, 
                {75, 75, ICON, DESCRIPTION, 69, CTA, APP_WALL}, 
                {480, 320, ICON, DESCRIPTION, 69, CTA, CAROUSEL}, 
                {480, 320, ICON, DESCRIPTION, 69, CTA, CHAT_LIST}, 
                {480, 320, ICON, DESCRIPTION, 69, CTA, CONTENT_STREAM}, 
                {480, 320, ICON, DESCRIPTION, 69, CTA, CONTENT_WALL}, 
                {480, 320, ICON, DESCRIPTION, 69, CTA, NEWS_FEED}, 
                {75, 75, ICON, DESCRIPTION, 0, STAR_RATING, APP_WALL}, 
                {480, 320, ICON, DESCRIPTION, 0, STAR_RATING, CAROUSEL}, 
                {480, 320, ICON, DESCRIPTION, 0, STAR_RATING, CHAT_LIST}, 
                {480, 320, ICON, DESCRIPTION, 0, STAR_RATING, CONTENT_STREAM}, 
                {480, 320, ICON, DESCRIPTION, 0, STAR_RATING, CONTENT_WALL}, 
                {480, 320, ICON, DESCRIPTION, 0, STAR_RATING, NEWS_FEED}, 
                {75, 75, ICON, DESCRIPTION, 0, CTA, APP_WALL}, 
                {480, 320, ICON, DESCRIPTION, 0, CTA, CAROUSEL}, 
                {480, 320, ICON, DESCRIPTION, 0, CTA, CHAT_LIST}, 
                {480, 320, ICON, DESCRIPTION, 0, CTA, CONTENT_STREAM}, 
                {480, 320, ICON, DESCRIPTION, 0, CTA, CONTENT_WALL}, 
                {480, 320, ICON, DESCRIPTION, 0, CTA, NEWS_FEED},
                

                {75, 75, SCREENSHOT, TITLE, 69, STAR_RATING, APP_WALL}, 
                {480, 320, SCREENSHOT, TITLE, 69, STAR_RATING, CAROUSEL}, 
                {480, 320, SCREENSHOT, TITLE, 69, STAR_RATING, CHAT_LIST}, 
                {480, 320, SCREENSHOT, TITLE, 69, STAR_RATING, CONTENT_STREAM}, 
                {480, 320, SCREENSHOT, TITLE, 69, STAR_RATING, CONTENT_WALL}, 
                {480, 320, SCREENSHOT, TITLE, 69, STAR_RATING, NEWS_FEED}, 
                {75, 75, SCREENSHOT, TITLE, 69, CTA, APP_WALL}, 
                {480, 320, SCREENSHOT, TITLE, 69, CTA, CAROUSEL}, 
                {480, 320, SCREENSHOT, TITLE, 69, CTA, CHAT_LIST}, 
                {480, 320, SCREENSHOT, TITLE, 69, CTA, CONTENT_STREAM}, 
                {480, 320, SCREENSHOT, TITLE, 69, CTA, CONTENT_WALL}, 
                {480, 320, SCREENSHOT, TITLE, 69, CTA, NEWS_FEED}, 
                {75, 75, SCREENSHOT, TITLE, 0, STAR_RATING, APP_WALL}, 
                {480, 320, SCREENSHOT, TITLE, 0, STAR_RATING, CAROUSEL}, 
                {480, 320, SCREENSHOT, TITLE, 0, STAR_RATING, CHAT_LIST}, 
                {480, 320, SCREENSHOT, TITLE, 0, STAR_RATING, CONTENT_STREAM}, 
                {480, 320, SCREENSHOT, TITLE, 0, STAR_RATING, CONTENT_WALL}, 
                {480, 320, SCREENSHOT, TITLE, 0, STAR_RATING, NEWS_FEED}, 
                {75, 75, SCREENSHOT, TITLE, 0, CTA, APP_WALL}, 
                {480, 320, SCREENSHOT, TITLE, 0, CTA, CAROUSEL}, 
                {480, 320, SCREENSHOT, TITLE, 0, CTA, CHAT_LIST}, 
                {480, 320, SCREENSHOT, TITLE, 0, CTA, CONTENT_STREAM}, 
                {480, 320, SCREENSHOT, TITLE, 0, CTA, CONTENT_WALL}, 
                {480, 320, SCREENSHOT, TITLE, 0, CTA, NEWS_FEED},
                

                {75, 75, SCREENSHOT, DESCRIPTION, 69, STAR_RATING, APP_WALL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, STAR_RATING, CAROUSEL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, STAR_RATING, CHAT_LIST}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, STAR_RATING, CONTENT_STREAM}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, STAR_RATING, CONTENT_WALL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, STAR_RATING, NEWS_FEED}, 
                {75, 75, SCREENSHOT, DESCRIPTION, 69, CTA, APP_WALL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, CTA, CAROUSEL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, CTA, CHAT_LIST}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, CTA, CONTENT_STREAM}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, CTA, CONTENT_WALL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 69, CTA, NEWS_FEED}, 
                {75, 75, SCREENSHOT, DESCRIPTION, 0, STAR_RATING, APP_WALL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, STAR_RATING, CAROUSEL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, STAR_RATING, CHAT_LIST}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, STAR_RATING, CONTENT_STREAM}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, STAR_RATING, CONTENT_WALL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, STAR_RATING, NEWS_FEED}, 
                {75, 75, SCREENSHOT, DESCRIPTION, 0, CTA, APP_WALL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, CTA, CAROUSEL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, CTA, CHAT_LIST}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, CTA, CONTENT_STREAM}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, CTA, CONTENT_WALL}, 
                {480, 320, SCREENSHOT, DESCRIPTION, 0, CTA, NEWS_FEED},
                

        };
    }
    
    @org.testng.annotations.Test(dataProvider = "DataProviderForRtbNative")
    public void verifyNativeOnRtb(final Integer w, final Integer h,
                                  final NativeAdContentAsset imageContentAsset,
                                  final NativeAdContentAsset textContentAsset,
                                  Integer titleMaxLen, final NativeAdContentAsset otherContentAsset,
                                  final NativeAdContentUILayoutType layoutType) throws Exception {

        NativeContentJsonObject nativeContentJsonObject = new NativeContentJsonObject();
        com.inmobi.adserve.contracts.misc.contentjson.Dimension dimension =
            new com.inmobi.adserve.contracts.misc.contentjson.Dimension();
        dimension.setHeight(h);
        dimension.setWidth(w);
        ImageAsset imageAsset = new ImageAsset();
        CommonAssetAttributes commonAssetAttributes = new CommonAssetAttributes();
        commonAssetAttributes.setAdContentAsset(imageContentAsset);
        commonAssetAttributes.setOptional(false);
        imageAsset.setCommonAttributes(commonAssetAttributes);
        imageAsset.setDimension(dimension);
        nativeContentJsonObject.setImageAssets(Arrays.asList(imageAsset));

        CommonAssetAttributes commonAssetAttributesText = new CommonAssetAttributes();
        commonAssetAttributesText.setAdContentAsset(textContentAsset);
        commonAssetAttributesText.setOptional(false);
        TextAsset textAsset = new TextAsset();
        textAsset.setCommonAttributes(commonAssetAttributesText);
        textAsset.setMaxChars(titleMaxLen);
        nativeContentJsonObject.setTextAssets(Arrays.asList(textAsset));

        CommonAssetAttributes commonAssetAttributesOther = new CommonAssetAttributes();
        commonAssetAttributesOther.setAdContentAsset(otherContentAsset);
        commonAssetAttributesOther.setOptional(false);
        OtherAsset otherAsset = new OtherAsset();
        otherAsset.setCommonAttributes(commonAssetAttributesOther);
        nativeContentJsonObject.setOtherAssets(Arrays.asList(otherAsset));

        final NativeAdTemplateEntity nativeAdTemplateEntity = NativeAdTemplateEntity.newBuilder()
            .templateId(4408950071453012585l)
            .contentJson(nativeContentJsonObject)
            .imageKey("imageKey")
            .mandatoryKey("mandatoryKey")
            .modifiedOn(new Timestamp(new Date().getTime()))
            .placementId(1431975538797857l)
            .template("template")
            .nativeUILayout(layoutType)
            .build();
        CommonNativeBuilderImpl ixNativeBuilder = new CommonNativeBuilderImpl(nativeAdTemplateEntity);
        Native natObj = ixNativeBuilder.buildNative();

        Integer imageType;
        if (imageContentAsset == SCREENSHOT) {
            imageType = Image.ImageAssetType.MAIN.getId();
        } else if (imageContentAsset == ICON){
            imageType = Image.ImageAssetType.ICON.getId();
        } else {
            imageType = null;
        }
        assertEquals(w, natObj.getRequestobj().getAssets().get(0).getImg().getWmin());
        assertEquals(h, natObj.getRequestobj().getAssets().get(0).getImg().getHmin());
        assertEquals(imageType, natObj.getRequestobj().getAssets().get(0).getImg().getType());
        assertEquals((Integer) (!false ? 1 : 0), natObj.getRequestobj().getAssets().get(0).getRequired());

        titleMaxLen = titleMaxLen == 0 ? 100 : titleMaxLen;
        if(textContentAsset == TITLE) {
            assertEquals(titleMaxLen, natObj.getRequestobj().getAssets().get(1).getTitle().getLen());
        } else if (textContentAsset == DESCRIPTION) {
            assertEquals(titleMaxLen, natObj.getRequestobj().getAssets().get(1).getData().getLen());
        }
        assertEquals((Integer) (!false ? 1 : 0), natObj.getRequestobj().getAssets().get(1).getRequired());
        Integer type;
        if(otherContentAsset == CTA){
            type = (Integer) Data.DataAssetType.CTA_TEXT.getId();
        } else if (otherContentAsset == STAR_RATING){
            type = (Integer)Data.DataAssetType.RATING.getId();
        }else{
            type = null;
        }
        assertEquals(type, natObj.getRequestobj().getAssets().get(2).getData().getType());
        assertEquals((Integer) (!false ? 1 : 0), natObj.getRequestobj().getAssets().get(2).getRequired());
    }

    @DataProvider(name = "DataProviderForRtbNativeWhenEntityNull")
    public Object[][] paramDataProviderForRtbNativeWhenEntityNull() {
        return new Object[][] {
            {NEWS_FEED, "layoutConstraint.1", null},
            {NEWS_FEED, "layoutConstraint.2", null},
            {NEWS_FEED, "layoutConstraint.3", "inmTag.a083"},
            {NEWS_FEED, "layoutConstraint.3", "inmTag.a067"},
            {NEWS_FEED, "layoutConstraint.3", "inmTag.a64"},
            {CONTENT_WALL, "layoutConstraint.1", null},
            {CONTENT_WALL, "layoutConstraint.2", null},
            {CONTENT_WALL, "layoutConstraint.3", "inmTag.a083"},
            {CONTENT_WALL, "layoutConstraint.3", "inmTag.a067"},
            {CONTENT_WALL, "layoutConstraint.3", "inmTag.a64"},
            {CONTENT_STREAM, "layoutConstraint.1", null},
            {CONTENT_STREAM, "layoutConstraint.2", null},
            {CONTENT_STREAM, "layoutConstraint.3", "inmTag.a083"},
            {CONTENT_STREAM, "layoutConstraint.3", "inmTag.a067"},
            {CONTENT_STREAM, "layoutConstraint.3", "inmTag.a64"},
            {CAROUSEL, "layoutConstraint.1", null},
            {CAROUSEL, "layoutConstraint.2", null},
            {CAROUSEL, "layoutConstraint.3", "inmTag.a083"},
            {CAROUSEL, "layoutConstraint.3", "inmTag.a067"},
            {CAROUSEL, "layoutConstraint.3", "inmTag.a64"},
            {CHAT_LIST, "layoutConstraint.1", null},
            {CHAT_LIST, "layoutConstraint.2", null},
            {CHAT_LIST, "layoutConstraint.3", "inmTag.a083"},
            {CHAT_LIST, "layoutConstraint.3", "inmTag.a067"},
            {CHAT_LIST, "layoutConstraint.3", "inmTag.a64"},
            {APP_WALL, "layoutConstraint.1", null},
            {APP_WALL, "layoutConstraint.2", null},
            {APP_WALL, "layoutConstraint.3", "inmTag.a083"},
            {APP_WALL, "layoutConstraint.3", "inmTag.a067"},
            {APP_WALL, "layoutConstraint.3", "inmTag.a64"},
        };
    }


    @org.testng.annotations.Test(dataProvider = "DataProviderForRtbNativeWhenEntityNull")
    public void verifyNativeOnRtbWhenEntityNull(final NativeAdContentUILayoutType layoutType, final String mandatoryKey,
                                                final String imageKey) throws Exception {
        final NativeAdTemplateEntity nativeAdTemplateEntity = NativeAdTemplateEntity.newBuilder()
            .templateId(4408950071453012585l)
            .contentJson(null)
            .imageKey(imageKey)
            .mandatoryKey(mandatoryKey)
            .modifiedOn(new Timestamp(new Date().getTime()))
            .placementId(1431975538797857l)
            .template("template")
            .nativeUILayout(layoutType)
            .build();
        CommonNativeBuilderImpl ixNativeBuilder = new CommonNativeBuilderImpl(nativeAdTemplateEntity);
        Native natObj = ixNativeBuilder.buildNative();

        assertEquals((Integer)Image.ImageAssetType.ICON.getId(), natObj.getRequestobj().getAssets().get(0).getImg().getType());
        assertEquals((Integer)300, natObj.getRequestobj().getAssets().get(0).getImg().getWmin());
        assertEquals((Integer)300, natObj.getRequestobj().getAssets().get(0).getImg().getHmin());
        assertEquals((Integer) 1, natObj.getRequestobj().getAssets().get(0).getRequired());
        assertEquals(NativeLayoutId.findByInmobiNativeUILayoutType(layoutType).getKey(), natObj.getRequestobj().getLayout().intValue());

        assertEquals((Integer)100, natObj.getRequestobj().getAssets().get(1).getTitle().getLen());
        assertEquals((Integer)1, natObj.getRequestobj().getAssets().get(1).getRequired());


        assertEquals((Integer)2, natObj.getRequestobj().getAssets().get(2).getData().getType());
        assertEquals((Integer)1, natObj.getRequestobj().getAssets().get(2).getRequired());

        int index = 3;
        if(null != imageKey){
            Integer wMin=0, hMin=0;
            if(imageKey.equals("inmTag.a083")){
                wMin = 250; hMin = 300;
            } else if(imageKey.equals("inmTag.a067")){
                wMin = 320 ; hMin = 480;
            } else if(imageKey.equals("inmTag.a64")){
                wMin = 320; hMin = 50;
            }
            index += 1;
            assertEquals((Integer) Image.ImageAssetType.MAIN.getId(), natObj.getRequestobj().getAssets().get(3).getImg().getType());
            assertEquals(wMin, natObj.getRequestobj().getAssets().get(3).getImg().getWmin());
            assertEquals(hMin, natObj.getRequestobj().getAssets().get(3).getImg().getHmin());
        }

        assertEquals((Integer)12, natObj.getRequestobj().getAssets().get(index).getData().getType());
        assertEquals((Integer)5, natObj.getRequestobj().getAssets().get(index+1).getData().getType());
        assertEquals((Integer)3, natObj.getRequestobj().getAssets().get(index+2).getData().getType());
    }
}