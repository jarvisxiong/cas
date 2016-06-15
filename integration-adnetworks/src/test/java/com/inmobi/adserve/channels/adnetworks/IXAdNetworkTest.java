package com.inmobi.adserve.channels.adnetworks;

import static com.inmobi.casthrift.DemandSourceType.IX;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.support.membermodification.MemberMatcher;

import com.google.common.collect.Lists;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
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
import com.inmobi.adserve.channels.entity.WapSiteUACEntity.Builder;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.adserve.contracts.ix.response.Bid;
import com.inmobi.adserve.contracts.ix.response.BidResponse;
import com.inmobi.adserve.contracts.ix.response.SeatBid;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.gson.GsonManager;

import io.netty.channel.Channel;

public class IXAdNetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String ixHost = "http://exapi-us-east.rubiconproject.com/a/api/exchange.json?tk_sdc=us-east";
    private static final SASRequestParameters sas = new SASRequestParameters();
    private static final String ixAdvId = "id";
    private static final int OS_ID = 14;
    private static final Short SLOT_ID = 15;
    private static final String SITE_ID = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final long COUNTRY_ID = 94L;
    private static Configuration mockConfig = null;
    private static IXAdNetwork ixAdNetwork;
    private static BidResponse bidResponse;
    private static RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        final String advertiserName = "ix";
        expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".unknownAdvId")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".urlArg")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".rtbVer", "2.0")).andReturn("2.0").anyTimes();
        expect(mockConfig.getString(advertiserName + ".wnUrlback")).andReturn("").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(true).anyTimes();
        expect(mockConfig.getString(advertiserName + ".ixMethod")).andReturn("").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getString(advertiserName + ".userName")).andReturn("test").anyTimes();
        expect(mockConfig.getString(advertiserName + ".password")).andReturn("api").anyTimes();
        expect(mockConfig.getInt(advertiserName + ".accountId")).andReturn(11726).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".htmlSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", false)).andReturn(false).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".bidFloorPercent", 100)).andReturn(100).anyTimes();
        expect(mockConfig.getString(advertiserName + ".sprout.uniqueIdentifierRegex", "(?s).*data-creative[iI]d.*"))
                .andReturn("(?s)data-creativeId").anyTimes();
        expect(mockConfig.getInt(advertiserName + ".vast.minimumSupportedSdkVersion", 450)).andReturn(450).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".vast.defaultTrafficPercentage", 50)).andReturn(50).anyTimes();
        expect(mockConfig.getStringArray("ix.blockedAdvertisers")).andReturn(new String[] {"king.com", "supercell.net",
                "paps.com", "fhs.com", "china.supercell.com", "supercell.com"}).anyTimes();
        expect(mockConfig.getList("ix.globalBlind")).andReturn(new ArrayList<>(Arrays.asList("1", "2"))).anyTimes();
        expect(mockConfig.getDouble(advertiserName + ".rubiconCutsInDeal", 0.0)).andReturn(0.0725).anyTimes();
        replay(mockConfig);
    }

    @Before
    public void setUp() throws Exception {
        final DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
        defaultConfiguration.setGsonManager(new GsonManager());
        MemberMatcher.field(IXAdNetwork.class, "templateConfiguration").set(IXAdNetwork.class, defaultConfiguration);

        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        sas.setCountryId(COUNTRY_ID);
        sas.setOsId(OS_ID);
        sas.setSiteId(SITE_ID);
        sas.setSource("APP");
        sas.setCarrierId(0);
        sas.setDst(8);
        sas.setDemandSourceType(IX);
        final String urlBase = "";
        final CurrencyConversionEntity currencyConversionEntity = EasyMock.createMock(CurrencyConversionEntity.class);

        expect(currencyConversionEntity.getConversionRate()).andReturn(10.0).anyTimes();
        EasyMock.replay(currencyConversionEntity);
        repositoryHelper = EasyMock.createNiceMock(RepositoryHelper.class);
        expect(repositoryHelper.queryCurrencyConversionRepository(EasyMock.isA(String.class)))
                .andReturn(currencyConversionEntity).anyTimes();
        expect(repositoryHelper.queryIXBlocklistRepository(anyObject(String.class), anyObject(IXBlocklistKeyType.class),
                anyObject(IXBlocklistType.class))).andReturn(null).anyTimes();

        final SlotSizeMapEntity slotSizeMapEntityFor1 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor1.getDimension()).andReturn(new Dimension(120, 20)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor1);
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor12 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor12.getDimension()).andReturn(new Dimension(468, 60)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor12);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4)).andReturn(slotSizeMapEntityFor4)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9)).andReturn(slotSizeMapEntityFor9)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11)).andReturn(slotSizeMapEntityFor11)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 12)).andReturn(slotSizeMapEntityFor12)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14)).andReturn(slotSizeMapEntityFor14)
                .anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15)).andReturn(slotSizeMapEntityFor15)
                .anyTimes();

        expect(repositoryHelper.queryIXPackageRepository(OS_ID, SITE_ID, (int) COUNTRY_ID, SLOT_ID))
                .andReturn(null).anyTimes();
        EasyMock.replay(repositoryHelper);

        ixAdNetwork = new IXAdNetwork(mockConfig, null, base, serverChannel, urlBase, "ix");

        final Field asyncHttpClientProviderField = IXAdNetwork.class.getDeclaredField("asyncHttpClientProvider");
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

        final Bid bid2 = new Bid("ab73dd4868a0bbadf8fd7527d95136b4", "impressionId", 2.4028260707855225);
        bid2.setCrid("CRID");
        bid2.setAdm(
                "<style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='http://www.inmobi.com/' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
        bid2.setEstimated(0);
        bid2.setPmptier(3);
        final List<Bid> bidList = new ArrayList<Bid>();
        bidList.add(bid2);
        final SeatBid seatBid = new SeatBid(bidList);
        seatBid.setSeat("TO-BE-DETERMINED");
        final List<SeatBid> seatBidList = new ArrayList<SeatBid>();
        seatBidList.add(seatBid);
        bidResponse = new BidResponse("SGu1Jpq1IO", seatBidList);
        bidResponse.setBidid("ac1a2c944cff0a176643079625b0cad4a1bbe4a3");

        ixAdNetwork.setBidResponse(bidResponse);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.setHost(ixHost);
    }



    @Test
    public void testGetRequestUri() throws URISyntaxException {
        final URI uri = new URI("urlBase");
        ixAdNetwork.setUrlArg("urlArg");
        ixAdNetwork.setHost("urlBase");
        assertEquals(uri, ixAdNetwork.getRequestUri());
    }

    @Test
    public void testConfigureParameters() {
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sas.setRemoteHostIp("206.29.182.240");
        sas.setSource("wap");
        sas.setUserAgent(
                "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalSiteKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                repositoryHelper), false);
    }

    @Test
    public void testShouldNotConfigureAdapterIfSiteIdNotSentInAdditionalParams() {
        boolean adapterCreated = true;
        final String externalSiteKey = "f6wqjq1r5v";

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null, 0,
                            null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"3\":\"160212\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                            new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sas.setRemoteHostIp("206.29.182.240");
            sas.setSiteId(SITE_ID);
            sas.setSource("wap");
            final Builder builder = WapSiteUACEntity.newBuilder();
            // builder.setAppType("Games");
            sas.setWapSiteUACEntity(builder.build());
            sas.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sas.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertFalse(adapterCreated);

            sas.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertFalse(adapterCreated);
        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }

    }

    public void testShouldNotConfigureAdapterIfZoneIdNotSentInAdditionalParams() {
        boolean adapterCreated = true;
        final String externalSiteKey = "f6wqjq1r5v";

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null, 0,
                            null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null, null, 0,
                            new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sas.setRemoteHostIp("206.29.182.240");
            sas.setSiteId(SITE_ID);
            sas.setSource("wap");
            final Builder builder = WapSiteUACEntity.newBuilder();
            // builder.setAppType("Games");
            sas.setWapSiteUACEntity(builder.build());
            sas.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sas.setUserAgent(
                    "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertFalse(adapterCreated);

            sas.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertFalse(adapterCreated);
        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }
    }

    @Test
    public void testShouldSetRightBlindListIfTransparencyEnabled() {
        boolean adapterCreated = false;
        List<Integer> blindList;
        final String externalSiteKey = "f6wqjq1r5v";

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null, 0,
                            null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null,
                            null, 0, new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sas.setRemoteHostIp("206.29.182.240");
            sas.setSiteId(SITE_ID);
            sas.setSource("wap");
            final Builder builder = WapSiteUACEntity.newBuilder();
            builder.isTransparencyEnabled(false);
            builder.bundleId("com.play.google.testApp.bundleId");
            builder.marketId("com.play.google.testApp.marketId");
            builder.siteUrl("http://www.testSite.com");
            sas.setWapSiteUACEntity(builder.build());
            sas.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sas.setDemandSourceType(IX);
            sas.setUserAgent(
                    "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");

            // Test case for transparency false
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().getBlind(), (Integer) 1);
            assertNotNull(ixAdNetwork.getBidRequest().getSite().getExt().getBlind().getDomain());

            sas.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getId(), sas.getSiteId());
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().getBlind(), (Integer) 1);
            assertNotNull(ixAdNetwork.getBidRequest().getApp().getExt().getBlind().getBundle());
            assertEquals(ixAdNetwork.getBidRequest().getApp().getExt().getBlind().getBundle(),
                    "com.ix.7dea362b-3fac-3e00-956a-4952a3d4f474");
            assertEquals(ixAdNetwork.getBidRequest().getApp().getBundle(),
                    "com.ix.7dea362b-3fac-3e00-956a-4952a3d4f474");

            // Test Cases for transparency=true
            blindList = new ArrayList<Integer>(Arrays.asList(1, 2));

            // Test case when site_blind_list and pub_blind_list are null, should take global blind list, set above.
            sas.setSource("wap");
            builder.isTransparencyEnabled(true);
            sas.setWapSiteUACEntity(builder.build());

            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getId(), sas.getSiteId());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().getBlind(), (Integer) 0);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().getBlindbuyers(), blindList);
            assertNotNull(ixAdNetwork.getBidRequest().getSite().getExt().getBlind().getDomain());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getExt().getBlind().getDomain(),
                    "http://www.ix.com/7dea362b-3fac-3e00-956a-4952a3d4f474");
            assertEquals(ixAdNetwork.getBidRequest().getSite().getPage(), "http://www.testSite.com");

            sas.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getId(), sas.getSiteId());
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().getBlind(), (Integer) 0);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().getBlindbuyers(), blindList);
            assertNotNull(ixAdNetwork.getBidRequest().getApp().getExt().getBlind().getBundle());
            assertEquals(ixAdNetwork.getBidRequest().getApp().getBundle(), "com.play.google.testApp.marketId");

            // Test case when site_blind_list or pub_blind_list is present
            sas.setSource("wap");
            builder.isTransparencyEnabled(true);
            blindList = new ArrayList<Integer>(Arrays.asList(8, 2, 3));
            builder.blindList(blindList);
            sas.setWapSiteUACEntity(builder.build());

            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getId(), sas.getSiteId());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().getBlind(), (Integer) 0);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().getBlindbuyers(), blindList);
            assertNotNull(ixAdNetwork.getBidRequest().getSite().getExt().getBlind().getPage());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getExt().getBlind().getDomain(),
                    "http://www.ix.com/7dea362b-3fac-3e00-956a-4952a3d4f474");

            sas.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getId(), sas.getSiteId());
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().getBlind(), (Integer) 0);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().getBlindbuyers(), blindList);
            assertNotNull(ixAdNetwork.getBidRequest().getApp().getExt().getBlind().getBundle());

        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }

    }

    @Test
    public void testShouldSetSensitivityCorrectlyInAdQuality() {
        boolean adapterCreated = false;
        final String externalSiteKey = "f6wqjq1r5v";

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null, 0,
                            null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null,
                            null, 0, new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sas.setRemoteHostIp("206.29.182.240");
            sas.setSiteId(SITE_ID);

            final Builder builder = WapSiteUACEntity.newBuilder();
            sas.setWapSiteUACEntity(builder.build());
            sas.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sas.setDemandSourceType(IX);
            sas.setUserAgent(
                    "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");

            sas.setSource("wap");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getAq().getSensitivity(), "high");


            sas.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getAq().getSensitivity(), "high");

            // if site type is performance
            sas.setSiteContentType(ContentType.PERFORMANCE);

            sas.setSource("wap");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getAq().getSensitivity(), "low");


            sas.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getAq().getSensitivity(), "low");


        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }

    }


    @Test
    public void testShouldSetSiteOrAppNotBoth() {
        boolean adapterCreated = false;
        final String externalSiteKey = "f6wqjq1r5v";
        Builder builder;

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null, 0,
                            null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null,
                            null, 0, new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sas.setRemoteHostIp("206.29.182.240");
            sas.setSiteId(SITE_ID);
            sas.setSource("wap");
            sas.setSiteContentType(ContentType.PERFORMANCE);
            sas.setSiteIncId(423);
            builder = WapSiteUACEntity.newBuilder();
            builder.appType("Games");
            builder.siteName("TESTSITE");
            builder.siteUrl("www.testSite.com");
            builder.isTransparencyEnabled(true);
            sas.setWapSiteUACEntity(builder.build());
            sas.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sas.setDemandSourceType(IX);
            sas.setUserAgent(
                    "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);
            List<Integer> apiFrameworkValues = ixAdNetwork.getBidRequest().getImp().get(0).getBanner().getApi();
            assertNull(apiFrameworkValues); // Will not set any apiFramework values for wap sites.

            assertTrue(adapterCreated);
            assertNull(ixAdNetwork.getBidRequest().getApp());
            assertNotNull(ixAdNetwork.getBidRequest().getSite());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getName(), "TESTSITE");
            assertEquals(ixAdNetwork.getBidRequest().getSite().getPage(), "www.testSite.com");
            assertEquals(ixAdNetwork.getBidRequest().getSite().getBlocklists(),
                    Lists.newArrayList("blk423", "InMobiPERFAdv", "InMobiPERFInd", "InMobiPERFCre"));
            assertEquals(ixAdNetwork.getBidRequest().getSite().getPublisher().getExt().getRp().getAccount_id(),
                    (Integer) 11726);

            // checking for blocked list if siteType is not PERFORMANCE, also if site is not transparent

            sas.setSiteContentType(ContentType.FAMILY_SAFE);
            sas.setSiteIncId(423);
            builder = WapSiteUACEntity.newBuilder();
            builder.appType("Games");
            builder.siteName("TESTSITE");
            builder.siteUrl("www.testSite.com");
            builder.isTransparencyEnabled(false);

            sas.setWapSiteUACEntity(builder.build());

            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);

            assertTrue(adapterCreated);
            assertNull(ixAdNetwork.getBidRequest().getApp());
            assertNotNull(ixAdNetwork.getBidRequest().getSite());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getId(), SITE_ID);
            // assertEquals(ixAdNetwork.getBidRequest().getSite().getId(), getBlindedSiteId(sasParams.getSiteIncId(),
            // entity.getIncId(getCreativeType())));
            assertEquals(ixAdNetwork.getBidRequest().getSite().getName(), "Games");
            assertNotNull(ixAdNetwork.getBidRequest().getSite().getPage());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getBlocklists(),
                    Lists.newArrayList("blk423", "InMobiFSAdv", "InMobiFSInd", "InMobiFSCre"));
            assertEquals(ixAdNetwork.getBidRequest().getSite().getPublisher().getExt().getRp().getAccount_id(),
                    (Integer) 11726);

            sas.setSource("app");
            sas.setSdkVersion("a430");
            adapterCreated = ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15,
                    repositoryHelper);
            apiFrameworkValues = ixAdNetwork.getBidRequest().getImp().get(0).getBanner().getApi();
            assertEquals(5, apiFrameworkValues.get(0).intValue());
            assertEquals(3, apiFrameworkValues.get(1).intValue());
            assertEquals(1001, apiFrameworkValues.get(2).intValue());
            assertEquals(1000, apiFrameworkValues.get(3).intValue());

            assertTrue(adapterCreated);
            assertNull(ixAdNetwork.getBidRequest().getSite());
            assertNotNull(ixAdNetwork.getBidRequest().getApp());
            assertNotNull(ixAdNetwork.getBidRequest().getApp().getExt().getBlind().getBundle());

            sas.setSource("app");
            sas.setSdkVersion("a350");
            ixAdNetwork.configureParameters(sas, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
            apiFrameworkValues = ixAdNetwork.getBidRequest().getImp().get(0).getBanner().getApi();
            assertNull(apiFrameworkValues); // Will not set any api framework values for sdk version < 370

        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }
    }

}
