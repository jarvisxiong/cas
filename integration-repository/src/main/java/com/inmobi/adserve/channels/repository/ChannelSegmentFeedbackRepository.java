package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class ChannelSegmentFeedbackRepository
        extends AbstractStatsMaintainingDBRepository<ChannelSegmentFeedbackEntity, String> implements RepositoryManager {

    @Override
    public DBEntity<ChannelSegmentFeedbackEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow) {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        logger.debug("result set is not null");
        final String adGroupId = row.getString("ad_group_id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        try {
            final String advertiserId = row.getString("advertiser_id");
            final double eCPM = row.getDouble("ecpm");
            final double fillRatio = row.getDouble("fill_ratio");
            final int todayImpressions = row.getInt("today_impressions");

            final ChannelSegmentFeedbackEntity.Builder builder = ChannelSegmentFeedbackEntity.newBuilder();
            builder.setAdvertiserId(advertiserId);
            builder.setAdGroupId(adGroupId);
            builder.setECPM(eCPM);
            builder.setFillRatio(fillRatio);
            builder.setTodayImpressions(todayImpressions);

            final ChannelSegmentFeedbackEntity entity = builder.build();

            logger.debug("adgroup id for the loaded channelSegmentFeedbackEntity is " + adGroupId);
            return new DBEntity<ChannelSegmentFeedbackEntity, String>(entity, modifiedOn);
        } catch (final Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<ChannelSegmentFeedbackEntity, String>(new EntityError<String>(adGroupId,
                    "ERROR_IN_EXTRACTING_SEGMENT"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(final ChannelSegmentFeedbackEntity entity) {
        if (entity.getAdGroupId() == null) {
            return true;
        }
        return false;
    }

    @Override
    public HashIndexKeyBuilder<ChannelSegmentFeedbackEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public ChannelSegmentFeedbackEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
