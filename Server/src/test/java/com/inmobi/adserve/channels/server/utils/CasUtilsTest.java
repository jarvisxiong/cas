package com.inmobi.adserve.channels.server.utils;

import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.MOVIEBOARD;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.VAST;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableList;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.segment.impl.AdTypeEnum;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CasConfigUtil.class, NativeAdTemplateEntity.class})
public class CasUtilsTest {
    private final SASRequestParameters sasParams = new SASRequestParameters();
    private final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
    private final CasUtils casUtils = new CasUtils(mockRepositoryHelper);
    private final long PLACEMENT_ID = 1L;

    @Test
    public void testisBannerVideoSupported() {
        mockStatic(CasConfigUtil.class);
        Configuration mockServerConfig = createMock(Configuration.class);
        NativeAdTemplateEntity mockNativeAdTemplateEntity = PowerMock.createMock(NativeAdTemplateEntity.class);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockServerConfig).anyTimes();
        expect(mockServerConfig.getInt("adtype.vast.minimumSupportedSdkVersion", 450)).andReturn(450).anyTimes();
        expect(mockServerConfig.getDouble("adtype.vast.minimumSupportedAndroidVersion")).andReturn(4.0).anyTimes();
        expect(mockServerConfig.getDouble("adtype.vast.minimumSupportedIOSVersion")).andReturn(6.0).anyTimes();
        expect(mockServerConfig.getDouble("adtype.inlineBanner.minimumSupportedIOSVersion")).andReturn(7.0).anyTimes();
        expect(mockServerConfig.getDouble("adtype.inlineBanner.minimumSupportedAndroidVersion")).andReturn(4.0).anyTimes();
        expect(mockRepositoryHelper
                .queryNativeAdTemplateRepository(PLACEMENT_ID, MOVIEBOARD))
                .andReturn(mockNativeAdTemplateEntity).times(1).andReturn(null).anyTimes();
        expect(mockRepositoryHelper
                .queryNativeAdTemplateRepository(PLACEMENT_ID, VAST))
                .andReturn(null).anyTimes();
        replayAll();

        final SasParamsTestData[] testData = {
            new SasParamsTestData(GlobalConstant.APP, "a370", 3, "4.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false),
            new SasParamsTestData(GlobalConstant.APP, "a450", 3, "4.4", IntegrationType.ANDROID_SDK, true, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false),
            new SasParamsTestData(GlobalConstant.APP, "i370", 5, "6.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", IntegrationType.ANDROID_SDK, true, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false),
            new SasParamsTestData(GlobalConstant.APP, null, 3, "4.4", IntegrationType.ANDROID_API, false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false),
            new SasParamsTestData(GlobalConstant.APP, null, 5, "6.0", IntegrationType.ANDROID_API, false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false),
            new SasParamsTestData(GlobalConstant.APP, null, 5, "6.0", IntegrationType.ANDROID_API, true, DemandSourceType.IX, RequestedAdType.VAST, false),
            new SasParamsTestData(GlobalConstant.APP, null, 5, "6.0", IntegrationType.ANDROID_API, false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.NATIVE, false),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.BANNER, false),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.INLINE_BANNER, false),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.RTBD, RequestedAdType.INTERSTITIAL, false), // Video not supported on RTBD
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.DCP, RequestedAdType.INTERSTITIAL, false), // Video not supported on DCP
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "5.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false), // Unsupported iOS version
            new SasParamsTestData(GlobalConstant.APP, "a450", 3, "3.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false), // Unsupported Android version
            new SasParamsTestData(GlobalConstant.WAP, "a450", 3, "4.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL, false),  // Unsupported Source
            // Inline Banner MovieBoardReq +ve
            new SasParamsTestData(GlobalConstant.APP, "a450", 3, "5.0", IntegrationType.ANDROID_SDK, true, DemandSourceType.IX, RequestedAdType.INLINE_BANNER, true),
            // OS version mismatch
            new SasParamsTestData(GlobalConstant.APP, "a450", 3, "3.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.INLINE_BANNER, true),
            // No Template found
            new SasParamsTestData(GlobalConstant.APP, "a450", 3, "5.0", IntegrationType.ANDROID_SDK, false, DemandSourceType.IX, RequestedAdType.INLINE_BANNER, true),
        };

        int i = 1;
        for (final SasParamsTestData data : testData) {

            // This print statement will help in quickly identifying the failure, if any.
            System.out.println("Running test with input # " + i++);

            // Set SasParams values
            sasParams.setSource(data.source);
            sasParams.setSdkVersion(data.sdkVersion);
            sasParams.setOsId(data.osId);
            sasParams.setOsMajorVersion(data.osVersion);
            sasParams.setPubControlSupportedAdTypes(ImmutableList.of(AdTypeEnum.VIDEO));
            sasParams.setDst(data.dst.getValue());
            sasParams.setRequestedAdType(data.requestedAdType);
            sasParams.setPlacementId(PLACEMENT_ID);

            IntegrationDetails integrationDetails = new IntegrationDetails();
            integrationDetails.setIntegrationType(data.integrationType);
            sasParams.setIntegrationDetails(integrationDetails);
            sasParams.setMovieBoardRequest(data.isMovieboardSupported);
            final boolean testResult = casUtils.isVideoSupportedSite(sasParams);
            assertEquals(data.expectedResult, testResult);
        }
    }

    /**
     * Inner class to hold required sasParams data for testing testisBannerVideoSupported().
     */
    class SasParamsTestData {
        final String source;
        final String sdkVersion;
        final int osId;
        final String osVersion;
        final boolean expectedResult;
        final DemandSourceType dst;
        final RequestedAdType requestedAdType;
        final IntegrationType integrationType;
        final boolean isMovieboardSupported;

        SasParamsTestData(final String source, final String sdkVersion, final int osId, final String osVersion, final
                          IntegrationType integrationType, final boolean expectedResult, final DemandSourceType dst,
                          final RequestedAdType requestedAdType, final boolean isMovieboardSupported) {
            this.source = source;
            this.sdkVersion = sdkVersion;
            this.osId = osId;
            this.osVersion = osVersion;
            this.expectedResult = expectedResult;
            this.dst = dst;
            this.requestedAdType = requestedAdType;
            this.integrationType = integrationType;
            this.isMovieboardSupported = isMovieboardSupported;
        }
    }
}
