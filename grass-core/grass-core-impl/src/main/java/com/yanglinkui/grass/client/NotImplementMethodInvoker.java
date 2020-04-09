package com.yanglinkui.grass.client;

import com.yanglinkui.grass.Invoker;
import com.yanglinkui.grass.InvokerAttributes;
import com.yanglinkui.grass.exception.NotSupportException;

import java.lang.reflect.Method;

public class NotImplementMethodInvoker implements Invoker {

    private final String methodName;

    private final Method method;

    private final String clazzName;

    public NotImplementMethodInvoker(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("method cannot be null");
        }

        this.methodName = method.getName();
        this.method = method;
        this.clazzName = method.getDeclaringClass().getSimpleName();
    }

    @Override
    public Object invoke(Object[] argv) {
        throw new NotSupportException("The " + this.clazzName + "'s " + methodName + " has no default implement");
    }

    @Override
    public InvokerAttributes getAttributes() {
        return null;
    }
}
