CREATE OR REPLACE TYPE channel_segment_feedback_type AS
(
    advertiser_id            CHARACTER VARYING(128),
    adgroup_id		     CHARACTER VARYING(128),	
    ecpm                     DOUBLE,
    fill_ratio		     DOUBLE,	
);
ALTER TYPE channel_segment_feedback_type OWNER TO postgres;
