CREATE OR REPLACE FUNCTION placement_native_template_fun_movieboard(last_updated timestamp without time zone)
RETURNS SETOF placement_native_template_type AS
$BODY$
DECLARE
    row1 placement_native_template_type%ROWTYPE;
BEGIN
     FOR row1 IN
	    SELECT
                    placement_templates.placement_id,
                    placement_templates.template_id,
                    placement_templates.ui_layout_id,
                    placement_templates.content_json,
                    at.binary_template,
                    GREATEST(at.modified_on, placement_templates.modified_on) as modified_on
                FROM
                    ad_template as at,
                    (SELECT
                        pnt.placement_id AS placement_id,
                        pnt.template_id AS template_id,
                        pnt.ui_layout_id AS ui_layout_id,
                        pnt.content_json AS content_json,
                        pnt.modified_on AS modified_on
                    FROM
                        placement_native_template as pnt
                    WHERE
                        pnt.status = '1'
                    UNION ALL
                    SELECT
                        pct.placement_id AS placement_id,
                        pct.template_id AS template_id,
                        NULL as ui_layout_id,
                        NULL as content_json,
                        pct.modified_on AS modified_on
                        FROM
                            placement_custom_template as pct
                        WHERE
                            pct.status = '1')
                    AS placement_templates
                WHERE
                    placement_templates.template_id = at.id
                AND (at.modified_on >= last_updated
                	OR placement_templates.modified_on >= last_updated)
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;