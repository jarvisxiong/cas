CREATE OR REPLACE FUNCTION wap_channel_fun_25072012(last_updated timestamp without time zone)
RETURNS SETOF wap_channel_type_25072012 AS
$BODY$
DECLARE
    row1    wap_channel_type_25072012%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT id, name, account_id, reporting_api_key, reporting_api_url, username, password, burst_qps, is_active,is_test_mode, 
        modified_on, impression_ceil, priority, demand_source_type_id, impression_floor
        FROM wap_channel 
        WHERE  modified_on >= last_updated
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
