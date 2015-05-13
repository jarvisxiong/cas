package com.inmobi.adserve.channels.api.natives;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;

public abstract class NativeBuilderImpl implements NativeBuilder {
    protected final NativeAdTemplateEntity templateEntity;

    public NativeBuilderImpl(final NativeAdTemplateEntity templateEntity) {
        this.templateEntity = templateEntity;
    }
}
