package com.yanglinkui.grass.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface GrassService {

    public String value() default "";

    public String prefix() default "";
}
