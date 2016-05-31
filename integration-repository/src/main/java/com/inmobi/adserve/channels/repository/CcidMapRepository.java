package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.CcidMapEntity;
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
 * Created by ishanbhatnagar on 30/4/15.
 */
public class CcidMapRepository extends AbstractStatsMaintainingDBRepository<CcidMapEntity, Integer>
        implements RepositoryManager {

    @Override
    public DBEntity<CcidMapEntity, Integer> buildObjectFromRow(ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Integer countryCarrierId = row.getInt("country_carrier_id");

        try {
            final String country = row.getString("country");
            final String carrier = row.getString("carrier");

            final CcidMapEntity.Builder builder = CcidMapEntity.newBuilder();
            builder.countryCarrierId(countryCarrierId);
            builder.country(country);
            builder.carrier(carrier);

            final CcidMapEntity entity = builder.build();
            return new DBEntity<>(entity, null);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<>(new EntityError<>(countryCarrierId, "ERROR_IN_EXTRACTING_CCID_MAP"), null);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(CcidMapEntity object) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<CcidMapEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public CcidMapEntity queryUniqueResult(RepositoryQuery q) throws RepositoryException {
        return null;
    }
}
