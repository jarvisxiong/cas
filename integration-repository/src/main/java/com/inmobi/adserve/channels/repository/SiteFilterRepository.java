package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.SiteFilterEntity;
import com.inmobi.adserve.channels.query.SiteFilterQuery;
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

/**
 * Created by yasir.imteyaz on 27/09/14.
 */
public class SiteFilterRepository extends
        AbstractStatsMaintainingDBRepository<SiteFilterEntity, SiteFilterQuery> implements Repository,
        RepositoryManager {

    @Override
    public SiteFilterEntity queryUniqueResult(RepositoryQuery SiteFilterQuery) throws RepositoryException {
        Collection<SiteFilterEntity> SiteFilterEntityResultSet = query(SiteFilterQuery);
        if (SiteFilterEntityResultSet == null || SiteFilterEntityResultSet.isEmpty()) {
            return null;
        }
        return (SiteFilterEntity) SiteFilterEntityResultSet.toArray()[0];
    }

    @Override
    public DBEntity<SiteFilterEntity, SiteFilterQuery> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        SiteFilterEntity SiteFilterEntity = new SiteFilterEntity();
        Timestamp modifyTime = row.getTimestamp("modified_on");
        SiteFilterEntity.setRuleType(row.getInt("rule_type_id"));
        SiteFilterEntity.setSiteId(row.getString("site_id"));
        SiteFilterEntity.setPubId(row.getString("pub_id"));
        String[] tempArray = (String[]) row.getArray("filter_data");

        if (SiteFilterEntity.getRuleType() == 4) {
            SiteFilterEntity.setBlockedIabCategories(tempArray);
        } else if (SiteFilterEntity.getRuleType() == 6) {
            SiteFilterEntity.setBlockedAdvertisers(tempArray);
        } else {
            SiteFilterEntity = new SiteFilterEntity();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Got SiteFilterEntity Entity" + SiteFilterEntity.toString());
        }
        return new DBEntity<SiteFilterEntity, SiteFilterQuery>(SiteFilterEntity, modifyTime);
    }

    @Override
    public HashIndexKeyBuilder<SiteFilterEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public boolean isObjectToBeDeleted(SiteFilterEntity SiteFilterEntity) {
        return false;
    }
}