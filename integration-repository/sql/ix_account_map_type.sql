    CREATE OR REPLACE TYPE ix_account_map_type AS
    (
        rp_network_id            bigint,
        inmobi_account_id        character varying(255),
        network_name             character varying(255),
        network_type             character varying(255),
        modified_on              TIMESTAMP WITHOUT TIME ZONE
    );
    ALTER TYPE ix_account_map_type OWNER TO postgres;