package com.inmobi.adserve.channels.server.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Target;


/**
 * @author abhishek.parwal
 * 
 */
@Target({ FIELD, METHOD, PARAMETER })
public @interface LoggerConfiguration {

}
