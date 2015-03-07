package com.inmobi.template.gson;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Custom Deserialiser for classes annotated with @GsonContract. This deserialiser enforces @Required and @DefaultValue.
 * 
 * @author Ishan Bhatnagar
 */
@SuppressWarnings("restriction")
public class GsonContractDeserialiser<T> implements JsonDeserializer<T> {
    @Getter
    @RequiredArgsConstructor
    private class Pair<K, V> {
        private final K key;
        private final V value;
    }

    private static final Gson gson = new Gson();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public T deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final T pojo = gson.fromJson(json, typeOfT);

        final List<Pair<Object, Field>> fields = new ArrayList<>();
        for (final Field field : pojo.getClass().getDeclaredFields()) {
            fields.add(new Pair<Object, Field>(pojo, field));
        }

        final ListIterator<Pair<Object, Field>> iterator = fields.listIterator();
        while (iterator.hasNext()) {
            final Pair<Object, Field> pair = iterator.next();
            final Field field = pair.getValue();
            final Object object = pair.getKey();
            Type argumentType = field.getGenericType();
            if (argumentType instanceof ParameterizedType) {
                argumentType = ((ParameterizedTypeImpl) argumentType).getActualTypeArguments()[0];
            }
            field.setAccessible(true);

            if (((Class) argumentType).isAnnotationPresent(GsonContract.class)) {
                try {
                    final Object instance = field.get(object);
                    if (null != instance) {
                        if (instance instanceof List) {
                            for (int i = 0; i < ((List) instance).size(); ++i) {
                                for (final Field fieldField : Arrays.asList(((Class) argumentType).getDeclaredFields())) {
                                    iterator.add(new Pair<>(((List) instance).get(i), fieldField));
                                    iterator.previous();
                                }
                            }
                        } else {
                            for (final Field fieldField : Arrays.asList(((Class) argumentType).getDeclaredFields())) {
                                iterator.add(new Pair<>(instance, fieldField));
                                iterator.previous();
                            }
                        }
                    }
                } catch (final IllegalAccessException e) {
                    // This should never be thrown
                    throw new JsonParseException(e);
                }
            }

            // @Required overrides behavior of @DefaultValue
            if (field.isAnnotationPresent(Required.class)) {
                try {
                    if (null == field.get(object)) {
                        throw new JsonParseException("Missing required field: "
                                + field.getDeclaringClass().getSimpleName() + "." + field.getName());
                    }
                } catch (final IllegalAccessException e) {
                    // This should never be thrown
                    throw new JsonParseException(e);
                }
            } else if (field.isAnnotationPresent(DefaultValue.class)) {
                try {
                    if (null == field.get(object)) {
                        field.set(object, field.getAnnotation(DefaultValue.class).value());
                    }
                } catch (final IllegalAccessException e) {
                    // This should never be thrown
                    throw new JsonParseException(e);
                }
            }
        }
        return pojo;
    }
}
