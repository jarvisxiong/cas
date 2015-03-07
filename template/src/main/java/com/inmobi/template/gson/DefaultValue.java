package com.inmobi.template.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ishanbhatnagar on 13/2/15.
 */
// Overriden by @Required
// Can only be used to enforce integer default values
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultValue {
    int value();
}
