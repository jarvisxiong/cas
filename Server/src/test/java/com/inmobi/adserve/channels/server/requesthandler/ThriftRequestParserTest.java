package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.util.DebugLogger;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

public class ThriftRequestParserTest extends TestCase {
   
    public void setUp()
    {
        Configuration mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
        DebugLogger.init(mockConfig);
    }

    /*@Test
    public void testParseRequestParameters() 
    {
        AdPoolRequest adPoolRequest = new AdPoolRequest();
        Site site = new Site();
        site.setContentRating(ContentRating.FAMILY_SAFE);
        site.setCpcFloor(1);
        site.setEcpmFloor(3.2);
        site.setInventoryType(InventoryType.APP);
        site.setSiteId("siteId");
        site.setSiteIncId(12345);
        site.setPublisherId("publisherId");
        site.setSiteUrl("siteUrl");
        adPoolRequest.setSite(site);
        SASRequestParameters sasRequestParameters = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        DebugLogger debugLogger = new DebugLogger();
        ThriftRequestParser.parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, debugLogger, 6);
        assertEquals(sasRequestParameters.getSiteIncId(), 12345);
    }
    */
}
