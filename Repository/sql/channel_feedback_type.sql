CREATE OR REPLACE TYPE channel_feedback_type AS
(
    id                       CHARACTER VARYING(128),
    total_inflow             DOUBLE,
    total_burn		     DOUBLE,	
    balance                  DOUBLE,
    average_latency          BIGINT,
    total_impressions        BIGINT,
    todays_impressions       BIGINT,
    revenue                  DOUBLE,
    modified_on              TIMESTAMP WITHOUT TIME ZONE,
);
ALTER TYPE channel_feedback_type OWNER TO postgres;
