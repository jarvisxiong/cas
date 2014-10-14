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
    public SiteFilterEntity queryUniqueResult(RepositoryQuery siteFilterQuery) throws RepositoryException {
        Collection<SiteFilterEntity> siteFilterEntityResultSet = query(siteFilterQuery);
        if (siteFilterEntityResultSet == null || siteFilterEntityResultSet.isEmpty()) {
            return null;
        }
        return (SiteFilterEntity) siteFilterEntityResultSet.toArray()[0];
    }

    @Override
    public DBEntity<SiteFilterEntity, SiteFilterQuery> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        SiteFilterEntity siteFilterEntity = new SiteFilterEntity();
        Timestamp modifyTime = row.getTimestamp("modified_on");
        siteFilterEntity.setRuleType(row.getInt("rule_type_id"));
        siteFilterEntity.setSiteId(row.getString("site_id"));
        siteFilterEntity.setPubId(row.getString("pub_id"));
        String[] tempArray = (String[]) row.getArray("filter_data");

        if (siteFilterEntity.getRuleType() == 4) {
            siteFilterEntity.setBlockedIabCategories(tempArray);
        } else if (siteFilterEntity.getRuleType() == 6) {
            siteFilterEntity.setBlockedAdvertisers(tempArray);
        } else {
            siteFilterEntity = new SiteFilterEntity();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Got SiteFilterEntity Entity" + siteFilterEntity.toString());
        }
        return new DBEntity<SiteFilterEntity, SiteFilterQuery>(siteFilterEntity, modifyTime);
    }

    @Override
    public HashIndexKeyBuilder<SiteFilterEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public boolean isObjectToBeDeleted(SiteFilterEntity siteFilterEntity) {
        return false;
    }
}