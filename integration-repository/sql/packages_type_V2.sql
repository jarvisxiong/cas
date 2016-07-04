CREATE TYPE packages_type_V2 AS
(
    -- package fields
    package_id                          INTEGER,
    package_modified_on                 TIMESTAMP WITHOUT TIME ZONE,
    package_enforce_viewability_sdks    BOOLEAN,

    -- Reading array of arrays as an array of text for now as
    -- array aggregating multi-dimensional arrays requires all sub-arrays to have the same dimensions
    -- Not using arrays of composite types as JDBC postgres driver doesn't support the SQLData Interface

    -- targeting segment fields
    targeting_segment_ids                       BIGINT[],
    ts_inventory_types_list_is_inclusion        BOOLEAN[],
    ts_inventory_types                          TEXT[], --rtb_inventory_type[][],
    ts_site_content_types_list_is_inclusion     BOOLEAN[],
    ts_site_content_types                       TEXT[], --rtb_site_content_type[][],
    ts_connection_types_list_is_inclusion       BOOLEAN[],
    ts_connection_types                         TEXT[], --rtb_connection_type[][],
    ts_location_sources_list_is_inclusion       BOOLEAN[],
    ts_location_sources                         TEXT[], --rtb_location_source_type[][],
    ts_integration_methods_list_is_inclusion    BOOLEAN[],
    ts_integration_methods                      TEXT[], --rtb_integration_method[][],

    ts_sdk_version_json                         TEXT[],
    ts_os_version_json                          TEXT[],
    ts_country_city_json                        TEXT[],
    ts_manuf_model_json                         TEXT[],

    ts_publisher_list_is_inclusion              BOOLEAN[],
    ts_publishers                               TEXT[], --TEXT[][],
    ts_site_list_is_inclusion                   BOOLEAN[],
    ts_sites                                    TEXT[], --TEXT[][],
    ts_carrier_list_is_inclusion                BOOLEAN[],
    ts_carriers                                 TEXT[], --LONG[][],
    ts_language_list_is_inclusion               BOOLEAN[],
    ts_languages                                TEXT[], --TEXT[][],

    ts_geo_region_is_inclusion                  BOOLEAN[],
    ts_geo_fence_region                         VARCHAR(255)[],
    ts_slots_list_is_inclusion                  BOOLEAN[],
    ts_slots                                    TEXT[], --SMALLINT[][],
    ts_ad_types_list_is_inclusion               BOOLEAN[],
    ts_ad_types                                 TEXT[], --INTEGER[][],
    ts_csid_filter_expression                   TEXT[],
    ts_data_vendor_cost                         DOUBLE PRECISION[],

    -- deal fields
    deal_ids                                    VARCHAR(100)[],
    deal_floors                                 DOUBLE PRECISION[],
    deal_floor_curs                             CHAR(3)[],
    deal_types                                  VARCHAR(32)[],
    deal_auction_types                          TEXT[],
    deal_dsts                                   INTEGER[],
    deal_dsp_account_ids                        VARCHAR(128)[],
    deal_third_party_tracker_jsons              TEXT[],
    deal_bill_on_viewability_flags              BOOLEAN[],
    deal_agency_rebate_percentages              DOUBLE PRECISION[],
    deal_external_agency_ids                    INTEGER[]
);