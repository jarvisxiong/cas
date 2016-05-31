package com.inmobi.adserve.channels.entity.pmp;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Range;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class TargetingSegmentEntity {
    private final long id;

    // Fields derived from associated deals
    private final Set<DemandSourceType> dsts;
    private Set<String> dsps;

    private final Set<InventoryType> includedInventoryTypes;
    private final Set<ContentType> includedSiteContentTypes;
    private final Set<ConnectionType> includedConnectionTypes;
    private final Set<LocationSource> includedLocationSources;
    private final Set<IntegrationMethod> includedIntegrationMethod;
    private final Set<String> includedPublishers;
    private final Set<String> includedSites;
    private final Set<Long> includedCarriers;
    private final Set<String> includedLanguages;
    private final String includedGeoCustomRegion;
    private final Set<Short> includedSlots;
    private final Set<SecondaryAdFormatConstraints> includedAdTypes;

    private final Set<InventoryType> excludedInventoryTypes;
    private final Set<ContentType> excludedSiteContentTypes;
    private final Set<ConnectionType> excludedConnectionTypes;
    private final Set<LocationSource> excludedLocationSources;
    private final Set<IntegrationMethod> excludedIntegrationMethod;
    private final Set<String> excludedPublishers;
    private final Set<String> excludedSites;
    private final Set<Long> excludedCarriers;
    private final Set<String> excludedLanguages;
    private final String excludedGeoCustomRegion;
    private final Set<Short> excludedSlots;
    private final Set<SecondaryAdFormatConstraints> excludedAdTypes;

    // SDK Versions (Incl/Excl)
    private Pair<Boolean, Set<Integer>> sdkVersionsInclExcl;

    // OS + OS Versions (Range only)
    private final Set<Integer> osSet;
    private Map<Integer, Range<Double>> osVersionsRange;

    // Country + Cities
    private final Set<Integer> countries;
    private Map<Integer, Pair<Boolean, Set<Integer>>> citiesInclExcl;

    // Manuf + Model
    private final Set<Long> manufacturers;
    private Map<Long, Pair<Boolean, Set<Long>>> deviceModelsInclExcl;

    private Set<Set<Integer>> csidFilterExpression;
    private Double dataVendorCost;
}
