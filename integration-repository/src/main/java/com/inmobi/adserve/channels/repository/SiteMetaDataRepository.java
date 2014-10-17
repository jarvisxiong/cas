package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class SiteMetaDataRepository extends AbstractStatsMaintainingDBRepository<SiteMetaDataEntity, String>
    implements
      RepositoryManager {

  @Override
  public DBEntity<SiteMetaDataEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow)
      throws RepositoryException {
    final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
    final String siteId = row.getString("site_id");
    final String pubId = row.getString("pub_id");
    final Boolean selfServeAllowed = row.getBoolean("allow_self_serve");
    final Timestamp modifiedOn = row.getTimestamp("modified_on");
    final String[] siteAdvertisers = (String[]) row.getArray("site_advertiser_incl_list");
    final String[] publisherAdvertisers = (String[]) row.getArray("pub_advertiser_incl_list");

    final Set<String> advertisersIncludedBySite = new HashSet<String>();
    final Set<String> advertisersIncludedByPublisher = new HashSet<String>();
    if (siteAdvertisers != null) {
      advertisersIncludedBySite.addAll(Arrays.asList(siteAdvertisers));
    }
    if (publisherAdvertisers != null) {
      advertisersIncludedByPublisher.addAll(Arrays.asList(publisherAdvertisers));
    }

    final SiteMetaDataEntity.Builder builder = SiteMetaDataEntity.newBuilder();
    builder.setSiteId(siteId);
    builder.setPubId(pubId);
    builder.setSelfServeAllowed(selfServeAllowed);
    builder.setModified_on(modifiedOn);
    builder.setAdvertisersIncludedBySite(advertisersIncludedBySite);
    builder.setAdvertisersIncludedByPublisher(advertisersIncludedByPublisher);
    final SiteMetaDataEntity entity = builder.build();
    return new DBEntity<SiteMetaDataEntity, String>(entity, modifiedOn);
  }

  @Override
  public boolean isObjectToBeDeleted(final SiteMetaDataEntity entity) {
    if (entity.getSiteId() == null) {
      return true;
    }
    return false;
  }

  @Override
  public HashIndexKeyBuilder<SiteMetaDataEntity> getHashIndexKeyBuilder(final String className) {
    return null;
  }

  @Override
  public SiteMetaDataEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
    return null;
  }

}
