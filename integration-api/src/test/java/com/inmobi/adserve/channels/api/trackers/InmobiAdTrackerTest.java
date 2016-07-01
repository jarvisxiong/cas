package com.inmobi.adserve.channels.api.trackers;

import static junit.framework.TestCase.fail;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reflections.Reflections;

import com.google.common.collect.ImmutableMap;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;

import lombok.RequiredArgsConstructor;

/**
 * This test class contains all the test cases for InmobiAdTracker and InmobiAdTrackerBuilder
 */
@RequiredArgsConstructor
@RunWith(Parameterized.class)
public class InmobiAdTrackerTest {
    public static SASRequestParameters mockSasParams;
    public static Configuration mockConfiguration;
    public final Class<? extends InmobiAdTrackerBuilder> inmobiAdTrackerBuilderClass;
    public final Class<? extends InmobiAdTracker> inmobiAdTrackerClass;

    static {
        Reflections reflections = new Reflections("com.inmobi.adserve.channels.api.trackers");

        if (reflections.getSubTypesOf(InmobiAdTracker.class).size() != data().size()) {
            fail("SubClass of InmobiAdTracker has not been tested. Add it to the data method in "
                    + "InmobiAdTrackerBuilderTest.");
        }
    }

    @BeforeClass
    public static void setUp() {
        mockSasParams = createNiceMock(SASRequestParameters.class);
        mockConfiguration = createNiceMock(Configuration.class);

        expect(mockSasParams.getAge()).andReturn((short)20).anyTimes();
        expect(mockSasParams.getCarrierId()).andReturn(2).anyTimes();
        expect(mockSasParams.getCountryId()).andReturn(12L).anyTimes();
        expect(mockSasParams.getSiteSegmentId()).andReturn(201).anyTimes();
        expect(mockSasParams.getGender()).andReturn("m").anyTimes();
        expect(mockSasParams.getHandsetInternalId()).andReturn(1L).anyTimes();
        expect(mockSasParams.getIpFileVersion()).andReturn(1).anyTimes();
        expect(mockSasParams.getTUidParams()).andReturn(ImmutableMap.of("UDID", "uidvalue")).anyTimes();
        expect(mockSasParams.getState()).andReturn(0).anyTimes();
        expect(mockSasParams.getSiteIncId()).andReturn(1L).anyTimes();
        expect(mockSasParams.getDst()).andReturn(2).anyTimes();
        expect(mockSasParams.getPlacementId()).andReturn(null).anyTimes();
        expect(mockSasParams.getNormalizedUserId()).andReturn("normalizedUserId").anyTimes();
        expect(mockSasParams.getAppBundleId()).andReturn("appBundleId").anyTimes();
        expect(mockSasParams.getRequestedAdType()).andReturn(RequestedAdType.INTERSTITIAL).anyTimes();
        expect(mockSasParams.getIntegrationDetails()).andReturn(new IntegrationDetails().setIntegrationType(
                IntegrationType.ANDROID_SDK).setIntegrationVersion(370).setIntegrationMethod(IntegrationMethod.SDK))
                .anyTimes();
        expect(mockSasParams.isCoppaEnabled()).andReturn(true).anyTimes();
        expect(mockSasParams.isSandBoxRequest()).andReturn(false).anyTimes();

        expect(mockConfiguration.getString("key.1.value")).andReturn("clickmaker.key.1.value").anyTimes();
        expect(mockConfiguration.getString("key.2.value")).andReturn("clickmaker.key.2.value").anyTimes();
        expect(mockConfiguration.getString("beaconURLPrefix")).andReturn("http://localhost:8800").anyTimes();
        expect(mockConfiguration.getString("clickURLPrefix")).andReturn("http://localhost:8800").anyTimes();

        replay(mockSasParams, mockConfiguration);

        DefaultLazyInmobiAdTrackerBuilder.init(mockConfiguration);
    }

    @Parameters
    public static Collection<Object> data() {
        return Arrays.asList(new Object[][] {
                {DefaultLazyInmobiAdTrackerBuilder.class, DefaultLazyInmobiAdTracker.class}
        });
    }

    @Test
    public void testBuildInmobiAdTrackerContract() throws Exception {
        final InmobiAdTrackerBuilder inmobiAdTrackerBuilder = (InmobiAdTrackerBuilder)
                (inmobiAdTrackerBuilderClass.getConstructors()[0]).newInstance(mockSasParams, "76256371268", true);
        final InmobiAdTracker inmobiAdTracker = inmobiAdTrackerBuilder.buildInmobiAdTracker();

        assertThat(inmobiAdTracker.getClass(), is(equalTo(inmobiAdTrackerClass)));
    }
}