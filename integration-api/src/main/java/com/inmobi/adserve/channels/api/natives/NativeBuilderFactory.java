package com.inmobi.adserve.channels.api.natives;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;



public interface NativeBuilderFactory {

    /**
     * 
     * @param entity
     * @return
     */
    NativeBuilder create(final NativeAdTemplateEntity entity);

}
