package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Collection;

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

/**
 * Created by yasir.imteyaz on 27/09/14.
 */
public class SiteFilterRepository extends AbstractStatsMaintainingDBRepository<SiteFilterEntity, SiteFilterQuery>
        implements
            Repository,
            RepositoryManager {

    @Override
    public DBEntity<SiteFilterEntity, SiteFilterQuery> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        SiteFilterEntity siteFilterEntity = new SiteFilterEntity();
        final Timestamp modifyTime = row.getTimestamp("modified_on");
        siteFilterEntity.setRuleType(row.getInt("rule_type_id"));
        siteFilterEntity.setSiteId(row.getString("site_id"));
        siteFilterEntity.setPubId(row.getString("pub_id"));
        siteFilterEntity.setModified_on(modifyTime);
        final String[] tempArray = (String[]) row.getArray("filter_data");

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
    public boolean isObjectToBeDeleted(final SiteFilterEntity siteFilterEntity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SiteFilterEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public SiteFilterEntity queryUniqueResult(final RepositoryQuery siteFilterQuery) throws RepositoryException {
        final Collection<SiteFilterEntity> siteFilterEntityResultSet = query(siteFilterQuery);
        if (siteFilterEntityResultSet == null || siteFilterEntityResultSet.isEmpty()) {
            return null;
        }
        return (SiteFilterEntity) siteFilterEntityResultSet.toArray()[0];
    }
}
