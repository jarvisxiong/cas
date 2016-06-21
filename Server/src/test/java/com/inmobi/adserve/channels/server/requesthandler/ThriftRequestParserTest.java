package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.awt.Dimension;
import java.util.*;

import com.inmobi.adserve.adpool.*;
import com.inmobi.adserve.adpool.ResponseFormat;
import com.inmobi.user.photon.datatypes.attribute.core.CoreAttributes;
import com.inmobi.user.photon.datatypes.commons.attribute.IntAttribute;
import com.inmobi.user.photon.datatypes.commons.attribute.ValueProperties;
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.types.ContentRating;
import com.inmobi.types.Gender;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;
import com.inmobi.types.SupplySource;

import junit.framework.TestCase;


public class ThriftRequestParserTest extends TestCase {

    ThriftRequestParser thriftRequestParser;

    @Override
    public void setUp() {
        final Configuration mockConfig = createMock(Configuration.class);
        RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        // GeoZipRepository mockGeoZipRepository = createMock(GeoZipRepository.class);
        //GeoZipEntity mockGeoZipEntity = createMock(GeoZipEntity.class);

        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");

        replay(mockConfig, mockRepositoryHelper);
        final WapSiteUACEntity wapSiteUACEntity = EasyMock.createMock(WapSiteUACEntity.class);
        final SiteEcpmEntity siteEcpmEntity = EasyMock.createMock(SiteEcpmEntity.class);
        final SlotSizeMapEntity slotSizeMapEntityFor1 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor1.getDimension()).andReturn(new Dimension(120, 20)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor1);
        final SlotSizeMapEntity slotSizeMapEntityFor2 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor2.getDimension()).andReturn(new Dimension(168, 28)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor2);
        final SlotSizeMapEntity slotSizeMapEntityFor3 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor3.getDimension()).andReturn(new Dimension(216, 36)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor3);
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor12 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor12.getDimension()).andReturn(new Dimension(468, 60)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor12);
        final SlotSizeMapEntity slotSizeMapEntityFor13 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor13.getDimension()).andReturn(new Dimension(120, 600)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor13);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        RepositoryHelper repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.queryWapSiteUACRepository(EasyMock.isA(String.class)))
                .andReturn(wapSiteUACEntity).anyTimes();
        EasyMock.expect(repositoryHelper.querySiteEcpmRepository(EasyMock.isA(String.class), EasyMock.isA(Integer.class), EasyMock.isA(Integer.class)))
                .andReturn(siteEcpmEntity).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)1))
                .andReturn(slotSizeMapEntityFor1).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)2))
                .andReturn(slotSizeMapEntityFor2).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)3))
                .andReturn(slotSizeMapEntityFor3).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)5))
                .andReturn(null).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)6))
                .andReturn(null).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)7))
                .andReturn(null).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)10))
                .andReturn(slotSizeMapEntityFor10).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)12))
                .andReturn(slotSizeMapEntityFor12).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)13))
                .andReturn(slotSizeMapEntityFor13).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);
        CasConfigUtil.repositoryHelper = repositoryHelper;
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
        user.setYearOfBirth((short)(Calendar.getInstance().get(Calendar.YEAR)-85));
        user.setGender(Gender.MALE);

        final Geo geo = new Geo();
        final Set<Integer> cities = new HashSet<Integer>();
        cities.add(12);
        geo.setCityIds(cities);
        geo.setCountryCode("US");
        geo.setCountryId(94);
        geo.setLocationSource(LocationSource.LATLON);
        geo.setLatLong(new LatLong(12d, 12d));

        final Set<Integer> stateIds = new HashSet<>();
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
        final List<SupplyContentType> supplyAllowedContents = new ArrayList<>();
        supplyAllowedContents.add(SupplyContentType.TEXT);
        adPoolRequest.setSupplyAllowedContents(supplyAllowedContents);
        adPoolRequest.setRemoteHostIp("10.14.118.143");
        adPoolRequest.setSiteSegmentId(234);
        final List<Short> selectedSlots = new ArrayList<Short>();
        selectedSlots.add((short) 12);
        adPoolRequest.setSelectedSlots(selectedSlots);
        adPoolRequest.setRequestedAdCount((short) 1);
        adPoolRequest.setTaskId("tid");
        adPoolRequest.setResponseFormatDeprecated(ResponseFormat.XHTML);
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
        assertEquals(sasRequestParameters.getAge(), new Short("85"));
        assertEquals(sasRequestParameters.getGender(), "M");
        assertEquals(sasRequestParameters.getLocationSource(), LocationSource.LATLON);
        assertEquals(sasRequestParameters.getCountryCode(), "US");
        assertEquals(sasRequestParameters.getCountryId(), new Long(94));
        assertEquals(sasRequestParameters.getImpressionId(), null); // Internal, Populated in cas
        assertEquals(sasRequestParameters.getSiteId(), "siteId");
        assertEquals(sasRequestParameters.getSiteContentType(), ContentType.FAMILY_SAFE);
        assertEquals(sasRequestParameters.getSiteIncId(), 12345);
        assertEquals(sasRequestParameters.getAdIncId(), 0); // Internal, Populated in cas
        assertEquals(sasRequestParameters.getAdcode(), "NON-JS");
        assertEquals(sasRequestParameters.getCategories(), Collections.emptyList());
        assertEquals(sasRequestParameters.getSiteFloor(), 3.2);
        assertEquals(sasRequestParameters.getAllowBannerAds(), Boolean.FALSE);
        assertEquals(sasRequestParameters.getSiteSegmentId(), new Integer(234));
        assertEquals(sasRequestParameters.getTUidParams(), null);
        assertEquals(sasRequestParameters.getRqIframe(), "009");
        assertEquals(sasRequestParameters.getRFormat(), "xhtml");
        assertEquals(sasRequestParameters.getOsId(), 123);
        assertEquals(sasRequestParameters.getRqMkAdcount(), new Short("1"));
        assertEquals(sasRequestParameters.getTid(), "tid");
        assertEquals(sasRequestParameters.getHandsetInternalId(), 456);
        assertEquals(sasRequestParameters.getCarrierId(), new Integer(12345));
        assertEquals(sasRequestParameters.getCity(), new Integer(12));
        assertEquals(sasRequestParameters.getState(), new Integer(123));
        assertEquals(sasRequestParameters.getProcessedMkSlot().get(0), new Short("12"));
        assertEquals(sasRequestParameters.getIpFileVersion(), new Integer(3456));
        assertEquals(sasRequestParameters.isRichMedia(), false);
        assertEquals(sasRequestParameters.getRequestedAdType(), RequestedAdType.INTERSTITIAL);
        assertEquals(sasRequestParameters.getImaiBaseUrl(), null); // Internal, Populated in cas
        assertEquals(sasRequestParameters.getAppUrl(), "siteUrl");
        assertEquals(sasRequestParameters.getModelId(), 234);
        assertEquals(sasRequestParameters.getDst(), 6);
        assertEquals(sasRequestParameters.getAccountSegment(), Collections.<Integer>emptySet());
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
        final List<SupplyContentType> supplyAllowedContents = new ArrayList<>();
        supplyAllowedContents.add(SupplyContentType.TEXT);
        adPoolRequest.setSupplyAllowedContents(supplyAllowedContents);
        adPoolRequest.setRemoteHostIp("10.14.118.143");
        adPoolRequest.setSiteSegmentId(234);
        adPoolRequest.setRequestedAdCount((short) 1);
        adPoolRequest.setTaskId("tid");
        adPoolRequest.setResponseFormatDeprecated(ResponseFormat.XHTML);
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
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 4, (short) 9, (short) 10, (short) 11, (short) 12, (short) 13));
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
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList( (short) 9, (short) 10, (short) 4, (short) 12, (short) 11, (short) 14, (short) 13) );
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
        final List<SupplyContentType> supplyAllowedContents = new ArrayList<>();
        supplyAllowedContents.add(SupplyContentType.TEXT);
        adPoolRequest.setSupplyAllowedContents(supplyAllowedContents);
        adPoolRequest.setRemoteHostIp("10.14.118.143");
        adPoolRequest.setSiteSegmentId(234);
        adPoolRequest.setRequestedAdCount((short) 1);
        adPoolRequest.setTaskId("tid");
        adPoolRequest.setResponseFormatDeprecated(ResponseFormat.XHTML);
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
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 1, (short) 2, (short) 3, (short) 4, (short) 9, (short) 10 ));
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
        assertEquals(sasRequestParameters.getProcessedMkSlot(), Arrays.asList((short) 3, (short) 4, (short) 1, (short) 10, (short) 9, (short) 11, (short) 12));
        selectedSlots.clear();
    }


    @Test
    public void testSasParamCSITagUpdateLogic() {
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
        final Carrier carrier = new Carrier();
        final User user = new User();
        final Geo geo = new Geo();
        final Set<Integer> stateIds = new HashSet<Integer>();
        final IntegrationDetails integrationDetails = new IntegrationDetails();
        final AdPoolRequest adPoolRequest = new AdPoolRequest();
        adPoolRequest.setSite(site);
        adPoolRequest.setDevice(device);
        adPoolRequest.setCarrier(carrier);
        adPoolRequest.setUser(user);
        adPoolRequest.setGeo(geo);
        adPoolRequest.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        final SASRequestParameters sasRequestParameters = new SASRequestParameters();

        // userProfileCSiTAG and coreAttribute CSIIds are null
        try {
            thriftRequestParser
                    .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);
            Assert.assertTrue(sasRequestParameters.getCsiTags() != null);
        } catch (final Exception e) {
            Assert.assertFalse(true);
        }

        // userProfileCSiTAG not null and coreAttribute CSIIds are null
        final Set<Integer> umpCSITag = new HashSet<>();
        umpCSITag.add(1);
        final UserProfile userProfile = new UserProfile();
        userProfile.setCsiTags(umpCSITag);
        adPoolRequest.getUser().setUserProfile(userProfile);
        try {
            thriftRequestParser
                    .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);
            Assert.assertEquals(sasRequestParameters.getCsiTags(), umpCSITag);
        } catch (final Exception e) {
            Assert.assertFalse(true);
        }

        // userProfileCSiTAG  null and coreAttribute CSIIds are not null
        final Map<Integer, ValueProperties> valuePropertiesMap = new HashMap<>();
        valuePropertiesMap.put(1, new ValueProperties());

        final Map<String, Map<Integer, ValueProperties>> valueMap = new HashMap<>();
        valueMap.put("BRAND", valuePropertiesMap);

        final IntAttribute intAttribute = new IntAttribute();
        intAttribute.setValueMap(valueMap);

        final CoreAttributes coreAttributes = new CoreAttributes();
        coreAttributes.setCsids(intAttribute);

        userProfile.setCsiTags(null);
        adPoolRequest.getUser().setUserProfile(userProfile);
        adPoolRequest.setCoreAttributes(coreAttributes);
        final Set<Integer> expectedSet = new HashSet<>();
        expectedSet.add(1);
        try {
            thriftRequestParser
                    .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);
            Assert.assertEquals(sasRequestParameters.getCsiTags(), expectedSet);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }

        // userProfileCSiTAG  not null and coreAttribute CSIIds are not null
        umpCSITag.add(2);
        userProfile.setCsiTags(umpCSITag);
        adPoolRequest.getUser().setUserProfile(userProfile);
        adPoolRequest.setCoreAttributes(coreAttributes);
        expectedSet.add(1);
        expectedSet.add(2);
        try {
            thriftRequestParser
                    .parseRequestParameters(adPoolRequest, sasRequestParameters, casInternalRequestParameters, 6);
            Assert.assertEquals(sasRequestParameters.getCsiTags(), expectedSet);
        } catch (final Exception e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
    }
}