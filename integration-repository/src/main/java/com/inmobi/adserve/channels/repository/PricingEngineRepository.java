package com.inmobi.adserve.channels.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.*;


public class PricingEngineRepository extends
        AbstractStatsMaintainingDBRepository<PricingEngineEntity, PricingEngineQuery> implements Repository,
        RepositoryManager {

    private static final String JSON_ERROR = "JSON_ERROR";

    @Override
    public PricingEngineEntity queryUniqueResult(RepositoryQuery pricingEngineIdQuery) throws RepositoryException {
        Collection<PricingEngineEntity> pricingEngineEntityResultSet = query(pricingEngineIdQuery);
        if (pricingEngineEntityResultSet == null || pricingEngineEntityResultSet.size() == 0) {
            return null;
        }
        else if (pricingEngineEntityResultSet.size() >= 1) {
            return (PricingEngineEntity) pricingEngineEntityResultSet.toArray()[0];
        }
        return null;
    }

    @Override
    public DBEntity<PricingEngineEntity, PricingEngineQuery> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        Timestamp modifyTime = row.getTimestamp("modified_on");
        PricingEngineEntity.Builder builder = PricingEngineEntity.newBuilder();
        int countryId = row.getInt("country_id");
        int osId = row.getInt("os_id");
        builder.setCountryId(countryId);
        builder.setOsId(osId);
        builder.setRtbFloor(row.getDouble("rtb_floor"));
        builder.setDcpFloor(row.getDouble("dcp_floor"));
        try {
            builder.setSupplyToDemandMap(getSupplyToDemandMap(row.getString("supply_demand_json")));
        }
        catch (Exception e) {
            return new DBEntity<PricingEngineEntity, PricingEngineQuery>(new EntityError<PricingEngineQuery>(
                    new PricingEngineQuery(countryId, osId), JSON_ERROR), modifyTime);
        }
        PricingEngineEntity entity = builder.build();

        if (logger.isDebugEnabled()) {
            logger.debug("Adding pricing entity : " + entity);
        }
        return new DBEntity<PricingEngineEntity, PricingEngineQuery>(entity, modifyTime);
    }

    Map<String, Set<String>> getSupplyToDemandMap(String supplyDemandJson) {
        if (supplyDemandJson == null) {
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, HashSet<String>>>() {
        }.getType();
        return gson.fromJson(supplyDemandJson, type);
    }

    @Override
    public HashIndexKeyBuilder<PricingEngineEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public boolean isObjectToBeDeleted(PricingEngineEntity pricingEngineEntity) {
        return pricingEngineEntity.getRtbFloor() == 0.0 && pricingEngineEntity.getDcpFloor() == 0.0;
    }
}
