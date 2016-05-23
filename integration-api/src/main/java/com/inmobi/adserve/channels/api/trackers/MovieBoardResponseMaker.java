package com.inmobi.adserve.channels.api.trackers;

import static com.inmobi.adserve.contracts.common.response.nativead.DefaultResponses.DEFAULT_CTA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.template.context.AParamsObject;
import com.inmobi.template.context.AdObject;
import com.inmobi.template.context.CreativeObject;
import com.inmobi.template.context.CtaObject;
import com.inmobi.template.context.JsonObject;
import com.inmobi.template.context.MovieBoardAdData;
import com.inmobi.template.context.RequestJsonObject;
import com.inmobi.template.exception.TemplateException;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.interfaces.TemplateConfiguration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MovieBoardResponseMaker {
    private static final String NAMESPACE_PARAM = "NAMESPACE";
    public static final String TEMPLATE_ID_PARAM = "templateId";
    private final TemplateParser templateParser;
    private final Gson gson;

    @Inject
    public MovieBoardResponseMaker(final TemplateParser tp, final TemplateConfiguration tc) throws TemplateException {
        gson = tc.getGsonManager().getGsonInstance();
        this.templateParser = tp;
    }

    public String makeIXMovieBoardVideoResponse(final MovieBoardAdData app, final Map<String, String> params)
            throws Exception {
        final VelocityContext context = new VelocityContext();
        context.put(NAMESPACE_PARAM, Formatter.getIXNamespace());
        return createMovieBoardAd(context, app, params);
    }

    private String createMovieBoardAd(final VelocityContext vc, final MovieBoardAdData app,
            final Map<String, String> params) throws Exception {
        log.debug("Generating Movie Board Response");

        final String templateId = params.get(TEMPLATE_ID_PARAM);
        final String pubContent = templateParser.format(app, templateId);

        final MovieBoardAd movieBoardAd = gson.fromJson(pubContent, MovieBoardAd.class);

        final String namespace = (String) vc.get(NAMESPACE_PARAM);
        movieBoardAd.setNamespace(namespace);
        final String movieBoardResponseJSON = gson.toJson(movieBoardAd);
        log.debug("Movieboard response generated: {}", movieBoardResponseJSON);
        return movieBoardResponseJSON;
    }

    public RequestJsonObject getRequestJsonForMovieBoardTemplate(final Integer parentViewWidth) {
        return new RequestJsonObject(new AParamsObject(parentViewWidth));
    }

    public List<CreativeObject> getCreativeListForMovieBoardTemplate() {
        final JsonObject jsonObject = new JsonObject(new AdObject(new CtaObject(DEFAULT_CTA)));
        final String jsonObjectString = gson.toJson(jsonObject);

        return new ArrayList<>(Collections.singletonList(new CreativeObject(jsonObjectString)));
    }

    @Data
    private static class MovieBoardAd {
        private final String pubContent;
        private String namespace;
    }
}
