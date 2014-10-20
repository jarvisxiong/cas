package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

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


public class SiteEcpmRepository extends AbstractStatsMaintainingDBRepository<SiteEcpmEntity, SiteEcpmQuery>
        implements
            Repository,
            RepositoryManager {

    @Override
    public DBEntity<SiteEcpmEntity, SiteEcpmQuery> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final String siteId = row.getString("site_id");
        final Integer countryId = row.getInt("country_id");
        final Integer osId = row.getInt("os_id");
        final double ecpm = row.getDouble("ecpm");
        final double networkEcpm = row.getDouble("network_ecpm");
        final Timestamp modified_on = row.getTimestamp("modified_on");

        final SiteEcpmEntity.Builder builder = SiteEcpmEntity.newBuilder();
        builder.setSiteId(siteId);
        builder.setCountryId(countryId);
        builder.setOsId(osId);
        builder.setEcpm(ecpm);
        builder.setNetworkEcpm(networkEcpm);
        builder.setModifiedOn(modified_on);
        final SiteEcpmEntity entity = builder.build();
        return new DBEntity<SiteEcpmEntity, SiteEcpmQuery>(entity, modified_on);
    }

    @Override
    public boolean isObjectToBeDeleted(final SiteEcpmEntity siteEcpmEntity) {
        if (DateUtils.addDays(siteEcpmEntity.getModifiedOn(), 3).before(new Date())) {
            return true;
        }
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SiteEcpmEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public SiteEcpmEntity queryUniqueResult(final RepositoryQuery repositoryQuery) throws RepositoryException {
        return null;
    }
}
