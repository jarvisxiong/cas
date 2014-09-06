package com.inmobi.adserve.channels.repository;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;
import org.apache.log4j.Logger;

public class WapSiteUACRepository extends AbstractStatsMaintainingDBRepository<WapSiteUACEntity, String> implements
        RepositoryManager {
    // private static final long IOS_SITE_TYPE = 21;
    private static final long ANDROID_SITE_TYPE = 22;
    private static final Map<String, String> CONTENT_RATING_MAP = new HashMap<>();

    static {
        CONTENT_RATING_MAP.put("High Maturity", "17+");
        CONTENT_RATING_MAP.put("Medium Maturity", "12+");
        CONTENT_RATING_MAP.put("Low Maturity", "9+");
        CONTENT_RATING_MAP.put("Everyone", "4+");
        CONTENT_RATING_MAP.put("Not rated", "Not yet rated");
    }

    @Override
    public DBEntity<WapSiteUACEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final String id = row.getString("id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {

            final long siteTypeId = row.getLong("site_type_id");
            final String pubId = row.getString("pub_id");

            final String contentRating = row.getString("content_rating");
            final String appType = row.getString("app_type");
            final String categories = row.getString("categories");

            final boolean transparencyEnabled = row.getBoolean("is_transparency_enabled");
            final boolean exchangeEnabled = row.getBoolean("is_exchange_enabled");

            final Integer pubBlockArr[] = (Integer[])row.getArray("pub_block_list");

            final Integer siteBlockArr[] = (Integer[])row.getArray("site_block_list");


            final boolean coppaEnabled = row.getBoolean("coppa_enabled");

            final String marketId = row.getString("market_id");
            final String siteUrl = row.getString("siteUrl");

            final WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
            builder.setId(id);
            builder.setModifiedOn(modifiedOn);
            builder.setSiteTypeId(siteTypeId);
            builder.setCoppaEnabled(coppaEnabled);
            builder.setExchangeEnabled(exchangeEnabled);
            builder.setTransparencyEnabled(transparencyEnabled);
            builder.setPubId(pubId);
            builder.setMarketId(marketId);
            builder.setSiteUrl(siteUrl);

            if(null != siteBlockArr)
            {
                builder.setBlockList(Arrays.asList(siteBlockArr));
            }
            else if(null != pubBlockArr)
            {
                builder.setBlockList(Arrays.asList(pubBlockArr));
            }
            else
            {
                builder.setBlockList(new ArrayList<Integer>());
            }

            if (siteTypeId == ANDROID_SITE_TYPE && contentRating != null && !contentRating.trim().isEmpty()) {
                builder.setContentRating(CONTENT_RATING_MAP.get(contentRating));
            } else {
                builder.setContentRating(contentRating);
            }
            //

            builder.setAppType(appType);
            final List<String> catList = new ArrayList<>();
            if (categories != null && !categories.isEmpty()) {
                for (String cat : categories.split(",")) {
                    if (cat != null) {
                        cat = cat.trim();
                        if (!cat.isEmpty() && !"ALL".equalsIgnoreCase(cat)) {
                            catList.add(cat);
                        }
                    }
                }
                builder.setCategories(catList);
            }
            final WapSiteUACEntity entity = builder.build();
            if (logger.isDebugEnabled()) {
                logger.debug("Found WapSiteUACEntity : " + entity);
            }
            return new DBEntity<WapSiteUACEntity, String>(entity, modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<WapSiteUACEntity, String>(new EntityError<String>(id, "ERROR_IN_EXTRACTING_WAP_SITE_UAC"),
                    modifiedOn);
        }

    }

    @Override
    public boolean isObjectToBeDeleted(final WapSiteUACEntity entity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<WapSiteUACEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public WapSiteUACEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
