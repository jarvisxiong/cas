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
	public Context deserialize(JsonElement json, Type typeOf,JsonDeserializationContext context) throws JsonParseException {
		
		JsonObject jsonObj = json.getAsJsonObject();
		//String ar = jsonObj.get("ar").getAsString();
		int    w   = jsonObj.get("w").getAsInt();
		int    h   = jsonObj.get("h").getAsInt();
		String url = jsonObj.get("imageurl").getAsString();
		Screenshot.Builder b = Screenshot.newBuilder();
		b.setH(h);
		b.setW(w);
		double ar = (double)h/w;
		b.setAr(String.valueOf(ar));
		b.setUrl(url);
		
//		Screenshot screenShot = new Screenshot();
	    //screenShot.setAr(ar);
//	    screenShot.setW(w);
//	    screenShot.setH(h);
//	    screenShot.setUrl(url);
		return b.build();
	}

}
