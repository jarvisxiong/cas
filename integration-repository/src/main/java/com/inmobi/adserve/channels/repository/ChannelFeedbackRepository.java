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
        implements
            RepositoryManager {

    @Override
    public DBEntity<ChannelFeedbackEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        logger.debug("result set is not null");
        final String advertiserId = row.getString("id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        try {
            final double totalInflow = row.getDouble("total_inflow");
            final double totalBurn = row.getDouble("total_burn");
            final double balance = row.getDouble("balance");
            final double revenue = row.getDouble("revenue");
            final int averageLatency = row.getInt("average_latency");
            final long totalImpressions = row.getLong("total_impressions");
            final long todayImpressions = row.getLong("today_impressions");
            final long todayRequests = row.getInt("today_requests");

            final ChannelFeedbackEntity.Builder builder = ChannelFeedbackEntity.newBuilder();
            builder.setAdvertiserId(advertiserId);
            builder.setTotalInflow(totalInflow);
            builder.setTotalBurn(totalBurn);
            builder.setBalance(balance);
            builder.setAverageLatency(averageLatency);
            builder.setTotalImpressions(totalImpressions);
            builder.setTodayImpressions(todayImpressions);
            builder.setTodayRequests(todayRequests);
            builder.setRevenue(revenue);

            final ChannelFeedbackEntity entity = builder.build();
            return new DBEntity<ChannelFeedbackEntity, String>(entity, modifiedOn);
        } catch (final Exception e) {
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
