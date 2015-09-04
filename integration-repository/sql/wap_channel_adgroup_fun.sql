CREATE OR REPLACE FUNCTION wap_channel_adgroup_fun_28082015(last_updated timestamp without time zone)
RETURNS SETOF wap_channel_adgroup_type_28082015 AS
$BODY$
DECLARE
    row1    wap_channel_adgroup_type_28082015%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT  wap_channel_adgroup.adgroup_id,
                adgroup_formats.ad_ids,
                wap_channel_adgroup.channel_id,
                wap_channel_adgroup.advertiser_id,
                wap_channel_adgroup.external_site_key,
                wap_channel_adgroup.platform_targeting_int,
                wap_channel_adgroup.rc_list, tags, wap_channel_adgroup.status, is_test_mode,
                GREATEST (wap_channel_adgroup.modified_on, dcp_segment_site_inclusion_exclusion.modified_on) as modified_on,
                wap_channel_adgroup.campaign_id,
                wap_channel_adgroup.slot_ids,
                wap_channel_adgroup.ad_type_targeting,
                adgroup_formats.ad_inc_ids as ad_inc_ids,
                wap_channel_adgroup.all_targeting_tags as all_tags,
                wap_channel_adgroup.pricing_model,
                wap_channel_adgroup.targeting_platform as targeting_platform,
                wap_channel_adgroup.site_ratings as site_ratings,
                wap_channel_adgroup.os_version_targeting as os_version_targeting,
                wap_channel_adgroup.category_taxomony as category_taxomony,
                wap_channel_adgroup.segment_flags as segment_flags,
                wap_channel_adgroup.additional_params as additional_params,
                wap_channel_adgroup.adgroup_inc_id as adgroup_inc_id,
                dcp_segment_site_inclusion_exclusion.sie_json,
                wap_channel_adgroup.impression_ceil as impression_ceil,
                wap_channel_adgroup.manuf_model_targeting as manuf_model_targeting,
                wap_channel_adgroup.ecpm_boost as ecpm_boost,
                wap_channel_adgroup.boost_date as boost_date,
                wap_channel_adgroup.tod as tod,
                wap_channel_adgroup.dst as dst,
                wap_channel_adgroup.campaign_inc_id,
                adgroup_formats.ad_format_ids,
                wap_channel_adgroup.automation_test_id as automation_test_id
          FROM  wap_channel_adgroup, dcp_segment_site_inclusion_exclusion,
                (SELECT ad_group_id,
                        array_agg(id order by is_banner_ad asc) as ad_ids,
                        array_agg(inc_id order by is_banner_ad asc) as ad_inc_ids,
                        array_agg(is_banner_ad order by is_banner_ad asc) as ad_format_ids
                   FROM wap_channel_ad
                   GROUP BY ad_group_id) as adgroup_formats
         WHERE  wap_channel_adgroup.adgroup_id = dcp_segment_site_inclusion_exclusion.adgroup_guid(+)
           AND  adgroup_formats.ad_group_id = wap_channel_adgroup.adgroup_id
           AND  (wap_channel_adgroup.modified_on >= last_updated
                 OR dcp_segment_site_inclusion_exclusion.modified_on >= last_updated)
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
