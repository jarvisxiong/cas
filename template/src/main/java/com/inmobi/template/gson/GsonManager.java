package com.inmobi.template.gson;

import java.util.Set;

import org.reflections.Reflections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

public class GsonManager {
    private final Gson gson;

    @SuppressWarnings("rawtypes")
    @Inject
    public GsonManager() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Reflections reflections = new Reflections("com.inmobi.adserve.contracts");
        final Set<Class<?>> gsonContracts = reflections.getTypesAnnotatedWith(GsonContract.class);
        for (final Class gsonContract : gsonContracts) {
            gsonBuilder.registerTypeAdapter(gsonContract, new GsonContractDeserialiser());
        }
        gson = gsonBuilder.disableHtmlEscaping().create();
    }

    /**
     * 
     * @return
     */
    public Gson getGsonInstance() {
        return gson;
    }
}
