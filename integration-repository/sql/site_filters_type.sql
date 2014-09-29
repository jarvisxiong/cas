CREATE OR REPLACE TYPE site_filter_repo_type AS
(
    pub_id                   CHARACTER VARYING(128),
    site_id                  CHARACTER VARYING(128),
    rule_type_id             INT,
    filter_data              CHARACTER VARYING[],
    is_exact                 BOOLEAN,
    modified_on              TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE site_filter_repo_type OWNER TO postgres;

