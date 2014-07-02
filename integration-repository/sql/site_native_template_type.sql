CREATE OR REPLACE TYPE site_native_template_type_05102013 AS
(
    site_id                 CHARACTER VARYING(128),
    native_ad_id            BIGINT,
    binary_template         VARCHAR
);
ALTER TYPE site_native_template_type_05102013 OWNER TO postgres;
