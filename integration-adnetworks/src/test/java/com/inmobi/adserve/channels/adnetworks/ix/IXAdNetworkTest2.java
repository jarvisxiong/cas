package com.inmobi.adserve.channels.adnetworks.ix;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.createMockBuilder;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;

import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.Utils.ClickUrlsRegenerator;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.casthrift.ADCreativeType;

// TODO: Merge with IXAdNetworkTest.java
public class IXAdNetworkTest2 {
    private static Configuration mockConfig;
    private static final String advertiserName = "ix";

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
        replay(mockConfig);
    }

    @BeforeClass
    public static void setUp() {
        prepareMockConfig();
    }

    @Test
    public void testParseResponseNoAd() {
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        expect(mockStatus.code()).andReturn(404).times(4).andReturn(200).times(6);
        replay(mockStatus);

        final String response1 = "";
        final String response2 = "Dummy";
        final String response3 = null;
        final String response4 = "{\"id\":\"ce3adf2d-0149-1000-e483-3e96d9a8a2c1\",\"bidid\":\"1bc93e72-3c81-4bad-ba35-9458b54e109a\",\"seatbid\":[{\"bid\":[]}],\"statuscode\":10}";
        final IXAdNetwork ixAdNetwork =
                new IXAdNetwork(mockConfig, null, null, null, null, advertiserName, 0, null, false);

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
        final HttpResponseStatus mockStatus = createMock(HttpResponseStatus.class);
        expect(mockStatus.code()).andReturn(200).times(2);
        replay(mockStatus);

        final String response = "{INVALID_JSON}";
        final IXAdNetwork ixAdNetwork =
                createMockBuilder(IXAdNetwork.class).addMockedMethod("getName").createMock();
        expect(ixAdNetwork.getName()).andReturn("ix").times(1);
        replay(ixAdNetwork);

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(500)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("TERM")));
    }

    @Test
    public void testParseResponsePassedDeserializationBannerBuilding() throws Exception {
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

        replay(mockStatus, mockHttpRequestHandlerBase, mockChannel, mockRepositoryHelper, mockSasParams,
                mockChannelSegmentEntity);

        final String response = TestUtils.SampleStrings.ixResponseJson;
        final IXAdNetwork ixAdNetwork =
                createMockBuilder(IXAdNetwork.class)
                        .addMockedMethod("getAdMarkUp")
                        .addMockedMethod("isNativeRequest")
                        .addMockedMethod("configureParameters", null)
                        .addMockedMethod("updateDSPAccountInfo")
                        .withConstructor(mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                                advertiserName, 0, mockRepositoryHelper, true).createMock();

        expect(ixAdNetwork.isNativeRequest()).andReturn(false).times(1);
        expect(ixAdNetwork.getAdMarkUp()).andReturn(TestUtils.SampleStrings.ixResponseADM).times(1);
        expect(ixAdNetwork.configureParameters()).andReturn(true).times(1);
        expect(ixAdNetwork.updateDSPAccountInfo("2770")).andReturn(true).times(1);
        replay(ixAdNetwork);

        ixAdNetwork.configureParameters(mockSasParams, null, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15);
        Formatter.init();

        ixAdNetwork.parseResponse(response, mockStatus);
        assertThat(ixAdNetwork.getHttpResponseStatusCode(), is(equalTo(200)));
        assertThat(ixAdNetwork.getAdStatus(), is(equalTo("AD")));
        assertThat(
                ixAdNetwork.getResponseContent(),
                is(equalTo("<html><body style=\"margin:0;padding:0;\"><script><style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='https://play.google.com/store/apps/details?id=com.sweetnspicy.recipes&hl=en' target='_blank'><img src='http://redge-a.akamaihd.net/FileData/50758558-c167-463d-873e-f989f75da95215.png' border='0'/></a></p></script><img src='http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/c124b6b5-0148-1000-c54a-00012e330000/0/5l/-1/0/0/x/0/nw/101/1/1/bc20cfc3?b=${WIN_BID}' height=1 width=1 border=0 /><img src='http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}' height=1 width=1 border=0 /></body></html>")));
    }

    @Test
    public void testUpdateDSPAccountInfo() {
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

        replay(mockHttpRequestHandlerBase, mockChannel, mockRepositoryHelper, mockAccountEntity,
                mockChannelAdGroupRepo, mockChannelSegmentEntity, mockSasParams);

        final IXAdNetwork ixAdNetwork =
                createMockBuilder(IXAdNetwork.class)
                        .addMockedMethod("getCreativeType")
                        .addMockedMethod("getImpressionId")
                        .addMockedMethod("configureParameters", null)
                        .withConstructor(mockConfig, new Bootstrap(), mockHttpRequestHandlerBase, mockChannel, "",
                                advertiserName, 0, mockRepositoryHelper, true).createMock();

        expect(ixAdNetwork.getCreativeType()).andReturn(ADCreativeType.BANNER).anyTimes();
        expect(ixAdNetwork.getImpressionId()).andReturn(TestUtils.SampleStrings.impressionId).anyTimes();
        expect(ixAdNetwork.configureParameters()).andReturn(true).times(1);
        replay(ixAdNetwork);

        ixAdNetwork.configureParameters(mockSasParams, null, mockChannelSegmentEntity,
                TestUtils.SampleStrings.clickUrl, TestUtils.SampleStrings.beaconUrl, (short) 15);
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
}
