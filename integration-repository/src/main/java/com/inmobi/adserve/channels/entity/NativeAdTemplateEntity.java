package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.query.NativeAdTemplateQuery;
import com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType;
import com.inmobi.adserve.contracts.misc.contentjson.NativeContentJsonObject;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public final class NativeAdTemplateEntity implements IdentifiableEntity<NativeAdTemplateQuery> {
    public static final String VAST_KEY = "ad.videos";
    private static final long serialVersionUID = -648051414378424341L;
    private final long placementId;
    private final TemplateClass templateClass;
    private final Long templateId;
    private final String mandatoryKey;
    private final String imageKey;
    private NativeAdContentUILayoutType nativeUILayout;
    private NativeContentJsonObject contentJson;
    private final String template;
    private final Timestamp modifiedOn;

    @Override
    public String getJSON() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public NativeAdTemplateQuery getId() {
        return new NativeAdTemplateQuery(placementId, templateClass);
    }

    public enum TemplateClass {
        VAST, STATIC
    }
}
