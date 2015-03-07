package com.inmobi.template.interfaces;

import com.google.gson.JsonDeserializer;

public interface GsonDeserializerConfiguration {
    /**
     * 
     * @return
     */
    JsonDeserializer<Context> getAppDeserializer();

    /**
     * 
     * @return
     */
    JsonDeserializer<Context> getIconDeserializer();

    /**
     * 
     * @return
     */
    JsonDeserializer<Context> getImageDeserializer();

    /**
     * 
     * @return
     */
    JsonDeserializer<Context> getDataDeserializer();
}
