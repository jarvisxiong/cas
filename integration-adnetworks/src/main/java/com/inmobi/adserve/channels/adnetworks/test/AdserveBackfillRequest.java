package com.inmobi.adserve.channels.adnetworks.test;

import com.inmobi.adserve.adpool.*;
import com.inmobi.types.AdPlacement;
import com.inmobi.types.ContentRating;
import com.inmobi.types.Gender;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import com.inmobi.adserve.adpool.

public class AdserveBackfillRequest {

    public static AdPoolRequest formulateNewBackFillRequest(byte[] aesKey, byte[] ivKey)  //, mandatoryParams, inputObj)
    {
        String adpool_requestid = "anotherreqid3";
        String adpool_remotehostip = "50.121.84.235";
        long site_siteincid = 1410556827919274l;//1380034566962368l;
        String site_siteurl = "newsiteurl";
        double site_cpcfloor = 0.6;
        double site_ecpmfloor = 3.5;
        String site_siteid = "6ab6976cffc44ebf84b413fe88d0c461";//"2bbf7d95fc314256a9bf9dd8de9d41bf";//"e445cd9750454deabe9174808d19aaa2";
        //"qwqqqw8254c74b80b117200ff0cb6";//"742c9e4363424d4ea73a3e8626375736";
        System.out.println("Site id is : "+site_siteid);
        String site_publisherid = "newpublisherid";
        InventoryType site_inventorytype = InventoryType.APP;
        ContentRating site_contentrating = ContentRating.PERFORMANCE;
        Set<Integer> site_sitetags = new HashSet<Integer>();
        site_sitetags.add(70);
        site_sitetags.add(72);
        site_sitetags.add(8);
        Set<Integer> site_sitetaxonomies = new HashSet<Integer>();
        site_sitetaxonomies.add(70);
        site_sitetaxonomies.add(71);
        String device_useragent = "Mozilla/5.0 (Linux; Android 4.4.2; Nexus 7 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Safari/537.36";
        long device_modelid = 1234;
        long device_manufacturerid = 12;
        long device_osid = 3;
        String device_osmajorversion = "6.2";
        long device_browserid = 0;
        String device_browsermajorversion = "0";
        long device_handsetinternalid = 0;
        DeviceType device_devicetype = DeviceType.SMARTPHONE;
        long carrier_carrierid = 0;
        NetworkType carrier_networktype = NetworkType.WIFI;
        short adpool_requestadcount = (short) 1;
        //List<SupplyCapability> adpool_supplycapability = new ArrayList<SupplyCapability>();
        //adpool_supplycapability.add(SupplyCapability.BANNER);

        List<Short> adpool_selectedslots = new ArrayList<Short>();
        adpool_selectedslots.add((short) 14);



        AdPoolRequest adPoolRequest = new AdPoolRequest();
        adPoolRequest.setTaskId(adpool_requestid);
        adPoolRequest.setRemoteHostIp(adpool_remotehostip);
        //adPoolRequest.setRequestedAdType(RequestedAdType.BANNER);
        adPoolRequest.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        adPoolRequest.setResponseFormatDeprecated(ResponseFormat.JSON);

        Site site = new Site();
        site.setSiteIncId(site_siteincid);
        site.setSiteUrl(site_siteurl);
        site.setCpcFloor(site_cpcfloor);
        site.setEcpmFloor(site_ecpmfloor);
        site.setSiteId(site_siteid);
        site.setPublisherId(site_publisherid);
        site.setInventoryType(site_inventorytype);
        //site.setContentRating(site_contentrating);
        site.setSiteTags(site_sitetags);
        site.setSiteTaxonomies(site_sitetaxonomies);


        adPoolRequest.setSite(site);


        Device device = new Device();
        device.setUserAgent(device_useragent);
        device.setModelId(device_modelid);
        device.setManufacturerId(device_manufacturerid);
        device.setOsId(device_osid);
        device.setOsMajorVersion(device_osmajorversion);
        device.setBrowserId(device_browserid);
        device.setBrowserMajorVersion(device_browsermajorversion);
        device.setHandsetInternalId(device_handsetinternalid);
        //device.setDeviceType(device_devicetype);

        adPoolRequest.setDevice(device);
        adPoolRequest.setPlacementId(1443209276693l);


        Carrier carrier = new Carrier();
        carrier.setCarrierId(carrier_carrierid);
        carrier.setNetworkType(carrier_networktype);

        adPoolRequest.setCarrier(carrier);

        adPoolRequest.setRequestedAdCount(adpool_requestadcount);
        //adPoolRequest.setResponseFormat(adpool_responseformat);
        //adPoolRequest.setTraceRequest(false);
        adPoolRequest.setTranscoderIpDetected(false);
        // adPoolRequest.setRequestedAdType(adpool_requestadtype);
        //adPoolRequest.setSupplyCapabilities(adpool_supplycapability);


        Geo geo = new Geo();
        geo.setCountryId(94);
        geo.setCountryCode("US");
        LocationSource geo_locationsource = LocationSource.LATLON;
        geo.setLocationSource(geo_locationsource);

        LatLong latLong = new LatLong();
        latLong.setLatitude(40.79);
        latLong.setLongitude(86.41);
        latLong.setAccuracy(0.0);
        geo.setLatLong(latLong);

        Set<Integer> geo_zipids = new HashSet<Integer>();
        geo.setZipIds(geo_zipids);
        geo.setFenceIds(new HashSet<Long>());
        geo.setCityIds(new HashSet<Integer>());
        geo.setStateIds(new HashSet<Integer>());

        adPoolRequest.setGeo(geo);

        UidParams uidParams = new UidParams();

        Map<UidType, String> rawUidValues = new HashMap<UidType, String>();


        rawUidValues.put(UidType.UDID, "abcd80571d65720efasdfaf");
        rawUidValues.put(UidType.IEM, "testimei");
        rawUidValues.put(UidType.GPID, "38E31250-DD17-4C48-A9B9-15B8B9AE5D02");

        uidParams.setRawUidValues(rawUidValues);
        uidParams.setUdidFromRequest("someudid");
        uidParams.setUuidFromUidCookie("somecookie");
        uidParams.setLimitIOSAdTracking(false);


        adPoolRequest.setUidParams(uidParams);


        User user = new User();
        user.setDataVendorId(1);
        user.setDataVendorName("user_datavendorname");
        user.setYearOfBirth((short) 0);
        user.setGender(Gender.MALE);
        user.setIncome(1000);
        user.setMaritalStatus(MaritalStatus.RELATIONSHIP);
        user.setEducation(Education.POST_GRADUATE_OR_ABOVE);
        user.setNativeLanguage("somelangua");
        //user.setInterests(user_interests);
        user.setEthnicity(Ethnicity.ASIAN);
        user.setSexualOrientation(SexualOrientation.STRAIGHT);
        user.setHasChildren(false);

        adPoolRequest.setUser(user);


        adPoolRequest.setSelectedSlots(adpool_selectedslots);
        adPoolRequest.setDemandTypesAllowed(new HashSet<DemandType>());

        //adPoolRequest.setSegmentId(0);
        //adPoolRequest.setTestRequest(true);
        //        adPoolRequest.setSupplySource(adpool_supplySource);
        //        adPoolRequest.setIpFileVersion(adpool_ipfileversion);

        // For backFill
        // And For rtbdFill
        //adPoolRequest.setIntegrationDetails( new IntegrationDetails().setIntegrationType(IntegrationType
        //																						  .ANDROID_SDK)
        //.setIntegrationVersion(400));

        adPoolRequest.setIntegrationDetails(
            new IntegrationDetails().setIntegrationType(IntegrationType.ANDROID_SDK).setIntegrationVersion(
                455));

        List<com.inmobi.adserve.adpool.SupplyContentType> allowedContent = new java.util.ArrayList<>();
        allowedContent.add(com.inmobi.adserve.adpool.SupplyContentType.BANNER);
        adPoolRequest.setSupplyAllowedContents(allowedContent);
        //adPoolRequest.setSupplyCapability(com.inmobi.segment.impl.SupplyCapability.BANNER);
		/*
		* Keys are required only for SDK 430+
		*/
		/* EncryptionKeys encryp = new EncryptionKeys();
        encryp.setAesKey(fromByteArray(aesKey));
        encryp.setInitializationVector(fromByteArray(ivKey));


        adPoolRequest.setEncryptionKeys(encryp);*/

        //        adPoolRequest.setIntegrationDetails( new IntegrationDetails().setIntegrationType(IntegrationType.IOS_SDK).setIntegrationVersion(430));
        //       adPoolRequest.setIntegrationDetails(null);
        //       adPoolRequest.setIntegrationDetails( new IntegrationDetails().setIntegrationType(IntegrationType.ANDROID_SDK).setIntegrationVersion(370));
        //        adPoolRequest.setIntegrationDetails( new IntegrationDetails().setIntegrationType(IntegrationType.ANDROID_SDK).setIntegrationVersion(400));
        //       adPoolRequest.setIntegrationDetails( new IntegrationDetails().setIntegrationType(IntegrationType.ANDROID_SDK).setIntegrationVersion(230));

        //        adPoolRequest.setIpFileVersion(1234);
        //
        //
        //
        //
        //        adPoolRequest.setRequestedAdType(RequestedAdType.INTERSTITIAL);
        //        adPoolRequest.setSite(site);
        //        adPoolRequest.setDevice(device);
        //        adPoolRequest.setCarrier(carrier);
        //        adPoolRequest.setResponseFormat("html");
        //        adPoolRequest.setGeo(geo);
        //
        //        adPoolRequest.setSupplyCapability(SupplyCapability.BANNER);
        //
        //        List<Short> slots = new LinkedList<Short>();
        //        slots.add((short)2);
        //        adPoolRequest.setSelectedSlots(slots);


        return adPoolRequest;
    }

    public static ByteBuffer fromByteArray(byte[] bytes) {
        final ByteBuffer ret = ByteBuffer.wrap(new byte[bytes.length]);

        ret.put(bytes);
        ret.flip();

        return ret;
    }

}
