CREATE OR REPLACE FUNCTION currency_conversion_fun(last_updated timestamp without time zone)
RETURNS SETOF currency_conversion_type AS
$BODY$
DECLARE
    row1    currency_conversion_type%ROWTYPE;
BEGIN
    FOR row1 IN
        SELECT  id,
          currency_id,
          conversion_rate,
          start_date,
          end_date,
          modified_on from currency_conversion_rate
          where is_deleted = 'f' and end_date = to_timestamp('30-DEC-9999 00:00:00', 'DD-Mon-YYYY')
          and modified_on >= last_updated
 LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;
