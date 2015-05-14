CREATE OR REPLACE FUNCTION channel_site_metadata_fun_self_serve(last_updated timestamp without time zone)
RETURNS SETOF channel_site_metadata_type_self_serve AS
$BODY$
DECLARE
    sitemetadatarow    channel_site_metadata_type_self_serve%ROWTYPE;
BEGIN
    FOR sitemetadatarow IN
        SELECT  a.id,
                a.pub_id,
                serve_tp_ads,
                allow_self_serve,
                GREATEST(a.modified_on, b.modified_on, c.modified_on),
                b.advertiser_incl_list as site_advertiser_incl_list,
                c.advertiser_incl_list as pub_advertiser_incl_list
        FROM    (
                    SELECT  id,
                            pub_id,
                            serve_tp_ads,
                            allow_self_serve,
                            modified_on
                    FROM wap_site where status = 'activated'
                ) as a,
                dcp_site_advertiser_preference as b,
                publisher_advertiser_preference as c
        WHERE   a.id = b.site_id(+) AND a.pub_id = c.pub_id(+)
        AND     (
                    a.modified_on >= last_updated
                    OR b.modified_on >= last_updated
                    OR c.modified_on >= last_updated
                )
LOOP
        RETURN NEXT sitemetadatarow;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
