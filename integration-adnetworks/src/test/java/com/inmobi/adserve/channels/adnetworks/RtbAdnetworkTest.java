package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

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
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.SeatBid;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;


public class RtbAdnetworkTest {

    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private RtbAdNetwork rtbAdNetwork;
    private final SASRequestParameters sas = new SASRequestParameters();
    private final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    private final String rtbAdvId = "id";
    BidResponse bidResponse;
    private RepositoryHelper repositoryHelper;

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
        expect(mockConfig.getBoolean(advertiserName + ".bannerVideoSupported", false)).andReturn(true).once();
        expect(mockConfig.getStringArray("rtb.blockedAdvertisers")).andReturn(
                new String[] {"king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com",
                        "supercell.com"}).anyTimes();
        expect(mockConfig.getString("key.1.value")).andReturn("clickmaker.key.1.value").anyTimes();
        expect(mockConfig.getString("key.2.value")).andReturn("clickmaker.key.2.value").anyTimes();
        expect(mockConfig.getString("beaconURLPrefix")).andReturn("clickmaker.beaconURLPrefix").anyTimes();
        expect(mockConfig.getString("clickURLPrefix")).andReturn("clickmaker.clickURLPrefix").anyTimes();
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
        sas.setRqAdType("");
        sas.setDst(2);

        final String urlBase = "";
        final CurrencyConversionEntity currencyConversionEntity = EasyMock.createMock(CurrencyConversionEntity.class);
        EasyMock.expect(currencyConversionEntity.getConversionRate()).andReturn(10.0).anyTimes();
        EasyMock.replay(currencyConversionEntity);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.queryCurrencyConversionRepository(EasyMock.isA(String.class)))
                .andReturn(currencyConversionEntity).anyTimes();
        final SlotSizeMapEntity slotSizeMapEntityFor1 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor1.getDimension()).andReturn(new Dimension(120, 20)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor1);
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
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 1)).andReturn(slotSizeMapEntityFor1)
                .anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 4)).andReturn(slotSizeMapEntityFor4)
                .anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 9)).andReturn(slotSizeMapEntityFor9)
                .anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 11)).andReturn(slotSizeMapEntityFor11)
                .anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 14)).andReturn(slotSizeMapEntityFor14)
                .anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 15)).andReturn(slotSizeMapEntityFor15)
                .anyTimes();
        EasyMock.replay(repositoryHelper);

        rtbAdNetwork = new RtbAdNetwork(mockConfig, null, base, serverChannel, urlBase, "rtb", true);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        rtbAdNetwork.setHost("http://localhost:8080");


        final Field asyncHttpClientProviderField = RtbAdNetwork.class.getDeclaredField("asyncHttpClientProvider");
        asyncHttpClientProviderField.setAccessible(true);
        final ServerConfig serverConfig = createMock(ServerConfig.class);
        expect(serverConfig.getDcpRequestTimeoutInMillis()).andReturn(800).anyTimes();
        expect(serverConfig.getRtbRequestTimeoutInMillis()).andReturn(200).anyTimes();
        expect(serverConfig.getMaxDcpOutGoingConnections()).andReturn(200).anyTimes();
        expect(serverConfig.getMaxRtbOutGoingConnections()).andReturn(200).anyTimes();
        replay(serverConfig);
        final AsyncHttpClientProvider asyncHttpClientProvider =
                new AsyncHttpClientProvider(serverConfig, Executors.newCachedThreadPool());
        asyncHttpClientProvider.setup();
        asyncHttpClientProviderField.set(null, asyncHttpClientProvider);

        final Bid bid2 = new Bid();
        bid2.id = "ab73dd4868a0bbadf8fd7527d95136b4";
        bid2.adid = "1335571993285";
        bid2.price = 0.2;
        bid2.cid = "cid";
        bid2.crid = "crid";
        bid2.adm =
                "<style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='http://www.inmobi.com/' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>";
        bid2.impid = "impressionId";
        final List<Bid> bidList = new ArrayList<Bid>();
        bidList.add(bid2);
        final SeatBid seatBid = new SeatBid();
        seatBid.seat = "TO-BE-DETERMINED";
        seatBid.bid = bidList;
        final List<SeatBid> seatBidList = new ArrayList<SeatBid>();
        seatBidList.add(seatBid);
        bidResponse = new BidResponse();
        bidResponse.setSeatbid(seatBidList);
        bidResponse.id = "SGu1Jpq1IO";
        bidResponse.bidid = "ac1a2c944cff0a176643079625b0cad4a1bbe4a3";
        bidResponse.cur = "USD";
        rtbAdNetwork.setBidResponse(bidResponse);
    }

    @Test
    public void testImpressionCallback() {
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String beaconUrl = "";
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 15,
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
        sas.setRqAdType("");
        sas.setRqAdType("");
        sas.setSource("wap");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String externalSiteKey = "f6wqjq1r5v";
        final String beaconUrl = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, clickUrl, beaconUrl,
                (short) 15, repositoryHelper), false);
    }

    @Test
    public void testShouldTestCategorySetForSiteNameOrAppName() {
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setRqAdType("");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("app");
        sas.setWapSiteUACEntity(null);
        sas.setCategories(Lists.newArrayList(15L, 12L, 11L));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
                repositoryHelper);
        // 15 mean board games. Refer to CategoryList
        assertEquals("Board", rtbAdNetwork.getBidRequest().getApp().getName());

        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("wap");
        sas.setWapSiteUACEntity(null);
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals("Games", rtbAdNetwork.getBidRequest().getSite().getName());

        // If WapSiteUACEntity and InMobi categories are null.
        sas.setSource("wap");
        sas.setWapSiteUACEntity(null);
        final ArrayList<Long> list = new ArrayList<Long>();
        sas.setCategories(list);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
                repositoryHelper);

        assertEquals("miscellenous", rtbAdNetwork.getBidRequest().getSite().getName());

        sas.setSource("wap");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.setAppType("Games");
        sas.setWapSiteUACEntity(new WapSiteUACEntity(builder));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
                repositoryHelper);

        // First UAC Entity Category should be present as Site Name.
        assertEquals("Games", rtbAdNetwork.getBidRequest().getSite().getName());

        // For App, First UAC Entity Category should be present as App Name.
        sas.setSource("app");
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
                repositoryHelper);
        assertEquals("Games", rtbAdNetwork.getBidRequest().getApp().getName());


        // If WapSiteUACEntity is not null, then it should set primary category name from uac.
        sas.setSource("app");
        builder = WapSiteUACEntity.newBuilder();
        builder.setAppType("Social");
        sas.setWapSiteUACEntity(new WapSiteUACEntity(builder));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
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
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setSource("wap");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
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
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedAdvertisers(Lists.newArrayList("abcd.com"));
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setSource("wap");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
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
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setSource("wap");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setBlockedIabCategories(Lists.newArrayList("IAB-1", "IAB-2", "IAB-3"));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
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
        sasParams.setLocSrc("wifi");
        sasParams.setGender("Male");
        casInternalRequestParameters.setUid("1234");
        sasParams.setAge((short) 26);
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setRqAdType("");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setAuctionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String externalSiteKey = "f6wqjq1r5v";
        final String beaconUrl = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl,
                beaconUrl, (short) 1, repositoryHelper), true);
    }

    @Test
    public void testRtbGetName() throws Exception {
        assertEquals(rtbAdNetwork.getName(), "rtb");
    }

    @Test
    public void testReplaceMacros() {
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String externalSiteKey = "f6wqjq1r5v";
        final String beaconUrl = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 15,
                repositoryHelper);
        rtbAdNetwork.setCallbackUrl("http://rtb:8970/${AUCTION_ID}/${AUCTION_BID_ID}");
        rtbAdNetwork.setCallbackUrl(rtbAdNetwork.replaceRTBMacros(rtbAdNetwork.getCallbackUrl()));
        assertEquals("http://rtb:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3",
                rtbAdNetwork.getCallbackUrl());
    }

    @Test
    public void testReplaceMacrosAllPosibilities() {
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String externalSiteKey = "f6wqjq1r5v";
        final String beaconUrl = "";
        sas.setSource("app");
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 15,
                repositoryHelper);
        rtbAdNetwork
                .setCallbackUrl("http://rtb:8970/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_IMP_ID}/${AUCTION_SEAT_ID}/${AUCTION_AD_ID}/${AUCTION_PRICE}/${AUCTION_CURRENCY}");
        rtbAdNetwork.setEncryptedBid("abc");
        rtbAdNetwork.setCallbackUrl(rtbAdNetwork.replaceRTBMacros(rtbAdNetwork.getCallbackUrl()));
        assertEquals(
                "http://rtb:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3/4f8d98e2-4bbd-40bc-8795-22da170700f9/TO-BE-DETERMINED/1335571993285/0.0/USD",
                rtbAdNetwork.getCallbackUrl());
    }

    @Test
    public void testParseResponse() throws TException {
        final StringBuilder str = new StringBuilder();
        // Temporarily using ixResponseJSON instead of rtbdResponseJSON
        str.append(TestUtils.SampleStrings.ixResponseJson);
        final StringBuilder responseAdm = new StringBuilder();
        responseAdm.append("<html><body style=\"margin:0;padding:0;\">");
        responseAdm
                .append("<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
        responseAdm.append("<img src=\'beacon?b=${WIN_BID}\' height=1 width=1 border=0 />");
        responseAdm.append("</body></html>");
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String beaconUrl = "beacon";
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 15,
                repositoryHelper);
        final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
        assertEquals(responseAdm.toString(), rtbAdNetwork.getResponseContent());
        rtbAdNetwork.setEncryptedBid("0.23");
        rtbAdNetwork.setSecondBidPrice(0.23);
        final String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
        assertEquals(afterMacros, rtbAdNetwork.getResponseContent());
        rtbAdNetwork.parseResponse(str.toString(), HttpResponseStatus.NOT_FOUND);
        assertEquals("", rtbAdNetwork.getResponseContent());
    }

    @Test
    public void testParseResponseSDK450Interstitial() throws TException {
        final StringBuilder str = new StringBuilder();
        // Temporarily using ixResponseJSON instead of rtbdResponseJSON
        str.append(TestUtils.SampleStrings.ixResponseJson);
        final StringBuilder responseAdm = new StringBuilder();
        responseAdm
                .append("<html><body style=\"margin:0;padding:0;\"><script src=\"mraid.js\" ></script><style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='http://www.inmobi.com/' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p><script type=\"text/javascript\">var readyHandler=function(){_im_imai.fireAdReady();_im_imai.removeEventListener('ready',readyHandler);};_im_imai.addEventListener('ready',readyHandler);</script><img src='beacon?b=${WIN_BID}' height=1 width=1 border=0 /></body></html>");
        sas.setSdkVersion("a450");
        sas.setRqAdType("int");
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String beaconUrl = "beacon";
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 15,
                repositoryHelper);
        final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
        assertEquals(responseAdm.toString(), rtbAdNetwork.getResponseContent());
        rtbAdNetwork.setEncryptedBid("0.23");
        rtbAdNetwork.setSecondBidPrice(0.23);
        final String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
        assertEquals(afterMacros, rtbAdNetwork.getResponseContent());
        rtbAdNetwork.parseResponse(str.toString(), HttpResponseStatus.NOT_FOUND);
        assertEquals("", rtbAdNetwork.getResponseContent());
    }

    @Test
    public void testParseResponseWithRMD() throws TException {
        bidResponse.setCur("RMD");
        bidResponse.getSeatbid().get(0).getBidIterator().next().setNurl("${AUCTION_PRICE}${AUCTION_CURRENCY}");
        final StringBuilder responseAdm = new StringBuilder();
        responseAdm.append("<html><body style=\"margin:0;padding:0;\">");
        responseAdm
                .append("<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
        responseAdm
                .append("<img src=\'?b=${WIN_BID}\' height=1 width=1 border=0 /><img src=\'${AUCTION_PRICE}${AUCTION_CURRENCY}\' height=1 width=1 border=0 />");
        responseAdm.append("</body></html>");
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String beaconUrl = "";
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        sas.setDst(2);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 15,
                repositoryHelper);
        final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
        assertEquals(responseAdm.toString(), rtbAdNetwork.getResponseContent());
        rtbAdNetwork.setEncryptedBid("0.23");
        rtbAdNetwork.setSecondBidPrice(0.23);
        final String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
        assertEquals(afterMacros, rtbAdNetwork.getResponseContent());
    }
    
    @Test
    public void testWapWithTransparency() {

        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setRqAdType("");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");


        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("wap");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.setAppType("Games");
        builder.setTransparencyEnabled(true);
        builder.setSiteUrl("www.inmobi.com");
        sas.setWapSiteUACEntity(new WapSiteUACEntity(builder));
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
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
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setRqAdType("");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");


        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("wap");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.setAppType("Games");
        sas.setWapSiteUACEntity(new WapSiteUACEntity(builder));
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
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
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setRqAdType("");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");


        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("wap");
        sas.setWapSiteUACEntity(null);
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
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
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setRqAdType("");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");


        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("app");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.setAppType("Games");
        builder.setTransparencyEnabled(true);
        builder.setSiteUrl("www.inmobi.com");
        builder.setBundleId("com.android.app");
        sas.setWapSiteUACEntity(new WapSiteUACEntity(builder));
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals("Games", rtbAdNetwork.getBidRequest().getApp().getName());
        assertEquals("com.android.app", rtbAdNetwork.getBidRequest().getApp().getBundle());
        assertEquals("some_site_id", rtbAdNetwork.getBidRequest().getApp().getId());

    }

    @Test
    public void testAppWithBlind() {

        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setRqAdType("");
        sas.setRqAdType("");
        sas.setSiteId("some_site_id");
        sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");


        // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
        sas.setSource("app");
        WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
        builder.setAppType("Games");
        sas.setWapSiteUACEntity(new WapSiteUACEntity(builder));
        sas.setCategories(Lists.newArrayList(11L, 12L, 15L));
        rtbAdNetwork.setSiteBlinded(false);
        rtbAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, "", "", (short) 15,
                repositoryHelper);
        // 11 mean Games. Refer to CategoryList
        assertEquals("Games", rtbAdNetwork.getBidRequest().getApp().getName());
        assertEquals("com.ix.7dea362b-3fac-3e00-956a-4952a3d4f474", rtbAdNetwork.getBidRequest().getApp().getBundle());
        assertEquals("7dea362b-3fac-3e00-956a-4952a3d4f474", rtbAdNetwork.getBidRequest().getApp().getId());
    }
}
