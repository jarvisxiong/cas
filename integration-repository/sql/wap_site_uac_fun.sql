CREATE OR REPLACE FUNCTION wap_site_uac_fun(last_updated timestamp without time zone)
RETURNS SETOF wap_site_uac_type AS
$BODY$
DECLARE
    row1    wap_site_uac_type%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT  
        	id,
    		site_type_id,
    		content_rating,
    		app_type,
    		categories,
    		uac_mod_on from wap_site_uac
    		where content_rating != '' or app_type !='' or categories != ''
    		or content_rating is not null or app_type is not null or categories is not null
    		and uac_mod_on >=last_updated
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
