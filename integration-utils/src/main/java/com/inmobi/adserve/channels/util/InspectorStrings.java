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
    public static final String IX_REQUEST_DROPPED_FOR_NAPP_SCORE_40 = "IXRequestDroppedForNappScore40";
    public static final String TOTAL_NATIVE_REQUESTS = "TotalNativeRequests";
    public static final String TOTAL_NATIVE_RESPONSES = "TotalNativeResponses";
    public static final String TOTAL_NATIVE_VIDEO_RESPONSES = "TotalNativeVideoResponses";
    public static final String TOTAL_MOVIEBOARD_RESPONSES = "TotalMovieBoardResponses";
    public static final String NON_AD_REQUESTS = "NonAdRequests";
    public static final String TOTAL_INVOCATIONS = "TotalInvocations";
    public static final String SUCCESSFUL_CONFIGURE = "SuccessfulConfigure";
    public static final String TOTAL_NO_FILLS = "TotalNoFills";
    public static final String TOTAL_FILLS = "TotalFills";
    public static final String RULE_ENGINE_FILLS = "RuleEngineFills";
    public static final String IX_FILLS = "IXFills";
    public static final String DCP_FILLS = "DCPFills";
    public static final String LATENCY = "Latency";
    public static final String COUNT = "Count";
    public static final String TOTAL_TIMEOUT = "TotalTimeout";
    public static final String CONNECTION_TIMEOUT = "ConnectionTimeout";
    public static final String TOTAL_TERMINATE = "TotalTerminate";
    public static final String SUCCESSFUL_REQUESTS = "success";
    public static final String MISSING_ADDITIONAL_PARAMS = "MissingAdditionalParam";

    // Request filters
    public static final String TERMINATED_REQUESTS = "TerminateRequests";
    public static final String JSON_PARSING_ERROR = "Terminated_JsonError";
    public static final String THRIFT_PARSING_ERROR = "Terminated_ThriftError";
    public static final String THRIFT_PARSING_ERROR_EMPTY_ADPOOLREQUEST = "Terminated_ThriftError.EmptyAdPoolRequest";
    public static final String PROCESSING_ERROR = "Terminated_ServerError";
    public static final String MISSING_SITE_ID = "Terminated_NoSiteId";
    public static final String INCOMPATIBLE_SITE_TYPE = "Terminated_IncompatibleSite";
    public static final String BANNER_NOT_ALLOWED = "Terminated_BannerNotAllowedFilter";
    public static final String INVALID_SLOT_REQUEST = "Terminated_InInvalidSlotRequest";
    public static final String CHINA_MOBILE_TARGETING = "Terminated_ChinaMobileTargeting";
    public static final String NO_SUPPORTED_SLOTS = "Terminated_NoSupportedSlots";
    public static final String MISSING_CATEGORY = "Terminated_MissingCategory";
    public static final String LOW_SDK_VERSION = "Terminated_LowSdkVersion";
    public static final String NO_SAS_PARAMS = "Terminated_NoSASParams";
    public static final String MISSING_MRAID_PATH = "Terminated_NoMraidPath-";
    public static final String MISSING_SDK_VERSION = "Terminated_NoSDKVersion-";
    public static final String INVALID_SERVLET_REQUEST = "Terminated_InvalidServletRequest";

    public static final String PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse";
    public static final String RESPONSE_CONTRACT_NOT_HONOURED = "ResponseContractNotHonoured";
    public static final String BANNER_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse-Banner";
    public static final String NATIVE_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse-Native";
    public static final String VIDEO_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse-Video";
    public static final String PURE_VAST_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse-PureVast";
    public static final String CAU_PARSE_RESPONSE_EXCEPTION = "ExceptionInParseResponse-CAU";
    public static final String NATIVE_VM_TEMPLATE_ERROR = "NativeVMTemplateError";
    public static final String CLIENT_TIMER_LATENCY = "ClientTimerLatency";
    public static final String CAS_TIMEOUT_HANDLER_LATENCY = "CasTimeoutHandlerLatency";
    public static final String TIMER_LATENCY = "timerLatency";
    public static final String TIMEOUT_EXCEPTION = "TimeoutException";
    public static final String IO_EXCEPTION = "IOException";
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

    public static final String IMEI_CACHE_MISS = "IMEICacheMiss";
    public static final String IMEI_CACHE_HIT = "IMEICacheHit";
    public static final String IMEI_REQUESTS_TO_AEROSPIKE = "IMEIRequestsToAerospike";
    public static final String IMEI_FAILED_TO_LOAD_FROM_AEROSPIKE = "IMEIFailedToLoadFromAerospike";
    public static final String IMEI_LATENCY = "IMEILatency";
    public static final String IMEI_MATCH = "IMEI-MATCH-UidO1";

    public static final String IMEI = "Imei";
    public static final String IMEI_BEING_SENT_FOR = "ImeiBeingSentFor";

    public static final String DROPPED_IN_RTB_BALANCE_FILTER = "DroppedInRtbBalanceFilter";
    public static final String DROPPED_IN_RTB_BID_FLOOR_FILTER = "DroppedInRtbBidFloorFilter";
    public static final String DROPPED_IN_DEAL_FLOOR_FILTER = "DroppedInDealFloorFilter";
    public static final String POTENTIALLY_DROPPED_IN_DEAL_FLOOR_FILTER = "PotentiallyDroppedInDealFloorFilter";
    public static final String DROPPED_IN_RTB_AUCTION_ID_MIS_MATCH_FILTER = "DroppedInRtbAuctionIdMisMatchFilter";
    public static final String DROPPED_IN_RTB_SEATID_MIS_MATCH_FILTER = "DroppedInRtbSeatidMisMatchFilter";
    public static final String DROPPED_IN_RTB_IMPRESSION_ID_MIS_MATCH_FILTER = "DroppedInRtbImpressionIdMisMatchFilter";
    public static final String DROPPED_IN_CREATIVE_ID_MISSING_FILTER = "DroppedInCreativeIdMissingFilter";
    public static final String DROPPED_IN_CREATIVE_VALIDATOR_FILTER = "DroppedInCreativeValidatorFilter";
    public static final String DROPPED_IN_AUCTION_IX_IMPRESSION_ID_FILTER = "DroppedInAuctionIxImpressionIdFilter";
    public static final String DROPPED_IN_ACCOUNT_SEGMENT_FILTER = "DroppedInAccountSegmentFilter";
    public static final String DROPPED_IN_SUPPLY_DEMAND_CLASSIFICATION_FILTER =
            "DroppedInSupplyDemandClassificationFilter";
    public static final String DROPPED_IN_RTB_CURRENCY_NOT_SUPPORTED_FILTER = "DroppedInRtbCurrencyNotSupportedFilter";
    public static final String DROPPED_IN_INVALID_DETAILS_FILTER = "DroppedInInvalidDetailsFilter";
    public static final String DROPPED_IN_PARTNER_COUNT_FILTER = "DroppedInPartnerCountFilter";
    public static final String DROPPED_IN_DAILY_IMP_COUNT_FILTER = "DroppedInDailyImpressionCountFilter";
    public static final String IX_SENT_AS_TRANSPARENT = "IXSentAsTransparent";
    public static final String IX_SENT_AS_BLIND = "IXSentAsBlind";
    public static final String IX_ZONE_ID_NOT_PRESENT = "IXZoneIdNotPresent";
    public static final String IX_SITE_ID_NOT_PRESENT = "IXSiteIdNotPresent";
    public static final String UNKNOWN_ADV_ID = "ADFromUnknownAdvId";
    public static final String DROPPED_INVALID_DSP_ID = "DroppedDueToInvalidDspId";
    public static final String TOTAL_VALID_SPROUT_RESPONSES = "TotalValidSproutResponses";
    public static final String TOTAL_RICH_MEDIA_REQUESTS = "TotalRichMediaRequests";
    public static final String TOTAL_UMP_CAU_REQUESTS = "TotalUMPCAURequests";
    public static final String TOTAL_CAU_REQUESTS = "TotalCAURequests";
    public static final String TOTAL_CAU_RESPONSES = "TotalCAUResponses";
    public static final String DROPPED_AS_SPROUT_ADS_ARE_NOT_SUPPORTED = "DroppedAsSproutAdsAreNotSupportedOn";
    public static final String TOTAL_VIDEO_REQUESTS = "TotalVideoRequests";
    public static final String TOTAL_VAST_VIDEO_REQUESTS = "TotalVASTVideoRequests";
    public static final String TOTAL_PURE_VAST_REQUESTS = "TotalPureVASTRequests";
    public static final String TOTAL_NATIVE_VAST_REQUESTS = "TotalNativeVASTRequests";
    public static final String TOTAL_MOVIEBOARD_REQUESTS = "TotalMovieBoardRequests";
    public static final String TOTAL_MOVIEBOARD_TEMPLATES = "TotalMovieBoardTemplates";
    public static final String TOTAL_PURE_VAST_RESPONSE = "TotalPureVASTResponses";
    public static final String TOTAL_PURE_VAST_RESPONSE_INLINE_OR_WRAPPER_MISSING =
            "totalPureVastResponseInlineOrWrapperMissing";
    public static final String TOTAL_PURE_VAST_RESPONSE_TRACKING_EVENTS_MISSING =
            "totalPureVastResponseTrackingEventsMissing";
    public static final String TOTAL_REWARDED_VAST_VIDEO_REQUESTS = "TotalRewardedVASTVideoRequests";
    public static final String TOTAL_VIDEO_RESPONSES = "TotalVideoResponses";
    public static final String TOTAL_VAST_VIDEO_RESPONSES = "TotalVASTVideoResponses";
    public static final String TOTAL_REWARDED_VAST_VIDEO_RESPONSES = "TotalRewardedVASTVideoResponses";
    public static final String TOTAL_VIEWABILITY_RESPONSES = "TotalViewabilityResponses";
    public static final String INVALID_VIDEO_RESPONSE_COUNT = "InvalidVideoResponseCount";
    public static final String INVALID_MEDIA_PREFERENCES_JSON = "InvalidMediaPreferencesJson";
    public static final String MRAID_PATH_NOT_FOUND = "MraidPathWasMissing-";
    public static final String UNCAUGHT_EXCEPTIONS = "UncaughtExceptions";
    public static final String TRACKER_BEING_FETCHED_BEFORE_GENERATION = "trackerBeingFetchedBeforeGeneration";
    public static final String TOTAL_AGENCY_REBATE_DEAL_RESPONSES = "TotalAgencyRebateDealResponses";
    public static final String DROPPED_IN_SECURE_NOT_SUPPORTED_FILTER = "DroppedInSecureNotSupportedFilter";


    /**
     *  Stats related to PMP
     */

    // Targeting Segment Filter Stats
    public static final String TARGETING_SEGMENT_FILTER_STATS = "TargetingSegmentFilterStats";
    public static final String DROPPED_IN_TARGETING_SEGMENT_MANUF_MODEL_FILTER = "DroppedInManufModelFilter";
    public static final String DROPPED_IN_TARGETING_SEGMENT_COUNTRY_CITY_FILTER = "DroppedInCountryCityFilter";
    public static final String DROPPED_IN_TARGETING_SEGMENT_OS_VERSION_FILTER = "DroppedInOsVersionFilter";
    public static final String DROPPED_IN_TARGETING_SEGMENT_CSID_MATCH_FILTER = "DroppedInCsidMatchFilter";
    public static final String DROPPED_IN_TARGETING_SEGMENT_SDK_VERSION_FILTER = "DroppedInSdkVersionFilter";
    public static final String DROPPED_IN_TARGETING_SEGMENT_GEO_REGION_INCLUSION_FILTER = "DroppedInGeoRegionInclusionFilter";
    public static final String DROPPED_IN_TARGETING_SEGMENT_GEO_REGION_EXCLUSION_FILTER = "DroppedInGeoRegionExclusionFilter";

    // Package (V2) Filter Stats
    public static final String PACKAGE_V2_FILTER_STATS = "PackageV2FilterStats";
    public static final String DROPPED_IN_PACKAGE_V2_VIEWABILITY_SDK_VERSIONS_ENFORCER_FILTER = "DroppedInViewabilitySdkVersionsEnforcerFilter";

    // Package Filter Stats
    public static final String PACKAGE_FILTER_STATS = "PackageFilterStats";
    public static final String DROPPED_IN_PACKAGE_DMP_FILTER = "DroppedInPackageDMPFilter";
    public static final String DROPPED_IN_PACKAGE_AD_TYPE_TARGETING_FILTER = "DroppedInPackageAdTypeTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_MANUF_MODEL_FILTER = "DroppedInPackageManufModelTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_OS_VERSION_FILTER = "DroppedInPackageOSVersionTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_GEO_REGION_FILTER = "DroppedInPackageGeoRegionTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_SEGMENT_SUBSET_FILTER = "DroppedInPackageSegmentSubsetFilter";
    public static final String DROPPED_IN_PACKAGE_LANGUAGE_TARGETING_FILTER = "DroppedInPackageLanguageTargetingFilter";
    public static final String DROPPED_IN_PACKAGE_SDK_VERSION_FILTER = "DroppedInPackageSDKVersionTargetingFilter";

    // General Stats for PMP
    public static final String OVERALL_PMP_STATS = "OverallPMPStats";
    public static final String OVERALL_PMP_REQUEST_STATS = "OverallPMPRequestStats";
    public static final String OVERALL_PMP_RESPONSE_STATS = "OverallPMPResponseStats";
    public static final String OVERALL_PMP_ERROR_STATS = "OverallPMPErrorStats";
    public static final String FORWARDED_PACKAGES_LIST_TRUNCATED = "ForwardedPackagesListTruncated";
    public static final String TOTAL_DEAL_REQUESTS = "TotalDealRequests";
    public static final String TOTAL_DEAL_RESPONSES = "TotalDealResponses";
    public static final String PACKAGE_FORWARDED = "PackageForwarded-"; // Packages may be implicitly forwarded as deals
    public static final String DEAL_FORWARDED = "DealForwarded-"; // TODO
    public static final String DEAL_RESPONSES = "DealResponses-";

    // Deal errors
    public static final String RESPONSE_DROPPED_AS_UNKNOWN_DEAL_WAS_RECEIVED = "ResponseDroppedAsUnknownDealWasReceived-";
    public static final String RESPONSE_DROPPED_AS_NON_FORWARDED_DEAL_WAS_RECEIVED = "ResponseDroppedAsNonForwardedDealWasReceived-";
    public static final String BADLY_CONFIGURED_DEAL = "BadlyConfiguredDeal-";
    public static final String BADLY_CONFIGURED_TARGETING_SEGMENT = "BadlyConfiguredTargetingSegment-";

    // Latency metrics for Targeting Segments, Packages and Deals.
    public static final String TARGETING_SEGMENTS_MATCH_LATENCY = "TargetingSegmentsMatchLatency";
    public static final String PACKAGES_V2_MATCH_LATENCY = "PackagesV2MatchLatency";
    public static final String IX_PACKAGE_MATCH_LATENCY = "IxPackageMatchLatency";

    /**
     *  Stats related to the auction
     */

    // General Stats for the Auction
    public static final String AUCTION_STATS = "AuctionStats";
    public static final String ALL_SEGMENTS_DROPPED_IN_AUCTION_FILTERS = "-AllSegmentsDroppedInAuctionFilters";
    public static final String TOTAL_AUCTIONS_CONDUCTED = "-TotalAuctionsConducted";
    public static final String AUCTIONS_WON_BY_TRUMP_DEALS = "-TotalAuctionsWonByTrumpDeals";
    public static final String AUCTIONS_WON_BY_NON_TRUMP_DEALS = "-TotalAuctionsWonByNonTrumpDeals";
    public static final String AUCTIONS_WITH_NO_COMPETITION = "-TotalAuctionsWithNoCompetition";
    public static final String AUCTIONS_WITH_OPPORTUNITY_LOSS = "-TotalAuctionsWithOpportunityLoss";
    public static final String OPPORTUNITY_LOSS_IN_100xCPM = "-OpportunityLossIn100xCPM";
    public static final String AUCTIONS_WON_AT_CLEARING_PRICE = "-TotalAuctionsWonAtClearingPrice";
    public static final String EFFECTIVELY_FIRST_PRICE_SECOND_PRICE_AUCTIONS = "-TotalEffectivelyFirstPriceSecondPriceAuctions";

    // public static final String NO_MATCH_SEGMENT_STATS = "DetailedNoMatchSegmentStats";
    public static final String BID_GUIDANCE_ABSENT = "-BidGuidanceAbsent";
    public static final String BID_GUIDANCE_LESS_OR_EQUAL_TO_FLOOR = "-BidGuidanceIsLessOrEqualToFloor";
    public static final String BID_FLOOR_TOO_LOW = "-BidFloorTooLow";

    // Total Requests to RP = IX-TotalSingleFormatRequests + IX-TotalMultiFormatRequests * fanout
    public static final String TOTAL_MULTI_FORMAT_REQUESTS = "IX-TotalMultiFormatRequests";
    public static final String TOTAL_SINGLE_FORMAT_REQUESTS = "IX-TotalSingleFormatRequests";

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
    public static final String NULL_URI = "NullURI";
    public static final String NULL_HOST_NAME = "NullHostName";
    
    // Stats related to Native Image Analysis
    public static final String NATIVE_IMAGE_NOT_PROPER = "NativeImageWidthOrHeightSmall-";
    public static final String NATIVE_IMAGE_AR_DIFF_MORE_10_PERCENT = "NativeImageArDiffMoreThan10Percent-";
    public static final String NATIVE_IMAGE_WIDTH_MODIFIED = "NativeImageWidthModified-";
    public static final String ALL_NATIVE_ASSETS_DEFAULT= "AllNativeAssetsDefault";

    public static final String TOTAL_SECURE_REQUEST = "TotalSecureRequest";
    public static final String TOTAL_SECURE_RESPONSE = "TotalSecureResponse";

    public static final String UH1_TO_RP_WEST_PREFIX = "UH1ToRPWest";

    // Native 1.0 Video errors
    public static final String NATIVE_VIDEO_REQUEST_DROPPED_AS_TEMPLATE_WAS_MISSING = "NativeVideoResponse.DroppedAsTemplateWasMissing";
    public static final String NATIVE_VIDEO_RESPONSE_DROPPED_AS_VAST_XML_GENERATION_FAILED = "NativeVideoResponse.DroppedAsVastXMLGenerationFailed";
    public static final String NATIVE_VIDEO_RESPONSE_DROPPED_AS_TEMPLATE_MERGING_FAILED = "NativeVideoResponse.DroppedAsTemplateMergingFailed";

    // Movieboard errors
    public static final String MOVIE_BOARD_REQUEST_DROPPED_AS_MIN_OS_CHECK_FAILED = "MovieBoardRequest.DroppedAsMinOSCheckFailed";
    public static final String MOVIE_BOARD_REQUEST_DROPPED_AS_PARENT_VIEW_WIDTH_WAS_INVALID = "MovieBoardRequest.DroppedAsParentViewWidthWasInvalid";
    public static final String MOVIE_BOARD_REQUEST_DROPPED_AS_TEMPLATE_WAS_MISSING = "MovieBoardRequest.DroppedAsTemplateWasMissing";
    public static final String MOVIE_BOARD_RESPONSE_DROPPED_AS_VAST_XML_GENERATION_FAILED = "MovieBoardResponse.DroppedAsVastXMLGenerationFailed";
    public static final String MOVIE_BOARD_RESPONSE_DROPPED_AS_TEMPLATE_MERGING_FAILED = "MovieBoardResponse.DroppedAsTemplateMergingFailed";

    public static final String TOTAL_MISMATCH_BUNDLE_ID_FOR_DST = "TotalMismatchBundleIdForDST_";
    public static final String TOTAL_REQUEST_FOR_NAPP_SCORE = "TotalRequestForNappScore";
    public static final String TOTAL_REQUEST_WITHOUT_NAPP_SCORE = "TotalRequestWithoutNappScore";
    public static final String TOTAL_REQUEST_WITH_SCORE_GREATER_THAN_100 = "TotalRequestWithScoreGreaterThan100";
    public static final String TOTAL_REQUEST_WITHOUT_MAPP_RESPONSE = "TotalRequestWithoutMappResponse";

    public static final String PHOTON = "photon";
    public static final String TOTAL_PHOTON_REQUEST = "Request";
    public static final String TOTAL_PHOTON_REQUEST_THRIFT_PARSE_EXCEPTION = "ThriftParseException";
    public static final String TOTAL_PHOTON_IO_EXCEPTION = "IOException";
    public static final String TOTAL_PHOTON_RESPONSE_STATUS_CODE = "ResponseWithStatusCode_";
    public static final String TOTAL_PHOTON_RESPONSE_THRIFT_PARSE_EXCEPTION = "ResponseParseException_";
    public static final String TOTAL_PHOTON_EXCEPTION = "PhotonExpection";
    public static final String PHOTON_LATENCY = "Latency";
    public static final String TOTAL_PHOTON_REQUEST_TIMEOUT = "RequestTimeout";
    public static final String TOTAL_INTERRUPTED_EXCEPTION_IN_PHOTON_RESPONSE = "InterruptedExceptionResponse";
    public static final String TOTAL_EXECUTION_EXCEPTION_IN_PHOTON_RESPONSE = "ExecutionExceptionResponse";
    public static final String TOTAL_TIMEOUT_IN_PHOTON_RESPONSE = "TimeoutResponse";
    public static final String TOTAL_PHOTON_ERROR_CALLBACK_FOR = "ErrorCallBackFor_";
    public static final String LATENCY_FOR_PHOTON_FUTURE_CALL = "FutureCallLatency";
    public static final String LATENCY_FOR_NING_PHOTON_RESPONSE = "NingResponseLatency";
    public static final String TOTAL_NULL_CSIDS = "NullCSIds";
}
