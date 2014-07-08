CREATE OR REPLACE FUNCTION site_native_template(last_update timestamp without time zone)
RETURNS SETOF site_native_template_type_05102013 AS
$BODY$
DECLARE
    row1    site_native_template_type_05102013%ROWTYPE;
BEGIN
    FOR row1 IN
	 select site_id, native_ad_id, binary_template
	 from site_native_ad_settings,ad_template
	 WHERE site_native_ad_settings.native_ad_id = ad_template.id
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;