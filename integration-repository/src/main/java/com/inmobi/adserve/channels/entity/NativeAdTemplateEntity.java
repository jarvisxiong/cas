package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType;
import com.inmobi.adserve.contracts.misc.contentjson.NativeContentJsonObject;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public final class NativeAdTemplateEntity implements IdentifiableEntity<String> {
    private static final long serialVersionUID = -648051414378424341L;
    private final String siteId;
    private final long nativeAdId;
    private final String mandatoryKey;
    private final String imageKey;
    private NativeAdContentUILayoutType nativeUILayout;
    private NativeContentJsonObject contentJson;
    private final String template;
    private final Timestamp modifiedOn;

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
}
