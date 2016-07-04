-- modifying old ix packages function
CREATE FUNCTION ix_package_fun_03052016()
RETURNS
    SETOF ix_package_type_21042016 AS
$BODY$
DECLARE
    row1    ix_package_type_21042016%ROWTYPE;
BEGIN
    FOR row1 IN

SELECT
                ix_packages.id AS id,
                ix_packages.name AS name,
                ix_packages.rp_data_segment_id AS rp_data_segment_id,
                ix_packages.pmp_class AS pmp_class,
                ix_packages.country_ids AS country_ids,
                ix_packages.inventory_types AS inventory_types,
                ix_packages.os_ids AS os_ids,
                ix_packages.os_version_targeting AS os_version_targeting,
                ix_packages.manuf_model_targeting AS manuf_model_targeting,
                ix_packages.viewable,
                ix_packages.language_targeting_list,
                ix_packages.sdk_version_targeting,
                ix_packages.carrier_ids AS carrier_ids,
                ix_packages.site_categories AS site_categories,
                ix_packages.connection_types AS connection_types,
                ix_packages.geo_source_types AS geo_source_types,
                ix_packages.geo_fence_region AS geo_fence_region,
                ix_packages.app_store_categories AS app_store_categories,
                ix_packages.lat_long_only AS lat_long_only,
                ix_packages.zip_code_only AS zip_code_only,
                ix_packages.ifa_only AS ifa_only,
                ix_packages.site_ids AS site_ids,
                ix_packages.data_vendor_id AS data_vendor_id,
                ix_packages.dmp_id AS dmp_id,
                ix_packages.dmp_filter_expression AS dmp_filter_expression,
                ix_packages.zip_codes AS zip_codes,
                ix_packages.cs_ids AS cs_ids,
                ix_packages.min_bid AS min_bid,
                ix_packages.scheduled_tods AS scheduled_tods,
                ix_packages.placement_slot_ids AS placement_slot_ids,
                ix_packages.ad_types,
                ix_packages.is_active AS is_active,
                ix_packages.start_date AS start_date,
                ix_packages.end_date AS end_date,
                ix_packages.last_modified AS last_modified,
                ix_packages.data_vendor_cost AS data_vendor_cost,
                ix_packages.city_ids AS city_ids,
                ix_packages.modified_by AS modified_by,
                CASE
	                When ix_packages.geocookie_status = true THEN ix_packages.geocookie_id
	            ELSE null
	        END AS geocookie_id,
                deals.access_types as access_types,
                deals.deal_ids AS deal_ids,
                deals.deal_floors AS deal_floors,
                deals.rp_agency_ids AS rp_agency_ids,
                deals.agency_rebate_percentages AS agency_rebate_percentages,
                deals.third_party_tracker_json_list AS third_party_tracker_json_list
                from ix_packages JOIN (
                    select
                        makeList(rp_deal_id) AS deal_ids,
                        makeList(deal_floor) AS deal_floors,
                        makeList(access_type) AS access_types,
                        makeList(rp_agency_id) AS rp_agency_ids,
                        makeList(agency_rebate_percentage) AS agency_rebate_percentages,
                        makeList(third_party_tracker_json) AS third_party_tracker_json_list,
                        package_id
                    from ix_package_deals
                    where (start_date is null or start_date <= now()+interval '1 minute')
                    and (end_date is null or end_date >= now())
                    group by package_id
                )
                AS deals ON deals.package_id = ix_packages.id
                where is_active=true
                and (targeting_segment_ids is null or COALESCE(array_length(targeting_segment_ids, 1), 0) = 0)
                and (start_date is null or start_date <= now()+interval '1 minute')
                and (end_date is null or end_date >= now())
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
