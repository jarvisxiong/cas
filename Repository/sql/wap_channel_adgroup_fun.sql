CREATE OR REPLACE FUNCTION wap_channel_adgroup_fun_17102012(last_updated timestamp without time zone)
RETURNS SETOF wap_channel_adgroup_type_17102012 AS
$BODY$
DECLARE
    row1    wap_channel_adgroup_type_17102012%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT adgroup_id, ad_id, channel_id, advertiser_id, external_site_key, platform_targeting_int, rc_list, tags, status,is_test_mode, 
        modified_on, campaign_id, slot_ids, ad_inc_id as inc_id, all_targeting_tags as all_tags, pricing_model,
        targeting_platform as targeting_platform, site_ratings as site_ratings, os_version_targeting as os_version_targeting
        FROM wap_channel_adgroup 
        WHERE  modified_on >= last_updated
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
