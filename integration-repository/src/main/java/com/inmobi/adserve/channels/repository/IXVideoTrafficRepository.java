package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.IXVideoTrafficEntity;
import com.inmobi.adserve.channels.query.IXVideoTrafficQuery;
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

import org.apache.commons.lang.StringUtils;

public class IXVideoTrafficRepository extends AbstractStatsMaintainingDBRepository<IXVideoTrafficEntity, IXVideoTrafficQuery>
        implements Repository, RepositoryManager {

    public static final String ALL_SITES = StringUtils.EMPTY;
    public static final int ALL_COUNTRY = -1;
    public static final short DEFAULT_TRAFFIC_PERCENTAGE = 20;  // 20%

    @Override
    public DBEntity<IXVideoTrafficEntity, IXVideoTrafficQuery> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Timestamp modifyTime = row.getTimestamp("modified_on");

        IXVideoTrafficEntity.Builder builder = IXVideoTrafficEntity.newBuilder();

        builder.setSiteId(row.getString("site_id"));
        builder.setCountryId(row.getInt("country_id"));
        builder.setTrafficPercentage((short) row.getInt("traffic_percentage"));
        builder.setIsActive(row.getBoolean("is_active"));
        builder.setModifiedOn(modifyTime);

        IXVideoTrafficEntity iXVideoTrafficEntity = builder.build();

        if (logger.isDebugEnabled()) {
            logger.debug("Got IXVideoTrafficEntity Entity" + iXVideoTrafficEntity.toString());
        }
        return new DBEntity<>(iXVideoTrafficEntity, modifyTime);
    }

    @Override
    public boolean isObjectToBeDeleted(final IXVideoTrafficEntity entity) {
        return !entity.getIsActive();
    }

    @Override
    public HashIndexKeyBuilder<IXVideoTrafficEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public IXVideoTrafficEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }
}
