package com.inmobi.adserve.channels.server.utils;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.Arrays;

public class CasUtilsTest extends TestCase {

    private final SASRequestParameters sasParams = new SASRequestParameters();
    private final CasUtils casUtils = new CasUtils(EasyMock.createMock(RepositoryHelper.class));


    @Test
    public void testisBannerVideoSupported() {

        SasParamsTestData[] testData = {
                new SasParamsTestData("APP", "a370", (short) 14, 3, "4.0", true),
                new SasParamsTestData("APP", "a369", (short) 14, 3, "4.0", false),
                new SasParamsTestData("APP", "a370", (short) 14, 3, "4.4", true),
                new SasParamsTestData("APP", "a440", (short) 32, 3, "4.4", true),
                new SasParamsTestData("APP", "i370", (short) 14, 5, "6.0", true),
                new SasParamsTestData("APP", "i440", (short) 32, 5, "6.0", true),
                new SasParamsTestData("APP", "i440", (short) 32, 5, "5.0", false),  // Unsupported iOS version
                new SasParamsTestData("APP", "a370", (short) 14, 3, "3.0", false),  // Unsupported Android version.
                new SasParamsTestData("APP", "a370", (short) 16, 3, "4.0", false),  // Unsupported Slot
                new SasParamsTestData("WAP", "a370", (short) 14, 3, "4.0", false)}; // Unsupported Source

        int i = 1;
        for (SasParamsTestData data : testData) {

            // This print statement will help in quickly identifying the failure, if any.
            System.out.println("Running test with input # " + i++);

            // Set SasParams values
            sasParams.setSource(data.source);
            sasParams.setSdkVersion(data.sdkVersion);
            sasParams.setSlot(data.slot);
            sasParams.setOsId(data.osId);
            sasParams.setOsMajorVersion(data.osVersion);

            boolean testResult = casUtils.isBannerVideoSupported(sasParams);
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

        SasParamsTestData(final String source, final String sdkVersion, final short slot,
                          final int osId, final String osVersion, boolean expectedResult) {
            this.source = source;
            this.sdkVersion = sdkVersion;
            this.slot = slot;
            this.osId = osId;
            this.osVersion = osVersion;
            this.expectedResult = expectedResult;
        }
    }
}
