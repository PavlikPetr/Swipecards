package com.topface.topface.utils.debug;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Этой аннотацией помечаем костыли
 * Created by tiberal on 20.05.16.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD
        , ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE})
public @interface FuckingVoodooMagic {
    String description() default "";
}
