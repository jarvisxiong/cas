CREATE OR REPLACE FUNCTION site_ecpm_fun(last_updated timestamp without time zone)        
RETURNS SETOF site_ecpm_type AS                                                                                                                             
$BODY$
DECLARE
    row1    site_ecpm_type%ROWTYPE;
BEGIN
    FOR row1 IN
                SELECT  scoe.site_id,
                        scoe.country_id,
                        hdo.id as os_id,
                        scoe.ecpm,
                        scoe.network_ecpm,
                        GREATEST(scoe.modified_on, hdo.modified_on, ws.modified_on) AS modified_on
                FROM    site_country_os_ecpm scoe,
                		handset_device_os hdo,
                		wap_site ws
                WHERE   (scoe.modified_on >= last_updated or hdo.modified_on >= last_updated or ws.modified_on >= last_updated)
                AND     ws.serve_tp_ads = 't'
                AND     scoe.site_id = ws.id
                AND     scoe.operating_system = hdo.name

 	LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;