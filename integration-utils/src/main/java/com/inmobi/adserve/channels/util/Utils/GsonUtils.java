package com.inmobi.adserve.channels.util.Utils;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Created by ishanbhatnagar on 10/2/15.
 */
public class GsonUtils {
    /**
     * Boiler Plate code for getting an attribute from a jsonObject
     * 
     * @param attr
     * @param jsonObj
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAttribute(final Class<T> clazz, final String attr, final JsonObject jsonObj,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonElement jsonElt = jsonObj.get(attr);
        if (null != jsonElt) {
            return (T) context.deserialize(jsonElt, clazz);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAttribute(final Class<T> clazz, final String attr, final JsonObject jsonObj,
            final JsonDeserializationContext context, final T defaultValue) throws JsonParseException {
        final JsonElement jsonElt = jsonObj.get(attr);
        if (null != jsonElt) {
            return (T) context.deserialize(jsonElt, clazz);
        } else {
            return defaultValue;
        }
    }

    /**
     * Boiler Plate code for getting an list of attribute from a jsonObject
     * 
     * @param attr
     * @param jsonObj
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getListAttribute(final Class<T> clazz, final String attr, final JsonObject jsonObj,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonArray jsonArray = jsonObj.getAsJsonArray(attr);

        if (null != jsonArray) {
            final List<T> list = new ArrayList<T>();
            for (final JsonElement jsonElt : jsonArray) {
                list.add((T) context.deserialize(jsonElt, clazz));
            }
            return list;
        } else {
            return null;
        }
    }
}
