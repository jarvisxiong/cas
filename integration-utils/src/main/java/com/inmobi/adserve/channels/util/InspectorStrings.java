package com.inmobi.adserve.channels.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Please consult https://github.corp.inmobi.com/channel-adserve/nagios-monitoring before modifying/removing any values
 * as this may break Alerting and existing data funnels.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InspectorStrings {

    public static final String TOTAL_REQUESTS = "TotalRequests";
    public static final String RULE_ENGINE_REQUESTS = "RuleEngineRequests";
    public static final String BACK_FILL_REQUESTS = "BackFillRequests";
    public static final String IX_REQUESTS = "IXRequests";
    public static final String TOTAL_NATIVE_REQUESTS = "TotalNativeRequests";
    public static final String TOTAL_NATIVE_RESPONSES = "TotalNativeResponses";
    public static final String NON_AD_REQUESTS = "NonAdRequests";
    public static final String TOTAL_INVOCATIONS = "TotalInvocations";
    public static final String SUCCESSFUL_CONFIGURE = "SuccessfulConfigure";
    public static final String TOTAL_NO_FILLS = "TotalNoFills";
    public static final String TOTAL_FILLS = "TotalFills";
    public static final String RULE_ENGINE_FILLS = "RuleEngineFills";
    public static final String IX_FILLS = "IXFills";
    public static final String DCP_FILLS = "DCPFills";
    public static final String LATENCY = "Latency";
    public static final String CONNECTION_LATENCY = "ConnectionLatency";
    public static final String COUNT = "Count";
    public static final String TOTAL_TIMEOUT = "TotalTimeout";
    public static final String CONNECTION_TIMEOUT = "ConnectionTimeout";
    public static final String TOTAL_TERMINATE = "TotalTerminate";
    public static final String SUCCESSFUL_REQUESTS = "success";
    public static final String JSON_PARSING_ERROR = "Terminated_JsonError";
    public static final String THRIFT_PARSING_ERROR = "Terminated_ThriftError";
    public static final String THRIFT_PARSING_ERROR_EMPTY_ADPOOLREQUEST = "Terminated_ThriftError.EmptyAdPoolRequest";
    public static final String PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse";
    public static final String BANNER_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse(Banner)";
    public static final String NATIVE_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse(Native)";
    public static final String VIDEO_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse(Video)";
    public static final String CAU_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse(CAU)";
    public static final String NATIVE_VM_TEMPLATE_ERROR = "NativeVMTemplateError";
    public static final String PROCESSING_ERROR = "Terminated_ServerError";
    public static final String MISSING_SITE_ID = "Terminated_NoSite";
    public static final String CLIENT_TIMER_LATENCY = "ClientTimerLatency";
    public static final String CAS_TIMEOUT_HANDLER_LATENCY = "CasTimeoutHandlerLatency";
    public static final String TIMER_LATENCY = "timerLatency";
    public static final String TIMEOUT_EXCEPTION = "TimeoutException";
    public static final String IO_EXCEPTION = "IOException";
    public static final String INCOMPATIBLE_SITE_TYPE = "Terminated_IncompatibleSite";
    public static final String PERCENT_ROLL_OUT = "PercentRollout";
    public static final String NO_MATCH_SEGMENT_LATENCY = "NoMatchSegmentLatency";
    public static final String NO_MATCH_SEGMENT_COUNT = "NoMatchSegmentCount";
    public static final String DROPPED_IN_IMPRESSION_FILTER = "DroppedInImpressionFilter";
    public static final String DROPPED_IN_FAILURE_TROTTLER_FILTER = "DroppedInFailureTrottlerFilter";
    public static final String NUMBER_OF_TIMES_CIRCUIT_OPENED = "NumberOfTimesCircuitOpened";
    public static final String DROPPED_IN_PROPERTY_VIOLATION_FILTER = "DroppedInPropertyViolationFilter";
    public static final String DROPPED_IN_BURN_FILTER = "DroppedInBurnFilter";
    public static final String DROPPED_IN_REQUEST_CAP_FILTER = "DroppedInRequestCapFilter";
    public static final String DROPPED_IN_SEGMENT_PER_REQUEST_FILTER = "DroppedInSegmentPerRequestFilter";
    public static final String TOTAL_MATCHED_SEGMENTS = "TotalMatchedSegments";
    public static final String TOTAL_SELECTED_SEGMENTS = "TotalSelectedSegments";
    public static final String LOW_SDK_VERSION = "LowSdkVersion";
    public static final String SERVER_IMPRESSION = "Impression";
    public static final String CHANNEL_EXCEPTION = "ChannelException";
    public static final String DROPPED_IN_UDID_FILTER = "DroppedInUdidFilter";
    public static final String DROPPED_IN_LAT_LONG_FILTER = "DroppedInLatLongFilter";
    public static final String DROPPED_IN_ZIPCODE_FILTER = "DroppedInZipcodeFilter";
    public static final String DROPPED_IN_RICH_MEDIA_FILTER = "DroppedInRichMediaFilter";
    public static final String DROPPED_IN_ONLY_INTERSTITIAL_FILTER = "DroppedInOnlyInterstitialFilter";
    public static final String DROPPED_IN_ONLY_NON_INTERSTITIAL_FILTER = "DroppedInOnlyNonInterstitialFilter";
    public static final String DROPPED_IN_ADVERTISER_EXCLUSION_FILTER = "DroppedinAdvertiserExclusionFilter";
    public static final String DROPPED_IN_SITE_EXCLUSION_FILTER = "DroppedinSiteExclusionFilter";
    public static final String DROPPED_IN_HANDSET_TARGETING_FILTER = "DroppedinHandsetTargetingFilter";
    public static final String DROPPED_IN_AUTOMATION_FRAMEWORK_FILTER = "DroppedinAutomationFrameworkFilter";
    public static final String DROPPED_IN_AD_TYPE_TARGETING_FILTER = "DroppedInAdTypeTargetingFilter";
    public static final String DROPPED_AS_UNKNOWN_ADGROUP_AD_TYPE =
        "DroppedInAdTypeTargetingFilter.UnknownAdTypeFormat";
    public static final String DROPPED_IN_PRICING_ENGINE_FILTER = "DroppedinPricingEngineFilter";
    public static final String DROPPED_IN_TOD_FILTER = "DroppedInTODFilter";
    public static final String SITE_FEEDBACK_CACHE_HIT = "SiteFeedbackCacheHit";
    public static final String SITE_FEEDBACK_CACHE_MISS = "SiteFeedbackCacheMiss";
    public static final String SITE_FEEDBACK_LATENCY = "SiteFeedbackLatency";
    public static final String SITE_FEEDBACK_REQUESTS_TO_AEROSPIKE = "SiteFeedbackRequestsToAerospike";
    public static final String SITE_FEEDBACK_FAILED_TO_LOAD_FROM_AEROSPIKE = "SiteFeedbackFailedToLoadFromAerospike";
    public static final String MISSING_CATEGORY = "MissingCategory";

    public static final String IMEI_CACHE_MISS = "IMEICacheMiss";
    public static final String IMEI_CACHE_HIT = "IMEICacheHit";
    public static final String IMEI_REQUESTS_TO_AEROSPIKE = "IMEIRequestsToAerospike";
    public static final String IMEI_FAILED_TO_LOAD_FROM_AEROSPIKE = "IMEIFailedToLoadFromAerospike";
    public static final String IMEI_LATENCY = "IMEILatency";
    public static final String IMEI_MATCH = "IMEI-MATCH-UidO1";

    public static final String IMEI = "Imei";
    public static final String IMEI_IN_IX_COUNT = "ImeiInIxCount";
    public static final String IMEI_IN_RTBD_COUNT = "ImeiInRtbdCount";
    public static final String IMEI_IN_DCP_COUNT = "ImeiInDcpCount";

    public static final String DROPPED_IN_RTB_BALANCE_FILTER = "DroppedInRtbBalanceFilter";
    public static final String DROPPED_IN_RTB_BID_FLOOR_FILTER = "DroppedInRtbBidFloorFilter";
    public static final String DROPPED_IN_DEAL_FLOOR_FILTER = "DroppedInDealFloorFilter";
    public static final String DROPPED_IN_RTB_AUCTION_ID_MIS_MATCH_FILTER = "DroppedInRtbAuctionIdMisMatchFilter";
    public static final String DROPPED_IN_RTB_SEATID_MIS_MATCH_FILTER = "DroppedInRtbSeatidMisMatchFilter";
    public static final String DROPPED_IN_RTB_IMPRESSION_ID_MIS_MATCH_FILTER = "DroppedInRtbImpressionIdMisMatchFilter";
    public static final String DROPPED_IN_CREATIVE_ID_MISSING_FILTER = "DroppedInCreativeIdMissingFilter";
    public static final String DROPPED_IN_SAMPLE_IMAGE_URL_MISSING_FILTER = "DroppedInSampleImageUrlMissingFilter";
    public static final String DROPPED_IN_ADVERTISER_DOMAINS_MISSING_FILTER = "DroppedInAdvertiserDomainsFilter";
    public static final String DROPPED_IN_CREATIVE_ATTRIBUTES_MISSING_FILTER = "DroppedInCreativeAttributesFilter";
    public static final String DROPPED_IN_CREATIVE_VALIDATOR_FILTER = "DroppedInCreativeValidatorFilter";
    public static final String DROPPED_IN_AUCTION_IX_IMPRESSION_ID_FILTER = "DroppedInAuctionIxImpressionIdFilter";
    public static final String DROPPED_IN_ACCOUNT_SEGMENT_FILTER = "DroppedInAccountSegmentFilter";
    public static final String DROPPED_IN_SUPPLY_DEMAND_CLASSIFICATION_FILTER =
        "DroppedInSupplyDemandClassificationFilter";
    public static final String DROPPED_IN_RTB_CURRENCY_NOT_SUPPORTED_FILTER = "DroppedInRtbCurrencyNotSupportedFilter";
    public static final String DROPPED_IN_INVALID_DETAILS_FILTER = "DroppedInInvalidDetailsFilter";
    public static final String DROPPED_IN_BANNER_NOT_ALLOWED_FILTER = "DroppedInBannerNotAllowedFilter";
    // public static final String DROPPED_CUSTOM_TEMPLATE_NOT_ALLOWED_FILTER = "DroppedCustomTemplateNotAllowedFilter";
    public static final String DROPPED_IN_PARTNER_COUNT_FILTER = "DroppedInPartnerCountFilter";
    public static final String DROPPED_IN_DAILY_IMP_COUNT_FILTER = "DroppedInDailyImpressionCountFilter";
    public static final String DROPPED_IN_INVALID_SLOT_REQUEST_FILTER = "DroppedInInvalidSlotRequestFilter";
    public static final String IX_SENT_AS_TRANSPARENT = "IXSentAsTransparent";
    public static final String IX_SENT_AS_BLIND = "IXSentAsBlind";
    public static final String IX_ZONE_ID_NOT_PRESENT = "IXZoneIdNotPresent";
    public static final String IX_SITE_ID_NOT_PRESENT = "IXSiteIdNotPresent";
    public static final String INVALID_ADV_ID = "NoAdvertiserId";
    public static final String UNKNOWN_ADV_ID = "ADFromUnknownAdvId";
    public static final String DROPPED_INVALID_DSP_ID = "DroppedDueToInvalidDspId";
    public static final String IX_PACKAGE_MATCH_LATENCY = "IxPackageMatchLatency";
    public static final String IX_DEAL_NON_EXISTING = "IxDealNonExisting";
    public static final String TOTAL_DEAL_REQUESTS = "TotalDealRequests";
    public static final String TOTAL_DEAL_RESPONSES = "TotalDealResponses";
    public static final String TOTAL_VALID_SPROUT_RESPONSES = "TotalValidSproutResponses";
    public static final String TOTAL_RICH_MEDIA_REQUESTS = "TotalRichMediaRequests";
    public static final String TOTAL_UMP_CAU_REQUESTS = "TotalUMPCAURequests";
    public static final String TOTAL_CAU_REQUESTS = "TotalCAURequests";
    public static final String TOTAL_CAU_RESPONSES = "TotalCAUResponses";
    public static final String DROPPED_AS_SPROUT_ADS_ARE_NOT_SUPPORTED = "DroppedAsSproutAdsAreNotSupportedOn";
    public static final String TOTAL_VIDEO_REQUESTS = "TotalVideoRequests";
    public static final String TOTAL_VAST_VIDEO_REQUESTS = "TotalVASTVideoRequests";
    public static final String TOTAL_REWARDED_VAST_VIDEO_REQUESTS = "TotalRewardedVASTVideoRequests";
    public static final String TOTAL_VIDEO_RESPONSES = "TotalVideoResponses";
    public static final String TOTAL_VAST_VIDEO_RESPONSES = "TotalVASTVideoResponses";
    public static final String TOTAL_REWARDED_VAST_VIDEO_RESPONSES = "TotalRewardedVASTVideoResponses";
    public static final String TOTAL_RESPONSES_WITH_THIRD_PARTY_VIEWABILITY_TRACKERS = "TotalResponsesWithThirdPartyViewabilityTrackers";
    public static final String TOTAL_VIEWABILITY_RESPONSES = "TotalViewabilityResponses";
    public static final String TOTAL_ALT_SLOT_SIZE_REQUESTS = "TotalAltSizeRequests";
    public static final String TOTAL_ALT_SLOT_SIZE_RESPONSES = "TotalAltSizeResponses";
    public static final String INVALID_VIDEO_RESPONSE_COUNT = "InvalidVideoResponseCount";
    public static final String INVALID_MEDIA_PREFERENCES_JSON = "InvalidMediaPreferencesJson";
    public static final String DROPPED_AS_MRAID_PATH_NOT_FOUND = "DroppedAsMraidPathWasMissing-";
    public static final String UNCAUGHT_EXCEPTIONS = "UncaughtExceptions";
    public static final String TRACKER_BEING_FETCHED_BEFORE_GENERATION = "trackerBeingFetchedBeforeGeneration";
    public static final String AGENCY_ID_MISSING_IN_REBATE_DEAL_RESPONSE = "AgencyRebateDealResponse.AgencyIdMissing";
    public static final String AGENCY_ID_MISMATCH_IN_REBATE_DEAL_RESPONSE = "AgencyRebateDealResponse.AgencyIdMismatch";
    public static final String AGENCY_ID_CANNOT_BE_DETERMINED_IN_REBATE_DEAL_RESPONSE =
        "AgencyRebateDealResponse.AgencyIdIndeterminate";
    public static final String TOTAL_AGENCY_REBATE_DEAL_RESPONSES = "TotalAgencyRebateDealResponses";
    public static final String DROPPED_IN_SECURE_NOT_SUPPORTED_FILTER = "DroppedInSecureNotSupportedFilter";

    // Request metrics
    public static final String ADPOOL_REQUEST_STATS = "AdPoolRequestStats";
    public static final String PUB_CONTROLS_ALSO_CONTAINS_BANNER_FOR_REWARDED_PLACEMENT = "PubControlsAlsoContainsBannerForRewardedPlacement(Approx)";

    // Auction Level Stats
    public static final String AUCTION_STATS = "AuctionStats";
    // public static final String NO_MATCH_SEGMENT_STATS = "DetailedNoMatchSegmentStats";
    public static final String CLEARING_PRICE_WON = "ClearingPriceWonOverSecondHighestBid";
    public static final String BID_GUIDANCE_ABSENT = "-BidGuidanceAbsent";
    public static final String BID_GUIDANCE_EQUAL_TO_UMP_FLOOR = "-BidGuidanceIsEqualToUmpFloor";
    public static final String BID_FLOOR_TOO_LOW = "-BidFloorTooLow";
    public static final String MULTI_FORMAT_AUCTIONS_TOTAL = "IX-MultiFormatAuctions.TotalAuctions";
    public static final String MULTI_FORMAT_AUCTIONS_NO_TRUMP = "IX-MultiFormatAuctions.TotalAuctionsWithNoTrumpDeals";
    public static final String MULTI_FORMAT_AUCTIONS_SINGLE_TRUMP =
        "IX-MultiFormatAuctions.TotalAuctionsWithOnlyOneTrumpDeal";
    public static final String MULTI_FORMAT_AUCTIONS_MULTIPLE_TRUMP =
        "IX-MultiFormatAuctions.TotalAuctionsWithMultipleTrumpDeals";
    public static final String MULTI_FORMAT_AUCTIONS_VAST_VIDEO_WINS = "IX-MultiFormatAuctions.TotalVastVideoWins";
    public static final String MULTI_FORMAT_AUCTIONS_STATIC_WINS = "IX-MultiFormatAuctions.TotalStaticWins";

    // Total Requests to RP = IX-TotalSingleFormatRequests + IX-TotalMultiFormatRequests * fanout (=2)
    public static final String TOTAL_MULTI_FORMAT_REQUESTS = "IX-TotalMultiFormatRequests";
    public static final String TOTAL_SINGLE_FORMAT_REQUESTS = "IX-TotalSingleFormatRequests";

    // Package Filter Level Stats
    public static final String PACKAGE_FILTER_STATS = "PackageFilterStats";
    public static final String IX_PACKAGE_THRESHOLD_EXCEEDED_COUNT = "IXPackageThresholdExceededCount";
    public static final String DROPPED_IN_PACKAGE_DMP_FILTER = "DroppedInPackageDMPFilter";
    public static final String DROPPED_IN_PACKAGE_AD_TYPE_TARGETING_FILTER = "DroppedInPackageAdTypeTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_MANUF_MODEL_FILTER = "DroppedInPackageManufModelTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_OS_VERSION_FILTER = "DroppedInPackageOSVersionTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_GEO_REGION_FILTER = "DroppedInPackageGeoRegionTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_SEGMENT_SUBSET_FILTER = "DroppedInPackageSegmentSubsetFilter";
    public static final String DROPPED_IN_PACKAGE_LANGUAGE_TARGETING_FILTER = "DroppedInPackageLanguageTargetingFilter";



    // More than one segments were present during the IX/Hosted auctions
    // (This will never be incremented as extra segments will be dropped in partner count filter)
    public static final String INVALID_AUCTION = "InvalidAuction";
    public static final String DROPPED_IN_PACKAGE_SDK_VERSION_FILTER = "DroppedInPackageSDKVersionTargetingFilter";

    // IX Response Object Status Code Strings
    public static final String IX_INVALID_REQUEST = "NO_AD.InvalidRequest";
    public static final String IX_NO_MATCH = "NO_AD.NoAdMatchedCriteria";
    public static final String IX_REFERRER_NOT_ALLOWED = "NO_AD.ReferrerNotAllowed";
    public static final String IX_INVENTORY_IDENTIFIER_INVALID = "NO_AD.InvalidInventoryIdentifiers";
    public static final String IX_SUSPECTED_SPIDER = "NO_AD.SuspectedSpider";
    public static final String IX_SUSPECTED_BOTNET = "NO_AD.SuspectedBotnet";
    public static final String IX_REFERRER_BLOCKED = "NO_AD.ReferrerBlocked";
    public static final String IX_NOT_AUTHORIZED = "NO_AD.NotAuthorized";
    public static final String IX_PROXY_BID_WINS = "NO_AD.ProxyBidWins";
    public static final String IX_OTHER_ERRORS = "NO_AD.OtherErrors";

    // TestStrings
    public static final String LATENCY_FOR_MEASURING_AT_POINT_ = "LatencyForMeasuringAtPoint_";

    // IP repository Strings
    public static final String URI_SYNTAX_EXCEPTION = "URISyntaxException";
    public static final String UNKNOWN_HOST_EXCEPTION = "UnknownHostException";
    public static final String NULL_HOST_NAME = "NullHostName";
    public static final String NULL_IP_ADDRESS = "NullIPAddress";

}
