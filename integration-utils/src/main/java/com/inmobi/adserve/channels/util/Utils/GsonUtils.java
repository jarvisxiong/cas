package com.inmobi.adserve.channels.util.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
     * @param attr
     * @param jsonObj
     * @param <T>
     * @return
     */
    public static <T> T getAttribute(Class<T> clazz, final String attr, final JsonObject jsonObj,
                                            JsonDeserializationContext context) throws JsonParseException {
        JsonElement jsonElt = jsonObj.get(attr);
        if (null != jsonElt) {
            return (T)context.deserialize(jsonElt, clazz);
        } else {
            return null;
        }
    }

    public static <T> T getAttribute(Class<T> clazz, final String attr, final JsonObject jsonObj,
                                     JsonDeserializationContext context, T defaultValue) throws JsonParseException {
        JsonElement jsonElt = jsonObj.get(attr);
        if (null != jsonElt) {
            return (T)context.deserialize(jsonElt, clazz);
        } else {
            return defaultValue;
        }
    }

    /**
     * Boiler Plate code for getting an list of attribute from a jsonObject
     * @param attr
     * @param jsonObj
     * @param <T>
     * @return
     */
    public static <T> List<T> getListAttribute(Class<T> clazz, final String attr, final JsonObject jsonObj,
                                                      JsonDeserializationContext context) throws JsonParseException {
        JsonArray jsonArray = jsonObj.getAsJsonArray(attr);

        if (null != jsonArray) {
            List<T> list = new ArrayList<T>();
            for (JsonElement jsonElt : jsonArray) {
                list.add((T)context.deserialize(jsonElt, clazz));
            }
            return list;
        } else {
            return null;
        }
    }
}
