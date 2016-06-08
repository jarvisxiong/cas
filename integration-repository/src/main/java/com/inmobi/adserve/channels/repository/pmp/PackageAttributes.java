package com.inmobi.adserve.channels.repository.pmp;


import static com.googlecode.cqengine.query.QueryFactory.none;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.inmobi.adserve.channels.entity.pmp.PackageEntity;

/**
 * The following types of queries are optimised for packages:
 * 1) Targeting segment id -> Package in O(1) time
 *
 */
public final class PackageAttributes {
    public static final Query<PackageEntity> NONE = none(PackageEntity.class);

    public static final Attribute<PackageEntity, Integer> PACKAGE_ID = new SimpleAttribute<PackageEntity, Integer>("id") {
        @Override
        public Integer getValue(PackageEntity pe, QueryOptions qo) {
            return pe.getId();
        }
    };

    public static final Attribute<PackageEntity, Long> TARGETING_SEGMENT_IDS = new MultiValueAttribute<PackageEntity, Long>("targetingSegments") {
        @Override
        public Iterable<Long> getValues(final PackageEntity pe, QueryOptions qo) {
            return pe.getTargetingSegmentIds();
        }
    };
}
