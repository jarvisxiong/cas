package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

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

public class WapSiteUACRepository extends AbstractStatsMaintainingDBRepository<WapSiteUACEntity, String> implements
		RepositoryManager {

	@Override
	public DBEntity<WapSiteUACEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow) throws RepositoryException {
		NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
		final String id = row.getString("id");
		final Timestamp modifiedOn = row.getTimestamp("uac_mod_on");

		try {
			final long siteTypeId = row.getLong("site_type_id");
			final String contentRating = row.getString("content_rating");
			final String appType = row.getString("app_type");
			WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
			builder.setId(id);
			builder.setUacModifiedOn(modifiedOn);
			builder.setSiteTypeId(siteTypeId);
			// TODO: Set mapped values
			builder.setContentRating(contentRating);
			builder.setAppType(appType);
			WapSiteUACEntity entity = builder.build();
			if (logger.isDebugEnabled()) {
				logger.debug("Found WapSiteUACEntity : " + entity);
			}
			return new DBEntity<WapSiteUACEntity, String>(entity, modifiedOn);
		} catch (Exception exp) {
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
