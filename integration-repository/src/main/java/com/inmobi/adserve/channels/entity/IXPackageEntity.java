package com.inmobi.adserve.channels.entity;

import com.inmobi.segment.Segment;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
public class IXPackageEntity {

    private long id;
    private Segment segment;

    private int dmpId;
    private int dmpVendorId;
    private Set<Set<Integer>> dmpFilterSegmentExpression;
    private Integer[][] scheduledTimeOfDays;

    public IXPackageEntity(final Builder builder) {
        id = builder.id;
        segment = builder.segment;
        dmpId = builder.dmpId;
        dmpVendorId = builder.dmpVendorId;
        dmpFilterSegmentExpression = builder.dmpFilterSegmentExpression;
        scheduledTimeOfDays = builder.scheduledTimeOfDays;
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

        public IXPackageEntity build() {
            return new IXPackageEntity(this);
        }
    }
}
