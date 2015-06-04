package com.inmobi.adserve.channels.api.trackers;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;

public class DefaultLazyInmobiAdTrackerTest {
    public static SASRequestParameters baseSasParams;
    public static Configuration mockConfiguration;

    @BeforeClass
    public static void setUp() {
        baseSasParams = new SASRequestParameters();
        mockConfiguration = createNiceMock(Configuration.class);

        baseSasParams.setAge((short)20);
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
        baseSasParams.setIntegrationDetails(new IntegrationDetails().setIntegrationType(
                IntegrationType.ANDROID_SDK).setIntegrationVersion(370).setIntegrationMethod(IntegrationMethod.SDK));

        expect(mockConfiguration.getString("key.1.value")).andReturn("clickmaker.key.1.value").anyTimes();
        expect(mockConfiguration.getString("key.2.value")).andReturn("clickmaker.key.2.value").anyTimes();
        expect(mockConfiguration.getString("beaconURLPrefix")).andReturn("http://localhost:8800").anyTimes();
        expect(mockConfiguration.getString("clickURLPrefix")).andReturn("http://localhost:8800").anyTimes();

        replay(mockConfiguration);

        DefaultLazyInmobiAdTrackerBuilder.init(mockConfiguration);
    }

    @Test
    public void testDefaultLazyInmobiAdTrackerDefaultCase() {
        SASRequestParameters sasParams = copyHelper();
        DefaultLazyInmobiAdTrackerBuilder builder = new DefaultLazyInmobiAdTrackerBuilder(sasParams, "76256371268",
                true);
        DefaultLazyInmobiAdTracker inmobiAdTracker = builder.buildInmobiAdTracker();

        String expectedBeaconUrl = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/"
                + "76256371268/-1/5l/-1/0/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/"
                + "2BBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/4d5de1c7";
        String expectedClickUrl = "http://localhost:8800/C/t/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/"
                + "76256371268/-1/5l/-1/1/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/"
                + "2BBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/eb576f7e";

        assertThat(inmobiAdTracker.getBeaconUrl(), is(equalTo(expectedBeaconUrl)));
        assertThat(inmobiAdTracker.getClickUrl(), is(equalTo(expectedClickUrl)));
    }

    @Test
    public void testDefaultLazyInmobiAdTrackerWithCPCFalse() {
        SASRequestParameters sasParams = copyHelper();
        DefaultLazyInmobiAdTrackerBuilder builder = new DefaultLazyInmobiAdTrackerBuilder(sasParams, "76256371268",
                false);
        DefaultLazyInmobiAdTracker inmobiAdTracker = builder.buildInmobiAdTracker();

        String expectedBeaconUrl = "http://localhost:8800/C/b/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/"
                + "76256371268/-1/5l/-1/0/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/"
                + "2BBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/12690401";
        String expectedClickUrl = "http://localhost:8800/C/b/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/"
                + "76256371268/-1/5l/-1/1/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/"
                + "2BBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/241db49b";

        assertThat(inmobiAdTracker.getBeaconUrl(), is(equalTo(expectedBeaconUrl)));
        assertThat(inmobiAdTracker.getClickUrl(), is(equalTo(expectedClickUrl)));
    }

    @Test
    public void testDefaultLazyInmobiAdTrackerPlacementIdIsNotNullButPlacementSegmentIdIsPresent() {
        SASRequestParameters sasParams = copyHelper();
        sasParams.setPlacementSegmentId(5678);
        DefaultLazyInmobiAdTrackerBuilder builder = new DefaultLazyInmobiAdTrackerBuilder(sasParams, "76256371268",
                false);
        DefaultLazyInmobiAdTracker inmobiAdTracker = builder.buildInmobiAdTracker();

        String expectedBeaconUrl = "http://localhost:8800/C/b/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/"
                + "76256371268/-1/5l/-1/0/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/"
                + "2BBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/12690401";
        String expectedClickUrl = "http://localhost:8800/C/b/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/"
                + "76256371268/-1/5l/-1/1/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/"
                + "2BBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/241db49b";

        assertThat(inmobiAdTracker.getBeaconUrl(), is(equalTo(expectedBeaconUrl)));
        assertThat(inmobiAdTracker.getClickUrl(), is(equalTo(expectedClickUrl)));
    }

    @Test
    public void testDefaultLazyInmobiAdTrackerBothPlacementIdAndPlacementSegmentIdArePresent() {
        SASRequestParameters sasParams = copyHelper();
        sasParams.setPlacementId(1234L);
        sasParams.setPlacementSegmentId(5678);
        DefaultLazyInmobiAdTrackerBuilder builder = new DefaultLazyInmobiAdTrackerBuilder(sasParams, "76256371268",
                false);
        DefaultLazyInmobiAdTracker inmobiAdTracker = builder.buildInmobiAdTracker();

        String expectedBeaconUrl = "http://localhost:8800/C/b/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/"
                + "76256371268/-1/4dq/-1/0/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/"
                + "NqQTqBBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/8e3980bf";
        String expectedClickUrl = "http://localhost:8800/C/b/1/1/1/c/2/m/k/0/0/eyJVRElEIjoidWlkdmFsdWUifQ~~/"
                + "76256371268/-1/4dq/-1/1/0/x/0/nw/101/1/sdk/3.7.0/-1/YXBwQnVuZGxlSWQ~/"
                + "NqQTqBBub3JtYWxpemVkVXNlcklkGAxJTlRFUlNUSVRJQUwA/1/813f0f69";

        assertThat(inmobiAdTracker.getBeaconUrl(), is(equalTo(expectedBeaconUrl)));
        assertThat(inmobiAdTracker.getClickUrl(), is(equalTo(expectedClickUrl)));
    }

    private static final SASRequestParameters copyHelper() {
        try {
            return (SASRequestParameters) BeanUtils.cloneBean(baseSasParams);
        } catch (Exception ignored) {
            return null;
        }
    }

}