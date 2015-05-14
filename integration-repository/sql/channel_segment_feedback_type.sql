CREATE OR REPLACE TYPE channel_segment_feedback_type_11052015 AS
(
    advertiser_id			CHARACTER VARYING(128),
    ad_group_id				CHARACTER VARYING(128),	
    ecpm					DOUBLE PRECISION,
    fill_ratio				DOUBLE PRECISION,
    today_impressions		BIGINT,
    modified_on				TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE channel_segment_feedback_type_11052015 OWNER TO postgres;
