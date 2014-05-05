package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.types.CreativeStatus;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.Repository;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;

import java.sql.Timestamp;
import java.util.Collection;


public class CreativeRepository extends
        AbstractStatsMaintainingDBRepository<CreativeEntity, CreativeQuery> implements Repository,
        RepositoryManager {

    @Override
    public CreativeEntity queryUniqueResult(final RepositoryQuery creativeQuery) throws RepositoryException {
        Collection<CreativeEntity> creativeEntityResultSet = query(creativeQuery);
        if (creativeEntityResultSet == null || creativeEntityResultSet.size() == 0) {
            return null;
        }
        else if (creativeEntityResultSet.size() >= 1) {
            return (CreativeEntity) creativeEntityResultSet.toArray()[0];
        }
        return null;
    }

    @Override
    public DBEntity<CreativeEntity, CreativeQuery> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        Timestamp modifyTime = row.getTimestamp("modified_on");
        CreativeEntity.Builder builder = CreativeEntity.newBuilder();
        String advertiserId = row.getString("advertiser_id");
        String creativeId = row.getString("creative_id");
        String status = row.getString("status");
        builder.setAdvertiserId(advertiserId);
        builder.setCreativeId(creativeId);
        builder.setCreativeStatus(CreativeStatus.valueOf(status));
        CreativeEntity entity = builder.build();

        if (logger.isDebugEnabled()) {
            logger.debug("Adding creative entity : " + entity);
        }
        return new DBEntity<CreativeEntity, CreativeQuery>(entity, modifyTime);
    }

    @Override
    public boolean isObjectToBeDeleted(CreativeEntity object) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<CreativeEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

}
