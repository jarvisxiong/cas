package com.inmobi.adserve.channels.api.trackers;

import static com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerHelper.appendSeparator;
import static com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerHelper.getCarrierIdBase36;
import static com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerHelper.getEncodedJson;
import static com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerHelper.getIdBase36;
import static com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerHelper.getIntegrationVersionStr;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.util.Utils.CryptoHashGenerator;
import com.google.gson.Gson;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.types.eventserver.ImpressionInfo;

import lombok.Getter;
import lombok.Setter;

import lombok.Builder;

/**
 * Default Lazy InmobiAdTracker. Previously, ClickUrlMakerV6.
 *
 * click and beacon urls are generated lazily on the first invocation of getBeaconUrl() or getClickUrl()
 */
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class DefaultLazyInmobiAdTracker implements InmobiAdTracker {
    private static final Logger LOG = LoggerFactory.getLogger(InmobiAdTracker.class);
    private static final String DEFAULT_UDID_VALUE = "x";
    private static final String URLVERSIONINITSTR = "/C";
    private static final String URLMARKERCPC = "/t";
    private static final String URLMARKERCPM = "/b";
    private static final String DEFAULTIMSDK = "-1";
    private static final String CLICK = GlobalConstant.ONE;
    private static final String BEACON = GlobalConstant.ZERO;
    private static final String IS_TEST = GlobalConstant.ONE;
    private static final String IS_NOT_TEST = GlobalConstant.ZERO;
    private static final String RTB_SUPPLY = "rtb";
    private static final String NON_RTB_SUPPLY = "nw";
    private static final Long CLICK_URL_HASHING_SECRET_KEY_VERSION = (long) 1;
    private static final String CLICK_URL_HASHING_SECRET_KEY_VERSION_BASE_36 =
            getIdBase36(CLICK_URL_HASHING_SECRET_KEY_VERSION);
    private static final Long CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION = (long) 2;
    private static final String CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION_BASE_36 =
            getIdBase36(CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION);
    private static final String DEFAULT_UNUSED_PARAMETER = "-1";
    private static final String DEFAULT_BUNDLE_ID = "x";

    private final Map<String, String> udIdVal;
    private final String testCryptoSecretKey;
    private final String cryptoSecretKey;
    private final String rmBeaconURLPrefix;
    private final String imageBeaconURLPrefix;
    private final Integer siteSegmentId;
    private final Integer placementSegmentId;
    private final String clickURLPrefix;
    private final String imSdk; // Never Set
    private final String impressionId;
    private final boolean isRmAd;
    private final Boolean isBillableDemog;
    private final boolean isCPC;
    private final Long siteIncId;
    private final Long handsetInternalId;
    private final Long ipFileVersion;
    private final int carrierId;
    private final int countryId;
    private final String gender;
    private final int age;
    private final int location;
    private final boolean testMode;
    private final boolean isBeaconEnabledOnSite;
    private final boolean imageBeaconFlag;
    private final String tierInfo;
    private final boolean isTestRequest;
    private final String latlonval;
    private final boolean isRtbSite;
    private final String creativeId;
    private final String budgetBucketId;
    private final String dst;
    private final Long placementId;
    private IntegrationDetails integrationDetails;
    private String appBundleId;
    private String normalizedUserId;
    private RequestedAdType requestedAdType;

    // State
    private boolean trackersHaveBeenGenerated = false;

    // Trackers
    private String beaconUrl;
    private String clickUrl;

    private void generateInmobiAdTrackers() {
        trackersHaveBeenGenerated = true;
        final StringBuilder adUrlSuffix = new StringBuilder(150);
        // 1st URL component: url format version info
        adUrlSuffix.append(URLVERSIONINITSTR);
        // 2nd URL component: CPC/CPM information
        if (isCPC) {
            adUrlSuffix.append(URLMARKERCPC);
        } else {
            adUrlSuffix.append(URLMARKERCPM);
        }
        // 3rd URL component: site inc id
        if (null == siteIncId) {
            LOG.debug("Site inc id is null so returning");
            return;
        }
        adUrlSuffix.append(appendSeparator(getIdBase36(siteIncId)));
        // 4th URL Component: handset device id
        if (null == handsetInternalId) {
            LOG.debug("handsetInternaleId is null so returning");
            return;
        }
        adUrlSuffix.append(appendSeparator(getIdBase36(handsetInternalId)));
        // 5th URL Component: ip file version
        if (null == ipFileVersion) {
            LOG.debug("ipFileVersion is null so returning");
            return;
        }
        adUrlSuffix.append(appendSeparator(getIdBase36(ipFileVersion)));
        // 6th URL Component: country id
        adUrlSuffix.append(appendSeparator(getIdBase36(countryId)));
        // 7th URL Component: ccid
        adUrlSuffix.append(appendSeparator(getCarrierIdBase36(carrierId, countryId)));
        // 8th URL Component: gender
        adUrlSuffix.append(appendSeparator(gender));
        // 9th URL Component: age
        adUrlSuffix.append(appendSeparator(getIdBase36(age)));
        // 10th URL Component: location
        adUrlSuffix.append(appendSeparator(getIdBase36(location)));
        // 11th URL Component: Billable Click
        String billable;
        if (isBillableDemog) {
            billable = GlobalConstant.ONE;
        } else {
            billable = GlobalConstant.ZERO;
        }
        adUrlSuffix.append(appendSeparator(billable));
        // 12th URL Component: udid or odin1. Based on PI311
        if (null == udIdVal || udIdVal.isEmpty()) {
            LOG.debug("udIdVal is null or empty so using default value");
            adUrlSuffix.append(appendSeparator(DEFAULT_UDID_VALUE));
        } else {
            adUrlSuffix.append(appendSeparator(getEncodedJson(udIdVal)));
        }
        // 13th impression id
        if (null == impressionId) {
            LOG.debug("impressionId is null so returning");
            return;
        }
        adUrlSuffix.append(appendSeparator(impressionId));
        // 14th imsdk, 0 for web and -1 for other ad formats)
        if (!StringUtils.isEmpty(imSdk)) {
            adUrlSuffix.append(appendSeparator(imSdk));
        } else {
            adUrlSuffix.append(appendSeparator(DEFAULTIMSDK));
        }

        // Creation of ImpressionInfo object (28th field)
        ImpressionInfo impInfo = new ImpressionInfo();

        // 15th siteSegmentId/placementSegmentId
        String segmentIdStr = Integer.toString(0);
        if (null != placementId && null != placementSegmentId) {
            segmentIdStr = getIdBase36(placementSegmentId);
            // Setting placementId in the ImpressionInfo object
            impInfo.setPlacementId(placementId);
        } else if (null != siteSegmentId) {
            segmentIdStr = getIdBase36(siteSegmentId);
        }
        adUrlSuffix.append(appendSeparator(segmentIdStr));

        // 16the field for tier info
        adUrlSuffix.append(appendSeparator(tierInfo));

        StringBuilder beaconUrlSuffix = new StringBuilder(150);
        // 17th field for event type <click or beacon>
        beaconUrlSuffix = beaconUrlSuffix.append(adUrlSuffix.toString());
        adUrlSuffix.append(appendSeparator(CLICK));
        beaconUrlSuffix.append(appendSeparator(BEACON));

        // 18th field for test mode
        if (isTestRequest) {
            adUrlSuffix.append(appendSeparator(IS_TEST));
            beaconUrlSuffix.append(appendSeparator(IS_TEST));
        } else {
            adUrlSuffix.append(appendSeparator(IS_NOT_TEST));
            beaconUrlSuffix.append(appendSeparator(IS_NOT_TEST));
        }

        // 19th URL Component: request's latlong
        adUrlSuffix.append(appendSeparator(latlonval));
        beaconUrlSuffix.append(appendSeparator(latlonval));

        // 20th URL Component: creative id
        adUrlSuffix.append(appendSeparator(creativeId));
        beaconUrlSuffix.append(appendSeparator(creativeId));

        // 21st URL Component: supply source
        if (isRtbSite) {
            adUrlSuffix.append(appendSeparator(RTB_SUPPLY));
            beaconUrlSuffix.append(appendSeparator(RTB_SUPPLY));
        } else {
            adUrlSuffix.append(appendSeparator(NON_RTB_SUPPLY));
            beaconUrlSuffix.append(appendSeparator(NON_RTB_SUPPLY));
        }

        // 22th budget bucket id
        adUrlSuffix.append(appendSeparator(budgetBucketId));
        beaconUrlSuffix.append(appendSeparator(budgetBucketId));

        // 23th dst
        adUrlSuffix.append(appendSeparator(dst));
        beaconUrlSuffix.append(appendSeparator(dst));

        // 24nd URL Component: integrationMethod -- not using it, hence setting it default value
        String integrationMethod = DEFAULT_UNUSED_PARAMETER;
        if (null != integrationDetails && integrationDetails.isSetIntegrationMethod()) {
            integrationMethod = integrationDetails.getIntegrationMethod().toString().toLowerCase()
                    .replace("_", StringUtils.EMPTY);
        }
        adUrlSuffix.append(appendSeparator(integrationMethod));
        beaconUrlSuffix.append(appendSeparator(integrationMethod));

        // 25nd URL Component: integrationVersion
        String integrationVersion = DEFAULT_UNUSED_PARAMETER;
        if (null != integrationDetails && integrationDetails.isSetIntegrationVersion()) {
            integrationVersion = String.valueOf(getIntegrationVersionStr(integrationDetails.getIntegrationVersion()));
        }
        adUrlSuffix.append(appendSeparator(integrationVersion));
        beaconUrlSuffix.append(appendSeparator(integrationVersion));

        // 26nd URL Component: tpName
        String tpName = DEFAULT_UNUSED_PARAMETER;
        if (null != integrationDetails && integrationDetails.isSetIntegrationThirdPartyName()) {
            tpName = StringUtils.substring(integrationDetails.getIntegrationThirdPartyName(), 0, 10);
        }
        adUrlSuffix.append(appendSeparator(tpName));
        beaconUrlSuffix.append(appendSeparator(tpName));

        // 27th URL Component: bundle id
        String bundleId = appBundleId;
        if (StringUtils.isBlank(bundleId)) {
            bundleId = DEFAULT_BUNDLE_ID;
        }
        String encodedBundleId = new String(Base64.encodeBase64(bundleId.getBytes()));
        String finalBundleId = encodedBundleId.replaceAll("\\+", "-").replaceAll("\\/", "_").replaceAll("=", "~");
        adUrlSuffix.append(appendSeparator(finalBundleId));
        beaconUrlSuffix.append(appendSeparator(finalBundleId));

        // 28th URL Component: encoded thrift serialized object with placementID, normalizedUserID and requestedAdType
        // placementId is set alongside the 15th field
        impInfo.setNormalizedUserId(normalizedUserId);
        if (null != requestedAdType) {
            impInfo.setRequestedAdType(requestedAdType.toString());
        }

        TSerializer serializer = new TSerializer(new TCompactProtocol.Factory());
        String encodedString = StringUtils.EMPTY;
        try {
            byte[] bytes = serializer.serialize(impInfo);
            encodedString = new String(Base64.encodeBase64URLSafe(bytes));
        } catch (TException e) {
            LOG.error("Error while serializing impressionInfo object", e);
        }


        adUrlSuffix.append(appendSeparator(encodedString));
        beaconUrlSuffix.append(appendSeparator(encodedString));

        // 29th and 30th URL Component: hash key version and url hash
        CryptoHashGenerator cryptoHashGenerator;
        if (testMode) {
            adUrlSuffix.append(appendSeparator(CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION_BASE_36));
            beaconUrlSuffix
                    .append(appendSeparator(CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION_BASE_36));
            cryptoHashGenerator = new CryptoHashGenerator(testCryptoSecretKey);
        } else {
            adUrlSuffix.append(appendSeparator(CLICK_URL_HASHING_SECRET_KEY_VERSION_BASE_36));
            beaconUrlSuffix.append(appendSeparator(CLICK_URL_HASHING_SECRET_KEY_VERSION_BASE_36));
            cryptoHashGenerator = new CryptoHashGenerator(cryptoSecretKey);
        }
        adUrlSuffix.append(appendSeparator(cryptoHashGenerator.generateHash(adUrlSuffix.toString())));
        beaconUrlSuffix.append(appendSeparator(cryptoHashGenerator.generateHash(beaconUrlSuffix.toString())));
        if (null != clickURLPrefix) {
            clickUrl = clickURLPrefix + adUrlSuffix.toString();
        }

        if (isRmAd) {
            if (null != rmBeaconURLPrefix) {
                beaconUrl = rmBeaconURLPrefix + beaconUrlSuffix.toString();
            }
            return;
        }

        if (imageBeaconFlag || isBeaconEnabledOnSite) {
            if (null != imageBeaconURLPrefix) {
                beaconUrl = imageBeaconURLPrefix + beaconUrlSuffix.toString();
            }
        }

        LOG.debug("Generated Click Url: {}", clickUrl);
        LOG.debug("Generated Beacon Url: {}", beaconUrl);
    }

    public String getClickUrl() {
        if (!trackersHaveBeenGenerated) {
            generateInmobiAdTrackers();
        }
        return clickUrl;
    }

    public String getBeaconUrl() {
        if (!trackersHaveBeenGenerated) {
            generateInmobiAdTrackers();
        }
        return beaconUrl;
    }

}
