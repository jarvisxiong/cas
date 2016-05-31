package com.inmobi.adserve.channels.adnetworks.ix;

import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Range;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@NoArgsConstructor(access = PRIVATE)
class TargetingSegmentFiltersV2 {

    static boolean checkForAdTypeTargeting(final SecondaryAdFormatConstraints adgroupSecondaryAdFormatConstraints,
            final Set<SecondaryAdFormatConstraints> packageSecondaryAdFormatConstraintsSet) {

        return packageSecondaryAdFormatConstraintsSet.contains(SecondaryAdFormatConstraints.ALL)
                || packageSecondaryAdFormatConstraintsSet.contains(adgroupSecondaryAdFormatConstraints);
    }

    /**
     * Checks whether Os Version Targeting is satisfied.
     *
     * If OS Version Targeting map is null or empty then no targeting takes place and true is returned.
     *
     * Otherwise, check whether the os (major version) lies within the range.
     *
     * Assumption: osId will always have an entry in the osVersionTargeting map if the map is not empty
     *
     * @param osVersionTargeting
     * @param osId
     * @param osMajorVersionStr
     * @return
     */
    static boolean checkForOsVersionTargeting(final Map<Integer, Range<Double>> osVersionTargeting, final int osId,
            final String osMajorVersionStr) {
        if (null == osVersionTargeting || osVersionTargeting.isEmpty()) {
            return true;
        }

        Double osMajorVersion;
        Boolean checkPassed;
        try {
            osMajorVersion = Double.parseDouble(osMajorVersionStr);
            checkPassed = osVersionTargeting.get(osId).contains(osMajorVersion);
        } catch (final NumberFormatException nfe) {
            // Sanity: If the osMajorVersion is not a double, check whether any os version targeting was applied
            if (osVersionTargeting.get(osId).encloses(Range.all())) {
                checkPassed = true;
            } else {
                checkPassed = false;
                log.info("osMajorVersion is not a double");
            }
        } catch (final NullPointerException npe) {
            checkPassed = false;
            log.info("osId not present in the osVersionTargeting Map");
        }
        return checkPassed;
    }

    /**
     * Checks whether Manufacturer Model Targeting is satisfied.
     *
     * If Manufacturer Model Targeting map is null or empty; or if the corresponding Model Id Set is empty for the
     * manufacturerId, then no targeting takes place and true is returned.
     *
     * If the Manufacturer Id is not found in the Manufacturer Model Targeting map, then false is returned. Otherwise,
     * If inclusion is true, then true is returned only if the modelId is present in the Set of Model Ids. Else if
     * inclusion is false, then true is returned only if the modelId is not present in the Set of Model Ids.
     *
     * @param manufModelTargeting
     * @param manufacturerId
     * @param modelId
     * @return
     */
    static boolean checkForManufModelTargeting(final Map<Long, Pair<Boolean, Set<Long>>> manufModelTargeting,
            final long manufacturerId, final long modelId) {
        if (null == manufModelTargeting || manufModelTargeting.isEmpty()) {
            return true;
        }

        final Pair<Boolean, Set<Long>> inclusionModelIdSetPair = manufModelTargeting.get(manufacturerId);
        if (null == inclusionModelIdSetPair) {
            return false;
        }

        final boolean incl = inclusionModelIdSetPair.getLeft();
        final Set<Long> modelIdsSet = inclusionModelIdSetPair.getRight();

        boolean checkPassed;
        if (modelIdsSet.isEmpty()) {
            checkPassed = true;
        } else if (incl) {
            checkPassed = modelIdsSet.contains(modelId);
        } else {
            checkPassed = !modelIdsSet.contains(modelId);
        }
        return checkPassed;
    }

    static boolean checkForCityTargeting(final Map<Integer, Pair<Boolean, Set<Integer>>> countryCityTargeting,
            final int countryId, final Set<Integer> cities) {
        if (null == countryCityTargeting || countryCityTargeting.isEmpty()) {
            return true;
        }

        final Pair<Boolean, Set<Integer>> inclusionCityIdSetPair = countryCityTargeting.get(countryId);
        if (null == inclusionCityIdSetPair) {
            return false;
        }

        final boolean incl = inclusionCityIdSetPair.getLeft();
        final Set<Integer> cityIdsSet = inclusionCityIdSetPair.getRight();

        boolean checkPassed;
        if (cityIdsSet.isEmpty()) {
            checkPassed = true;
        } else if (incl) {
            checkPassed = CollectionUtils.isNotEmpty(CollectionUtils.intersection(cityIdsSet, cities));
        } else {
            checkPassed = CollectionUtils.isEmpty(CollectionUtils.intersection(cityIdsSet, cities));
        }
        return checkPassed;
    }

    static boolean checkForSDKVersionTargeting(final String sdkVersionStr,
            final Pair<Boolean, Set<Integer>> sdkVersionTargeting, final boolean checkForViewability,
            final Pair<Boolean, Set<Integer>> sdkViewabilityEligibility) {
        boolean returnValue = true;
        Integer sdkVersion;
        try {
            sdkVersion = StringUtils.isBlank(sdkVersionStr) ? null : Integer.parseInt(sdkVersionStr.substring(1));
        } catch (final NumberFormatException nfe) {
            return false;
        }

        if (null == sdkVersion) {
            // API case
            if (checkForViewability) {
                // Drop if viewability is on
                returnValue = false;
            }
            // else allowed (if a package reaches this point then it would have already passed the APP & WAP check
            // enforced by Segments Lib, WAP is dropped if viewability is on)
        } else {
            // SDK case
            if (null != sdkVersionTargeting) {
                final Set<Integer> sdkVersionSet = sdkVersionTargeting.getRight();
                if (sdkVersionTargeting.getLeft()) {
                    // If exclusion sdk version list
                    returnValue = !sdkVersionSet.contains(sdkVersion);
                } else {
                    // If inclusion sdk version list
                    returnValue = sdkVersionSet.contains(sdkVersion);
                }
            }

            if (checkForViewability) {
                if (null != sdkViewabilityEligibility) {
                    final Set<Integer> sdkViewabilityVersionSet = sdkViewabilityEligibility.getRight();
                    if (null != sdkViewabilityVersionSet) {
                        if (sdkViewabilityEligibility.getLeft()) {
                            // If exclusion sdk version list
                            returnValue = returnValue && !sdkViewabilityVersionSet.contains(sdkVersion);
                        } else {
                            // If inclusion sdk version list
                            returnValue = returnValue && sdkViewabilityVersionSet.contains(sdkVersion);
                        }
                    } else {
                        log.debug("Unsupported ad type for viewability");
                        returnValue = false;
                    }
                } else {
                    returnValue = false;
                }
            }
        }
        return returnValue;
    }
}
