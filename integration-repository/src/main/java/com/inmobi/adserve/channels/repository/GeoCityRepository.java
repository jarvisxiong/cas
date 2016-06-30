package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

import com.inmobi.adserve.channels.entity.GeoCityEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;

public class GeoCityRepository extends AbstractStatsMaintainingDBRepository <GeoCityEntity, Integer> implements RepositoryManager {
    @Override
    public DBEntity<GeoCityEntity, Integer> buildObjectFromRow(final ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Integer id = row.getInt("id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {
            final String name = row.getString("name");

            final GeoCityEntity.Builder builder = GeoCityEntity.newBuilder();
            builder.id(id);
            builder.name(name);
            builder.modifiedOn(modifiedOn);

            return new DBEntity<>(builder.build(), modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<>(new EntityError<>(id, "ERROR_IN_EXTRACTING_GEO_CITY"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(final GeoCityEntity entity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<GeoCityEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public GeoCityEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
