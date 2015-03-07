package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

@Getter
public final class NativeAdTemplateEntity implements IdentifiableEntity<String> {
    private static final long serialVersionUID = -648051414378424341L;
    private final String siteId;
    private final long nativeAdId;
    private final String mandatoryKey;
    private final String imageKey;
    private final String template;
    private final Timestamp modifiedOn;

    private NativeAdTemplateEntity(final Builder builder) {
        siteId = builder.siteId;
        nativeAdId = builder.nativeAdId;
        imageKey = builder.imageKey;
        mandatoryKey = builder.mandatoryKey;
        template = builder.template;
        modifiedOn = builder.modifiedOn;
    }

    @Override
    public String getJSON() {
        return String
                .format("{\"siteId\":\"%s\",\"nativeAdId\":%s,\"mandatoryKey\":\"%s\",\"imageKey\":\"%s\",\"template\":\"%s\"}",
                        siteId, nativeAdId, mandatoryKey, imageKey, template);
    }

    @Override
    public String getId() {
        return siteId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String siteId;
        private long nativeAdId;
        private String mandatoryKey;
        private String imageKey;
        private String template;
        private Timestamp modifiedOn;

        public NativeAdTemplateEntity build() {
            return new NativeAdTemplateEntity(this);
        }
    }

}
