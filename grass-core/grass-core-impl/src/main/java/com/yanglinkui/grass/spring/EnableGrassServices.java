package com.yanglinkui.grass.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({GrassServicesRegister.class, GrassServiceExporter.class})
public @interface EnableGrassServices {
    String[] value() default {};

    public String spi() default "";

    public String protocol() default "message";

    public String serializer() default "json";

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};
}
