CREATE OR REPLACE TYPE wap_channel_adgroup_type_17102012 AS
(
    adgroup_id               CHARACTER VARYING(128),
    ad_id                    CHARACTER VARYING(128),
    channel_id               CHARACTER VARYING(128),
    advertiser_id            CHARACTER VARYING(128),
    external_site_key        CHARACTER VARYING(128),
    platform_targeting_int   BIGINT,
    rc_list                  BIGINT[],
    tags                     BIGINT[],
    status                   BOOLEAN,
    is_test_mode             BOOLEAN,
    modified_on              TIMESTAMP WITHOUT TIME ZONE,
    campaign_id              CHARACTER VARYING(128),
    slot_ids                 BIGINT[],
    inc_id                   BIGINT,
    all_tags                 BOOLEAN,
    pricing_model            character(3),
    targeting_platform       INT,
    site_ratings             INTEGER[],
    os_version_targeting     CHARACTER VARYING(1024)
);
ALTER TYPE wap_channel_adgroup_type_17102012 OWNER TO postgres;
