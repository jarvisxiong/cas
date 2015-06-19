package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

import com.inmobi.adserve.channels.entity.SdkMraidMapEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;

public class SdkMraidMapRepository extends AbstractStatsMaintainingDBRepository<SdkMraidMapEntity, String>
        implements RepositoryManager {

    @Override
    public DBEntity<SdkMraidMapEntity, String> buildObjectFromRow(ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final String sdkName =  row.getString("name");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {
            final String mraidPath = row.getString("mraid_path");

            final SdkMraidMapEntity.Builder builder = SdkMraidMapEntity.newBuilder();
            builder.setSdkName(sdkName);
            builder.setMraidPath(mraidPath);
            builder.setModifiedOn(modifiedOn);

            final SdkMraidMapEntity entity = builder.build();
            return new DBEntity<SdkMraidMapEntity, String>(entity, modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<SdkMraidMapEntity, String>(new EntityError<String>(sdkName,
                    "ERROR_IN_EXTRACTING_SDK_MRAID_MAP"), modifiedOn);
        }    }

    @Override
    public boolean isObjectToBeDeleted(SdkMraidMapEntity object) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SdkMraidMapEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public SdkMraidMapEntity queryUniqueResult(RepositoryQuery q) throws RepositoryException {
        return null;
    }
}
