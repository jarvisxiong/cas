package com.inmobi.adserve.channels.entity;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import com.inmobi.segment.Segment;

@Getter
public class IXPackageEntity {

    private long id;
    private Segment segment;

    private int dmpId;
    private int dmpVendorId;
    private Set<Set<Integer>> dmpFilterSegmentExpression;
    private Integer[][] scheduledTimeOfDays;
    private List<String> dealIds;
    private List<Double> dealFloors;
    private Double dataVendorCost;

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
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private long id;
        private Segment segment;
        private int dmpId;
        private int dmpVendorId;
        private Set<Set<Integer>> dmpFilterSegmentExpression;
        private Integer[][] scheduledTimeOfDays;
        private List<String> dealIds;
        private List<Double> dealFloors;
        private Double dataVendorCost;

        public IXPackageEntity build() {
            return new IXPackageEntity(this);
        }
    }
}
