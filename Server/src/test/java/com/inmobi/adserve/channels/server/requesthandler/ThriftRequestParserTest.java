package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.adpool.*;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.types.ContentRating;
import com.inmobi.types.Gender;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;

import java.util.Collections;

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
    
    public void testParseRequestParameters() 
    {
        Site site = new Site();
        site.setContentRating(ContentRating.FAMILY_SAFE);
        site.setCpcFloor(1);
        site.setEcpmFloor(3.2);
        site.setInventoryType(InventoryType.APP);
        site.setSiteId("siteId");
        site.setSiteIncId(12345);
        site.setPublisherId("publisherId");
        site.setSiteUrl("siteUrl");

        Device device = new Device();
        device.setDeviceType(DeviceType.SMARTPHONE);
        device.setUserAgent("UserAgent");
        device.setOsId(123);
        device.setModelId(234);
        device.setHandsetInternalId(456);
        
        Carrier carrier = new Carrier();
        carrier.setCarrierId(12345);
        
        User user = new User();
        user.setYearOfBirth((short)1930);
        user.setGender(Gender.MALE);

        Geo geo = new Geo();
        geo.setCityId(12);
        geo.setCountryCode("US");
        geo.setCountryId(94);
        geo.setLocationSource(LocationSource.LATLON);
        geo.setLatLong(new LatLong(12d, 12d));

        AdPoolRequest adPoolRequest = new AdPoolRequest();
        adPoolRequest.setSite(site);
        adPoolRequest.setDevice(device);
        adPoolRequest.setCarrier(carrier);
        adPoolRequest.setUser(user);
        adPoolRequest.setGeo(geo);
        adPoolRequest.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        adPoolRequest.setSupplyCapability(SupplyCapability.TEXT);
        adPoolRequest.setRemoteHostIp("10.14.118.143");
        adPoolRequest.setSegmentId(234);
        adPoolRequest.setRequestSlotId((short)12);
        adPoolRequest.setRequestedAdCount((short)1);
        adPoolRequest.setSdkVersion("i362");
        adPoolRequest.setRequestId("tid");
        adPoolRequest.setResponseFormat("html");

        SASRequestParameters sasRequestParameters = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        DebugLogger debugLogger = new DebugLogger();
        ThriftRequestParser.parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, debugLogger, 6);

        assertEquals(sasRequestParameters.getRemoteHostIp(), "10.14.118.143");
        assertEquals(sasRequestParameters.getUserAgent(), "UserAgent");
        assertEquals(sasRequestParameters.getSource(), "FAMILY_SAFE");
        assertEquals(sasRequestParameters.getAge(), "83");
        assertEquals(sasRequestParameters.getGender(), "M");
        assertEquals(sasRequestParameters.getLocSrc(), "LATLON");
        assertEquals(sasRequestParameters.getPostalCode(), null); //Not coming in dcp
        assertEquals(sasRequestParameters.getCountry(), "US");
        assertEquals(sasRequestParameters.getCountryStr(), "94");
        assertEquals(sasRequestParameters.getImpressionId(), null); //Internal, Populated in cas
        assertEquals(sasRequestParameters.getClurl(), null); //Internal, Populated in cas
        assertEquals(sasRequestParameters.getSiteId(), "siteId");
        assertEquals(sasRequestParameters.getSlot(), "12");
        assertEquals(sasRequestParameters.getSiteType(), "APP");
        assertEquals(sasRequestParameters.getSdkVersion(), "i362"); 
        assertEquals(sasRequestParameters.getSiteIncId(), 12345);
        assertEquals(sasRequestParameters.getAdIncId(), 0);  //Internal, Populated in cas
        assertEquals(sasRequestParameters.getAdcode(), null); //Not coming in dcp
        assertEquals(sasRequestParameters.getCategories(), Collections.emptyList());
        assertEquals(sasRequestParameters.getSiteFloor(), 3.2);
        assertEquals(sasRequestParameters.getAllowBannerAds(), Boolean.FALSE);
        assertEquals(sasRequestParameters.getSiteSegmentId(), new Integer(234));
        assertEquals(sasRequestParameters.getUidParams(), null);
        assertEquals(sasRequestParameters.getTUidParams(), null);
        assertEquals(sasRequestParameters.getRqIframe(), null); //Not coming in dcp
        assertEquals(sasRequestParameters.getRFormat(), "html");
        assertEquals(sasRequestParameters.getOsId(), 123);
        assertEquals(sasRequestParameters.getRqMkAdcount(), "1");
        assertEquals(sasRequestParameters.getTid(), "tid");
        assertEquals(sasRequestParameters.getHandsetInternalId(), 456);
        assertEquals(sasRequestParameters.getCarrierId(), 12345);
        assertEquals(sasRequestParameters.getCity(), "12");
        assertEquals(sasRequestParameters.getArea(), null); //Not coming in dcp
        assertEquals(sasRequestParameters.getRqMkSlot(), "12");
        assertEquals(sasRequestParameters.getIpFileVersion(), null); //Not coming in dcp
        assertEquals(sasRequestParameters.isRichMedia(), false);
        assertEquals(sasRequestParameters.getRqAdType(), "INTERSTITIAL");
        assertEquals(sasRequestParameters.getImaiBaseUrl(), null);  //Internal, Populated in cas
        assertEquals(sasRequestParameters.getAppUrl(), "siteUrl");
        assertEquals(sasRequestParameters.getModelId(), 234);
        assertEquals(sasRequestParameters.getDst(), 6);
        assertEquals(sasRequestParameters.getAccountSegment(), Collections.emptySet());
        assertEquals(sasRequestParameters.isResponseOnlyFromDcp(), false);
    }
}
