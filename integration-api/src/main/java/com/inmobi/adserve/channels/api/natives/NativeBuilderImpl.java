package com.inmobi.adserve.channels.api.natives;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;

/**
 * 
 * @author ritwik.kumar
 *
 */
public abstract class NativeBuilderImpl implements NativeBuilder {
    protected final NativeAdTemplateEntity templateEntity;

    /**
     * 
     * @param templateEntity
     */
    public NativeBuilderImpl(final NativeAdTemplateEntity templateEntity) {
        this.templateEntity = templateEntity;
    }
}
