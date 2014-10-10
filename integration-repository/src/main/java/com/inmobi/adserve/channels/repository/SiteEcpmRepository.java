package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.Repository;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;
import org.apache.commons.lang.time.DateUtils;

import java.sql.Timestamp;
import java.util.Date;


public class SiteEcpmRepository extends AbstractStatsMaintainingDBRepository<SiteEcpmEntity, SiteEcpmQuery> implements
        Repository, RepositoryManager {

    @Override
    public DBEntity<SiteEcpmEntity, SiteEcpmQuery> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        String siteId = row.getString("site_id");
        Integer countryId = row.getInt("country_id");
        Integer osId = row.getInt("os_id");
        double ecpm = row.getDouble("ecpm");
        double networkEcpm = row.getDouble("network_ecpm");
        Timestamp modified_on = row.getTimestamp("modified_on");

        SiteEcpmEntity.Builder builder = SiteEcpmEntity.newBuilder();
        builder.setSiteId(siteId);
        builder.setCountryId(countryId);
        builder.setOsId(osId);
        builder.setEcpm(ecpm);
        builder.setNetworkEcpm(networkEcpm);
        builder.setModifiedOn(modified_on);
        SiteEcpmEntity entity = builder.build();
        return new DBEntity<SiteEcpmEntity, SiteEcpmQuery>(entity, modified_on);
    }

    @Override
    public boolean isObjectToBeDeleted(SiteEcpmEntity siteEcpmEntity) {
        if (DateUtils.addDays(siteEcpmEntity.getModifiedOn(), 3).before(new Date())) {
            return true;
        }
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SiteEcpmEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public SiteEcpmEntity queryUniqueResult(RepositoryQuery repositoryQuery) throws RepositoryException {
        return null;
    }
}
