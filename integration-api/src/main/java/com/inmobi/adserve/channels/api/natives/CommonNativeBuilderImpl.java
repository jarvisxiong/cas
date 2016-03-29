package com.inmobi.adserve.channels.api.natives;

import static com.inmobi.adserve.channels.repository.NativeConstraints.LAYOUT_FEED;
import static com.inmobi.adserve.channels.repository.NativeConstraints.LAYOUT_ICON;
import static com.inmobi.adserve.channels.repository.NativeConstraints.LAYOUT_STREAM;

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
// import com.inmobi.adserve.contracts.misc.contentjson.CommonAssetAttributes;
// import com.inmobi.adserve.contracts.misc.contentjson.Dimension;
// import com.inmobi.adserve.contracts.misc.contentjson.ImageAsset;
// import com.inmobi.adserve.contracts.misc.contentjson.NativeContentJsonObject;
// import com.inmobi.adserve.contracts.misc.contentjson.OtherAsset;
// import com.inmobi.adserve.contracts.misc.contentjson.TextAsset;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */
public final class CommonNativeBuilderImpl extends NativeBuilderImpl {
    private static final NativeLayoutId DEFAULT_NATIVE_LAYOUT_ID_FOR_LAYOUT_ICON = NativeLayoutId.NEWS_FEED;
    private static final NativeLayoutId DEFAULT_NATIVE_LAYOUT_ID_FOR_LAYOUT_FEED = NativeLayoutId.NEWS_FEED;
    private static final NativeLayoutId DEFAULT_NATIVE_LAYOUT_ID_FOR_LAYOUT_STREAM = NativeLayoutId.CONTENT_STREAM;

    private static final int DEFAULT_MAX_TITLE_LENGTH = 100;
    private static final int DEFAULT_MAX_DESC_LENGTH = 100;
    private static final int ICON_DEFAULT_DIMENSION = 37;
    private static final int DEFAULT_AD_UNIT_ID = 500;

    private final NativeReqObj nativeReqObj;

    @Inject
    public CommonNativeBuilderImpl(@Assisted final NativeAdTemplateEntity templateEntity) {
        super(templateEntity);
        NativeLayoutId layoutId = NativeLayoutId.findByInmobiNativeUILayoutType(templateEntity.getNativeUILayout());
        if (null == layoutId) {
            switch (templateEntity.getMandatoryKey()) {
                case LAYOUT_ICON:
                    layoutId = DEFAULT_NATIVE_LAYOUT_ID_FOR_LAYOUT_ICON;
                    break;
                case LAYOUT_FEED:
                    layoutId = DEFAULT_NATIVE_LAYOUT_ID_FOR_LAYOUT_FEED;
                    break;
                case LAYOUT_STREAM:
                    layoutId = DEFAULT_NATIVE_LAYOUT_ID_FOR_LAYOUT_STREAM;
                    break;
                // default is already taken care of in the repository.
            }
        }
        nativeReqObj = new NativeReqObj(layoutId);
        // https://jira.corp.inmobi.com/browse/CAS-81
        nativeReqObj.setAdunit(DEFAULT_AD_UNIT_ID);
    }

    @Override
    public Native buildNative() {
        // if (null != templateEntity.getContentJson()) {
        // buildUsingContentJson();
        // } else {
        buildMandatory();
        buildNonMandatory();
        // }
        return new Native(nativeReqObj);
    }

 /* private void buildUsingContentJson() {
        buildImageAssets();
        buildTextAssets();
        buildOtherAssets();
    }
    
    private void buildImageAssets() {
        final NativeContentJsonObject nativeContentObject = templateEntity.getContentJson();
        for (final ImageAsset imageAsset : nativeContentObject.getImageAssets()) {
            final Dimension dimensions = imageAsset.getDimension();
            final Image image = new Image();
            image.setHmin(dimensions.getHeight());
            image.setWmin(dimensions.getWidth());
            final CommonAssetAttributes attributes = imageAsset.getCommonAttributes();
            switch (attributes.getAdContentAsset()) {
                case SCREENSHOT:
                    image.setType(Image.ImageAssetType.MAIN);
                    break;
                case ICON:
                    image.setType(Image.ImageAssetType.ICON);
                    break;
                default:
                    // Stray objects are ignored
                    continue;
            }
            nativeReqObj.addAsset(!attributes.isOptional(), image);
        }
    }
    
    private void buildTextAssets() {
        final NativeContentJsonObject nativeContentObject = templateEntity.getContentJson();
        for (final TextAsset textAsset : nativeContentObject.getTextAssets()) {
            final CommonAssetAttributes attributes = textAsset.getCommonAttributes();
            int maxChars = textAsset.getMaxChars();
            switch (attributes.getAdContentAsset()) {
                case TITLE:
                    maxChars = 0 == maxChars ? DEFAULT_TITLE_LENGTH : maxChars;
                    nativeReqObj.addAsset(!attributes.isOptional(), new Title(maxChars));
                    break;
                case DESCRIPTION:
                    final Data description = new Data(Data.DataAssetType.DESC);
                    description.setLen(0 == maxChars ? DEFAULT_MAX_DESC_LENGTH : maxChars);
                    nativeReqObj.addAsset(!attributes.isOptional(), description);
                    break;
                default:
                    // Stray objects are ignored
                    continue;
            }
        }
    }
    
    private void buildOtherAssets() {
        final NativeContentJsonObject nativeContentObject = templateEntity.getContentJson();
        for (final OtherAsset otherAsset : nativeContentObject.getOtherAssets()) {
            final CommonAssetAttributes attributes = otherAsset.getCommonAttributes();
            Data data;
            switch (attributes.getAdContentAsset()) {
                case CTA:
                    data = new Data(Data.DataAssetType.CTA_TEXT);
                    break;
                case STAR_RATING:
                    data = new Data(Data.DataAssetType.RATING);
                    break;
                default:
                    // Stray objects are ignored
                    continue;
            }
            // Making CTA, STAR_RATING as optional fields
            // nativeReqObj.addAsset(!attributes.isOptional(), data);
            nativeReqObj.addAsset(false, data);
        }
    }*/

    private void buildMandatory() {
        final List<Mandatory> mandatoryKeys = NativeConstraints.getMandatoryList(templateEntity.getMandatoryKey());
        for (final Mandatory mandatory : mandatoryKeys) {
            switch (mandatory) {
                case TITLE:
                    nativeReqObj.addAsset(true, new Title(DEFAULT_MAX_TITLE_LENGTH));
                    break;
                case ICON:
                    final Image icon = new Image();
                    icon.setType(Image.ImageAssetType.ICON);
                    icon.setWmin(ICON_DEFAULT_DIMENSION);
                    icon.setHmin(ICON_DEFAULT_DIMENSION);
                    nativeReqObj.addAsset(true, icon);
                    break;
                case DESCRIPTION:
                    final Data desc = new Data(Data.DataAssetType.DESC);
                    desc.setLen(DEFAULT_MAX_DESC_LENGTH);
                    nativeReqObj.addAsset(true, desc);
                    break;
                case SCREEN_SHOT:
                    final Image screen = NativeConstraints.getImage(templateEntity.getImageKey());
                    nativeReqObj.addAsset(true, screen);
                    break;
            }
        }
    }

    private void buildNonMandatory() {
        nativeReqObj.addAsset(false, new Data(Data.DataAssetType.CTA_TEXT));
        nativeReqObj.addAsset(false, new Data(Data.DataAssetType.DOWNLOADS));
        nativeReqObj.addAsset(false, new Data(Data.DataAssetType.RATING));
    }
}
