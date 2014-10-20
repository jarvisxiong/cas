package com.inmobi.adserve.channels.repository;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.Repository;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;


public class PricingEngineRepository
        extends AbstractStatsMaintainingDBRepository<PricingEngineEntity, PricingEngineQuery>
        implements
            Repository,
            RepositoryManager {

    private static final String JSON_ERROR = "JSON_ERROR";

    @Override
    public PricingEngineEntity queryUniqueResult(final RepositoryQuery pricingEngineIdQuery) throws RepositoryException {
        final Collection<PricingEngineEntity> pricingEngineEntityResultSet = query(pricingEngineIdQuery);
        if (pricingEngineEntityResultSet == null || pricingEngineEntityResultSet.isEmpty()) {
            return null;
        }
        return (PricingEngineEntity) pricingEngineEntityResultSet.toArray()[0];
    }

    @Override
    public DBEntity<PricingEngineEntity, PricingEngineQuery> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Timestamp modifyTime = row.getTimestamp("modified_on");
        final PricingEngineEntity.Builder builder = PricingEngineEntity.newBuilder();
        final int countryId = row.getInt("country_id");
        final int osId = row.getInt("os_id");
        builder.setCountryId(countryId);
        builder.setOsId(osId);
        builder.setRtbFloor(row.getDouble("rtb_floor"));
        builder.setDcpFloor(row.getDouble("dcp_floor"));
        try {
            builder.setSupplyToDemandMap(getSupplyToDemandMap(row.getString("supply_demand_json")));
        } catch (final Exception e) {
            return new DBEntity<PricingEngineEntity, PricingEngineQuery>(new EntityError<PricingEngineQuery>(
                    new PricingEngineQuery(countryId, osId), JSON_ERROR), modifyTime);
        }
        final PricingEngineEntity entity = builder.build();

        if (logger.isDebugEnabled()) {
            logger.debug("Adding pricing entity : " + entity);
        }
        return new DBEntity<PricingEngineEntity, PricingEngineQuery>(entity, modifyTime);
    }

    Map<String, Set<String>> getSupplyToDemandMap(final String supplyDemandJson) {
        if (supplyDemandJson == null) {
            return null;
        }
        final Gson gson = new Gson();
        final Type type = new TypeToken<HashMap<String, HashSet<String>>>() {

            /**
           * 
           */
            private static final long serialVersionUID = 1L;
        }.getType();
        return gson.fromJson(supplyDemandJson, type);
    }

    @Override
    public HashIndexKeyBuilder<PricingEngineEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public boolean isObjectToBeDeleted(final PricingEngineEntity pricingEngineEntity) {
        return pricingEngineEntity.getRtbFloor() == 0.0 && pricingEngineEntity.getDcpFloor() == 0.0;
    }
}
