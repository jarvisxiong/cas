package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.types.CreativeExposure;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.Repository;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class CreativeRepository extends AbstractStatsMaintainingDBRepository<CreativeEntity, CreativeQuery>
    implements Repository, RepositoryManager {

  @Override
  public CreativeEntity queryUniqueResult(final RepositoryQuery creativeQuery) throws RepositoryException {
    final Collection<CreativeEntity> creativeEntityResultSet = query(creativeQuery);
    if (creativeEntityResultSet == null || creativeEntityResultSet.isEmpty()) {
        return null;
    }
    return (CreativeEntity) creativeEntityResultSet.toArray()[0];
  }

  @Override
  public DBEntity<CreativeEntity, CreativeQuery> buildObjectFromRow(final ResultSetRow resultSetRow)
      throws RepositoryException {
    final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
    final Timestamp modifyTime = row.getTimestamp("modified_on");
    final CreativeEntity.Builder builder = CreativeEntity.newBuilder();
    final String advertiserId = row.getString("advertiser_id");
    final String creativeId = row.getString("creative_id");
    final String exposureLevel = row.getString("exposure_level");
    final String imageUrl = row.getString("sample_url");
    builder.setAdvertiserId(advertiserId);
    builder.setCreativeId(creativeId);
    CreativeExposure exposure = CreativeExposure.SELF_SERVE;
    if (!StringUtils.isEmpty(exposureLevel)) {
      exposure =
          "SELF-SERVE".equals(exposureLevel) ? CreativeExposure.SELF_SERVE : CreativeExposure.valueOf(exposureLevel);
    }
    builder.setExposureLevel(exposure);
    builder.setImageUrl(imageUrl);
    final CreativeEntity entity = builder.build();

    if (logger.isDebugEnabled()) {
      logger.debug("Adding creative entity : " + entity);
    }
    return new DBEntity<CreativeEntity, CreativeQuery>(entity, modifyTime);
  }

  @Override
  public boolean isObjectToBeDeleted(final CreativeEntity object) {
    return false;
  }

  @Override
  public HashIndexKeyBuilder<CreativeEntity> getHashIndexKeyBuilder(final String className) {
    return null;
  }

}
