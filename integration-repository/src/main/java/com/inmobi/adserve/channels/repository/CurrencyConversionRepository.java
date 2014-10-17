package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;

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


public class CurrencyConversionRepository
    extends AbstractStatsMaintainingDBRepository<CurrencyConversionEntity, String> implements RepositoryManager {

  @Override
  public DBEntity<CurrencyConversionEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow)
      throws RepositoryException {
    final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
    final Timestamp modifiedOn = row.getTimestamp("modified_on");
    final String currencyId = row.getString("currency_id");
    try {
      final Double conversionRate = row.getDouble("conversion_rate");
      final Timestamp startDate = row.getTimestamp("start_date");
      final Timestamp endDate = row.getTimestamp("end_date");

      final CurrencyConversionEntity.Builder builder = CurrencyConversionEntity.newBuilder();
      builder.setCurrencyId(currencyId);
      builder.setConversionRate(conversionRate);
      builder.setStartDate(startDate);
      builder.setEndDate(endDate);
      builder.setModifiedOn(modifiedOn);
      final CurrencyConversionEntity entity = builder.build();
      if (logger.isDebugEnabled()) {
        logger.debug("Found Currency Conversion Entity : " + entity);
      }
      return new DBEntity<CurrencyConversionEntity, String>(entity, modifiedOn);
    } catch (final Exception e) {
      logger.error("Error in resultset row", e);
      return new DBEntity<CurrencyConversionEntity, String>(new EntityError<String>(currencyId,
          "ERROR_IN_EXTRACTING_CURRENCY_CONVERSION_RATE"), modifiedOn);
    }
  }

  @Override
  public boolean isObjectToBeDeleted(final CurrencyConversionEntity entity) {
    return false;
  }

  @Override
  public HashIndexKeyBuilder<CurrencyConversionEntity> getHashIndexKeyBuilder(final String className) {
    return null;
  }

  @Override
  public CurrencyConversionEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
    return null;
  }

}
