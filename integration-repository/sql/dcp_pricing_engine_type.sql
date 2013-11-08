CREATE OR REPLACE TYPE dcp_pricing_engine_sdc_type AS
(
country_id INTEGER,
os_id INTEGER,
rtb_floor DOUBLE PRECISION,
dcp_floor DOUBLE PRECISION,
supply_demand_json VARCHAR,
modified_on TIMESTAMP WITHOUT TIME ZONE
);