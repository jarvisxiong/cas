package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import com.inmobi.adserve.channels.entity.GeoRegionFenceMapEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class GeoRegionFenceMapRepository extends AbstractStatsMaintainingDBRepository<GeoRegionFenceMapEntity, String> implements RepositoryManager {
    @Override
    public DBEntity<GeoRegionFenceMapEntity, String> buildObjectFromRow(ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);

        // TODO: migrate to geo region id
        final String geoRegionName = row.getString("geo_region_name");
        final Long countryId = row.getLong("country_id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        try {
            List<Long> fenceIdsList = null;
            if (null != row.getArray("fence_ids_list")) {
                fenceIdsList = Arrays.asList((Long[])row.getArray("fence_ids_list"));
            }

            final GeoRegionFenceMapEntity.Builder builder = GeoRegionFenceMapEntity.newBuilder();
            builder.geoRegionName(geoRegionName);
            builder.countryId(countryId);
            builder.fenceIdsList(fenceIdsList);
            builder.modifiedOn(modifiedOn);

            final GeoRegionFenceMapEntity entity = builder.build();
            return new DBEntity<>(entity, modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<>(new EntityError<>(geoRegionName, "ERROR_IN_EXTRACTING_GEO_REGION_FENCE_MAP"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(GeoRegionFenceMapEntity object) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<GeoRegionFenceMapEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public GeoRegionFenceMapEntity queryUniqueResult(RepositoryQuery q) throws RepositoryException {
        return null;
    }
}
