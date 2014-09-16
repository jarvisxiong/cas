package com.inmobi.adserve.channels.server.requesthandler;

import com.google.inject.Singleton;
import com.inmobi.adserve.adpool.*;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.types.InventoryType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.*;
import java.util.Map.Entry;


@Singleton
public class ThriftRequestParser {

    private static final Logger LOG = LoggerFactory.getLogger(ThriftRequestParser.class);

    public void parseRequestParameters(final AdPoolRequest tObject, final SASRequestParameters params,
            final CasInternalRequestParameters casInternalRequestParameters, final int dst) {
        LOG.debug("Inside parameter parser : ThriftParser");
        params.setAllParametersJson(tObject.toString());
        params.setDst(dst);
        params.setResponseOnlyFromDcp(2 == dst);

        // Fill params from AdPoolRequest Object
        params.setRemoteHostIp(tObject.remoteHostIp);
        // TODO Iterate over the segments using all slots
        Short slotId = null != tObject.selectedSlots && !tObject.selectedSlots.isEmpty() ? tObject.selectedSlots.get(0)
                : (short) 0;
        params.setSlot(slotId);
        params.setRqMkSlot(tObject.selectedSlots);
        params.setRFormat(getResponseFormat(tObject.responseFormat));
        params.setRqMkAdcount(tObject.requestedAdCount);
        params.setTid(tObject.requestId);
        params.setAllowBannerAds(tObject.isSetSupplyCapabilities()
                && tObject.supplyCapabilities.contains(SupplyCapability.BANNER));
        // TODO use segment id in cas as long
        int segmentId = tObject.isSetSegmentId() ? (int) tObject.segmentId : 0;
        params.setSiteSegmentId(segmentId);
        boolean isInterstitial = tObject.isSetRequestedAdType()
                && (tObject.requestedAdType == RequestedAdType.INTERSTITIAL);
        params.setRqAdType(isInterstitial ? "int" : (tObject.isSetRequestedAdType() ? tObject.requestedAdType.name()
                : ""));
        params.setRichMedia(tObject.isSetSupplyCapabilities()
                && tObject.supplyCapabilities.contains(SupplyCapability.RICH_MEDIA));
        params.setAccountSegment(getAccountSegments(tObject.demandTypesAllowed));
        params.setIpFileVersion(new Long(tObject.ipFileVersion).intValue());
        params.setSst(tObject.isSetSupplySource() ? tObject.supplySource.getValue() : 0);
        EncryptionKeys encryptionKeys = tObject.getEncryptionKeys();
        params.setEncryptionKey(encryptionKeys);

        // Fill params from integration details object
        if (tObject.isSetIntegrationDetails()) {
            params.setRqIframe(tObject.integrationDetails.iFrameId);
            if (tObject.integrationDetails.isSetAdCodeType()) {
                params.setAdcode(tObject.integrationDetails.adCodeType.toString());
            }
            params.setSdkVersion(getSdkVersion(tObject.integrationDetails.integrationType,
                    tObject.integrationDetails.integrationVersion));
            // TODO Wait for the final contract from Devashish
            params.setAdcode(getAdCode(tObject.integrationDetails.integrationType));
        }

        // Fill param from Site Object
        if (tObject.isSetSite()) {
            params.setSiteId(tObject.site.siteId);
            final boolean isApp = tObject.site.isSetInventoryType() && tObject.site.inventoryType == InventoryType.APP;
            params.setSource(isApp ? "APP" : "WAP");
			if (isApp) {
				if (CasConfigUtil.repositoryHelper != null) {
					params.setWapSiteUACEntity(CasConfigUtil.repositoryHelper.queryWapSiteUACRepository(tObject.site.siteId));
				}
			}
			if (CasConfigUtil.repositoryHelper != null) {
				params.setSiteEcpmEntity(CasConfigUtil.repositoryHelper.querySiteEcpmRepository(tObject.site.siteId,
						tObject.geo.countryId, (int) tObject.device.osId));
			}
			params.setSiteType(tObject.site.isSetContentRating() ? tObject.site.contentRating.toString() : "FAMILY_SAFE");
            params.setCategories(convertIntToLong(tObject.site.siteTaxonomies));
            double ecpmFloor = Math.max(tObject.site.ecpmFloor, tObject.site.cpmFloor);
            params.setSiteFloor(ecpmFloor);
            params.setSiteIncId(tObject.site.siteIncId);
            params.setAppUrl(tObject.site.siteUrl);
            params.setPubId(tObject.site.publisherId);
        }

        // Fill params from Device Object
        if (tObject.isSetDevice()) {
            params.setUserAgent(tObject.device.userAgent);
            // TODO Change to int in thrift
            params.setOsId(new Long(tObject.device.osId).intValue());
            params.setModelId(new Long(tObject.device.modelId).intValue());
            params.setHandsetInternalId(tObject.device.getHandsetInternalId());
            params.setOsMajorVersion(tObject.device.getOsMajorVersion());            
        }

        // Fill params from Geo Object
        if (tObject.isSetGeo()) {
            params.setLocSrc(tObject.geo.isSetLocationSource() ? tObject.geo.locationSource.name() : "LATLON");
            // TODO Change format in dcp
            String latLong = "";
            if (tObject.geo.latLong != null) {
                latLong = tObject.geo.latLong.latitude + "," + tObject.geo.latLong.longitude;
            }
            params.setLatLong(latLong);
            params.setCountryCode(tObject.geo.countryCode);
            params.setCountryId((long) tObject.geo.getCountryId());
            Set<Integer> cities = tObject.geo.getCityIds();
            params.setCity(null != cities && cities.iterator().hasNext() ? tObject.geo.getCityIds().iterator().next()
                    : null);
            Set<Integer> postalCodes = tObject.geo.getZipIds();
            params.setPostalCode(null != postalCodes && postalCodes.iterator().hasNext() ? tObject.geo.getZipIds()
                    .iterator().next() : null);
            Set<Integer> states = tObject.geo.getStateIds();
            params.setState(null != states && states.iterator().hasNext() ? tObject.geo.getStateIds().iterator().next()
                    : null);
        }

        // Fill Params from User Object
        if (tObject.isSetUser()) {
            // TODO Change age to integer in DCP
            int currentYear = (short) Calendar.getInstance().get(Calendar.YEAR);
            int yob = tObject.user.yearOfBirth;

            // Condition to check whether user's age is less than 100
            if ((yob > currentYear - 100) && (yob < currentYear)) {
                int age = currentYear - yob;
                params.setAge((short) age);
            }

            if (tObject.user.gender != null) {
                switch (tObject.user.gender) {
                    case FEMALE:
                        params.setGender("F");
                        break;
                    case MALE:
                        params.setGender("M");
                        break;
                    default:
                        params.setGender(null);
                        break;
                }

            }
            else
                params.setGender(null);
        }

        // Fill params from UIDParams Object
        if (tObject.isSetUidParams()) {
            setUserIdParams(casInternalRequestParameters, tObject.getUidParams());
            params.setTUidParams(getUserIdMap(tObject.getUidParams().getRawUidValues()));
        }

        // Fill params from Carrier Object
        if (tObject.isSetCarrier()) {
            params.setCarrierId(new Long(tObject.carrier.carrierId).intValue());
            params.setNetworkType(tObject.carrier.networkType);
            
            
        }

        LOG.debug("Successfully parsed tObject, SAS params are : {}", params.toString());
    }

    private String getResponseFormat(final ResponseFormat rqFormat) {
        String rFormat = "html";
        if (null == rqFormat) {
            return rFormat;
        }
        switch (rqFormat) {
            case HTML:
                rFormat = "html";
                break;
            case XHTML:
                rFormat = "xhtml";
                break;
            case AXML:
                rFormat = "axml";
                break;
            case JSON:
                rFormat = "json";
                break;
            case RTBS:
                rFormat = "rtbs";
                break;
            case IMAI:
                rFormat = "imai";
                break;
            case NATIVE:
                rFormat = "native";
                break;
            default:// Do Nothing
        }
        return rFormat;
    }

    private Set<Integer> getAccountSegments(final Set<DemandType> demandTypes) {

        LOG.debug("demandTypesAllowed value is: {}", demandTypes);

        if (null == demandTypes) {
            return Collections.emptySet();
        }
        Set<Integer> accountsSegments = new HashSet<Integer>();
        for (DemandType demandType : demandTypes) {
            switch (demandType) {
                case BRAND:
                    accountsSegments.add(4);
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

    private List<Long> convertIntToLong(final Set<Integer> intList) {
        if (null == intList) {
            return Collections.emptyList();
        }
        List<Long> longList = new ArrayList<Long>();
        for (Integer obj : intList) {
            longList.add(Long.valueOf(obj));
        }
        return longList;
    }

    private Map<String, String> getUserIdMap(final Map<UidType, String> uidMap) {
        Map<String, String> userIdMap = new HashMap<String, String>();
        for (Entry<UidType, String> entry : uidMap.entrySet()) {
            UidType uidType = entry.getKey();
            userIdMap.put(uidType.toString().toUpperCase(), entry.getValue());
        }
        return userIdMap;
    }

    private void setUserIdParams(final CasInternalRequestParameters parameter, final UidParams uidParams) {
        Map<UidType, String> uidMap = uidParams.getRawUidValues();
        parameter.uidADT = uidParams.isLimitIOSAdTracking() ? "0" : "1";
        parameter.uuidFromUidCookie = uidParams.getUuidFromUidCookie();

        for (Entry<UidType, String> entry : uidMap.entrySet()) {
            UidType uidType = entry.getKey();
            String uidValue = entry.getValue();
            switch (uidType) {
                case UDID:
                    parameter.uid = uidValue;
                    if (StringUtils.isNotBlank(parameter.uid) && parameter.uid.length() != 32) {
                        parameter.uid = MD5(parameter.uid);
                    }
                    break;
                case O1:
                    parameter.uidO1 = uidValue;
                    break;
                case UM5:
                    parameter.uidMd5 = uidValue;
                    break;
                case IDA:
                    parameter.uidIFA = uidValue;
                    break;
                case SO1:
                    parameter.uidSO1 = uidValue;
                    break;
                case IDV:
                    parameter.uidIFV = uidValue;
                    break;
                case IUDS1:
                    parameter.uidIDUS1 = uidValue;
                    break;
                case WC:
                    parameter.uidWC = uidValue;
                    break;
                case GPID:
                    parameter.gpid = uidValue;
                    break;
                default:
                    break;
            }
        }
    }

    public String MD5(final String md5) {
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

    public String getAdCode(final IntegrationType integrationType) {
        if (integrationType == IntegrationType.JSAC || integrationType == IntegrationType.WINDOWS_JS_SDK) {
            return "JS";
        }
        return "NON-JS";
    }

    public String getSdkVersion(final IntegrationType integrationType, final int version) {
        if (integrationType == IntegrationType.ANDROID_SDK) {
            return "a" + version;
        }
        else if (integrationType == IntegrationType.IOS_SDK) {
            return "i" + version;
        }
        return null;
    }
}
