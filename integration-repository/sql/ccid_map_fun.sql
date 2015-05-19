CREATE OR REPLACE FUNCTION ccid_map_fun(last_updated timestamp without time zone)
RETURNS SETOF ccid_map_type AS
$BODY$
DECLARE
    row1    ccid_map_type%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT country_carrier_id,
               country,
               carrier
        FROM country_carrier_map
        WHERE country_carrier_id >= 0
          AND carrier NOT IN ('WiFi', 'Others')
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;