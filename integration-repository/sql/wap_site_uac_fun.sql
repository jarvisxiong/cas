CREATE OR REPLACE FUNCTION wap_site_uac_fun_13102014(last_updated timestamp without time zone)
RETURNS SETOF wap_site_uac_type_13102014 AS
$BODY$
DECLARE
    row1    wap_site_uac_type_13102014%ROWTYPE;
BEGIN
    FOR row1 IN

SELECT
				ws.id AS id,
				wpix.id AS pub_id,
				wsu.market_id AS market_id,
    			ws.site_type_id AS site_type_id,
    			wsu.content_rating AS content_rating,
    			wsu.app_type AS app_type,
    			wsu.categories AS categories,
    			wsu.coppa_enabled AS coppa_enabled,
    			wpix.exchange_setting AS exchange_settings,
    			wpix.pub_blind_list AS pub_blind_list,
    			ws.site_blind_list AS site_blind_list,
    			ws.is_site_transparent AS is_site_transparent,
    			ws.site_url AS site_url,
    			ws.site_name AS site_name,
    			wsu.title AS title,
    			wsu.bundle_id AS bundle_id,
    			LEAST(wsu.modified_on, ws.modified_on, wpix.modified_on) AS modified_on
    			from wap_site AS ws LEFT OUTER JOIN wap_site_uac AS wsu ON (ws.id=wsu.id),wap_publisher_ix wpix
    		    where
    		    ( wsu.modified_on >= last_updated or ws.modified_on >= last_updated or wpix.modified_on >= last_updated)
    			and ws.pub_id = wpix.id
    			and ws.status = 'activated'
LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
