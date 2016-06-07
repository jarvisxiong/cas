package com.inmobi.adserve.channels.repository.pmp;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.SimpleNullableAttribute;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.casthrift.DemandSourceType;

/**
 * The following types of queries are optimised for deals:
 * 1) Deal id -> Deal in O(1) time
 * 2) Package id, dst, dsp* -> Deals in O(1) time
 *
 * dsp here refers to an RTBD partner or Rubicon
 */
public final class DealAttributes {

    public static final Attribute<DealEntity, String> DEAL_ID = new SimpleAttribute<DealEntity, String>("id") {
        @Override
        public String getValue(final DealEntity o) {
            return o.getId();
        }
    };

    public static final Attribute<DealEntity, Integer> PACKAGE_ID = new SimpleAttribute<DealEntity, Integer>("packageId") {
        @Override
        public Integer getValue(final DealEntity o) {
            return o.getPackageId();
        }
    };

    public static final Attribute<DealEntity, DemandSourceType> DST_ID = new SimpleAttribute<DealEntity, DemandSourceType>("dst") {
        @Override
        public DemandSourceType getValue(final DealEntity o) {
            return o.getDst();
        }
    };

    public static final Attribute<DealEntity, String> DSP_ID = new SimpleNullableAttribute<DealEntity, String>("dsp") {
        @Override
        public String getValue(final DealEntity o) {
            return o.getDsp();
        }
    };
}
