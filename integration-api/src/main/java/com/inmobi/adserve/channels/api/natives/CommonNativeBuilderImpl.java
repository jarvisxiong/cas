package com.inmobi.adserve.channels.api.natives;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeConstraints;
import com.inmobi.adserve.channels.repository.NativeConstraints.Mandatory;
import com.inmobi.adserve.contracts.common.request.nativead.Data;
import com.inmobi.adserve.contracts.common.request.nativead.Image;
import com.inmobi.adserve.contracts.common.request.nativead.Native;
import com.inmobi.adserve.contracts.common.request.nativead.NativeReqObj;
import com.inmobi.adserve.contracts.common.request.nativead.Title;
import com.inmobi.adserve.contracts.iab.NativeLayoutId;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */
public final class CommonNativeBuilderImpl extends NativeBuilderImpl {
    private static final int DEFAULT_MAX_TITLE_LENGTH = 100;
    private static final int DEFAULT_MAX_DESC_LENGTH = 100;
    private static final int ICON_DEFAULT_DIMENSION = 37;
    private static final int DEFAULT_AD_UNIT_ID = 500;

    private final NativeReqObj nativeReqObj;

    @Inject
    public CommonNativeBuilderImpl(@Assisted final NativeAdTemplateEntity templateEntity) {
        super(templateEntity);
        final NativeLayoutId layoutId = NativeLayoutId.findByInmobiNativeUILayoutType(templateEntity.getNativeUILayout());
        nativeReqObj = new NativeReqObj(layoutId);
        // https://jira.corp.inmobi.com/browse/CAS-81
        nativeReqObj.setAdunit(DEFAULT_AD_UNIT_ID);
    }

    @Override
    public Native buildNative() {
        buildUsingNativeConstraints();
        return new Native(nativeReqObj);
    }

    private void buildUsingNativeConstraints() {
        final List<Mandatory> mandatoryKeys = NativeConstraints.getMandatoryList(templateEntity.getMandatoryKey());
        nativeReqObj.addAsset(false, new Data(Data.DataAssetType.CTA_TEXT));
        nativeReqObj.addAsset(false, new Data(Data.DataAssetType.RATING));
        // Add mandatory assets
        // https://jira.corp.inmobi.com/browse/IX-265
        // Making title, icon, description as optional since we have a default of it
        for (final Mandatory mandatory : mandatoryKeys) {
            switch (mandatory) {
                case TITLE:
                    nativeReqObj.addAsset(false, new Title(DEFAULT_MAX_TITLE_LENGTH));
                    break;
                case ICON:
                    final Image icon = new Image();
                    icon.setType(Image.ImageAssetType.ICON);
                    icon.setWmin(ICON_DEFAULT_DIMENSION);
                    icon.setHmin(ICON_DEFAULT_DIMENSION);
                    nativeReqObj.addAsset(false, icon);
                    break;
                case DESCRIPTION:
                    final Data desc = new Data(Data.DataAssetType.DESC);
                    desc.setLen(DEFAULT_MAX_DESC_LENGTH);
                    nativeReqObj.addAsset(false, desc);
                    break;
                case SCREEN_SHOT:
                    final Image screen = NativeConstraints.getImage(templateEntity.getImageKey());
                    nativeReqObj.addAsset(true, screen);
                    break;
            }
        }
    }

}
