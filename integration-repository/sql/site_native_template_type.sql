CREATE OR REPLACE TYPE site_native_template_type AS
(
    site_id                 CHARACTER VARYING(128),
    native_ad_id            BIGINT,
    binary_template         VARCHAR,
    modified_on             TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE site_native_template_type OWNER TO postgres;
