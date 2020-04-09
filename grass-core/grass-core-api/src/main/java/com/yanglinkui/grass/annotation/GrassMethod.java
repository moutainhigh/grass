package com.yanglinkui.grass.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface GrassMethod {

    public String value() default "";

    public String id() default "";

    public String domain() default "";

    public String action() default "";

    public String version() default "";

    public String description() default "";

}
