
CREATE OR REPLACE FUNCTION site_filter_repo_fun_20141219(last_updated timestamp without time zone)
RETURNS SETOF site_filter_repo_type AS
$BODY$
DECLARE
    row1 site_filter_repo_type%ROWTYPE;
BEGIN
    FOR row1 IN

        SELECT site_filters.pub_id AS pub_id,
               site_filters.site_id AS site_id,
               site_filters.rule_type_id AS rule_type_id,
               site_filters.filter_data AS filter_data,
               site_filters.is_exact AS is_exact,
               site_filters.modified_on AS modified_on
          FROM site_filters
         WHERE site_filters.modified_on >= last_updated

    LOOP
        --
        -- For rule_type_id 4 (category filters), fetch and return the IAB standard Ids.
        --
        IF row1.rule_type_id = 4 AND row1.filter_data != '{}' THEN
            SELECT array_agg(distinct iabt.iab_standard_id)
              INTO row1.filter_data
              FROM adgroup_taxonomy agt, iab_taxonomy iabt
             WHERE agt.iab_id = iabt.id
               AND agt.id in (select unnest(row1.filter_data));
        END IF;
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE plpgsql;

ALTER FUNCTION site_filter_repo_fun_20141219(timestamp without time zone) OWNER TO postgres;
