package com.inmobi.adserve.channels.api;

import static org.easymock.EasyMock.expect;

import org.apache.commons.configuration.Configuration;
import org.apache.velocity.VelocityContext;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;

import static org.testng.Assert.*;


public class FormatterTest
{

    private Configuration mockConfig;
    private DebugLogger   logger;

    @BeforeTest
    public void setUp() throws Exception
    {
        mockConfig = EasyMock.createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        EasyMock.replay(mockConfig);
        DebugLogger.init(mockConfig);
        Formatter.init();
        logger = new DebugLogger();
    }

    @Test
    public void testInit()
    {
        try {
            Formatter.init();
        }
        catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testrequestFromSDK360Onwards()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams, logger));
        sasParams.setSdkVersion("i350");
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams, logger));
        sasParams.setSdkVersion("a350");
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams, logger));
        sasParams.setSdkVersion("i360");
        assertEquals(true, Formatter.requestFromSDK360Onwards(sasParams, logger));
        sasParams.setSdkVersion("a360");
        assertEquals(true, Formatter.requestFromSDK360Onwards(sasParams, logger));
        sasParams.setSdkVersion("i");
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams, logger));
        sasParams.setSdkVersion("aaaa");
        assertEquals(false, Formatter.requestFromSDK360Onwards(sasParams, logger));
    }

    @Test
    public void testupdateVelocityContextWithNoBeaconUrlAndWapRequest()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSource("wap");
        VelocityContext context = new VelocityContext();
        Formatter.updateVelocityContext(context, sasParams, null, logger);
        assertNull(context.get(VelocityTemplateFieldConstants.IMBeaconUrl));
        assertNull(context.get(VelocityTemplateFieldConstants.APP));
        assertNull(context.get(VelocityTemplateFieldConstants.SDK360Onwards));
        assertNull(context.get(VelocityTemplateFieldConstants.IMAIBaseUrl));
    }

    @Test
    public void testupdateVelocityContextWithBeaconUrlAndAppRequestNonIMAI()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSource("APP");
        sasParams.setSdkVersion("i360");
        VelocityContext context = new VelocityContext();
        Formatter.updateVelocityContext(context, sasParams, "beacon", logger);
        assertEquals(context.get(VelocityTemplateFieldConstants.IMBeaconUrl), "beacon");
        assertEquals(context.get(VelocityTemplateFieldConstants.APP), true);
        assertEquals(context.get(VelocityTemplateFieldConstants.SDK360Onwards), true);
        assertNull(context.get(VelocityTemplateFieldConstants.IMAIBaseUrl));
    }

    @Test
    public void testupdateVelocityContextWithBeaconUrlAndAppRequestIMAI()
    {
        SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSource("APP");
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("imai");
        VelocityContext context = new VelocityContext();
        Formatter.updateVelocityContext(context, sasParams, "beacon", logger);
        assertEquals(context.get(VelocityTemplateFieldConstants.IMAIBaseUrl), "imai");
    }

    @DataProvider(name = "slot")
    public Object[][] slot()
    {
        return new Object[][]
        {
        { "1" },
        { "1.0" },
        { "2" },
        { "2.0" },
        { "3" },
        { "3.0" },
        { "4" },
        { "4.0" },
        { "9" },
        { "9.0" },
        { "11" },
        { "11.0" },
        { "12" },
        { "12.0" },
        { "15" },
        { "15.0" },
        { "5.0" },
        { "10.1" } };
    }

    @Test(dataProvider = "slot")
    public void testGetRichTextTemplateForSlot(String slot)
    {
        String template = Formatter.getRichTextTemplateForSlot(slot);
        Double slotType = Double.valueOf(slot);
        if (!slot.equals(slotType.toString()) && slot.equals(slotType.toString() + ".0")) {
            slotType = 0.0;
        }
        switch (slotType.intValue())
        {
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
}
