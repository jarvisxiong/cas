package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Arrays;

import com.inmobi.adserve.channels.entity.GeoZipEntity;
import com.inmobi.adserve.channels.repository.GeoZipRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.phoenix.exception.RepositoryException;
import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import com.inmobi.adserve.adpool.AdCodeType;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.Carrier;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.Device;
import com.inmobi.adserve.adpool.DeviceType;
import com.inmobi.adserve.adpool.Geo;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.LatLong;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.adpool.ResponseFormat;
import com.inmobi.adserve.adpool.Site;
import com.inmobi.adserve.adpool.SupplyCapability;
import com.inmobi.adserve.adpool.User;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.types.ContentRating;
import com.inmobi.types.Gender;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;
import com.inmobi.types.SupplySource;


public class ThriftRequestParserTest extends TestCase {

    ThriftRequestParser thriftRequestParser;

    @Override
    public void setUp() {
        final Configuration mockConfig = createMock(Configuration.class);
        RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        GeoZipRepository mockGeoZipRepository = createMock(GeoZipRepository.class);
        GeoZipEntity mockGeoZipEntity = createMock(GeoZipEntity.class);

        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");

        replay(mockConfig, mockRepositoryHelper);
        CasConfigUtil.repositoryHelper = null;
        thriftRequestParser = new ThriftRequestParser();
    }

    @Test
    public void testParseRequestParameters() {

        final Site site = new Site();
        site.setContentRatingDeprecated(ContentRating.FAMILY_SAFE);
        site.setCpcFloor(1);
        site.setEcpmFloor(3.2);
        site.setInventoryType(InventoryType.APP);
        site.setSiteId("siteId");
        site.setSiteIncId(12345);
        site.setPublisherId("publisherId");
        site.setSiteUrl("siteUrl");

        final Device device = new Device();
        device.setDeviceTypeDeprecated(DeviceType.SMARTPHONE);
        device.setUserAgent("UserAgent");
        device.setOsId(123);
        device.setModelId(234);
        device.setHandsetInternalId(456);

        final Carrier carrier = new Carrier();
        carrier.setCarrierId(12345);

        final User user = new User();
        user.setYearOfBirth((short) 1930);
        user.setGender(Gender.MALE);

        final Geo geo = new Geo();
        final Set<Integer> cities = new HashSet<Integer>();
        cities.add(12);
        geo.setCityIds(cities);
        geo.setCountryCode("US");
        geo.setCountryId(94);
        geo.setLocationSource(LocationSource.LATLON);
        geo.setLatLong(new LatLong(12d, 12d));

        final Set<Integer> stateIds = new HashSet<Integer>();
        stateIds.add(123);
        geo.setStateIds(stateIds);

        final IntegrationDetails integrationDetails = new IntegrationDetails();
        integrationDetails.setAdCodeType(AdCodeType.BASIC);
        integrationDetails.setIFrameId("009");
        integrationDetails.setIntegrationType(IntegrationType.IOS_SDK);
        integrationDetails.setIntegrationVersion(231);

        final AdPoolRequest adPoolRequest = new AdPoolRequest();
        adPoolRequest.setSite(site);
        adPoolRequest.setDevice(device);
        adPoolRequest.setCarrier(carrier);
        adPoolRequest.setUser(user);
        adPoolRequest.setGeo(geo);
        adPoolRequest.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        final List<SupplyCapability> supplyCapabilities = new ArrayList<SupplyCapability>();
        supplyCapabilities.add(SupplyCapability.TEXT);
        adPoolRequest.setSupplyCapabilities(supplyCapabilities);
        adPoolRequest.setRemoteHostIp("10.14.118.143");
        adPoolRequest.setSegmentId(234);
        final List<Short> selectedSlots = new ArrayList<Short>();
        selectedSlots.add((short) 12);
        adPoolRequest.setSelectedSlots(selectedSlots);
        adPoolRequest.setRequestedAdCount((short) 1);
        adPoolRequest.setTaskId("tid");
        adPoolRequest.setResponseFormat(ResponseFormat.XHTML);
        adPoolRequest.setIntegrationDetails(integrationDetails);
        adPoolRequest.setIpFileVersion(3456);
        adPoolRequest.setSupplySource(SupplySource.RTB_EXCHANGE);
        adPoolRequest.setReferralUrl("refUrl");

        final SASRequestParameters sasRequestParameters = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);

        assertEquals(sasRequestParameters.getRemoteHostIp(), "10.14.118.143");
        assertEquals(sasRequestParameters.getUserAgent(), "UserAgent");
        assertEquals(sasRequestParameters.getSource(), "APP");
        assertEquals(sasRequestParameters.getAge(), new Short("84"));
        assertEquals(sasRequestParameters.getGender(), "M");
        assertEquals(sasRequestParameters.getLocSrc(), "LATLON");
        assertEquals(sasRequestParameters.getCountryCode(), "US");
        assertEquals(sasRequestParameters.getCountryId(), new Long(94));
        assertEquals(sasRequestParameters.getImpressionId(), null); // Internal, Populated in cas
        assertEquals(sasRequestParameters.getClurl(), null); // Internal, Populated in cas
        assertEquals(sasRequestParameters.getSiteId(), "siteId");
        assertEquals(sasRequestParameters.getSiteContentType(), ContentType.FAMILY_SAFE);
        assertEquals(sasRequestParameters.getSdkVersion(), "i231");
        assertEquals(sasRequestParameters.getSiteIncId(), 12345);
        assertEquals(sasRequestParameters.getAdIncId(), 0); // Internal, Populated in cas
        assertEquals(sasRequestParameters.getAdcode(), "NON-JS");
        assertEquals(sasRequestParameters.getCategories(), Collections.<Long>emptyList());
        assertEquals(sasRequestParameters.getSiteFloor(), 3.2);
        assertEquals(sasRequestParameters.getAllowBannerAds(), Boolean.FALSE);
        assertEquals(sasRequestParameters.getSiteSegmentId(), new Integer(234));
        assertEquals(sasRequestParameters.getUidParams(), null);
        assertEquals(sasRequestParameters.getTUidParams(), null);
        assertEquals(sasRequestParameters.getRqIframe(), "009");
        assertEquals(sasRequestParameters.getRFormat(), "xhtml");
        assertEquals(sasRequestParameters.getOsId(), 123);
        assertEquals(sasRequestParameters.getRqMkAdcount(), new Short("1"));
        assertEquals(sasRequestParameters.getTid(), "tid");
        assertEquals(sasRequestParameters.getHandsetInternalId(), 456);
        assertEquals(sasRequestParameters.getCarrierId(), 12345);
        assertEquals(sasRequestParameters.getCity(), new Integer(12));
        assertEquals(sasRequestParameters.getState(), new Integer(123));
        assertEquals(sasRequestParameters.getProcessedMkSlot().get(0), new Short("12"));
        assertEquals(sasRequestParameters.getIpFileVersion(), new Integer(3456));
        assertEquals(sasRequestParameters.isRichMedia(), false);
        assertEquals(sasRequestParameters.getRqAdType(), "int");
        assertEquals(sasRequestParameters.getImaiBaseUrl(), null); // Internal, Populated in cas
        assertEquals(sasRequestParameters.getAppUrl(), "siteUrl");
        assertEquals(sasRequestParameters.getModelId(), 234);
        assertEquals(sasRequestParameters.getDst(), 6);
        assertEquals(sasRequestParameters.getAccountSegment(), Collections.<Integer>emptySet());
        assertEquals(sasRequestParameters.isResponseOnlyFromDcp(), false);
        assertEquals(sasRequestParameters.getSst(), 100);
        assertEquals(sasRequestParameters.getReferralUrl(), "refUrl");
    }

    @Test
    public void testSlotListFormationForIx() {
        final Site site = new Site();
        site.setContentRatingDeprecated(ContentRating.FAMILY_SAFE);
        site.setCpcFloor(1);
        site.setEcpmFloor(3.2);
        site.setInventoryType(InventoryType.APP);
        site.setSiteId("siteId");
        site.setSiteIncId(12345);
        site.setPublisherId("publisherId");
        site.setSiteUrl("siteUrl");

        final Device device = new Device();
        device.setDeviceTypeDeprecated(DeviceType.SMARTPHONE);
        device.setUserAgent("UserAgent");
        device.setOsId(123);
        device.setModelId(234);
        device.setHandsetInternalId(456);

        final Carrier carrier = new Carrier();
        carrier.setCarrierId(12345);

        final User user = new User();
        user.setYearOfBirth((short) 1930);
        user.setGender(Gender.MALE);

        final Geo geo = new Geo();
        final Set<Integer> cities = new HashSet<Integer>();
        cities.add(12);
        geo.setCityIds(cities);
        geo.setCountryCode("US");
        geo.setCountryId(94);
        geo.setLocationSource(LocationSource.LATLON);
        geo.setLatLong(new LatLong(12d, 12d));

        final Set<Integer> stateIds = new HashSet<Integer>();
        stateIds.add(123);
        geo.setStateIds(stateIds);

        final IntegrationDetails integrationDetails = new IntegrationDetails();
        integrationDetails.setAdCodeType(AdCodeType.BASIC);
        integrationDetails.setIFrameId("009");
        integrationDetails.setIntegrationType(IntegrationType.IOS_SDK);
        integrationDetails.setIntegrationVersion(231);

        final AdPoolRequest adPoolRequest = new AdPoolRequest();
        adPoolRequest.setSite(site);
        adPoolRequest.setDevice(device);
        adPoolRequest.setCarrier(carrier);
        adPoolRequest.setUser(user);
        adPoolRequest.setGeo(geo);
        adPoolRequest.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        final List<SupplyCapability> supplyCapabilities = new ArrayList<SupplyCapability>();
        supplyCapabilities.add(SupplyCapability.TEXT);
        adPoolRequest.setSupplyCapabilities(supplyCapabilities);
        adPoolRequest.setRemoteHostIp("10.14.118.143");
        adPoolRequest.setSegmentId(234);
        adPoolRequest.setRequestedAdCount((short) 1);
        adPoolRequest.setTaskId("tid");
        adPoolRequest.setResponseFormat(ResponseFormat.XHTML);
        adPoolRequest.setIntegrationDetails(integrationDetails);
        adPoolRequest.setIpFileVersion(3456);
        adPoolRequest.setSupplySource(SupplySource.RTB_EXCHANGE);
        adPoolRequest.setReferralUrl("refUrl");

        final SASRequestParameters sasRequestParameters = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        //List in sasParams should contain all slots sent by UMP if no. of slots <5
        final List<Short> selectedSlots = new ArrayList<Short>();
        selectedSlots.addAll(Arrays.asList((short) 4, (short) 9, (short) 10, (short) 11));
        adPoolRequest.setSelectedSlots(selectedSlots);

        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 8);

        assertEquals(sasRequestParameters.getDst(), 8);
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 4, (short) 9, (short) 10, (short) 11));
        selectedSlots.clear();

        //List in sasParams should contain only 5 slots, even if more than 5 slots are sent by UMP
        selectedSlots.addAll(Arrays.asList((short) 4, (short) 9, (short) 10, (short) 11, (short) 12, (short) 13));
        adPoolRequest.setSelectedSlots(selectedSlots);

        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 8);

        assertEquals(sasRequestParameters.getDst(), 8);
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 4, (short) 9, (short) 10, (short) 11, (short) 12));
        selectedSlots.clear();

        //List in sasParams should contain only the slots present in SLOT_MAP
        selectedSlots.addAll(Arrays.asList((short) 5, (short) 6, (short) 9, (short) 10, (short) 4, (short) 11));
        adPoolRequest.setSelectedSlots(selectedSlots);

        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 8);

        assertEquals(sasRequestParameters.getDst(), 8);
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 9, (short) 10, (short) 4, (short) 11));
        selectedSlots.clear();

        //List in sasParams should contain only the slots present in SLOT_MAP
        selectedSlots.addAll(Arrays.asList((short) 5, (short) 6, (short) 9, (short) 10, (short) 4, (short) 12, (short) 7, (short) 11, (short) 14, (short) 13));
        adPoolRequest.setSelectedSlots(selectedSlots);

        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 8);

        assertEquals(sasRequestParameters.getDst(), 8);
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 9, (short) 10, (short) 4, (short) 12, (short) 11));
        selectedSlots.clear();
    }

    @Test
    public void testSlotListFormationForDcpOrRtbd() {
        final Site site = new Site();
        site.setContentRatingDeprecated(ContentRating.FAMILY_SAFE);
        site.setCpcFloor(1);
        site.setEcpmFloor(3.2);
        site.setInventoryType(InventoryType.APP);
        site.setSiteId("siteId");
        site.setSiteIncId(12345);
        site.setPublisherId("publisherId");
        site.setSiteUrl("siteUrl");

        final Device device = new Device();
        device.setDeviceTypeDeprecated(DeviceType.SMARTPHONE);
        device.setUserAgent("UserAgent");
        device.setOsId(123);
        device.setModelId(234);
        device.setHandsetInternalId(456);

        final Carrier carrier = new Carrier();
        carrier.setCarrierId(12345);

        final User user = new User();
        user.setYearOfBirth((short) 1930);
        user.setGender(Gender.MALE);

        final Geo geo = new Geo();
        final Set<Integer> cities = new HashSet<Integer>();
        cities.add(12);
        geo.setCityIds(cities);
        geo.setCountryCode("US");
        geo.setCountryId(94);
        geo.setLocationSource(LocationSource.LATLON);
        geo.setLatLong(new LatLong(12d, 12d));

        final Set<Integer> stateIds = new HashSet<Integer>();
        stateIds.add(123);
        geo.setStateIds(stateIds);

        final IntegrationDetails integrationDetails = new IntegrationDetails();
        integrationDetails.setAdCodeType(AdCodeType.BASIC);
        integrationDetails.setIFrameId("009");
        integrationDetails.setIntegrationType(IntegrationType.IOS_SDK);
        integrationDetails.setIntegrationVersion(231);

        final AdPoolRequest adPoolRequest = new AdPoolRequest();
        adPoolRequest.setSite(site);
        adPoolRequest.setDevice(device);
        adPoolRequest.setCarrier(carrier);
        adPoolRequest.setUser(user);
        adPoolRequest.setGeo(geo);
        adPoolRequest.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        final List<SupplyCapability> supplyCapabilities = new ArrayList<SupplyCapability>();
        supplyCapabilities.add(SupplyCapability.TEXT);
        adPoolRequest.setSupplyCapabilities(supplyCapabilities);
        adPoolRequest.setRemoteHostIp("10.14.118.143");
        adPoolRequest.setSegmentId(234);
        adPoolRequest.setRequestedAdCount((short) 1);
        adPoolRequest.setTaskId("tid");
        adPoolRequest.setResponseFormat(ResponseFormat.XHTML);
        adPoolRequest.setIntegrationDetails(integrationDetails);
        adPoolRequest.setIpFileVersion(3456);
        adPoolRequest.setSupplySource(SupplySource.RTB_EXCHANGE);
        adPoolRequest.setReferralUrl("refUrl");

        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        //List in sasParams should contain all slots sent by UMP if no. of slots <5
        final List<Short> selectedSlots = new ArrayList<Short>();
        selectedSlots.addAll(Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4));
        adPoolRequest.setSelectedSlots(selectedSlots);
        final SASRequestParameters sasRequestParameters = new SASRequestParameters();

        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);

        assertEquals(sasRequestParameters.getDst(), 6);
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4));
        selectedSlots.clear();

        //List in sasParams should contain only 5 slots, even if more than 5 slots are sent by UMP
        selectedSlots.addAll(Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4, (short) 9, (short) 10));
        adPoolRequest.setSelectedSlots(selectedSlots);

        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);

        assertEquals(sasRequestParameters.getDst(), 6);
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4, (short) 9));
        selectedSlots.clear();

        //List in sasParams should contain only the slots present in SLOT_MAP
        selectedSlots.addAll(Arrays.asList((short) 5, (short) 6, (short) 3, (short) 4, (short) 1, (short) 10));
        adPoolRequest.setSelectedSlots(selectedSlots);

        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);

        assertEquals(sasRequestParameters.getDst(), 6);
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 3, (short) 4, (short) 1, (short) 10));
        selectedSlots.clear();

        //List in sasParams should contain only the slots present in SLOT_MAP
        selectedSlots.addAll(Arrays.asList((short) 5, (short) 6, (short) 3, (short) 4, (short) 1, (short) 10, (short) 7, (short) 9, (short) 11, (short) 12));
        adPoolRequest.setSelectedSlots(selectedSlots);

        thriftRequestParser
                .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);

        assertEquals(sasRequestParameters.getDst(), 6);
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 3, (short) 4, (short) 1, (short) 10, (short) 9));
        selectedSlots.clear();


    }
}
