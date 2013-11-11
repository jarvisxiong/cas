CREATE OR REPLACE FUNCTION publisher_filter_repo_fun(last_updated timestamp without time zone)        
RETURNS SETOF publisher_filter_repo_type AS                                                                                                                             
$BODY$
DECLARE
    row1    publisher_filter_repo_type%ROWTYPE;
BEGIN
    FOR row1 IN

        SELECT
               publisher_filters.pub_id as pub_id,
               publisher_filters.site_id as site_id,
               publisher_filters.rule_type_id as rule_type_id,
               publisher_filters.filter_data as filter_data,
               publisher_filters.is_exact as is_exact,
               publisher_filters.modified_on as modified_on,     
	       publisher_filters.is_expired as is_expired
               FROM publisher_filters
               WHERE publisher_filters.modified_on >= last_updated
 	LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION publisher_filter_repo_fun(timestamp without time zone) OWNER TO postgres;
