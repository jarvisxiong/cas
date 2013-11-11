CREATE OR REPLACE TYPE channel_segment_feedback_type_17042013 AS
(
    advertiser_id            CHARACTER VARYING(128),
    ad_group_id		     CHARACTER VARYING(128),	
    ecpm                     DOUBLE PRECISION,
    fill_ratio		     DOUBLE PRECISION,
    today_impressions        BIGINT
);
ALTER TYPE channel_segment_feedback_type_17042013 OWNER TO postgres;
