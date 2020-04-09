package com.yanglinkui.grass.spring;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.annotation.GrassMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class GrassServiceExporter implements BeanPostProcessor, InitializingBean, ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(GrassServiceExporter.class);

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        GrassContext context = this.applicationContext.getBean(GrassContext.class);
        String applicationName = this.applicationContext.getApplicationName();
        String zone = System.getenv(Environment.SERVICE_ZONE_KEY);

        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            GrassMethod grassMethod = AnnotationUtils.getAnnotation(method, GrassMethod.class);
            if (grassMethod == null) {
                continue;
            }

            String version = grassMethod.version();

//            Protocol protocol = context.getProtocol(context.getDefaultProtocol());
//            Assert.isTrue(protocol != null, context.getDefaultProtocol() + " protocol cannot be null");
//
//            String serviceId = protocol.getServiceId(grassMethod.id() != null ? grassMethod.id() : grassMethod.value(), zone, applicationName, bean.getClass(), method);
//
//            Serializer serializer = context.getSerializer(context.getDefaultSerializer());
//            Assert.isTrue(serializer != null,context.getDefaultProtocol() + " serializer cannot be null");
//
//            if (logger.isInfoEnabled()) {
//                logger.info("Exported {} 's method[{}] is exported as a service [id={}, protocol={}, serializer={}].", bean.getClass(), method.getName(), serviceId, protocol.getId(), serializer.getId());
//            }
//
//            Invoker invoker = new GrassServiceInvoker(serviceId, zone, applicationName, bean, method, version, serializer, null);
//            ServiceDescription serviceDescription = protocol.export(invoker);
//
//            context.getRegistry(context.getRegistry()).register(serviceDescription);
        }

        return bean;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
