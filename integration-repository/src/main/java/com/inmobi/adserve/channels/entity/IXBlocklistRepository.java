package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import com.inmobi.adserve.channels.query.IXBlocklistsQuery;
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
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
 * Created by ishanbhatnagar on 1/6/15.
 */
public class IXBlocklistRepository extends AbstractStatsMaintainingDBRepository<IXBlocklistEntity, IXBlocklistsQuery>
        implements RepositoryManager {

    @Override
    public DBEntity<IXBlocklistEntity, IXBlocklistsQuery> buildObjectFromRow(
            ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        final String keyId = row.getString("key_id");
        final Integer keyType = row.getInt("key_type");
        final Integer blocklistType = row.getInt("filter_data_type");

        try {
            final String blocklistName = row.getString("blocklist_name");
            final int blocklistSize = row.getInt("filter_data_size");
            final IXBlocklistKeyType ixBlocklistKeyType = IXBlocklistKeyType.getByValue(keyType);
            final IXBlocklistType ixBlocklistType = IXBlocklistType.getByValue(blocklistType);

            final IXBlocklistEntity.Builder builder = IXBlocklistEntity.newBuilder();
            builder.blocklistName(blocklistName);
            builder.keyId(keyId);
            builder.keytype(ixBlocklistKeyType);
            builder.blocklistType(ixBlocklistType);
            builder.blocklistSize(blocklistSize);
            builder.modifiedOn(modifiedOn);
            return new DBEntity<>(builder.build(), modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<>(new EntityError<>(new IXBlocklistsQuery(keyId, IXBlocklistKeyType.UNKNOWN,
                    IXBlocklistType.UNKNOWN), "ERROR_IN_EXTRACTING_IX_BLOCKLIST"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(IXBlocklistEntity object) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<IXBlocklistEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public IXBlocklistEntity queryUniqueResult(RepositoryQuery q) throws RepositoryException {
        return null;
    }
}
