package com.inmobi.template.context;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.inmobi.template.interfaces.Context;


@ToString
@Getter
public final class Screenshot extends AbstractContext {
    private final int w;
    private final int h;
    private final int width;
    private final int height;
    private final String ar;
    private final double aspectRatio;
    private final String url;

    private Screenshot(final Builder builder) {
        w = builder.w;
        h = builder.h;
        width = w;
        height = h;
        url = builder.url;
        aspectRatio = (double) w / (double) h;
        ar = String.valueOf(aspectRatio);
        setValues(params);
    }

    @Override
    public void setValues(final Map<String, Object> params) {
        params.put(KeyConstants.WIDTH, w);
        params.put(KeyConstants.HEIGHT, h);
        params.put(KeyConstants.WIDTH_FULL_TEXT, w);
        params.put(KeyConstants.HEIGHT_FULL_TEXT, h);
        params.put(KeyConstants.AR, ar);
        params.put(KeyConstants.AR_FULL_TEXT, aspectRatio);
        params.put(KeyConstants.URL, url);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private int w;
        private int h;
        private String url;

        public Context build() {
            return new Screenshot(this);
        }
    }

}
