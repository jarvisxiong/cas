-- Creating enums for convenience
CREATE TYPE rtb_auction_type AS ENUM ('FIRST_PRICE', 'SECOND_PRICE');
CREATE TYPE rtb_region_of_origin AS ENUM ('NA','EMEA','CH','JP','KR','INSEA','META','ANZ','Not Defined');
CREATE TYPE rtb_inventory_type AS ENUM ('BROWSER', 'APP');
CREATE TYPE rtb_site_content_type AS ENUM ('PERFORMANCE', 'MATURE', 'FAMILY_SAFE');
CREATE TYPE rtb_connection_type AS ENUM ('UNKNOWN', 'ETHERNET', 'WIFI', 'CELLULAR_UNKNOWN', 'CELLULAR_2G',
'CELLULAR_3G', 'CELLULAR_4G');
CREATE TYPE rtb_location_source_type AS ENUM ('CCID', 'WIFI', 'LATLON', 'DERIVED_LAT_LON', 'NO_TARGETING',
'BSSID_DERIVED', 'VISIBLE_BSSID', 'CELL_TOWER', 'NID');
CREATE TYPE rtb_integration_method AS ENUM ('SDK','AD_CODE','API','UNKNOWN','API_VAST','VAULT');


-- Creating new table for storing targeting segments
CREATE TABLE targeting_segments (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(250) UNIQUE DEFAULT NULL,
    description VARCHAR(1000),

    -- Fields with small value universe sizes
    inventory_types_list_is_inclusion       BOOLEAN DEFAULT TRUE,
    inventory_types                         rtb_inventory_type[],

    site_content_types_list_is_inclusion    BOOLEAN DEFAULT TRUE,
    site_content_types                      rtb_site_content_type[],

    connection_types_list_is_inclusion      BOOLEAN DEFAULT TRUE,
    connection_types                        rtb_connection_type[],

    location_sources_list_is_inclusion      BOOLEAN DEFAULT TRUE,
    location_sources                        rtb_location_source_type[],

    integration_methods_list_is_inclusion   BOOLEAN DEFAULT TRUE,
    integration_methods                     rtb_integration_method[],

    -- Fields with incl/excl or ranges
    sdk_version_json                        json,
    os_version_json                         json,
    country_city_json                       json,
    manuf_model_json                        json,

    -- Simple fields with no nesting
    publisher_list_is_inclusion             BOOLEAN DEFAULT TRUE,
    publishers                              TEXT[],
    site_list_is_inclusion                  BOOLEAN DEFAULT TRUE,
    sites                                   TEXT[],
    carrier_list_is_inclusion               BOOLEAN DEFAULT TRUE,
    carriers                                TEXT[],
    language_list_is_inclusion              BOOLEAN DEFAULT TRUE,
    languages                               TEXT[],

    geo_region_is_inclusion                 BOOLEAN DEFAULT TRUE,
    geo_fence_region                        VARCHAR(255),

    slots_list_is_inclusion                 BOOLEAN DEFAULT TRUE,
    slots                                   INTEGER[],
    ad_types_list_is_inclusion              BOOLEAN DEFAULT TRUE,
    ad_types                                INTEGER[],

    csid_filter_expression                  TEXT,
    data_vendor_id                          INTEGER,
    data_provider_id                        INTEGER,
    data_vendor_cost                        DOUBLE PRECISION,

    -- Auditing
    created_on                              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by                              VARCHAR(100) NOT NULL,
    modified_on                             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    modified_by                             VARCHAR(100) NOT NULL
);

COMMENT ON TABLE targeting_segments IS 'Table for storing targeting segments for packages';
COMMENT ON COLUMN targeting_segments.name IS 'Targeting Segment Name';
COMMENT ON COLUMN targeting_segments.description IS 'Description';
COMMENT ON COLUMN targeting_segments.inventory_types_list_is_inclusion IS 'Whether inventory_types is an inclusion list';
COMMENT ON COLUMN targeting_segments.inventory_types IS 'List of inventory types. See enum: rtb_inventory_type';
COMMENT ON COLUMN targeting_segments.site_content_types_list_is_inclusion IS 'Whether site_content_types is an inclusion list';
COMMENT ON COLUMN targeting_segments.site_content_types IS 'List of site content types. See enum: rtb_site_content_type';
COMMENT ON COLUMN targeting_segments.connection_types_list_is_inclusion IS 'Whether connection_types is an inclusion list';
COMMENT ON COLUMN targeting_segments.connection_types IS 'List of connection types. See enum: rtb_connection_type';
COMMENT ON COLUMN targeting_segments.location_sources_list_is_inclusion IS 'Whether location_sources is an inclusion list';
COMMENT ON COLUMN targeting_segments.location_sources IS 'List of location source types. See enum: rtb_location_source_type';
COMMENT ON COLUMN targeting_segments.integration_methods_list_is_inclusion IS 'Whether integration_methods is an inclusion list';
COMMENT ON COLUMN targeting_segments.integration_methods IS 'List of integration methods. See enum: rtb_integration_method';
COMMENT ON COLUMN targeting_segments.sdk_version_json IS 'SDK Version Targeting Json';
COMMENT ON COLUMN targeting_segments.os_version_json IS 'OS Version Targeting Json';
COMMENT ON COLUMN targeting_segments.country_city_json IS 'Country City Targeting Json';
COMMENT ON COLUMN targeting_segments.manuf_model_json IS 'Manuf Model Targeting Json';
COMMENT ON COLUMN targeting_segments.publisher_list_is_inclusion IS 'Whether publishers is an inclusion list';
COMMENT ON COLUMN targeting_segments.publishers IS 'List of applicable publishers';
COMMENT ON COLUMN targeting_segments.site_list_is_inclusion IS 'Whether sites is an inclusion list';
COMMENT ON COLUMN targeting_segments.sites IS 'List of applicable sites';
COMMENT ON COLUMN targeting_segments.carrier_list_is_inclusion IS 'Whether carriers is an inclusion list';
COMMENT ON COLUMN targeting_segments.carriers IS 'List of applicable carriers';
COMMENT ON COLUMN targeting_segments.language_list_is_inclusion IS 'Whether language is an inclusion list';
COMMENT ON COLUMN targeting_segments.languages IS 'List of applicable languages';
COMMENT ON COLUMN targeting_segments.geo_region_is_inclusion IS 'Whether the geo region is included or excluded';
COMMENT ON COLUMN targeting_segments.geo_fence_region IS 'Geo region Name';
COMMENT ON COLUMN targeting_segments.slots_list_is_inclusion IS 'Whether slots is an inclusion list';
COMMENT ON COLUMN targeting_segments.slots IS 'List of applicable slots';
COMMENT ON COLUMN targeting_segments.ad_types_list_is_inclusion IS 'Whether ad_types is an inclusion list';
COMMENT ON COLUMN targeting_segments.ad_types IS 'List of ad types';
COMMENT ON COLUMN targeting_segments.csid_filter_expression IS 'Boolean expression for csids';
COMMENT ON COLUMN targeting_segments.data_vendor_id IS 'Data Vendor Id';
COMMENT ON COLUMN targeting_segments.data_provider_id IS 'Data Provider Id';
COMMENT ON COLUMN targeting_segments.data_vendor_cost IS 'Data Vendor Cost';
COMMENT ON COLUMN targeting_segments.created_on IS 'Created on time';
COMMENT ON COLUMN targeting_segments.created_by IS 'Created by';
COMMENT ON COLUMN targeting_segments.modified_on IS 'Modified on time';
COMMENT ON COLUMN targeting_segments.modified_by IS 'Modified by';