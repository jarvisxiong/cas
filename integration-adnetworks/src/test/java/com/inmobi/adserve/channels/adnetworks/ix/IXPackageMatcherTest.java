package com.inmobi.adserve.channels.adnetworks.ix;

import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.checkForManufModelTargeting;
import static com.inmobi.adserve.channels.adnetworks.ix.IXPackageMatcher.checkForOsVersionTargeting;

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
}