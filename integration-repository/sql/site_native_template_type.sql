CREATE OR REPLACE TYPE site_native_template_type_07052015 AS
(
    site_id                 CHARACTER VARYING(128),
    native_ad_id            BIGINT,
    ui_layout_id            SMALLINT,
    content_json            TEXT,
    binary_template         VARCHAR,
    modified_on             TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE site_native_template_type_07052015 OWNER TO postgres;
