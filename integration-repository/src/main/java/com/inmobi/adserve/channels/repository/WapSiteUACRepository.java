package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableSet;
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

public class WapSiteUACRepository extends AbstractStatsMaintainingDBRepository<WapSiteUACEntity, String>
        implements
            RepositoryManager {
    private static final Map<String, String> CONTENT_RATING_MAP = new HashMap<>();

    private static final String CONFIG_OVERRIDE_KEY_FOR_SITE = "override.site";
    private static final String CONFIG_OVERRIDE_SPLIT_CHAR = "\\.";
    private static final String MARKET_ID = "marketId";
    private static final String BUNDLE_ID = "bundleId";
    private static final String SITE_URL = "siteUrl";
    private static final Set<String> ALLOWED_FIELDS_FOR_OVERRIDE = ImmutableSet.of(MARKET_ID, BUNDLE_ID, SITE_URL);

    // site -> (field, overridden value) mapping
    protected final Map<String, Map<String, String>> overrides = new HashMap<>();

    // not static so as to be able to use the existing repository logger
    @SuppressWarnings("unchecked")
    public void initOverrides(Configuration config, final org.apache.log4j.Logger logger) {
        try {
            config = config.subset(CONFIG_OVERRIDE_KEY_FOR_SITE);
            final Iterator<String> configIterator = config.getKeys();
            while (configIterator.hasNext()) {
                final String key = configIterator.next();
                final String[] keyMeta = key.split(CONFIG_OVERRIDE_SPLIT_CHAR);
                if (keyMeta.length != 2) {
                    logger.error("Faulty override key structure. Faulty config line: " + key);
                    continue;
                } else {
                    final String siteId = keyMeta[0];
                    final String field = keyMeta[1];

                    if (!ALLOWED_FIELDS_FOR_OVERRIDE.contains(field)) {
                        logger.error("Unsupported override field. Faulty config line: " + key);
                        continue;
                    } else {
                        final String value = config.getString(key);
                        if (overrides.containsKey(siteId)) {
                            overrides.get(siteId).put(field, value);
                        } else {
                            final Map<String, String> siteLevelOverridesMap = new HashMap<>();
                            siteLevelOverridesMap.put(field, value);
                            overrides.put(siteId, siteLevelOverridesMap);
                        }
                        logger.error("Added override for site" + siteId + ": Setting " + field + " to " + value);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Error encountered while handling overrides for Wap Site UAC Repo. Proceeding as usual. "
                    + "Exception: {}", e);
        }
    }

    static {
        CONTENT_RATING_MAP.put("High Maturity", "17+");
        CONTENT_RATING_MAP.put("Medium Maturity", "12+");
        CONTENT_RATING_MAP.put("Low Maturity", "9+");
        CONTENT_RATING_MAP.put("Everyone", "4+");
        CONTENT_RATING_MAP.put("Not rated", "Not yet rated");
    }

    @Override
    public DBEntity<WapSiteUACEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final String id = row.getString("id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        try {
            final long siteTypeId = row.getLong("site_type_id");
            final String contentRating = row.getString("content_rating");
            final String appType = row.getString("app_type");
            final String categories = row.getString("categories");
            final boolean coppaEnabled = row.getBoolean("coppa_enabled");
            final Integer exchange_settings = row.getInt("exchange_settings");
            final Integer pubBlindArr[] = (Integer[]) row.getArray("pub_blind_list");
            final Integer siteBlindArr[] = (Integer[]) row.getArray("site_blind_list");
            final boolean siteTransparencyEnabled = row.getBoolean("is_site_transparent");
            final String siteName = row.getString("site_name");
            final String appTitle = row.getString("title");
            String marketId = row.getString("market_id");
            String siteUrl = row.getString("site_url");
            String bundleId = row.getString("bundle_id");

            boolean overrideMarketId = false;
            // Setting overrides
            if (overrides.containsKey(id)) {
                final Map<String, String> overridesMap = overrides.get(id);
                for (final Map.Entry<String, String> entry : overridesMap.entrySet()) {
                    switch (entry.getKey()) {
                        case SITE_URL:
                            siteUrl = entry.getValue();
                            break;
                        case MARKET_ID:
                            marketId = entry.getValue();
                            overrideMarketId = true;
                            break;
                        case BUNDLE_ID:
                            bundleId = entry.getValue();
                            break;
                        default:
                            logger.error("Mapping not defined for field " + entry.getKey());
                    }
                }
            }

            final WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
            if (siteTypeId == WapSiteUACEntity.ANDROID_SITE_TYPE && contentRating != null
                    && !contentRating.trim().isEmpty()) {
                builder.contentRating(CONTENT_RATING_MAP.get(contentRating));
            } else {
                builder.contentRating(contentRating);
            }

            final List<String> catList = new ArrayList<>();
            if (StringUtils.isNotEmpty(categories)) {
                for (String cat : categories.split(",")) {
                    if (cat != null) {
                        cat = cat.trim();
                        if (!cat.isEmpty() && !"ALL".equalsIgnoreCase(cat)) {
                            catList.add(cat);
                        }
                    }
                }
                builder.categories(catList);
            }

            // 1 = EXCHANGE_ENABLED WITH SITE TRANSPARENCY ON
            // 2 = EXCHANGE_ENABLED WITH SITE TRANSPARENCY OFF
            // 3 = EXCHANGE_DISABLED
            final boolean pubTransparencyEnabled = 1 == exchange_settings;
            // Both Publisher level and site level transparency have to be enabled for an ad request to be transparent
            builder.isTransparencyEnabled(pubTransparencyEnabled && siteTransparencyEnabled);

            // If Site Id is set, we take site level blindlist, otherwise publisher level blind list
            if (null != siteBlindArr && siteBlindArr.length > 0) {
                builder.blindList(Arrays.asList(siteBlindArr));
            } else if (null != pubBlindArr && pubBlindArr.length > 0) {
                builder.blindList(Arrays.asList(pubBlindArr));
            }

            builder.id(id);
            builder.marketId(marketId);
            builder.siteTypeId(siteTypeId);
            builder.isCoppaEnabled(coppaEnabled);
            builder.appType(appType);
            builder.siteUrl(siteUrl);
            builder.siteName(siteName);
            builder.appTitle(appTitle);
            builder.bundleId(bundleId);
            builder.overrideMarketId(overrideMarketId);
            builder.modifiedOn(modifiedOn);

            final WapSiteUACEntity entity = builder.build();
            if (logger.isDebugEnabled()) {
                logger.debug("Found WapSiteUACEntity : " + entity);
            }
            return new DBEntity<>(entity, modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<>(new EntityError<>(id, "ERROR_IN_EXTRACTING_WAP_SITE_UAC"), modifiedOn);
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
