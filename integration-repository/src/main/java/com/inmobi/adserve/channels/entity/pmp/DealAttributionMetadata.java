package com.inmobi.adserve.channels.entity.pmp;

import static com.inmobi.adserve.channels.entity.pmp.DealAttributionMetadata.Builder.build;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
// TODO: UTs for this!
public class DealAttributionMetadata {
    private static final Double DEFAULT_DATA_VENDOR_COST = 0d;
    private final Double dataVendorCost;
    private final Set<Integer> usedCsids;
    private final Set<Long> targetingSegmentsUsed;
    private final boolean isDataVendorAttributionRequired;

    public static final class Builder {

        public static DealAttributionMetadata build(final Set<Integer> requestCsids, final Set<TargetingSegmentEntity> usedTargetingSegments) {
            Double dataVendorCost = DEFAULT_DATA_VENDOR_COST;
            Set<Integer> usedCsids = null;
            Set<Long> targetingSegmentsUsed = new HashSet<>();

            if (CollectionUtils.isNotEmpty(usedTargetingSegments)) {
                for (final TargetingSegmentEntity tse : usedTargetingSegments) {
                    targetingSegmentsUsed.add(tse.getId());
                    if (null != tse.getDataVendorCost()) {
                        if (tse.getDataVendorCost() > dataVendorCost) {
                            dataVendorCost = tse.getDataVendorCost();
                            final Set<Integer> matchedCsids = getCsIdsIntersection(requestCsids, tse.getCsidFilterExpression());
                            if (CollectionUtils.isNotEmpty(matchedCsids)) {
                                usedCsids = matchedCsids;
                            }
                        } else if (tse.getDataVendorCost().equals(dataVendorCost) && tse.getDataVendorCost() > DEFAULT_DATA_VENDOR_COST) {
                            final Set<Integer> matchedCsids = getCsIdsIntersection(requestCsids, tse.getCsidFilterExpression());
                            if (CollectionUtils.isNotEmpty(matchedCsids)) {
                                if (CollectionUtils.isNotEmpty(usedCsids)) {
                                    usedCsids.addAll(matchedCsids);
                                } else {
                                    usedCsids = matchedCsids;
                                }
                            }
                        }
                    }
                }
            }

            return new DealAttributionMetadata(dataVendorCost, usedCsids, targetingSegmentsUsed, CollectionUtils.isNotEmpty(usedCsids));
        }

        public static DealAttributionMetadata build(final Set<Integer> requestCsids, final IXPackageEntity ixPe,
                final boolean geoCookieWasUsed) {
            final Set<Integer> usedCsids = new HashSet<>();
            if (geoCookieWasUsed) {
                usedCsids.add(ixPe.getGeocookieId());
            }
            usedCsids.addAll(getCsIdsIntersection(requestCsids, ixPe.getDmpFilterSegmentExpression()));

            final Double dataVendorCost = defaultIfNull(ixPe.getDataVendorCost(), DEFAULT_DATA_VENDOR_COST);

            return new DealAttributionMetadata(dataVendorCost, usedCsids, null, CollectionUtils.isNotEmpty(usedCsids));
        }
    }

    public static DealAttributionMetadata generateDealAttributionMetadata(final Set<Long> forwardedTargetingSegments,
            final Integer packageId,
            final Set<Integer> requestCsids, final RepositoryHelper repositoryHelper, final boolean geoCookieWasUsed) {

        DealAttributionMetadata returnValue = null;

        final Set<Long> targetingSegmentIds = repositoryHelper.queryTargetingSegmentsUsedByPackage(
                packageId, forwardedTargetingSegments);

        if (CollectionUtils.isNotEmpty(targetingSegmentIds)) {
            // New style package
            final Set<TargetingSegmentEntity> usedTseSet = new HashSet<>();
            for (final Long targetingSegmentId : targetingSegmentIds) {
                final Optional<TargetingSegmentEntity> tse =
                        repositoryHelper.getTargetingSegmentEntityById(targetingSegmentId);
                if (tse.isPresent()) {
                    usedTseSet.add(tse.get());
                }
            }
            returnValue = build(requestCsids, usedTseSet);
        } else {
            final Optional<IXPackageEntity> ixPackageEntityOptional =
                    repositoryHelper.queryIXPackageEntityByPackageId(packageId);
            if (ixPackageEntityOptional.isPresent()) {
                returnValue = build(requestCsids, ixPackageEntityOptional.get(), geoCookieWasUsed);
            }
        }
        return returnValue;
    }

    private static Set<Integer> getCsIdsIntersection(final Set<Integer> requestCsids,
            final Set<Set<Integer>> csidFilterExpression) {
        final Set<Integer> usedCsIds = new HashSet<>();

        if (CollectionUtils.isNotEmpty(csidFilterExpression)) {
            for (final Set<Integer> innerSet : csidFilterExpression) {
                for (final Integer csId : innerSet) {
                    if (requestCsids.contains(csId)) {
                        usedCsIds.add(csId);
                    }
                }
            }
        }
        return  usedCsIds;
    }

}
