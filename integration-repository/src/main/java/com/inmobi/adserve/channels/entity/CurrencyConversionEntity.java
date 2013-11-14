package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Data;
import lombok.Setter;

import java.sql.Timestamp;


@Data
public class CurrencyConversionEntity implements IdentifiableEntity<String> {

    private static final long serialVersionUID = 1L;

    private final Integer     id;
    private final String      currencyId;
    private final Double      conversionRate;
    private final Timestamp   startDate;
    private final Timestamp   endDate;
    private final Timestamp   modifiedOn;

    public CurrencyConversionEntity(Builder builder) {
        this.id = builder.id;
        this.currencyId = builder.currencyId;
        this.conversionRate = builder.conversionRate;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private Integer   id;
        private String    currencyId;
        private Double    conversionRate;
        private Timestamp startDate;
        private Timestamp endDate;
        private Timestamp modifiedOn;

        public CurrencyConversionEntity build() {
            return new CurrencyConversionEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return null;
    }

    @Override
    public String getId() {
        return currencyId;
    }

}
