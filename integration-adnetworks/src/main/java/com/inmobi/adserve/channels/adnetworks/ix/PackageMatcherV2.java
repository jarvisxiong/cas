package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.adpool.RequestedAdType.BANNER;
import static com.inmobi.adserve.adpool.RequestedAdType.INTERSTITIAL;
import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.BANNER_ALLOWED_SDKS_FOR_VIEWABILITY;
import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.INTERSTITIAL_ALLOWED_SDKS_FOR_VIEWABILITY;
import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.VAST_ALLOWED_SDKS_FOR_VIEWABILITY;
import static com.inmobi.adserve.channels.adnetworks.ix.TargetingSegmentFiltersV2.checkForSDKVersionTargeting;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_PACKAGE_V2_VIEWABILITY_SDK_VERSIONS_ENFORCER_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.PACKAGES_V2_MATCH_LATENCY;
import static com.inmobi.adserve.channels.util.InspectorStrings.PACKAGE_V2_FILTER_STATS;
import static com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints.REWARDED_VAST_VIDEO;
import static com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints.STATIC;
import static com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints.VAST_VIDEO;
import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.pmp.PackageEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
// TODO: Do package matching on the intersection of ump selected slots and those present in the adgroup
class PackageMatcherV2 {

    static Set<Integer> getMatchingPackageIds(final Set<Long> targetingSegmentIds,
            final RepositoryHelper repositoryHelper, final SASRequestParameters sasParams,
            final ChannelSegmentEntity adGroupEntity) {

        log.debug("Finding matching packages (V2)");
        final long startTime = System.currentTimeMillis();

        final ResultSet<PackageEntity> resultSet = repositoryHelper.queryPackagesByTargetingSegmentIds(targetingSegmentIds);

        final long latency = System.currentTimeMillis() - startTime;
        log.debug("Packages (V2) match latency: {}", latency);
        InspectorStats.updateYammerTimerStats(sasParams.getDemandSourceType().name(), PACKAGES_V2_MATCH_LATENCY, latency);

        if (log.isDebugEnabled() && null != resultSet) {
            log.debug("Number of packages (V2) returned from CQEngine: {}", resultSet.size());
        }

        int droppedInViewabilitySdkVersionsEnforcerFilter = 0;
        log.debug("Applying package (V2) filters");

        final Set<Integer> matchedPackageIds = new HashSet<>();
        if (null != resultSet) {
            for (final PackageEntity segmentUnderJudgement : resultSet) {
                final int id = segmentUnderJudgement.getId();
                log.debug("Package (V2) under judgement: {}", id);

                Set<Integer> viewabilityInclusionList = null;
                final SecondaryAdFormatConstraints adgroupSecondaryAdFormat = adGroupEntity.getSecondaryAdFormatConstraints();
                if (STATIC == adgroupSecondaryAdFormat) {
                    if (BANNER == sasParams.getRequestedAdType()) {
                        viewabilityInclusionList = BANNER_ALLOWED_SDKS_FOR_VIEWABILITY;
                    } else if (INTERSTITIAL == sasParams.getRequestedAdType()) {
                        viewabilityInclusionList = INTERSTITIAL_ALLOWED_SDKS_FOR_VIEWABILITY;
                    }
                } else if (VAST_VIDEO == adgroupSecondaryAdFormat || REWARDED_VAST_VIDEO == adgroupSecondaryAdFormat) {
                    viewabilityInclusionList = VAST_ALLOWED_SDKS_FOR_VIEWABILITY;
                }

                // Enforcing Viewability SDK Versions
                if (!checkForSDKVersionTargeting(sasParams.getSdkVersion(), null, segmentUnderJudgement.enforceViewabilitySDKs(), Pair.of(false, viewabilityInclusionList))) {
                    log.debug("Package (V2) {} dropped in SDK Version Filter", id);
                    droppedInViewabilitySdkVersionsEnforcerFilter += 1;
                    continue;
                }

                log.debug("Package (V2) {} passed", id);
                matchedPackageIds.add(id);
            }
        }

        incrementPackageFilterStats(DROPPED_IN_PACKAGE_V2_VIEWABILITY_SDK_VERSIONS_ENFORCER_FILTER, droppedInViewabilitySdkVersionsEnforcerFilter);

        if (log.isDebugEnabled()) {
            log.debug("Packages (V2) selected: {}", Arrays.toString(matchedPackageIds.toArray()));
        }
        return matchedPackageIds;
    }

    private static void incrementPackageFilterStats(final String dropInFilterStat, final long increment) {
        InspectorStats.incrementStatCount(PACKAGE_V2_FILTER_STATS, dropInFilterStat, increment);
    }

}
