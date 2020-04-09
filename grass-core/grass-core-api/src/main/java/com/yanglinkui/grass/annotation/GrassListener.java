package com.yanglinkui.grass.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface GrassListener {

    public String application();

    public String value() default "";

    public String id() default "";

    public String domain() default "";

    public String action() default "";

    public  GrassEventBoundary when() default GrassEventBoundary.OnReturn;

    String serializer() default "";
}
