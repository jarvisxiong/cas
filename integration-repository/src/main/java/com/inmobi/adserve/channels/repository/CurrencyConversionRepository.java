package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
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


public class CurrencyConversionRepository extends
        AbstractStatsMaintainingDBRepository<CurrencyConversionEntity, String> implements RepositoryManager {

    @Override
    public DBEntity<CurrencyConversionEntity, String> buildObjectFromRow(ResultSetRow resultSetRow)
            throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        Timestamp modifiedOn = row.getTimestamp("modified_on");
        Integer id = row.getInt("id");
        String currencyId = row.getString("currency_id");
        try {
            Double conversionRate = row.getDouble("conversion_rate");
            Timestamp startDate = row.getTimestamp("start_date");
            Timestamp endDate = row.getTimestamp("end_date");

            CurrencyConversionEntity.Builder builder = CurrencyConversionEntity.newBuilder();
            builder.setCurrencyId(currencyId);
            builder.setConversionRate(conversionRate);
            builder.setStartDate(startDate);
            builder.setEndDate(endDate);
            builder.setModifiedOn(modifiedOn);
            CurrencyConversionEntity entity = builder.build();
            if (logger.isDebugEnabled()) {
                logger.debug("Found Currency Conversion Entity : " + entity);
            }
            return new DBEntity<CurrencyConversionEntity, String>(entity, modifiedOn);
        }
        catch (Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<CurrencyConversionEntity, String>(new EntityError<String>(currencyId,
                    "ERROR_IN_EXTRACTING_CURRENCY_CONVERSION_RATE"), modifiedOn);
        }
    }

    @Override
    public boolean isObjectToBeDeleted(CurrencyConversionEntity entity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<CurrencyConversionEntity> getHashIndexKeyBuilder(String className) {
        return null;
    }

    @Override
    public CurrencyConversionEntity queryUniqueResult(RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
