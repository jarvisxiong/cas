package com.inmobi.template.gson;

import java.util.Set;

import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.inmobi.template.context.App;
import com.inmobi.template.context.DataMap;
import com.inmobi.template.context.Icon;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.interfaces.GsonDeserializerConfiguration;

public class GsonManager {
    private final Gson gson;

    @Inject
    public GsonManager(final GsonDeserializerConfiguration dc) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(App.class, dc.getAppDeserializer());
        gsonBuilder.registerTypeAdapter(Icon.class, dc.getIconDeserializer());
        gsonBuilder.registerTypeAdapter(Screenshot.class, dc.getImageDeserializer());
        gsonBuilder.registerTypeAdapter(DataMap.class, dc.getDataDeserializer());

        Reflections reflections = new Reflections("com.inmobi.adserve.contracts");
        Set<Class<?>> gsonContracts = reflections.getTypesAnnotatedWith(GsonContract.class);
        for (Class gsonContract : gsonContracts) {
            gsonBuilder.registerTypeAdapter(gsonContract, new GsonContractDeserialiser());
        }
        // gson = gsonBuilder.disableHtmlEscaping().create();
        gson = gsonBuilder.create();
    }

    public Gson getGsonInstance() {
        return gson;
    }
}
