CREATE OR REPLACE TYPE ccid_map_type AS
(
    country_carrier_id    INTEGER,
    country               CHARACTER VARYING(150),
    carrier               CHARACTER VARYING(100)
);
ALTER TYPE ccid_map_type OWNER TO postgres;