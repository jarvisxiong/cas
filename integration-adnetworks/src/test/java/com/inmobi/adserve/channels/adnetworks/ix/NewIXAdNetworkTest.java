package com.inmobi.adserve.channels.adnetworks.ix;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.googlecode.cqengine.resultset.common.NoSuchObjectException;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.SproutTemplateConstants;
import com.inmobi.adserve.channels.util.Utils.ClickUrlsRegenerator;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.casthrift.ADCreativeType;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

// TODO: Merge with IXAdNetworkTest.java
@RunWith(PowerMockRunner.class)
@PrepareForTest({IXAdNetwork.class, InspectorStats.class})
@PowerMockIgnore("javax.crypto.*")
public class NewIXAdNetworkTest {
    private static Configuration mockConfig;
    private static final String advertiserName = "ix";
    private static RepositoryHelper repositoryHelper;

    private static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("advertiserId").anyTimes();
        expect(mockConfig.getString(advertiserName + ".urlArg")).andReturn("urlArg").anyTimes();
        expect(mockConfig.getString(advertiserName + ".wnUrlback")).andReturn(
                "http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}")
                .anyTimes();
        expect(mockConfig.getString(advertiserName + ".ixMethod")).andReturn("ixMethod").anyTimes();
        expect(mockConfig.getString(advertiserName + ".userName")).andReturn("userName").anyTimes();
        expect(mockConfig.getString(advertiserName + ".password")).andReturn("password").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".htmlSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", false)).andReturn(false).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".accountId")).andReturn(1).anyTimes();
        expect(mockConfig.getList(advertiserName + ".globalBlind")).andReturn(null).anyTimes();
        expect(mockConfig.getString("key.1.value")).andReturn("Secret Key").anyTimes();
        expect(mockConfig.getString("beaconURLPrefix")).andReturn("BeaconPrefix").anyTimes();
        expect(mockConfig.getString("clickURLPrefix")).andReturn("ClickPrefix").anyTimes();
        replayAll();
    }

    @BeforeClass
    public static void setUp() throws IllegalAccessException {
        prepareMockConfig();
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

        MemberModifier.field(InspectorStats.class, "boxName")
                .set(InspectorStats.class, "randomBox");

        expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);
    }

    @Test
    public void testParseResponseNoAd() {
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        expect(mockStatus.code()).andReturn(404).times(4).andReturn(200).times(6);
        replayAll();

        final String response1 = "";
        final String response2 = "Dummy";
        final String response3 = null;
        final String response4 =
                "{\"id\":\"ce3adf2d-0149-1000-e483-3e96d9a8a2c1\",\"bidid\":\"1bc93e72-3c81-4bad-ba35-9458b54e109a\",\"seatbid\":[{\"bid\":[]}],\"statuscode\":10}";
        final IXAdNetwork ixAdNetwork =
                new IXAdNetwork(mockConfig, null, null, null, null, advertiserName, 0, false);

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
        final SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("wap").times(3);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("SdkVer").times(1);
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException()).anyTimes();

        replayAll();

        final String response = TestUtils.SampleStrings.ixResponseJson;

        Object[] constructerArgs = {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                advertiserName, 0, true};
        String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateDSPAccountInfo"};
        IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixResponseADM).anyTimes();
        expect(ixAdNetwork.updateDSPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork.configureParameters(mockSasParams, null, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<html><head><style type=\"text/css\">#im_1011_ad{display: table;}#im_1011_p{vertical-align: middle; text-align: center;}</style></head><body style=\"margin:0;padding:0;\"><div id=\"im_1011_ad\" style=\"width:100%;height:100%\"><div id=\"im_1011_p\" style=\"width:100%;height:100%\" class=\"im_1011_bg\"><style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/apps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akamaihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p></div></div><img src='http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3?b=${WIN_BID}${DEAL_GET_PARAM}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></body></html>")));
    }

    @Test
    public void testParseResponsePassedDeserializationRichMediaBuildingCoppaDisabled() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").anyTimes();
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getCountryCode()).andReturn("55").times(1);
        expect(mockSasParams.getRqAdType()).andReturn("banner").anyTimes();
        expect(mockSasParams.getImaiBaseUrl())
                .andReturn("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js").anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException()).anyTimes();
        expect(mockCasInternalRequestParameters.getLatLong()).andReturn("123.45,678.90").anyTimes();
        expect(mockCasInternalRequestParameters.getZipCode()).andReturn("560103").anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        Object[] constructerArgs = {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                advertiserName, 0, true};
        String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateDSPAccountInfo"};
        IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.isSproutSupported = true;
        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixStudioResponseAdTag).anyTimes();
        expect(ixAdNetwork.updateDSPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParameters, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, height=device-height,user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><base href=\"http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js\"></base><style type=\"text/css\">#im_1011_ad{display: table;}#im_1011_p{vertical-align: middle; text-align: center;}</style></head><body style=\"margin:0;padding:0;\"><div id=\"im_1011_ad\" style=\"width:100%;height:100%\"><div id=\"im_1011_p\" style=\"width:100%;height:100%\" class=\"im_1011_bg\"><script src=\"mraid.js\"></script><div id=\"Sprout_ShCMGj4G1A4GIIsw_div\" data-creativeId=\"ShCMGj4G1A4GIIsw\"></div><script type=\"text/javascript\">var _Sprout = _Sprout || {};/* 3rd Party Impression Tracker: a tracking pixel URL for tracking 3rd party impressions */_Sprout.impressionTracker = \"PUT_IMPRESSION_TRACKER_HERE\";/* 3rd Party Click Tracker: A URL or Macro like %c for third party exit tracking */_Sprout.clickTracker = \"PUT_CLICK_TRACKER_HERE\";/* Publisher Label: What you want to call this line-item in Studio reports */_Sprout.publisherLabel = \"PUT_PUBLISHER_LABEL_HERE\";_Sprout._inMobiAdTagTracking={st:new Date().getTime(),rr:0};Sprout[\"ShCMGj4G1A4GIIsw\"]={querystring:{im_curl:\"http:\\/\\/localhost:8800\\/C\\/t\\/1\\/1\\/1\\/c\\/2\\/m\\/k\\/0\\/0\\/eyJVRElEIjoidWlkdmFsdWUifQ~~\\/c124b6b5-0148-1000-c54a-00012e330000\\/0\\/5l\\/-1\\/0\\/0\\/x\\/0\\/nw\\/101\\/1\\/1\\/bc20cfc3?b=${WIN_BID}${DEAL_GET_PARAM}\",im_sdk:\"a450\",click:\"http:\\/\\/localhost:8800\\/C\\/t\\/1\\/1\\/1\\/c\\/2\\/m\\/k\\/0\\/0\\/eyJVRElEIjoidWlkdmFsdWUifQ~~\\/c124b6b5-0148-1000-c54a-00012e330000\\/0\\/5l\\/-1\\/0\\/0\\/x\\/0\\/nw\\/101\\/1\\/1\\/bc20cfc3\",adFormat:\"interstitial\",im_recordEventFun:\"\",geo_lat:\"123.45\",geo_lng:\"678.9\",geo_cc:\"55\",geo_zip:\"560103\",js_esc_geo_city:\"\",openLandingPage:\"\"}};var _sproutReadyEvt=document.createEvent(\"Event\");_sproutReadyEvt.initEvent(\"sproutReady\",true,true);window.dispatchEvent(_sproutReadyEvt);var sr, sp=\"/load/ShCMGj4G1A4GIIsw.inmobi.html.review.js?_t=\"(Date.now())\"\", _Sprout_load=function(){var e=document.getElementsByTagName(\"script\"),e=e[e.length-1],t=document.createElement(\"script\");t.async=!0;t.type=\"text/javascript\";(https:==document.location.protocol?sr=\"http://farm.sproutbuilder.com\":sr=\"http://farm.sproutbuilder.com\");t.src=sr+sp;e.parentNode.insertBefore(t,e.nextSibling)};\"0\"===window[\"_Sprout\"][\"ShCMGj4G1A4GIIsw\"][\"querystring\"][\"__im_sdk\"]||\"complete\"===document.readyState?_Sprout_load():window.addEventListener(\"load\",_Sprout_load,!1)</script></div></div><img src='http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3?b=${WIN_BID}${DEAL_GET_PARAM}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></body></html>")));
    }

    @Test
    public void testParseResponsePassedDeserializationRichMediaBuildingCoppaSet() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").anyTimes();
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getCountryCode()).andReturn("55").times(1);
        expect(mockSasParams.getRqAdType()).andReturn("banner").anyTimes();
        expect(mockSasParams.getImaiBaseUrl())
                .andReturn("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js").anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException()).anyTimes();
        expect(mockCasInternalRequestParameters.getLatLong()).andReturn("123.45,678.90").anyTimes();
        expect(mockCasInternalRequestParameters.getZipCode()).andReturn("560103").anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        Object[] constructerArgs = {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                advertiserName, 0, true};
        String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateDSPAccountInfo"};
        IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.isSproutSupported = true;
        ixAdNetwork.setHost("http://localhost:8080/getIXBid");
        ixAdNetwork.isCoppaSet = true;

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixStudioResponseAdTag).anyTimes();
        expect(ixAdNetwork.updateDSPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParameters, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, height=device-height,user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><base href=\"http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js\"></base><style type=\"text/css\">#im_1011_ad{display: table;}#im_1011_p{vertical-align: middle; text-align: center;}</style></head><body style=\"margin:0;padding:0;\"><div id=\"im_1011_ad\" style=\"width:100%;height:100%\"><div id=\"im_1011_p\" style=\"width:100%;height:100%\" class=\"im_1011_bg\"><script src=\"mraid.js\"></script><div id=\"Sprout_ShCMGj4G1A4GIIsw_div\" data-creativeId=\"ShCMGj4G1A4GIIsw\"></div><script type=\"text/javascript\">var _Sprout = _Sprout || {};/* 3rd Party Impression Tracker: a tracking pixel URL for tracking 3rd party impressions */_Sprout.impressionTracker = \"PUT_IMPRESSION_TRACKER_HERE\";/* 3rd Party Click Tracker: A URL or Macro like %c for third party exit tracking */_Sprout.clickTracker = \"PUT_CLICK_TRACKER_HERE\";/* Publisher Label: What you want to call this line-item in Studio reports */_Sprout.publisherLabel = \"PUT_PUBLISHER_LABEL_HERE\";_Sprout._inMobiAdTagTracking={st:new Date().getTime(),rr:0};Sprout[\"ShCMGj4G1A4GIIsw\"]={querystring:{im_curl:\"http:\\/\\/localhost:8800\\/C\\/t\\/1\\/1\\/1\\/c\\/2\\/m\\/k\\/0\\/0\\/eyJVRElEIjoidWlkdmFsdWUifQ~~\\/c124b6b5-0148-1000-c54a-00012e330000\\/0\\/5l\\/-1\\/0\\/0\\/x\\/0\\/nw\\/101\\/1\\/1\\/bc20cfc3?b=${WIN_BID}${DEAL_GET_PARAM}\",im_sdk:\"a450\",click:\"http:\\/\\/localhost:8800\\/C\\/t\\/1\\/1\\/1\\/c\\/2\\/m\\/k\\/0\\/0\\/eyJVRElEIjoidWlkdmFsdWUifQ~~\\/c124b6b5-0148-1000-c54a-00012e330000\\/0\\/5l\\/-1\\/0\\/0\\/x\\/0\\/nw\\/101\\/1\\/1\\/bc20cfc3\",adFormat:\"interstitial\",im_recordEventFun:\"\",geo_lat:\"\",geo_lng:\"\",geo_cc:\"\",geo_zip:\"\",js_esc_geo_city:\"\",openLandingPage:\"\"}};var _sproutReadyEvt=document.createEvent(\"Event\");_sproutReadyEvt.initEvent(\"sproutReady\",true,true);window.dispatchEvent(_sproutReadyEvt);var sr, sp=\"/load/ShCMGj4G1A4GIIsw.inmobi.html.review.js?_t=\"(Date.now())\"\", _Sprout_load=function(){var e=document.getElementsByTagName(\"script\"),e=e[e.length-1],t=document.createElement(\"script\");t.async=!0;t.type=\"text/javascript\";(https:==document.location.protocol?sr=\"http://farm.sproutbuilder.com\":sr=\"http://farm.sproutbuilder.com\");t.src=sr+sp;e.parentNode.insertBefore(t,e.nextSibling)};\"0\"===window[\"_Sprout\"][\"ShCMGj4G1A4GIIsw\"][\"querystring\"][\"__im_sdk\"]||\"complete\"===document.readyState?_Sprout_load():window.addEventListener(\"load\",_Sprout_load,!1)</script></div></div><img src='http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3?b=${WIN_BID}${DEAL_GET_PARAM}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></body></html>")));
    }

    @Test
    public void testParseResponseFailedDeserializationRichMediaBuildingSdkLowerThan370() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").anyTimes();
        expect(mockSasParams.getSdkVersion()).andReturn("a350").anyTimes();
        expect(mockSasParams.getCountryCode()).andReturn("55").times(1);
        expect(mockSasParams.getRqAdType()).andReturn("banner").anyTimes();
        expect(mockSasParams.getImaiBaseUrl())
                .andReturn("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js").anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException()).anyTimes();
        expect(mockCasInternalRequestParameters.getLatLong()).andReturn("123.45,678.90").anyTimes();
        expect(mockCasInternalRequestParameters.getZipCode()).andReturn("560103").anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        Object[] constructerArgs = {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                advertiserName, 0, true};
        String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateDSPAccountInfo"};
        IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.isSproutSupported = false;
        ixAdNetwork.setHost("http://localhost:8080/getIXBid");
        ixAdNetwork.isCoppaSet = true;

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixStudioResponseAdTag).anyTimes();
        expect(ixAdNetwork.updateDSPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParameters, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("")));
    }

    @Test
    public void testParseResponseFailedDeserializationRichMediaBuildingWAP() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSource()).andReturn("WAP").anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").anyTimes();
        expect(mockSasParams.getSdkVersion()).andReturn("a350").anyTimes();
        expect(mockSasParams.getCountryCode()).andReturn("55").times(1);
        expect(mockSasParams.getRqAdType()).andReturn("banner").anyTimes();
        expect(mockSasParams.getImaiBaseUrl())
                .andReturn("http://inmobisdk-a.akamaihd.net/sdk/android/mraid.js").anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException()).anyTimes();
        expect(mockCasInternalRequestParameters.getLatLong()).andReturn("123.45,678.90").anyTimes();
        expect(mockCasInternalRequestParameters.getZipCode()).andReturn("560103").anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        Object[] constructerArgs = {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                advertiserName, 0, true};
        String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateDSPAccountInfo"};
        IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.isSproutSupported = false;
        ixAdNetwork.setHost("http://localhost:8080/getIXBid");
        ixAdNetwork.isCoppaSet = true;

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixStudioResponseAdTag).anyTimes();
        expect(ixAdNetwork.updateDSPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParameters, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("NO_AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("")));
    }


    @Test
    public void testParseResponsePassedDeserializationInterstitialBuildingForSDK450() throws Exception {
        mockStaticNice(InspectorStats.class);
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(mockStatus.code()).andReturn(200).times(2);
        expect(mockSasParams.getSiteIncId()).andReturn(1234L).times(1);
        expect(mockSasParams.getImpressionId()).andReturn("ImpressionId").times(1);
        expect(mockSasParams.getSdkVersion()).andReturn("a450").anyTimes();
        expect(mockSasParams.getImaiBaseUrl()).andReturn("imaiBaseUrl").anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getRqAdType()).andReturn("int").anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("ExtSiteKey").times(1);
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(123L).times(1);
        expect(mockRepositoryHelper.queryIxPackageByDeal("DealWaleBabaJi")).andThrow(new NoSuchObjectException()).anyTimes();

        final String response = TestUtils.SampleStrings.ixResponseJson;
        Object[] constructerArgs = {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                advertiserName, 0, true};
        String[] methodsToBeMocked = {"getAdMarkUp", "isNativeRequest", "updateDSPAccountInfo"};
        IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixResponseADM).anyTimes();
        expect(ixAdNetwork.updateDSPAccountInfo("2770")).andReturn(true).times(1);
        replayAll();

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork.configureParameters(mockSasParams, null, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15, mockRepositoryHelper);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<html><head><meta name=\"viewport\" content=\"width=device-width, height=device-height,user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><base href=\"imaiBaseUrl\"></base><style type=\"text/css\">#im_1011_ad{display: table;}#im_1011_p{vertical-align: middle; text-align: center;}</style></head><body style=\"margin:0;padding:0;\"><div id=\"im_1011_ad\" style=\"width:100%;height:100%\"><div id=\"im_1011_p\" style=\"width:100%;height:100%\" class=\"im_1011_bg\"><script src=\"mraid.js\" ></script><style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/apps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akamaihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p><script type=\"text/javascript\">var readyHandler=function(){_im_imai.fireAdReady();_im_imai.removeEventListener('ready',readyHandler);};_im_imai.addEventListener('ready',readyHandler);</script></div></div><img src='http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3?b=${WIN_BID}${DEAL_GET_PARAM}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></body></html>")));
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

        Object[] constructerArgs = {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                advertiserName, 0, true};
        String[] methodsToBeMocked = {"getCreativeType", "getImpressionId"};
        IXAdNetwork ixAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);


        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        expect(ixAdNetwork.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(ixAdNetwork.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).anyTimes();
        replayAll();

        ixAdNetwork.setHost("http://localhost:8080/getIXBid");

        MemberModifier.suppress(IXAdNetwork.class.getDeclaredMethod("configureParameters"));
        ixAdNetwork.configureParameters(mockSasParams, null, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15, mockRepositoryHelper);
        ImpressionIdGenerator.init((short) 123, (byte) 10);
        ClickUrlsRegenerator.init(mockConfig);

        boolean result;

        result = ixAdNetwork.updateDSPAccountInfo(null);
        assertThat(result, is(false));
        result = ixAdNetwork.updateDSPAccountInfo("can't parse this");
        assertThat(result, is(false));
        result = ixAdNetwork.updateDSPAccountInfo("2770L");
        assertThat(result, is(false));
        result = ixAdNetwork.updateDSPAccountInfo("");
        assertThat(result, is(false));

        // ixAccountMapEntity is null
        result = ixAdNetwork.updateDSPAccountInfo("2770");
        assertThat(result, is(false));
        // ixAccountMapEntity.getInmobiAccountId is null
        result = ixAdNetwork.updateDSPAccountInfo("2770");
        assertThat(result, is(false));
        // ixAccountMapEntity.getInmobiAccountId is empty
        result = ixAdNetwork.updateDSPAccountInfo("2770");
        assertThat(result, is(false));
        // channelAdGroupRepository is null
        result = ixAdNetwork.updateDSPAccountInfo("2770");
        assertThat(result, is(false));
        // adGroupMap is null
        result = ixAdNetwork.updateDSPAccountInfo("2770");
        assertThat(result, is(false));
        // adGroupMap is empty
        result = ixAdNetwork.updateDSPAccountInfo("2770");
        assertThat(result, is(false));
        // Positive Test Case
        result = ixAdNetwork.updateDSPAccountInfo("2770");
        assertThat(result, is(true));
    }

    @Test
    public void testIsRichMediaAd() throws Exception{
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

        Object[] constructerArgs = {mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                advertiserName, 0, true};
        String[] methodsToBeMocked = {"isNativeRequest", "updateDSPAccountInfo"};
        IXAdNetwork mockIXAdNetwork = createPartialMock(IXAdNetwork.class, methodsToBeMocked, constructerArgs);
        replayAll();

        MemberModifier.field(IXAdNetwork.class, "adm").set(mockIXAdNetwork, null);
        assertThat(mockIXAdNetwork.isSproutAd(), is(false));

        MemberModifier.field(IXAdNetwork.class, "adm")
                .set(mockIXAdNetwork, SproutTemplateConstants.SPROUT_UNIQUE_STRING);
        assertThat(mockIXAdNetwork.isSproutAd(), is(true));
    }
}
