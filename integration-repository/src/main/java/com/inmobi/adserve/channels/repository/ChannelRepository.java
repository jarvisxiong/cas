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


public class ChannelRepository extends AbstractStatsMaintainingDBRepository<ChannelEntity, String> implements
        RepositoryManager {

    @Override
    public DBEntity<ChannelEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow) {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        Timestamp modifiedOn = row.getTimestamp("modified_on");
        String id = row.getString("id");
        try {
            String name = row.getString("name");
            String accountId = row.getString("account_id");
            String reportingApiKey = row.getString("reporting_api_key");
            String reportingApiUrl = row.getString("reporting_api_url");
            String username = row.getString("username");
            String password = row.getString("password");
            boolean isTestMode = row.getBoolean("is_test_mode");
            boolean isActive = row.getBoolean("is_active");
            long burstQps = row.getLong("burst_qps");
            long impressionCeil = row.getLong("impression_ceil");
            long impressionFloor = row.getLong("impression_floor");
            long requestCap = row.getLong("request_cap");
            if (requestCap == 0) {
                requestCap = Long.MAX_VALUE;
            }
            int priority = row.getInt("priority");
            int demandSourceTypeId = row.getInt("demand_source_type_id");
            String sIEJson = row.getString("sie_json");
            Set<String> sitesIE = getSites(sIEJson);
            boolean isSiteIncl = getMode(sIEJson);
            int accountSegment = row.getInt("account_segment");

            ChannelEntity.Builder builder = ChannelEntity.newBuilder();
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

            ChannelEntity entity = builder.build();
            return new DBEntity<ChannelEntity, String>(entity, modifiedOn);
        } catch (Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<ChannelEntity, String>(new EntityError<String>(id, "ERROR_IN_EXTRACTING_CHANNEL"),
                    modifiedOn);
        }
    }

    private boolean getMode(final String sIEJson) {
        boolean mode = false;
        if (sIEJson != null) {
            try {
                JSONObject jObject = new JSONObject(sIEJson);
                mode = "inclusion".equals(jObject.getString("mode"));
            } catch (JSONException e) {
                logger.info("wrong json in site_json in channel repo", e);
            }
        }
        return mode;
    }

    private Set<String> getSites(final String sIEJson) {
        Set<String> sitesIE = new HashSet<String>();
        if (sIEJson != null) {
            try {
                JSONObject jObject = new JSONObject(sIEJson);
                JSONArray sites = jObject.getJSONArray("sites");
                for (int i = 0; i < sites.length(); i++) {
                    sitesIE.add(sites.getString(i));
                }
            } catch (JSONException e) {
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
