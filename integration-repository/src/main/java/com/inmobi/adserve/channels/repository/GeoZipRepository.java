package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

import com.inmobi.adserve.channels.entity.GeoZipEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;

/**
 * Created by anshul.soni on 10/11/14.
 */
public class GeoZipRepository extends AbstractStatsMaintainingDBRepository <GeoZipEntity, Integer> implements RepositoryManager {
    @Override
    public DBEntity<GeoZipEntity, Integer> buildObjectFromRow(final ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Integer zipId = row.getInt("id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {
            final String zipCode = row.getString("zipcode");

            final GeoZipEntity.Builder builder = GeoZipEntity.newBuilder();
            builder.setZipId(zipId);
            builder.setZipCode(zipCode);
            builder.setModifiedOn(modifiedOn);

            final GeoZipEntity entity = builder.build();
            return new DBEntity<GeoZipEntity, Integer>(entity, modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<GeoZipEntity, Integer>(new EntityError<Integer>(zipId,
                    "ERROR_IN_EXTRACTING_GEO_ZIP"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(final GeoZipEntity entity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<GeoZipEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public GeoZipEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
