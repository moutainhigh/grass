package com.yanglinkui.grass.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface GrassEvent {

    public String value() default "";

    public String id() default "";

    public  GrassEventBoundary when() default GrassEventBoundary.OnReturn;

}
