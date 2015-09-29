/**
 *
 */
package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.inmobi.adserve.channels.entity.CAUMetadataEntity;
import com.inmobi.adserve.channels.entity.CAUMetadataEntity.Constraint;
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
 * @author ritwik.kumar
 *
 */
public class CAUMetaDataRepository extends AbstractStatsMaintainingDBRepository<CAUMetadataEntity, Long>
        implements
            RepositoryManager {
    // 1,2,3 - Close , Animation, Frame
    private static Integer CLOSE_ELEMENT_ID = 1;
    private static Integer ANIMATION_ELEMENT_ID = 2;
    private static Integer FRAME_ELEMENT_ID = 3;
    private static final Set<Integer> SUPPORTED_ELEMENTS = Sets.newHashSet(CLOSE_ELEMENT_ID, ANIMATION_ELEMENT_ID,
            FRAME_ELEMENT_ID);
    private final static Gson GSON = new Gson();

    @Override
    public DBEntity<CAUMetadataEntity, Long> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Long id = (long) row.getInt("id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        try {
            final Integer[] elementIdsArr = (Integer[]) row.getArray("element_ids");
            if (elementIdsArr == null || elementIdsArr.length == 0) {
                throw new Exception("No Supported Elements for CAU Id " + id);
            }
            for (final Integer elementId : elementIdsArr) {
                if (!SUPPORTED_ELEMENTS.contains(elementId)) {
                    throw new Exception("Un-Supported Elements for CAU Id " + id + " Element Id " + elementId);
                }
            }
            
            final Object elementObject = row.getObject("element_json");
            final Object constraintObject = row.getObject("constraint_json");
            if (elementObject == null || constraintObject == null) {
                throw new Exception("element_json/constraint_json can not be null");
            }

            final CAUMetadataEntity.Builder builder = CAUMetadataEntity.newBuilder();
            builder.id(id);
            builder.version(row.getInt("version"));
            builder.elementSecureJson(elementObject.toString().replaceAll("http:", "https:"));
            final Constraint constraint = GSON.fromJson(constraintObject.toString(), Constraint.class);
            builder.constraint(constraint);

            final CAUMetadataEntity entity = builder.build();
            return new DBEntity<CAUMetadataEntity, Long>(entity, modifiedOn);
        } catch (final Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<CAUMetadataEntity, Long>(new EntityError<Long>(id, "ERROR_IN_READING_NATIVE_TEMPLATE"),
                    modifiedOn);
        }
    }



    public static void main(final String[] args) {
        final String json =
                "{\"supply_dim\":{\"w\":320,\"h\":480},\"creative_dim\":{\"min\":{\"w\":160.0,\"h\":240.0},\"max\":{\"w\":1600.0,\"h\":2400.0},\"actual\":{\"w\":199.0,\"h\":300.0},\"aspectRatio\":0.667,\"tolerance\":0.03}}";
        final Gson gson = new Gson();
        final Constraint constraint = gson.fromJson(json, Constraint.class);
        System.out.println(constraint);

        final Integer[] elementIdsArr = null;
        final Set<Integer> elementIdsSet = Sets.newHashSet(elementIdsArr);
        System.out.println(elementIdsSet);
    }

    @Override
    public CAUMetadataEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }



    @Override
    public boolean isObjectToBeDeleted(final CAUMetadataEntity object) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<CAUMetadataEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

}
