package com.inmobi.adserve.channels.api.natives;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeConstraints;
import com.inmobi.adserve.channels.repository.NativeConstraints.Mandatory;
import com.inmobi.adserve.contracts.ix.request.nativead.Data;
import com.inmobi.adserve.contracts.ix.request.nativead.Data.DataAssetType;
import com.inmobi.adserve.contracts.ix.request.nativead.Image.ImageAssetType;
import com.inmobi.adserve.contracts.ix.request.nativead.NativeReqObj;
import com.inmobi.adserve.contracts.ix.request.nativead.Title;
import com.inmobi.casthrift.rtb.Native;

public class NativeBuilderImpl implements NativeBuilder {
    private final NativeAdTemplateEntity templateEntity;
    private final Native nativeObj;
    private final NativeReqObj nativeReqObj;

    @Inject
    public NativeBuilderImpl(@Assisted final NativeAdTemplateEntity templateEntity) {
        nativeObj = new Native();
        nativeReqObj = new NativeReqObj(1);
        this.templateEntity = templateEntity;
    }

    @Override
    public Native build() {
        nativeObj.setMandatory(NativeConstraints.getRTBDMandatoryList(templateEntity.getMandatoryKey()));
        nativeObj.setImage(NativeConstraints.getRTBImage(templateEntity.getImageKey()));
        return nativeObj;
    }

    @Override
    public com.inmobi.adserve.contracts.ix.request.nativead.Native buildNativeIX() {
        buildMandatory();
        buildNonMandatory();
        return new com.inmobi.adserve.contracts.ix.request.nativead.Native(nativeReqObj);
    }

    private void buildMandatory() {
        final List<Mandatory> mandatoryKeys = NativeConstraints.getIXMandatoryList(templateEntity.getMandatoryKey());
        for (final Mandatory mandatory : mandatoryKeys) {
            switch (mandatory) {
                case TITLE:
                    nativeReqObj.addAsset(true, new Title(100));
                    break;
                case ICON:
                    final com.inmobi.adserve.contracts.ix.request.nativead.Image icon =
                            new com.inmobi.adserve.contracts.ix.request.nativead.Image();
                    icon.setType(ImageAssetType.ICON);
                    icon.setWmin(300);
                    icon.setHmin(300);
                    nativeReqObj.addAsset(true, icon);
                    break;
                case DESCRIPTION:
                    nativeReqObj.addAsset(true, new Data(DataAssetType.DESC));
                    break;
                case SCREEN_SHOT:
                    final com.inmobi.adserve.contracts.ix.request.nativead.Image screen =
                            NativeConstraints.getIXImage(templateEntity.getImageKey());
                    nativeReqObj.addAsset(true, screen);
                    break;
            }
        }
        nativeReqObj.addAsset(true, new Data(DataAssetType.CTA_TEXT));
    }

    private void buildNonMandatory() {
        nativeReqObj.addAsset(false, new Data(DataAssetType.DOWNLOADS));
        nativeReqObj.addAsset(false, new Data(DataAssetType.RATING));
    }


}
