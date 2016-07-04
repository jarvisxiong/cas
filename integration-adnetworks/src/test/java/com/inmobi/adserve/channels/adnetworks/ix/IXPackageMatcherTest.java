/*
package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.checkForManufModelTargeting;
import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.checkForOsVersionTargeting;
import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.checkForSDKVersionTargeting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

public class IXPackageMatcherTest {

    @DataProvider(name = "OS Version Targeting DP")
    public Object[][] paramDataProviderForOsVersionTargeting() {

        Map<Integer, Range<Double>> emptyMap = new HashMap<>();
        Map<Integer, Range<Double>> mapWithSingleEntryWithNoTargeting = new HashMap<>();
        Map<Integer, Range<Double>> mapWithSingleEntryWithTargetingInsideRange = new HashMap<>();
        Map<Integer, Range<Double>> mapWithSingleEntryWithTargetingOutsideRange1 = new HashMap<>();
        Map<Integer, Range<Double>> mapWithSingleEntryWithTargetingOutsideRange2 = new HashMap<>();
        Map<Integer, Range<Double>> mapWithTwoEntriesWithTargetingOS1PassesOS2Fails = new HashMap<>();

        mapWithSingleEntryWithNoTargeting.put(3, Range.all());
        mapWithSingleEntryWithTargetingInsideRange.put(3, Range.closed(4.0, 5.0));
        mapWithSingleEntryWithTargetingOutsideRange1.put(3, Range.closed(4.0, 5.0));
        mapWithSingleEntryWithTargetingOutsideRange2.put(3, Range.closed(4.0, 5.0));

        mapWithTwoEntriesWithTargetingOS1PassesOS2Fails.put(3, Range.closed(4.0, 5.0));
        mapWithTwoEntriesWithTargetingOS1PassesOS2Fails.put(5, Range.closed(7.0, 8.0));

        return new Object[][] {
                {"testNull", null, 3, "4.1", true},
                {"testEmpty", emptyMap, 3, "4.1", true},
                {"testSingleEntryMapWithNoTargeting", mapWithSingleEntryWithNoTargeting, 3, "4.1", true},
                {"testSingleEntryMapWithNoTargetingFaultyOsMajorVersion", mapWithSingleEntryWithNoTargeting, 3, "faulty", true},
                {"testSingleEntryMapWithTargetingFaultyOsMajorVersion", mapWithSingleEntryWithTargetingInsideRange, 3, "faulty", false},
                {"testSingleEntryMapWithTargetingInsideRange", mapWithSingleEntryWithTargetingInsideRange, 3, "4.1", true},
                {"testSingleEntryMapWithTargetingOutsideRange1", mapWithSingleEntryWithTargetingOutsideRange1, 3, "3.9", false},
                {"testSingleEntryMapWithTargetingOutsideRange2", mapWithSingleEntryWithTargetingOutsideRange2, 3, "5.1", false},
                {"testTwoEntriesMapWithTargetingOS1PassesOS2Fails-OS1", mapWithTwoEntriesWithTargetingOS1PassesOS2Fails, 3, "4.1", true},
                {"testTwoEntriesMapWithTargetingOS1PassesOS2Fails-OS2", mapWithTwoEntriesWithTargetingOS1PassesOS2Fails, 5, "4.1", false},
                {"testTwoEntriesMapWithTargetingUnknownOS", mapWithTwoEntriesWithTargetingOS1PassesOS2Fails, 6, "2003", false},
        };
    }

    @Test(dataProvider = "OS Version Targeting DP")
    public void testCheckForOsVersionTargeting(final String useCaseName, final Map<Integer, Range<Double>> metaData,
            final int osId, final String osMajorVersionStr,
            final boolean expectedOutcome) throws Exception {
        Assert.assertEquals(expectedOutcome, checkForOsVersionTargeting(metaData, osId, osMajorVersionStr));
    }

    @DataProvider(name = "Manuf Model Targeting DP")
    public Object[][] paramDataProviderForManufModelTargeting() {

        Map<Long, Pair<Boolean, Set<Long>>> emptyMap = new HashMap<>();
        Map<Long, Pair<Boolean, Set<Long>>> mapWithSingleEntryWithNoTargetingSameManuf = new HashMap<>();
        Map<Long, Pair<Boolean, Set<Long>>> mapWithSingleEntryWithNoTargetingDiffManuf = new HashMap<>();
        Map<Long, Pair<Boolean, Set<Long>>> mapWithSingleEntryWithTargetingInclusionIsTrue = new HashMap<>();
        Map<Long, Pair<Boolean, Set<Long>>> mapWithThreeEntries = new HashMap<>();
        Set<Long> emptySet = new HashSet<>();

        mapWithSingleEntryWithNoTargetingSameManuf.put(5L, ImmutablePair.of(true, emptySet));
        mapWithSingleEntryWithNoTargetingDiffManuf.put(5L, ImmutablePair.of(true, emptySet));
        mapWithSingleEntryWithTargetingInclusionIsTrue.put(5L, ImmutablePair.of(true, ImmutableSet.of(6L, 7L)));

        mapWithThreeEntries.put(5L, ImmutablePair.of(true, ImmutableSet.of(6L, 7L)));
        mapWithThreeEntries.put(8L, ImmutablePair.of(true, ImmutableSet.of(61L, 71L)));
        mapWithThreeEntries.put(18L, ImmutablePair.of(false, ImmutableSet.of(41L, 51L)));


        return new Object[][] {
                {"testNull", null, 5L, 6L, true},
                {"testEmpty", emptyMap, 5L, 6L, true},
                {"testSingleEntryMapWithNoTargetingSameManuf", mapWithSingleEntryWithNoTargetingSameManuf, 5L, 6L, true},
                {"testSingleEntryMapWithNoTargetingDiffManuf", mapWithSingleEntryWithNoTargetingDiffManuf, 3L, 6L, false},
                {"testSingleEntryMapWithNoTargetingInclusionIsTrueAndModelsLieInModelsList", mapWithSingleEntryWithTargetingInclusionIsTrue, 5L, 6L, true},
                {"testSingleEntryMapWithNoTargetingInclusionIsTrueAndModelsDoNotLieInModelsList", mapWithSingleEntryWithTargetingInclusionIsTrue, 5L, 7L, true},
                {"testThreeEntriesMapWithFirstTwoHavingInclusionAsTrue-Part1-Manuf1-ModelsLieInModelsList", mapWithThreeEntries, 5L, 7L, true},
                {"testThreeEntriesMapWithFirstTwoHavingInclusionAsTrue-Part2-Manuf1-ModelsDoNotLieInModelsList", mapWithThreeEntries, 5L, 17L, false},
                {"testThreeEntriesMapWithFirstTwoHavingInclusionAsTrue-Part3-Manuf2-ModelsLieInModelsList", mapWithThreeEntries, 8L, 61L, true},
                {"testThreeEntriesMapWithFirstTwoHavingInclusionAsTrue-Part4-Manuf2-ModelsDoNotLieInModelsList", mapWithThreeEntries, 8L, 62L, false},
                {"testThreeEntriesMapWithFirstTwoHavingInclusionAsTrue-Part5-Manuf3-ModelsDoNotLieInModelsList", mapWithThreeEntries, 18L, 62L, true},
                {"testThreeEntriesMapWithFirstTwoHavingInclusionAsTrue-Part6-Manuf3-ModelsDoLieInModelsList", mapWithThreeEntries, 18L, 41L, false},
        };
    }

    @Test(dataProvider = "Manuf Model Targeting DP")
    public void testCheckForManufModelTargeting(final String useCaseName,
            final Map<Long, Pair<Boolean, Set<Long>>> metaData,
            final long manufacturerId, final long modelId,
            final boolean expectedOutcome) throws Exception {
        Assert.assertEquals(expectedOutcome, checkForManufModelTargeting(metaData, manufacturerId, modelId));
    }

    @DataProvider(name = "Sdk Version Targeting DP")
    public Object[][] paramDataProviderForSdkVersionTargeting() {
        final Pair<Boolean, Set<Integer>> emptyExclusion = ImmutablePair.of(true, ImmutableSet.of());
        final Pair<Boolean, Set<Integer>> emptyInclusion = ImmutablePair.of(false, ImmutableSet.of());
        final Pair<Boolean, Set<Integer>> filledExclusion = ImmutablePair.of(true, ImmutableSet.of(400, 600));
        final Pair<Boolean, Set<Integer>> filledInclusion = ImmutablePair.of(false, ImmutableSet.of(400, 600));
        final Pair<Boolean, Set<Integer>> nullInclusion = ImmutablePair.of(false, null);

        return new Object[][] {
            {"testNullSdkWithEmptyExclusion", null, emptyExclusion, false, emptyExclusion, true},
            {"testNullSdkWithFilledExclusion", null, filledExclusion, false, emptyExclusion, true},
            {"testNullSdkWithEmptyInclusion", null, emptyInclusion, false, emptyExclusion, true},
            {"testNullSdkWithFilledInclusion", null, filledInclusion, false, emptyExclusion, true},
            {"testEmptySdkWithEmptyExclusion", null, emptyExclusion, false, emptyExclusion, true},
            {"testEmptySdkWithFilledExclusion", null, filledExclusion, false, emptyExclusion, true},
            {"testEmptySdkWithEmptyInclusion", null, emptyInclusion, false, emptyExclusion, true},
            {"testEmptySdkWithFilledInclusion", null, filledInclusion, false, emptyExclusion, true},
            {"testNullSdkWithEmptyExclusionViewabilityOn", null, emptyExclusion, true, emptyExclusion, false},
            {"testNullSdkWithFilledExclusionViewabilityOn", null, filledExclusion, true, emptyExclusion, false},
            {"testNullSdkWithEmptyInclusionViewabilityOn", null, emptyInclusion, true, emptyExclusion, false},
            {"testNullSdkWithFilledInclusionViewabilityOn", null, filledInclusion, true, emptyExclusion, false},
            {"testFaultySdkWithEmptyExclusion", "asds", emptyExclusion, false, emptyExclusion, false},
            {"testFaultySdkWithFilledExclusion", "asds", filledExclusion, false, emptyExclusion, false},
            {"testFaultySdkWithEmptyInclusion", "asds", emptyInclusion, false, emptyExclusion, false},
            {"testFaultySdkWithFilledInclusion", "asds", filledInclusion, false, emptyExclusion, false},
            {"testFaultySdkWithEmptyExclusionViewabilityOn", "asds", emptyExclusion, true, emptyExclusion, false},
            {"testFaultySdkWithFilledExclusionViewabilityOn", "asds", filledExclusion, true, emptyExclusion, false},
            {"testFaultySdkWithEmptyInclusionViewabilityOn", "asds", emptyInclusion, true, emptyExclusion, false},
            {"testFaultySdkWithFilledInclusionViewabilityOn", "asds", filledInclusion, true, emptyExclusion, false},
            {"testSaneSdkWithEmptyExclusion", "a400", emptyExclusion, false, emptyExclusion, true},
            {"testSaneSdkWithFilledExclusion", "a400", filledExclusion, false, emptyExclusion, false},
            {"testSaneSdkWithEmptyInclusion", "a400", emptyInclusion, false, emptyExclusion, false},
            {"testSaneSdkWithFilledInclusion", "a400", filledInclusion, false, emptyExclusion, true},
            {"testSaneSdkWithEmptyExclusionViewabilityOnNullEligibility", "a400", emptyExclusion, true, null, false},
            {"testSaneSdkWithFilledExclusionViewabilityOnNullEligibility", "a400", filledExclusion, true, null, false},
            {"testSaneSdkWithEmptyInclusionViewabilityOnNullEligibility", "a400", emptyInclusion, true, null, false},
            {"testSaneSdkWithFilledInclusionViewabilityOnNullEligibility", "a400", filledInclusion, true, null, false},
            {"testSaneSdkWithEmptyExclusionViewabilityOnEmptyExclusionEligibility", "a400", emptyExclusion, true, emptyExclusion, true},
            {"testSaneSdkWithFilledExclusionViewabilityOnEmptyExclusionEligibility", "a400", filledExclusion, true, emptyExclusion, false},
            {"testSaneSdkWithEmptyInclusionViewabilityOnEmptyExclusionEligibility", "a400", emptyInclusion, true, emptyExclusion, false},
            {"testSaneSdkWithFilledInclusionViewabilityOnEmptyExclusionEligibility", "a400", filledInclusion, true, emptyExclusion, true},
            {"testSaneSdkWithEmptyExclusionViewabilityOnFilledExclusionEligibility", "a400", emptyExclusion, true, filledExclusion, false},
            {"testSaneSdkWithFilledExclusionViewabilityOnFilledExclusionEligibility", "a400", filledExclusion, true, filledExclusion, false},
            {"testSaneSdkWithEmptyInclusionViewabilityOnFilledExclusionEligibility", "a400", emptyInclusion, true, filledExclusion, false},
            {"testSaneSdkWithFilledInclusionViewabilityOnFilledExclusionEligibility", "a400", filledInclusion, true, filledExclusion, false},
            {"testSaneSdkWithEmptyExclusionViewabilityOnEmptyInclusionEligibility", "a400", emptyExclusion, true, emptyInclusion, false},
            {"testSaneSdkWithFilledExclusionViewabilityOnEmptyInclusionEligibility", "a400", filledExclusion, true, emptyInclusion, false},
            {"testSaneSdkWithEmptyInclusionViewabilityOnEmptyInclusionEligibility", "a400", emptyInclusion, true, emptyInclusion, false},
            {"testSaneSdkWithFilledInclusionViewabilityOnEmptyInclusionEligibility", "a400", filledInclusion, true, emptyInclusion, false},
            {"testSaneSdkWithEmptyExclusionViewabilityOnFilledInclusionEligibility", "a400", emptyExclusion, true, filledInclusion, true},
            {"testSaneSdkWithFilledExclusionViewabilityOnFilledInclusionEligibility", "a400", filledExclusion, true, filledInclusion, false},
            {"testSaneSdkWithEmptyInclusionViewabilityOnFilledInclusionEligibility", "a400", emptyInclusion, true, filledInclusion, false},
            {"testSaneSdkWithFilledInclusionViewabilityOnFilledInclusionEligibility", "a400", filledInclusion, true, filledInclusion, true},
            {"testSaneSdkWithEmptyExclusionViewabilityOnFilledInclusionEligibilityCase2", "a401", emptyExclusion, true, filledInclusion, false},
            {"testSaneSdkWithFilledExclusionViewabilityOnFilledInclusionEligibilityCase2", "a401", filledExclusion, true, filledInclusion, false},
            {"testSaneSdkWithEmptyInclusionViewabilityOnFilledInclusionEligibilityCase2", "a401", emptyInclusion, true, filledInclusion, false},
            {"testSaneSdkWithFilledInclusionViewabilityOnFilledInclusionEligibilityCase2", "a401", filledInclusion, true, filledInclusion, false},
            {"testEmptyViewabilityInclusion", "a401", filledInclusion, true, nullInclusion, false},
        };
    }

    @Test(dataProvider = "Sdk Version Targeting DP")
    public void testCheckForSDKVersionTargeting(final String useCaseName, final String sdkVersionStr,
        final Pair<Boolean, Set<Integer>> sdkVersionTargeting, final boolean checkForViewability,
        final Pair<Boolean, Set<Integer>> sdkViewabilityEligibility, final boolean expectedOutcome) throws Exception {
        Assert.assertEquals(expectedOutcome, checkForSDKVersionTargeting(sdkVersionStr, sdkVersionTargeting, checkForViewability, sdkViewabilityEligibility));
    }
}
*/
