package com.yanglinkui.grass.spring;

import com.yanglinkui.grass.annotation.GrassMethod;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(GrassMethod.class)
public class GrassConfiguration {

}
