CREATE FUNCTION packages_func_V2()
RETURNS
    SETOF packages_type_V2 AS
$BODY$
DECLARE
    row1    packages_type_V2%ROWTYPE;
BEGIN
    FOR row1 IN

WITH packages_filtered AS (
    SELECT
        id AS package_id,
        last_modified AS package_modified_on,
        viewable AS package_enforce_viewability_sdks,
        targeting_segment_ids AS targeting_segment_ids
    FROM ix_packages
        WHERE is_active = true
            AND COALESCE(array_length(targeting_segment_ids, 1), 0) > 0
            AND (start_date is null or start_date <= now() + interval '1 minute')
            AND (end_date is null or end_date >= now())
), deals_filtered_agg AS (
    SELECT
         package_id as package_id,
         array_agg(rp_deal_id) AS deal_ids,
         array_agg(deal_floor) AS deal_floors,
         array_agg(deal_floor_cur) AS deal_floor_curs,
         array_agg(access_type) AS deal_types,
         array_agg(auction_type::TEXT) AS deal_auction_types,
         array_agg(dst) AS deal_dsts,
         array_agg(dsp_account_id) AS deal_dsp_account_ids,
         array_agg(third_party_tracker_json) AS deal_third_party_tracker_jsons,
         array_agg(bill_on_viewability) AS deal_bill_on_viewability_flags,
         array_agg(agency_rebate_percentage) AS deal_agency_rebate_percentages,
         array_agg(rp_agency_id) AS deal_external_agency_ids
    FROM ix_package_deals
        WHERE (start_date is null or start_date <= now() + interval '1 minute')
            AND (end_date is null or end_date >= now())
        GROUP BY package_id
), packages_targeting_segments_join AS (
    SELECT
        packages_filtered.package_id AS package_id,
        packages_filtered.package_modified_on AS package_modified_on,
        packages_filtered.package_enforce_viewability_sdks AS package_enforce_viewability_sdks,
        -- Targeting Segments
        array_agg(targeting_segments.id) as targeting_segment_ids,
        array_agg(inventory_types_list_is_inclusion) AS ts_inventory_types_list_is_inclusion,
        array_agg(inventory_types::TEXT) AS ts_inventory_types,
        array_agg(site_content_types_list_is_inclusion) AS ts_site_content_types_list_is_inclusion,
        array_agg(site_content_types::TEXT) AS ts_site_content_types,
        array_agg(connection_types_list_is_inclusion) AS ts_connection_types_list_is_inclusion,
        array_agg(connection_types::TEXT) AS ts_connection_types,
        array_agg(location_sources_list_is_inclusion) AS ts_location_sources_list_is_inclusion,
        array_agg(location_sources::TEXT) AS ts_location_sources,
        array_agg(integration_methods_list_is_inclusion) AS ts_integration_methods_list_is_inclusion,
        array_agg(integration_methods::TEXT) AS ts_integration_methods,
        array_agg(sdk_version_json::TEXT) AS ts_sdk_version_json,
        array_agg(os_version_json::TEXT) AS ts_os_version_json,
        array_agg(country_city_json::TEXT) AS ts_country_city_json,
        array_agg(manuf_model_json::TEXT) AS ts_manuf_model_json,
        array_agg(publisher_list_is_inclusion) AS ts_publisher_list_is_inclusion,
        array_agg(publishers::TEXT) AS ts_publishers,
        array_agg(site_list_is_inclusion) AS ts_site_list_is_inclusion,
        array_agg(sites::TEXT) AS ts_sites,
        array_agg(carrier_list_is_inclusion) AS ts_carrier_list_is_inclusion,
        array_agg(carriers::TEXT) AS ts_carriers,
        array_agg(language_list_is_inclusion) AS ts_language_list_is_inclusion,
        array_agg(languages::TEXT) AS ts_languages,
        array_agg(geo_region_is_inclusion) AS ts_geo_region_is_inclusion,
        array_agg(geo_fence_region) AS ts_geo_fence_region,
        array_agg(slots_list_is_inclusion) AS ts_slots_list_is_inclusion,
        array_agg(slots::TEXT) AS ts_slots,
        array_agg(ad_types_list_is_inclusion) AS ts_ad_types_list_is_inclusion,
        array_agg(ad_types::TEXT) AS ts_ad_types,
        array_agg(csid_filter_expression) AS ts_csid_filter_expression,
        array_agg(data_vendor_cost) AS ts_data_vendor_cost
    FROM packages_filtered
        INNER JOIN targeting_segments ON (targeting_segments.id = ANY(packages_filtered.targeting_segment_ids))
    GROUP BY
        packages_filtered.package_id,
        packages_filtered.package_modified_on,
        packages_filtered.package_enforce_viewability_sdks
)
SELECT
    -- Package
    packages_targeting_segments_join.package_id,
    package_modified_on,
    package_enforce_viewability_sdks,
    -- Targeting Segments
    targeting_segment_ids,
    ts_inventory_types_list_is_inclusion,
    ts_inventory_types,
    ts_site_content_types_list_is_inclusion,
    ts_site_content_types,
    ts_connection_types_list_is_inclusion,
    ts_connection_types,
    ts_location_sources_list_is_inclusion,
    ts_location_sources,
    ts_integration_methods_list_is_inclusion,
    ts_integration_methods,
    ts_sdk_version_json,
    ts_os_version_json,
    ts_country_city_json,
    ts_manuf_model_json,
    ts_publisher_list_is_inclusion,
    ts_publishers,
    ts_site_list_is_inclusion,
    ts_sites,
    ts_carrier_list_is_inclusion,
    ts_carriers,
    ts_language_list_is_inclusion,
    ts_languages,
    ts_geo_region_is_inclusion,
    ts_geo_fence_region,
    ts_slots_list_is_inclusion,
    ts_slots,
    ts_ad_types_list_is_inclusion,
    ts_ad_types,
    ts_csid_filter_expression,
    ts_data_vendor_cost,
    -- Deals
    deal_ids,
    deal_floors,
    deal_floor_curs,
    deal_types,
    deal_auction_types,
    deal_dsts,
    deal_dsp_account_ids,
    deal_third_party_tracker_jsons,
    deal_bill_on_viewability_flags,
    deal_agency_rebate_percentages,
    deal_external_agency_ids

    FROM packages_targeting_segments_join
        INNER JOIN deals_filtered_agg ON packages_targeting_segments_join.package_id = deals_filtered_agg.package_id

LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;