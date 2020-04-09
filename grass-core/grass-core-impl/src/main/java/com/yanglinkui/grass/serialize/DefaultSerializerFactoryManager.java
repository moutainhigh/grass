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

package com.yanglinkui.grass.serialize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSerializerFactoryManager implements SerializerFactoryManager {

    private final Map<String, SerializerFactory> factoryList = new ConcurrentHashMap<>();

    @Override
    public SerializerFactory getSerializerFactory(String id) {
        if (id == null) {
            return null;
        }

        return this.factoryList.get(id);
    }

    public void addSerializerFactory(SerializerFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }

        this.factoryList.put(factory.getId(), factory);
        this.factoryList.put(factory.getContentType(), factory);
    }

    public void removeSerializerFactory(String id) {
        if (id == null) {
            return;
        }

        SerializerFactory factory = this.factoryList.get(id);
        if (factory == null) {
            return;
        }

        this.factoryList.remove(factory.getContentType());
        this.factoryList.remove(factory.getId());
    }

}
