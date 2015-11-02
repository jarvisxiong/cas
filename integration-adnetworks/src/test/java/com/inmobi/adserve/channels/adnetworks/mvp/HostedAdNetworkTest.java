package com.inmobi.adserve.channels.adnetworks.mvp;


import static com.inmobi.adserve.channels.util.config.GlobalConstant.CPC;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.lang.reflect.Field;

import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.types.LocationSource;

import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ImpressionIdGenerator.class, HostedAdNetwork.class, InspectorStats.class})
public class HostedAdNetworkTest {
    private static final String advertiserName = "hosted";
    private static Configuration mockConfig;
    private final String hostedHost = "https://mrp.rubiconproject.com/ad_request";

    private static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("advertiserId").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(true).anyTimes();
        expect(mockConfig.getDouble(advertiserName + ".bidToUmpInUSD")).andReturn(10.0).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".htmlSupported", false)).andReturn(false).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getString(advertiserName + ".userName")).andReturn("userName").anyTimes();
        expect(mockConfig.getString(advertiserName + ".password")).andReturn("password").anyTimes();
        replayAll();
    }

    @BeforeClass
    public static void setUp() {
        prepareMockConfig();
    }

    @Test
    public void testCreateHostedBidRequestObjectVariations() throws Exception {
        String expectedBidRequestJSON;
        Boolean result;

        long siteIncId = 5L;
        long requestId = 10L;
        long adGroupIncId = 15L;
        String urlBase = "urlBase";
        String latLong = "123.45,678.90";
        String ip = "127.0.0.1";
        String userAgent = TestUtils.SampleStrings.userAgent;
        //String limitAdTrackingIsFalse = "0";
        //String limitAdTrackingIsTrue = "1";
        String idfa = "idfaValue";
        String siteId = "siteId";
        String impressionId = "impressionId";
        Long slotId = 15L;
        String additionalParamsStr = "{\"app\":\"997EC9A04C2B01324BD122000B4000BD\"}";
        JSONObject additionalParams = new JSONObject(additionalParamsStr);

        mockStatic(ImpressionIdGenerator.class);
        mockStaticNice(InspectorStats.class);
        ImpressionIdGenerator mockImpressionIdGenerator = createMock(ImpressionIdGenerator.class);
        // RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        SASRequestParameters mockSasParams = createMock(SASRequestParameters.class);
        CasInternalRequestParameters mockCasInternalRequestParams = createMock(CasInternalRequestParameters.class);
        ChannelSegmentEntity mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);

        expect(ImpressionIdGenerator.getInstance()).andReturn(mockImpressionIdGenerator).anyTimes();
        expect(mockImpressionIdGenerator.getUniqueId(siteIncId)).andReturn(requestId).anyTimes();

        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn("externalSiteKey").anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adGroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPC).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(2).anyTimes();
        expect(mockChannelSegmentEntity.getSecondaryAdFormatConstraints()).andReturn(SecondaryAdFormatConstraints.STATIC).anyTimes();
        expect(mockChannelSegmentEntity.getAdditionalParams())
                .andReturn(additionalParams).times(2)
                .andReturn(null).times(1);

        expect(mockSasParams.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(siteIncId).anyTimes();
        expect(mockSasParams.getRFormat()).andReturn("native").anyTimes();
        expect(mockSasParams.getLocationSource())
                .andReturn(LocationSource.LATLON).times(1)
                .andReturn(LocationSource.NO_TARGETING).times(7)
                .andReturn(LocationSource.LATLON).times(1);
        expect(mockSasParams.getRemoteHostIp()).andReturn(ip).anyTimes();
        expect(mockSasParams.getUserAgent()).andReturn(userAgent).anyTimes();
        expect(mockSasParams.getSiteId()).andReturn(siteId).anyTimes();
        expect(mockSasParams.getSource()).andReturn("APP").anyTimes();
        expect(mockSasParams.getDst()).andReturn(6).anyTimes();

        expect(mockCasInternalRequestParams.getLatLong()).andReturn(latLong).anyTimes();
        expect(mockCasInternalRequestParams.isTrackingAllowed()).andReturn(true).times(1).andReturn(false).times(1)
                .andReturn(true).times(1);
        expect(mockCasInternalRequestParams.getUidIFA()).andReturn(idfa).anyTimes();

        replayAll(mockSasParams, mockCasInternalRequestParams, mockChannelSegmentEntity);

        HostedAdNetwork hostedAdNetwork =
                new HostedAdNetwork(mockConfig, null, null, null, urlBase, advertiserName, false);
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        hostedAdNetwork.setHost(hostedHost);

        // Positive Test Case #1: All mandatory params present
        expectedBidRequestJSON =
                "{\"id\":10,\"app\":\"997EC9A04C2B01324BD122000B4000BD\",\"clt\":\"INMB_SERVER_NATIVE_1.0.0\",\"rtyp\":\"nativejson\",\"typ\":4,\"lat\":123.45,\"lng\":678.9,\"ltyp\":1,\"ip\":\"127.0.0.1\",\"udid\":\"idfaValue\",\"tud\":3,\"eud\":0}";
        result =
                hostedAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParams, mockChannelSegmentEntity, slotId, null);
        assertThat(hostedAdNetwork.getBidRequestJson(), is(equalTo(expectedBidRequestJSON)));
        assertThat(result, is(equalTo(true)));

        // Positive Test Case #2: All mandatory params present + LocationSource is NO_TARGETING + Limit_Ad_Tracking is true => No location and device uid params in request
        expectedBidRequestJSON =
                "{\"id\":10,\"app\":\"997EC9A04C2B01324BD122000B4000BD\",\"clt\":\"INMB_SERVER_NATIVE_1.0.0\",\"rtyp\":\"nativejson\",\"typ\":4,\"ip\":\"127.0.0.1\"}";
        result =
                hostedAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParams, mockChannelSegmentEntity, slotId, null);
        assertThat(hostedAdNetwork.getBidRequestJson(), is(equalTo(expectedBidRequestJSON)));
        assertThat(result, is(equalTo(true)));

        // Negative Test Case #1: app is missing
        expectedBidRequestJSON = null;
        result =
                hostedAdNetwork.configureParameters(mockSasParams, mockCasInternalRequestParams, mockChannelSegmentEntity, slotId, null);
        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void testParseResponseVariations() throws Exception {
        String response;
        String expectedResponseContent;
        String expectedResponseStatus;
        String urlBase = "urlBase";

        mockStaticNice(InspectorStats.class);
        // RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        HttpResponseStatus mockHttpResponseStatus = createMock(HttpResponseStatus.class);

        expect(mockHttpResponseStatus.code())
                .andReturn(200).times(2)
                .andReturn(404).times(1)
                .andReturn(200).times(6);


        // Mocking NativeAdMaking() and isNativeRequest() in HostedAdNetwork
        Object[] constructerArgs = {mockConfig, null, null, null, urlBase, advertiserName, false};
        String[] methodsToBeMocked = {"nativeAdBuilding", "isNativeRequest"};
        HostedAdNetwork hostedAdNetwork = createPartialMock(HostedAdNetwork.class, methodsToBeMocked, constructerArgs);

        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        expect(hostedAdNetwork.isNativeRequest())
                .andReturn(true).times(1);
        hostedAdNetwork.nativeAdBuilding();
        expectLastCall();

        replayAll();

        //Negative Test Case #1: Response is empty
        response = "";
        expectedResponseContent = "";
        expectedResponseStatus = "NO_AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));

        //Negative Test Case #2: Response is null
        response = null;
        expectedResponseContent = "";
        expectedResponseStatus = "NO_AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));

        //Negative Test Case #3: Status Code is != 200
        response = null;
        expectedResponseContent = "";
        expectedResponseStatus = "NO_AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));

        //Negative Test Case #4: Failed Deserialisation as JSON is incorrect (neither response nor error JSON)
        response = "{\"id\":\"10\", \"fail\":\"\"}";
        expectedResponseContent = "";
        expectedResponseStatus = "NO_AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));

        //Negative Test Case #5: ErrorCode = 1001 (Invalid Credentials)
        response = "{\"id\":\"45\",\"status\":\"FAIL\",\"error_msg\":\"Invalid credentials\",\"error_code\":1001}";
        expectedResponseContent = "";
        expectedResponseStatus = "NO_AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));

        //Negative Test Case #6: ErrorCode = 1002 (NO AD Condition for HAS)
        response =
                "{\"id\":\"4567876766666\",\"status\":\"FAIL\",\"error_msg\":\"No ads available to serve\",\"error_code\":1002}";
        expectedResponseContent = "";
        expectedResponseStatus = "NO_AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));

        //Negative Test Case #7: ErrorCode = 1003 (RFM error in selecting ad)
        response =
                "{\"id\":\"456787676666454546\",\"status\":\"FAIL\",\"error_msg\":\"Ad filtered during validation/processing\",\"error_code\":1003}";
        expectedResponseContent = "";
        expectedResponseStatus = "NO_AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));

        //Negative Test Case #8: ErrorCode = 2000 (Unknown Errors from RP)
        response =
                "{\"id\":\"45678767666465655\",\"status\":\"FAIL\",\"error_msg\":\"Can't serve ad as something broke on our side\",\"error_code\":2000}";
        expectedResponseContent = "";
        expectedResponseStatus = "NO_AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));

        //Positive Test Case #1: Successful Deserialisation
        response = "{\"id\":\"10\", \"ads\":[{}]}";
        expectedResponseStatus = "AD";
        hostedAdNetwork.parseResponse(response, mockHttpResponseStatus);
        assertThat(hostedAdNetwork.getAdStatus(), is(equalTo(expectedResponseStatus)));
    }


}



