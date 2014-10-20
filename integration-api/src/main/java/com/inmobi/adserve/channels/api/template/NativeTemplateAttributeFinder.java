package com.inmobi.adserve.channels.api.template;

import com.inmobi.adserve.channels.api.attribute.NativeAttributeType;


public class NativeTemplateAttributeFinder {

    public <T> T findAttribute(final NativeAttributeType<T> type) {

        return type.getAttributes();
    }

}
