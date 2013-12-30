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

import java.util.*;

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
        Set<Integer> cities = new HashSet<Integer>();
        cities.add(12);
        geo.setCityIds(cities);
        geo.setCountryCode("US");
        geo.setCountryId(94);
        geo.setLocationSource(LocationSource.LATLON);
        geo.setLatLong(new LatLong(12d, 12d));
        Set<Integer> zipIds = new HashSet<Integer>();
        zipIds.add(123);
        geo.setZipIds(zipIds);
        Set<Integer> stateIds = new HashSet<Integer>();
        stateIds.add(123);
        geo.setStateIds(stateIds);
        
        IntegrationDetails integrationDetails = new IntegrationDetails();
        integrationDetails.setAdCodeType(AdCodeType.BASIC);
        integrationDetails.setIFrameId("009");

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
        List<Short> selectedSlots = new ArrayList<Short>();
        selectedSlots.add((short) 12);
        adPoolRequest.setSelectedSlots(selectedSlots);
        adPoolRequest.setRequestedAdCount((short) 1);
        adPoolRequest.setRequestId("tid");
        adPoolRequest.setResponseFormat("html");
        adPoolRequest.setIntegrationDetails(integrationDetails);
        adPoolRequest.setIpFileVersion(3456);

        SASRequestParameters sasRequestParameters = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        DebugLogger debugLogger = new DebugLogger();
        ThriftRequestParser.parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, debugLogger, 6);

        assertEquals(sasRequestParameters.getRemoteHostIp(), "10.14.118.143");
        assertEquals(sasRequestParameters.getUserAgent(), "UserAgent");
        assertEquals(sasRequestParameters.getSource(), "FAMILY_SAFE");
        assertEquals(sasRequestParameters.getAge(), new Short("83"));
        assertEquals(sasRequestParameters.getGender(), "M");
        assertEquals(sasRequestParameters.getLocSrc(), "LATLON");
        assertEquals(sasRequestParameters.getPostalCode(), new Integer(123)); 
        assertEquals(sasRequestParameters.getCountryCode(), "US");
        assertEquals(sasRequestParameters.getCountryId(), new Long(94));
        assertEquals(sasRequestParameters.getImpressionId(), null); //Internal, Populated in cas
        assertEquals(sasRequestParameters.getClurl(), null); //Internal, Populated in cas
        assertEquals(sasRequestParameters.getSiteId(), "siteId");
        assertEquals(sasRequestParameters.getSlot(), new Short("12"));
        assertEquals(sasRequestParameters.getSiteType(), "APP");
        assertEquals(sasRequestParameters.getSdkVersion(), null); 
        assertEquals(sasRequestParameters.getSiteIncId(), 12345);
        assertEquals(sasRequestParameters.getAdIncId(), 0);  //Internal, Populated in cas
        assertEquals(sasRequestParameters.getAdcode(), AdCodeType.BASIC.toString()); 
        assertEquals(sasRequestParameters.getCategories(), Collections.<Long>emptyList());
        assertEquals(sasRequestParameters.getSiteFloor(), 3.2);
        assertEquals(sasRequestParameters.getAllowBannerAds(), Boolean.FALSE);
        assertEquals(sasRequestParameters.getSiteSegmentId(), new Integer(234));
        assertEquals(sasRequestParameters.getUidParams(), null);
        assertEquals(sasRequestParameters.getTUidParams(), null);
        assertEquals(sasRequestParameters.getRqIframe(), "009"); 
        assertEquals(sasRequestParameters.getRFormat(), "html");
        assertEquals(sasRequestParameters.getOsId(), 123);
        assertEquals(sasRequestParameters.getRqMkAdcount(), new Short("1"));
        assertEquals(sasRequestParameters.getTid(), "tid");
        assertEquals(sasRequestParameters.getHandsetInternalId(), 456);
        assertEquals(sasRequestParameters.getCarrierId(), 12345);
        assertEquals(sasRequestParameters.getCity(), new Integer(12));
        assertEquals(sasRequestParameters.getState(), new Integer(123)); 
        assertEquals(sasRequestParameters.getRqMkSlot().get(0), new Short("12"));
        assertEquals(sasRequestParameters.getIpFileVersion(), new Integer(3456)); 
        assertEquals(sasRequestParameters.isRichMedia(), false);
        assertEquals(sasRequestParameters.getRqAdType(), "INTERSTITIAL");
        assertEquals(sasRequestParameters.getImaiBaseUrl(), null);  //Internal, Populated in cas
        assertEquals(sasRequestParameters.getAppUrl(), "siteUrl");
        assertEquals(sasRequestParameters.getModelId(), 234);
        assertEquals(sasRequestParameters.getDst(), 6);
        assertEquals(sasRequestParameters.getAccountSegment(), Collections.<Integer>emptySet());
        assertEquals(sasRequestParameters.isResponseOnlyFromDcp(), false);
        assertEquals(sasRequestParameters.getSst(), 0);
    }
}
