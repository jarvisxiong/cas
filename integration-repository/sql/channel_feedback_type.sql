CREATE OR REPLACE TYPE channel_feedback_type AS
(
    id                       CHARACTER VARYING(128),
    total_inflow             DOUBLE PRECISION,
    total_burn		     DOUBLE PRECISION,	
    balance                  DOUBLE PRECISION,
    average_latency          BIGINT,
    total_impressions        BIGINT,
    today_impressions        BIGINT,
    today_requests           BIGINT,
    revenue                  DOUBLE PRECISION,
    modified_on              TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE channel_feedback_type OWNER TO postgres;
