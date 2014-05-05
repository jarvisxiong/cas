CREATE OR REPLACE TYPE rtbd_creative_type AS
(
    advertiser_id                       CHARACTER VARYING(128),
    creative_id                         CHARACTER VARYING(128),
    status                              CHARACTER VARYING(128),
    modified_on                         TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE rtbd_creative_type OWNER TO postgres;
