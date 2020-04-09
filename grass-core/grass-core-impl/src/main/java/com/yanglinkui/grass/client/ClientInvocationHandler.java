package com.yanglinkui.grass.client;

import ch.qos.logback.core.spi.ContextAware;
import com.yanglinkui.grass.GrassContext;
import com.yanglinkui.grass.GrassContextManager;
import com.yanglinkui.grass.Invoker;
import com.yanglinkui.grass.common.Utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ClientInvocationHandler<T> implements InvocationHandler {

    private final Map<Method, Invoker> dispatch;

    private final Map<Method, Invoker> defaultMethodHandlers;

    private final Class<?> interfaceClass;

    private final Class<?> fallbackClass;

    private final Class<?> fallbackFactoryClass;

    private final AtomicReference<FallbackFactory<T>> fallbackFactoryHolder = new AtomicReference<>();

    private final String id;

    public ClientInvocationHandler(String id, Class<?> interfaceClass, Map<Method, Invoker> dispatch, Map<Method, Invoker> defaultMethodHandlers, Class<?> fallbackClass, Class<?> fallbackFactoryClass) {
        this.id = id;
        this.interfaceClass = interfaceClass;
        this.fallbackClass = fallbackClass;
        this.fallbackFactoryClass = fallbackFactoryClass;
        this.dispatch = Utils.checkNotNull(dispatch, "dispatch");
        this.defaultMethodHandlers = defaultMethodHandlers == null ? new LinkedHashMap<>() : defaultMethodHandlers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        boolean isAsync = false; //是否异步

        if ("async".equals(methodName)) {
            if (args == null || args.length == 0) {
                throw new IllegalArgumentException("No method is specified");
            }

            //改变当前方法为目标方法
            method = (Method) args[0];
            methodName = method.getName();

            //重构参数
            Object[] args1 = new Object[args.length - 1];
            for (int i = 0; i < args1.length; i++) {
                args1[i] = args[i + 1];
            }

            args = args1;
            isAsync = true;
        }

        //一般方法
        if ("equals".equals(methodName)) {
            try {
                Object otherHandler =
                        args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                return equals(otherHandler);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else if ("hashCode".equals(methodName)) {
            return hashCode();
        } else if ("toString".equals(methodName)) {
            return toString();
        } else if ("getInvokerList".equals(methodName)) {
            return getInvokerList();
        } else if ("getInterface".equals(methodName)) {
            return this.interfaceClass;
        } else if ("getId".equals((methodName))) {
            return this.id;
        }

        //其他方法
        try {
            Invoker invoker = this.dispatch.get(method);
            //TODO: 没想好怎么设置一个优雅的异步判断
            if (invoker instanceof ClientInvoker) {
                return ((ClientInvoker) invoker).invoke(args, isAsync);
            } else {
                return invoker.invoke(args);
            }
        } catch (Throwable e) {
            //执行fallback
            FallbackFactory<T> fallbackFactory = getFallbackFactory();
            if (fallbackFactory != null) {
                T fallback = fallbackFactory.create(e);
                return method.invoke(fallback, args);
            }

            //默认方法当做fallback
            Invoker defaultMethodHandler = this.defaultMethodHandlers.get(method);
            if (defaultMethodHandler != null) {
                return defaultMethodHandler.invoke(args);
            }

            throw e;
        }
    }


    T getFallback(Class<?> fallback, GrassContext context) {
        if (fallback != void.class && fallback != null) {
            if (!this.interfaceClass.isAssignableFrom(fallback)) {
                throw new IllegalStateException(String.format(
                        "The fallback(%s)'s must be implemented interface(%s).class",
                        fallback.getCanonicalName(), this.interfaceClass.getCanonicalName()));
            }

            return Optional.ofNullable((T) context.getInstance(fallback)).orElseThrow(
                    () -> new IllegalStateException(String.format(
                            "No fallback instance of type %s found for remote client %s",
                            fallback.getCanonicalName(), this.interfaceClass.getCanonicalName()))
            );
        }

        return null;

    }

    List<Invoker> getInvokerList() {
        return this.dispatch.values().stream().collect(Collectors.toList());
    }

    FallbackFactory<T> getFallbackFactory(Class<?> fallbackFactory, GrassContext context) {
        if (fallbackFactory != void.class && fallbackFactory != null) {
            if (!FallbackFactory.class.isAssignableFrom(fallbackFactory)) {
                throw new IllegalStateException(String.format(
                        "The fallback factory(%s) must be implemented interface(%s).class",
                        fallbackFactory.getCanonicalName(), FallbackFactory.class.getCanonicalName()));
            }

            return Optional.ofNullable((FallbackFactory<T>) context.getInstance(fallbackFactory)).orElseThrow(
                    () -> new IllegalStateException(String.format(
                            "No fallback factory of type %s found for remote client %s",
                            fallbackFactory.getCanonicalName(), this.interfaceClass.getCanonicalName()))
            );
        }

        return null;
    }

    private FallbackFactory<T> getFallbackFactory() {
        if (this.fallbackFactoryHolder.get() == null) {
            T fallback = getFallback(fallbackClass, GrassContextManager.getContext());
            FallbackFactory<T> fallbackFactory = getFallbackFactory(fallbackFactoryClass, GrassContextManager.getContext());
            fallbackFactory = fallback != null ? new FallbackFactory.Default<T>(fallback) : fallbackFactory;
            this.fallbackFactoryHolder.compareAndSet(null, fallbackFactory);
        }

        return this.fallbackFactoryHolder.get();
    }

    public Map<Method, Invoker> getDispatch() {
        return dispatch;
    }

    public Map<Method, Invoker> getDefaultMethodHandlers() {
        return defaultMethodHandlers;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public Class<?> getFallbackClass() {
        return fallbackClass;
    }

    public Class<?> getFallbackFactoryClass() {
        return fallbackFactoryClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientInvocationHandler) {
            ClientInvocationHandler that = (ClientInvocationHandler) obj;
            return this.dispatch.equals(that.dispatch);
        }
        return false;
    }

    @Override
    public String toString() {
        return "This is a grass client of " + this.interfaceClass.getCanonicalName();
    }

}
