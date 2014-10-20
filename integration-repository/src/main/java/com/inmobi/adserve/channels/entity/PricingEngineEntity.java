package com.inmobi.adserve.channels.entity;

import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.Setter;

import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Data
public class PricingEngineEntity implements IdentifiableEntity<PricingEngineQuery> {

	private static final long serialVersionUID = 1L;
	private final Integer countryId;
	private final Integer osId;
	private final double rtbFloor;
	private final double dcpFloor;
	private final Map<String, Set<String>> supplyToDemandMap;

	public static final byte[][] DEFAULT_SUPPLY_DEMAND_MAPPING = { {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, {1, 1, 1, 0, 0, 0, 0, 0, 0, 0}, {1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
			{1, 1, 1, 1, 1, 0, 0, 0, 0, 0}, {1, 1, 1, 1, 1, 1, 0, 0, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
			{1, 1, 1, 1, 1, 1, 1, 1, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},};

	public PricingEngineEntity(final Builder builder) {
		countryId = builder.countryId;
		osId = builder.osId;
		rtbFloor = builder.rtbFloor;
		dcpFloor = builder.dcpFloor;
		supplyToDemandMap = builder.supplyToDemandMap;
	}

	public static PricingEngineEntity.Builder newBuilder() {
		return new Builder();
	}

	@Setter
	public static class Builder {
		private Integer countryId;
		private Integer osId;
		private double rtbFloor;
		private double dcpFloor;
		private Map<String, Set<String>> supplyToDemandMap;

		public PricingEngineEntity build() {
			return new PricingEngineEntity(this);
		}
	}

	public boolean isSupplyAcceptsDemand(final int supply, final int demand) {
		if (supplyToDemandMap != null && supplyToDemandMap.containsKey(String.valueOf(supply))) {
			return supplyToDemandMap.get(String.valueOf(supply)).contains(String.valueOf(demand));
		}
		return DEFAULT_SUPPLY_DEMAND_MAPPING[supply][demand] == 1;
	}

	@Override
	public String getJSON() {
		return null;
	}

	@Override
	public PricingEngineQuery getId() {
		return new PricingEngineQuery(getCountryId(), getOsId());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PricingEngineEntity other = (PricingEngineEntity) obj;
		if (null == countryId) {
			if (null != other.countryId) {
				return false;
			}
		} else if (!countryId.equals(other.countryId)) {
			return false;
		}
		if (osId == null) {
			if (other.osId != null) {
				return false;
			}
		} else if (!osId.equals(other.osId)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (countryId == null ? 0 : countryId.hashCode());
		result = prime * result + (osId == null ? 0 : osId.hashCode());
		return result;
	}
}
