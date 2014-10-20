package com.inmobi.adserve.channels.entity;

import lombok.Data;
import lombok.Setter;

import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.types.CreativeExposure;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Data
public class CreativeEntity implements IdentifiableEntity<CreativeQuery> {

	private static final long serialVersionUID = 1L;
	private final String advertiserId;
	private final String creativeId;
	private final CreativeExposure exposureLevel;
	private final String imageUrl;

	public CreativeEntity(final Builder builder) {
		advertiserId = builder.advertiserId;
		creativeId = builder.creativeId;
		exposureLevel = builder.exposureLevel;
		imageUrl = builder.imageUrl;
	}

	public static CreativeEntity.Builder newBuilder() {
		return new Builder();
	}

	@Setter
	public static class Builder {
		private String advertiserId;
		private String creativeId;
		private CreativeExposure exposureLevel;
		private String imageUrl;

		public CreativeEntity build() {
			return new CreativeEntity(this);
		}
	}

	@Override
	public String getJSON() {
		return null;
	}

	@Override
	public CreativeQuery getId() {
		return new CreativeQuery(advertiserId, creativeId);
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
		final CreativeEntity other = (CreativeEntity) obj;
		if (null == advertiserId) {
			if (null != other.advertiserId) {
				return false;
			}
		} else if (!advertiserId.equals(other.advertiserId)) {
			return false;
		}
		if (null == creativeId) {
			if (null != other.creativeId) {
				return false;
			}
		} else if (!creativeId.equals(other.creativeId)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (advertiserId == null ? 0 : advertiserId.hashCode());
		result = prime * result + (creativeId == null ? 0 : creativeId.hashCode());
		return result;
	}
}
