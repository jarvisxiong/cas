    CREATE OR REPLACE TYPE wap_site_uac_type_04092014 AS
    (
        id                      character varying(255),
        pub_id                  character varying(255),
        market_id               character varying(255),
        site_type_id            bigint,
        content_rating          character varying(255),
        app_type                character varying(255),
        categories              text,
        coppa_enabled           boolean,
        is_transparency_enabled boolean,
        is_exchange_enabled     boolean,
        pub_block_list          int[],
        site_block_list         int[],
        site_url                text,
        modified_on             TIMESTAMP WITHOUT TIME ZONE
    );
    ALTER TYPE wap_site_uac_type_04092014 OWNER TO postgres;