package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.adnetworks.AdapterTestHelper.setInmobiAdTrackerBuilderFactoryForTest;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.CPM;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.googlecode.cqengine.resultset.common.NoSuchObjectException;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.natives.IxNativeBuilderImpl;
import com.inmobi.adserve.channels.api.natives.NativeBuilderFactory;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.adserve.channels.util.demand.enums.DemandAdFormatConstraints;
import com.inmobi.adserve.contracts.ix.request.BidRequest;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.phoenix.batteries.util.WilburyUUID;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.config.DefaultGsonDeserializerConfiguration;
import com.inmobi.template.gson.GsonManager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

// TODO: Merge with IXAdNetworkTest.java
// TODO: Remove dependency on beacon and click changes
@RunWith(PowerMockRunner.class)
@PrepareForTest({IXAdNetwork.class, InspectorStats.class, IXPackageMatcher.class})
@PowerMockIgnore("javax.crypto.*")
public class NewIXAdNetworkTest {
    private static final String advertiserName = "ix";
    private static Configuration mockConfig;
    private static RepositoryHelper repositoryHelper;

    private static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("advertiserId").anyTimes();
        expect(mockConfig.getString(advertiserName + ".unknownAdvId")).andReturn("2770").anyTimes();
        expect(mockConfig.getString(advertiserName + ".urlArg")).andReturn("urlArg").anyTimes();
        expect(mockConfig.getString(advertiserName + ".wnUrlback")).andReturn(
                "http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}")
                .anyTimes();
        expect(mockConfig.getString(advertiserName + ".ixMethod")).andReturn("ixMethod").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getString(advertiserName + ".userName")).andReturn("userName").anyTimes();
        expect(mockConfig.getString(advertiserName + ".password")).andReturn("password").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".htmlSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", false)).andReturn(true).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".accountId")).andReturn(1).anyTimes();
        expect(mockConfig.getList(advertiserName + ".globalBlind")).andReturn(Lists.newArrayList("123")).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".bidFloorPercent", 100)).andReturn(100).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".vast.minimumSupportedSdkVersion", 450)).andReturn(450).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".vast.defaultTrafficPercentage", 50)).andReturn(50).anyTimes();
        expect(mockConfig.getString(advertiserName + ".sprout.uniqueIdentifierRegex", "(?s).*data-creative[iI]d.*"))
                .andReturn("(?s).*data-creative[iI]d.*").anyTimes();
        expect(mockConfig.getString("key.1.value")).andReturn("Secret Key").anyTimes();
        expect(mockConfig.getString("key.2.value")).andReturn("Secret Key").anyTimes();
        expect(mockConfig.getString("beaconURLPrefix")).andReturn("BeaconPrefix").anyTimes();
        expect(mockConfig.getString("clickURLPrefix")).andReturn("ClickPrefix").anyTimes();
        replayAll();
    }

    @BeforeClass
    public static void setUp() throws IllegalAccessException {
        final DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
        defaultConfiguration.setGsonManager(new GsonManager(new DefaultGsonDeserializerConfiguration()));
        MemberMatcher.field(IXAdNetwork.class, "templateConfiguration").set(IXAdNetwork.class, defaultConfiguration);

        prepareMockConfig();
        DefaultLazyInmobiAdTrackerBuilder.init(mockConfig);
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);

        expect(repositoryHelper.querySlotSizeMapRepository((short) 4)).andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9)).andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11)).andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14)).andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15)).andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);

        final short hostIdCode = (short) 5;
        final byte dataCenterIdCode = 1;
        ImpressionIdGenerator.init(hostIdCode, dataCenterIdCode);
    }

    @Test
    public void testParseResponseNoAd() {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        expect(mockStatus.code()).andReturn(404).times(4).andReturn(200).times(6);
        replayAll();

        final String response1 = "";
        final String response2 = "Dummy";
        final String response3 = null;
        final String response4 =
                "{\"id\":\"ce3adf2d-0149-1000-e483-3e96d9a8a2c1\",\"bidid\":\"1bc93e72-3c81-4bad-ba35-9458b54e109a\",\"seatbid\":[{\"bid\":[]}],\"statuscode\":10}";
        final IXAdNetwork ixAdNetwork = new IXAdNetwork(mockConfig, null, null, null, null, advertiserName, false);

        ixAdNetwork.parseResponse(response1, mockStatus);
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(404)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));

        ixAdNetwork.parseResponse(response2, mockStatus);
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(404)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));

        ixAdNetwork.parseResponse(response1, mockStatus);
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(500)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));

        ixAdNetwork.parseResponse(response3, mockStatus);
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(500)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));

        ixAdNetwork.parseResponse(response4, mockStatus);
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(500)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));
    }

    @Test
    public void testParseResponseFailedDeserialization() {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        expect(mockStatus.code()).andReturn(200).times(2);
        replayAll();

        final String response = "{INVALID_JSON}";
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, "getName");
        expect(ixAdNetwork.getName()).andReturn("ix").times(1);
        replayAll();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(500)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("TERM")));
    }

    @Test
    public void testParseResponsePassedDeserializationBannerBuilding() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("wap").times(3);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("SdkVer").times(1);
        expect(mockSasParams.getCarrierId()).andReturn(0).times(1);
        expect(mockSasParams.getIpFileVersion()).andReturn(0).times(1);
        expect(mockSasParams.getDst()).andReturn(8).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException())
                .anyTimes();

        replayAll();

        final String response = TestUtils.SampleStrings.ixResponseJson;

        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateRPAccountInfo"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixResponseADM).anyTimes();
        expect(ixAdNetwork.updateRPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<html><head><style type=\"text/css\">#im_1011_ad{display: table;}#im_1011_p{vertical-align: middle; text-align: center;}</style></head><body style=\"margin:0;padding:0;\"><div id=\"im_1011_ad\" style=\"width:100%;height:100%\"><div id=\"im_1011_p\" style=\"width:100%;height:100%\" class=\"im_1011_bg\"><style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/apps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akamaihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p><script>function clickHandler(){var x=document.createElement(\"img\");x.setAttribute(\"src\", \"ClickPrefix/C/b/0/0/0/0/0/u/0/0/0/x/c124b6b5-0148-1000-c54a-00012e330000/-1/0/-1/1/0/x/0/nw/101/7/-1/-1/-1/eA~~/_DwcFv-_v-vf0qTbfRb_____3____3UAFgEAAEwAAA/1/242d80c6\");x.setAttribute(\"height\", \"1\");x.setAttribute(\"width\", \"1\");x.setAttribute(\"border\", \"0\");document.body.appendChild(x);document.removeEventListener('click',clickHandler);};document.addEventListener('click',clickHandler);</script></div></div><div style=\"display: none;\"><img src='BeaconPrefix/C/b/0/0/0/0/0/u/0/0/0/x/c124b6b5-0148-1000-c54a-00012e330000/-1/0/-1/0/0/x/0/nw/101/7/-1/-1/-1/eA~~/_DwcFv-_v-vf0qTbfRb_____3____3UAFgEAAEwAAA/1/c7e1f87e?b=${WIN_BID}${DEAL_GET_PARAM}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></div></body></html>")));
    }

    // @Test
    public void testCAUAdBuilding() throws Exception {
        mockStaticNice(InspectorStats.class);
        mockStaticNice(IXPackageMatcher.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        final JSONObject additionalParams = new JSONObject();
        additionalParams.put("site", "12345");
        additionalParams.put("default", "98766");

        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getAdditionalParams()).andReturn(additionalParams).anyTimes();
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(
                mockRepositoryHelper.queryIXBlocklistRepository(anyObject(String.class),
                        anyObject(IXBlocklistKeyType.class), anyObject(IXBlocklistType.class))).andReturn(null)
                .anyTimes();
        replayAll();

        final IXAdNetwork ixAdNetwork =
                new IXAdNetwork(mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                        advertiserName, true);

        final WapSiteUACEntity.Builder wapBuild = new WapSiteUACEntity.Builder();
        wapBuild.setTransparencyEnabled(true);

        final SASRequestParameters sas = new SASRequestParameters();
        sas.setRemoteHostIp("10.14.112.15");
        sas.setUserAgent("userAgent");
        sas.setSource("APP");
        sas.setSiteIncId(1234L);
        sas.setImpressionId("ImpressionId");
        sas.setSiteId("siteId");
        sas.setPlacementId(99L);
        sas.setWapSiteUACEntity(wapBuild.build());
        sas.setCountryId(1L);
        sas.setDst(8);
        sas.setSiteContentType(ContentType.PERFORMANCE);
        sas.setCustomTemplatesOnly(true);

        final CasInternalRequestParameters casInt = new CasInternalRequestParameters();
        casInt.setImpressionId("ImpressionId");

        ixAdNetwork.configureParameters(sas, casInt, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        // final BidRequest bidReq = ixAdNetwork.getBidRequest();

        // assertThat(ixAdNetwork.isNativeRequest(), is(true));
        // assertThat(bidReq.getImp().get(0).getNat().getRequestobj().getAssets().isEmpty(), is(false));
        // assertThat(bidReq.getImp().get(0).getNat().getRequestobj().getAssets().size(), is(6));
    }

    @Test
    public void testNativeAdBuilding() throws Exception {
        mockStaticNice(InspectorStats.class);
        mockStaticNice(IXPackageMatcher.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final NativeBuilderFactory mockNativeBuilderfactory = createMock(NativeBuilderFactory.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        final NativeAdTemplateEntity.Builder builder = NativeAdTemplateEntity.newBuilder();
        builder.mandatoryKey("layoutConstraint.1");
        final NativeAdTemplateEntity entity = builder.build();

        final JSONObject additionalParams = new JSONObject();
        additionalParams.put("site", "12345");
        additionalParams.put("default", "98766");

        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getAdditionalParams()).andReturn(additionalParams).anyTimes();
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockNativeBuilderfactory.create(entity)).andReturn(new IxNativeBuilderImpl(entity));
        expect(mockRepositoryHelper.queryNativeAdTemplateRepository(99L)).andReturn(entity);
        expect(
                mockRepositoryHelper.queryIXBlocklistRepository(anyObject(String.class),
                        anyObject(IXBlocklistKeyType.class), anyObject(IXBlocklistType.class))).andReturn(null)
                .anyTimes();
        replayAll();

        final Field nativeBuilderfactoryField = IXAdNetwork.class.getDeclaredField("nativeBuilderfactory");
        nativeBuilderfactoryField.setAccessible(true);
        nativeBuilderfactoryField.set(null, mockNativeBuilderfactory);

        final IXAdNetwork ixAdNetwork =
                new IXAdNetwork(mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                        advertiserName, true);

        final WapSiteUACEntity.Builder wapBuild = new WapSiteUACEntity.Builder();
        wapBuild.setTransparencyEnabled(true);

        final SASRequestParameters sas = new SASRequestParameters();
        sas.setRemoteHostIp("10.14.112.15");
        sas.setUserAgent("userAgent");
        sas.setSource("APP");
        sas.setRFormat("native");
        sas.setSiteIncId(1234L);
        sas.setImpressionId("ImpressionId");
        sas.setSiteId("siteId");
        sas.setPlacementId(99L);
        sas.setWapSiteUACEntity(wapBuild.build());
        sas.setCountryId(1L);
        sas.setDst(8);
        sas.setSiteContentType(ContentType.PERFORMANCE);

        final CasInternalRequestParameters casInt = new CasInternalRequestParameters();
        casInt.setImpressionId("ImpressionId");

        ixAdNetwork.configureParameters(sas, casInt, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        final BidRequest bidReq = ixAdNetwork.getBidRequest();

        assertThat(ixAdNetwork.isNativeRequest(), is(true));
        assertThat(bidReq.getImp().get(0).getNat().getRequestobj().getAssets().isEmpty(), is(false));
        assertThat(bidReq.getImp().get(0).getNat().getRequestobj().getAssets().size(), is(6));
    }

    @Test
    public void testParseResponsePassedDeserializationRichMediaBuildingCoppaDisabled() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockSasParams.getCountryCode()).andReturn("55").times(1);
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.BANNER).anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js")
                .anyTimes();
        expect(mockSasParams.getDst()).andReturn(8).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException())
                .anyTimes();
        expect(mockCasInternalRequestParameters.getLatLong()).andReturn("123.45,678.90").anyTimes();
        expect(mockCasInternalRequestParameters.getZipCode()).andReturn("560103").anyTimes();
        expect(mockCasInternalRequestParameters.getAuctionId()).andReturn("AuctionId").anyTimes();
        expect(mockCasInternalRequestParameters.getUidIFA()).andReturn("userId").anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateRPAccountInfo"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.isSproutSupported = true;
        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixStudioResponseAdTag).anyTimes();
        expect(ixAdNetwork.updateRPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParameters, mockChannelSegmentEntity,
                (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, height=device-height,user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><base href=\"http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js\"></base><style type=\"text/css\">#im_1011_ad{display: table;}#im_1011_p{vertical-align: middle; text-align: center;}body{overflow:hidden;}</style></head><body style=\"margin:0;padding:0;\"><div id=\"im_1011_ad\" style=\"width:100%;height:100%\"><div id=\"im_1011_p\" style=\"width:100%;height:100%\" class=\"im_1011_bg\"><script src=\"mraid.js\"></script><div id=\"Sprout_ShCMGj4G1A4GIIsw_div\" data-creativeId=\"ShCMGj4G1A4GIIsw\"></div><script type=\"text/javascript\">var _Sprout = _Sprout || {};/* 3rd Party Impression Tracker: a tracking pixel URL for tracking 3rd party impressions */_Sprout.impressionTracker = \"PUT_IMPRESSION_TRACKER_HERE\";/* 3rd Party Click Tracker: A URL or Macro like %c for third party exit tracking */_Sprout.clickTracker = \"PUT_CLICK_TRACKER_HERE\";/* Publisher Label: What you want to call this line-item in Studio reports */_Sprout.publisherLabel = \"PUT_PUBLISHER_LABEL_HERE\";_Sprout._inMobiAdTagTracking={st:new Date().getTime(),rr:0};Sprout[\"ShCMGj4G1A4GIIsw\"]={querystring:{im_curl:\"BeaconPrefix\\/C\\/b\\/0\\/0\\/0\\/0\\/0\\/u\\/0\\/0\\/0\\/x\\/c124b6b5-0148-1000-c54a-00012e330000\\/-1\\/0\\/-1\\/0\\/0\\/x\\/0\\/nw\\/101\\/7\\/-1\\/-1\\/-1\\/eA~~\\/6AZCQU5ORVIcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA\\/1\\/ab0e03eb?b=${WIN_BID}${DEAL_GET_PARAM}\",im_sdk:\"a450\",click:\"ClickPrefix\\/C\\/b\\/0\\/0\\/0\\/0\\/0\\/u\\/0\\/0\\/0\\/x\\/c124b6b5-0148-1000-c54a-00012e330000\\/-1\\/0\\/-1\\/1\\/0\\/x\\/0\\/nw\\/101\\/7\\/-1\\/-1\\/-1\\/eA~~\\/6AZCQU5ORVIcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA\\/1\\/82970ec2\",adFormat:\"interstitial\",im_recordEventFun:\"\",geo_lat:\"123.45\",geo_lng:\"678.9\",geo_cc:\"55\",geo_zip:\"560103\",js_esc_geo_city:\"\",openLandingPage:\"\"}};var _sproutReadyEvt=document.createEvent(\"Event\");_sproutReadyEvt.initEvent(\"sproutReady\",true,true);window.dispatchEvent(_sproutReadyEvt);var sr, sp=\"/load/ShCMGj4G1A4GIIsw.inmobi.html.review.js?_t=\"(Date.now())\"\", _Sprout_load=function(){var e=document.getElementsByTagName(\"script\"),e=e[e.length-1],t=document.createElement(\"script\");t.async=!0;t.type=\"text/javascript\";(https:==document.location.protocol?sr=\"http://farm.sproutbuilder.com\":sr=\"http://farm.sproutbuilder.com\");t.src=sr+sp;e.parentNode.insertBefore(t,e.nextSibling)};\"0\"===window[\"_Sprout\"][\"ShCMGj4G1A4GIIsw\"][\"querystring\"][\"__im_sdk\"]||\"complete\"===document.readyState?_Sprout_load():window.addEventListener(\"load\",_Sprout_load,!1)</script><script>function clickHandler(){var x=document.createElement(\"img\");x.setAttribute(\"src\", \"ClickPrefix/C/b/0/0/0/0/0/u/0/0/0/x/c124b6b5-0148-1000-c54a-00012e330000/-1/0/-1/1/0/x/0/nw/101/7/-1/-1/-1/eA~~/6AZCQU5ORVIcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA/1/82970ec2\");x.setAttribute(\"height\", \"1\");x.setAttribute(\"width\", \"1\");x.setAttribute(\"border\", \"0\");document.body.appendChild(x);document.removeEventListener('click',clickHandler);};document.addEventListener('click',clickHandler);</script></div></div><div style=\"display: none;\"><img src='BeaconPrefix/C/b/0/0/0/0/0/u/0/0/0/x/c124b6b5-0148-1000-c54a-00012e330000/-1/0/-1/0/0/x/0/nw/101/7/-1/-1/-1/eA~~/6AZCQU5ORVIcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA/1/ab0e03eb?b=${WIN_BID}${DEAL_GET_PARAM}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></div></body></html>")));
    }

    @Test
    public void testParseResponsePassedDeserializationRichMediaBuildingCoppaSet() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getCountryCode()).andReturn("55").times(1);
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.BANNER).anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js")
                .anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockSasParams.getDst()).andReturn(8).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException())
                .anyTimes();
        expect(mockCasInternalRequestParameters.getLatLong()).andReturn("123.45,678.90").anyTimes();
        expect(mockCasInternalRequestParameters.getZipCode()).andReturn("560103").anyTimes();
        expect(mockCasInternalRequestParameters.getAuctionId()).andReturn("AuctionId").anyTimes();
        expect(mockCasInternalRequestParameters.getUidIFA()).andReturn("userId").anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateRPAccountInfo"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.isSproutSupported = true;
        ixAdNetwork.setHost("http://localhost:8080/getIXBid");
        ixAdNetwork.isCoppaSet = true;

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixStudioResponseAdTag).anyTimes();
        expect(ixAdNetwork.updateRPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParameters, mockChannelSegmentEntity,
                (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, height=device-height,user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><base href=\"http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js\"></base><style type=\"text/css\">#im_1011_ad{display: table;}#im_1011_p{vertical-align: middle; text-align: center;}body{overflow:hidden;}</style></head><body style=\"margin:0;padding:0;\"><div id=\"im_1011_ad\" style=\"width:100%;height:100%\"><div id=\"im_1011_p\" style=\"width:100%;height:100%\" class=\"im_1011_bg\"><script src=\"mraid.js\"></script><div id=\"Sprout_ShCMGj4G1A4GIIsw_div\" data-creativeId=\"ShCMGj4G1A4GIIsw\"></div><script type=\"text/javascript\">var _Sprout = _Sprout || {};/* 3rd Party Impression Tracker: a tracking pixel URL for tracking 3rd party impressions */_Sprout.impressionTracker = \"PUT_IMPRESSION_TRACKER_HERE\";/* 3rd Party Click Tracker: A URL or Macro like %c for third party exit tracking */_Sprout.clickTracker = \"PUT_CLICK_TRACKER_HERE\";/* Publisher Label: What you want to call this line-item in Studio reports */_Sprout.publisherLabel = \"PUT_PUBLISHER_LABEL_HERE\";_Sprout._inMobiAdTagTracking={st:new Date().getTime(),rr:0};Sprout[\"ShCMGj4G1A4GIIsw\"]={querystring:{im_curl:\"BeaconPrefix\\/C\\/b\\/0\\/0\\/0\\/0\\/0\\/u\\/0\\/0\\/0\\/x\\/c124b6b5-0148-1000-c54a-00012e330000\\/-1\\/0\\/-1\\/0\\/0\\/x\\/0\\/nw\\/101\\/7\\/-1\\/-1\\/-1\\/eA~~\\/6AZCQU5ORVIcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA\\/1\\/ab0e03eb?b=${WIN_BID}${DEAL_GET_PARAM}\",im_sdk:\"a450\",click:\"ClickPrefix\\/C\\/b\\/0\\/0\\/0\\/0\\/0\\/u\\/0\\/0\\/0\\/x\\/c124b6b5-0148-1000-c54a-00012e330000\\/-1\\/0\\/-1\\/1\\/0\\/x\\/0\\/nw\\/101\\/7\\/-1\\/-1\\/-1\\/eA~~\\/6AZCQU5ORVIcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA\\/1\\/82970ec2\",adFormat:\"interstitial\",im_recordEventFun:\"\",geo_lat:\"\",geo_lng:\"\",geo_cc:\"\",geo_zip:\"\",js_esc_geo_city:\"\",openLandingPage:\"\"}};var _sproutReadyEvt=document.createEvent(\"Event\");_sproutReadyEvt.initEvent(\"sproutReady\",true,true);window.dispatchEvent(_sproutReadyEvt);var sr, sp=\"/load/ShCMGj4G1A4GIIsw.inmobi.html.review.js?_t=\"(Date.now())\"\", _Sprout_load=function(){var e=document.getElementsByTagName(\"script\"),e=e[e.length-1],t=document.createElement(\"script\");t.async=!0;t.type=\"text/javascript\";(https:==document.location.protocol?sr=\"http://farm.sproutbuilder.com\":sr=\"http://farm.sproutbuilder.com\");t.src=sr+sp;e.parentNode.insertBefore(t,e.nextSibling)};\"0\"===window[\"_Sprout\"][\"ShCMGj4G1A4GIIsw\"][\"querystring\"][\"__im_sdk\"]||\"complete\"===document.readyState?_Sprout_load():window.addEventListener(\"load\",_Sprout_load,!1)</script><script>function clickHandler(){var x=document.createElement(\"img\");x.setAttribute(\"src\", \"ClickPrefix/C/b/0/0/0/0/0/u/0/0/0/x/c124b6b5-0148-1000-c54a-00012e330000/-1/0/-1/1/0/x/0/nw/101/7/-1/-1/-1/eA~~/6AZCQU5ORVIcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA/1/82970ec2\");x.setAttribute(\"height\", \"1\");x.setAttribute(\"width\", \"1\");x.setAttribute(\"border\", \"0\");document.body.appendChild(x);document.removeEventListener('click',clickHandler);};document.addEventListener('click',clickHandler);</script></div></div><div style=\"display: none;\"><img src='BeaconPrefix/C/b/0/0/0/0/0/u/0/0/0/x/c124b6b5-0148-1000-c54a-00012e330000/-1/0/-1/0/0/x/0/nw/101/7/-1/-1/-1/eA~~/6AZCQU5ORVIcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA/1/ab0e03eb?b=${WIN_BID}${DEAL_GET_PARAM}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></div></body></html>")));
    }

    @Test
    public void testParseResponseFailedDeserializationRichMediaBuildingSdkLowerThan370() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a350").anyTimes();
        expect(mockSasParams.getCountryCode()).andReturn("55").times(1);
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.BANNER).anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js")
                .anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException())
                .anyTimes();
        expect(mockCasInternalRequestParameters.getLatLong()).andReturn("123.45,678.90").anyTimes();
        expect(mockCasInternalRequestParameters.getZipCode()).andReturn("560103").anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateRPAccountInfo"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.isSproutSupported = false;
        ixAdNetwork.setHost("http://localhost:8080/getIXBid");
        ixAdNetwork.isCoppaSet = true;

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixStudioResponseAdTag).anyTimes();
        expect(ixAdNetwork.updateRPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParameters, mockChannelSegmentEntity,
                (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
    }

    @Test
    public void testParseResponseFailedDeserializationRichMediaBuildingWAP() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("WAP").anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a350").anyTimes();
        expect(mockSasParams.getCountryCode()).andReturn("55").times(1);
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.BANNER).anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js")
                .anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException())
                .anyTimes();
        expect(mockCasInternalRequestParameters.getLatLong()).andReturn("123.45,678.90").anyTimes();
        expect(mockCasInternalRequestParameters.getZipCode()).andReturn("560103").anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateRPAccountInfo"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.isSproutSupported = false;
        ixAdNetwork.setHost("http://localhost:8080/getIXBid");
        ixAdNetwork.isCoppaSet = true;

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixStudioResponseAdTag).anyTimes();
        expect(ixAdNetwork.updateRPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParameters, mockChannelSegmentEntity,
                (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
    }


    @Test
    public void testParseResponsePassedDeserializationInterstitialBuildingForSDK450() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("imaiBaseUrl").anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockSasParams.getDst()).andReturn(8).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockChannelSegmentEntity.getDemandAdFormatConstraints()).andReturn(DemandAdFormatConstraints.STATIC)
                .anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException())
                .anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateRPAccountInfo"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixResponseADM).anyTimes();
        expect(ixAdNetwork.updateRPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<html><head><meta name=\"viewport\" content=\"width=device-width, height=device-height,user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><base href=\"imaiBaseUrl\"></base><style type=\"text/css\">#im_1011_ad{display: table;}#im_1011_p{vertical-align: middle; text-align: center;}</style></head><body style=\"margin:0;padding:0;\"><div id=\"im_1011_ad\" style=\"width:100%;height:100%\"><div id=\"im_1011_p\" style=\"width:100%;height:100%\" class=\"im_1011_bg\"><script src=\"mraid.js\" ></script><style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/apps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akamaihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p><script type=\"text/javascript\">var readyHandler=function(){_im_imai.fireAdReady();_im_imai.removeEventListener('ready',readyHandler);};_im_imai.addEventListener('ready',readyHandler);</script><script>function clickHandler(){var x=document.createElement(\"img\");x.setAttribute(\"src\", \"ClickPrefix/C/b/0/0/0/0/0/u/0/0/0/x/c124b6b5-0148-1000-c54a-00012e330000/-1/0/-1/1/0/x/0/nw/101/7/-1/-1/-1/eA~~/6AxJTlRFUlNUSVRJQUwcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA/1/fe832cb2\");x.setAttribute(\"height\", \"1\");x.setAttribute(\"width\", \"1\");x.setAttribute(\"border\", \"0\");document.body.appendChild(x);document.removeEventListener('click',clickHandler);};document.addEventListener('click',clickHandler);</script></div></div><div style=\"display: none;\"><img src='BeaconPrefix/C/b/0/0/0/0/0/u/0/0/0/x/c124b6b5-0148-1000-c54a-00012e330000/-1/0/-1/0/0/x/0/nw/101/7/-1/-1/-1/eA~~/6AxJTlRFUlNUSVRJQUwcPBwW_7-_69_SpNt9Fv_____f____dQAWAQAATAAA/1/2a9c304e?b=${WIN_BID}${DEAL_GET_PARAM}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></div></body></html>")));
    }

    @Test
    public void testUpdateDSPAccountInfo() throws Exception {
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final IXAccountMapEntity mockAccountEntity = createMock(IXAccountMapEntity.class);
        final ChannelAdGroupRepository mockChannelAdGroupRepo = createMock(ChannelAdGroupRepository.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);

        final String dummyAccountId = "2770";
        final long dummyIncId = 1234L;

        expect(mockRepositoryHelper.queryIXAccountMapRepository(2770L)).andReturn(null).times(1)
                .andReturn(mockAccountEntity).anyTimes();

        expect(mockAccountEntity.getInmobiAccountId()).andReturn(null).times(1).andReturn("").times(1)
                .andReturn(dummyAccountId).anyTimes();

        expect(mockRepositoryHelper.getChannelAdGroupRepository()).andReturn(null).times(1)
                .andReturn(mockChannelAdGroupRepo).anyTimes();

        expect(mockChannelAdGroupRepo.getEntities(dummyAccountId)).andReturn(null).times(1)
                .andReturn(new ArrayList<ChannelSegmentEntity>()).times(1)
                .andReturn(Arrays.asList(mockChannelSegmentEntity)).anyTimes();

        expect(mockChannelSegmentEntity.getIncId(ADCreativeType.BANNER)).andReturn(dummyIncId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("SiteKey").anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(1234L).anyTimes();
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();

        expect(mockSasParams.isRichMedia()).andReturn(false).anyTimes();
        expect(mockSasParams.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRFormat()).andReturn("banner").anyTimes();
        expect(mockSasParams.getDst()).andReturn(8).anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.BANNER).anyTimes();

        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"getCreativeType", "getImpressionId", "getAuctionId"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        String auctionId = WilburyUUID.setIntKey(TestUtils.SampleStrings.impressionId, 100).toString();

        expect(ixAdNetwork.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(ixAdNetwork.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).anyTimes();
        expect(ixAdNetwork.getAuctionId()).andReturn(auctionId).anyTimes();
        replayAll();

        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        ImpressionIdGenerator.init((short) 123, (byte) 10);

        boolean result;
        result = ixAdNetwork.updateRPAccountInfo("2770");
        assertThat(result, is(false));
        // adGroupMap is null
        result = ixAdNetwork.updateRPAccountInfo("2770");
        assertThat(result, is(false));
        // adGroupMap is empty
        result = ixAdNetwork.updateRPAccountInfo("2770");
        assertThat(result, is(false));
        // Positive Test Case
        result = ixAdNetwork.updateRPAccountInfo("2770");
        assertThat(result, is(true));

        String oldImpressionId = WilburyUUID.setIntKey(TestUtils.SampleStrings.impressionId, 0).toString();
        String newImpressionId = WilburyUUID.setIntKey(ixAdNetwork.getImpressionId(), 0).toString();
        auctionId = WilburyUUID.setIntKey(ixAdNetwork.getImpressionId(), 0).toString();

        assertThat(oldImpressionId, is(equalTo(newImpressionId)));
        assertThat(oldImpressionId, is(equalTo(auctionId)));
    }

    @Test
    public void testIsRichMediaAd() throws Exception {
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final IXAccountMapEntity mockAccountEntity = createMock(IXAccountMapEntity.class);
        final ChannelAdGroupRepository mockChannelAdGroupRepo = createMock(ChannelAdGroupRepository.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);

        final String dummyAccountId = "1a3d6a94f0024377885edc3c701ba548";
        final long dummyIncId = 1234L;

        expect(mockRepositoryHelper.queryIXAccountMapRepository(2770L)).andReturn(null).times(1)
                .andReturn(mockAccountEntity).anyTimes();

        expect(mockAccountEntity.getInmobiAccountId()).andReturn(null).times(1).andReturn("").times(1)
                .andReturn(dummyAccountId).anyTimes();

        expect(mockRepositoryHelper.getChannelAdGroupRepository()).andReturn(null).times(1)
                .andReturn(mockChannelAdGroupRepo).anyTimes();

        expect(mockChannelAdGroupRepo.getEntities(dummyAccountId)).andReturn(null).times(1)
                .andReturn(new ArrayList<ChannelSegmentEntity>()).times(1)
                .andReturn(Arrays.asList(mockChannelSegmentEntity)).anyTimes();

        expect(mockChannelSegmentEntity.getIncId(ADCreativeType.BANNER)).andReturn(dummyIncId).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("SiteKey").anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(1234L).anyTimes();

        expect(mockSasParams.isRichMedia()).andReturn(false).anyTimes();
        expect(mockSasParams.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).anyTimes();

        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"isNativeRequest", "updateRPAccountInfo"};
        final IXAdNetwork mockIXAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);
        replayAll();

        Object[][] testAdm =
                { {null, false}, {"data-creativeId", true}, {"data-creativeid", true}, {"\ndata-creativeid", true},
                        {"\ndata-creativei", false}, {"\n\n\n\n\n\n\ndata-creativeid\n\n\n\n\n\n\n", true},
                        {"?*+{}|^.\\\t\n\r\fdata-creativeid", true}};

        for (Object[] adm : testAdm) {
            MemberMatcher.field(IXAdNetwork.class, "adm").set(mockIXAdNetwork, adm[0]);
            assertThat(mockIXAdNetwork.isSproutAd(), is(adm[1]));
        }
    }

    @Test
    public void testDeserialisationFailureResponseDoesNotConformToContract() throws Exception {
        mockStaticNice(InspectorStats.class);
        final IXAdNetwork ixAdNetwork = new IXAdNetwork(mockConfig, null, null, null, "", advertiserName, true);
        replayAll();

        // Renaming fields for which @Required is being tested
        final String faultyJsons[] =
                {
                        // Null case
                        null,
                        // Empty String case
                        "",
                        // Not a Json
                        "{45454",
                        // Not a Json
                        "45454}",
                        // Empty Json
                        "{}",
                        // BidResponse.id is missing
                        "{  \"ids\":\"a35e38bb-0148-1000-ec1b-000402530000\",\"seatbid\":[  {  \"bid\":[  { \"id\":\"ab73dd4868a"
                                + "0bbadf8fd7527d95136b4\",\"impid\":\"1\",\"price\":14.496040344238281,\"estimated\":0,\"deali"
                                + "d\":\"DealWaleBabaJi\",\"pmptier\":3,\"nurl\":\"http://partner-wn.dummy-bidder.com/callback"
                                + "/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}\",\"adm\":\"<style type='text/css'>body {"
                                + " margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/ap"
                                + "ps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akama"
                                + "ihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p>\",\"crid"
                                + "\":\"CRID\",\"h\":320,\"w\":480,\"aqid\":\"Test_AQID\"}],\"buyer\":\"2770\",\"seat\":\"f55c"
                                + "9d46d7704f8789015a64153a7012\"}],\"bidid\":\"a35e38bb-0148-1000-ec1b-000402530000\"}",
                        // BidResponse.SeatBid List is missing
                        "{  \"id\":\"a35e38bb-0148-1000-ec1b-000402530000\",\"seatbids\":[  {  \"bid\":[  { \"id\":\"ab73dd4868a"
                                + "0bbadf8fd7527d95136b4\",\"impid\":\"1\",\"price\":14.496040344238281,\"estimated\":0,\"deali"
                                + "d\":\"DealWaleBabaJi\",\"pmptier\":3,\"nurl\":\"http://partner-wn.dummy-bidder.com/callback"
                                + "/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}\",\"adm\":\"<style type='text/css'>body {"
                                + " margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/ap"
                                + "ps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akama"
                                + "ihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p>\",\"crid"
                                + "\":\"CRID\",\"h\":320,\"w\":480,\"aqid\":\"Test_AQID\"}],\"buyer\":\"2770\",\"seat\":\"f55c"
                                + "9d46d7704f8789015a64153a7012\"}],\"bidid\":\"a35e38bb-0148-1000-ec1b-000402530000\"}",
                        // SeatBid.Bid List is missing
                        "{  \"id\":\"a35e38bb-0148-1000-ec1b-000402530000\",\"seatbid\":[  {  \"bids\":[  { \"id\":\"ab73dd4868a"
                                + "0bbadf8fd7527d95136b4\",\"impid\":\"1\",\"price\":14.496040344238281,\"estimated\":0,\"deali"
                                + "d\":\"DealWaleBabaJi\",\"pmptier\":3,\"nurl\":\"http://partner-wn.dummy-bidder.com/callback"
                                + "/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}\",\"adm\":\"<style type='text/css'>body {"
                                + " margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/ap"
                                + "ps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akama"
                                + "ihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p>\",\"crid"
                                + "\":\"CRID\",\"h\":320,\"w\":480,\"aqid\":\"Test_AQID\"}],\"buyer\":\"2770\",\"seat\":\"f55c"
                                + "9d46d7704f8789015a64153a7012\"}],\"bidid\":\"a35e38bb-0148-1000-ec1b-000402530000\"}",
                        // Bid.id is missing
                        "{  \"id\":\"a35e38bb-0148-1000-ec1b-000402530000\",\"seatbid\":[  {  \"bid\":[  { \"ids\":\"ab73dd4868a"
                                + "0bbadf8fd7527d95136b4\",\"impid\":\"1\",\"price\":14.496040344238281,\"estimated\":0,\"deali"
                                + "d\":\"DealWaleBabaJi\",\"pmptier\":3,\"nurl\":\"http://partner-wn.dummy-bidder.com/callback"
                                + "/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}\",\"adm\":\"<style type='text/css'>body {"
                                + " margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/ap"
                                + "ps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akama"
                                + "ihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p>\",\"crid"
                                + "\":\"CRID\",\"h\":320,\"w\":480,\"aqid\":\"Test_AQID\"}],\"buyer\":\"2770\",\"seat\":\"f55c"
                                + "9d46d7704f8789015a64153a7012\"}],\"bidid\":\"a35e38bb-0148-1000-ec1b-000402530000\"}",
                        // Bid.impid is missing
                        "{  \"id\":\"a35e38bb-0148-1000-ec1b-000402530000\",\"seatbid\":[  {  \"bid\":[  { \"id\":\"ab73dd4868a"
                                + "0bbadf8fd7527d95136b4\",\"impids\":\"1\",\"price\":14.496040344238281,\"estimated\":0,\"deali"
                                + "d\":\"DealWaleBabaJi\",\"pmptier\":3,\"nurl\":\"http://partner-wn.dummy-bidder.com/callback"
                                + "/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}\",\"adm\":\"<style type='text/css'>body {"
                                + " margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/ap"
                                + "ps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akama"
                                + "ihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p>\",\"crid"
                                + "\":\"CRID\",\"h\":320,\"w\":480,\"aqid\":\"Test_AQID\"}],\"buyer\":\"2770\",\"seat\":\"f55c"
                                + "9d46d7704f8789015a64153a7012\"}],\"bidid\":\"a35e38bb-0148-1000-ec1b-000402530000\"}",
                        // Bid.price is missing
                        "{  \"id\":\"a35e38bb-0148-1000-ec1b-000402530000\",\"seatbid\":[  {  \"bid\":[  { \"id\":\"ab73dd4868a"
                                + "0bbadf8fd7527d95136b4\",\"impid\":\"1\",\"prices\":14.496040344238281,\"estimated\":0,\"deali"
                                + "d\":\"DealWaleBabaJi\",\"pmptier\":3,\"nurl\":\"http://partner-wn.dummy-bidder.com/callback"
                                + "/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}\",\"adm\":\"<style type='text/css'>body {"
                                + " margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/ap"
                                + "ps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akama"
                                + "ihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p>\",\"crid"
                                + "\":\"CRID\",\"h\":320,\"w\":480,\"aqid\":\"Test_AQID\"}],\"buyer\":\"2770\",\"seat\":\"f55c"
                                + "9d46d7704f8789015a64153a7012\"}],\"bidid\":\"a35e38bb-0148-1000-ec1b-000402530000\"}",
                        // Native.asset list is missing
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assetss\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}",
                        // Native.link is missing
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"links\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}",
                        // Link.url is missing
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"urls\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}",
                        // Asset.id is missing (in the first asset)
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"ids\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}",
                        // Asset.id is missing (in the second asset). This test case ensures that @Required is enforced
                        // in all
                        // items of a list
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"ids\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}",
                        // Title.text is missing
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"texts\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}",
                        // Image.url is missing
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"urls\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}",
                        // TODO: Add check for Video.vasttag when Video on Native is added
                        // Data.value is missing
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"values\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}"};

        for (final String faultyJson : faultyJsons) {
            assertThat(ixAdNetwork.conformsToContract(faultyJson), is(equalTo(false)));
        }
    }

    @Test
    public void testDeserialisationSuccessfulAsResponseConformsToContract() throws Exception {
        mockStaticNice(InspectorStats.class);
        final IXAdNetwork ixAdNetwork = new IXAdNetwork(mockConfig, null, null, null, "", advertiserName, true);
        replayAll();

        // Renaming fields for which @Required is being tested
        final String jsons[] =
                {
                        TestUtils.SampleStrings.ixResponseJson,
                        // Exchange Spec v1.2 Sample Banner Response
                        "{\"id\":\"request-id\",\"bidid\":\"61bfa200-a3fc-4b7c-a741-8aabc8545be0\","
                                + "\"seatbid\":[{\"buyer\":\"2770\",\"bid\":[{\"id\":\"0\",\"impid\":\"1\",\"price\":0.65,"
                                + "\"crid\":\"3694586\",\"w\":320,\"h\":50,\"adm\":\"...\"}]}],\"statuscode\":0}",
                        // Exchange Spec v1.2 Modified Sample Native Response
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"native\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}}]}],\"statuscode\":0}"};

        for (final String json : jsons) {
            assertThat(ixAdNetwork.conformsToContract(json), is(equalTo(true)));
        }
    }

    @Test
    public void testDeserialisationFailureResponseDoesNotConformToValidBidStructure() throws Exception {
        mockStaticNice(InspectorStats.class);
        final IXAdNetwork ixAdNetwork = new IXAdNetwork(mockConfig, null, null, null, "", advertiserName, true);
        replayAll();

        final String faultyJsons[] =
                {
                        // SeatBid List is empty
                        "{\"id\":\"request-id\",\"bidid\":\"61bfa200-a3fc-4b7c-a741-8aabc8545be0\","
                                + "\"seatbid\":[],\"statuscode\":0}",
                        // Bid List is empty
                        "{\"id\":\"request-id\",\"bidid\":\"61bfa200-a3fc-4b7c-a741-8aabc8545be0\","
                                + "\"seatbid\":[{\"buyer\":\"2770\",\"bid\":[]}],\"statuscode\":0}",
                        // buyer is missing
                        "{\"id\":\"request-id\",\"bidid\":\"61bfa200-a3fc-4b7c-a741-8aabc8545be0\","
                                + "\"seatbid\":[{\"bid\":[{\"id\":\"0\",\"impid\":\"1\",\"price\":0.65,"
                                + "\"crid\":\"3694586\",\"w\":320,\"h\":50,\"adm\":\"...\"}]}],\"statuscode\":0}",
                        // aqid is missing - It can be null so commented
                        // "{\"id\":\"request-id\",\"bidid\":\"61bfa200-a3fc-4b7c-a741-8aabc8545be0\","
                        // + "\"seatbid\":[{\"buyer\":\"2770\",\"bid\":[{\"id\":\"0\",\"impid\":\"1\",\"price\":0.65,"
                        // + "\"crid\":\"3694586\",\"w\":320,\"h\":50,\"adm\":\"...\"}]}],\"statuscode\":0}",
                        // both adm and admobject are missing
                        "{\"id\":\"request-id\",\"bidid\":\"61bfa200-a3fc-4b7c-a741-8aabc8545be0\","
                                + "\"seatbid\":[{\"buyer\":\"2770\",\"bid\":[{\"id\":\"0\",\"impid\":\"1\",\"price\":0.65,"
                                + "\"aqid\":\"3694586\",\"w\":320,\"h\":50}]}],\"statuscode\":0}",
                        // both adm and admobject are set
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"adm\":\"...\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}"};

        for (final String faultyJson : faultyJsons) {
            ixAdNetwork.conformsToContract(faultyJson);
            assertThat(ixAdNetwork.conformsToValidBidStructure(), is(equalTo(false)));
        }
    }

    @Test
    public void testDeserialisationSuccessfulAsResponseConformsToValidBidStructure() throws Exception {
        mockStaticNice(InspectorStats.class);
        final IXAdNetwork ixAdNetwork = new IXAdNetwork(mockConfig, null, null, null, "", advertiserName, true);
        replayAll();

        final String jsons[] =
                {
                        TestUtils.SampleStrings.ixResponseJson,
                        // Exchange Spec v1.2 Sample Banner Response
                        "{\"id\":\"request-id\",\"bidid\":\"61bfa200-a3fc-4b7c-a741-8aabc8545be0\","
                                + "\"seatbid\":[{\"buyer\":\"2770\",\"bid\":[{\"id\":\"0\",\"impid\":\"1\",\"price\":0.65,"
                                + "\"aqid\":\"3694586\",\"w\":320,\"h\":50,\"adm\":\"...\"}]}],\"statuscode\":0}",
                        // Exchange Spec v1.2 Modified Sample Native Response
                        "{\"id\":\"gCDNC0Zv3llJb4d\",\"bidid\":\"7ed12492-dbf8-46e1-8038-05bb22f62669\",\"seatbid\":[{\"buyer\""
                                + ":\"3320\",\"bid\":[{\"id\":\"11\",\"impid\":\"gCDNC0Zv3llJb4d\",\"price\":1.192731,\"crid\""
                                + ":\"3702042\",\"aqid\":\"rtb:3320:123456\",\"admobject\":{\"ver\":1,\"link\":{\"url\":\"http"
                                + ": //i.am.a/URL\",\"fallback\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"htt"
                                + "p: //a.com/a\",\"http: //b.com/b\"]},\"imptrackers\":[\"http: //a.com/a\",\"http: //b.com/"
                                + "b\"],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"},\"link\":{\"url\":\"http://"
                                + "i.am.a/URL\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":\"http://cdn.mo"
                                + "bad.com/ad.png\",\"w\":64,\"h\":64}},{\"id\":4,\"data\":{\"value\":\"Install\"},\"link\":"
                                + "{\"url\":\"http://i.am.a/URL\"}}]}}]}],\"statuscode\":0}"};

        for (final String json : jsons) {
            ixAdNetwork.conformsToContract(json);
            assertThat(ixAdNetwork.conformsToValidBidStructure(), is(equalTo(true)));
        }
    }

    @Test
    public void testParseResponsePassedDeserializationWithSkippedNativeBuilding() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("imaiBaseUrl").anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException())
                .anyTimes();

        final String response = TestUtils.SampleStrings.ixNativeResponseJson;
        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"isNativeRequest", "updateRPAccountInfo", "nativeAdBuilding"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        expect(ixAdNetwork.isNativeRequest()).andReturn(true).times(1);
        expect(ixAdNetwork.updateRPAccountInfo("2770")).andReturn(true).times(1);
        ixAdNetwork.nativeAdBuilding();
        expectLastCall();
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
    }

    @Test
    public void testSetDealRelatedMetadataAgencyRebateFailsLowerBound() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final IXPackageEntity mockIXPackageEntity = createMock(IXPackageEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("imaiBaseUrl").anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal(null)).andReturn(mockIXPackageEntity).anyTimes();
        expect(mockIXPackageEntity.getId()).andReturn(1).anyTimes();

        final List<String> stringListWithNull = new ArrayList<>();
        stringListWithNull.add(null);
        expect(mockIXPackageEntity.getDealIds()).andReturn(stringListWithNull).anyTimes();
        expect(mockIXPackageEntity.getDealFloors()).andReturn(ImmutableList.of(5.0)).anyTimes();
        expect(mockIXPackageEntity.getAccessTypes()).andReturn(ImmutableList.of("PREFERRED_DEAL")).anyTimes();
        expect(mockIXPackageEntity.getDataVendorCost()).andReturn(0.0).anyTimes();
        expect(mockIXPackageEntity.getAgencyRebatePercentages()).andReturn(ImmutableList.of(0.0)).anyTimes();
        replayAll();

        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"isNativeRequest", "updateDSPAccountInfo", "nativeAdBuilding"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);


        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.setDealRelatedMetadata();
        assertThat(ixAdNetwork.getAgencyRebatePercentage(), is(equalTo(null)));
    }

    @Test
    public void testSetDealRelatedMetadataAgencyRebateFailsUpperBound() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final IXPackageEntity mockIXPackageEntity = createMock(IXPackageEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("imaiBaseUrl").anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal(null)).andReturn(mockIXPackageEntity).anyTimes();
        expect(mockIXPackageEntity.getId()).andReturn(1).anyTimes();

        final List<String> stringListWithNull = new ArrayList<>();
        stringListWithNull.add(null);
        /*final List<Double> doubleListWithNull = new ArrayList<>();
        doubleListWithNull.add(null);*/
        expect(mockIXPackageEntity.getDealIds()).andReturn(stringListWithNull).anyTimes();
        expect(mockIXPackageEntity.getDealFloors()).andReturn(ImmutableList.of(5.0)).anyTimes();
        expect(mockIXPackageEntity.getDataVendorCost()).andReturn(0.0).anyTimes();
        expect(mockIXPackageEntity.getAccessTypes()).andReturn(ImmutableList.of("PREFERRED_DEAL")).anyTimes();
        expect(mockIXPackageEntity.getAgencyRebatePercentages()).andReturn(ImmutableList.of(101.0)).anyTimes();
        replayAll();

        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"isNativeRequest", "updateDSPAccountInfo", "nativeAdBuilding"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);


        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.setDealRelatedMetadata();
        assertThat(ixAdNetwork.getAgencyRebatePercentage(), is(equalTo(null)));
    }

    @Test
    public void testSetDealRelatedMetadataAgencyRebateResponseSeatIdMissingAndDealMetaDataAgencyMissing()
            throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final IXPackageEntity mockIXPackageEntity = createMock(IXPackageEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("imaiBaseUrl").anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal(null)).andReturn(mockIXPackageEntity).anyTimes();
        expect(mockIXPackageEntity.getId()).andReturn(1).anyTimes();

        final List<String> stringListWithNull = new ArrayList<>();
        stringListWithNull.add(null);
        final List<Integer> integerListWithNull = new ArrayList<>();
        integerListWithNull.add(null);
        expect(mockIXPackageEntity.getDealIds()).andReturn(stringListWithNull).anyTimes();
        expect(mockIXPackageEntity.getDealFloors()).andReturn(ImmutableList.of(5.0)).anyTimes();
        expect(mockIXPackageEntity.getDataVendorCost()).andReturn(0.0).anyTimes();
        expect(mockIXPackageEntity.getAccessTypes()).andReturn(ImmutableList.of("PREFERRED_DEAL")).anyTimes();
        expect(mockIXPackageEntity.getAgencyRebatePercentages()).andReturn(ImmutableList.of(55.0)).anyTimes();
        expect(mockIXPackageEntity.getRpAgencyIds()).andReturn(integerListWithNull).anyTimes();
        replayAll();

        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"isNativeRequest", "updateDSPAccountInfo", "nativeAdBuilding"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.setDealRelatedMetadata();
        assertThat(ixAdNetwork.getAgencyRebatePercentage(), is(equalTo(null)));
    }

    @Test
    public void testSetDealRelatedMetadataAgencyRebateResponseSeatIdAndDealMetaDataAgencyMismatch() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final IXPackageEntity mockIXPackageEntity = createMock(IXPackageEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("imaiBaseUrl").anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal(null)).andReturn(mockIXPackageEntity).anyTimes();
        expect(mockIXPackageEntity.getId()).andReturn(1).anyTimes();

        final List<String> stringListWithNull = new ArrayList<>();
        stringListWithNull.add(null);
        /*final List<Integer> integerListWithNull = new ArrayList<>();
        integerListWithNull.add(null);*/
        expect(mockIXPackageEntity.getDealIds()).andReturn(stringListWithNull).anyTimes();
        expect(mockIXPackageEntity.getDealFloors()).andReturn(ImmutableList.of(5.0)).anyTimes();
        expect(mockIXPackageEntity.getDataVendorCost()).andReturn(0.0).anyTimes();
        expect(mockIXPackageEntity.getAccessTypes()).andReturn(ImmutableList.of("PREFERRED_DEAL")).anyTimes();
        expect(mockIXPackageEntity.getAgencyRebatePercentages()).andReturn(ImmutableList.of(55.0)).anyTimes();
        expect(mockIXPackageEntity.getRpAgencyIds()).andReturn(ImmutableList.of(1)).anyTimes();
        replayAll();

        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"isNativeRequest", "updateDSPAccountInfo", "nativeAdBuilding"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        MemberModifier.field(IXAdNetwork.class, "seatId").set(ixAdNetwork, "2");
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.setDealRelatedMetadata();
        assertThat(ixAdNetwork.getAgencyRebatePercentage(), is(equalTo(null)));
    }

    @Test
    public void testSetDealRelatedMetadataAgencyRebatePositive() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createNiceMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final IXPackageEntity mockIXPackageEntity = createMock(IXPackageEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("imaiBaseUrl").anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(8).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(0).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(0).anyTimes();
        expect(mockRepositoryHelper.queryIxPackageByDeal(null)).andReturn(mockIXPackageEntity).anyTimes();
        expect(mockIXPackageEntity.getId()).andReturn(1).anyTimes();

        final List<String> stringListWithNull = new ArrayList<>();
        stringListWithNull.add(null);
        expect(mockIXPackageEntity.getDealIds()).andReturn(stringListWithNull).anyTimes();
        expect(mockIXPackageEntity.getDealFloors()).andReturn(ImmutableList.of(5.0)).anyTimes();
        expect(mockIXPackageEntity.getDataVendorCost()).andReturn(0.0).anyTimes();
        expect(mockIXPackageEntity.getAccessTypes()).andReturn(ImmutableList.of("PREFERRED_DEAL")).anyTimes();
        expect(mockIXPackageEntity.getAgencyRebatePercentages()).andReturn(ImmutableList.of(55.0)).anyTimes();
        expect(mockIXPackageEntity.getRpAgencyIds()).andReturn(ImmutableList.of(1)).anyTimes();
        replayAll();

        final Object[] constructerArgs =
                {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "", advertiserName, true};
        final String[] methodsToBeMocked = {"isNativeRequest", "updateDSPAccountInfo", "nativeAdBuilding"};
        final IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        setInmobiAdTrackerBuilderFactoryForTest(ixAdNetwork);
        ixAdNetwork
                .configureParameters(mockSasParams, null, mockChannelSegmentEntity, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.setDealRelatedMetadata();
        assertThat(ixAdNetwork.getAgencyRebatePercentage(), is(equalTo(55.0)));
        assertThat(ixAdNetwork.isAgencyRebateDeal(), is(equalTo(true)));
    }
}
