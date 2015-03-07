package com.inmobi.template.deserializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.inmobi.template.context.App;
import com.inmobi.template.context.DataMap;
import com.inmobi.template.context.Icon;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.interfaces.Context;

public class AppDeserializer implements JsonDeserializer<Context> {

    @Override
    public Context deserialize(final JsonElement json, final Type typeOf, final JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jsonObj = json.getAsJsonObject();

        final String title = getAttributeAsString("title", jsonObj);
        final String desc = getAttributeAsString("description", jsonObj);
        final String actionText = getAttributeAsString("actiontext", jsonObj);
        final String actionLink = getAttributeAsString("actionlink", jsonObj);
        final String id = getAttributeAsString("uid", jsonObj, "");

        final JsonElement imgElement = jsonObj.get("image");
        Screenshot imgs[] = null;
        if (imgElement != null) {
            imgs = new Screenshot[] {(Screenshot) context.deserialize(imgElement, Screenshot.class)};
        }

        final JsonElement dataElement = jsonObj.get("data");
        DataMap dataMap = null;
        if (dataElement != null) {
            dataMap = context.deserialize(dataElement, DataMap.class);
        }

        final JsonElement iconElement = jsonObj.get("iconurl");
        Icon icons[] = null;
        if (iconElement != null) {
            final String iconurl = iconElement.getAsString();
            final Icon.Builder icon = Icon.newBuilder();
            icon.setH(300);
            icon.setW(300);
            icon.setUrl(iconurl);
            icons = new Icon[] {(Icon) icon.build()};
        }

        final App.Builder app = App.newBuilder();
        app.setDesc(desc);
        app.setTitle(title);
        app.setOpeningLandingUrl(actionLink);
        app.setActionText(actionText);
        app.setId(id);
        app.setPixelUrls(getUrls(jsonObj.get("pixelurl")));
        app.setClickUrls(getUrls(jsonObj.get("clickurl")));
        if (icons != null) {
            app.setIcons(Arrays.asList(icons));
        }
        if (imgs != null) {
            app.setScreenshots(Arrays.asList(imgs));
        }
        if (dataMap != null) {
            app.setDownloads(dataMap.getDownloads());
            app.setRating(dataMap.getRating());
            app.setRating_count(dataMap.getRating_count());
        }
        return app.build();
    }

    private List<String> getUrls(final JsonElement jsonElement) {
        List<String> urlList = null;
        if (jsonElement != null) {
            final JsonArray array = jsonElement.getAsJsonArray();

            if (array != null) {
                urlList = Lists.newArrayList();
                final Iterator<JsonElement> itr = array.iterator();
                while (itr.hasNext()) {
                    final String url = itr.next().getAsString();
                    urlList.add(url);
                }
            }
        }
        return urlList;
    }

    private String getAttributeAsString(final String attr, final JsonObject jsonObj) {
        final JsonElement jsonElt = jsonObj.get(attr);
        return null != jsonElt ? jsonElt.getAsString() : null;
    }

    private String getAttributeAsString(final String attr, final JsonObject jsonObj, final String defaultValue) {
        final JsonElement jsonElt = jsonObj.get(attr);
        return null != jsonElt ? jsonElt.getAsString() : defaultValue;
    }

}
