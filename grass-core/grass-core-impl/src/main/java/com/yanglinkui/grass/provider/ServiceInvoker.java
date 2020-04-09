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
import jdk.nashorn.internal.ir.ObjectNode;

import java.lang.reflect.Method;
import java.util.Objects;

public class ServiceInvoker implements Invoker {

    public final Object ref;

    private final Method method;

    private final InvokerAttributes attributes;

    public ServiceInvoker(Object ref, Method method, InvokerAttributes properties) {
        this.ref = ref;
        this.method = method;
        this.attributes = properties;
    }

    @Override
    public Object invoke(Object[] args) throws Throwable {
        return this.method.invoke(this.ref, args);
    }

    @Override
    public InvokerAttributes getAttributes() {
        return this.attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInvoker invoker = (ServiceInvoker) o;
        return Objects.equals(ref, invoker.ref) &&
                Objects.equals(this.attributes.getId(), invoker.getAttributes().getId()) &&
                Objects.equals(this.attributes.getAction(), invoker.getAttributes().getAction());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, method, attributes);
    }
}
