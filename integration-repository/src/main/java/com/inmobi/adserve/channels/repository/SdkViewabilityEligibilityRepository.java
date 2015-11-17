package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.inmobi.adserve.channels.entity.SdkViewabilityEligibilityEntity;
import com.inmobi.adserve.channels.query.SdkViewabilityEligibilityQuery;
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
 * Created by ishan.bhatnagar on 04/11/15.
 */
public class SdkViewabilityEligibilityRepository
        extends AbstractStatsMaintainingDBRepository<SdkViewabilityEligibilityEntity, SdkViewabilityEligibilityQuery>
        implements RepositoryManager {

    @Override
    public DBEntity<SdkViewabilityEligibilityEntity, SdkViewabilityEligibilityQuery> buildObjectFromRow(
        ResultSetRow resultSetRow) throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        final Integer countryId = row.getInt("country_id");
        final String adType = row.getString("ad_type");
        final Integer dst = row.getInt("dst");

        try {
            final Pair<Boolean, Set<Integer>> sdkViewabilityInclusionExclusion =
                IXPackageRepository.extractSdkVersionTargeting(row.getString("sdk_ie_list"));

            final SdkViewabilityEligibilityEntity.Builder builder = SdkViewabilityEligibilityEntity.newBuilder();
            builder.countryId(countryId);
            builder.adType(adType);
            builder.dst(dst);
            builder.sdkViewabilityInclusionExclusion(sdkViewabilityInclusionExclusion);
            builder.modifiedOn(modifiedOn);
            return new DBEntity<>(builder.build(), modifiedOn);
        } catch (final Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<>(new EntityError<>(new SdkViewabilityEligibilityQuery(countryId, adType, dst),
                "ERROR_IN_EXTRACTING_SDK_VIEWABILITY_ELIGIBILITY"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(SdkViewabilityEligibilityEntity sdkViewabilityEligibilityEntity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<SdkViewabilityEligibilityEntity> getHashIndexKeyBuilder(String s) {
        return null;
    }

    @Override
    public SdkViewabilityEligibilityEntity queryUniqueResult(RepositoryQuery repositoryQuery)
        throws RepositoryException {
        return null;
    }
}
