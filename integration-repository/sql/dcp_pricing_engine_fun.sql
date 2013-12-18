CREATE OR REPLACE FUNCTION dcp_pricing_engine_sdc_fun(last_updated timestamp without time zone)        
RETURNS SETOF dcp_pricing_engine_sdc_type AS                                                                                                                             
$BODY$
DECLARE
    row1    dcp_pricing_engine_sdc_type%ROWTYPE;
BEGIN
    FOR row1 IN

        SELECT
               dcp_pricing_engine.country_id as country_id,
               dcp_pricing_engine.os_id as os_id,
               dcp_pricing_engine.rtb_floor as rtb_floor,
	           dcp_pricing_engine.dcp_floor as dcp_floor,
	           dcp_pricing_engine.supply_demand_json as supply_demand_json,
	           dcp_pricing_engine.modified_on as modified_on
               FROM dcp_pricing_engine
               WHERE dcp_pricing_engine.modified_on >= last_updated
 	LOOP
        RETURN NEXT row1;
    END LOOP;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;