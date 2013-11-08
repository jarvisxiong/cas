CREATE OR REPLACE FUNCTION site_ecpm_fun(last_updated timestamp without time zone)        
RETURNS SETOF site_ecpm_type AS                                                                                                                             
$BODY$
DECLARE
    row1    site_ecpm_type%ROWTYPE;
BEGIN
    FOR row1 IN
                SELECT  a.site_id,
                        a.country_id,
                        c.id as os_id,
                        a.ecpm,
                        a.network_ecpm,
                        a.modified_on
                FROM    site_country_os_ecpm a,
                        (SELECT id
                         FROM   wap_site
                         WHERE  serve_tp_ads = 't') b,
                         handset_device_os c
                WHERE   a.site_id = b.id
                AND     a.operating_system = c.name

 	LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;