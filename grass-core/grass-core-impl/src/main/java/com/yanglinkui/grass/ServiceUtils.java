package com.yanglinkui.grass;

import com.yanglinkui.grass.annotation.GrassMethod;
import com.yanglinkui.grass.annotation.GrassParam;
import com.yanglinkui.grass.annotation.GrassTimeout;
import com.yanglinkui.grass.common.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceUtils {

    static String getInvokerId(String prefix, GrassMethod grassMethod, String methodName) {
        Utils.checkNotNull(grassMethod, "service can not be null.");
        Utils.checkNotNull(methodName, "methodName can not be null.");

        String id = Utils.getOrDefault(grassMethod.id(), grassMethod.domain());
        id = Utils.getOrDefault(id, grassMethod.value());
        id = Utils.getOrDefault(id, methodName);

        if (Utils.isNotEmpty(prefix)) {
            id = prefix + "." + id;
        }
        return id;
    }



    public static InvokerAttributes getInvokerAttributes(String prefix, Method method) {
        Utils.checkNotNull(method, "Method cannot be null.");

        GrassMethod grassMethod = method.getAnnotation(GrassMethod.class);
        if (grassMethod == null) {
            throw new IllegalArgumentException("The method is not annotated by @GrassMethod");
        }

        GrassTimeout grassTimeout = method.getAnnotation(GrassTimeout.class);
        long timeout = grassTimeout == null ? 0 : grassTimeout.value();

        String serviceId = getInvokerId(prefix, grassMethod, method.getName());

        //判断是否参数化
        ParameterAttribute[] parameterAttributes = getParameterAttributes(method);
        Type returnType = method.getGenericReturnType();
        Class returnTypeClass = method.getReturnType();

        return new InvokerAttributes(serviceId, grassMethod.action(), grassMethod.version(), timeout, parameterAttributes, returnType, returnTypeClass);

    }

    static ParameterAttribute[] getParameterAttributes(Method method) {
        //判断是否参数化
        Parameter[] parameters = method.getParameters();
        ParameterAttribute[] parameterAttributes = new ParameterAttribute[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String name = null;

            GrassParam grassParam = parameter.getAnnotation(GrassParam.class);
            if (grassParam != null) {
                name = grassParam.value();
                //不能为空，或者默认名字的开头(避免重复)
                if (Utils.isEmpty(name) || name.startsWith(ParameterAttribute.NAME_PREFIX)) {
                    throw new IllegalStateException("The value of @GrassParam cannot be \"\" or starts with _");
                }
            }

            parameterAttributes[i] = new ParameterAttribute(parameter.getType(), name, i);
        }

        return parameterAttributes;
    }
}
