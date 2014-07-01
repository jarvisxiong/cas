CREATE OR REPLACE TYPE wap_site_uac_type AS
(
    id                      character varying(255),
    site_type_id            bigint,
    content_rating          character varying(255),
    app_type                character varying(255),
    categories              text,
    uac_mod_on              TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE wap_site_uac_type OWNER TO postgres;