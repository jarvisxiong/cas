package com.inmobi.adserve.channels.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Range;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.segment.Segment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class IXPackageEntity {
    private int id;
    private Segment segment;
    private int dmpId;
    private int dmpVendorId;
    private boolean viewable;
    private Set<Set<Integer>> dmpFilterSegmentExpression;
    private Integer[][] scheduledTimeOfDays;
    private List<String> dealIds;
    private List<String> accessTypes;
    private List<Double> dealFloors;
    private List<Integer> rpAgencyIds;
    private List<Double> agencyRebatePercentages;
    private List<Map<String, String>> thirdPartyTrackerMapList;
    private Map<Integer, Range<Double>> osVersionTargeting;
    private Map<Long, Pair<Boolean, Set<Long>>> manufModelTargeting;
    private Pair<Boolean, Set<Integer>> sdkVersionTargeting;
    private Double dataVendorCost;
    private String geoFenceRegion;
    private Set<SecondaryAdFormatConstraints> secondaryAdFormatConstraints;
    private final Set<String> languageTargetingSet;
}
