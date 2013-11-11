CREATE OR REPLACE TYPE wap_channel_type_05102013 AS
(
    id                      CHARACTER VARYING(128),
    name                    CHARACTER VARYING(128),
    account_id              CHARACTER VARYING(128),
    reporting_api_key       CHARACTER VARYING(128),
    reporting_api_url       CHARACTER VARYING(128),
    username                CHARACTER VARYING(128),
    password                CHARACTER VARYING(128),
    burst_qps               BIGINT,
    is_active               BOOLEAN,
    is_test_mode            BOOLEAN,
    modified_on             TIMESTAMP WITHOUT TIME ZONE,
    impression_ceil         BIGINT,
    priority                INTEGER,
    demand_source_type_id   INTEGER,
    impression_floor        BIGINT,
    request_cap             BIGINT,
    sie_json                VARCHAR,
    account_segment	    INTEGER
);
ALTER TYPE wap_channel_type_05102013 OWNER TO postgres;
