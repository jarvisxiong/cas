package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType;
import com.inmobi.adserve.contracts.misc.contentjson.NativeContentJsonObject;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public final class NativeAdTemplateEntity implements IdentifiableEntity<Long> {
    private static final long serialVersionUID = -648051414378424341L;
    private final long placementId;
    private final long nativeTemplateId;
    private final String mandatoryKey;
    private final String imageKey;
    private NativeAdContentUILayoutType nativeUILayout;
    private NativeContentJsonObject contentJson;
    private final String template;
    private final Timestamp modifiedOn;

    @Override
    public String getJSON() {
        return String
                .format("{\"placementId\":\"%d\",\"nativeTemplateId\":%d,\"mandatoryKey\":\"%s\",\"imageKey\":\"%s\",\"template\":\"%s\"}",
                        placementId, nativeTemplateId, mandatoryKey, imageKey, template);
    }

    @Override
    public Long getId() {
        return placementId;
    }
}
