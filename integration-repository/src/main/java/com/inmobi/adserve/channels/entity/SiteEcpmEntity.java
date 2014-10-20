package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Getter
@ToString
public class SiteEcpmEntity implements IdentifiableEntity<SiteEcpmQuery> {

	private static final long serialVersionUID = 1L;

	private final String siteId;
	private final Integer countryId;
	private final Integer osId;
	private final double ecpm;
	private final double networkEcpm;
	private final Timestamp modifiedOn;

	public SiteEcpmEntity(final Builder builder) {
		siteId = builder.siteId;
		countryId = builder.countryId;
		osId = builder.osId;
		ecpm = builder.ecpm;
		networkEcpm = builder.networkEcpm;
		modifiedOn = builder.modifiedOn;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	@Setter
	public static class Builder {
		private String siteId;
		private Integer countryId;
		private Integer osId;
		private double ecpm;
		private double networkEcpm;
		private Timestamp modifiedOn;

		public SiteEcpmEntity build() {
			return new SiteEcpmEntity(this);
		}
	}

	@Override
	public SiteEcpmQuery getId() {
		return new SiteEcpmQuery(siteId, countryId, osId);
	}

	@Override
	public String getJSON() {
		return null;
	}
}
