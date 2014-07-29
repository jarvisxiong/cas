package com.inmobi.template.config;

import lombok.Getter;

import com.inmobi.template.deserializer.AppDeserializer;
import com.inmobi.template.deserializer.DataDeserializer;
import com.inmobi.template.deserializer.IconDeserializer;
import com.inmobi.template.deserializer.ImageDeserializer;
import com.inmobi.template.interfaces.DeserializerConfiguration;

public class DefaultDeserializerConfiguration implements DeserializerConfiguration {
	
	@Getter 
	private AppDeserializer appDeserializer = new AppDeserializer();
	
	@Getter 
	private IconDeserializer iconDeserializer = new IconDeserializer();
	
	@Getter 
	private ImageDeserializer imageDeserializer = new ImageDeserializer();
	
	@Getter 
	private DataDeserializer dataDeserializer = new DataDeserializer();
	

}
