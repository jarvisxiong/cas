package com.inmobi.adserve.channels.api.natives;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, PARAMETER})
public @interface IxNativeBuilderFactory {

}
