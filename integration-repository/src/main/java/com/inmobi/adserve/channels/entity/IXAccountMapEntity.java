package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import lombok.Data;
import lombok.Setter;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

/**
 * @author ritwik.kumar
 */
@Data
public class IXAccountMapEntity implements IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;
    private final Long rpNetworkId;
    private final String inmobiAccountId;
    private final String networkName;
    private final String networkType;
    private final Timestamp modifiedOn;

    public IXAccountMapEntity(final Builder builder) {
        rpNetworkId = builder.rpNetworkId;
        inmobiAccountId = builder.inmobiAccountId;
        networkName = builder.networkName;
        networkType = builder.networkType;
        modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private Long rpNetworkId;
        private String inmobiAccountId;
        private String networkName;
        private String networkType;
        private Timestamp modifiedOn;

        public IXAccountMapEntity build() {
            return new IXAccountMapEntity(this);
        }
    }

    @Override
    public String getJSON() {
        return String
                .format("{\"rpNetworkId\":\"%s\",\"inmobiAccountId\":%s,\"networkName\":\"%s\",\"networkType\":%s,\"modifiedOn\":\"%s\"}",
                        rpNetworkId, inmobiAccountId, networkName, networkType, modifiedOn);
    }

    @Override
    public Long getId() {
        return rpNetworkId;
    }

}
