package com.inmobi.adserve.channels.entity;

import java.util.List;
import java.util.Set;

import com.inmobi.segment.Segment;

import lombok.Getter;
import lombok.Setter;

@Getter
public class IXPackageEntity {

    private int id;
    private Segment segment;

    private int dmpId;
    private int dmpVendorId;
    private Set<Set<Integer>> dmpFilterSegmentExpression;
    private Integer[][] scheduledTimeOfDays;
    private List<String> dealIds;
    private List<String> accessTypes;
    private List<Double> dealFloors;
    private Double dataVendorCost;
    private String geoFenceRegion;

    public IXPackageEntity(final Builder builder) {
        id = builder.id;
        segment = builder.segment;
        dmpId = builder.dmpId;
        dmpVendorId = builder.dmpVendorId;
        dmpFilterSegmentExpression = builder.dmpFilterSegmentExpression;
        scheduledTimeOfDays = builder.scheduledTimeOfDays;
        dealIds = builder.dealIds;
        dealFloors = builder.dealFloors;
        dataVendorCost = builder.dataVendorCost;
        geoFenceRegion = builder.geoFenceRegion;
        accessTypes = builder.accessTypes;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private int id;
        private Segment segment;
        private int dmpId;
        private int dmpVendorId;
        private Set<Set<Integer>> dmpFilterSegmentExpression;
        private Integer[][] scheduledTimeOfDays;
        private List<String> dealIds;
        private List<Double> dealFloors;
        private List<String> accessTypes;
        private Double dataVendorCost;
        private String geoFenceRegion;

        public IXPackageEntity build() {
            return new IXPackageEntity(this);
        }
    }
}
