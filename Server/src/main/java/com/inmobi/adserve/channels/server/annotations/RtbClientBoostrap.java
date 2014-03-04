package com.inmobi.adserve.channels.server.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;


/**
 * @author abhishek.parwal
 * 
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD, METHOD, PARAMETER })
public @interface RtbClientBoostrap {

}