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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractSerializerFactory implements SerializerFactory {

    protected final List<SerializeProcessor> serializeProcessorList = new CopyOnWriteArrayList<>();
    protected final List<DeserializeProcessor> deserializeProcessorList = new CopyOnWriteArrayList<>();

    @Override
    public void register(SerializeProcessor processor) {
        this.serializeProcessorList.add(processor);
    }

    @Override
    public void register(DeserializeProcessor processor) {
        this.deserializeProcessorList.add(processor);
    }
}
