package com.inmobi.template.deserializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.inmobi.template.context.Icon;
import com.inmobi.template.interfaces.Context;

public class IconDeserializer implements JsonDeserializer<Context> {

	@Override
	public Context deserialize(JsonElement json, Type typeOf,JsonDeserializationContext context) throws JsonParseException {
		
		JsonObject jsonObj = json.getAsJsonObject();
		String url = jsonObj.get("url").getAsString();
		int    w   = jsonObj.get("w").getAsInt();
		int    h   = jsonObj.get("h").getAsInt();
		
		Icon.Builder icon = Icon.newBuilder();
		icon.setH(h);
		icon.setW(w);
		icon.setUrl(url);
		
		return icon.build();
	}

}
