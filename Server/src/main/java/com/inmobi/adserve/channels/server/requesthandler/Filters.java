package com.inmobi.adserve.channels.server.requesthandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author devashish Filters class to filter the Channel Segments selected by MatchSegment class
 */

public class Filters {

    private static final Logger                                                    LOG                       = LoggerFactory
                                                                                                                     .getLogger(Filters.class);

    private final static Comparator<ChannelSegment>                                COMPARATOR                = new Comparator<ChannelSegment>() {
                                                                                                                 @Override
                                                                                                                 public int compare(
                                                                                                                         final ChannelSegment o1,
                                                                                                                         final ChannelSegment o2) {
                                                                                                                     return o1
                                                                                                                             .getPrioritisedECPM() > o2
                                                                                                                             .getPrioritisedECPM() ? -1
                                                                                                                             : 1;
                                                                                                                 }
                                                                                                             };

    private final static Map<String/* advertiserId */, String/* advertiserName */> advertiserIdToNameMapping = new HashMap<String, String>();
    private final static String                                                    ENDS_WITH_ADVERTISER_ID   = ".advertiserId";
    private Map<String, HashMap<String, ChannelSegment>>                           matchedSegments;
    private final Configuration                                                    serverConfiguration;
    private final Configuration                                                    adapterConfiguration;
    private final SASRequestParameters                                             sasParams;
    private final double                                                           revenueWindow;
    private final RepositoryHelper                                                 repositoryHelper;
    private final int                                                              rtbBalanceFilterAmount;
    private int                                                                    siteImpressions;
    private final double                                                           normalizingFactor;
    private PricingEngineEntity                                                    pricingEngineEntity;
    private final byte                                                             defaultSupplyClass;
    private final byte                                                             defaultDemandClass;
    private final String[]                                                         supplyClassFloors;
    private byte                                                                   supplyClass;
    private SiteEcpmEntity                                                         siteEcpmEntity;

    public static Map<String, String> getAdvertiserIdToNameMapping() {
        return advertiserIdToNameMapping;
    }

    public Map<String, HashMap<String, ChannelSegment>> getMatchedSegments() {
        return matchedSegments;
    }

    public Filters(final Map<String, HashMap<String, ChannelSegment>> matchedSegments,
            final Configuration serverConfiguration, final Configuration adapterConfiguration,
            final SASRequestParameters sasParams, final RepositoryHelper repositoryHelper) {
        this.matchedSegments = matchedSegments;
        this.serverConfiguration = serverConfiguration;
        this.adapterConfiguration = adapterConfiguration;
        this.sasParams = sasParams;
        this.revenueWindow = serverConfiguration.getDouble("revenueWindow", 0.33);
        this.repositoryHelper = repositoryHelper;
        this.rtbBalanceFilterAmount = serverConfiguration.getInt("rtbBalanceFilterAmount", 50);
        this.normalizingFactor = serverConfiguration.getDouble("normalizingFactor", 0.1);
        this.defaultSupplyClass = (byte) (serverConfiguration.getInt("defaultSupplyClass", 9));
        this.defaultDemandClass = (byte) (serverConfiguration.getInt("defaultDemandClass", 0));
        this.supplyClassFloors = serverConfiguration.getStringArray("supplyClassFloors");
    }

    public static void init(final Configuration adapterConfiguration) {
        Iterator<String> itr = adapterConfiguration.getKeys();
        while (null != itr && itr.hasNext()) {
            String str = itr.next();
            if (str.endsWith(ENDS_WITH_ADVERTISER_ID)) {
                advertiserIdToNameMapping.put(adapterConfiguration.getString(str),
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
        if (matchedSegments.isEmpty()) {
            LOG.debug("No adGroups left for filtering , so returning empty list");
            return Collections.emptyList();
        }
        fetchPricingEngineEntity();
        adGroupLevelFiltering();
        List<ChannelSegment> channelSegments = convertToSegmentsList(matchedSegments);
        return selectTopAdGroupsForRequest(channelSegments);
    }

    private void sumUpSiteImpressions() {
        for (HashMap<String, ChannelSegment> advertiserMap : this.matchedSegments.values()) {
            for (ChannelSegment channelSegment : advertiserMap.values()) {
                this.siteImpressions += channelSegment.getChannelSegmentCitrusLeafFeedbackEntity().getBeacons();
            }
        }
        LOG.debug("Site impressions:{}", this.siteImpressions);
    }

    /**
     * Returns true if advertiser has balance remaining less than its max revenue of last fifteen days times 3
     */
    boolean isAdvertiserBurnLimitExceeded(final ChannelSegment channelSegment) {
        boolean result = channelSegment.getChannelFeedbackEntity().getBalance() < channelSegment
                .getChannelFeedbackEntity()
                    .getRevenue() * revenueWindow;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (result) {
            LOG.debug("Burn limit exceeded by advertiser {}", advertiserId);
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInburnFilter);
        }
        else {
            LOG.debug("Burn limit filter passed by advertiser {}", advertiserId);
        }
        return result;
    }

    boolean isAdvertiserDroppedInRtbBalanceFilter(final ChannelSegment channelSegment) {
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        boolean isRtbPartner = adapterConfiguration.getBoolean(advertiserIdToNameMapping.get(advertiserId) + ".isRtb",
            false);
        boolean result = false;
        if (isRtbPartner) {
            result = channelSegment.getChannelFeedbackEntity().getBalance() < rtbBalanceFilterAmount;
        }
        if (result) {
            LOG.debug("Balance is less than {} dollars for advertiser {}", rtbBalanceFilterAmount, advertiserId);
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInRtbBalanceFilter);
        }
        else {
            LOG.debug("RTB balance filter passed by advertiser {}", advertiserId);
        }
        return result;
    }

    /**
     * Returns true if advertiser has served more impressions than its daily limit
     */
    boolean isAdvertiserDailyImpressionCeilingExceeded(final ChannelSegment channelSegment) {
        boolean result = channelSegment.getChannelFeedbackEntity().getTodayImpressions() > channelSegment
                .getChannelEntity()
                    .getImpressionCeil();
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (result) {
            LOG.debug("Impression limit exceeded by advertiser{}", advertiserId);
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInImpressionFilter);
        }
        else {
            LOG.debug("Impression limit filter passed by advertiser {}", advertiserId);
        }
        return result;
    }

    /**
     * Returns true if request sent to advetiser today is greater than its daily request cap
     */
    boolean isAdvertiserDailyRequestCapExceeded(final ChannelSegment channelSegment) {
        boolean result = channelSegment.getChannelFeedbackEntity().getTodayRequests() > channelSegment
                .getChannelEntity()
                    .getRequestCap();
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (result) {
            LOG.debug("Request Cap exceeded by advertiser {}", advertiserId);
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInRequestCapFilter);
        }
        else {
            LOG.debug("Request Cap filter passed by advertiser {}", advertiserId);
        }
        return result;
    }

    /**
     * Returns true if advertiser is not present in site's advertiser inclusion list OR if advertiser is not present in
     * publisher's advertiser inclusion list when site doesnt have advertiser inclusion list
     */
    boolean isAdvertiserExcluded(final ChannelSegment channelSegment) {
        boolean result = false;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        SiteMetaDataEntity siteMetaDataEntity = repositoryHelper.querySiteMetaDetaRepository(sasParams.getSiteId());
        if (siteMetaDataEntity != null) {
            Set<String> advertisersIncludedbySite = siteMetaDataEntity.getAdvertisersIncludedBySite();
            Set<String> advertisersIncludedbyPublisher = siteMetaDataEntity.getAdvertisersIncludedByPublisher();
            // checking if site has advertiser inclusion list
            if (!advertisersIncludedbySite.isEmpty()) {
                result = !advertisersIncludedbySite.contains(advertiserId);
            }
            // else checking in publisher advertiser inclusion list if any
            else {
                result = !advertisersIncludedbyPublisher.isEmpty()
                        && !advertisersIncludedbyPublisher.contains(advertiserId);
            }
        }
        if (result) {
            LOG.debug("Dropping in Advertiser Inclusion Filter, advertiser {}", advertiserId);
            channelSegment.incrementInspectorStats(InspectorStrings.droppedinAdvertiserInclusionFilter);
        }
        else {
            LOG.debug("Advertiser Inclusion filter passed by advertiser {}", advertiserId);
        }
        return result;
    }

    /**
     * Returns true in case of site is not present in advertiser's inclusion list OR site is present in advertiser's
     * exclusion list
     */
    boolean isSiteExcludedByAdvertiser(final ChannelSegment channelSegment) {
        boolean result;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (channelSegment.getChannelEntity().getSitesIE().contains(sasParams.getSiteId())) {
            result = !channelSegment.getChannelEntity().isSiteInclusion();
        }
        else {
            result = channelSegment.getChannelEntity().isSiteInclusion();
        }
        if (result) {
            LOG.debug("Dropping as site not present in inclusion list of advertiser {}", advertiserId);
            channelSegment.incrementInspectorStats(InspectorStrings.droppedinSiteInclusionFilter);
        }
        else {
            LOG.debug("Site inclusion exclusion passed by advertiser {}", advertiserId);
        }
        return result;
    }

    /**
     * Returns true in case of site is not present in adgroup's inclusion list OR site is present in adgroup's exclusion
     * list
     */
    boolean isSiteExcludedByAdGroup(final ChannelSegment channelSegment) {
        boolean result;
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        String adGroupId = channelSegment.getChannelSegmentEntity().getAdgroupId();
        if (channelSegment.getChannelSegmentEntity().getSitesIE().contains(sasParams.getSiteId())) {
            result = !channelSegment.getChannelSegmentEntity().isSiteInclusion();
        }
        else {
            result = channelSegment.getChannelSegmentEntity().isSiteInclusion();
        }
        if (result) {
            LOG.debug("Dropping as site not present in inclusion list of adroup {}", adGroupId);
            channelSegment.incrementInspectorStats(InspectorStrings.droppedinSiteInclusionFilter);
        }
        else {
            LOG.debug("Site inclusion exclusion passed by adgroup {}", adGroupId);
        }
        return result;
    }

    boolean isAdGroupDailyImpressionCeilingExceeded(final ChannelSegment channelSegment) {
        boolean result = channelSegment.getChannelSegmentFeedbackEntity().getTodayImpressions() > channelSegment
                .getChannelSegmentEntity()
                    .getImpressionCeil();
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (result) {
            LOG.debug("Impression limit exceeded by adgroup {} advertiser {}", channelSegment
                    .getChannelSegmentEntity()
                        .getAdgroupId(), advertiserId);
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInImpressionFilter);
        }
        else {
            LOG.debug("Impression limit filter passed by adgroup {} advertiser {}", channelSegment
                    .getChannelSegmentEntity()
                        .getAdgroupId(), advertiserId);
        }
        return result;
    }

    /**
     * Filter that performs advertiser level filtering Drops all the segment of the advertiser being filtered out
     */
    void advertiserLevelFiltering() {
        LOG.debug("Inside advertiserLevelFiltering");
        Map<String, HashMap<String, ChannelSegment>> rows = new HashMap<String, HashMap<String, ChannelSegment>>();
        ChannelSegment channelSegment;
        for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments.entrySet()) {
            String advertiserId = advertiserEntry.getKey();
            channelSegment = advertiserEntry.getValue().values().iterator().next();
            // dropping advertiser if balance is less than revenue of
            // that advertiser OR if todays impression is greater than impression
            // ceiling OR site is not present in advertiser's whiteList
            if (isAdvertiserBurnLimitExceeded(channelSegment)
                    || isAdvertiserDailyImpressionCeilingExceeded(channelSegment)
                    || isAdvertiserDailyRequestCapExceeded(channelSegment) || isAdvertiserExcluded(channelSegment)
                    || isAdvertiserDroppedInRtbBalanceFilter(channelSegment)
                    || isAdvertiserFailedInAccountSegmentFilter(channelSegment)) {
                continue;
            }
            // otherwise adding the advertiser to the list
            rows.put(advertiserId, matchedSegments.get(advertiserId));
        }
        printSegments(rows);
        matchedSegments = rows;
    }

    /**
     * Filter to perform adgroup level filtering and short list a configurable number of adgroups per advertiser
     */
    void adGroupLevelFiltering() {
        LOG.debug("Inside adGroupLevelFiltering");
        Map<String, HashMap<String, ChannelSegment>> rows = new HashMap<String, HashMap<String, ChannelSegment>>();
        supplyClass = getSupplyClass(sasParams);
        LOG.debug("Supply class is {}", supplyClass);
        for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments.entrySet()) {
            String advertiserId = advertiserEntry.getKey();
            HashMap<String, ChannelSegment> hashMap = new HashMap<String, ChannelSegment>();
            List<ChannelSegment> segmentListToBeSorted = new ArrayList<ChannelSegment>();
            Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
            for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
                ChannelSegment channelSegment = adGroupEntry.getValue();
                // applying siteFloor filter
                if (channelSegment.getChannelSegmentFeedbackEntity().getECPM() < sasParams.getSiteFloor()) {
                    LOG.debug("sitefloor filter failed by adgroup {}", channelSegment
                            .getChannelSegmentFeedbackEntity()
                                .getId());
                    continue;
                }
                else {
                    LOG.debug("sitefloor filter passed by adgroup {}", channelSegment
                            .getChannelSegmentFeedbackEntity()
                                .getId());
                }

                // applying dst filter
                if (isDroppedInDstFilter(advertiserId, channelSegment)) {
                    continue;
                }

                // applying pricing engine filter
                if (isChannelSegmentFilteredOutByPricingEngine(advertiserId, pricingEngineEntity == null ? null
                        : pricingEngineEntity.getDcpFloor(), channelSegment)) {
                    continue;
                }

                // applying timeOfDayTargeting filter
                if (isTODTargetingFailed(channelSegment)) {
                    continue;
                }

                // applying impression cap filter at adgroup level
                if (isAdGroupDailyImpressionCeilingExceeded(channelSegment)) {
                    continue;
                }
                // applying segment property filter
                if (isAnySegmentPropertyViolated(channelSegment)) {
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
                        && !channelSegment.getChannelSegmentEntity().getManufModelTargetingList().isEmpty()
                        && !channelSegment
                                .getChannelSegmentEntity()
                                    .getManufModelTargetingList()
                                    .contains(sasParams.getModelId())) {
                    LOG.debug("{} dropped in model id filter", channelSegment.getChannelSegmentEntity().getId());
                    channelSegment.incrementInspectorStats(InspectorStrings.droppedinHandsetTargetingFilter);
                    continue;
                }
                // applying pricing engine filter
                if (isChannelSegmentFilteredOutByPricingEngine(advertiserId, pricingEngineEntity == null ? null
                        : pricingEngineEntity.getDcpFloor(), channelSegment)) {
                    continue;
                }

                channelSegment.setPrioritisedECPM(calculatePrioritisedECPM(channelSegment));

                if (!isDemandAcceptedBySupply(channelSegment)) {
                    continue;
                }

                segmentListToBeSorted.add(channelSegment);
            }
            if (segmentListToBeSorted.isEmpty()) {
                continue;
            }
            Collections.sort(segmentListToBeSorted, COMPARATOR);
            // choosing top segments from the sorted list
            int adGpCount = 1;
            int partnerSegmentNo;
            partnerSegmentNo = adapterConfiguration.getInt(advertiserIdToNameMapping.get(advertiserId)
                    + ".partnerSegmentNo", serverConfiguration.getInt("partnerSegmentNo", 2));
            LOG.debug("PartnersegmentNo for advertiser {} is {}", advertiserId, partnerSegmentNo);
            for (ChannelSegment channelSegment : segmentListToBeSorted) {
                if (adGpCount > partnerSegmentNo) {
                    break;
                }
                hashMap.put(channelSegment.getChannelSegmentFeedbackEntity().getId(), channelSegment);
                adGpCount++;
                channelSegment.incrementInspectorStats(InspectorStrings.totalSelectedSegments);
            }
            rows.put(advertiserId, hashMap);
        }
        printSegments(rows);
        matchedSegments = rows;
    }

    /*
     * Filter to perform accountSegment filtering on the basis of dso brand, dso performance and dso programmatic
     */
    boolean isAdvertiserFailedInAccountSegmentFilter(final ChannelSegment channelSegment) {
        int accountSegment = channelSegment.getChannelEntity().getAccountSegment();
        String advertiserId = channelSegment.getChannelEntity().getAccountId();
        if (sasParams.getDst() == 6 && null != sasParams.getAccountSegment()
                && !sasParams.getAccountSegment().isEmpty() && !sasParams.getAccountSegment().contains(accountSegment)) {
            if (getAdvertiserIdToNameMapping().containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(getAdvertiserIdToNameMapping().get(advertiserId),
                    InspectorStrings.droppedInAccountSegmentFilter);
            }
            LOG.debug("Account segment filter failed by advertiser {}", advertiserId);
            return true;
        }
        LOG.debug("Account segment filter passed by advertiser {}", advertiserId);
        return false;
    }

    /*
     * 2 is tpan and 6 is rtbd This filter will work only if request come from rule engine
     */
    boolean isDroppedInDstFilter(final String advertiserId, final ChannelSegment channelSegment) {
        if (sasParams.getDst() == 6 && channelSegment.getChannelSegmentEntity().getDst() != sasParams.getDst()) {
            LOG.debug("dropped in dst filter for advertiser {}", advertiserId);
            if (getAdvertiserIdToNameMapping().containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(getAdvertiserIdToNameMapping().get(advertiserId),
                    InspectorStrings.droppedInDstFilter);
            }
            return true;
        }
        if (sasParams.getDst() == 2 && sasParams.isResponseOnlyFromDcp()
                && channelSegment.getChannelSegmentEntity().getDst() != sasParams.getDst()) {
            LOG.debug("dropped in dst filter for advertiser {}", advertiserId);
            if (getAdvertiserIdToNameMapping().containsKey(advertiserId)) {
                InspectorStats.incrementStatCount(getAdvertiserIdToNameMapping().get(advertiserId),
                    InspectorStrings.droppedInDstFilter);
            }
            return true;
        }
        LOG.debug("dst filter passed for advertiser {}", advertiserId);
        return false;
    }

    byte getSupplyClass(final SASRequestParameters sasParams) {
        siteEcpmEntity = repositoryHelper.querySiteEcpmRepository(sasParams.getSiteId(),
            Integer.valueOf(sasParams.getCountryStr()), sasParams.getOsId());
        if (siteEcpmEntity == null) {
            LOG.debug("SiteEcpmEntity is null, thus returning default class");
            return defaultSupplyClass;
        }
        else {
            LOG.debug("Number of supply classes {}", supplyClassFloors.length);
            LOG.debug("SupplyClassFloors {} ", (Object[]) supplyClassFloors);
            LOG.debug("Site ecpm is {} Network ecpm is {}", siteEcpmEntity.getEcpm(), siteEcpmEntity.getNetworkEcpm());
            return getEcpmClass(siteEcpmEntity.getEcpm(), siteEcpmEntity.getNetworkEcpm());
        }
    }

    boolean isDemandAcceptedBySupply(final ChannelSegment channelSegment) {
        byte demandClass;
        boolean result;

        if (siteEcpmEntity == null) {
            demandClass = defaultDemandClass;
        }
        else {
            demandClass = getEcpmClass(channelSegment.getPrioritisedECPM(), siteEcpmEntity.getNetworkEcpm());
        }
        LOG.debug("Demand class is {} for adgroup {}", demandClass, channelSegment
                .getChannelSegmentEntity()
                    .getAdgroupId());
        if (pricingEngineEntity == null) {
            result = PricingEngineEntity.DEFAULT_SUPPLY_DEMAND_MAPPING[supplyClass][demandClass] == 1;
        }
        else {
            result = pricingEngineEntity.isSupplyAcceptsDemand(supplyClass, demandClass);
        }
        String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
        if (!result) {
            LOG.debug("Supply does not accepts demand");
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInSupplyDemandClassificationFilter);
        }
        return result;
    }

    byte getEcpmClass(final Double ecpm, final Double networkEcpm) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ecpm is {} network ecpm {}", ecpm, networkEcpm);
        }
        double ratio = ecpm / (networkEcpm > 0 ? networkEcpm : 1);
        byte ecpmClass = 0;
        for (String floor : supplyClassFloors) {
            if (ratio >= Double.valueOf(floor)) {
                return ecpmClass;
            }
            ecpmClass++;
        }
        return ecpmClass;
    }

    boolean isTODTargetingFailed(final ChannelSegment channelSegment) {
        if (null == channelSegment.getChannelSegmentEntity().getTod()) {
            LOG.debug("{} has all ToD and DoW targeting. Passing the ToD check ", channelSegment
                    .getChannelSegmentEntity()
                        .getAdgroupId());
            return false;
        }
        Calendar now = Calendar.getInstance();
        int hourOfDay = 1 << now.get(Calendar.HOUR_OF_DAY);
        Long[] timeOfDayTargetingArray = channelSegment.getChannelSegmentEntity().getTod();
        LOG.debug("ToD array is :  {}", (Object[]) timeOfDayTargetingArray);
        long dayOfWeek = timeOfDayTargetingArray[now.get(Calendar.DAY_OF_WEEK) - 1];
        long todt = dayOfWeek & hourOfDay;
        LOG.debug("dayOfWeek is : {} hourOfDay is : {} todt calculated is : {}", dayOfWeek, hourOfDay, todt);
        if (todt == 0) {
            LOG.debug("Hour of day targeting failed. Returning true");
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInTODFilter);
            return true;
        }
        LOG.debug("Hour of day targeting passed. Returning false");
        return false;

    }

    boolean isChannelSegmentFilteredOutByPricingEngine(final String advertiserId, final Double dcpFloor,
            final ChannelSegment channelSegment) {
        // applying dcp floor

        if (null != dcpFloor) {
            // applying the boost
            Date eCPMBoostExpiryDate = channelSegment.getChannelSegmentEntity().getEcpmBoostExpiryDate();
            Date today = new Date();
            double ecpm = channelSegment.getChannelSegmentCitrusLeafFeedbackEntity().getECPM();
            if (null != eCPMBoostExpiryDate && eCPMBoostExpiryDate.compareTo(today) > 0) {
                LOG.debug("EcpmBoost is applied for {}", channelSegment.getChannelSegmentEntity().getAdgroupId());
                LOG.debug("Ecpm before boost is {}", ecpm);
                ecpm = ecpm + channelSegment.getChannelSegmentEntity().getEcpmBoost();
                LOG.debug("Ecpm after boost is {}", ecpm);
            }

            int percentage;
            if (dcpFloor > 0.0) {
                percentage = (int) ((ecpm / dcpFloor) * 100);
            }
            else {
                percentage = 150;
            }

            // Allow percentage of times any segment
            if (percentage > 100) {
                percentage = 100;
            }
            else if (percentage == 100) {
                percentage = 50;
            }
            else if (percentage >= 80) {
                percentage = 10;
            }
            else {
                percentage = 1;
            }

            LOG.debug("pricing engine percentage allowed is {}" + percentage);
            // applying dcp floor
            if (ServletHandler.random.nextInt(100) <= percentage) {
                LOG.debug("dcp floor filter passed by adgroup {}", channelSegment
                        .getChannelSegmentFeedbackEntity()
                            .getId());
                return false;
            }
            else {
                LOG.debug("dcp floor filter failed by adgroup {}", channelSegment
                        .getChannelSegmentFeedbackEntity()
                            .getId());
                channelSegment.incrementInspectorStats(InspectorStrings.droppedinPricingEngineFilter);
                return true;
            }
        }
        return false;
    }

    /**
     * Segment property filter
     * 
     */
    boolean isAnySegmentPropertyViolated(final ChannelSegment channelSegment) {
        ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
        if (channelSegmentEntity.isUdIdRequired()
                && (StringUtils.isEmpty(sasParams.getUidParams()) || sasParams.getUidParams().equals("{}"))) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInUdidFilter);
            return true;
        }
        if (channelSegmentEntity.isZipCodeRequired() && StringUtils.isEmpty(sasParams.getPostalCode())) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInZipcodeFilter);
            return true;
        }
        if (channelSegmentEntity.isLatlongRequired() && StringUtils.isEmpty(sasParams.getLatLong())) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInLatLongFilter);
            return true;
        }
        if (channelSegmentEntity.isRestrictedToRichMediaOnly() && !sasParams.isRichMedia()) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInRichMediaFilter);
            return true;
        }
        if (channelSegmentEntity.isInterstitialOnly()
                && (sasParams.getRqAdType() == null || !sasParams.getRqAdType().equals("int"))) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInOnlyInterstitialFilter);
            return true;
        }
        if (channelSegmentEntity.isNonInterstitialOnly() && sasParams.getRqAdType() != null
                && sasParams.getRqAdType().equals("int")) {
            channelSegment.incrementInspectorStats(InspectorStrings.droppedInOnlyNonInterstitialFilter);
            return true;
        }
        return false;
    }

    /**
     * Method which selects a configurable number of top adgroups based on their prioritised ecpm
     * 
     * @param rows
     *            list containing those adgroups
     * @return returns the list having top adgroups
     */
    List<ChannelSegment> selectTopAdGroupsForRequest(final List<ChannelSegment> rows) {
        LOG.debug("Inside selectTopAdGroupsForRequest Filter");
        LOG.debug("{}", rows.size());
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
            }
            else {
                channelSegment.incrementInspectorStats(InspectorStrings.droppedInSegmentPerRequestFilter);
            }
        }
        LOG.debug("Number of  ShortListed Segments are : {}", Integer.valueOf(shortlistedRow.size()).toString());
        for (ChannelSegment aShortlistedRow : shortlistedRow) {
            LOG.debug("Segment with advertiserId {} adGroupId {} Pecpm {}", aShortlistedRow
                    .getChannelSegmentEntity()
                        .getAdvertiserId(), aShortlistedRow.getChannelSegmentEntity().getAdgroupId(), aShortlistedRow
                    .getPrioritisedECPM());
        }
        return shortlistedRow;
    }

    /**
     * Method which returns prioritised ecpm
     * 
     */
    double calculatePrioritisedECPM(final ChannelSegment channelSegment) {
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = channelSegment
                .getChannelSegmentCitrusLeafFeedbackEntity();
        double eCPM = channelSegmentFeedbackEntity.getECPM();
        int impressionsRendered = channelSegmentFeedbackEntity.getBeacons();
        LOG.debug("Impressions Rendered {} for adGroup{}", impressionsRendered, channelSegment
                .getChannelSegmentEntity()
                    .getAdgroupId());
        double eCPMBoost;
        if (this.siteImpressions == 0) {
            eCPMBoost = 0;
        }
        else {
            if (impressionsRendered == 0) {
                impressionsRendered = 1;
            }
            eCPMBoost = Math.sqrt(this.normalizingFactor * Math.log(this.siteImpressions) / impressionsRendered);

        }
        int manualPriority = channelSegment.getChannelEntity().getPriority();
        manualPriority = manualPriority > 5 ? 1 : 5 - manualPriority;
        double prioritisedECPM = (eCPM + eCPMBoost) * manualPriority;
        LOG.debug("Ecpm: {} Boost: {} Priority: {}", eCPM, eCPMBoost, manualPriority);
        LOG.debug("PrioritisedECPM={}", prioritisedECPM);
        return prioritisedECPM;
    }

    void printSegments(final Map<String, HashMap<String, ChannelSegment>> matchedSegments) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Remaining AdGroups are :");
            for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments.entrySet()) {
                Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
                for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
                    ChannelSegment channelSegment = adGroupEntry.getValue();
                    LOG.debug("Advertiser is {} and AdGp is {}", channelSegment
                            .getChannelSegmentEntity()
                                .getAdvertiserId(), channelSegment.getChannelSegmentEntity().getAdgroupId());
                }
            }
        }
    }

    /**
     * Method which ranks segments in weighted random of their prioritised ecpm
     * 
     * @param segment
     *            list to be ranked
     * @return returns the ranked list
     */
    public List<ChannelSegment> rankAdapters(final List<ChannelSegment> segment) {
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
                }
                else {
                    LOG.debug("rank {} adapter has channel id {}", rank++, segment
                            .get(index)
                                .getAdNetworkInterface()
                                .getId());
                    rankedList.add(segment.get(index));
                    segment.remove(index);
                    break;
                }
            }
        }
        LOG.debug("rank {} adapter has channel id{}", rank, segment.get(0).getAdNetworkInterface().getId());
        rankedList.add(segment.get(0));
        LOG.info("Ranked candidate adapters randomly");
        return rankedList;
    }

    List<ChannelSegment> convertToSegmentsList(final Map<String, HashMap<String, ChannelSegment>> matchedSegments) {
        ArrayList<ChannelSegment> segmentList = new ArrayList<ChannelSegment>();
        for (Map.Entry<String, HashMap<String, ChannelSegment>> advertiserEntry : matchedSegments.entrySet()) {
            Map<String, ChannelSegment> adGroups = advertiserEntry.getValue();
            for (Map.Entry<String, ChannelSegment> adGroupEntry : adGroups.entrySet()) {
                ChannelSegment channelSegment = adGroupEntry.getValue();
                segmentList.add(channelSegment);
                LOG.debug("ChannelSegment Added to list for advertiserid : {} and adgroupid {}", channelSegment
                        .getChannelSegmentEntity()
                            .getAdvertiserId(), channelSegment.getChannelSegmentEntity().getAdgroupId());
            }
        }
        return segmentList;
    }

    /**
     * Guaranteed delivery Filter to drop drop an advertiser if it has guaranteed delivery enabled and not on top in
     * ranklist
     * 
     * @param rankList
     *            : List containing ranked segments }
     */
    public List<ChannelSegment> ensureGuaranteedDelivery(final List<ChannelSegment> rankList) {
        LOG.debug("Inside guaranteed delivery filter");
        List<ChannelSegment> newRankList = new ArrayList<ChannelSegment>();
        newRankList.add(rankList.get(0));
        for (int rank = 1; rank < rankList.size(); rank++) {
            ChannelSegment rankedSegment = rankList.get(rank);
            if (!adapterConfiguration.getString(
                rankedSegment.getAdNetworkInterface().getName() + ".guaranteedDelivery", "false").equals("true")) {
                newRankList.add(rankedSegment);
            }
            else {
                LOG.debug("Dropping partner {} rank {} due to guarnteed delivery", rankedSegment
                        .getAdNetworkInterface()
                            .getName(), rank);
                rankedSegment.incrementInspectorStats(InspectorStrings.droppedInGuaranteedDelivery);
            }
        }
        LOG.debug("New ranklist size :" + newRankList.size());
        return newRankList;
    }

    public List<ChannelSegment> ensureGuaranteedDeliveryInCaseOfRTB(final List<ChannelSegment> rtbSegments,
            final List<ChannelSegment> rankList) {
        LOG.debug("Inside guaranteed delivery RTB filter");
        if (!rankList.isEmpty()
                && adapterConfiguration.getString(
                    rankList.get(0).getAdNetworkInterface().getName() + ".gauranteedDelivery", "false").equals("true")) {
            rtbSegments.clear();
            LOG.debug("Dropped all RTB segments due to guaranteed delivery RTB filter");
        }
        else {
            LOG.debug("All RTB segments passed guaranteed delivery RTB filter");
        }
        return rtbSegments;
    }

    void fetchPricingEngineEntity() {
        // Fetching pricing engine entity
        int country = 0;
        if (null != sasParams.getCountryStr()) {
            country = Integer.parseInt(sasParams.getCountryStr());
        }
        int os = sasParams.getOsId();
        if (null != repositoryHelper && country != 0) {
            pricingEngineEntity = repositoryHelper.queryPricingEngineRepository(country, os);
        }
    }

    public Double getRtbFloor() {
        return pricingEngineEntity == null ? null : pricingEngineEntity.getRtbFloor();
    }

    // Please do not call this, this is only for testing
    void setSiteEcpmEntity(final SiteEcpmEntity siteEcpmEntity) {
        this.siteEcpmEntity = siteEcpmEntity;
    }
}
