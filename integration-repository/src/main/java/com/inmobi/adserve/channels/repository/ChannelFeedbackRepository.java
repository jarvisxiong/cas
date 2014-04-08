package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class ChannelFeedbackRepository extends AbstractStatsMaintainingDBRepository<ChannelFeedbackEntity, String>
        implements RepositoryManager {

    @Override
    public DBEntity<ChannelFeedbackEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        logger.debug("result set is not null");
        String advertiserId = row.getString("id");
        Timestamp modifiedOn = row.getTimestamp("modified_on");
        try {
            double totalInflow = row.getDouble("total_inflow");
            double totalBurn = row.getDouble("total_burn");
            double balance = row.getDouble("balance");
            long averageLatency = row.getLong("average_latency");
            double revenue = row.getDouble("revenue");
            int totalImpressions = row.getInt("total_impressions");
            int todayImpressions = row.getInt("today_impressions");
            int todayRequests = row.getInt("today_requests");

            ChannelFeedbackEntity.Builder builder = ChannelFeedbackEntity.newBuilder();
            builder.setAdvertiserId(advertiserId);
            builder.setTotalInflow(totalInflow);
            builder.setTotalBurn(totalBurn);
            builder.setBalance(balance);
            builder.setAverageLatency(averageLatency);
            builder.setTotalImpressions(totalImpressions);
            builder.setTodayImpressions(todayImpressions);
            builder.setTodayRequests(todayRequests);
            builder.setRevenue(revenue);

            ChannelFeedbackEntity entity = builder.build();
            return new DBEntity<ChannelFeedbackEntity, String>(entity, modifiedOn);
        }
        catch (Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<ChannelFeedbackEntity, String>(new EntityError<String>(advertiserId,
                    "ERROR_IN_EXTRACTING_CHANNEL"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(final ChannelFeedbackEntity entity) {
        if (entity.getAdvertiserId() == null) {
            return true;
        }
        return false;
    }

    @Override
    public HashIndexKeyBuilder<ChannelFeedbackEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public ChannelFeedbackEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
