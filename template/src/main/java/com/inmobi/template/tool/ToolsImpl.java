package com.inmobi.template.tool;

import static com.inmobi.template.context.KeyConstants.AD_UNIT_TRACKERS;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.inmobi.template.context.KeyConstants;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.Context;
import com.inmobi.template.interfaces.Tools;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;


public class ToolsImpl extends Tools {
    private final Gson gson;

    @Inject
    public ToolsImpl(final GsonManager gsonManager) {
        gson = gsonManager.getGsonInstance();
    }

    @Override
    public Object jpath(final Context context, final String key) {
        return context.get(key);
    }

    @Override
    public String jpathStr(final Context context, final String key) {
        return (String) jpath(context, key);
    }

    @Override
    public String jsonEncode(final Object map) {
        return gson.toJson(map);
    }

    @Override
    public String nativeAd(final Context context, final String pubContent) {
        return pubContent;
    }

    @Override
    public String getVastXMl(final Context context) {
        return jpathStr(context, KeyConstants.AD_VASTCONTENT);
    }

    @Override
    public Object getAdUnitTrackersJson(final Context context) {
        final String adUnitTrackersStr = jpathStr(context, AD_UNIT_TRACKERS);
        return evalJsonPath(adUnitTrackersStr, "adUnitTrackersList");
    }

    public Object evalJsonPath(final String json, final String jsonPathExpression) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        final JsonPath jsonPath = JsonPath.compile(jsonPathExpression);
        try {
            final Type mapType = new TypeToken<HashMap<String, Object>>() {}.getType();
            final Map<String, Object> jsonMap = gson.fromJson(json, mapType);
            return jsonPath.read(jsonMap);
        } catch (final Exception exp) {
            return null;
        }
    }

    public Object evalJsonPath(final Object jsonObject, final String jsonPathExpression) {
        final JsonPath jsonPath = JsonPath.compile(jsonPathExpression);
        try {
            return jsonPath.read(jsonObject);
        } catch (final InvalidPathException e) {
            return null;
        }
    }
}
