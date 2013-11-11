CREATE OR REPLACE TYPE site_ecpm_type AS
(
    site_id                         CHARACTER VARYING(128),
    country_id                      INTEGER,
    os_id                           INTEGER,
    ecpm                            NUMERIC,
    network_ecpm                    NUMERIC,
    modified_on                     TIMESTAMP WITHOUT TIME ZONE
);