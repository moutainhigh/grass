package com.yanglinkui.grass.client;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.annotation.*;
import com.yanglinkui.grass.common.StringOptional;
import com.yanglinkui.grass.exception.GrassStateException;
import com.yanglinkui.grass.common.Utils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ClientBuilder<T> {

    protected Class<T> interfaceClass;

    protected T client;

    public synchronized T build() {
        if (this.client != null) {
            return this.client;
        }

        this.client = _build();

        return this.client;
    }

    private T _build() {
        ClientInvocationHandler handler = buildClientInvocationHandler();

        //代理
        T proxy = (T) Proxy.newProxyInstance(this.interfaceClass.getClassLoader(),
                new Class<?>[] {this.interfaceClass, Client.class}, handler);

        Map<Method, Invoker> defaultMethodHandlers = handler.getDefaultMethodHandlers();
        for (Invoker Invoker : defaultMethodHandlers.values()) {
            ((DefaultMethodInvoker) Invoker).bindTo(proxy);
        }

        return proxy;
    }

    protected ClientInvocationHandler buildClientInvocationHandler() {
        if (this.interfaceClass == null) {
            throw new IllegalStateException("The client interface cannot be null.");
        }

        GrassClient grassClient = this.interfaceClass.getAnnotation(GrassClient.class);
        String applicationName = Utils.getOrDefault(grassClient.application(), grassClient.value());
        String spi = grassClient.spi();
        String prefix = grassClient.prefix();
        String qualifier = StringOptional.of(grassClient.qualifier()).orElse(this.interfaceClass.getCanonicalName());

        if (Utils.isEmpty(spi) && Utils.isEmpty(applicationName)) {
            throw new GrassStateException("The application or spi of GrassClient should be specified by value or application or spi property.");
        }

        if (Utils.isNotEmpty(spi) && Utils.isNotEmpty(applicationName)) {
            throw new GrassStateException("The application and spi cannot be both specified.");
        }

        Class<?> spiRouter = grassClient.spiRouter();
        Class<?> loadBalance = grassClient.lb();
        Class<?> fallbackClass = grassClient.fallback();
        Class<?> fallbackFactoryClass = grassClient.fallbackFactory();

        Map<Method, Invoker> methodToHandler = new LinkedHashMap();
        Map<Method, DefaultMethodInvoker> defaultMethodHandlers = new LinkedHashMap<>();

        for (Method method : this.interfaceClass.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            } else {
                if (Utils.isDefaultMethod(method)) {
                    //执行接口默认方法
                    DefaultMethodInvoker handler = new DefaultMethodInvoker(method);
                    defaultMethodHandlers.put(method, handler);
                }

                GrassMethod grassMethod = AnnotationUtils.getAnnotation(method, GrassMethod.class);

                if (grassMethod == null) {
                    //没声明的方法，当执行的时候直接抛出异常
                    methodToHandler.put(method, new NotImplementMethodInvoker(method));
                } else {
                    //通过protocol获得Invoker
                    if (spiRouter != null && SpiRouter.class.isAssignableFrom(spiRouter)) {
                        throw new GrassStateException("The SpiRouter("
                                + spiRouter.getCanonicalName() +
                                ") must be implemented SpiRouter.class");
                    }
                    InvokerAttributes properties = ServiceUtils.getInvokerAttributes(prefix, method);
                    ClientInvoker invoker = new ClientInvoker(spi, applicationName, spiRouter, loadBalance, properties);
                    methodToHandler.put(method, invoker);
                }
            }
        }

        return new ClientInvocationHandler(qualifier, this.interfaceClass, methodToHandler,
                defaultMethodHandlers, fallbackClass, fallbackFactoryClass);
    }

    public Class<T> getInterface() {
        return this.interfaceClass;
    }

    public void setInterface(Class<T> interfaceClass) {
        if (interfaceClass == null || !interfaceClass.isInterface()) {
            throw new IllegalArgumentException("The client interface must be an interface class");
        }

        if (interfaceClass.getAnnotation(GrassClient.class) == null) {
            throw new IllegalArgumentException("The client interface must be annotated by GrassClient.");
        }

        if (this.client != null) {
            throw new IllegalStateException("The client interface cannot be changed after client was created");
        }

        this.interfaceClass = interfaceClass;
    }


}

