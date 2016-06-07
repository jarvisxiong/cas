package com.inmobi.adserve.channels.repository.pmp;


import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.segment.Segment;
import com.inmobi.segment.impl.Country;
import com.inmobi.segment.impl.DeviceOs;
import com.inmobi.segment.impl.SiteId;
import com.inmobi.segment.impl.SlotId;
import com.inmobi.segmentparameter.SegmentParameter;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class DeprecatedIXPackageAttributes {
    public static final String ALL_SITE_ID = "A";
    public static final Integer ALL_COUNTRY_ID = -1;
    public static final Integer ALL_OS_ID = -1;
    public static final Integer ALL_SLOT_ID = -1;

    public static final Attribute<IXPackageEntity, String> SITE_ID = new MultiValueAttribute<IXPackageEntity, String>(
            "site_id") {
        @Override
        public List<String> getValues(final IXPackageEntity o) {
            final Segment segment = o.getSegment();

            Collection<String> siteIds = null;
            final SegmentParameter<?> siteIdParam = segment.getSegmentParameters().get(SiteId.class.getName());
            if (siteIdParam != null) {
                siteIds = (Collection<String>) siteIdParam.getValue();
            }
            if (siteIds == null || siteIds.isEmpty()) {
                siteIds = Collections.singleton(ALL_SITE_ID);
            }
            return new ArrayList<>(siteIds);
        }
    };

    public static final Attribute<IXPackageEntity, Integer> COUNTRY_ID =
            new MultiValueAttribute<IXPackageEntity, Integer>("country_id") {
                @Override
                public List<Integer> getValues(final IXPackageEntity o) {
                    final Segment segment = o.getSegment();
                    Collection<Integer> countryIds = null;
                    final SegmentParameter<?> countryIdParam =
                            segment.getSegmentParameters().get(Country.class.getName());
                    if (countryIdParam != null) {
                        countryIds = (Collection<Integer>) countryIdParam.getValue();
                    }
                    if (countryIds == null || countryIds.isEmpty()) {
                        countryIds = Collections.singleton(ALL_COUNTRY_ID);
                    }
                    return new ArrayList<>(countryIds);
                }
            };

    public static final Attribute<IXPackageEntity, Integer> OS_ID = new MultiValueAttribute<IXPackageEntity, Integer>(
            "os_id") {
        @Override
        public List<Integer> getValues(final IXPackageEntity o) {
            final Segment segment = o.getSegment();

            Collection<Integer> osIds = null;
            final SegmentParameter<?> osIdParam = segment.getSegmentParameters().get(DeviceOs.class.getName());
            if (osIdParam != null) {
                osIds = (Collection<Integer>) osIdParam.getValue();
            }
            if (osIds == null || osIds.isEmpty()) {
                osIds = Collections.singleton(ALL_OS_ID);
            }
            return new ArrayList<>(osIds);
        }
    };

    public static final Attribute<IXPackageEntity, Integer> SLOT_ID =
            new MultiValueAttribute<IXPackageEntity, Integer>("slot_id") {
                @Override
                public List<Integer> getValues(final IXPackageEntity o) {
                    final Segment segment = o.getSegment();

                    Collection<Integer> slotIds = null;
                    final SegmentParameter<?> slotIdParam = segment.getSegmentParameters().get(SlotId.class.getName());
                    if (slotIdParam != null) {
                        slotIds = (Collection<Integer>) slotIdParam.getValue();
                    }
                    if (slotIds == null || slotIds.isEmpty()) {
                        slotIds = Collections.singleton(ALL_SLOT_ID);
                    }
                    return new ArrayList<>(slotIds);
                }
            };

    public static final Attribute<IXPackageEntity, Integer> PACKAGE_ID = new SimpleAttribute<IXPackageEntity, Integer>("packageId") {
        @Override
        public Integer getValue(final IXPackageEntity o) {
            return o.getId();
        }
    };
}
