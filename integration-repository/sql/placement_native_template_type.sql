CREATE OR REPLACE TYPE placement_native_template_type AS
(
    placement_id            BIGINT,
    native_template_id      BIGINT,
    ui_layout_id            SMALLINT,
    content_json            TEXT,
    binary_template         VARCHAR,
    modified_on             TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE placement_native_template_type OWNER TO postgres;