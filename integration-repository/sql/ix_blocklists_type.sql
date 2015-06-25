CREATE OR REPLACE TYPE ix_blocklists_type AS
(
      blocklist_name    CHARACTER VARYING(64),
      key_id            CHARACTER VARYING(100),
      key_type          INTEGER,
      filter_data_type  INTEGER,
      filter_data_size  INTEGER,
      modified_on       TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE ix_blocklists_type OWNER TO postgres;