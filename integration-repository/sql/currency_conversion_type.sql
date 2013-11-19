CREATE OR REPLACE TYPE currency_conversion_type AS
(
    id                      INTEGER,
    currency_id             CHARACTER VARYING(3),
    conversion_rate         DOUBLE PRECISION,
    start_date              TIMESTAMP WITHOUT TIME ZONE,
    end_date                TIMESTAMP WITHOUT TIME ZONE,
    modified_on             TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE currency_conversion_type OWNER TO postgres;
