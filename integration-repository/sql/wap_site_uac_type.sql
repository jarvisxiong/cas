CREATE OR REPLACE TYPE wap_site_uac_type_coppa AS
(
    id                      character varying(255),
    market_id               character varying(255),
    site_type_id            bigint,
    content_rating          character varying(255),
    app_type                character varying(255),
    categories              text,
    coppa_enabled           boolean,
    modified_on             TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE wap_site_uac_type_coppa OWNER TO postgres;