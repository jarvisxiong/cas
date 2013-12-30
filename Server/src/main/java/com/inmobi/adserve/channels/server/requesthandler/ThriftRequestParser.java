package com.inmobi.adserve.channels.server.requesthandler;

import com.google.gson.Gson;
import com.inmobi.adserve.adpool.*;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.DebugLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.*;


public class ThriftRequestParser {

    // Extracting params.
    public static AdPoolRequest extractParams(Map<String, List<String>> params) throws JSONException,
            UnsupportedEncodingException {
        Gson gson = new Gson();
        if (!params.isEmpty()) {
            List<String> values = params.get("post");
            if (CollectionUtils.isNotEmpty(values)) {
                String stringVal = values.iterator().next();
                AdPoolRequest adPoolRequest = new AdPoolRequest();
                TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                try {
                    tDeserializer.deserialize(adPoolRequest, stringVal.getBytes());
                } catch (TException e) {
                    e.printStackTrace();
                }
                return adPoolRequest;
            }
        }
        return null;
    }

    public static void parseRequestParameters(AdPoolRequest tObject, SASRequestParameters params,
            CasInternalRequestParameters casInternalRequestParameters, DebugLogger logger, int dst) {
        logger.debug("Inside thrift request parser");
        params.setAllParametersJson(tObject.toString());
        params.setDst(dst);
        params.setResponseOnlyFromDcp(2 == dst);
        

        //Fill params from AdPoolRequest Object
        params.setRemoteHostIp(tObject.remoteHostIp);
        //TODO Iterate over the segments using all slots
        Short slotId =  null != tObject.selectedSlots ?  tObject.selectedSlots.get(0) : (short)0;
        params.setSlot(slotId);
        params.setRqMkSlot(tObject.selectedSlots);
        params.setRFormat(tObject.responseFormat);
        //TODO change to short in DCP too
        params.setRqMkAdcount(tObject.requestedAdCount);
        params.setTid(tObject.requestId);
        params.setAllowBannerAds(tObject.supplyCapability == SupplyCapability.BANNER);
        //TODO use segment id in cas as long
        params.setSiteSegmentId(new Long(tObject.segmentId).intValue());
        params.setRqAdType(null != tObject.requestedAdType ? tObject.requestedAdType.name() : "INTERSTITIAL");
        params.setRichMedia(tObject.supplyCapability == SupplyCapability.RICH_MEDIA);
        params.setAccountSegment(getAccountSegments(tObject.demandTypesAllowed));
        params.setIpFileVersion(new Long(tObject.ipFileVersion).intValue());
        params.setSst(tObject.supplySource != null ? tObject.supplySource.ordinal() : 0);

        
        //Fill params from integration details object
        if (null != tObject.integrationDetails) {
            params.setRqIframe(tObject.integrationDetails.iFrameId);
            params.setAdcode(tObject.integrationDetails.adCodeType.toString());
        }
        
        
        //Fill param from Site Object
        if (null != tObject.site) {
            params.setSiteId(tObject.site.siteId);
            params.setSource(null != tObject.site.contentRating ? tObject.site.contentRating.toString() : "FAMILY_SAFE");
            params.setSiteType(null != tObject.site.inventoryType ? tObject.site.inventoryType.toString() : "APP");
            params.setCategories(convertIntToLong(tObject.site.siteTags));
            params.setSiteFloor(tObject.site.ecpmFloor);
            params.setSiteIncId(tObject.site.siteIncId);
            params.setAppUrl(tObject.site.siteUrl);
        }
        
        
        //Fill params from Device Object
        if (null != tObject.device) {
            params.setUserAgent(tObject.device.userAgent);
            //TODO Change to int in thrift
            params.setOsId(new Long(tObject.device.osId).intValue());
            params.setModelId(new Long(tObject.device.modelId).intValue());
            params.setHandsetInternalId(tObject.device.getHandsetInternalId());
        }

        //Fill params from Geo Object
        if (null != tObject.geo) {
            params.setLocSrc(null != tObject.geo.locationSource ? tObject.geo.locationSource.name() : "LATLON");
            //TODO Change format in dcp 
            String latLong = "";
            if (tObject.geo.latLong != null) {
               latLong = tObject.geo.latLong.latitude + "," + tObject.geo.latLong.longitude; 
            }
            params.setLatLong(latLong);
            params.setCountryCode(tObject.geo.countryCode);
            //TODO Clean the names country name and country id
            params.setCountryId(tObject.geo.getCountryId());
            Set<Integer> cities = tObject.geo.getCityIds();
            params.setCity(null != cities && cities.iterator().hasNext() ? tObject.geo.getCityIds().iterator().next() : null);
            Set<Integer> postalCodes = tObject.geo.getZipIds();
            params.setPostalCode(null != postalCodes && postalCodes.iterator().hasNext() ? tObject.geo.getZipIds().iterator().next() : null);
            Set<Integer> states = tObject.geo.getStateIds();
            params.setState(null != states && states.iterator().hasNext() ? tObject.geo.getStateIds().iterator().next() : null);
        }
        

        //Fill Params from User Object
        if (null != tObject.user) {
            //TODO Change age to integer in DCP
            int currentYear = (short)Calendar.getInstance().get(Calendar.YEAR);
            int yob = tObject.user.yearOfBirth;
            int age = currentYear - yob;
            params.setAge((short)age);
            params.setGender(tObject.user.gender.name().equalsIgnoreCase("Male") ? "M" : "F");
        }

        
        //Fill params from UIDParams Object
        if (null != tObject.getUidParams()) {
            setUserIdParams(casInternalRequestParameters, tObject.getUidParams());
            params.setTUidParams(getUserIdMap(tObject.getUidParams().getRawUidValues()));
        }

        
        //Fill params from Carrier Object
        if (null != tObject.carrier) {
            params.setCarrierId(new Long(tObject.carrier.carrierId).intValue());
        }
        
        logger.debug("Successfully parsed tObject, SAS params are : ", params.toString());
    }
    
    private static Set<Integer> getAccountSegments(Set<DemandType> demandTypes) {
        if (null == demandTypes) {
            return Collections.emptySet();
        }
        Set<Integer> accountsSegments = new HashSet<Integer>();
        for (DemandType demandType : demandTypes) {
            switch (demandType) {
                case BRAND:
                     accountsSegments.add(1);
                    break;
                case PERFORMANCE:
                    accountsSegments.add(6);
                    break;
                case PROGRAMMATIC:
                    accountsSegments.add(11);
                    break;
                default:
                    break;
            }
        }
        return accountsSegments;
    }

    private static List convertIntToLong(Set<Integer> intList) {
        if (null == intList) {
            return Collections.EMPTY_LIST;
        }
        List<Long> longList = new ArrayList<Long>();
        for (Integer obj : intList) {
            longList.add(Long.valueOf(obj));
        }
        return longList;
    }
    
    private static Map<String, String> getUserIdMap(Map<UidType, String> uidMap) {
        Map<String, String> userIdMap = new HashMap<String, String>();
        for (UidType uidType : uidMap.keySet()) {
            userIdMap.put(uidType.toString().toUpperCase(Locale.ENGLISH), uidMap.get(uidType));
        }
        return userIdMap;
    }

    private static void setUserIdParams(CasInternalRequestParameters parameter, UidParams uidParams) {
        Map<UidType, String> uidMap =  uidParams.getRawUidValues();
        parameter.uidADT = uidParams.isLimitIOSAdTracking() ? "0" : "1";
        for (UidType uidType : uidMap.keySet()) {
            switch (uidType) {
                case UDID:
                    parameter.uid = uidMap.get(uidType);
                    if (StringUtils.isNotBlank(parameter.uid) && parameter.uid.length() != 32) {
                        parameter.uid = MD5(parameter.uid);
                    }
                case O1:
                    parameter.uidO1 = uidMap.get(uidType);
                    break;
                case UM5:
                    parameter.uidMd5 = uidMap.get(uidType);
                    break;
                case IDA:
                    parameter.uidIFA = uidMap.get(uidType);
                    break;
                case SO1:
                    parameter.uidSO1 = uidMap.get(uidType);
                    break;
                case IDV:
                    parameter.uidIFV = uidMap.get(uidType);
                    break;
                case IUDS1:
                    parameter.uidIDUS1 = uidMap.get(uidType);
                    break;
                default:
                    break;
            }
        }
    }

    public static String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException ignored) {
        }
        return null;
    }
    
}
