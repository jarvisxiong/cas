package com.inmobi.adserve.channels.server.requesthandler;

import com.google.inject.Singleton;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.DemandType;
import com.inmobi.adserve.adpool.EncryptionKeys;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.adpool.ResponseFormat;
import com.inmobi.adserve.adpool.SupplyCapability;
import com.inmobi.adserve.adpool.UidParams;
import com.inmobi.adserve.adpool.UidType;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.types.InventoryType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


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

        params.setRqMkSlot(tObject.selectedSlots);
        getSlotList(tObject.selectedSlots, params, dst);

        params.setRFormat(getResponseFormat(tObject.responseFormat));
        params.setRqMkAdcount(tObject.requestedAdCount);
        params.setTid(tObject.taskId);
        params.setAllowBannerAds(tObject.isSetSupplyCapabilities()
                && tObject.supplyCapabilities.contains(SupplyCapability.BANNER));
        // TODO use segment id in cas as long
        final int segmentId = tObject.isSetSegmentId() ? (int) tObject.segmentId : 0;
        params.setSiteSegmentId(segmentId);
        final boolean isInterstitial =
                tObject.isSetRequestedAdType() && tObject.requestedAdType == RequestedAdType.INTERSTITIAL;
        params.setRqAdType(isInterstitial ? "int" : tObject.isSetRequestedAdType()
                ? tObject.requestedAdType.name()
                : "");
        params.setRichMedia(tObject.isSetSupplyCapabilities()
                && tObject.supplyCapabilities.contains(SupplyCapability.RICH_MEDIA));
        params.setAccountSegment(getAccountSegments(tObject.demandTypesAllowed));
        params.setIpFileVersion((int) tObject.ipFileVersion);
        params.setSst(tObject.isSetSupplySource() ? tObject.supplySource.getValue() : 0);
        final EncryptionKeys encryptionKeys = tObject.getEncryptionKeys();
        params.setEncryptionKey(encryptionKeys);
        params.setReferralUrl(tObject.referralUrl);

        // Fill params from integration details object
        if (tObject.isSetIntegrationDetails()) {
            params.setRqIframe(tObject.integrationDetails.iFrameId);
            if (tObject.integrationDetails.isSetAdCodeType()) {
                params.setAdcode(tObject.integrationDetails.adCodeType.toString());
            }
            params.setSdkVersion(getSdkVersion(tObject.integrationDetails.integrationType,
                    tObject.integrationDetails.integrationVersion));
            params.setAdcode(getAdCode(tObject.integrationDetails.integrationType));
        }

        // Fill param from Site Object
        if (tObject.isSetSite()) {
            params.setSiteId(tObject.site.siteId);
            final boolean isApp = tObject.site.isSetInventoryType() && tObject.site.inventoryType == InventoryType.APP;
            params.setSource(isApp ? "APP" : "WAP");

            if (CasConfigUtil.repositoryHelper != null) {
                params.setWapSiteUACEntity(CasConfigUtil.repositoryHelper
                        .queryWapSiteUACRepository(tObject.site.siteId));

                params.setSiteEcpmEntity(CasConfigUtil.repositoryHelper.querySiteEcpmRepository(tObject.site.siteId,
                        tObject.geo.countryId, (int) tObject.device.osId));
            }
            params.setSiteContentType(tObject.site.isSetSiteContentType()
                    ? tObject.site.getSiteContentType()
                    : ContentType.FAMILY_SAFE);
            params.setCategories(convertIntToLong(tObject.site.siteTaxonomies));
            final double ecpmFloor = Math.max(tObject.site.ecpmFloor, tObject.site.cpmFloor);
            params.setSiteFloor(ecpmFloor);
            final double computedBidGuidance = tObject.guidanceBid * 1.0 / Math.pow(10, 6);

            if (tObject.isSetGuidanceBid() && computedBidGuidance > ecpmFloor) {
                params.setMarketRate(computedBidGuidance);
            } else {
                params.setMarketRate(ecpmFloor);
            }
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
            if (tObject.device.getDeviceType() != null) {
                params.setDeviceType(tObject.device.getDeviceType().toString()); // FEATURE_PHONE, SMARTPHONE, TABLET
            }
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
            params.setCountryId((long) tObject.geo.getCountryId()); // TODO: Evaluate if int->long casting is needed?
            final Set<Integer> cities = tObject.geo.getCityIds();
            params.setCity(null != cities && cities.iterator().hasNext()
                    ? tObject.geo.getCityIds().iterator().next()
                    : null);

            params.setPostalCode(getPostalCode(tObject.geo.getZipIds()));
            final Set<Integer> states = tObject.geo.getStateIds();
            params.setState(null != states && states.iterator().hasNext()
                    ? tObject.geo.getStateIds().iterator().next()
                    : null);
        }

        // Fill Params from User Object
        if (tObject.isSetUser()) {
            // TODO Change age to integer in DCP
            final int currentYear = (short) Calendar.getInstance().get(Calendar.YEAR);
            final int yob = tObject.user.yearOfBirth;

            // Condition to check whether user's age is less than 100
            if (yob > currentYear - 100 && yob < currentYear) {
                final int age = currentYear - yob;
                params.setAge((short) age);
            }
            if (tObject.user.isSetUserProfile()) {
                params.setCsiTags(tObject.user.userProfile.csiTags);
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

            } else {
                params.setGender(null);
            }
        }

        // Fill params from UIDParams Object
        if (tObject.isSetUidParams()) {
            setUserIdParams(casInternalRequestParameters, tObject.getUidParams());
            params.setTUidParams(getUserIdMap(tObject.getUidParams().getRawUidValues()));
        }

        // Fill params from Carrier Object
        if (tObject.isSetCarrier()) {
            params.setCarrierId((int) tObject.carrier.carrierId);
            params.setNetworkType(tObject.carrier.networkType);
        }

        LOG.debug("Successfully parsed tObject, SAS params are : {}", params.toString());
    }

    protected String getPostalCode(Set<Integer> postalCodes) {
        final Integer zipId = (null != postalCodes && postalCodes.iterator().hasNext() ? postalCodes
                .iterator().next() : null);
        if(zipId != null) {
            final String probableZipCode = CasConfigUtil.repositoryHelper.queryGeoZipRepository(zipId).getZipCode();
            if (NumberUtils.isNumber(probableZipCode)) {
                return probableZipCode;
            }
        }
        return null;
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
        final Set<Integer> accountsSegments = new HashSet<Integer>();
        for (final DemandType demandType : demandTypes) {
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
        final List<Long> longList = new ArrayList<Long>();
        for (final Integer obj : intList) {
            longList.add(Long.valueOf(obj));
        }
        return longList;
    }

    private Map<String, String> getUserIdMap(final Map<UidType, String> uidMap) {
        final Map<String, String> userIdMap = new HashMap<String, String>();
        for (final Entry<UidType, String> entry : uidMap.entrySet()) {
            final UidType uidType = entry.getKey();
            userIdMap.put(uidType.toString().toUpperCase(), entry.getValue());
        }
        return userIdMap;
    }

    private void setUserIdParams(final CasInternalRequestParameters parameter, final UidParams uidParams) {
        final Map<UidType, String> uidMap = uidParams.getRawUidValues();
        parameter.setUidADT(uidParams.isLimitIOSAdTracking() ? "0" : "1");
        parameter.setUuidFromUidCookie(uidParams.getUuidFromUidCookie());

        for (final Entry<UidType, String> entry : uidMap.entrySet()) {
            final UidType uidType = entry.getKey();
            final String uidValue = entry.getValue();
            switch (uidType) {
                case UDID:
                    parameter.setUid(uidValue);
                    if (StringUtils.isNotBlank(parameter.getUid()) && parameter.getUid().length() != 32) {
                        parameter.setUid(MD5(parameter.getUid()));
                    }
                    break;
                case O1:
                    parameter.setUidO1(uidValue);
                    break;
                case UM5:
                    parameter.setUidMd5(uidValue);
                    break;
                case IDA:
                    parameter.setUidIFA(uidValue);
                    break;
                case SO1:
                    parameter.setUidSO1(uidValue);
                    break;
                case IDV:
                    parameter.setUidIFV(uidValue);
                    break;
                case IUDS1:
                    parameter.setUidIDUS1(uidValue);
                    break;
                case WC:
                    parameter.setUidWC(uidValue);
                    break;
                case GPID:
                    parameter.setGpid(uidValue);
                    break;
                default:
                    break;
            }
        }
    }

    public String MD5(final String md5) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] array = md.digest(md5.getBytes());
            final StringBuffer sb = new StringBuffer();
            for (final byte anArray : array) {
                sb.append(Integer.toHexString(anArray & 0xFF | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (final java.security.NoSuchAlgorithmException ignored) {
            LOG.debug("Exception {} raised with String {} in MD5", ignored, md5);
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
        } else if (integrationType == IntegrationType.IOS_SDK) {
            return "i" + version;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public void getSlotList(final List<Short> selectedSlots, final SASRequestParameters sasRequestParameters,
                            final int dst) {

        if (DemandSourceType.IX.getValue() == dst) {
            // Keep at most 5 slots in the list
            List<Short> listOfIXSupportedSlots = new ArrayList<Short>();
            int slotListSize = selectedSlots.size();
            for (int slotIndex = 0; slotIndex < slotListSize && listOfIXSupportedSlots.size() < 5; slotIndex++) {
                final Short slotId = selectedSlots.get(slotIndex);
                if (SlotSizeMapping.getIX_SLOT_ID_MAP().containsKey(slotId)) {// check if Slot present in IXSupportedSlot
                    listOfIXSupportedSlots.add(slotId);
                }
            }
            sasRequestParameters.setProcessedMkSlot(listOfIXSupportedSlots);
        } else {
            List<Short> listOfUmpSlots = new ArrayList<Short>();
            int slotListSize = selectedSlots.size();
            for (int slotIndex = 0; slotIndex < slotListSize && listOfUmpSlots.size() < 5; slotIndex++) {
                final Short slotId = selectedSlots.get(slotIndex);
                if (null != CasConfigUtil.repositoryHelper.querySlotSizeMapRepository(slotId)) {// check if Slot present in SlotSizeRepo
                    listOfUmpSlots.add(slotId);
                }
            }
            sasRequestParameters.setProcessedMkSlot(listOfUmpSlots);
        }
        return;
    }
}
