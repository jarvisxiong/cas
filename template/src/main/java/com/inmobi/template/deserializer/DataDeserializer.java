package com.inmobi.template.deserializer;

import java.lang.reflect.Type;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.inmobi.template.context.DataMap;
import com.inmobi.template.context.DataMap.Builder;
import com.inmobi.template.interfaces.Context;

public class DataDeserializer implements JsonDeserializer<Context> {
	

	@Override
	public Context deserialize(JsonElement json, Type typeOf,
			JsonDeserializationContext arg2) throws JsonParseException {
		final JsonArray jsonArray = json.getAsJsonArray();//getAsJsonObject();
		Iterator<JsonElement> jsonItr = jsonArray.iterator();
		DataMap.Builder builder = DataMap.newBuilder();
		
		while(jsonItr.hasNext()){
			JsonObject jsonObject = jsonItr.next().getAsJsonObject();
			Integer seq = jsonObject.get("seq").getAsInt();
			String value = jsonObject.get("value").getAsString();
			setValue(seq, value, builder);
		}
		
		return builder.build();
	}
	
	
	private void setValue(int index, String value,Builder builder){
		switch(index){
		case 0:
			builder.setDownloads(Integer.valueOf(value));
			break;
		case 1:
			builder.setRating(value);
			break;
		case 2:
			builder.setRating_count(Integer.valueOf(value));
			break;
			default:
				throw new IllegalArgumentException("Index "+index+" is not defined");
			
		}
		
	}
	

}
