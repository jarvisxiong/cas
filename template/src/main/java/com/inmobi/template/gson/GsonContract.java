package com.inmobi.template.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to invoke custom deserialiser with support for @Required and @DefaultValue during gson deserialisation
 * @author Ishan Bhatnagar
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GsonContract {
}
