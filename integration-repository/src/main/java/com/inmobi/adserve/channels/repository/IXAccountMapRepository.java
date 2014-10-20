package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;

public class IXAccountMapRepository extends AbstractStatsMaintainingDBRepository<IXAccountMapEntity, Long>
        implements
            RepositoryManager {

    @Override
    public DBEntity<IXAccountMapEntity, Long> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Long rpNetworkId = row.getLong("rp_network_id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {
            final String inmobiAccountId = row.getString("inmobi_account_id");
            final String networkName = row.getString("network_name");
            final String networkType = row.getString("network_type");

            final IXAccountMapEntity.Builder builder = IXAccountMapEntity.newBuilder();
            builder.setRpNetworkId(rpNetworkId);
            builder.setInmobiAccountId(inmobiAccountId);
            builder.setNetworkName(networkName);
            builder.setNetworkType(networkType);
            builder.setModifiedOn(modifiedOn);

            final IXAccountMapEntity entity = builder.build();
            return new DBEntity<IXAccountMapEntity, Long>(entity, modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<IXAccountMapEntity, Long>(new EntityError<Long>(rpNetworkId,
                    "ERROR_IN_EXTRACTING_IX_ACCOUNT_MAP"), modifiedOn);
        }

    }

    @Override
    public boolean isObjectToBeDeleted(final IXAccountMapEntity entity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<IXAccountMapEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public IXAccountMapEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
