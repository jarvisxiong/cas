package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import lombok.Data;
import lombok.Setter;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Data
public class CurrencyConversionEntity implements IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final String currencyId;
    private final Double conversionRate;
    private final Timestamp startDate;
    private final Timestamp endDate;
    private final Timestamp modifiedOn;

    public CurrencyConversionEntity(final Builder builder) {
        currencyId = builder.currencyId;
        conversionRate = builder.conversionRate;
        startDate = builder.startDate;
        endDate = builder.endDate;
        modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String currencyId;
        private Double conversionRate;
        private Timestamp startDate;
        private Timestamp endDate;
        private Timestamp modifiedOn;

        public CurrencyConversionEntity build() {
            return new CurrencyConversionEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

    @Override
    public String getId() {
        return currencyId;
    }

}
