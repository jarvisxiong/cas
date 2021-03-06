package com.inmobi.template.context;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@ToString
// TODO: Use lombok builder for this
public final class App extends AbstractContext {
    private final String title;
    private final String desc;
    private final String vastContent;
    private final List<Icon> icons;
    private final List<Screenshot> screenshots;
    private final String id;
    private final String openingLandingUrl;
    private final String rating;
    private final String actionText;
    private final int rating_count;
    private final int downloads;
    private final List<String> pixelUrls;
    private final List<String> clickUrls;
    @Setter
    private String adImpressionId;

    private App(final Builder builder) {
        title = builder.title;
        desc = builder.desc;
        vastContent = builder.vastContent;
        icons = builder.icons;
        screenshots = builder.screenshots;
        id = builder.id;
        rating = builder.rating;
        rating_count = builder.rating_count;
        downloads = builder.downloads;
        openingLandingUrl = builder.openingLandingUrl;
        pixelUrls = builder.pixelUrls;
        clickUrls = builder.clickUrls;
        adImpressionId = builder.adImpressionId;
        actionText = builder.actionText;

        setValues(params);
    }

    // Supports a superset of
    // https://github.corp.inmobi.com/ci/publisher-core/blob/master/src/main/java/com/inmobi/publisher/core/constant/enums/NativeAdContentAsset.java
    @Override
    public void setValues(final Map<String, Object> params) {
        params.put(KeyConstants.APP_ICONS, icons);
        params.put(KeyConstants.APP_DESC, desc);
        params.put(KeyConstants.APP_SCREENSHOTS, screenshots);
        params.put(KeyConstants.APP_ID, id);
        params.put(KeyConstants.APP_TITLE, title);
        params.put(KeyConstants.APP_RATING, rating);
        params.put(KeyConstants.APP_RATING_COUNT, rating_count);
        params.put(KeyConstants.APP_DOWNLOADS, downloads);

        params.put(KeyConstants.AD_ICON, icons);
        params.put(KeyConstants.AD_TITLE, title);
        params.put(KeyConstants.AD_VASTCONTENT, vastContent);

        params.put(KeyConstants.ICON, icons);
        params.put(KeyConstants.IMAGE, screenshots);

        params.put(KeyConstants.IMNATIVE_APP_CREATIVE, this);
        params.put(KeyConstants.IMNATIVE_CREATIVE_ICON, icons);
        params.put(KeyConstants.IMNATIVE_IMAGE, screenshots);

        params.put(KeyConstants.IMNATIVE_CREATIVE_HEADLINE_BEAN, new CreativeBean("text", title));
        params.put(KeyConstants.IMNATIVE_CREATIVE_HEADLINE_TEXT, title);

        params.put(KeyConstants.IMNATIVE_CREATIVE_DESC_BEAN, new CreativeBean("text", desc));
        params.put(KeyConstants.IMNATIVE_CREATIVE_DESC_TEXT, desc);

        params.put(KeyConstants.IMNATIVE_ACTION_BEAN, new CreativeBean("text", actionText));
        params.put(KeyConstants.IMNATIVE_ACTION_TEXT, actionText);

        params.put(KeyConstants.IMNATIVE_CREATIVE_SOCIAL_RATING_BEAN, new CreativeBean("rating", rating));
        params.put(KeyConstants.IMNATIVE_CREATIVE_SOCIAL_RATING, rating);
    }


    public static Builder newBuilder() {
        return new Builder();
    }


    public static class Builder {
        @Setter
        private String title;
        @Setter
        private String desc;
        @Setter
        private String vastContent;
        @Setter
        private List<Icon> icons;
        @Setter
        private List<Screenshot> screenshots;
        @Setter
        private String id;
        @Setter
        private String openingLandingUrl;
        @Setter
        private String rating;
        @Setter
        private String actionText;
        @Setter
        private int rating_count;
        @Setter
        private int downloads;
        @Setter
        private List<String> pixelUrls;
        @Setter
        private List<String> clickUrls;
        @Setter
        private String adImpressionId;

        public App build() {
            return new App(this);
        }
    }

}
