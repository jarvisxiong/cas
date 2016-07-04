package com.inmobi.adserve.channels.api.trackers;

import static com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerHelper.getIdBase36;
import static com.inmobi.adserve.channels.util.Utils.TestUtils.SampleStrings.placementId;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.Configuration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.Utils.TestUtils.SampleStrings;
import com.inmobi.types.eventserver.ImpressionInfo;


public class ParameterizedDefaultLazyInmobiAdTrackerTest {
    private static SASRequestParameters baseSasParams;
    private static final Integer DEFAULT_SEGMENT_ID = 0;

    @BeforeClass
    public void setUp() {
        baseSasParams = new SASRequestParameters();
        final Configuration mockConfiguration = createNiceMock(Configuration.class);

        baseSasParams.setAge((short) 20);
        baseSasParams.setCarrierId(2);
        baseSasParams.setCountryId(12L);
        baseSasParams.setSiteSegmentId(201);
        baseSasParams.setGender("m");
        baseSasParams.setHandsetInternalId(1L);
        baseSasParams.setIpFileVersion(1);
        baseSasParams.setTUidParams(ImmutableMap.of("UDID", "uidvalue"));
        baseSasParams.setState(0);
        baseSasParams.setSiteIncId(1L);
        baseSasParams.setDst(2);
        baseSasParams.setPlacementId(null);
        baseSasParams.setNormalizedUserId("normalizedUserId");
        baseSasParams.setAppBundleId("appBundleId");
        baseSasParams.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        baseSasParams.setIntegrationDetails(new IntegrationDetails().setIntegrationType(IntegrationType.ANDROID_SDK)
                .setIntegrationVersion(370).setIntegrationMethod(IntegrationMethod.SDK));

        expect(mockConfiguration.getString("key.1.value")).andReturn("clickmaker.key.1.value").anyTimes();
        expect(mockConfiguration.getString("key.2.value")).andReturn("clickmaker.key.2.value").anyTimes();
        expect(mockConfiguration.getString("beaconURLPrefix")).andReturn("http://localhost:8800").anyTimes();
        expect(mockConfiguration.getString("clickURLPrefix")).andReturn("http://localhost:8800").anyTimes();

        replay(mockConfiguration);

        DefaultLazyInmobiAdTrackerBuilder.init(mockConfiguration);

        final short hostIdCode = (short) 5;
        final byte dataCenterIdCode = 1;
        ImpressionIdGenerator.init(hostIdCode, dataCenterIdCode);
    }

    private static SASRequestParameters copyHelper() {
        try {
            return (SASRequestParameters) BeanUtils.cloneBean(baseSasParams);
        } catch (Exception ignored) {
            return null;
        }
    }


    @DataProvider(name = "DataProviderForBeaconBillingRelatedPlacementContract")
    public Object[][] paramDataProviderForClearingPrice() {
        return new Object[][] {
                {"PlacementIdSet.PlacementSegmentIdSet.SiteSegmentIdNotSet", placementId, 12, 13, getIdBase36(13)},
                {"PlacementIdSet.PlacementSegmentIdNotSet.SiteSegmentIdNotSet", placementId, null, null, getIdBase36(DEFAULT_SEGMENT_ID)},
                {"PlacementIdNotSet.PlacementSegmentIdSet.SiteSegmentIdSet", null, 12, 13, getIdBase36(13)},
                {"PlacementIdNotSet.PlacementSegmentIdSet.SiteSegmentIdNotSet", null, 12, null, getIdBase36(DEFAULT_SEGMENT_ID)},
        };
    }

    @Test(dataProvider = "DataProviderForBeaconBillingRelatedPlacementContract")
    public void verifyBillingRelatedPlacementContract(final String testCaseName, final Long placementId,
            final Integer placementSegmentId, final Integer siteSegmentId, final String expectedSegmentId) throws Exception {

        final SASRequestParameters sasParams = copyHelper();
        sasParams.setPlacementId(placementId);
        sasParams.setPlacementSegmentId(placementSegmentId);
        sasParams.setSiteSegmentId(siteSegmentId);

        final DefaultLazyInmobiAdTrackerBuilder builder =
                new DefaultLazyInmobiAdTrackerBuilder(sasParams, SampleStrings.impressionId, true);
        final DefaultLazyInmobiAdTracker inmobiAdTracker = builder.buildInmobiAdTracker();

        final String actualBeaconUrl = inmobiAdTracker.getBeaconUrl();
        final ImpressionInfo extractedImpressionInfo = DefaultLazyInmobiAdTrackerUtils.extractImpressionInfo(actualBeaconUrl);
        final String extractedSegmentId = DefaultLazyInmobiAdTrackerUtils.extractSegmentId(actualBeaconUrl);

        assertThat(extractedImpressionInfo.isSetPlacementId()?extractedImpressionInfo.getPlacementId():null, is(equalTo(placementId)));
        assertThat(extractedImpressionInfo.isSetPlacementSegmentId()?(int)extractedImpressionInfo.getPlacementSegmentId():null, is(equalTo(placementSegmentId)));
        assertThat(extractedImpressionInfo.isSetSiteSegmentId()?(int)extractedImpressionInfo.getSiteSegmentId():null, is(equalTo(siteSegmentId)));
        assertThat(extractedSegmentId, is(equalTo(expectedSegmentId)));
    }
}