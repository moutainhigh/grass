/*
 * Copyright 2019 Jonas Yang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yanglinkui.grass.provider;

import com.yanglinkui.grass.Invoker;
import com.yanglinkui.grass.InvokerAttributes;
import com.yanglinkui.grass.ServiceUtils;
import com.yanglinkui.grass.annotation.GrassManagement;
import com.yanglinkui.grass.annotation.GrassMethod;
import com.yanglinkui.grass.annotation.GrassService;
import com.yanglinkui.grass.common.Utils;

import java.lang.reflect.Method;
import java.util.*;

public class ServiceBuilder<T> {

    private T ref;

    private Service<T> service;

    public void setHandler(T t) {
        this.ref = t;
    }

    public synchronized Service<T> build() {
        if (service != null) {
            return this.service;
        }

        if (this.ref == null) {
            throw new IllegalStateException("No service object");
        }

        //解析GrassService
        Class<?> clazz = this.ref.getClass();
        GrassService grassService = clazz.getAnnotation(GrassService.class);
        GrassManagement grassManagement = clazz.getAnnotation(GrassManagement.class);
        Service.Type type = grassManagement == null ? Service.Type.PUBLIC : Service.Type.MANAGEMENT;
        if (grassService == null) {
            throw new IllegalStateException("The object[" + clazz + "] is not denoted by @GrassService");
        }

        String prefix = Utils.getOrDefault(grassService.prefix(), grassService.value());

        List<Invoker> invokerList = getInvokerList(clazz, prefix, new LinkedList<>());

        return new ServiceImpl(ref, Collections.unmodifiableList(invokerList), type);
    }

    public List<Invoker> getInvokerList(Class clazz, String prefix, List<Invoker> invokerList) {
        List<Invoker> newlyList = new LinkedList<>();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            GrassMethod grassMethod = method.getAnnotation(GrassMethod.class);
            if (grassMethod == null) {
                continue;
            }

            InvokerAttributes properties = ServiceUtils.getInvokerAttributes(prefix, method);
            ServiceInvoker invoker = new ServiceInvoker(this.ref, method, properties);

            if (!invokerList.contains(invoker)) {
                newlyList.add(invoker);
            }
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null) {
            for (Class<?> interfaceClazz : interfaces) {
                newlyList.addAll(getInvokerList(interfaceClazz, prefix, newlyList));
            }
        }

        return newlyList;
    }

    static class ServiceImpl<T> implements Service<T> {

        private final T ref;

        private final List<Invoker> invokerList;

        private final Type type;

        public ServiceImpl(T ref, List<Invoker> invokerList, Type type) {
            this.ref = ref;
            this.invokerList = invokerList;
            this.type = type;
        }

        public List<Invoker> getInvokerList() {
            return this.invokerList;
        }

        @Override
        public T getObject() {
            return this.ref;
        }

        @Override
        public Type getType() {
            return this.type;
        }
    }

}
