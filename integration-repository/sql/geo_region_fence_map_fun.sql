CREATE OR REPLACE FUNCTION geo_region_fence_map_fun(last_updated timestamp without time zone)
RETURNS SETOF geo_region_fence_map_type AS
$BODY$
DECLARE
    row1    geo_region_fence_map_type%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT gcr.name as geo_region_name,
               gtd.country_id as country_id,
               gtd.lat_long_id_list as fence_id_list,
               GREATEST(gcr.modified_on, gtd.modified_on) as modified_on
        FROM geo_custom_region as gcr, geo_targeting_detail as gtd
        WHERE ARRAY[gtd.id] <@ gcr.targeting_ids
          AND gcr.is_active = 't'
          AND gcr.applicable_account_id_list = '{1b1bdc749bd44a6c9037e513701ee815}'
          AND (gcr.modified_on >= last_updated OR gtd.modified_on >= last_updated)
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;


