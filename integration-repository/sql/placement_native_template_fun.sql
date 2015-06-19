CREATE OR REPLACE FUNCTION placement_native_template_fun(last_updated timestamp without time zone)
RETURNS SETOF placement_native_template_type AS
$BODY$
DECLARE
    row1 placement_native_template_type%ROWTYPE;
BEGIN
     FOR row1 IN
	    SELECT
	        pnt.placement_id,
	        pnt.template_id,
	        pnt.ui_layout_id,
	        pnt.content_json,
	        at.binary_template,
	        GREATEST(at.modified_on, pnt.modified_on)
	    FROM placement_native_template as pnt, ad_template as at
	    WHERE
	        pnt.template_id = at.id
	    AND pnt.status = '1'
	    AND (at.modified_on >= last_updated
	        OR pnt.modified_on >= last_updated)
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;