/**
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
package com.yanglinkui.grass.client;

import java.util.concurrent.CompletableFuture;

/**
 * 调用的时候就使用
 * <p>
 * Async async = new Async(客户端实例);
 * Async.Method = async.of(method);
 * CompletableFuture future = method.invoke(参数1, 参数2...)
 * </p>
 *
 */
public class Async {

    private final Client client;

    public Async(Client client) {
        this.client = client;
    }

    public Method of(java.lang.reflect.Method method) {
        return new Method(method);
    }

    public <T> CompletableFuture<T> invoke(java.lang.reflect.Method method, Object... args) {
        return this.client.async(method, args);
    }

    public class Method {

        private final java.lang.reflect.Method method;

        public Method(java.lang.reflect.Method method) {
            this.method = method;
        }

        public <T> CompletableFuture<T> invoke(Object... args) {
            return Async.this.client.async(this.method, args);
        }
    }
}


