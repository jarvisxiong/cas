CREATE OR REPLACE FUNCTION ix_blocklists_fun(last_updated timestamp without time zone)
RETURNS SETOF ix_blocklists_type AS
$BODY$
DECLARE
    row1 ix_blocklists_type%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT blocklist_name,
               key_id,
               key_type,
               filter_data_type,
               array_length(filter_data, 1),
               modified_on
        FROM ix_blocklists
        WHERE is_active = true
          AND 0 != key_type
          AND modified_on >= last_updated
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;