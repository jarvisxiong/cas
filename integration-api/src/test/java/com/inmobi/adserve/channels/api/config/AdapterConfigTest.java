package com.inmobi.adserve.channels.api.config;

import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.util.List;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created by avinash.kumar on 4/25/16.
 */
public class AdapterConfigTest {
    private Configuration mockAdapterConfig;
    private ServerConfig mockServerConfig;

    private static final String westStateCode[] = {"10993", "12131", "12539", "28962", "13365", "17188", "28963", "18469", "19855",
            "20598", "22140", "23451", "28994", "11326", "12869", "28970", "14690", "15118", "16121", "28965",
            "17799", "29000", "20372", "21892", "22839", "28978"};

    private static final List<String> usaWestStateCodeList = Arrays.asList(westStateCode);
    private static final String uj1Host = "http://uj1/Host/";
    private static final String uh1Host = "http://uh1/Host/";
    private static final String lhr1Host = "http://lhr1/Host/";
    private static final String hkg1Host = "http://hkg1/Host/";
    private static final String defaultHost = "http://localhost:8091/getIXBid";

    @BeforeTest
    public void setUp() throws Exception {

        final String className = "com.inmobi.adserve.channels.api.config.AdapterConfig";

        mockAdapterConfig = EasyMock.createMock(Configuration.class);
        expect(mockAdapterConfig.getString("host.uj1")).andReturn(uj1Host).anyTimes();
        expect(mockAdapterConfig.getString("host.uh1")).andReturn(uh1Host).anyTimes();
        expect(mockAdapterConfig.getString("host.lhr1")).andReturn(lhr1Host).anyTimes();
        expect(mockAdapterConfig.getString("host.hkg1")).andReturn(hkg1Host).anyTimes();
        expect(mockAdapterConfig.getString("host.sandbox")).andReturn(defaultHost).anyTimes();
        expect(mockAdapterConfig.getString("class")).andReturn(className).anyTimes();
        EasyMock.replay(mockAdapterConfig);

        mockServerConfig = EasyMock.createMock(ServerConfig.class);
        expect(mockServerConfig.getRoutingUH1ToUJ1Percentage()).andReturn(100).anyTimes();
        expect(mockServerConfig.getUSWestStatesCodes()).andReturn(usaWestStateCodeList).anyTimes();

        EasyMock.replay(mockServerConfig);

    }

    @DataProvider(name = "USWestStateCodeProvider")
    public Object[][] dataProviderTestUSWestStateHost() {
        final String dcName[] = {"uh1", "lhr1", "hkg1"};
        final Integer westSateCode[] =
                {10993, 12131, 12539, 28962, 13365, 17188, 28963, 18469, 19855, 20598, 22140, 23451, 28994, 11326,
                        12869, 28970, 14690, 15118, 16121, 28965, 17799, 29000, 20372, 21892, 22839, 28978};

        Object[][] objects = new Object[dcName.length * westSateCode.length][3];
        int i = 0;
        for (String dc : dcName) {
            for (Integer code : westSateCode) {
                if (dc.equalsIgnoreCase("uh1")) {
                    objects[i++] = new Object[] {dc, code, "uj1"};
                } else {
                    objects[i++] = new Object[] {dc, code, dc};
                }
            }
        }
        return objects;
    }

    @Test(dataProvider = "USWestStateCodeProvider")
    public void testUsWestStateHost(final String dc, final int stateCode, final String expectedDc) {
        final AdapterConfig adapterConfig = new AdapterConfig(mockAdapterConfig, "adapter", dc, mockServerConfig);
        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setState(stateCode);
        sasParams.setSandBoxRequest(false);
        final String host = adapterConfig.getAdapterHost(sasParams, true);
        Assert.assertTrue(host.contains(expectedDc));
    }

    @DataProvider(name = "USEastStateCodeProvider")
    public Object[][] dataProviderTestUSEastStateHost() {
        final String dcName[] = {"uh1", "lhr1", "hkg1"};
        final Integer eastStateCode[] = {null, 53230, 13996, 53236, 28961, 28973, 29001, 12537, 15494, 22960, 23890,
                11135, 15753, 23084, 16658, 23713, 18569, 17025, 21619, 17848, 20778, 18003, 15310, 10688, 19399, 14902,
                12328, 14343, 53240};

        Object[][] objects = new Object[dcName.length * eastStateCode.length][3];
        int i = 0;
        for (String dc : dcName) {
            for (Integer code : eastStateCode) {
                objects[i++] = new Object[] {dc, code, dc};
            }
        }
        return objects;
    }

    @Test(dataProvider = "USEastStateCodeProvider")
    public void testUsEastStateHost(final String dc, final Integer stateCode, final String expectedDc) {
        final AdapterConfig adapterConfig = new AdapterConfig(mockAdapterConfig, "adapter", dc, mockServerConfig);
        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setState(stateCode);
        sasParams.setSandBoxRequest(false);
        final String host = adapterConfig.getAdapterHost(sasParams, true);
        Assert.assertTrue(host.contains(expectedDc));
    }

    @DataProvider(name = "integration request")
    public Object[][] dataProviderIntegrationRequest() {
        final String dcName[] = {"uh1", "lhr1", "hkg1", "uj1"};

        Object[][] objects = new Object[dcName.length][1];
        int i = 0;
        for (String dc : dcName) {
                objects[i++] = new Object[] {dc};
        }
        return objects;
    }
    @Test(dataProvider = "integration request")
    public void testIntegrationRequest(final String dc) {
        final AdapterConfig adapterConfig = new AdapterConfig(mockAdapterConfig, "adapter", dc, mockServerConfig);
        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSandBoxRequest(true);
        String host = adapterConfig.getAdapterHost(sasParams, false);
        Assert.assertTrue(StringUtils.equals(host, defaultHost));
        sasParams.setSandBoxRequest(false);
        host = adapterConfig.getAdapterHost(sasParams, false);
        final String expectedHost = "http://"+ dc + "/Host/";
        Assert.assertTrue(StringUtils.equals(host, expectedHost));
    }


}
