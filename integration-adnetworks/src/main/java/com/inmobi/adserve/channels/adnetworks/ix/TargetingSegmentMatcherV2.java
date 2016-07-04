package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.checkForCsidMatch;
import static com.inmobi.adserve.channels.adnetworks.ix.TargetingSegmentFiltersV2.checkForCityTargeting;
import static com.inmobi.adserve.channels.adnetworks.ix.TargetingSegmentFiltersV2.checkForManufModelTargeting;
import static com.inmobi.adserve.channels.adnetworks.ix.TargetingSegmentFiltersV2.checkForOsVersionTargeting;
import static com.inmobi.adserve.channels.adnetworks.ix.TargetingSegmentFiltersV2.checkForSDKVersionTargeting;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_TARGETING_SEGMENT_COUNTRY_CITY_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_TARGETING_SEGMENT_CSID_MATCH_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_TARGETING_SEGMENT_GEO_REGION_EXCLUSION_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_TARGETING_SEGMENT_GEO_REGION_INCLUSION_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_TARGETING_SEGMENT_MANUF_MODEL_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_TARGETING_SEGMENT_OS_VERSION_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_TARGETING_SEGMENT_SDK_VERSION_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.TARGETING_SEGMENTS_MATCH_LATENCY;
import static com.inmobi.adserve.channels.util.InspectorStrings.TARGETING_SEGMENT_FILTER_STATS;
import static lombok.AccessLevel.PRIVATE;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.GeoRegionFenceMapEntity;
import com.inmobi.adserve.channels.entity.pmp.TargetingSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.casthrift.DemandSourceType;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class TargetingSegmentMatcherV2 {

    public static Set<Long> getMatchingTargetingSegmentIds(final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final Short selectedSlotId,
            final ChannelSegmentEntity adGroupEntity) {

        log.debug("Finding matching targeting segments");
        final long startTime = System.currentTimeMillis();

        // TODO: Would a builder make more sense?
        final ResultSet<TargetingSegmentEntity> resultSet = repositoryHelper.queryForMatchingTargetingSegments(
                DemandSourceType.findByValue(sasParams.getDst()),
                adGroupEntity.getAdvertiserId(),
                sasParams.getInventoryType(),
                sasParams.getSiteContentType(),
                sasParams.getConnectionType(),
                sasParams.getLocationSource(),
                null != sasParams.getIntegrationDetails() ? sasParams.getIntegrationDetails().getIntegrationMethod() : null,
                selectedSlotId,
                adGroupEntity.getSecondaryAdFormatConstraints(),
                sasParams.getPubId(),
                sasParams.getSiteId(),
                sasParams.getLanguage(),
                sasParams.getCarrierId(),
                sasParams.getOsId(),
                sasParams.getCountryId(),
                sasParams.getManufacturerId());

        final long latency = System.currentTimeMillis() - startTime;
        log.debug("Targeting segments match latency: {}", latency);
        InspectorStats.updateYammerTimerStats(
                sasParams.getDemandSourceType().name(),
                TARGETING_SEGMENTS_MATCH_LATENCY,
                latency
        );

        if (log.isDebugEnabled() && null != resultSet) {
            log.debug("Number of targeting segments returned from CQEngine: {}", resultSet.size());
        }

        int droppedInManufModelFilter = 0;
        int droppedInCountryCityFilter = 0;
        int droppedInOsVersionFilter = 0;
        int droppedInCsidMatchFilter = 0;
        int droppedInSdkVersionFilter = 0;
        int droppedInGeoRegionInclusionFilter = 0;
        int droppedInGeoRegionExclusionFilter = 0;
        log.debug("Applying targeting segment filters");

        // TODO: Create filters
        final Set<Long> matchedTargetingSegmentIds = new HashSet<>();
        if (null != resultSet) {
            for (final TargetingSegmentEntity segmentUnderJudgement : resultSet) {
                final long id = segmentUnderJudgement.getId();
                log.debug("Targeting segment under judgement: {}", id);

                // Device manufacturer and model targeting
                if (!checkForManufModelTargeting(segmentUnderJudgement.getDeviceModelsInclExcl(), sasParams.getManufacturerId(), sasParams.getModelId())) {
                    log.debug("Targeting segment {} dropped in Manufacturer Model Filter", id);
                    droppedInManufModelFilter += 1;
                    continue;
                }

                // Country and city targeting
                if (!checkForCityTargeting(segmentUnderJudgement.getCitiesInclExcl(), sasParams.getCountryId().intValue(), sasParams.getCities())) {
                    log.debug("Targeting Segment {} dropped in Country City Filter", id);
                    droppedInCountryCityFilter += 1;
                    continue;
                }

                // OS Version Targeting
                if (!checkForOsVersionTargeting(segmentUnderJudgement.getOsVersionsRange(), sasParams
                        .getOsId(), sasParams.getOsMajorVersion())) {
                    log.debug("Targeting Segment {} dropped in OS Version Filter", id);
                    droppedInOsVersionFilter += 1;
                    continue;
                }

                // CSID Targeting
                if (CollectionUtils.isNotEmpty(segmentUnderJudgement.getCsidFilterExpression()) && !checkForCsidMatch(sasParams.getCsiTags(), segmentUnderJudgement
                        .getCsidFilterExpression())) {
                    log.debug("Targeting Segment {} dropped in Csid Match Filter", id);
                    droppedInCsidMatchFilter += 1;
                    continue;
                }

                // SDK Version Targeting (Viewability is not enforced here)
                if (!checkForSDKVersionTargeting(sasParams.getSdkVersion(), segmentUnderJudgement
                        .getSdkVersionsInclExcl(), false, null)) {
                    log.debug("Targeting Segment {} dropped in SDK Version Filter", id);
                    droppedInSdkVersionFilter += 1;
                    continue;
                }

                // Pass only if the request fence ids list and the geo region fence list overlap
                if (StringUtils.isNotEmpty(segmentUnderJudgement.getIncludedGeoCustomRegion())) {
                    final String geoRegionNameCountryCombo = segmentUnderJudgement.getIncludedGeoCustomRegion() + '_' + sasParams.getCountryId();
                    final GeoRegionFenceMapEntity geoFenceEntity = repositoryHelper.queryGeoRegionFenceMapRepositoryByRegionNameCountryCombo(geoRegionNameCountryCombo);

                    if (null == geoFenceEntity || null != geoFenceEntity.getFenceIdsList() && (null == sasParams.getGeoFenceIds() || !CollectionUtils
                            .containsAny(sasParams.getGeoFenceIds(), geoFenceEntity.getFenceIdsList()))) {
                        log.debug("Targeting Segment {} dropped in Inclusion Geo Fence Filter", id);
                        droppedInGeoRegionInclusionFilter += 1;
                        continue;
                    }
                }

                // Pass only if the request fence ids list and the geo region fence list do not overlap
                if (StringUtils.isNotEmpty(segmentUnderJudgement.getExcludedGeoCustomRegion())) {
                    final String geoRegionNameCountryCombo = segmentUnderJudgement.getExcludedGeoCustomRegion() + '_' + sasParams.getCountryId();
                    final GeoRegionFenceMapEntity geoFenceEntity = repositoryHelper.queryGeoRegionFenceMapRepositoryByRegionNameCountryCombo(geoRegionNameCountryCombo);

                    if (null == geoFenceEntity || null != geoFenceEntity.getFenceIdsList() && (null == sasParams.getGeoFenceIds() || CollectionUtils
                            .containsAny(sasParams.getGeoFenceIds(), geoFenceEntity.getFenceIdsList()))) {
                        log.debug("Targeting Segment {} dropped in Exclusion Geo Fence Filter", id);
                        droppedInGeoRegionExclusionFilter += 1;
                        continue;
                    }
                }

                log.debug("Targeting segment {} passed", id);
                matchedTargetingSegmentIds.add(id);
            }
        }

        incrementTargetingSegmentFilterStats(DROPPED_IN_TARGETING_SEGMENT_MANUF_MODEL_FILTER, droppedInManufModelFilter);
        incrementTargetingSegmentFilterStats(DROPPED_IN_TARGETING_SEGMENT_COUNTRY_CITY_FILTER, droppedInCountryCityFilter);
        incrementTargetingSegmentFilterStats(DROPPED_IN_TARGETING_SEGMENT_OS_VERSION_FILTER, droppedInOsVersionFilter);
        incrementTargetingSegmentFilterStats(DROPPED_IN_TARGETING_SEGMENT_CSID_MATCH_FILTER, droppedInCsidMatchFilter);
        incrementTargetingSegmentFilterStats(DROPPED_IN_TARGETING_SEGMENT_SDK_VERSION_FILTER, droppedInSdkVersionFilter);
        incrementTargetingSegmentFilterStats(DROPPED_IN_TARGETING_SEGMENT_GEO_REGION_INCLUSION_FILTER, droppedInGeoRegionInclusionFilter);
        incrementTargetingSegmentFilterStats(DROPPED_IN_TARGETING_SEGMENT_GEO_REGION_EXCLUSION_FILTER, droppedInGeoRegionExclusionFilter);

        log.debug("Targeting segments selected: {}", matchedTargetingSegmentIds.toArray());
        return matchedTargetingSegmentIds;
    }

    private static void incrementTargetingSegmentFilterStats(final String dropInFilterStat, final long increment) {
        InspectorStats.incrementStatCount(TARGETING_SEGMENT_FILTER_STATS, dropInFilterStat, increment);
    }

}
