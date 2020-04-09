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

package com.yanglinkui.grass.serialize.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanglinkui.grass.serialize.DeserializeProcessor;
import com.yanglinkui.grass.serialize.SerializeProcessor;
import com.yanglinkui.grass.serialize.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;

public class JsonSerializer implements Serializer {

    private final ObjectMapper objectMapper;

    private final List<SerializeProcessor> serializeProcessorList;

    private final List<DeserializeProcessor> deserializeProcessorList;

    public JsonSerializer(ObjectMapper objectMapper, List<SerializeProcessor> serializeProcessorList,
                          List<DeserializeProcessor> deserializeProcessorList) {
        this.objectMapper = objectMapper;
        this.serializeProcessorList = serializeProcessorList;
        this.deserializeProcessorList = deserializeProcessorList;
    }

    @Override
    public byte[] serialize(Object obj) throws IOException {
        for (SerializeProcessor processor : this.serializeProcessorList) {
            if (processor.isSupport(obj)) {
                return processor.serialize(new JsonContext(this.objectMapper), obj);
            }
        }

        return this.objectMapper.writeValueAsBytes(obj);
    }

    @Override
    public void serialize(Object obj, OutputStream out) throws IOException {
        for (SerializeProcessor processor : this.serializeProcessorList) {
            if (processor.isSupport(obj)) {
                processor.serialize(new JsonContext(this.objectMapper), obj, out);
                return;
            }
        }

        this.objectMapper.writeValue(out, obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Object description) throws IOException, ClassNotFoundException {
        if (bytes == null) {
            return null;
        }

        for (DeserializeProcessor processor : this.deserializeProcessorList) {
            if (processor.isSupport(description)) {
                return processor.deserialize(new JsonContext(this.objectMapper), bytes, description);
            }
        }

        JavaType javaType = this.objectMapper.constructType((Type) description);
        return (T) this.objectMapper.readValue(bytes, javaType);
    }

    @Override
    public <T> T deserialize(InputStream in, Object description) throws IOException, ClassNotFoundException {
        if (in == null) {
            return null;
        }

        for (DeserializeProcessor processor : this.deserializeProcessorList) {
            if (processor.isSupport(description)) {
                return processor.deserialize(new JsonContext(this.objectMapper), in, description);
            }
        }

        JavaType javaType = this.objectMapper.constructType((Type) description);
        return (T) this.objectMapper.readValue(in, javaType);    }
}
