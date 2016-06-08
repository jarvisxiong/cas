package com.inmobi.adserve.channels.repository.pmp;


import static com.googlecode.cqengine.query.QueryFactory.all;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.MultiValueNullableAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.channels.entity.pmp.TargetingSegmentEntity;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.types.InventoryType;
import com.inmobi.types.LocationSource;


/**
 * The following types of queries are optimised for targeting segments:
 * 1) DST_IDS, DSP_IDS, INVENTORY_TYPES, SITE_CONTENT_TYPES, CONNECTION_TYPES,
 * LOCATION_SOURCES, SLOTS, AD_FORMATS, INTEGRATION_METHODS, OS_IDS, COUNTRY_IDS -> Targeting Segments in O(1) time
 *
 * Worst case no. of indexes-> 40*2*3*7*6*14*2*2*15
 */
public final class TargetingSegmentAttributes {
    public static final boolean COLLECTION_MIGHT_BE_NULL = false;
    public static final Query<TargetingSegmentEntity> ALL = all(TargetingSegmentEntity.class);

    public static final Attribute<TargetingSegmentEntity, DemandSourceType> DST_IDS = new MultiValueAttribute<TargetingSegmentEntity, DemandSourceType>("dsts") {
        @Override
        public Iterable<DemandSourceType> getValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getDsts();
        }
    };

    public static final Attribute<TargetingSegmentEntity, String> DSP_IDS = new MultiValueNullableAttribute<TargetingSegmentEntity, String>("dsps", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<String> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getDsps();
        }
    };

    public static final Attribute<TargetingSegmentEntity, InventoryType> INC_INVENTORY_TYPES = new MultiValueNullableAttribute<TargetingSegmentEntity, InventoryType>("includedInventoryType", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<InventoryType> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedInventoryTypes();
        }
    };
    
    public static final Attribute<TargetingSegmentEntity, InventoryType> EXC_INVENTORY_TYPES = new MultiValueNullableAttribute<TargetingSegmentEntity, InventoryType>("excludedInventoryType", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<InventoryType> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedInventoryTypes();
        }
    };

    public static final Attribute<TargetingSegmentEntity, ContentType> INC_SITE_CONTENT_TYPES = new MultiValueNullableAttribute<TargetingSegmentEntity, ContentType>("includedSiteContentRatings", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<ContentType> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedSiteContentTypes();
        }
    };
    
    public static final Attribute<TargetingSegmentEntity, ContentType> EXC_SITE_CONTENT_TYPES = new MultiValueNullableAttribute<TargetingSegmentEntity, ContentType>("excludedSiteContentRatings", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<ContentType> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedSiteContentTypes();
        }
    };

    public static final Attribute<TargetingSegmentEntity, ConnectionType> INC_CONNECTION_TYPES = new MultiValueNullableAttribute<TargetingSegmentEntity, ConnectionType>("includedConnectionTypes", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<ConnectionType> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedConnectionTypes();
        }
    };
    
    public static final Attribute<TargetingSegmentEntity, ConnectionType> EXC_CONNECTION_TYPES = new MultiValueNullableAttribute<TargetingSegmentEntity, ConnectionType>("excludedConnectionTypes", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<ConnectionType> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedConnectionTypes();
        }
    };

    public static final Attribute<TargetingSegmentEntity, LocationSource> INC_LOCATION_SOURCES = new MultiValueNullableAttribute<TargetingSegmentEntity, LocationSource>("includedLocationSources", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<LocationSource> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedLocationSources();
        }
    };
    
    public static final Attribute<TargetingSegmentEntity, LocationSource> EXC_LOCATION_SOURCES = new MultiValueNullableAttribute<TargetingSegmentEntity, LocationSource>("excludedLocationSources", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<LocationSource> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedLocationSources();
        }
    };

    public static final Attribute<TargetingSegmentEntity, IntegrationMethod> INC_INTEGRATION_METHODS = new MultiValueNullableAttribute<TargetingSegmentEntity, IntegrationMethod>("includedIntegrationMethods", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<IntegrationMethod> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedIntegrationMethod();
        }
    };
    
    public static final Attribute<TargetingSegmentEntity, IntegrationMethod> EXC_INTEGRATION_METHODS = new MultiValueNullableAttribute<TargetingSegmentEntity, IntegrationMethod>("excludedIntegrationMethods", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<IntegrationMethod> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedIntegrationMethod();
        }
    };

    public static final Attribute<TargetingSegmentEntity, Short> INC_SLOTS = new MultiValueNullableAttribute<TargetingSegmentEntity, Short>("includedSlots", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<Short> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedSlots();
        }
    };

    public static final Attribute<TargetingSegmentEntity, Short> EXC_SLOTS = new MultiValueNullableAttribute<TargetingSegmentEntity, Short>("excludedSlots", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<Short> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedSlots();
        }
    };

    public static final Attribute<TargetingSegmentEntity, SecondaryAdFormatConstraints> INC_AD_FORMATS = new MultiValueNullableAttribute<TargetingSegmentEntity, SecondaryAdFormatConstraints>("includedAdTypes", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<SecondaryAdFormatConstraints> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedAdTypes();
        }
    };
    
    public static final Attribute<TargetingSegmentEntity, SecondaryAdFormatConstraints> EXC_AD_FORMATS = new MultiValueNullableAttribute<TargetingSegmentEntity, SecondaryAdFormatConstraints>("excludedAdTypes", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<SecondaryAdFormatConstraints> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedAdTypes();
        }
    };


    public static final Attribute<TargetingSegmentEntity, String> INC_PUBLISHERS = new MultiValueNullableAttribute<TargetingSegmentEntity, String>("includedPublishers", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<String> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedPublishers();
        }
    };

    public static final Attribute<TargetingSegmentEntity, String> EXC_PUBLISHERS = new MultiValueNullableAttribute<TargetingSegmentEntity, String>("excludedPublishers", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<String> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedPublishers();
        }
    };

    public static final Attribute<TargetingSegmentEntity, String> INC_SITES = new MultiValueNullableAttribute<TargetingSegmentEntity, String>("includedSites", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<String> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedSites();
        }
    };

    public static final Attribute<TargetingSegmentEntity, String> EXC_SITES = new MultiValueNullableAttribute<TargetingSegmentEntity, String>("excludedSites", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<String> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedSites();
        }
    };

    public static final Attribute<TargetingSegmentEntity, Long> INC_CARRIERS = new MultiValueNullableAttribute<TargetingSegmentEntity, Long>("includedCarriers", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<Long> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedCarriers();
        }
    };

    public static final Attribute<TargetingSegmentEntity, Long> EXC_CARRIERS = new MultiValueNullableAttribute<TargetingSegmentEntity, Long>("excludedCarriers", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<Long> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedCarriers();
        }
    };

    public static final Attribute<TargetingSegmentEntity, String> INC_LANGUAGES = new MultiValueNullableAttribute<TargetingSegmentEntity, String>("includedLanguages", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<String> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getIncludedLanguages();
        }
    };

    public static final Attribute<TargetingSegmentEntity, String> EXC_LANGUAGES = new MultiValueNullableAttribute<TargetingSegmentEntity, String>("excludedLanguages", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<String> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getExcludedLanguages();
        }
    };

    public static final Attribute<TargetingSegmentEntity, Integer> OS_IDS = new MultiValueNullableAttribute<TargetingSegmentEntity, Integer>("osSet", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<Integer> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getOsSet();
        }
    };

    public static final Attribute<TargetingSegmentEntity, Integer> COUNTRY_IDS = new MultiValueNullableAttribute<TargetingSegmentEntity, Integer>("countries", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<Integer> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getCountries();
        }
    };

    public static final Attribute<TargetingSegmentEntity, Long> MANUF_IDS = new MultiValueNullableAttribute<TargetingSegmentEntity, Long>("manufacturers", COLLECTION_MIGHT_BE_NULL) {
        @Override
        public Iterable<Long> getNullableValues(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getManufacturers();
        }
    };

    public static final Attribute<TargetingSegmentEntity, Long> TARGETING_SEGMENT_ID = new SimpleAttribute<TargetingSegmentEntity, Long>("id") {
        @Override
        public Long getValue(TargetingSegmentEntity tse, QueryOptions qo) {
            return tse.getId();
        }
    };

}
