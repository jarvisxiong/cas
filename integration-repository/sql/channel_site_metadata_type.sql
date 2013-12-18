CREATE OR REPLACE TYPE channel_site_metadata_type_modified_on_fix AS
(
    site_id                  CHARACTER VARYING(128),
    pub_id                   CHARACTER VARYING(128),
    modified_on              TIMESTAMP WITHOUT TIME ZONE,
    site_advertiser_incl_list CHARACTER VARYING[],
    pub_advertiser_incl_list CHARACTER VARYING[]
);
ALTER TYPE channel_site_metadata_type_modified_on_fix OWNER TO postgres;
