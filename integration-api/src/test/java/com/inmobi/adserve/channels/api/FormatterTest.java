package com.inmobi.adserve.channels.api;

import static org.easymock.EasyMock.expect;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.apache.commons.configuration.Configuration;
import org.apache.velocity.VelocityContext;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class FormatterTest {

    private Configuration mockConfig;

    @BeforeTest
    public void setUp() throws Exception {
        mockConfig = EasyMock.createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        EasyMock.replay(mockConfig);
        Formatter.init();
    }

    @Test
    public void testInit() {
        try {
            Formatter.init();
        } catch (final Exception e) {
            fail();
        }
    }

    @Test
    public void testrequestFromSDK360Onwards() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams));
        sasParams.setSdkVersion("i359");
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams));
        sasParams.setSdkVersion("a350");
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams));
        sasParams.setSdkVersion("i360");
        assertEquals(true, Formatter.requestFromSDK360Onwards(sasParams));
        sasParams.setSdkVersion("a360");
        assertEquals(true, Formatter.requestFromSDK360Onwards(sasParams));
        sasParams.setSdkVersion("i");
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams));
        sasParams.setSdkVersion("aaaa");
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams));
    }

    @Test
    public void testupdateVelocityContextWithNoBeaconUrlAndWapRequest() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSource("wap");
        final VelocityContext context = new VelocityContext();
        Formatter.updateVelocityContext(context, sasParams, null);
        assertNull(context.get(VelocityTemplateFieldConstants.IM_BEACON_URL));
        assertNull(context.get(VelocityTemplateFieldConstants.SDK));
        assertNull(context.get(VelocityTemplateFieldConstants.SDK360_ONWARDS));
        assertNull(context.get(VelocityTemplateFieldConstants.IMAI_BASE_URL));
    }

    @Test
    public void testupdateVelocityContextWithBeaconUrlAndAppRequestNonIMAI() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSource("APP");
        sasParams.setSdkVersion("i360");
        final VelocityContext context = new VelocityContext();
        Formatter.updateVelocityContext(context, sasParams, "beacon");
        assertEquals(context.get(VelocityTemplateFieldConstants.IM_BEACON_URL), "beacon");
        assertEquals(context.get(VelocityTemplateFieldConstants.SDK), true);
        assertEquals(context.get(VelocityTemplateFieldConstants.SDK360_ONWARDS), true);
        assertNull(context.get(VelocityTemplateFieldConstants.IMAI_BASE_URL));
    }

    @Test
    public void testupdateVelocityContextWithBeaconUrlAndAppRequestIMAI() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSource("APP");
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("imai");
        final VelocityContext context = new VelocityContext();
        Formatter.updateVelocityContext(context, sasParams, "beacon");
        assertEquals(context.get(VelocityTemplateFieldConstants.IMAI_BASE_URL), "imai");
    }

    @DataProvider(name = "slot")
    public Object[][] slot() {
        return new Object[][] { {"1"}, {"1.0"}, {"2"}, {"2.0"}, {"3"}, {"3.0"}, {"4"}, {"4.0"}, {"9"}, {"9.0"}, {"11"},
                {"11.0"}, {"12"}, {"12.0"}, {"15"}, {"15.0"}, {"5.0"}, {"10.1"}};
    }

    @Test(dataProvider = "slot")
    public void testGetRichTextTemplateForSlot(final String slot) {
        final String template = Formatter.getRichTextTemplateForSlot(slot);
        Double slotType = Double.valueOf(slot);
        if (!slot.equals(slotType.toString()) && slot.equals(slotType.toString() + ".0")) {
            slotType = 0.0;
        }
        switch (slotType.intValue()) {
            case 1:
                assertEquals(template, "template_120_20");
                break;
            case 2:
                assertEquals(template, "template_168_28");
                break;
            case 3:
                assertEquals(template, "template_216_36");
                break;
            case 4:
                assertEquals(template, "template_300_50");
                break;
            case 9:
                assertEquals(template, "template_320_48");
                break;
            case 11:
                assertEquals(template, "template_728_90");
                break;
            case 12:
                assertEquals(template, "template_468_60");
                break;
            case 15:
                assertEquals(template, "template_320_50");
                break;
            default:
                assertNull(template);
                break;
        }
    }

    @Test
    public void testgetNamespace() {
        final String namespace = Formatter.getNamespace();

        // Verify namespace
        assertTrue(namespace.matches("im_1\\d{4}_"));
    }
}
