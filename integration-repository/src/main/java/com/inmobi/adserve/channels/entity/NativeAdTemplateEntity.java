package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.query.NativeAdTemplateQuery;
import com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public final class NativeAdTemplateEntity implements IdentifiableEntity<NativeAdTemplateQuery> {
    public static final String VAST_KEY = "ad.videos";
    public static final String MOVIEBOARD_VAST_KEY = "ad.vast";
    public static final String MOVIEBOARD_CTA_KEY = "ad.cta.text";
    public static final List<String> MOVIEBOARD_REQUIRED_JPATH_KEYS = Arrays.asList(MOVIEBOARD_VAST_KEY, MOVIEBOARD_CTA_KEY);
    private static final long serialVersionUID = -648051414378424341L;
    private final static Gson GSON = new Gson();
    private final long placementId;
    private final TemplateClass templateClass;
    private final Long templateId;
    private final String mandatoryKey;
    private final String imageKey;
    private NativeAdContentUILayoutType nativeUILayout;
    private final String template;
    private final Timestamp modifiedOn;

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

    @Override
    public NativeAdTemplateQuery getId() {
        return new NativeAdTemplateQuery(placementId, templateClass);
    }

    public enum TemplateClass {
        VAST, STATIC, MOVIEBOARD
    }
}
