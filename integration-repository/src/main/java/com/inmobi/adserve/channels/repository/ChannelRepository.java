package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class ChannelRepository extends AbstractStatsMaintainingDBRepository<ChannelEntity, String>
		implements
			RepositoryManager {

	@Override
	public DBEntity<ChannelEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow) {
		final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
		final Timestamp modifiedOn = row.getTimestamp("modified_on");
		final String id = row.getString("id");
		try {
			final String name = row.getString("name");
			final String accountId = row.getString("account_id");
			final String reportingApiKey = row.getString("reporting_api_key");
			final String reportingApiUrl = row.getString("reporting_api_url");
			final String username = row.getString("username");
			final String password = row.getString("password");
			final boolean isTestMode = row.getBoolean("is_test_mode");
			final boolean isActive = row.getBoolean("is_active");
			final long burstQps = row.getLong("burst_qps");
			final long impressionCeil = row.getLong("impression_ceil");
			final long impressionFloor = row.getLong("impression_floor");
			long requestCap = row.getLong("request_cap");
			if (requestCap == 0) {
				requestCap = Long.MAX_VALUE;
			}
			final int priority = row.getInt("priority");
			final int demandSourceTypeId = row.getInt("demand_source_type_id");
			final String sIEJson = row.getString("sie_json");
			final Set<String> sitesIE = getSites(sIEJson);
			final boolean isSiteIncl = getMode(sIEJson);
			final int accountSegment = row.getInt("account_segment");

			final ChannelEntity.Builder builder = ChannelEntity.newBuilder();
			builder.setChannelId(id);
			builder.setName(name);
			builder.setAccountId(accountId);
			builder.setReportingApiKey(reportingApiKey);
			builder.setReportingApiUrl(reportingApiUrl);
			builder.setUsername(username);
			builder.setPassword(password);
			builder.setTestMode(isTestMode);
			builder.setActive(isActive);
			builder.setBurstQps(burstQps);
			builder.setImpressionCeil(impressionCeil);
			builder.setImpressionFloor(impressionFloor);
			builder.setModifiedOn(modifiedOn);
			builder.setPriority(priority);
			builder.setDemandSourceTypeId(demandSourceTypeId);
			builder.setRequestCap(requestCap);
			builder.setSitesIE(sitesIE);
			builder.setSiteInclusion(isSiteIncl);
			builder.setAccountSegment(accountSegment);

			final ChannelEntity entity = builder.build();
			return new DBEntity<ChannelEntity, String>(entity, modifiedOn);
		} catch (final Exception e) {
			logger.error("Error in resultset row", e);
			return new DBEntity<ChannelEntity, String>(new EntityError<String>(id, "ERROR_IN_EXTRACTING_CHANNEL"),
					modifiedOn);
		}
	}

	protected boolean getMode(final String sIEJson) {
		boolean mode = false;
		if (sIEJson != null) {
			try {
				final JSONObject jObject = new JSONObject(sIEJson);
				mode = "inclusion".equals(jObject.getString("mode"));
			} catch (final JSONException e) {
				logger.info("wrong json in site_json in channel repo", e);
			}
		}
		return mode;
	}

	protected Set<String> getSites(final String sIEJson) {
		final Set<String> sitesIE = new HashSet<String>();
		if (sIEJson != null) {
			try {
				final JSONObject jObject = new JSONObject(sIEJson);
				final JSONArray sites = jObject.getJSONArray("sites");
				for (int i = 0; i < sites.length(); i++) {
					sitesIE.add(sites.getString(i));
				}
			} catch (final JSONException e) {
				logger.info("wrong json in site_json in channel repo", e);
			}
		}
		return sitesIE;
	}

	@Override
	public boolean isObjectToBeDeleted(final ChannelEntity entity) {
		if (entity.getId() == null) {
			return true;
		}
		return false;
	}

	@Override
	public HashIndexKeyBuilder<ChannelEntity> getHashIndexKeyBuilder(final String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

}
