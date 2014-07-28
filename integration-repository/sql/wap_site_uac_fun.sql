CREATE OR REPLACE FUNCTION wap_site_uac_fun_coppa(last_updated timestamp without time zone)
RETURNS SETOF wap_site_uac_type_coppa AS
$BODY$
DECLARE
    row1    wap_site_uac_type_coppa%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT  
				id,       
				market_id,             
    			site_type_id,           
    			content_rating,          
    			app_type,              
    			categories,              
    			coppa_enabled,          
    			modified_on from wap_site_uac where
    			   (content_rating is not null and content_rating != '')  
    			or (app_type is not null and app_type !='')  
    			or (categories is not null and categories != '')
    			or coppa_enabled != false
    			and modified_on >=last_updated
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;