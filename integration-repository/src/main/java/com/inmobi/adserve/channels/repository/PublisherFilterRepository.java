package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.PublisherFilterEntity;
import com.inmobi.adserve.channels.query.PublisherFilterQuery;
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


public class PublisherFilterRepository extends
        AbstractStatsMaintainingDBRepository<PublisherFilterEntity, PublisherFilterQuery> implements Repository,
        RepositoryManager {

    @Override
    public PublisherFilterEntity queryUniqueResult(RepositoryQuery publisherFilterQuery) throws RepositoryException {
        Collection<PublisherFilterEntity> publisherFilterEntityResultSet = query(publisherFilterQuery);
        if (publisherFilterEntityResultSet == null || publisherFilterEntityResultSet.size() == 0) {
            return null;
        } else if (publisherFilterEntityResultSet.size() >= 1) {
            return (PublisherFilterEntity) publisherFilterEntityResultSet.toArray()[0];
        }
        return null;
    }

    @Override
    public DBEntity<PublisherFilterEntity, PublisherFilterQuery> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        PublisherFilterEntity publisherFilterEntity = new PublisherFilterEntity();
        Timestamp modifyTime = row.getTimestamp("modified_on");
        publisherFilterEntity.setExpired(row.getBoolean("is_expired"));
        publisherFilterEntity.setRuleType(row.getInt("rule_type_id"));
        publisherFilterEntity.setSiteId(row.getString("site_id"));
        publisherFilterEntity.setPubId(row.getString("pub_id"));
        String[] tempArray = (String[]) row.getArray("filter_data");

        if (publisherFilterEntity.getRuleType() == 4) {
            int i = 0;
            Long[] blockedCategories = new Long[tempArray.length];
            for (String cat : tempArray) {
                blockedCategories[i] = Long.parseLong(cat);
                i++;
            }
            publisherFilterEntity.setBlockedCategories(blockedCategories);
        } else if (publisherFilterEntity.getRuleType() == 6) {
            publisherFilterEntity.setBlockedAdvertisers(tempArray);
        } else {
            publisherFilterEntity = new PublisherFilterEntity();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Got publisherFilterEntity Entity" + publisherFilterEntity.toString());
        }
        return new DBEntity<PublisherFilterEntity, PublisherFilterQuery>(publisherFilterEntity, modifyTime);
    }

    @Override
    public HashIndexKeyBuilder<PublisherFilterEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public boolean isObjectToBeDeleted(PublisherFilterEntity publisherFilterEntity) {
        return publisherFilterEntity.isExpired();
    }
}
