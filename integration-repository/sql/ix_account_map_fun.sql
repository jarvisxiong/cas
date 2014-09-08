CREATE OR REPLACE FUNCTION ix_account_map_fun(last_updated timestamp without time zone)
RETURNS SETOF ix_account_map_type AS
$BODY$
DECLARE
    row1    ix_account_map_type%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT  
				 rp_network_id,
                 inmobi_account_id,
                 network_name,
                 network_type,
                 modified_on             
    	from ix_account_map
        where
    			modified_on >=last_updated
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;