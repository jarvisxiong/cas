package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.util.InspectorStrings.FORWARDED_PACKAGES_LIST_TRUNCATED;
import static com.inmobi.adserve.channels.util.InspectorStrings.OVERALL_PMP_REQUEST_STATS;
import static com.inmobi.adserve.channels.util.InspectorStrings.OVERALL_PMP_STATS;
import static com.inmobi.adserve.channels.util.InspectorStrings.PACKAGE_FORWARDED;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class PackageMatcherDelegateV2 {

    private static final int PACKAGE_MAX_LIMIT = 35;
    private static final boolean GEO_COOKIE_NOT_SUPPORTED = false;

    public static Map<Integer, Boolean> getMatchingPackageIds(final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final Short selectedSlotId, final ChannelSegmentEntity entity,
            final Set<Long> targetingSegments, final String advertiserName, final boolean checkInOldRepo) {

        if (log.isDebugEnabled()) {
            if (checkInOldRepo) {
                log.debug("Delegating actual package matching to PackageMatcherV2 & IXPackageMatcher");
            } else {
                log.debug("Delegating actual package matching to PackageMatcherV2. IXPackageMatcher is not being used");
            }
        }

        EnrichmentHelper.enrichCSIIdsWrapper(sasParams);

        final Set<Integer> packageV2Ids =
                PackageMatcherV2.getMatchingPackageIds(targetingSegments, repositoryHelper, sasParams, entity);
        Map<Integer, Boolean> packages = CollectionUtils.isNotEmpty(packageV2Ids) ?
                packageV2Ids.stream().collect(toMap(packageId -> packageId, packageId -> GEO_COOKIE_NOT_SUPPORTED)) :
                null;

        if (checkInOldRepo) {
            final Map<Integer, Boolean> oldStylePackageIds =
                    IXPackageMatcher.findMatchingPackageIds(sasParams, repositoryHelper, selectedSlotId, entity);
            final boolean oldStylePackageIdsMapIsNotNull = null != oldStylePackageIds;
            if (null != packages && oldStylePackageIdsMapIsNotNull) {
                packages.putAll(oldStylePackageIds);
            } else if (oldStylePackageIdsMapIsNotNull) {
                packages = oldStylePackageIds;
            }
        }

        if (null != packages && !packages.isEmpty()) {
            if (packages.size() > PACKAGE_MAX_LIMIT) {
                InspectorStats.incrementStatCount(OVERALL_PMP_STATS, FORWARDED_PACKAGES_LIST_TRUNCATED);
                InspectorStats.incrementStatCount(advertiserName, FORWARDED_PACKAGES_LIST_TRUNCATED);
            }

            final List<Entry<Integer, Boolean>> packagesList = new ArrayList<>(packages.entrySet());
            Collections.shuffle(packagesList);
            packages = packagesList.stream().limit(PACKAGE_MAX_LIMIT).collect(toMap(Entry::getKey, Entry::getValue));

            packages.keySet().forEach(id -> InspectorStats.incrementStatCount(OVERALL_PMP_REQUEST_STATS, PACKAGE_FORWARDED + id));

            if (log.isDebugEnabled()) {
                log.debug("Overall packages shortlisted: {}", Arrays.toString(packages.keySet().toArray()));
            }
        } else {
            log.debug("No packages shortlisted");
        }

        return packages;
    }
}
