    CREATE OR REPLACE TYPE wap_site_uac_type_08092014_transparencyFinal AS
    (
        id                      character varying(255),
        pub_id                  character varying(255),
        market_id               character varying(255),
        site_type_id            bigint,
        content_rating          character varying(255),
        app_type                character varying(255),
        categories              text,
        coppa_enabled           boolean,
        exchange_settings       int,
        pub_block_list          int[],
        site_block_list         int[],
        is_site_transparent     boolean,
        site_url                text,
        site_name               text,
        title               text,
        wsu_modified_on         TIMESTAMP WITHOUT TIME ZONE,
        ws_modified_on         TIMESTAMP WITHOUT TIME ZONE
    );
    ALTER TYPE wap_site_uac_type_08092014_transparencyFinal OWNER TO postgres;