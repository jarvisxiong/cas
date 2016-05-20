package com.inmobi.template.context;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;


public class MovieBoardAdData extends AbstractContext {
    private final String vastContent;
    @Getter
    private final boolean isVastXml;
    @Getter
    private final int parentViewWidth;
    @Getter
    private final List<CreativeObject> creativeList;
    @Getter
    private final NoJsObject noJsObject;
    @Getter
    private final RequestJsonObject requestJson;

    private MovieBoardAdData(final Builder builder) {
        vastContent = builder.vastContent;
        isVastXml = builder.isVastXml;
        parentViewWidth = builder.parentViewWidth;
        creativeList = builder.creativeObjectList;
        noJsObject = builder.noJsObject;
        requestJson = builder.requestJsonObject;

        setValues(params);
    }

    @Override
    public void setValues(final Map<String, Object> params) {
        params.put(KeyConstants.AD_VAST_XML, isVastXml);
        params.put(KeyConstants.AD_MOVIEBOARD, true);
        params.put(KeyConstants.AD_VASTCONTENT, vastContent);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        @Setter
        private String vastContent;
        @Setter
        private boolean isVastXml;
        @Setter
        private int parentViewWidth;
        @Setter
        private List<CreativeObject> creativeObjectList;
        @Setter
        private NoJsObject noJsObject;
        @Setter
        private RequestJsonObject requestJsonObject;

        public MovieBoardAdData build() {
            return new MovieBoardAdData(this);
        }
    }
}
