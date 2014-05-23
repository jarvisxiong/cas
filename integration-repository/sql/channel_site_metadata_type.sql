CREATE OR REPLACE TYPE channel_site_metadata_type_self_serve AS
(
    site_id                  CHARACTER VARYING(128),
    pub_id                   CHARACTER VARYING(128),
    serve_tp_ads             BOOLEAN,
    allow_self_serve         BOOLEAN,
    modified_on              TIMESTAMP WITHOUT TIME ZONE,
    site_advertiser_incl_list CHARACTER VARYING[],
    pub_advertiser_incl_list CHARACTER VARYING[]
);
ALTER TYPE channel_site_metadata_type_self_serve OWNER TO postgres;
