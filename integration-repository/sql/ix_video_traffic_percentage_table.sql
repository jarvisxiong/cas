--
-- Name: ix_video_traffic_percentage; Type: TABLE; Schema: public; Owner: postgres; Tablespace:
--
CREATE TABLE ix_video_traffic_percentage
(
  id serial PRIMARY KEY,
  site_id character varying(120) NOT NULL default '',
  country_id integer NOT NULL default -1,
  traffic_percentage smallint NOT NULL,
  is_active boolean DEFAULT true,
  created_on timestamp without time zone DEFAULT now(),
  created_by character varying(120) NOT NULL,
  modified_on timestamp without time zone DEFAULT now(),
  modified_by character varying(120) NOT NULL,
  CONSTRAINT country_or_site_mandatory CHECK (country_id IS NOT NULL OR site_id IS NOT NULL),
  CONSTRAINT country_site_unique UNIQUE (country_id, site_id),
  CONSTRAINT valid_percentage CHECK (traffic_percentage >= 0 AND traffic_percentage <= 100)
);

ALTER TABLE ix_video_traffic_percentage
  OWNER TO postgres;


COMMENT ON TABLE ix_video_traffic_percentage IS 'Stores the video traffic percentage allocation.';
COMMENT ON COLUMN ix_video_traffic_percentage.id IS 'Unique Id of a record';
COMMENT ON COLUMN ix_video_traffic_percentage.site_id IS 'Site Id';
COMMENT ON COLUMN ix_video_traffic_percentage.country_id IS 'Country Id';
COMMENT ON COLUMN ix_video_traffic_percentage.traffic_percentage IS 'Video Traffic percentage';
COMMENT ON COLUMN ix_video_traffic_percentage.is_active IS 'Whether the record is active';
COMMENT ON COLUMN ix_video_traffic_percentage.created_on IS 'Creation time';
COMMENT ON COLUMN ix_video_traffic_percentage.created_by IS 'Created by user';
COMMENT ON COLUMN ix_video_traffic_percentage.modified_on IS 'Last modified time';
COMMENT ON COLUMN ix_video_traffic_percentage.modified_by IS 'Last modified by user';

