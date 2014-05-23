CREATE OR REPLACE FUNCTION rtbd_creative_fun(last_updated timestamp without time zone)
RETURNS SETOF rtbd_creative_type AS
$BODY$
DECLARE
    row1    rtbd_creative_type%ROWTYPE;
BEGIN
    FOR row1 IN
	 select advertiser_id, creative_id, exposure_level, sample_url
	 from rtbd_creative
	 WHERE rtbd_creative.modified_on >= last_updated
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
