package com.inmobi.adserve.channels.repository;

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

import java.sql.Timestamp;


public class ChannelSegmentFeedbackRepository extends
        AbstractStatsMaintainingDBRepository<ChannelSegmentFeedbackEntity, String> implements RepositoryManager {

    @Override
    public DBEntity<ChannelSegmentFeedbackEntity, String> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        logger.debug("result set is not null");
        String adGroupId = row.getString("ad_group_id");
        Timestamp modifiedOn = row.getTimestamp("modified_on");
        try {
            String advertiserId = row.getString("advertiser_id");
            double eCPM = row.getDouble("ecpm");
            double fillRatio = row.getDouble("fill_ratio");
            int todayImpressions = row.getInt("today_impressions");

            ChannelSegmentFeedbackEntity.Builder builder = ChannelSegmentFeedbackEntity.newBuilder();
            builder.setAdvertiserId(advertiserId);
            builder.setAdGroupId(adGroupId);
            builder.setECPM(eCPM);
            builder.setFillRatio(fillRatio);
            builder.setTodayImpressions(todayImpressions);

            ChannelSegmentFeedbackEntity entity = builder.build();

            logger.debug("adgroup id for the loaded channelSegmentFeedbackEntity is " + adGroupId);
            return new DBEntity<ChannelSegmentFeedbackEntity, String>(entity, modifiedOn);
        }
        catch (Exception e) {
            if (e instanceof RepositoryException) {
                RepositoryException r = new RepositoryException(e.getMessage());
                r.setStackTrace(e.getStackTrace());
                throw r;
            }
            logger.error("Error in resultset row", e);
            return new DBEntity<ChannelSegmentFeedbackEntity, String>(new EntityError<String>(adGroupId,
                    "ERROR_IN_EXTRACTING_SEGMENT"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(ChannelSegmentFeedbackEntity entity) {
        if (entity.getAdGroupId() == null) {
            return true;
        }
        return false;
    }

    @Override
    public HashIndexKeyBuilder<ChannelSegmentFeedbackEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public ChannelSegmentFeedbackEntity queryUniqueResult(RepositoryQuery q) throws RepositoryException {
        return null;
    }

}