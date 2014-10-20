package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class SiteTaxonomyRepository extends AbstractStatsMaintainingDBRepository<SiteTaxonomyEntity, String>
        implements
            RepositoryManager {

    @Override
    public DBEntity<SiteTaxonomyEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final String id = String.valueOf(row.getInt("id"));
        final String name = row.getString("name");
        final String parentId = String.valueOf(row.getInt("parent_id"));
        final Timestamp modifyTime = row.getTimestamp("modified_on");
        final SiteTaxonomyEntity entity = new SiteTaxonomyEntity(id, name, parentId);

        logger.debug("Id for the loaded siteTaxonomyEntity is " + id);
        return new DBEntity<SiteTaxonomyEntity, String>(entity, modifyTime);
    }

    @Override
    public boolean isObjectToBeDeleted(final SiteTaxonomyEntity object) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SiteTaxonomyEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public SiteTaxonomyEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }
}
