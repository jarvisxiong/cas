package com.inmobi.template.context;

import java.util.Map;

import com.inmobi.template.interfaces.Context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
public class DataMap extends AbstractContext {
    private final int downloads;
    private final String rating;
    private final int rating_count;

    private DataMap(final Builder builder) {
        downloads = builder.downloads;
        rating = builder.rating;
        rating_count = builder.rating_count;
        setValues(params);
    }

    @Override
    void setValues(final Map<String, Object> params) {
        params.put("downloads", downloads);
        params.put("rating", rating);
        params.put("rating_count", rating_count);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private int downloads;
        private String rating;
        private int rating_count;

        public Context build() {
            return new DataMap(this);
        }
    }

}
