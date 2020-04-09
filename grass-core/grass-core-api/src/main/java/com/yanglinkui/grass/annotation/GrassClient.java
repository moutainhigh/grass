package com.yanglinkui.grass.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface GrassClient {

    String value() default "";

    String application() default "";

    String spi() default "";

    String prefix() default "";

    /**
     * SPI spiRouter, 需要实现SpiRouter接口
     * @return
     */
    Class<?> spiRouter() default void.class;

    Class<?> lb() default void.class;

    String qualifier() default "";

    /**
     * fallback机制，必须继承当前的接口，保证方法签名一致
     */
    Class<?> fallback() default void.class;

    /**
     * 定义一个fallback的工厂类（不是实现 FallbackFactory, 返回值必须是被当前annotation注释的相同接口的实现类)，一般产生动态代理类（proxy）
     * @return
     */
    Class<?> fallbackFactory() default void.class;

}
