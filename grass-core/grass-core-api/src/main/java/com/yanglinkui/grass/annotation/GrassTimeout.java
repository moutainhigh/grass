package com.yanglinkui.grass.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface GrassTimeout {
    public long value();
}
