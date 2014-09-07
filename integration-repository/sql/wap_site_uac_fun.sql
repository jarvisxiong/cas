
CREATE OR REPLACE FUNCTION wap_site_uac_fun_04092014(last_updated timestamp without time zone)
RETURNS SETOF wap_site_uac_type_04092014 AS
$BODY$
DECLARE
    row1    wap_site_uac_type_04092014%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT  
				wsu.id AS id,
				wp.id AS pub_id,
				wsu.market_id AS market_id,
    			wsu.site_type_id AS site_type_id,
    			wsu.content_rating AS content_rating,
    			wsu.app_type AS app_type,
    			wsu.categories AS categories,
    			wsu.coppa_enabled AS coppa_enabled,
    			wp.is_transparency_enabled AS is_transparency_enabled,
    			wp.is_exchange_enabled AS is_exchange_enabled,
    			wp.pub_block_list AS pub_block_list,
    			ws.site_block_list AS site_block_list,
    			ws.site_url AS site_url,
    			wsu.modified_on
    			from wap_site_uac wsu,wap_site ws,wap_publisher wp
    		    where
    			   ((wsu.content_rating is not null and wsu.content_rating != '')
    			or (wsu.app_type is not null and wsu.app_type !='')
    			or (wsu.categories is not null and wsu.categories != '')
    			or wsu.coppa_enabled != false)
    			and wsu.modified_on >=last_updated
    			and ws.id = wsu.id
    			and ws.pub_id = wp.id
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;