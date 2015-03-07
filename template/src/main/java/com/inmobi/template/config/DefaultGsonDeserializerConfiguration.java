package com.inmobi.template.config;

import com.inmobi.template.deserializer.AppDeserializer;
import com.inmobi.template.deserializer.DataDeserializer;
import com.inmobi.template.deserializer.IconDeserializer;
import com.inmobi.template.deserializer.ImageDeserializer;
import com.inmobi.template.interfaces.GsonDeserializerConfiguration;

import lombok.Getter;

@Getter
public class DefaultGsonDeserializerConfiguration implements GsonDeserializerConfiguration {
    private final AppDeserializer appDeserializer = new AppDeserializer();
    private final IconDeserializer iconDeserializer = new IconDeserializer();
    private final ImageDeserializer imageDeserializer = new ImageDeserializer();
    private final DataDeserializer dataDeserializer = new DataDeserializer();
}
