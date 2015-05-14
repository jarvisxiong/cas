CREATE OR REPLACE FUNCTION wap_channel_fun_05102013(last_updated timestamp without time zone)
RETURNS SETOF wap_channel_type_05102013 AS
$BODY$
DECLARE
    row1    wap_channel_type_05102013%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT  wap_channel.id,
                wap_channel.name,
                wap_channel.account_id,
                wap_channel.reporting_api_key,
                wap_channel.reporting_api_url,
                wap_channel.username,
                wap_channel.password,
                wap_channel.burst_qps,
                wap_channel.is_active,
                wap_channel.is_test_mode,
                GREATEST (wap_channel.modified_on, dcp_channel_site_inclusion_exclusion.modified_on) as modified_on,
                wap_channel.impression_ceil,
                wap_channel.priority,
                wap_channel.demand_source_type_id,
                wap_channel.impression_floor,
                wap_channel.request_cap,
                dcp_channel_site_inclusion_exclusion.sie_json,
		wap_channel.account_segment
		FROM 	wap_channel , dcp_channel_site_inclusion_exclusion
		WHERE   wap_channel.account_id = dcp_channel_site_inclusion_exclusion.account_guid(+)
		AND     (   wap_channel.modified_on >= last_updated
		            OR dcp_channel_site_inclusion_exclusion.modified_on >= last_updated)
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
