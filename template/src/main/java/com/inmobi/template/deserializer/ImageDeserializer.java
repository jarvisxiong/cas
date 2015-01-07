package com.inmobi.template.deserializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.interfaces.Context;

public class ImageDeserializer implements JsonDeserializer<Context> {

    @Override
    public Context deserialize(final JsonElement json, final Type typeOf, final JsonDeserializationContext context)
            throws JsonParseException {

        final JsonObject jsonObj = json.getAsJsonObject();
        final int w = jsonObj.get("w").getAsInt();
        final int h = jsonObj.get("h").getAsInt();
        final String url = jsonObj.get("imageurl").getAsString();
        final double ar = (double) h / w;

        final Screenshot.Builder builder = Screenshot.newBuilder();
        builder.setH(h);
        builder.setW(w);
        builder.setAr(String.valueOf(ar));
        builder.setUrl(url);
        return builder.build();
    }

}
