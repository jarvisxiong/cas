package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
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
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {
            final double ecpm = row.getDouble("ecpm");
            final double networkEcpm = row.getDouble("network_ecpm");


            final SiteEcpmEntity.Builder builder = SiteEcpmEntity.newBuilder();
            builder.siteId(siteId);
            builder.countryId(countryId);
            builder.osId(osId);
            builder.ecpm(ecpm);
            builder.networkEcpm(networkEcpm);
            builder.modifiedOn(modifiedOn);
            final SiteEcpmEntity entity = builder.build();
            return new DBEntity<SiteEcpmEntity, SiteEcpmQuery>(entity, modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<SiteEcpmEntity, SiteEcpmQuery>(new EntityError<SiteEcpmQuery>(new SiteEcpmQuery(siteId,
                    countryId, osId), "ERROR_IN_LOADING_SITE_ECPM"), modifiedOn);
        }
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
