package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.servlet.ServletHandler;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author devashish
 *         Filters class to filter the Channel Segments selected by MatchSegment
 *         class
 */

public class Filters {

    private final static Comparator<ChannelSegment> COMPARATOR = new Comparator<ChannelSegment>() {
        public int compare(ChannelSegment o1, ChannelSegment o2) {
            return o1.getPrioritisedECPM() > o2.getPrioritisedECPM() ? -1 : 1;
        }
    };

    final static Map<String/* advertiserId */, String/* advertiserName */>
            advertiserIdtoNameMapping = new HashMap<String, String>();
    private final static String ENDS_WITH_ADVERTISER_ID = ".advertiserId";
    private Map<String, HashMap<String, ChannelSegment>> matchedSegments;
    private Configuration serverConfiguration;
    private Configuration adapterConfiguration;
    private SASRequestParameters sasParams;
    private double revenueWindow;
    private RepositoryHelper repositoryHelper;
    private DebugLogger logger;
    private Double dcpFloor;
    private Double rtbFloor;
    private int rtbBalanceFilterAmount;
    private int siteImpressions;

    public static Map<String, String> getAdvertiserIdToNameMapping() {
        return advertiserIdtoNameMapping;
    }

    public Map<String, HashMap<String, ChannelSegment>> getMatchedSegments() {
        return matchedSegments;
    }

    public Filters(Map<String, HashMap<String, ChannelSegment>> matchedSegments,
                   Configuration serverConfiguration,
                   Configuration adapterConfiguration, SASRequestParameters sasParams,
                   RepositoryHelper repositoryHelper,
                   DebugLogger logger) {
        this.matchedSegments = matchedSegments;
        this.serverConfiguration = serverConfiguration;
        this.adapterConfiguration = adapterConfiguration;
        this.sasParams = sasParams;
        this.revenueWindow = serverConfiguration.getDouble("revenueWindow", 0.33);
        this.repositoryHelper = repositoryHelper;
        this.logger = logger;
        this.rtbBalanceFilterAmount = serverConfiguration.getInt("rtbBalanceFilterAmount", 50);
    }

    public static void init(Configuration adapterConfiguration) {
        Iterator<String> itr = adapterConfiguration.getKeys();
        while (null != itr && itr.hasNext()) {
            String str = itr.next();
            if (str.endsWith(ENDS_WITH_ADVERTISER_ID)) {
                advertiserIdtoNameMapping.put(adapterConfiguration.getString(str),
                        str.replace(ENDS_WITH_ADVERTISER_ID, ""));
            }
        }
    }

    /**
     * Apply all the filters in this class and
     *
     * @return returns a list of filtered channel segments
     */
    public List<ChannelSegment> applyFilters() {
        sumUpSiteImpressions();
        advertiserLevelFiltering();
        fetchPricingEngineFloors();
        adGroupLevelFiltering();
        List<ChannelSegment> channelSegments = convertToSegmentsList(matchedSegments);
        return selectTopAdGroupsForRequest(channelSegments);
    }

    private void sumUpSiteImpressions() {
        for (HashMap<String, ChannelSegment> advertiserMap
                : matchedSegments.values()) {
            for (ChannelSegment channelSegment : advertiserMap.values()) {
                 siteImpressions += channelSegment.getChannelSegmentCitrusLeafFeedbackEntity()
                         .getBeacons();
            }
        }
    }

    /**
     * Returns true if advertiser has balance remaining less than its max revenue
     * of last fifteen days times 3
     */
    boolean isAdvertiserBurnLimitExceeded(ChannelSegment channelSegment) {
        boolean result = channelSegment.getChannelFeedbackEntity().getBalance() < channelSegment
                .getChannelFeedbackEntity()
                .getRevenue() * revenueWindow;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (result) {
            logger.debug("Burn limit exceeded by advertiser", advertiserId);
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedInburnFilter);
            }
        } else {
            logger.debug("Burn limit filter passed by advertiser", advertiserId);
        }
        return result;
    }

    boolean isAdvertiserDroppedInRtbBalanceFilter(ChannelSegment channelSegment) {
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        boolean isRtbPartner = adapterConfiguration.getBoolean(advertiserIdtoNameMapping.get
                (advertiserId) + ".isRtb", false);
        boolean result = false;
        if (isRtbPartner) {
            result = channelSegment.getChannelFeedbackEntity().getBalance() <
                    rtbBalanceFilterAmount;
        }
        if (result) {
            logger.debug("Balance is less than", rtbBalanceFilterAmount,
                    "dollars for advertiser", advertiserId);
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedInRtbBalanceFilter);
            }
        } else {
            logger.debug("RTB balance filter passed by advertiser", advertiserId);
        }
        return result;
    }

    /**
     * Returns true if advertiser has served more impressions than its daily limit
     */
    boolean isAdvertiserDailyImpressionCeilingExceeded(ChannelSegment channelSegment) {
        boolean result = channelSegment.getChannelFeedbackEntity().getTodayImpressions() >
                channelSegment
                .getChannelEntity().getImpressionCeil();
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (result) {
            logger.debug("Impression limit exceeded by advertiser", advertiserId);
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedInImpressionFilter);
            }
        } else {
            logger.debug("Impression limit filter passed by advertiser", advertiserId);
        }
        return result;
    }

    /**
     * Returns true if request sent to advetiser today is greater than its daily
     * request cap
     */
    boolean isAdvertiserDailyRequestCapExceeded(ChannelSegment channelSegment) {
        boolean result = channelSegment.getChannelFeedbackEntity().getTodayRequests() >
                channelSegment.getChannelEntity()
                .getRequestCap();
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (result) {
            logger.debug("Request Cap exceeded by advertiser", advertiserId);
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedInRequestCapFilter);
            }
        } else {
            logger.debug("Request Cap filter passed by advertiser", advertiserId);
        }
        return result;
    }

    /**
     * Returns true if advertiser is not present in site's advertiser inclusion
     * list OR if advertiser is not present in publisher's advertiser inclusion
     * list when site doesnt have advertiser inclusion list
     */
    boolean isAdvertiserExcluded(ChannelSegment channelSegment) {
        boolean result = false;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        SiteMetaDataEntity siteMetaDataEntity = repositoryHelper.querySiteMetaDetaRepository
                (sasParams.getSiteId());
        if (siteMetaDataEntity != null) {
            Set<String> advertisersIncludedbySite = siteMetaDataEntity
                    .getAdvertisersIncludedBySite();
            Set<String> advertisersIncludedbyPublisher = siteMetaDataEntity
                    .getAdvertisersIncludedByPublisher();
            // checking if site has advertiser inclusion list
            if (!advertisersIncludedbySite.isEmpty()) {
                result = !advertisersIncludedbySite.contains(advertiserId);
            }
            // else checking in publisher advertiser inclusion list if any
            else {
                result = !advertisersIncludedbyPublisher.isEmpty() &&
                        !advertisersIncludedbyPublisher.contains(advertiserId);
            }
        }
        if (result) {
            logger.debug("Site/publisher inclusion list does not contain advertiser", advertiserId);
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedinAdvertiserInclusionFilter);
            }
        } else {
            logger.debug("Advertiser Inclusion filter passed by advertiser", advertiserId);
        }
        return result;
    }

    /**
     * Returns true in case of site is not present in advertiser's inclusion list
     * OR site is present in advertiser's exclusion list
     */
    boolean isSiteExcludedByAdvertiser(ChannelSegment channelSegment) {
        boolean result;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (channelSegment.getChannelEntity().getSitesIE().contains(sasParams.getSiteId())) {
            result = !channelSegment.getChannelEntity().isSiteInclusion();
        } else {
            result = channelSegment.getChannelEntity().isSiteInclusion();
        }
        if (result) {
            logger.debug("Dropping as site not present in inclusion list of advertiser",
                    advertiserId);
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedinSiteInclusionFilter);
            }
        } else {
            logger.debug("Site inclusion exclusion passed by advertiser", advertiserId);
        }
        return result;
    }

    /**
     * Returns true in case of site is not present in adgroup's inclusion list OR
     * site is present in adgroup's exclusion list
     */
    boolean isSiteExcludedByAdGroup(ChannelSegment channelSegment) {
        boolean result;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        String adGroupId = channelSegment.getChannelSegmentEntity().getAdgroupId();
        if (channelSegment.getChannelSegmentEntity().getSitesIE().contains(sasParams.getSiteId())) {
            result = !channelSegment.getChannelSegmentEntity().isSiteInclusion();
        } else {
            result = channelSegment.getChannelSegmentEntity().isSiteInclusion();
        }
        if (result) {
            logger.debug("Dropping as site not present in inclusion list of adroup", adGroupId);
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedinSiteInclusionFilter);
            }
        } else {
            logger.debug("Site inclusion exclusion passed by adgroup", adGroupId);
        }
        return result;
    }

    boolean isAdGroupDailyImpressionCeilingExceeded(ChannelSegment channelSegment) {
        boolean result = channelSegment.getChannelSegmentFeedbackEntity().getTodayImpressions() >
                channelSegment
                .getChannelSegmentEntity().getImpressionCeil();
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (result) {
            logger.debug("Impression limit exceeded by adgroup",
                    channelSegment.getChannelSegmentEntity().getAdgroupId(),
                    "advertiser", advertiserId);
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedInImpressionFilter);
            }
        } else {
            logger.debug("Impression limit filter passed by adgroup",
                    channelSegment.getChannelSegmentEntity().getAdgroupId(),
                    "advertiser", advertiserId);
        }
        return result;
    }

    /**
     * Filter that performs advertiser level filtering Drops all the segment of
     * the advertiser being filtered out
     */
    void advertiserLevelFiltering() {
        logger.debug("Inside advertiserLevelFiltering");
        Map<String, HashMap<String, ChannelSegment>> rows = new HashMap<String, HashMap<String,
                ChannelSegment>>();
        ChannelSegment channelSegment;
        for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments
                .entrySet()) {
            String advertiserId = advertiserEntry.getKey();
            channelSegment = advertiserEntry.getValue().values().iterator().next();
            // dropping advertiser if balance is less than revenue of
            // that advertiser OR if todays impression is greater than impression
            // ceiling OR site is not present in advertiser's whiteList
            if (isAdvertiserBurnLimitExceeded(channelSegment) ||
                    isAdvertiserDailyImpressionCeilingExceeded(channelSegment)
                    || isAdvertiserDailyRequestCapExceeded(channelSegment) ||
                    isAdvertiserExcluded(channelSegment) || isAdvertiserDroppedInRtbBalanceFilter
                    (channelSegment)) {
                continue;
            }
            // otherwise adding the advertiser to the list
            rows.put(advertiserId, matchedSegments.get(advertiserId));
        }
        printSegments(rows);
        matchedSegments = rows;
    }

    /**
     * Filter to perform adgroup level filtering and short list a configurable
     * number of adgroups per advertiser
     */
    void adGroupLevelFiltering() {
        logger.debug("Inside adGroupLevelFiltering");
        Map<String, HashMap<String, ChannelSegment>> rows = new HashMap<String, HashMap<String,
                ChannelSegment>>();

        for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments
                .entrySet()) {
            String advertiserId = advertiserEntry.getKey();
            HashMap<String, ChannelSegment> hashMap = new HashMap<String, ChannelSegment>();
            List<ChannelSegment> segmentListToBeSorted = new ArrayList<ChannelSegment>();
            Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
            for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
                ChannelSegment channelSegment = adGroupEntry.getValue();
                // applying siteFloor filter
                if (channelSegment.getChannelSegmentFeedbackEntity().getECPM() < sasParams
                        .getSiteFloor()) {
                    logger.debug("sitefloor filter failed by adgroup",
                            channelSegment.getChannelSegmentFeedbackEntity().getId());
                    continue;
                } else {
                    logger.debug("sitefloor filter passed by adgroup",
                            channelSegment.getChannelSegmentFeedbackEntity().getId());
                }

                //applying pricing engine filter
                if (isChannelSegmentFilteredOutByPricingEngine(advertiserId, getDcpFloor(),
                        channelSegment)) {
                    continue;
                }

                //applying timeOfDayTargeting filter
                if (isTODTargetingFailed(advertiserId, channelSegment)) {
                    continue;
                }

                //applying impression cap filter at adgroup level
                if (isAdGroupDailyImpressionCeilingExceeded(channelSegment)) {
                    continue;
                }
                // applying segment property filter
                if (isAnySegmentPropertyViolated(channelSegment.getChannelSegmentEntity())) {
                    continue;
                }
                // applying site inclusion-exclusion at advertiser level
                if (channelSegment.getChannelSegmentEntity().getSitesIE().isEmpty()) {
                    if (isSiteExcludedByAdvertiser(channelSegment)) {
                        continue;
                    }
                }
                // site inclusion exclusion not present at advertiser level so checking
                // at adgroup level
                else {
                    if (isSiteExcludedByAdGroup(channelSegment)) {
                        continue;
                    }
                }

                // applying model id filter
                if (null != channelSegment.getChannelSegmentEntity().getManufModelTargetingList()
                        && !channelSegment.getChannelSegmentEntity().getManufModelTargetingList()
                        .isEmpty()
                        && !channelSegment.getChannelSegmentEntity().getManufModelTargetingList()
                        .contains(sasParams.getModelId())) {
                    logger.debug(channelSegment.getChannelSegmentEntity().getId(),
                            " dropped in model id filter");
                    if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                        InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get
                                (advertiserId),
                                InspectorStrings.droppedinHandsetTargetingFilter);
                    }
                    continue;
                }
                channelSegment.setPrioritisedECPM(calculatePrioritisedECPM(channelSegment));
                segmentListToBeSorted.add(channelSegment);
            }
            if (segmentListToBeSorted.isEmpty()) {
                continue;
            }
            Collections.sort(segmentListToBeSorted, COMPARATOR);
            // choosing top segments from the sorted list
            int adGpCount = 1;
            int partnerSegmentNo;
            partnerSegmentNo = adapterConfiguration.getInt(advertiserIdtoNameMapping.get
                    (advertiserId) + ".partnerSegmentNo",
                    serverConfiguration.getInt("partnerSegmentNo", 2));
            logger.debug("PartnersegmentNo for advertiser", advertiserId,
                    " is " + partnerSegmentNo);
            for (ChannelSegment channelSegment : segmentListToBeSorted) {
                if (adGpCount > partnerSegmentNo) {
                    break;
                }
                hashMap.put(channelSegment.getChannelSegmentFeedbackEntity().getId(),
                        channelSegment);
                adGpCount++;
                if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                    InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                            InspectorStrings.totalSelectedSegments);
                }
            }
            rows.put(advertiserId, hashMap);
        }
        printSegments(rows);
        matchedSegments = rows;
    }

    boolean isTODTargetingFailed(String advertiserId, ChannelSegment channelSegment) {
        if (null == channelSegment.getChannelSegmentEntity().getTod()) {
            logger.debug(channelSegment.getChannelSegmentEntity().getAdgroupId(),
                    " has all ToD and DoW targeting. Passing the ToD check ");
            return false;
        }
        Calendar now = Calendar.getInstance();
        int hourOfDay = 1 << now.get(Calendar.HOUR_OF_DAY);
        Long[] timeOfDayTargetingArray = channelSegment.getChannelSegmentEntity().getTod();
        logger.debug("ToD array is :  ", timeOfDayTargetingArray);
        long dayOfWeek = timeOfDayTargetingArray[now.get(Calendar.DAY_OF_WEEK) - 1];
        long todt = dayOfWeek & hourOfDay;
        logger.debug("dayOfWeek is :  ", dayOfWeek, "hourOfDay is :  ", hourOfDay,
                "todt calculated is : ", todt);
        if (todt == 0) {
            logger.debug(logger, "Hour of day targeting failed. Returning true");
            if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                        InspectorStrings.droppedInTODFilter);
            }
            return true;
        }
        logger.debug(logger, "Hour of day targeting passed. Returning false");
        return false;

    }

    boolean isChannelSegmentFilteredOutByPricingEngine(String advertiserId, Double dcpFloor,
                                                       ChannelSegment channelSegment) {
        // applying dcp floor

        if (null != dcpFloor) {
            // applying the boost
            Date eCPMBoostExpiryDate = channelSegment.getChannelSegmentEntity()
                    .getEcpmBoostExpiryDate();
            Date today = new Date();
            double ecpm = channelSegment.getChannelSegmentCitrusLeafFeedbackEntity().getECPM();
            if (null != eCPMBoostExpiryDate && eCPMBoostExpiryDate.compareTo(today) > 0) {
                logger.debug("EcpmBoost is applied for ", channelSegment.getChannelSegmentEntity
                        ().getAdgroupId());
                logger.debug("Ecpm before boost is ", ecpm);
                ecpm = ecpm + channelSegment.getChannelSegmentEntity().getEcpmBoost();
                logger.debug("Ecpm after boost is ", ecpm);
            }

            int percentage;
            if (dcpFloor > 0.0) {
                percentage = (int) ((ecpm / dcpFloor) * 100);
            } else {
                percentage = 150;
            }

            // Allow percentage of times any segment
            if (percentage > 100) {
                percentage = 100;
            } else if (percentage == 100) {
                percentage = 50;
            } else if (percentage >= 80) {
                percentage = 10;
            } else {
                percentage = 1;
            }

            logger.debug("pricing engine percentage allowed is " + percentage);
            // applying dcp floor
            if (ServletHandler.random.nextInt(100) <= percentage) {
                logger.debug("dcp floor filter passed by adgroup",
                        channelSegment.getChannelSegmentFeedbackEntity().getId());
                return false;
            } else {
                logger.debug("dcp floor filter failed by adgroup",
                        channelSegment.getChannelSegmentFeedbackEntity().getId());
                if (advertiserIdtoNameMapping.containsKey(advertiserId)) {
                    InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(advertiserId),
                            InspectorStrings.droppedinPricingEngineFilter);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Segment property filter
     *
     */
    boolean isAnySegmentPropertyViolated(ChannelSegmentEntity channelSegmentEntity) {
        if (channelSegmentEntity.isUdIdRequired()
                && (StringUtils.isEmpty(sasParams.getUidParams()) || sasParams.getUidParams()
                .equals("{}"))) {
            InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity
                    .getAdvertiserId()),
                    InspectorStrings.droppedInUdidFilter);
            return true;
        }
        if (channelSegmentEntity.isZipCodeRequired() && StringUtils.isEmpty(sasParams
                .getPostalCode())) {
            InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity
                    .getAdvertiserId()),
                    InspectorStrings.droppedInZipcodeFilter);
            return true;
        }
        if (channelSegmentEntity.isLatlongRequired() && StringUtils.isEmpty(sasParams.getLatLong
                ())) {
            InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity
                    .getAdvertiserId()),
                    InspectorStrings.droppedInLatLongFilter);
            return true;
        }
        if (channelSegmentEntity.isRestrictedToRichMediaOnly() && !sasParams.isRichMedia()) {
            InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity
                    .getAdvertiserId()),
                    InspectorStrings.droppedInLatLongFilter);
            return true;
        }
        if (channelSegmentEntity.isInterstitialOnly()
                && (sasParams.getRqAdType() == null || !sasParams.getRqAdType().equals("int"))) {
            InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity
                    .getAdvertiserId()),
                    InspectorStrings.droppedInOnlyInterstitialFilter);
            return true;
        }
        if (channelSegmentEntity.isNonInterstitialOnly() && sasParams.getRqAdType() != null
                && sasParams.getRqAdType().equals("int")) {
            InspectorStats.incrementStatCount(advertiserIdtoNameMapping.get(channelSegmentEntity
                    .getAdvertiserId()),
                    InspectorStrings.droppedInOnlyNonInterstitialFilter);
            return true;
        }
        return false;
    }

    /**
     * Method which selects a configurable number of top adgroups based on their
     * prioritised ecpm
     *
     * @param rows list containing those adgroups
     * @return returns the list having top adgroups
     */
    List<ChannelSegment> selectTopAdGroupsForRequest(List<ChannelSegment> rows) {
        logger.debug("Inside selectTopAdGroupsForRequest Filter");
        logger.debug(rows.size());
        List<ChannelSegment> shortlistedRow = new ArrayList<ChannelSegment>();
        // Creating a sorted list of segments based on their pEcpm
        Collections.sort(rows, COMPARATOR);
        // choosing top segments from the sorted list
        int totalSegmentNo = serverConfiguration.getInt("totalSegmentNo", -1);
        for (ChannelSegment channelSegment : rows) {
            if (totalSegmentNo == -1) {
                shortlistedRow = rows;
                break;
            }
            if (shortlistedRow.size() < totalSegmentNo) {
                shortlistedRow.add(channelSegment);
            } else if (advertiserIdtoNameMapping.containsKey(channelSegment
                    .getChannelSegmentEntity().getAdvertiserId())) {
                InspectorStats.incrementStatCount(
                        advertiserIdtoNameMapping.get(channelSegment.getChannelSegmentEntity()
                                .getAdvertiserId()),
                        InspectorStrings.droppedInSegmentPerRequestFilter);
            }
        }
        logger.debug("Number of  ShortListed Segments are :", Integer.valueOf(shortlistedRow.size
                ()).toString());
        for (ChannelSegment aShortlistedRow : shortlistedRow) {
            if (logger.isDebugEnabled()) {
                logger.debug("Segment with advertiserId " + aShortlistedRow
                        .getChannelSegmentEntity().getAdvertiserId()
                        + " adGroupId " + aShortlistedRow.getChannelSegmentEntity()
                        .getAdgroupId() + " Pecpm "
                        + aShortlistedRow.getPrioritisedECPM());
            }
        }
        return shortlistedRow;
    }

    /**
     * Method which returns prioritised ecpm
     *
     */
    double calculatePrioritisedECPM(ChannelSegment channelSegment) {
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = channelSegment
                .getChannelSegmentCitrusLeafFeedbackEntity();
        double eCPM = channelSegmentFeedbackEntity.getECPM();
        double eCPMBoost =  Math.sqrt(Math.log(
                this.siteImpressions / channelSegmentFeedbackEntity.getBeacons())) * 1000;
        double prioritisedECPM = eCPM + eCPMBoost;
        logger.debug("PrioritisedECPM=", prioritisedECPM);
        return prioritisedECPM;
    }

    void printSegments(Map<String, HashMap<String, ChannelSegment>> matchedSegments) {
        if (logger.isDebugEnabled()) {
            logger.debug("Segments are :");
            for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry :
                    matchedSegments.entrySet()) {
                Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
                for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
                    ChannelSegment channelSegment = adGroupEntry.getValue();
                    logger.debug("Advertiser is", channelSegment.getChannelSegmentEntity()
                            .getAdvertiserId(), "and AdGp is",
                            channelSegment.getChannelSegmentEntity().getAdgroupId(), "ecpm is",
                            channelSegment.getPrioritisedECPM());
                }
            }
        }
    }

    /**
     * Method which ranks segments in weighted random of their prioritised ecpm
     *
     * @param segment list to be ranked
     * @return returns the ranked list
     */
    public List<ChannelSegment> rankAdapters(List<ChannelSegment> segment) {
        int rank = 0;
        // Arraylist that will contain the order in which we will wait for response
        // of the third party ad networks
        ArrayList<ChannelSegment> rankedList = new ArrayList<ChannelSegment>();
        if (segment.isEmpty()) {
            return rankedList;
        }
        while (segment.size() > 1) {
            double totalPriority = 0.0;
            for (ChannelSegment aSegment : segment) {
                // setting the prioritised ecpm for this segment
                totalPriority += aSegment.getPrioritisedECPM();
            }
            double randomNumber = Math.random() * totalPriority;
            for (int index = 0; index < segment.size(); index++) {
                if (randomNumber > segment.get(index).getPrioritisedECPM()) {
                    randomNumber -= segment.get(index).getPrioritisedECPM();
                } else {
                    logger.debug("rank", rank++, "adapter has channel id",
                            segment.get(index).getAdNetworkInterface().getId());
                    rankedList.add(segment.get(index));
                    segment.remove(index);
                    break;
                }
            }
        }
        logger.debug("rank", rank, "adapter has channel id",
                segment.get(0).getAdNetworkInterface().getId());
        rankedList.add(segment.get(0));
        logger.info("Ranked candidate adapters randomly");
        return rankedList;
    }

    List<ChannelSegment> convertToSegmentsList(
            Map<String, HashMap<String, ChannelSegment>> matchedSegments) {
        ArrayList<ChannelSegment> segmentList = new ArrayList<ChannelSegment>();
        for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry :
                matchedSegments.entrySet()) {
            Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
            for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
                ChannelSegment channelSegment = adGroupEntry.getValue();
                segmentList.add(channelSegment);
                logger.debug("ChannelSegment Added to list for advertiserid :",
                        channelSegment.getChannelSegmentEntity().getAdvertiserId(),
                        "and adgroupid", channelSegment.getChannelSegmentEntity().getAdgroupId());
            }
        }
        return segmentList;
    }

    /**
     * Guaranteed delivery Filter to drop drop an advertiser if it has guaranteed
     * delivery enabled and not on top in ranklist
     *
     * @param rankList : List containing ranked segments }
     */
    public List<ChannelSegment> ensureGuaranteedDelivery(List<ChannelSegment> rankList) {
        logger.debug("Inside guaranteed delivery filter");
        List<ChannelSegment> newRankList = new ArrayList<ChannelSegment>();
        newRankList.add(rankList.get(0));
        for (int rank = 1; rank < rankList.size(); rank++) {
            ChannelSegment rankedSegment = rankList.get(rank);
            if (!adapterConfiguration.getString(rankedSegment.getAdNetworkInterface().getName()
                    + ".guaranteedDelivery","false").equals("true")) {
                newRankList.add(rankedSegment);
            } else {
                logger.debug("Dropping partner", rankedSegment.getAdNetworkInterface().getName(),
                        "rank", rank, "due to guarnteed delivery");
                InspectorStats.incrementStatCount(
                        advertiserIdtoNameMapping.get(
                                rankedSegment.getChannelSegmentEntity().getAdvertiserId()),
                                InspectorStrings.droppedInGuaranteedDelivery);
            }
        }
        logger.debug("New ranklist size :" + newRankList.size());
        return newRankList;
    }

    public List<ChannelSegment> ensureGuaranteedDeliveryInCaseOfRTB(
            List<ChannelSegment> rtbSegments, List<ChannelSegment> rankList) {
        logger.debug("Inside guaranteed delivery RTB filter");
        if (!rankList.isEmpty() && adapterConfiguration.getString(
                rankList.get(0).getAdNetworkInterface().getName() + ".gauranteedDelivery", "false")
                .equals("true")) {
            rtbSegments.clear();
            logger.debug("Dropped all RTB segments due to guaranteed delivery RTB filter");
        } else {
            logger.debug("All RTB segments passed guaranteed delivery RTB filter");
        }
        return rtbSegments;
    }

    public void fetchPricingEngineFloors() {
        // Fetching pricing engine entity
        int country = 0;
        if (null != sasParams.getCountryStr()) {
            country = Integer.parseInt(sasParams.getCountryStr());
        }
        int os = sasParams.getOsId();
        PricingEngineEntity pricingEngineEntity = null;
        if (null != repositoryHelper && country != 0) {
            pricingEngineEntity = repositoryHelper.queryPricingEngineRepository(country, os, logger);
        }
        Double dcpFloor = null;
        Double rtbFloor = null;
        if (null != pricingEngineEntity) {
            dcpFloor = pricingEngineEntity.getDcpFloor();
            rtbFloor = pricingEngineEntity.getRtbFloor();
        }

        this.dcpFloor = dcpFloor;
        this.rtbFloor = rtbFloor;
    }

    public Double getDcpFloor() {
        return dcpFloor;
    }

    public Double getRtbFloor() {
        return rtbFloor;
    }
}
