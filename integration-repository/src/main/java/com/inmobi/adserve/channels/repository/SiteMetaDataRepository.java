package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class SiteMetaDataRepository extends AbstractStatsMaintainingDBRepository<SiteMetaDataEntity, String> implements
        RepositoryManager
{

    @Override
    public DBEntity<SiteMetaDataEntity, String> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException
    {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        String siteId = row.getString("site_id");
        String pubId = row.getString("pub_id");
        Timestamp modifiedOn = row.getTimestamp("modified_on");
        String[] siteAdvertisers = (String[]) row.getArray("site_advertiser_incl_list");
        String[] publisherAdvertisers = (String[]) row.getArray("pub_advertiser_incl_list");
        Set<String> advertisersIncludedBySite = new HashSet<String>();
        Set<String> advertisersIncludedByPublisher = new HashSet<String>();
        if (siteAdvertisers != null) {
            advertisersIncludedBySite.addAll(Arrays.asList(siteAdvertisers));
        }
        if (publisherAdvertisers != null) {
            advertisersIncludedByPublisher.addAll(Arrays.asList(publisherAdvertisers));
        }
        SiteMetaDataEntity.Builder builder = SiteMetaDataEntity.newBuilder();
        builder.setSiteId(siteId);
        builder.setPubId(pubId);
        builder.setModified_on(modifiedOn);
        builder.setAdvertisersIncludedBySite(advertisersIncludedBySite);
        builder.setAdvertisersIncludedByPublisher(advertisersIncludedByPublisher);
        SiteMetaDataEntity entity = builder.build();
        return new DBEntity<SiteMetaDataEntity, String>(entity, modifiedOn);
    }

    @Override
    public boolean isObjectToBeDeleted(SiteMetaDataEntity entity)
    {
        if (entity.getSiteId() == null) {
            return true;
        }
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SiteMetaDataEntity> getHashIndexKeyBuilder(String className)
    {
        return null;
    }

    @Override
    public SiteMetaDataEntity queryUniqueResult(RepositoryQuery q) throws RepositoryException
    {
        return null;
    }

}