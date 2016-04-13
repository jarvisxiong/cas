package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ishanbhatnagar on 12/3/15.
 */
@Getter
public class SdkMraidMapEntity implements IdentifiableEntity<String> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final String sdkName;
    private final String mraidPath;
    private final Timestamp modifiedOn;

    public SdkMraidMapEntity(final Builder builder) {
        this.sdkName = builder.sdkName;
        this.mraidPath = builder.mraidPath;
        this.modifiedOn = builder.modifiedOn;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String sdkName;
        private String mraidPath;
        private Timestamp modifiedOn;

        public SdkMraidMapEntity build() {
            return new SdkMraidMapEntity(this);
        }
    }

    @Override
    public String getId() {
        return sdkName;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }
}