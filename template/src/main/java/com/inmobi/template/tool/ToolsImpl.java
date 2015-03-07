package com.inmobi.template.tool;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.Context;
import com.inmobi.template.interfaces.Tools;

public class ToolsImpl extends Tools {
    private final Gson gson;

    @Inject
    public ToolsImpl(GsonManager gsonManager) {
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
    public String nativeAd(final Context creativeContext, final String pubContent) {
        return pubContent;
    }

}
