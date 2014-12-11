CREATE OR REPLACE FUNCTION site_native_template_fun_28112014(last_updated timestamp without time zone)
RETURNS SETOF site_native_template_type AS
$BODY$
DECLARE
    row1    site_native_template_type%ROWTYPE;
BEGIN
     FOR row1 IN
	 SELECT 
	 sna.site_id,
	 sna.native_ad_id,
	 at.binary_template,
	 at.modified_on from site_native_ad_settings sna, ad_template at WHERE
	 sna.native_ad_id = at.id
	 and sna.type in ('native_content_template', 'native_content_unit')
	 and sna.status = 'active'
	 and at.modified_on >= last_updated
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
