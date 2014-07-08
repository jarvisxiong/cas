package com.inmobi.template.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.inmobi.template.context.App;
import com.inmobi.template.context.DataMap;
import com.inmobi.template.context.Icon;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.deserializer.AppDeserializer;
import com.inmobi.template.deserializer.DataDeserializer;
import com.inmobi.template.deserializer.IconDeserializer;
import com.inmobi.template.deserializer.ImageDeserializer;
import com.inmobi.template.interfaces.Context;

public class GsonManager {
	
	private GsonBuilder gsonBuilder = new GsonBuilder();
	
	public GsonManager(){
	    gsonBuilder.registerTypeAdapter(App.class, new AppDeserializer());
	    gsonBuilder.registerTypeAdapter(Icon.class, new IconDeserializer());
	    gsonBuilder.registerTypeAdapter(Screenshot.class, new ImageDeserializer());
	    gsonBuilder.registerTypeAdapter(DataMap.class, new DataDeserializer());
	}
	
	public void setDeserializer(Class<Context> clazz,JsonDeserializer<Context> deserializer){
		gsonBuilder.registerTypeAdapter(clazz, deserializer);
	}
	
	public Gson createGson(){
		return gsonBuilder.create();
	}

}
