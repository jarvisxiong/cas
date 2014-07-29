package com.inmobi.template.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.inject.Inject;
import com.inmobi.template.context.App;
import com.inmobi.template.context.DataMap;
import com.inmobi.template.context.Icon;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.interfaces.Context;
import com.inmobi.template.interfaces.DeserializerConfiguration;

public class GsonManager {
	
	private GsonBuilder gsonBuilder = new GsonBuilder();
	
	@Inject
	public void setDeserializer(DeserializerConfiguration dc){
		 gsonBuilder.registerTypeAdapter(App.class, dc.getAppDeserializer());
		 gsonBuilder.registerTypeAdapter(Icon.class, dc.getIconDeserializer());
		 gsonBuilder.registerTypeAdapter(Screenshot.class, dc.getImageDeserializer());
		 gsonBuilder.registerTypeAdapter(DataMap.class, dc.getDataDeserializer());
	}
	
	public void setDeserializer(Class<Context> clazz,JsonDeserializer<Context> deserializer){
		gsonBuilder.registerTypeAdapter(clazz, deserializer);
	}
	
	public Gson createGson(){
		return gsonBuilder.create();
	}

}
