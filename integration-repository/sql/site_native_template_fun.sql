CREATE OR REPLACE FUNCTION site_native_template_fun_07052015(last_updated timestamp without time zone)
RETURNS SETOF site_native_template_type_07052015 AS
$BODY$
DECLARE
    row1 site_native_template_type_07052015%ROWTYPE;
BEGIN
	FOR row1 IN
		SELECT
     		sna.site_id,
     		sna.native_ad_id,
     		ncu.ui_layout_id,
     		ncu.content_json,
     		at.binary_template,
     		GREATEST(at.modified_on, sna.modified_on, ncu.modified_on) as modified_on
    	FROM site_native_ad_settings as sna, ad_template as at, native_content_unit as ncu
     	WHERE
     		sna.native_ad_id = at.id
     		AND at.id = ncu.template_id(+)
     		AND sna.type in ('native_content_template', 'native_content_unit')
     		AND sna.status = 'active'
     		AND (sna.modified_on >= last_updated
     			OR at.modified_on >= last_updated
     			OR ncu.modified_on >= last_updated)
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
