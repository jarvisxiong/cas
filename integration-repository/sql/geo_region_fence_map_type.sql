CREATE OR REPLACE TYPE geo_region_fence_map_type AS
(
    geo_region_name          CHARACTER VARYING(255),
    country_id               BIGINT,
    fence_ids_list           BIGINT[],
    modified_on              TIMESTAMP WITHOUT TIME ZONE
);
ALTER TYPE geo_region_fence_map_type OWNER TO postgres;