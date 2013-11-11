package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;

import java.sql.Timestamp;


public class SiteTaxonomyRepository extends AbstractStatsMaintainingDBRepository<SiteTaxonomyEntity, String> implements
        RepositoryManager {

    @Override
    public DBEntity<SiteTaxonomyEntity, String> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        String id = String.valueOf(row.getInt("id"));
        String name = row.getString("name");
        String parentId = String.valueOf(row.getInt("parent_id"));
        Timestamp modifyTime = row.getTimestamp("modified_on");
        SiteTaxonomyEntity entity = new SiteTaxonomyEntity(id, name, parentId);

        logger.debug("Id for the loaded siteTaxonomyEntity is " + id);
        return new DBEntity<SiteTaxonomyEntity, String>(entity, modifyTime);
    }

    @Override
    public boolean isObjectToBeDeleted(SiteTaxonomyEntity object) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SiteTaxonomyEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public SiteTaxonomyEntity queryUniqueResult(RepositoryQuery q) throws RepositoryException {
        return null;
    }
}
