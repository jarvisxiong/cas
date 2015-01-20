package com.inmobi.adserve.channels.repository;

import java.awt.Dimension;
import java.sql.Timestamp;

import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;

/**
 * Created by anshul.soni on 27/11/14.
 */
public class SlotSizeMapRepository extends AbstractStatsMaintainingDBRepository <SlotSizeMapEntity, Short> implements RepositoryManager {
    @Override
    public DBEntity<SlotSizeMapEntity, Short> buildObjectFromRow(final ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Short slotId = (short) row.getInt("id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {
            final Integer height = row.getInt("height");
            final Integer width = row.getInt("width");
            final Dimension dimension = new Dimension(width, height);

            final SlotSizeMapEntity.Builder builder = SlotSizeMapEntity.newBuilder();
            builder.setSlotId(slotId);
            builder.setDimension(dimension);
            builder.setModifiedOn(modifiedOn);

            final SlotSizeMapEntity entity = builder.build();
            return new DBEntity<SlotSizeMapEntity, Short>(entity, modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<SlotSizeMapEntity, Short>(new EntityError<Short>(slotId,
                    "ERROR_IN_EXTRACTING_SLOT_MAP"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(final SlotSizeMapEntity entity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SlotSizeMapEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public SlotSizeMapEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
