package com.inmobi.adserve.channels.server;

import static com.inmobi.adserve.channels.server.ChannelServerHelper.CONTAINER_NAME_KEY;
import static com.inmobi.adserve.channels.server.ChannelServerHelper.DATA_CENTRE_ID_KEY;
import static com.inmobi.adserve.channels.server.ChannelServerHelper.DATA_CENTRE_NAME_KEY;
import static com.inmobi.adserve.channels.server.ChannelServerHelper.NON_PROD_CONTAINER_ID;
import static com.inmobi.adserve.channels.server.ChannelServerHelper.NON_PROD_CONTAINER_NAME;
import static com.inmobi.adserve.channels.server.ChannelServerHelper.NON_PROD_DATA_CENTRE_ID;
import static com.inmobi.adserve.channels.server.ChannelServerHelper.NON_PROD_DATA_CENTRE_NAME;
import static com.inmobi.adserve.channels.server.ChannelServerHelper.RUN_ENVIRONMENT_KEY;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.NON_PROD;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.PROD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created by ishan.bhatnagar on 08/10/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ChannelServerHelper.class})
public class ChannelServerHelperTest {
    private ChannelServerHelper channelServerHelper;

    @BeforeClass
    public void setUp() {
        channelServerHelper = new ChannelServerHelper();
    }

    @DataProvider(name = "DataProviderForDataCentreName")
    public Object[][] paramDataProviderForDataCentreName() {
        return new Object[][] {
            {"NoEnvironment", "anything", null, NON_PROD_DATA_CENTRE_NAME},
            {"NonProdEnvironment", "anything", NON_PROD, NON_PROD_DATA_CENTRE_NAME},
            {"ProdEnvironmentDataCentreSet", "anything", PROD, "anything"},
            {"ProdEnvironmentDataCentreNull", null, PROD, null},
        };
    }

    @Test(dataProvider = "DataProviderForDataCentreName")
    public void testGetDataCentreName(final String testCaseName, final String dataCentreName,
        final String runEnvironment, final String expectedDataCentreName) throws Exception {

        if (null != dataCentreName) {
            System.setProperty(DATA_CENTRE_NAME_KEY, dataCentreName);
        } else {
            System.clearProperty(DATA_CENTRE_NAME_KEY);
        }
        if (null != runEnvironment) {
            System.setProperty(RUN_ENVIRONMENT_KEY, runEnvironment);
        } else {
            System.clearProperty(RUN_ENVIRONMENT_KEY);
        }

        assertThat(channelServerHelper.getDataCentreName(), is(equalTo(expectedDataCentreName)));
    }

    @DataProvider(name = "DataProviderForDataCentreId")
    public Object[][] paramDataProviderForDataCentreId() {
        return new Object[][] {
            {"NoEnvironment", "anything", null, NON_PROD_DATA_CENTRE_ID},
            {"NonProdEnvironment", "anything", NON_PROD, NON_PROD_DATA_CENTRE_ID},
            {"ProdEnvironmentDataCentreSet", "5", PROD, Byte.parseByte("5")},
            {"ProdEnvironmentDataCentreNull", null, PROD, null},
            {"ProdEnvironmentDataCentreNumberFormatException", "anything", PROD, null},
        };
    }

    @Test(dataProvider = "DataProviderForDataCentreId")
    public void testGetDataCentreId(final String testCaseName, final String dataCentreId,
        final String runEnvironment, final Byte expectedDataCentreId) throws Exception {

        if (null != dataCentreId) {
            System.setProperty(DATA_CENTRE_ID_KEY, dataCentreId);
        } else {
            System.clearProperty(DATA_CENTRE_ID_KEY);
        }
        if (null != runEnvironment) {
            System.setProperty(RUN_ENVIRONMENT_KEY, runEnvironment);
        } else {
            System.clearProperty(RUN_ENVIRONMENT_KEY);
        }

        assertThat(channelServerHelper.getDataCentreId(), is(equalTo(expectedDataCentreId)));
    }
    
    @DataProvider(name = "DataProviderForContainerName")
    public Object[][] paramDataProviderForContainerName() {
        return new Object[][] {
            {"NoEnvironment", "anything", null, NON_PROD_CONTAINER_NAME},
            {"NonProdEnvironment", "anything", NON_PROD, NON_PROD_CONTAINER_NAME},
            {"ProdEnvironmentContainerNameSet", "anything", PROD, "anything"},
            {"ProdEnvironmentContainerNameNull", null, PROD, null},
        };
    }

    @Test(dataProvider = "DataProviderForContainerName")
    public void testGetContainerName(final String testCaseName, final String ContainerName,
        final String runEnvironment, final String expectedContainerName) throws Exception {

        if (null != ContainerName) {
            System.setProperty(CONTAINER_NAME_KEY, ContainerName);
        } else {
            System.clearProperty(CONTAINER_NAME_KEY);
        }
        if (null != runEnvironment) {
            System.setProperty(RUN_ENVIRONMENT_KEY, runEnvironment);
        } else {
            System.clearProperty(RUN_ENVIRONMENT_KEY);
        }

        assertThat(channelServerHelper.getContainerName(), is(equalTo(expectedContainerName)));
    }

    @DataProvider(name = "DataProviderForContainerId")
    public Object[][] paramDataProviderForContainerId() {
        return new Object[][] {
            {"NoEnvironment", "anything", null, NON_PROD_CONTAINER_ID},
            {"NonProdEnvironment", "anything", NON_PROD, NON_PROD_CONTAINER_ID},
            {"ProdEnvironmentDataCentreSet", "cas1001", PROD, Short.parseShort("1001")},
            {"ProdEnvironmentDataCentreNull", null, PROD, null},
            {"ProdEnvironmentDataCentreNumberStringIndexOutOfBoundsException", "cas100", PROD, null},
        };
    }

    @Test(dataProvider = "DataProviderForContainerId")
    public void testGetContainerId(final String testCaseName, final String containerName,
        final String runEnvironment, final Short expectedContainerId) throws Exception {

        if (null != runEnvironment) {
            System.setProperty(RUN_ENVIRONMENT_KEY, runEnvironment);
        } else {
            System.clearProperty(RUN_ENVIRONMENT_KEY);
        }

        assertThat(channelServerHelper.getContainerId(containerName), is(equalTo(expectedContainerId)));
    }    
    
}
