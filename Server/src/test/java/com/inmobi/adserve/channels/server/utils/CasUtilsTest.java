package com.inmobi.adserve.channels.server.utils;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.ImmutableList;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.segment.impl.AdTypeEnum;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CasConfigUtil.class)
public class CasUtilsTest {
    private final SASRequestParameters sasParams = new SASRequestParameters();
    private final CasUtils casUtils = new CasUtils(EasyMock.createMock(RepositoryHelper.class));

    @Test
    public void testisBannerVideoSupported() {
        mockStatic(CasConfigUtil.class);
        Configuration mockServerConfig = createMock(Configuration.class);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockServerConfig).anyTimes();
        expect(mockServerConfig.getInt("adtype.vast.minimumSupportedSdkVersion", 450)).andReturn(450).anyTimes();
        replayAll();

        final SasParamsTestData[] testData = {
            new SasParamsTestData(GlobalConstant.APP, "a370", 3, "4.0", false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL),
            new SasParamsTestData(GlobalConstant.APP, "a450", 3, "4.4", true, DemandSourceType.IX, RequestedAdType.INTERSTITIAL),
            new SasParamsTestData(GlobalConstant.APP, "i370", 5, "6.0", false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", true, DemandSourceType.IX, RequestedAdType.INTERSTITIAL),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", false, DemandSourceType.IX, RequestedAdType.NATIVE),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", false, DemandSourceType.IX, RequestedAdType.BANNER),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", false, DemandSourceType.IX, RequestedAdType.INLINE_BANNER),
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", false, DemandSourceType.RTBD, RequestedAdType.INTERSTITIAL), // Video not supported on RTBD
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "6.0", false, DemandSourceType.DCP, RequestedAdType.INTERSTITIAL), // Video not supported on DCP
            new SasParamsTestData(GlobalConstant.APP, "i450", 5, "5.0", false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL), // Unsupported iOS version
            new SasParamsTestData(GlobalConstant.APP, "a450", 3, "3.0", false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL), // Unsupported Android version
            new SasParamsTestData(GlobalConstant.WAP, "a450", 3, "4.0", false, DemandSourceType.IX, RequestedAdType.INTERSTITIAL),  // Unsupported Source
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

            final boolean testResult = casUtils.isVideoSupported(sasParams);
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

        SasParamsTestData(final String source, final String sdkVersion, final int osId, final String osVersion,
            final boolean expectedResult, final DemandSourceType dst, final RequestedAdType requestedAdType) {
            this.source = source;
            this.sdkVersion = sdkVersion;
            this.osId = osId;
            this.osVersion = osVersion;
            this.expectedResult = expectedResult;
            this.dst = dst;
            this.requestedAdType = requestedAdType;
        }
    }
}
