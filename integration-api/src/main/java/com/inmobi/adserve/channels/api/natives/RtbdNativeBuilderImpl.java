package com.inmobi.adserve.channels.api.natives;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeConstraints;
import com.inmobi.casthrift.rtb.Native;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */
public final class RtbdNativeBuilderImpl extends NativeBuilderImpl {
    private final Native nativeObj;

    @Inject
    public RtbdNativeBuilderImpl(@Assisted NativeAdTemplateEntity templateEntity) {
        super(templateEntity);
        nativeObj = new Native();
    }

    @Override
    public Native buildNative() {
        nativeObj.setMandatory(NativeConstraints.getRTBDMandatoryList(templateEntity.getMandatoryKey()));
        nativeObj.setImage(NativeConstraints.getRTBImage(templateEntity.getImageKey()));
        return nativeObj;
    }
}
