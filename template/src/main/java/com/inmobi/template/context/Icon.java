package com.inmobi.template.context;

import java.util.Map;

import com.inmobi.template.interfaces.Context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Icon extends AbstractContext {
    private final int w;
    private final int width;
    private final int height;
    private final int h;
    private final String ar;
    private final double aspectRatio;
    private final String url;

    private Icon(final Builder builder) {
        w = builder.w;
        h = builder.h;
        height = builder.h;
        width = builder.w;
        url = builder.url;
        aspectRatio = (double) w / (double) h;
        ar = String.valueOf(aspectRatio);
        setValues(params);
    }

    @Override
    void setValues(final Map<String, Object> params) {
        params.put(KeyConstants.WIDTH, w);
        params.put(KeyConstants.HEIGHT, h);
        params.put(KeyConstants.AR, ar);
        params.put(KeyConstants.WIDTH_FULL_TEXT, w);
        params.put(KeyConstants.HEIGHT_FULL_TEXT, h);
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
            return new Icon(this);
        }

    }

}
