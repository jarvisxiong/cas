package com.inmobi.template.interfaces;

import com.google.gson.JsonDeserializer;

public interface DeserializerConfiguration {

	public JsonDeserializer<Context> getAppDeserializer();

	public JsonDeserializer<Context> getIconDeserializer();

	public JsonDeserializer<Context> getImageDeserializer();

	public JsonDeserializer<Context> getDataDeserializer();


}
