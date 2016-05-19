package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android;
import static com.inmobi.adserve.channels.util.InspectorStrings.AUCTION_STATS;
import static com.inmobi.adserve.channels.util.InspectorStrings.BID_FLOOR_TOO_LOW;
import static com.inmobi.adserve.channels.util.InspectorStrings.BID_GUIDANCE_ABSENT;
import static com.inmobi.adserve.channels.util.InspectorStrings.BID_GUIDANCE_LESS_OR_EQUAL_TO_FLOOR;
import static com.inmobi.adserve.channels.util.InspectorStrings.CSIDS_MIGRATION_NOT_SANE;
import static com.inmobi.adserve.channels.util.InspectorStrings.CSIDS_MIGRATION_SANE;
import static com.inmobi.adserve.channels.util.InspectorStrings.IMEI;
import static com.inmobi.adserve.channels.util.InspectorStrings.IMEI_BEING_SENT_FOR;
import static com.inmobi.adserve.channels.util.InspectorStrings.ONLY_NEW_CSIDS_SET;
import static com.inmobi.adserve.channels.util.InspectorStrings.ONLY_OLD_CSIDS_SET;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.DemandType;
import com.inmobi.adserve.adpool.Device;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.adpool.ResponseFormat;
import com.inmobi.adserve.adpool.Site;
import com.inmobi.adserve.adpool.SiteTemplateSettings;
import com.inmobi.adserve.adpool.SupplyContentType;
import com.inmobi.adserve.adpool.UidParams;
import com.inmobi.adserve.adpool.UidType;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.GeoZipEntity;
import com.inmobi.adserve.channels.entity.IMEIEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.types.AdAttributeType;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.segment.impl.AdTypeEnum;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;

import io.netty.util.CharsetUtil;


@Singleton
public class ThriftRequestParser {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftRequestParser.class);

    private static final String DEFAULT_PUB_CONTROL_MEDIA_PREFERENCES =
            "{\"incentiveJSON\": \"{}\",\"video\" :{\"preBuffer\": \"WIFI\",\"skippable\": true,\"soundOn\": false}}";
    private static final List<AdTypeEnum> DEFAULT_PUB_CONTROL_SUPPORTED_AD_TYPES =
            Arrays.asList(AdTypeEnum.BANNER, AdTypeEnum.VIDEO);

    public void parseRequestParameters(final AdPoolRequest tObject, final SASRequestParameters params,
            final CasInternalRequestParameters casInternal, final int dst) {
        LOG.debug("Inside parameter parser : ThriftParser");
        params.setAllParametersJson(tObject.toString());
        params.setDst(dst);
        // Fill params from AdPoolRequest Object
        params.setRemoteHostIp(tObject.remoteHostIp);
        params.setRqMkSlot(tObject.selectedSlots);
        params.setProcessedMkSlot(getValidSlotList(tObject.selectedSlots, DemandSourceType.IX.getValue() == dst));
        params.setRFormat(getResponseFormat(tObject.responseFormatDeprecated));
        params.setRqMkAdcount(tObject.requestedAdCount);
        params.setTid(tObject.taskId);
        params.setAllowBannerAds(tObject.isSetSupplyAllowedContents()
                && tObject.supplyAllowedContents.contains(SupplyContentType.BANNER));
        // TODO use segment id in cas as long
        if (tObject.isSetSiteSegmentId()) {
            params.setSiteSegmentId((int) tObject.siteSegmentId);
        }
        if (tObject.isSetPlacementSegmentId()) {
            params.setPlacementSegmentId((int) tObject.placementSegmentId);
        }
        if (tObject.isSetPlacementId()) {
            params.setPlacementId(tObject.placementId);
        }
        if (tObject.isSetVastProtocols()) {
            params.setVastProtocols(ImmutableSet.copyOf(tObject.getVastProtocols()));
        }
        params.setRequestedAdType(tObject.getRequestedAdType());
        params.setRichMedia(tObject.isSetSupplyAllowedContents()
                && tObject.supplyAllowedContents.contains(SupplyContentType.RICH_MEDIA));
        params.setAccountSegment(getAccountSegments(tObject.demandTypesAllowed));
        params.setIpFileVersion((int) tObject.ipFileVersion);
        params.setSst(tObject.isSetSupplySource() ? tObject.supplySource.getValue() : 0);
        params.setEncryptionKey(tObject.getEncryptionKeys());
        params.setReferralUrl(tObject.referralUrl);
        params.setIntegrationDetails(tObject.getIntegrationDetails());
        params.setAppBundleId(tObject.getAppBundleId());
        params.setRequestGuid(tObject.isSetRequestGuid() ? tObject.requestGuid : StringUtils.EMPTY);

        if (tObject.isSetNoJsTracking()) {
            params.setNoJsTracking(tObject.isNoJsTracking());
        }
        // Fill param from Site Object
        setSiteObject(tObject, params, dst);
        // Fill params from Device Object
        setDevice(tObject, params);
        // Fill params from Geo Object
        setGeo(tObject, params);
        // Fill Params from User Object
        setUser(tObject, params);
        // Fill params from Carrier Object
        setCarrier(tObject, params);
        // Fill params from integration details object
        setIntegrationDetails(tObject, params);

        // Fill params from UIDParams Object
        if (tObject.isSetUidParams()) {
            setUserIdParams(casInternal, tObject.getUidParams());
            if (tObject.getUidParams().isSetRawUidValues()) {
                params.setTUidParams(getUserIdMap(tObject.getUidParams().getRawUidValues()));
            }
        }

        // Set imei related fields
        if (Android.getValue() == params.getOsId()
                && GlobalConstant.CHINA_COUNTRY_CODE.equals(params.getCountryCode())) {
            if (tObject.isSetIem()) {
                final String imei = StringUtils.lowerCase(tObject.getIem());
                InspectorStats.incrementStatCount(IMEI,
                        IMEI_BEING_SENT_FOR + DemandSourceType.findByValue(dst).toString());
                casInternal.setImeiMD5(DigestUtils.md5Hex(imei));
                casInternal.setImeiSHA1(DigestUtils.sha1Hex(imei));
            } else {
                if (StringUtils.isNotBlank(casInternal.getUidO1())) {
                    final IMEIEntity entity =
                            CasConfigUtil.repositoryHelper.queryIMEIRepository(casInternal.getUidO1());
                    if (entity != null) {
                        InspectorStats.incrementStatCount(InspectorStrings.IMEI_MATCH);
                        casInternal.setImeiMD5(entity.getImei());
                    }
                }
            }
        }
        if (tObject.isSetRqSslEnabled()) {
            params.setSecureRequest(tObject.rqSslEnabled);
        }

        LOG.debug("Successfully parsed tObject, SAS params are : {}", params.toString());
    }

    private void setCarrier(final AdPoolRequest tObject, final SASRequestParameters params) {
        if (tObject.isSetCarrier()) {
            params.setCarrierId((int) tObject.carrier.carrierId);
            if (tObject.getCarrier().isSetConnectionType()) {
                params.setConnectionType(tObject.carrier.connectionType);
            } else if (tObject.getCarrier().isSetNetworkType()) {
                params.setConnectionType(NetworkType.WIFI == tObject.getCarrier().getNetworkType()
                        ? ConnectionType.WIFI
                        : ConnectionType.CELLULAR_UNKNOWN);
            } else {
                params.setConnectionType(ConnectionType.UNKNOWN);
            }
        }
    }

    private void setUser(final AdPoolRequest tObject, final SASRequestParameters params) {
        if (tObject.isSetUser()) {
            final int currentYear = (short) Calendar.getInstance().get(Calendar.YEAR);
            final int yob = tObject.user.yearOfBirth;
            if (tObject.getUser().isSetUserProfile() && tObject.getUser().getUserProfile().isSetNormalizedUserId()) {
                params.setNormalizedUserId(tObject.getUser().getUserProfile().getNormalizedUserId());
            }
            // Condition to check whether user's age is less than 100
            if (yob > currentYear - 100 && yob < currentYear) {
                final int age = currentYear - yob;
                params.setAge((short) age);
            }
            if (tObject.user.isSetUserProfile()) {
                final Set<Integer> csiTagsOld = tObject.user.userProfile.csiTags;
                params.setCsiTags(csiTagsOld);

                Set<Integer> csiTagsNew = null;
                if (tObject.user.userProfile.isSetTUserProfile()) {
                    if (tObject.user.userProfile.tUserProfile.isSetCsiIds()) {
                        csiTagsNew = ImmutableSet.copyOf(tObject.user.userProfile.getTUserProfile().getCsiIds());
                    }
                }

                final boolean csiTagsOldSet = CollectionUtils.isNotEmpty(csiTagsOld);
                final boolean csiTagsNewSet = CollectionUtils.isNotEmpty(csiTagsNew);

                if (csiTagsOldSet && !csiTagsNewSet) {
                    InspectorStats.incrementStatCount(ONLY_OLD_CSIDS_SET);
                } else if (!csiTagsOldSet && csiTagsNewSet) {
                    InspectorStats.incrementStatCount(ONLY_NEW_CSIDS_SET);
                } else if (csiTagsOldSet && csiTagsNewSet) {
                    if (csiTagsNew.containsAll(csiTagsOld)) {
                        InspectorStats.incrementStatCount(CSIDS_MIGRATION_SANE);
                    } else {
                        InspectorStats.incrementStatCount(CSIDS_MIGRATION_NOT_SANE);
                    }
                }
            }

            if (tObject.user.gender != null) {
                switch (tObject.user.gender) {
                    case FEMALE:
                        params.setGender(GlobalConstant.GENDER_FEMALE);
                        break;
                    case MALE:
                        params.setGender(GlobalConstant.GENDER_MALE);
                        break;
                    default:
                        params.setGender(null);
                        break;
                }
            }
        }
    }

    private void setGeo(final AdPoolRequest tObject, final SASRequestParameters params) {
        if (tObject.isSetGeo()) {
            // params.setLocSrc(tObject.geo.isSetLocationSource() ? tObject.geo.locationSource.name() :
            // GlobalConstant.LATLON);
            params.setLocationSource(
                    tObject.geo.isSetLocationSource() ? tObject.geo.getLocationSource() : LocationSource.LATLON);
            // TODO Change format in dcp
            String latLong = StringUtils.EMPTY;
            if (tObject.geo.latLong != null) {
                latLong = tObject.geo.latLong.latitude + "," + tObject.geo.latLong.longitude;
            }
            params.setLatLong(latLong);
            params.setCountryCode(tObject.geo.countryCode);
            params.setCountryId((long) tObject.geo.getCountryId()); // TODO: Evaluate if int->long casting is needed?
            final Set<Integer> cities = tObject.geo.getCityIds();
            params.setCity(
                    null != cities && cities.iterator().hasNext() ? tObject.geo.getCityIds().iterator().next() : null);
            params.setPostalCode(getPostalCode(tObject.geo.getZipIds()));
            final Set<Integer> states = tObject.geo.getStateIds();
            params.setState(
                    null != states && states.iterator().hasNext() ? tObject.geo.getStateIds().iterator().next() : null);
            params.setGeoFenceIds(tObject.geo.getFenceIds());
        }
    }

    private void setDevice(final AdPoolRequest tObject, final SASRequestParameters params) {
        if (tObject.isSetDevice()) {
            final Device tDevice = tObject.device;
            String userAgent = tDevice.userAgent;
            try {
                userAgent = userAgent != null ? URLDecoder.decode(userAgent, GlobalConstant.UTF_8) : null;
            } catch (final UnsupportedEncodingException e) {}
            params.setUserAgent(userAgent);
            params.setOsId(new Long(tDevice.osId).intValue());
            params.setModelId(tDevice.modelId);
            params.setManufacturerId(tDevice.manufacturerId);
            params.setHandsetInternalId(tDevice.getHandsetInternalId());
            params.setOsMajorVersion(tDevice.getOsMajorVersion());
            if (tDevice.isSetDeviceType()) {
                params.setDeviceType(tDevice.getDeviceType());
            }
            if (tDevice.isSetModelName()) {
                params.setDeviceModel(tDevice.getModelName());
            }
            if (tDevice.isSetManufacturerName()) {
                params.setDeviceMake(tDevice.getManufacturerName());
            }
            if (tDevice.isSetLocale()) {
                params.setLanguage(tDevice.getLocale());
            }
            if (tDevice.isSetDisplayName()) {
                params.setHandsetName(tDevice.getDisplayName());
            }
            if (tDevice.isSetDerivedDensity()) {
                params.setDerivedDeviceDensity(tDevice.getDerivedDensity());
            }
        }
    }

    private void setIntegrationDetails(final AdPoolRequest tObject, final SASRequestParameters params) {
        if (tObject.isSetIntegrationDetails()) {
            final IntegrationDetails tIntDetails = tObject.integrationDetails;
            params.setRqIframe(tIntDetails.iFrameId);
            if (tIntDetails.isSetAdCodeType()) {
                params.setAdcode(tIntDetails.adCodeType.toString());
            }
            params.setSdkVersion(getSdkVersion(tIntDetails.integrationType, tIntDetails.integrationVersion));
            params.setAdcode(getAdCode(tIntDetails.integrationType));
        }
    }

    private void setSiteObject(final AdPoolRequest tObject, final SASRequestParameters params, final int dst) {
        if (tObject.isSetSite()) {
            final Site tSite = tObject.site;
            params.setSiteId(tSite.siteId);
            params.setSiteIncId(tSite.siteIncId);
            params.setAppUrl(tSite.siteUrl);
            params.setPubId(tSite.publisherId);
            final boolean isApp = tSite.isSetInventoryType() && tSite.inventoryType == InventoryType.APP;
            params.setSource(isApp ? GlobalConstant.APP : GlobalConstant.WAP);
            final SiteTemplateSettings sts = tSite.siteTemplateSettings;
            if (sts != null) {
                // Set CAU
                final Set<Long> cauMetaDataSet = new HashSet<Long>();
                if (CollectionUtils.isNotEmpty(sts.getCustomAdUnitStableList())) {
                    cauMetaDataSet.addAll(sts.getCustomAdUnitStableList());
                }
                if (CollectionUtils.isNotEmpty(sts.getCustomAdUnitExperimentList())) {
                    cauMetaDataSet.addAll(sts.getCustomAdUnitExperimentList());
                }
                params.setCauMetadataSet(cauMetaDataSet);

                // Set CT
                final Set<Long> customTemplateSet = new HashSet<Long>();
                if (CollectionUtils.isNotEmpty(sts.getCustomTemplateStableList())) {
                    customTemplateSet.addAll(sts.getCustomTemplateStableList());
                }
                if (CollectionUtils.isNotEmpty(sts.getCustomTemplateExperimentList())) {
                    customTemplateSet.addAll(sts.getCustomTemplateExperimentList());
                }
                params.setCustomTemplateSet(customTemplateSet);
            }

            if (CasConfigUtil.repositoryHelper != null) {
                params.setWapSiteUACEntity(CasConfigUtil.repositoryHelper.queryWapSiteUACRepository(tSite.siteId));
                params.setSiteEcpmEntity(CasConfigUtil.repositoryHelper.querySiteEcpmRepository(tSite.siteId,
                        tObject.geo.countryId, (int) tObject.device.osId));
            }
            params.setSiteContentType(
                    tSite.isSetSiteContentType() ? tSite.getSiteContentType() : ContentType.FAMILY_SAFE);
            params.setCategories(convertIntToLong(tSite.siteTaxonomies));

            final DemandSourceType dstEnum = DemandSourceType.findByValue(dst);
            double ecpmFloor = Math.max(tSite.ecpmFloor, tSite.cpmFloor);
            if (GlobalConstant.MIN_BID_FLOOR >= ecpmFloor) {
                ecpmFloor = GlobalConstant.MIN_BID_FLOOR;
                InspectorStats.incrementStatCount(AUCTION_STATS, dstEnum + BID_FLOOR_TOO_LOW);
            }
            params.setSiteFloor(ecpmFloor);

            final double marketRate = tObject.guidanceBid * 1.0 / Math.pow(10, 6);
            if (marketRate <= 0.0) {
                InspectorStats.incrementStatCount(AUCTION_STATS, dstEnum + BID_GUIDANCE_ABSENT);
            }
            if (marketRate <= ecpmFloor) {
                InspectorStats.incrementStatCount(AUCTION_STATS, dstEnum + BID_GUIDANCE_LESS_OR_EQUAL_TO_FLOOR);
            }
            params.setMarketRate(marketRate);

            // Fill params for Pub Control - Supported Ad Types.
            List<AdTypeEnum> pubControlSupportedAdTypes = new ArrayList<>();
            if (tSite.isSetEnrichedSiteAllowedMediaAttributes()) {
                for (final int adAttribVal : tSite.getEnrichedSiteAllowedMediaAttributes()) {
                    if (adAttribVal == AdAttributeType.VIDEO.getValue()) {
                        pubControlSupportedAdTypes.add(AdTypeEnum.VIDEO);
                    } else if (adAttribVal == AdAttributeType.DEFAULT.getValue()) {
                        pubControlSupportedAdTypes.add(AdTypeEnum.BANNER);
                    } // Ignore other fields which are not relevant to us.
                }
            }
            // If we don't get any value, set default values.
            if (pubControlSupportedAdTypes.isEmpty()) {
                pubControlSupportedAdTypes = DEFAULT_PUB_CONTROL_SUPPORTED_AD_TYPES;
            }
            params.setPubControlSupportedAdTypes(pubControlSupportedAdTypes);
            if (tSite.isSetRewarded()) {
                params.setRewardedVideo(tSite.isRewarded());
            }

            // Fill params for pub control - Media preferences json.

            params.setPubControlPreferencesJson(
                    tSite.isSetMediaPreferences() ? tSite.mediaPreferences : DEFAULT_PUB_CONTROL_MEDIA_PREFERENCES);
        }
    }

    protected String getPostalCode(final Set<Integer> postalCodes) {
        final Integer zipId =
                null != postalCodes && postalCodes.iterator().hasNext() ? postalCodes.iterator().next() : null;
        if (zipId != null) {
            final GeoZipEntity geoZipEntity = CasConfigUtil.repositoryHelper.queryGeoZipRepository(zipId);
            // There are DUMMY string values in geo_zip.zipcode on wap_prod_adserve.
            // The below isNumber check is a hack to avoid sending dummy values.
            if (null != geoZipEntity && NumberUtils.isNumber(geoZipEntity.getZipCode())) {
                return geoZipEntity.getZipCode();
            }
        }
        return null;
    }

    private String getResponseFormat(final ResponseFormat rqFormat) {
        final String rFormat = "html";
        if (null == rqFormat) {
            return rFormat;
        }
        return rqFormat.name().toLowerCase();
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
        final List<Long> longList = new ArrayList<>();
        for (final Integer obj : intList) {
            longList.add(Long.valueOf(obj));
        }
        return longList;
    }

    private Map<String, String> getUserIdMap(final Map<UidType, String> uidMap) {
        final Map<String, String> userIdMap = new HashMap<>();
        for (final Entry<UidType, String> entry : uidMap.entrySet()) {
            final UidType uidType = entry.getKey();
            userIdMap.put(uidType.toString().toUpperCase(), entry.getValue());
        }
        return userIdMap;
    }

    private void setUserIdParams(final CasInternalRequestParameters parameter, final UidParams uidParams) {
        final Map<UidType, String> uidMap =
                uidParams.isSetRawUidValues() ? uidParams.getRawUidValues() : new HashMap<>();
        if (uidParams.isSetLimitIOSAdTracking()) {
            parameter.setTrackingAllowed(!uidParams.isLimitIOSAdTracking());
        }
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

        LOG.debug("CasInternalParams are {}", parameter);
    }

    public String MD5(final String md5) {
        try {
            final MessageDigest md = MessageDigest.getInstance(GlobalConstant.MD5);
            final byte[] array = md.digest(md5.getBytes(CharsetUtil.UTF_8));
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

    public List<Short> getValidSlotList(final List<Short> selectedSlots, final boolean isIX) {
        if (selectedSlots == null) {
            LOG.info("Emply selectedSlots received by CAS !!!");
            return Collections.emptyList();
        }
        final List<Short> validSlots = new ArrayList<>();
        for (final Short slotId : selectedSlots) {
            final boolean toAdd = isIX
                    ? SlotSizeMapping.isIXSupportedSlot(slotId)
                    : CasConfigUtil.repositoryHelper.querySlotSizeMapRepository(slotId) != null;
            if (toAdd) {
                validSlots.add(slotId);
            }
        }
        return validSlots;
    }

}
