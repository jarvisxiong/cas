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
public class SlotSizeMapRepository extends AbstractStatsMaintainingDBRepository<SlotSizeMapEntity, Short>
        implements
            RepositoryManager {
    private static final Short SLOT_ID_9 = 9;
    private static final Short SLOT_ID_24 = 24;
    private static final Dimension DIMENSION_320x50 = new Dimension(320, 50);

    @Override
    public DBEntity<SlotSizeMapEntity, Short> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Short slotId = (short) row.getInt("id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        try {
            final Integer height = row.getInt("height");
            final Integer width = row.getInt("width");
            // Slots Ids 9=320x48 & 24=320x53 for which we don't have demand. Thus map these slots to 320x50
            final Dimension dim =
                    slotId == SLOT_ID_9 || slotId == SLOT_ID_24 ? DIMENSION_320x50 : new Dimension(width, height);

            final SlotSizeMapEntity.Builder builder = SlotSizeMapEntity.newBuilder();
            builder.slotId(slotId);
            builder.dimension(dim);
            builder.modifiedOn(modifiedOn);
            return new DBEntity<SlotSizeMapEntity, Short>(builder.build(), modifiedOn);
        } catch (final Exception exp) {
            logger.error("Error in resultset row", exp);
            return new DBEntity<SlotSizeMapEntity, Short>(
                    new EntityError<Short>(slotId, "ERROR_IN_EXTRACTING_SLOT_MAP"), modifiedOn);
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
