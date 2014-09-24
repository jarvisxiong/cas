package com.inmobi.adserve.channels.entity;

import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Data;
import lombok.Setter;

import java.util.Map;
import java.util.Set;


@Data
public class PricingEngineEntity implements IdentifiableEntity<PricingEngineQuery> {

    private static final long              serialVersionUID              = 1L;
    private final Integer                  countryId;
    private final Integer                  osId;
    private final double                   rtbFloor;
    private final double                   dcpFloor;
    private final Map<String, Set<String>> supplyToDemandMap;

    public static final byte[][]           DEFAULT_SUPPLY_DEMAND_MAPPING = { { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0 },
            { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 }, { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 }, { 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, };

    public PricingEngineEntity(Builder builder) {
        this.countryId = builder.countryId;
        this.osId = builder.osId;
        this.rtbFloor = builder.rtbFloor;
        this.dcpFloor = builder.dcpFloor;
        this.supplyToDemandMap = builder.supplyToDemandMap;
    }

    public static PricingEngineEntity.Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private Integer                  countryId;
        private Integer                  osId;
        private double                   rtbFloor;
        private double                   dcpFloor;
        private Map<String, Set<String>> supplyToDemandMap;

        public PricingEngineEntity build() {
            return new PricingEngineEntity(this);
        }
    }

    public boolean isSupplyAcceptsDemand(int supply, int demand) {
        if (this.supplyToDemandMap != null && this.supplyToDemandMap.containsKey(String.valueOf(supply))) {
            return this.supplyToDemandMap.get(String.valueOf(supply)).contains(String.valueOf(demand));
        }
        return DEFAULT_SUPPLY_DEMAND_MAPPING[supply][demand] == 1;
    }

    @Override
    public String getJSON() {
        return null;
    }

    @Override
    public PricingEngineQuery getId() {
        return new PricingEngineQuery(this.getCountryId(), this.getOsId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PricingEngineEntity other = (PricingEngineEntity) obj;
        if (countryId == null && other.countryId != null) {
                return false;
        } else if (!countryId.equals(other.countryId)) {
            return false;
        }
        if (osId == null && other.osId != null){
                return false;
        } else if (!osId.equals(other.osId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((countryId == null) ? 0 : countryId.hashCode());
        result = prime * result + ((osId == null) ? 0 : osId.hashCode());
        return result;
    }
}
