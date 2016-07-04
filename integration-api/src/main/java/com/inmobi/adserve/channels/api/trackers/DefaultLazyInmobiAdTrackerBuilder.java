package com.inmobi.adserve.channels.api.trackers;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.ZERO;

import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.inmobi.adserve.channels.api.SASRequestParameters;


/**
 * Builder for the DefaultLazyInmobiAdTracker
 */
public class DefaultLazyInmobiAdTrackerBuilder extends InmobiAdTrackerBuilder {
    private final DefaultLazyInmobiAdTracker.Builder builder;

    // Config Constants
    private static String testCryptoSecretKey;
    private static String cryptoSecretKey;
    private static String rmBeaconURLPrefix;
    private static String clickURLPrefix;
    private static String clickSecureURLPrefix;
    private static String rmBeaconSecureURLPrefix;

    // Constants
    private static final Boolean IS_BILLABLE_DEMOG = false;
    private static final boolean TEST_MODE = false;
    private static final boolean IS_BEACON_ENABLED_ON_SITE = true;
    private static final boolean IMAGE_BEACON_FLAG = true;
    private static final boolean IS_TEST_REQUEST = false;
    private static final String BUDGET_BUCKET_ID = "101";
    private static final String TIER_INFO = "-1";

    @Inject
    public DefaultLazyInmobiAdTrackerBuilder(@Assisted final SASRequestParameters sasParams,
                                             @Assisted final String impressionId,
                                             @Assisted final boolean isCpc) {
        super(sasParams, impressionId, isCpc);
        builder = DefaultLazyInmobiAdTracker.newBuilder();
        buildHelper();
    }

    public static void init(Configuration clickmakerConfig) {
        cryptoSecretKey      = clickmakerConfig.getString("key.1.value");
        testCryptoSecretKey  = clickmakerConfig.getString("key.2.value");
        rmBeaconURLPrefix    = clickmakerConfig.getString("beaconURLPrefix");
        clickURLPrefix       = clickmakerConfig.getString("clickURLPrefix");
        clickSecureURLPrefix = clickmakerConfig.getString("clickSecureURLPrefix", "https://c2.w.inmobi.com/c.asm");
        rmBeaconSecureURLPrefix    = clickmakerConfig.getString("beaconSecureURLPrefix", "https://c2.w.inmobi.com/c.asm");
    }

    private final void buildHelper() {
        builder.impressionId(this.impressionId);
        builder.age(null != sasParams.getAge() ? Math.max(sasParams.getAge().intValue(), 0) : 0);
        builder.countryId(null != sasParams.getCountryId() ? sasParams.getCountryId().intValue() : 0);
        builder.location(null != sasParams.getState() ? sasParams.getState() : 0);
        builder.siteSegmentId(sasParams.getSiteSegmentId());
        builder.placementSegmentId(sasParams.getPlacementSegmentId());
        builder.gender(null != sasParams.getGender() ? sasParams.getGender() : "u");
        builder.isCPC(isCpc);
        builder.carrierId(sasParams.getCarrierId());
        builder.handsetInternalId(sasParams.getHandsetInternalId());
        builder.ipFileVersion(sasParams.getIpFileVersion().longValue());
        builder.siteIncId(sasParams.getSiteIncId());
        builder.udIdVal(sasParams.getTUidParams());
        builder.isRmAd(sasParams.isRichMedia());
        builder.latlonval(StringUtils.isEmpty(sasParams.getLatLong()) ? "x" : sasParams.getLatLong());
        builder.isRtbSite(sasParams.getSst() != 0);
        builder.dst(String.valueOf(sasParams.getDst() - 1));    // Dst-1
        builder.placementId(sasParams.getPlacementId());
        builder.integrationDetails(sasParams.getIntegrationDetails());
        builder.appBundleId(sasParams.getAppBundleId());
        builder.normalizedUserId(sasParams.getNormalizedUserId());
        builder.requestedAdType(sasParams.getRequestedAdType());

        // Config Constants
        builder.cryptoSecretKey(cryptoSecretKey);
        builder.testCryptoSecretKey(testCryptoSecretKey);
        final String beaconUrlPrefix = sasParams.isSecureRequest() ? rmBeaconSecureURLPrefix : rmBeaconURLPrefix;
        builder.rmBeaconURLPrefix(beaconUrlPrefix);
        builder.clickURLPrefix(sasParams.isSecureRequest() ?  clickSecureURLPrefix : clickURLPrefix);
        builder.imageBeaconURLPrefix(beaconUrlPrefix);

        // Constants
        builder.tierInfo(TIER_INFO);
        builder.creativeId(ZERO);
        builder.isBillableDemog(IS_BILLABLE_DEMOG);
        builder.imageBeaconFlag(IMAGE_BEACON_FLAG);
        builder.isBeaconEnabledOnSite(IS_BEACON_ENABLED_ON_SITE);
        builder.testMode(TEST_MODE);
        builder.isTestRequest(IS_TEST_REQUEST);
        builder.budgetBucketId(BUDGET_BUCKET_ID);
    }

    public void setAgencyRebatePercentage(Double agencyRebatePercentage) {
        builder.agencyRebatePercentage(agencyRebatePercentage);
    }

    public void setChargedBid(double originalBid) {
        if (originalBid > 0) {
            builder.chargedBid((long) (originalBid * Math.pow(10, 6)));
        }
    }

    public void setMatchedCsids(final Set<Integer> matchedCsidList) {
        builder.matchedCsids(matchedCsidList);
    }

    public void setEnrichmentCost(final Double dataVenderEnrichmentCost) {
        builder.enrichmentCost(dataVenderEnrichmentCost);
    }

    public void setNativeTemplateId(final Long nativeTemplateId) {
        builder.nativeTemplateId(nativeTemplateId);
    }

    @Override
    public DefaultLazyInmobiAdTracker buildInmobiAdTracker() {
        return builder.build();
    }
}
