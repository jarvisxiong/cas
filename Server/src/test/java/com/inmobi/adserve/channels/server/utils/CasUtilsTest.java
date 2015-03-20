package com.inmobi.adserve.channels.server.utils;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

public class CasUtilsTest extends TestCase {

    private final SASRequestParameters sasParams = new SASRequestParameters();
    private final CasUtils casUtils = new CasUtils(EasyMock.createMock(RepositoryHelper.class));


    @Test
    public void testisBannerVideoSupported() {

        final SasParamsTestData[] testData =
                {new SasParamsTestData("APP", "a370", (short) 14, 3, "4.0", true, Arrays.asList((short) 14)),
                        new SasParamsTestData("APP", "a370", (short) 14, 3, "4.4", true, Arrays.asList((short) 32)),
                        new SasParamsTestData("APP", "a440", (short) 32, 3, "4.4", true, Arrays.asList((short) 14)),
                        new SasParamsTestData("APP", "i370", (short) 14, 5, "6.0", true, Arrays.asList((short) 32)),
                        new SasParamsTestData("APP", "i440", (short) 32, 5, "6.0", true, Arrays.asList((short) 14)),
                        new SasParamsTestData("APP", "i440", (short) 32, 5, "5.0", false, Arrays.asList((short) 32)), // Unsupported iOS version
                        new SasParamsTestData("APP", "a370", (short) 14, 3, "3.0", false, Arrays.asList((short) 14)), // Unsupported Android
                        // version.
                        new SasParamsTestData("WAP", "a370", (short) 14, 3, "4.0", false, Arrays.asList((short) 32))}; // Unsupported Source

        int i = 1;
        for (final SasParamsTestData data : testData) {

            // This print statement will help in quickly identifying the failure, if any.
            System.out.println("Running test with input # " + i++);

            // Set SasParams values
            sasParams.setSource(data.source);
            sasParams.setSdkVersion(data.sdkVersion);
            sasParams.setProcessedMkSlot(Arrays.asList(data.slot));
            sasParams.setOsId(data.osId);
            sasParams.setOsMajorVersion(data.osVersion);
            sasParams.setProcessedMkSlot(data.rqMkSlot);

            final boolean testResult = casUtils.isVideoSupported(sasParams);
            assertEquals(data.expectedResult, testResult);
        }
    }

    /**
     * Inner class to hold required sasParams data for testing testisBannerVideoSupported().
     */
    class SasParamsTestData {
        String source;
        String sdkVersion;
        short slot;
        int osId;
        String osVersion;
        boolean expectedResult;
        List<Short> rqMkSlot;


        SasParamsTestData(final String source, final String sdkVersion, final short slot, final int osId,
                          final String osVersion, final boolean expectedResult, final List<Short> rqMkSlot) {
            this.source = source;
            this.sdkVersion = sdkVersion;
            this.slot = slot;
            this.osId = osId;
            this.osVersion = osVersion;
            this.expectedResult = expectedResult;
            this.rqMkSlot = rqMkSlot;
        }
    }
}
